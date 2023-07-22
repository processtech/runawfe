package ru.runa.wfe.commons.dbmigration.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Maps;

public class TransitionLogPatch extends DbMigration {

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ProcessLogDao processLogDao;

    @Override
    public void executeDML(Session session) throws Exception {
        String q;
        // JBPM_PASSTRANS
        q = "DELETE FROM JBPM_PASSTRANS WHERE TRANSITION_ID IS NULL OR NODE_ID IS NULL OR PROCESS_ID IS NULL";
        log.info("Deleted bad JBPM_PASSTRANS " + session.createSQLQuery(q).executeUpdate());
        Calendar limitDate = Calendar.getInstance();
        limitDate.add(Calendar.MONTH, -1);
        log.info("Deleting old transitions [before " + CalendarUtil.formatDate(limitDate) + "]");
        q = "DELETE FROM JBPM_PASSTRANS WHERE PROCESS_ID IN (SELECT ID FROM BPM_PROCESS WHERE END_DATE < :limitDate)";
        log.info("Deleted old JBPM_PASSTRANS " + session.createSQLQuery(q).setCalendar("limitDate", limitDate).executeUpdate());
        log.info("Processing graph history");
        q = "SELECT PROCESS_ID, NODE_ID, TRANSITION_ID FROM JBPM_PASSTRANS ORDER BY ID ASC";
        ScrollableResults scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        int failed = 0;
        int success = 0;
        Map<Deployment, Date> failedDeployments = Maps.newHashMap();
        while (scrollableResults.next()) {
            Process process = processDao.get(((Number) scrollableResults.get(0)).longValue());
            Deployment deployment = process.getDeployment();
            try {
                ProcessDefinition definition = processDefinitionLoader.getDefinition(deployment.getId());
                try {
                    Node node = definition.getNodeNotNull((String) scrollableResults.get(1));
                    Transition transition = node.getLeavingTransitionNotNull((String) scrollableResults.get(2));
                    TransitionLog transitionLog = new TransitionLog(transition);
                    transitionLog.setProcessId(process.getId());
                    transitionLog.setTokenId(process.getRootToken().getId());
                    transitionLog.setCreateDate(new Date());
                    processLogDao.create(transitionLog);
                    success++;
                } catch (Exception e) {
                    log.warn(e);
                    failed++;
                }
            } catch (InvalidDefinitionException e) {
                if (!failedDeployments.containsKey(deployment)) {
                    failedDeployments.put(deployment, process.getEndDate());
                    log.error("Unable to restore history for " + deployment + ": " + e);
                } else {
                    Date endDate = failedDeployments.get(deployment);
                    if (endDate != null && (process.getEndDate() == null || endDate.before(process.getEndDate()))) {
                        failedDeployments.put(deployment, process.getEndDate());
                    }
                }
            }
        }
        log.info("-------------------- RESULT OF " + getClass());
        for (Map.Entry<Deployment, Date> entry : failedDeployments.entrySet()) {
            log.warn("Unparsed definition " + entry.getKey() + ", last process end date = " + entry.getValue());
        }
        log.info("Reverted history [for parsed definitions] result: success " + success + ", failed " + failed);
    }

    @Override
    protected void executeDDLAfter() {
        executeUpdates(getDDLDropTable("JBPM_PASSTRANS"));
    }
}
