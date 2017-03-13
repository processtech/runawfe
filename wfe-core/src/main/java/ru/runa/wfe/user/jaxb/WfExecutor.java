package ru.runa.wfe.user.jaxb;

import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

public class WfExecutor extends Executor {
    private static final long serialVersionUID = 1L;
    private String executorClassName;

    public String getExecutorClassName() {
        return executorClassName;
    }

    public void setExecutorClassName(String executorClassName) {
        this.executorClassName = executorClassName;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        if (Actor.class.getName().equals(executorClassName)) {
            return SecuredObjectType.ACTOR;
        } else {
            return SecuredObjectType.GROUP;
        }
    }

}
