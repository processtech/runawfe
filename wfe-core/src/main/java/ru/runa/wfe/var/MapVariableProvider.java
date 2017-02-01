package ru.runa.wfe.var;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.ConvertToSimpleVariables;
import ru.runa.wfe.execution.ConvertToSimpleVariablesContext;
import ru.runa.wfe.execution.ConvertToSimpleVariablesResult;
import ru.runa.wfe.execution.ConvertToSimpleVariablesUnrollContext;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class MapVariableProvider extends AbstractVariableProvider {
    protected final Map<String, Object> values = Maps.newHashMap();
    private final ProcessDefinition processDefinition;

    public MapVariableProvider(Map<String, ? extends Object> variables) {
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) variables).entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        processDefinition = null;
    }

    /**
     * Creates instance for specified variables. May unroll variables to database related (simple) values, stored to database.
     *
     * @param variables
     *            Variables, accessible from this instance.
     * @param unroll
     *            Flag, equals true, if variables must be unrolled to database related (simple) values and false otherwise.
     * @param processDefinition
     *            Process definition, for return process related info from variable provider.
     */
    public MapVariableProvider(Map<String, WfVariable> variables, boolean unroll, ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        for (Map.Entry<String, WfVariable> entry : variables.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
            if (unroll) {
                VariableDefinition definition = entry.getValue().getDefinition();
                ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(definition, entry.getValue().getValueNoDefault());
                for (ConvertToSimpleVariablesResult unrolled : definition.getFormatNotNull().processBy(new ConvertToSimpleVariables(), context)) {
                    values.put(unrolled.variableDefinition.getName(), new WfVariable(unrolled.variableDefinition, unrolled.value));
                }
            }
        }
    }

    /**
     * Creates instance for specified variables. May unroll variables to database related (simple) values, stored to database.
     *
     * @param variables
     *            Variables, accessible from this instance.
     * @param unroll
     *            Flag, equals true, if variables must be unrolled to database related (simple) values and false otherwise.
     */
    public MapVariableProvider(Map<String, WfVariable> variables, boolean unroll) {
        this(variables, unroll, null);
    }

    /**
     * Creates instance for specified variables. May unroll variables to database related (simple) values, stored to database.
     *
     * @param variables
     *            Variables, accessible from this instance.
     * @param unroll
     *            Flag, equals true, if variables must be unrolled to database related (simple) values and false otherwise.
     */
    public MapVariableProvider(List<WfVariable> variables, boolean unroll) {
        this(convertVariablesListToMap(variables), unroll, null);
    }

    public void add(String variableName, Object object) {
        values.put(variableName, object);
        if (object instanceof UserTypeMap) {
            UserTypeMap userTypeMap = (UserTypeMap) object;
            Map<String, Object> expanded = userTypeMap.expand(variableName);
            for (Map.Entry<String, Object> entry : expanded.entrySet()) {
                if (entry.getValue() != null) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void add(WfVariable variable) {
        values.put(variable.getDefinition().getName(), variable);
    }

    public Object remove(String variableName) {
        return values.remove(variableName);
    }

    @Override
    public Long getProcessDefinitionId() {
        return processDefinition == null ? null : processDefinition.getId();
    }

    @Override
    public String getProcessDefinitionName() {
        return processDefinition == null ? null : processDefinition.getName();
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        return processDefinition == null ? null : processDefinition;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String name) {
        return processDefinition == null ? null : processDefinition.getUserType(name);
    }

    @Override
    public Object getValue(String variableName) {
        Object object = values.get(variableName);
        if (object instanceof WfVariable) {
            return ((WfVariable) object).getValueNoDefault();
        }
        return object;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        if (values.containsKey(variableName)) {
            Object object = values.get(variableName);
            if (object instanceof WfVariable) {
                return (WfVariable) object;
            }
            throw new InternalApplicationException("Only value found by " + variableName);
        }
        return null;
    }

    @Override
    public AbstractVariableProvider getSameProvider(Long processId) {
        return new MapVariableProvider(values);
    }

    /**
     * Convert variables list to map from variable name to variable.
     *
     * @param variables
     *            Variables list.
     * @return Returns map from variable name to variable.
     */
    private static Map<String, WfVariable> convertVariablesListToMap(List<WfVariable> variables) {
        Map<String, WfVariable> map = Maps.newHashMap();
        for (WfVariable var : variables) {
            map.put(var.getDefinition().getName(), var);
        }
        return map;
    }
}
