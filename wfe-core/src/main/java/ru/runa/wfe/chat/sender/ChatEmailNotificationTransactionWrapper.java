package ru.runa.wfe.chat.sender;

import java.util.List;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Created on 23.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
@Transactional(readOnly = true)
@Component
@MonitoredWithSpring
public class ChatEmailNotificationTransactionWrapper {

    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ProcessDefinitionLoader definitionLoader;

    public List<Actor> getAllActorsWithPagination(int pageIndex, int pageSize) {
        return executorDao.getAllActorsWithPagination(pageIndex, pageSize);
    }

    public boolean isProcessReadAllowed(Actor actor, Process process) {
        return permissionDao.isAllowed(actor, Permission.READ, process.getSecuredObjectType(), process.getIdentifiableId());
    }

    public ProcessDefinition getProcessDefinition(Process process) {
        return definitionLoader.getDefinition(process);
    }
}
