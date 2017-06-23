package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.impl.NullVariable;

public class VariableCreator {
    private static final Log log = LogFactory.getLog(VariableCreator.class);

    private List<VariableType> types;

    @Autowired
    private VariableType serializableVariableType;

    @Required
    public void setTypes(List<VariableType> types) {
        this.types = types;
    }

    /**
     * Creates new variable of the corresponding type.
     * 
     * @param value
     *            initial value
     * @return variable
     */
    private Variable<?> create(Object value) {
        for (VariableType type : types) {
            if (type.getMatcher().matches(value)) {
                try {
                    Variable<?> variable = type.getVariableClass().newInstance();
                    variable.setConverter(type.getConverter());
                    return variable;
                } catch (Exception e) {
                    throw new InternalApplicationException("Unable to create variable " + type.getVariableClass(), e);
                }
            }
        }
        throw new InternalApplicationException("No variable found for value " + value);
    }

    /**
     * Creates new variable of the corresponding type. This method does not persisit it.
     * 
     * @param value
     *            initial value
     * @return variable
     */
    public Variable<?> create(ru.runa.wfe.execution.Process process, VariableDefinition variableDefinition, Object value) {
        log.debug("Creating variable '" + variableDefinition.getName() + "' in " + process + " with value '" + value + "'"
                + (value != null ? " of " + value.getClass() : ""));
        Variable<?> variable;
        if (value == null) {
            variable = new NullVariable();
        } else if (variableDefinition.getStoreType() == VariableStoreType.BLOB && value instanceof Serializable) {
            try {
                variable = serializableVariableType.getVariableClass().newInstance();
                variable.setConverter(serializableVariableType.getConverter());
            } catch (Exception e) {
                throw new InternalApplicationException("Unable to create variable " + serializableVariableType.getVariableClass(), e);
            }
        } else {
            variable = create(value);
        }
        variable.setName(variableDefinition.getName());
        variable.setProcess(process);
        variable.setCreateDate(new Date());
        log.info(String.format("create: variableDefinition: %s variable: %s converter: %s", variableDefinition, variable, variable.getConverter()));
        return variable;
    }

}
