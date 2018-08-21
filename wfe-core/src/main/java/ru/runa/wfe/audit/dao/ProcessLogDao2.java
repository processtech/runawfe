package ru.runa.wfe.audit.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedProcess;
import ru.runa.wfe.execution.BaseProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.ProcessDao2;
import ru.runa.wfe.lang.ProcessDefinition;

@Component
@CommonsLog
public class ProcessLogDao2 extends GenericDao2<IProcessLog, ProcessLog, ProcessLogDao, ArchivedProcessLog, ArchivedProcessLogDao> {

    private ProcessDao2 processDao2;
    private ProcessLogAwareDao customizationDao;

    @Autowired
    protected ProcessLogDao2(ProcessLogDao dao1, ArchivedProcessLogDao dao2, ProcessDao2 processDao2, ProcessLogAwareDao customizationDao) {
        super(dao1, dao2);
        this.processDao2 = processDao2;
        this.customizationDao = customizationDao;
    }

    public List<? extends IProcessLog> getAll(@NonNull BaseProcess process) {
        if (process.isArchive()) {
            return dao2.getAll(process.getId());
        } else {
            return dao1.getAll(process.getId());
        }
    }

    /**
     * Called with TemporaryGroup.processId; other contexts have BaseProcess instance available.
     */
    public List<? extends IProcessLog> getAll(@NonNull Long processId) {
        return getAll(processDao2.getNotNull(processId));
    }

    public List<IProcessLog> getAll(final ProcessLogFilter filter) {
        val process = filter.getProcessId() != null
                ? processDao2.get(filter.getProcessId())
                : null;
        if (process == null) {
            val current = dao1.getAll(filter);
            val archived = dao2.getAll(filter);
            val result = new ArrayList<IProcessLog>(current.size() + archived.size());
            result.addAll(current);
            result.addAll(archived);
            result.sort(new Comparator<IProcessLog>() {
                @Override
                public int compare(IProcessLog o1, IProcessLog o2) {
                    return Long.compare(o1.getId(), o2.getId());
                }
            });
            return result;
        } else if (!process.isArchive()) {
            return dao1.getAll(filter);
        } else if (filter.getTokenId() != null) {
            // Archive does not have TOKEN_ID field.
            return Collections.emptyList();
        } else {
            return dao2.getAll(filter);
        }
    }

    public List<? extends IProcessLog> get(@NonNull BaseProcess process, ProcessDefinition definition) {
        if (process.isArchive()) {
            return dao2.get((ArchivedProcess) process, definition);
        } else {
            return dao1.get((Process) process, definition);
        }
    }

    public void addLog(ProcessLog processLog, Process process, Token token) {
        processLog.setProcessId(process.getId());
        if (token == null) {
            token = process.getRootToken();
        }
        processLog.setTokenId(token.getId());
        if (processLog.getNodeId() == null) {
            processLog.setNodeId(token.getNodeId());
        }
        processLog.setCreateDate(new Date());
        dao1.create(processLog);
        registerInCustomizationDao(processLog, process, token);
    }

    private void registerInCustomizationDao(ProcessLog processLog, Process process, Token token) {
        try {
            customizationDao.addLog(processLog, process, token);
        } catch (Throwable e) {
            log.warn("Custom log handler throws exception", e);
        }
    }
}
