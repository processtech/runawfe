package ru.runa.wfe.execution;

import ru.runa.wfe.var.VariableDefinition;

/**
 * Result for {@link ConvertToSimpleVariables} operation. Must contains only simple variables, which may not require additional transformations before
 * saving it to database.
 */
public class ConvertToSimpleVariablesResult {

    /**
     * Saving variable definition.
     */
    public final VariableDefinition variableDefinition;

    /**
     * Saving variable value.
     */
    public final Object value;

    /**
     * Is this variable virtual (created for UserTypes, Maps and so on elements).
     */
    public final boolean isVirtual;

    /**
     * Creates result for {@link ConvertToSimpleVariables} operation.
     *
     * @param context
     *            {@link ConvertToSimpleVariables} operation context for creating result for simple variable.
     * @param isVirtual
     *            Is this variable virtual (created for UserTypes, Maps and so on elements).
     */
    public ConvertToSimpleVariablesResult(ConvertToSimpleVariablesContext context, boolean isVirtual) {
        variableDefinition = context.getVariableDefinition();
        value = context.getValue();
        this.isVirtual = isVirtual;
    }

    /**
     * Creates result for {@link ConvertToSimpleVariables} operation.
     *
     * @param variableDefinition
     *            Saving variable definition.
     * @param value
     *            Saving variable value.
     * @param isVirtual
     *            Is this variable virtual (created for UserTypes, Maps and so on elements).
     */
    public ConvertToSimpleVariablesResult(VariableDefinition variableDefinition, Object value, boolean isVirtual) {
        this.variableDefinition = variableDefinition;
        this.value = value;
        this.isVirtual = isVirtual;
    }
}
