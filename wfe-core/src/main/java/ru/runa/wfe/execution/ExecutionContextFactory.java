package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public class ExecutionContextFactory {

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Token token) {
        return new ExecutionContext(processDefinition, token);
    }

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Process process) {
        return new ExecutionContext(processDefinition, process);
    }

    public ExecutionContext createExecutionContext(ProcessDefinition processDefinition, Task task) {
        return new ExecutionContext(processDefinition, task);
    }

}
