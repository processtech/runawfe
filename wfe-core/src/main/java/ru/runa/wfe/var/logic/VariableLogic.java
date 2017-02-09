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
package ru.runa.wfe.var.logic;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ConvertToSimpleVariables;
import ru.runa.wfe.execution.ConvertToSimpleVariablesContext;
import ru.runa.wfe.execution.ConvertToSimpleVariablesResult;
import ru.runa.wfe.execution.ConvertToSimpleVariablesUnrollContext;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionVariableProvider;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dao.VariableLoaderFromMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Variables access logic.
 *
 * @author Dofs
 * @since 2.0
 */
public class VariableLogic extends WFCommonLogic {
    @Autowired
    private AuditLogic auditLogic;
    @Autowired
    private VariableCreator variableCreator;

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfVariable> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            WfVariable variable = executionContext.getVariable(variableDefinition.getName(), false);
            if (!Utils.isNullOrEmpty(variable.getValue())) {
                result.add(variable);
            }
        }
        return result;
    }

    public WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter) throws ProcessDoesNotExistException {
        if (filter.getCreateDateFrom() == null) {
            return getHistoricalVariableOnDate(user, filter);
        } else {
            return getHistoricalVariableOnRange(user, filter);
        }
    }

    public WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter, Set<String> variables)
            throws ProcessDoesNotExistException {
        return filterHistoricalVariables(variables, getHistoricalVariables(user, filter));
    }

    public WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        ProcessLogFilter filter = new ProcessLogFilter();
        filter.setProcessId(processId);
        ProcessLogs processLogs = auditLogic.getProcessLogs(user, filter);
        if (taskId == null || Objects.equal(taskId, 0L)) { // Start process form.
            NodeLeaveLog leaveLog = processLogs.getFirstOrNull(NodeLeaveLog.class);
            if (leaveLog == null) {
                throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems completed");
            }
            filter.setCreateDateTo(leaveLog.getCreateDate());
            filter.setIdTo(leaveLog.getId());
            return getHistoricalVariableOnDate(user, filter);
        }
        Date taskCreateDate = null;
        Date taskCompletePressedDate = null;
        Date taskEndDate = null;
        Long tokenId = null;
        for (TaskCreateLog createLog : processLogs.getLogs(TaskCreateLog.class)) {
            if (Objects.equal(createLog.getTaskId(), taskId)) {
                tokenId = createLog.getTokenId();
                break;
            }
        }
        filter.setTokenId(tokenId);
        ProcessLogs tokenLogs = auditLogic.getProcessLogs(user, filter);
        for (ProcessLog log : tokenLogs.getLogs()) {
            if (log instanceof TaskCreateLog && Objects.equal(((TaskCreateLog) log).getTaskId(), taskId)) {
                taskCreateDate = log.getCreateDate();
            }
            if (log instanceof VariableLog && taskCreateDate != null && taskCompletePressedDate == null) {
                taskCompletePressedDate = log.getCreateDate();
            }
            if (log instanceof TaskEndLog && Objects.equal(((TaskEndLog) log).getTaskId(), taskId)) {
                taskEndDate = log.getCreateDate();
                break;
            }
        }
        if (taskCreateDate == null) {
            throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems started");
        }
        if (taskEndDate == null) {
            throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems completed");
        }
        filter = new ProcessLogFilter(processId);
        filter.setCreateDateTo(taskEndDate);
        Calendar dateFrom = CalendarUtil.dateToCalendar(taskCompletePressedDate != null ? taskCompletePressedDate : taskEndDate);
        dateFrom.add(Calendar.MILLISECOND, -100);
        filter.setCreateDateFrom(dateFrom.getTime());
        WfVariableHistoryState completeTaskState = getHistoricalVariableOnRange(user, filter);
        return completeTaskState;
    }

    public WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId, Set<String> variables)
            throws ProcessDoesNotExistException {
        return filterHistoricalVariables(variables, getHistoricalVariables(user, processId, taskId));
    }

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        return executionContext.getVariable(variableName, true);
    }

    public WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) {
        Task task = taskDAO.getNotNull(taskId);
        if (task.getIndex() == null) {
            return getVariable(user, processId, variableName);
        }
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        MultiTaskNode node = (MultiTaskNode) processDefinition.getNodeNotNull(task.getNodeId());
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (Objects.equal(mapping.getMappedName(), variableName) || variableName.startsWith(mapping.getMappedName() + UserType.DELIM)) {
                String mappedVariableName = variableName.replaceFirst(mapping.getMappedName(), mapping.getName()
                        + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex() + VariableFormatContainer.COMPONENT_QUALIFIER_END);
                WfVariable variable = getVariable(user, processId, mappedVariableName);
                if (variable == null) {
                    return null;
                }
                VariableDefinition mappedDefinition = new VariableDefinition(variableName, null, variable.getDefinition());
                return new WfVariable(mappedDefinition, variable.getValue());
            }
        }
        return getVariable(user, processId, variableName);
    }

    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Process process = processDAO.getNotNull(processId);
        // TODO check ProcessPermission.UPDATE
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        processLogDAO.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPDATE_VARIABLES), process, null);
        executionContext.setVariableValues(variables);
    }

    private WfVariableHistoryState getHistoricalVariableOnRange(User user, ProcessLogFilter filter) {
        HashSet<String> simpleVariablesChanged = Sets.<String> newHashSet();
        // Next call is for filling simpleVariablesChanged structure.
        loadSimpleVariablesState(user, processDAO.getNotNull(filter.getProcessId()), filter, simpleVariablesChanged);
        Date dateFrom = filter.getCreateDateFrom();
        filter.setCreateDateFrom(null);
        WfVariableHistoryState toState = getHistoricalVariableOnDate(user, filter);
        filter.setCreateDateTo(dateFrom);
        WfVariableHistoryState fromState = getHistoricalVariableOnDate(user, filter);
        return new WfVariableHistoryState(fromState.getVariables(), toState.getVariables(), simpleVariablesChanged);
    }

    private WfVariableHistoryState getHistoricalVariableOnDate(User user, ProcessLogFilter filter) {
        List<WfVariable> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(filter.getProcessId());
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        Set<String> simpleVariablesChanged = Sets.newHashSet();
        Map<Process, Map<String, Variable<?>>> processStateOnTime = getProcessStateOnTime(user, process, filter, simpleVariablesChanged);
        VariableLoader loader = new VariableLoaderFromMap(processStateOnTime);
        ProcessDefinition processDefinition = getDefinition(process);
        BaseProcessVariableLoader baseProcessVariableLoader = new BaseProcessVariableLoader(loader, process, processDefinition);
        removeSyncVariablesInBaseProcessMode(processStateOnTime, baseProcessVariableLoader);
        ConvertToSimpleVariables operation = new ConvertToSimpleVariables();
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            String name = variableDefinition.getName();
            WfVariable variable = baseProcessVariableLoader.get(name);
            if (variable != null) {
                ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(variableDefinition, variable.getValue());
                for (ConvertToSimpleVariablesResult simpleVariable : variableDefinition.getFormatNotNull().processBy(operation, context)) {
                    result.add(new WfVariable(simpleVariable.variableDefinition, simpleVariable.value));
                }
            }
        }
        return new WfVariableHistoryState(Lists.<WfVariable> newArrayList(), result, simpleVariablesChanged);
    }

    /**
     * Removes from processes state variables, which is in sync state with base process and BaseProcessMode is on.
     *
     * @param processStateOnTime
     *            Loaded from history state for process and all it's base processes.
     * @param baseProcessVariableLoader
     *            Component for loading variables with base process variable state support.
     */
    private void removeSyncVariablesInBaseProcessMode(Map<Process, Map<String, Variable<?>>> processStateOnTime,
            BaseProcessVariableLoader baseProcessVariableLoader) {
        ConvertToSimpleVariables operation = new ConvertToSimpleVariables();
        for (Process process : processStateOnTime.keySet()) {
            if (!baseProcessVariableLoader.getSubprocessSyncCache().isInBaseProcessIdMode(process)) {
                continue;
            }
            ProcessDefinition processDefinition = getDefinition(process);
            for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                if (baseProcessVariableLoader.getSubprocessSyncCache().getParentProcessSyncVariableDefinition(processDefinition, process,
                        variableDefinition) == null) {
                    continue;
                }
                WfVariable variable = baseProcessVariableLoader.get(variableDefinition.getName());
                if (variable != null) {
                    ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(variableDefinition, variable.getValue());
                    for (ConvertToSimpleVariablesResult simpleVariables : variableDefinition.getFormatNotNull().processBy(operation, context)) {
                        processStateOnTime.get(process).remove(simpleVariables.variableDefinition.getName());
                    }
                }
            }
        }
    }

    /**
     * Filter historical variables result to return only specified variables.
     *
     * @param variables
     *            Variables to return.
     * @param historicalVariables
     *            Full historical variables state.
     * @return Returns specified variables historical state.
     */
    private WfVariableHistoryState filterHistoricalVariables(Set<String> variables, WfVariableHistoryState historicalVariables) {
        IVariableProvider toDateProvider = new MapVariableProvider(historicalVariables.getVariables(), true);
        IVariableProvider fromDateProvider = new MapVariableProvider(historicalVariables.getStartDateRangeVariables(), true);
        List<WfVariable> toDateResult = Lists.newArrayList();
        List<WfVariable> startDateRangeResult = Lists.newArrayList();
        for (String variableName : variables) {
            WfVariable toDateState = toDateProvider.getVariable(variableName);
            WfVariable fromDateState = fromDateProvider.getVariable(variableName);
            if (toDateState != null) {
                toDateResult.add(toDateState);
            }
            if (fromDateState != null) {
                startDateRangeResult.add(fromDateState);
            }
        }
        return new WfVariableHistoryState(startDateRangeResult, toDateResult, historicalVariables.getSimpleVariablesChanged());
    }

    /**
     * Load process and all base processes state from logs according to filter.
     *
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading process state.
     * @param filter
     *            Filter for loaded process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from process to process variables, loaded according to process filter.
     */
    private Map<Process, Map<String, Variable<?>>> getProcessStateOnTime(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        Map<Process, Map<String, Object>> processToVariables = loadSimpleVariablesState(user, process, filter, simpleVariablesChanged);
        Map<Process, Map<String, Variable<?>>> result = Maps.newHashMap();
        for (Process currentProcess : processToVariables.keySet()) {
            Map<String, Object> processVariables = processToVariables.get(currentProcess);
            result.put(currentProcess, Maps.<String, Variable<?>> newHashMap());
            for (Process varProcess = currentProcess; varProcess != null; varProcess = getBaseProcess(user, varProcess)) {
                ProcessDefinition definition = getDefinition(varProcess);
                for (String variableName : processVariables.keySet()) {
                    VariableDefinition variableDefinition = definition.getVariable(variableName, false);
                    if (variableDefinition == null) {
                        continue;
                    }
                    Object value = processVariables.get(variableName);
                    if (value instanceof String) {
                        value = variableDefinition.getFormatNotNull().parse((String) value);
                    }
                    Variable<?> variable = variableCreator.create(varProcess, variableDefinition, value);
                    variable.setValue(new ExecutionContext(definition, varProcess), value, variableDefinition.getFormatNotNull());
                    result.get(currentProcess).put(variableName, variable);
                }
            }
        }
        return result;
    }

    /**
     * Load simple (as it stored in database/logs) variables state for process and all his base processes.
     *
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading variables.
     * @param filter
     *            Filter for loading process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from process to it simple variables state.
     */
    private Map<Process, Map<String, Object>> loadSimpleVariablesState(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        Map<Process, Map<String, Object>> processToVariables = Maps.newHashMap();
        for (Process loadingProcess = process; loadingProcess != null; loadingProcess = getBaseProcess(user, loadingProcess)) {
            processToVariables.put(loadingProcess, loadVariablesForProcessFromLogs(user, loadingProcess, filter, simpleVariablesChanged));
        }
        return processToVariables;
    }

    /**
     * Load simple variables (as it stored in database/logs) for process with specified filter parameters.
     *
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading variables.
     * @param filter
     *            Filter for loading process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from simple process variable name to it last known value.
     */
    private Map<String, Object> loadVariablesForProcessFromLogs(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        Long processId = filter.getProcessId();
        try {
            filter.setProcessId(process.getId());
            HashMap<String, Object> processVariables = Maps.<String, Object> newHashMap();
            for (VariableLog variableLog : auditLogic.getProcessLogs(user, filter).getLogs(VariableLog.class)) {
                String variableName = variableLog.getVariableName();
                if (!(variableLog instanceof VariableCreateLog) || !Utils.isNullOrEmpty(((VariableCreateLog) variableLog).getVariableNewValue())) {
                    simpleVariablesChanged.add(variableName);
                }
                if (variableLog instanceof VariableDeleteLog) {
                    processVariables.remove(variableName);
                    continue;
                }
                processVariables.put(variableName, variableLog.getVariableNewValue());
            }
            return processVariables;
        } finally {
            filter.setProcessId(processId);
        }
    }

    private Process getBaseProcess(User user, Process process) {
        if (Strings.isNullOrEmpty(SystemProperties.getBaseProcessIdVariableName())) {
            return null;
        }
        IVariableProvider processVariableProvider = new ExecutionVariableProvider(new ExecutionContext(getDefinition(process), process));
        final Long baseProcessId = (Long) processVariableProvider.getValue(SystemProperties.getBaseProcessIdVariableName());
        if (baseProcessId == null) {
            return null;
        }
        return processDAO.getNotNull(baseProcessId);
    }
}
