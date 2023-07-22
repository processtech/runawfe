/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.audit.logic;

import com.google.common.base.Preconditions;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

/**
 * Audit logic.
 * 
 * @author dofs
 * @since 4.0
 */
public class AuditLogic extends CommonLogic {
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ProcessLogDao processLogDao;
    @Autowired
    private NodeProcessDao nodeProcessDao;

    public void login(User user) {
        permissionDao.checkAllowed(user, Permission.LOGIN, SecuredSingleton.SYSTEM);
    }

    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        Preconditions.checkNotNull(filter.getProcessId(), "filter.processId");
        ru.runa.wfe.execution.Process process = processDao.getNotNull(filter.getProcessId());
        permissionDao.checkAllowed(user, Permission.READ, process);
        ProcessLogs result = new ProcessLogs(filter.getProcessId());
        List<ProcessLog> logs = processLogDao.getAll(filter);
        result.addLogs(logs, filter.isIncludeSubprocessLogs());
        if (filter.isIncludeSubprocessLogs()) {
            for (ru.runa.wfe.execution.Process subprocess : nodeProcessDao.getSubprocessesRecursive(process)) {
                ProcessLogFilter subprocessFilter = new ProcessLogFilter(subprocess.getId());
                subprocessFilter.setSeverities(filter.getSeverities());
                logs = processLogDao.getAll(subprocessFilter);
                result.addLogs(logs, filter.isIncludeSubprocessLogs());
            }
        }
        return result;
    }

    public Object getProcessLogValue(User user, Long logId) {
        Preconditions.checkNotNull(logId, "logId");
        ProcessLog processLog = processLogDao.getNotNull(logId);
        permissionDao.checkAllowed(user, Permission.READ, SecuredObjectType.PROCESS, processLog.getProcessId());
        return processLog.getBytesObject();
    }

    /**
     * Load system logs according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs.
     * @return Loaded system logs.
     */
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        permissionDao.checkAllowed(user, Permission.VIEW_LOGS, SecuredSingleton.SYSTEM);
        PresentationConfiguredCompiler<SystemLog> compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getBatch();
    }

    /**
     * Load system logs count according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs count.
     * @return System logs count.
     */
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        permissionDao.checkAllowed(user, Permission.VIEW_LOGS, SecuredSingleton.SYSTEM);
        PresentationConfiguredCompiler<SystemLog> compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

    /**
     *  Clean process logs before date
     *
     * @param user Requester user.
     * @param date Date
     */
    public void cleanProcessLogsBeforeDate(User user, Date date) {
        if (!executorDao.isAdministrator(user.getActor())) {
            throw new AuthorizationException("Only administrator can clean process logs");
        }
        processLogDao.deleteBeforeDate(user, date);
    }
}
