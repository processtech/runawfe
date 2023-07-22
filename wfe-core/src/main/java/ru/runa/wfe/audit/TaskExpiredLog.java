package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

/**
 * Logging task completion by timer.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "9")
public class TaskExpiredLog extends TaskCancelledLog {
    private static final long serialVersionUID = 1L;

    public TaskExpiredLog() {
    }

    public TaskExpiredLog(Task task, TaskCompletionInfo completionInfo) {
        super(task, completionInfo);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskExpiredLog(this);
    }
}
