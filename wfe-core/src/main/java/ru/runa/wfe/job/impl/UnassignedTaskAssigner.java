package ru.runa.wfe.job.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.logic.TaskAssigner;

public class UnassignedTaskAssigner {
    protected final Log log = LogFactory.getLog(getClass());
    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private TaskDao taskDao;

    @Transactional
    public void execute() {
        List<Task> unassignedTasks = taskDao.findUnassignedTasksInActiveProcesses();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            taskAssigner.assignTask(unassignedTask);
        }
    }

}
