package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.ProcessStartLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.StartNode;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
import ru.runa.wfe.var.VariableProvider;

public class ProcessFactory {
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private NodeProcessDao nodeProcessDao;
    @Autowired
    private SwimlaneDao swimlaneDao;

    private static final Map<Permission, Permission> DEFINITION_TO_PROCESS_PERMISSION_MAP = new HashMap<Permission, Permission>() {{
        put(Permission.READ_PROCESS, Permission.READ);
        put(Permission.CANCEL_PROCESS, Permission.CANCEL);
    }};

    private Set<Permission> getProcessPermissions(Executor executor, ProcessDefinition processDefinition) {
        Set<Permission> result = new HashSet<>();
        for (Map.Entry<Permission, Permission> kv : DEFINITION_TO_PROCESS_PERMISSION_MAP.entrySet()) {
            // Using isAllowed() because it takes DEFINITIONS list & executor groups into account.
            if (permissionDao.isAllowed(executor, kv.getKey(), processDefinition.getDeployment(), false)) {
                result.add(kv.getValue());
            }
        }
        return result;
    }

    /**
     * Creates and starts a new process for the given process definition, puts the root-token (=main path of execution) in the start state and
     * executes the initial node.
     *
     * @param variables
     *            will be inserted into the context variables after the context submodule has been created and before the process-start event is
     *            fired, which is also before the execution of the initial node.
     */
    public Process startProcess(ProcessDefinition processDefinition, Map<String, Object> variables, Actor actor, String transitionName,
            Map<String, Object> transientVariables) {
        Preconditions.checkNotNull(actor, "can't start a process when actor is null");
        ExecutionContext executionContext = createProcessInternal(processDefinition, variables, actor, null, transientVariables, transitionName);
        grantProcessPermissions(processDefinition, executionContext.getProcess(), actor);
        startProcessInternal(executionContext, transitionName);
        return executionContext.getProcess();
    }

    private void grantProcessPermissions(ProcessDefinition processDefinition, Process process, Actor actor) {
        boolean permissionsAreSetToProcessStarter = false;
        Executor processStarter = executorDao.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
        Set<Permission> processStarterPermissions = getProcessPermissions(processStarter, processDefinition);
        for (Executor executor : permissionDao.getExecutorsWithPermission(processDefinition.getDeployment())) {
            Set<Permission> permissions = getProcessPermissions(executor, processDefinition);
            if (Objects.equal(actor, executor)) {
                permissions = CollectionUtil.unionSet(permissions, processStarterPermissions);
                permissionsAreSetToProcessStarter = true;
            }
            if (permissions.size() > 0) {
                permissionDao.setPermissions(executor, permissions, process);
            }
        }
        if (!permissionsAreSetToProcessStarter) {
            permissionDao.setPermissions(actor, processStarterPermissions, process);
        }
    }

    public Process createSubprocess(ExecutionContext parentExecutionContext, ProcessDefinition processDefinition, Map<String, Object> variables,
            int index, boolean validate) {
        Process parentProcess = parentExecutionContext.getProcess();
        SubprocessNode subProcessNode = (SubprocessNode) parentExecutionContext.getNode();
        ExecutionContext subExecutionContext = createProcessInternal(processDefinition, variables, null, parentProcess, null, null);
        nodeProcessDao.create(new NodeProcess(subProcessNode, parentExecutionContext.getToken(), subExecutionContext.getProcess(), index));
        if (validate) {
            validateVariables(subExecutionContext, new ExecutionVariableProvider(subExecutionContext), processDefinition,
                    processDefinition.getStartStateNotNull().getNodeId(), variables);
        }
        return subExecutionContext.getProcess();
    }

    public void startSubprocess(ExecutionContext parentExecutionContext, ExecutionContext executionContext) {
        parentExecutionContext
                .addLog(new SubprocessStartLog(parentExecutionContext.getNode(), parentExecutionContext.getToken(), executionContext.getProcess()));
        grantSubprocessPermissions(executionContext.getProcessDefinition(), executionContext.getProcess(), parentExecutionContext.getProcess());
        startProcessInternal(executionContext, null);
    }

