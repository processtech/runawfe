package ru.runa.wfe.presentation;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.chat.ChatRoomClassPresentation;
import ru.runa.wfe.commons.error.TokenErrorClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionHistoryClassPresentation;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.ProcessWithTasksClassPresentation;
import ru.runa.wfe.relation.RelationClassPresentation;
import ru.runa.wfe.relation.RelationPairClassPresentation;
import ru.runa.wfe.report.ReportClassPresentation;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.task.TaskObservableClassPresentation;
import ru.runa.wfe.user.ActorClassPresentation;
import ru.runa.wfe.user.ExecutorClassPresentation;
import ru.runa.wfe.user.GroupClassPresentation;

public enum ClassPresentationType {
    NONE(null, ""),
    SYSTEM_LOG(SystemLogClassPresentation.getInstance(), "system_log"),
    EXECUTOR(ExecutorClassPresentation.getInstance(), "executor"),
    ACTOR(ActorClassPresentation.getInstance(), ""),
    GROUP(GroupClassPresentation.getInstance(), "group"),
    RELATION(RelationClassPresentation.getInstance(), "relation"),
    RELATIONPAIR(RelationPairClassPresentation.getInstance(), "relationpair"),
    DEFINITION(DefinitionClassPresentation.getInstance(), "process_definition"),
    DEFINITION_HISTORY(DefinitionHistoryClassPresentation.getInstance(), "process_definition"),
    PROCESS(ProcessClassPresentation.getInstance(), "process"),
    PROCESS_WITH_TASKS(ProcessWithTasksClassPresentation.getInstance(), "process"),
    TASK(TaskClassPresentation.getInstance(), "task"),
    TASK_OBSERVABLE(TaskObservableClassPresentation.getInstance(), "task"),
    REPORTS(ReportClassPresentation.getInstance(), "report"),
    TOKEN_ERRORS(TokenErrorClassPresentation.getInstance(), "error"),
    CHAT_ROOM(ChatRoomClassPresentation.getInstance(), "process");

    private final Class<?> presentationClass;
    private final List<String> restrictions;
    private final boolean withPaging;
    private final FieldDescriptor[] fields;
    private final HashMap<String, Integer> fieldIndexesByName = new HashMap<>();
    private final String localizationKey;
    private int variablePrototypeIndex = -1;
    private int swimlanePrototypeIndex = -1;

    ClassPresentationType(ClassPresentation cp, String localizationKey) {
        if (cp != null) {
            presentationClass = cp.getPresentationClass();
            restrictions = Lists.newArrayList(cp.getRestrictions());
            withPaging = cp.isWithPaging();
            fields = cp.getFields();
            populateFieldIndexesByName();
        } else {
            presentationClass = null;
            restrictions = null;
            withPaging = false;
            fields = null;
        }
        this.localizationKey = localizationKey;
    }

    private void populateFieldIndexesByName() {
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                fieldIndexesByName.put(fields[i].name, i);
                if (fields[i].variablePrototype) {
                    variablePrototypeIndex = i;
                }
                if (fields[i].swimlanePrototype) {
                    swimlanePrototypeIndex = i;
                }
            }
        }
    }

    public Class<?> getPresentationClass() {
        return presentationClass;
    }

    public List<String> getRestrictions() {
        return restrictions;
    }

    public boolean isWithPaging() {
        return withPaging;
    }

    public FieldDescriptor[] getFields() {
        return fields;
    }

    public int getFieldIndex(String name) {
        Integer result = fieldIndexesByName.get(name);
        if (result != null) {
            return result;
        } else {
            throw new InternalApplicationException("Field '" + name + "' is not found in " + this);
        }
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    public int getVariablePrototypeIndex() {
        return variablePrototypeIndex;
    }

    public int getSwimlanePrototypeIndex() {
        return swimlanePrototypeIndex;
    }
}