    protected void validateVariables(ExecutionContext executionContext, VariableProvider variableProvider, ProcessDefinition processDefinition,
            String nodeId, Map<String, Object> variables) throws ValidationException {
        Interaction interaction = processDefinition.getInteractionNotNull(nodeId);
        if (interaction.getValidationData() != null) {
            ValidatorContext context = ValidatorManager.getInstance().validate(executionContext, variableProvider, interaction.getValidationData(),
                    variables);
            if (context.hasGlobalErrors() || context.hasFieldErrors()) {
                throw new ValidationException(context.getFieldErrors(), context.getGlobalErrors());
            }
        }
    }

    private void grantSubprocessPermissions(ProcessDefinition processDefinition, Process subProcess, Process parentProcess) {
        Set<Executor> executors = new HashSet<>();
        executors.addAll(permissionDao.getExecutorsWithPermission(processDefinition.getDeployment()));
        executors.addAll(permissionDao.getExecutorsWithPermission(parentProcess));
        for (Executor executor : executors) {
            List<Permission> permissionsByParentProcess = permissionDao.getIssuedPermissions(executor, parentProcess);
            Set<Permission> permissionsByDefinition = getProcessPermissions(executor, processDefinition);
            Set<Permission> permissions = CollectionUtil.unionSet(permissionsByParentProcess, permissionsByDefinition);
            if (permissions.size() > 0) {
                permissionDao.setPermissions(executor, permissions, subProcess);
            }
        }
    }

    private ExecutionContext createProcessInternal(ProcessDefinition processDefinition, Map<String, Object> variables, Actor actor,
            Process parentProcess, Map<String, Object> transientVariables, String transitionName) {
        Preconditions.checkNotNull(processDefinition, "can't create a process when processDefinition is null");
        Process process = new Process(processDefinition.getDeployment());
        Token rootToken = new Token(processDefinition, process);
        process.setRootToken(rootToken);
        processDao.create(process);
        if (parentProcess != null) {
            process.setParentId(parentProcess.getId());
            process.setExternalData(parentProcess.getExternalData());
        }
        process.setHierarchyIds(
                ProcessHierarchyUtils.createHierarchy(parentProcess != null ? parentProcess.getHierarchyIds() : null, process.getId()));
        ExecutionContext executionContext = new ExecutionContext(processDefinition, rootToken);
        if (actor != null) {
            executionContext.addLog(new ProcessStartLog(actor));
            executionContext.addLog(new TaskCreateLog(process, processDefinition.getStartStateNotNull()));
            executionContext.addLog(new TaskAssignLog(process, processDefinition.getStartStateNotNull(), actor));
        }
        if (transientVariables != null) {
            for (Map.Entry<String, Object> entry : transientVariables.entrySet()) {
                executionContext.setTransientVariable(entry.getKey(), entry.getValue());
            }
        }
        executionContext.setVariableValues(variables);
        if (actor != null) {
            StartNode startNode = executionContext.getProcessDefinition().getStartStateNotNull();
            TaskDefinition taskDefinition = startNode.getFirstTaskNotNull();
            if (startNode.getFirstTaskNotNull().isReassignSwimlaneToTaskPerformer()) {
                SwimlaneDefinition swimlaneDefinition = processDefinition.getStartStateNotNull().getFirstTaskNotNull().getSwimlane();
                Swimlane swimlane = swimlaneDao.findOrCreate(process, swimlaneDefinition);
                swimlane.assignExecutor(executionContext, actor, false);
            }
            executionContext.addLog(new TaskEndLog(process, processDefinition.getStartStateNotNull(), actor, transitionName));
        }
        return executionContext;
    }

    private void startProcessInternal(ExecutionContext executionContext, String transitionName) {
        // execute the start node
        StartNode startNode = executionContext.getProcessDefinition().getStartStateNotNull();
        // startNode.enter(executionContext);
        Transition transition = null;
        if (transitionName != null) {
            transition = executionContext.getProcessDefinition().getStartStateNotNull().getLeavingTransitionNotNull(transitionName);
        }
        startNode.leave(executionContext, transition);
    }

}
