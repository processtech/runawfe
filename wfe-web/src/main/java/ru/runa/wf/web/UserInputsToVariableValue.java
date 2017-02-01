package ru.runa.wf.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.IExecutorLoader;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.format.VariableFormatVisitor;

/**
 * Extract variable value for variable definition, passed as operation context.
 */
public class UserInputsToVariableValue implements VariableFormatVisitor<Object, VariableDefinition> {

    private static final Log log = LogFactory.getLog(UserInputsToVariableValue.class);

    /**
     * User inputs. Map from field name to field value.
     */
    private final Map<String, ? extends Object> userInput;

    /**
     * Errors would be stored there. Map from field name to error description.
     */
    private final Map<String, String> errors = Maps.newHashMap();

    /**
     * Component for converting object to variable value.
     */
    private final ObjectToVariableValue objectConverter;

    public UserInputsToVariableValue(Map<String, ? extends Object> userInput, IExecutorLoader executorLoader) {
        super();
        this.userInput = userInput;
        objectConverter = new ObjectToVariableValue(executorLoader, errors);
    }

    @Override
    public Object onDate(DateFormat dateFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onTime(TimeFormat timeFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onDateTime(DateTimeFormat dateTimeFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object OnExecutor(ExecutorFormat executorFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onBoolean(BooleanFormat booleanFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onBigDecimal(BigDecimalFormat bigDecimalFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onDouble(DoubleFormat doubleFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onLong(LongFormat longFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onFile(FileFormat fileFormat, VariableDefinition variableDefinition) {
        Object value = userInput.get(variableDefinition.getName());
        return fileFormat.processBy(objectConverter, new ObjectToVariableValueContext(variableDefinition.getName(), value));
    }

    @Override
    public Object onHidden(HiddenFormat hiddenFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onList(ListFormat listFormat, VariableDefinition variableDefinition) {
        List<Integer> indexes = null;
        String sizeInputName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        String indexesInputName = variableDefinition.getName() + FormSubmissionUtils.INDEXES_SUFFIX;
        VariableFormat componentFormat = FormatCommons.createComponent(variableDefinition, 0);
        List<Object> list = null;
        String[] stringsIndexes = (String[]) userInput.get(indexesInputName);
        if (stringsIndexes == null || stringsIndexes.length != 1) {
            if (userInput.containsKey(sizeInputName)) {
                // js dynamic way
                String[] stringsSize = (String[]) userInput.get(sizeInputName);
                if (stringsSize == null || stringsSize.length != 1) {
                    log.error("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(stringsSize));
                    return FormSubmissionUtils.IGNORED_VALUE;
                }
                int listSize = TypeConversionUtil.convertTo(int.class, stringsSize[0]);
                list = Lists.newArrayListWithExpectedSize(listSize);
                indexes = Lists.newArrayListWithExpectedSize(listSize);
                for (int i = 0; indexes.size() < listSize && i < 1000; i++) {
                    String checkString = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    for (String key : userInput.keySet()) {
                        if (key.startsWith(checkString)) {
                            indexes.add(i);
                            break;
                        }
                    }
                }
                if (indexes.size() != listSize) {
                    errors.put(variableDefinition.getName(), ". Not all list items found. Expected:'" + listSize + "', found:'" + indexes.size());
                }
                for (Integer index : indexes) {
                    String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    String scriptingName = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, componentFormat);
                    Object componentValue = componentDefinition.processBy(this, componentDefinition);
                    if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, componentValue)) {
                        list.add(componentValue);
                    }
                }
                return list;
            } else {
                // http old-style way
                String[] strings = (String[]) userInput.get(variableDefinition.getName());
                if (strings == null || strings.length == 0) {
                    return FormSubmissionUtils.IGNORED_VALUE;
                }
                list = Lists.newArrayListWithExpectedSize(strings.length);
                for (String componentValue : strings) {
                    ObjectToVariableValueContext context = new ObjectToVariableValueContext(variableDefinition.getName(), componentValue);
                    list.add(componentFormat.processBy(objectConverter, context));
                }
                return list;
            }
        } else {
            int listSize = !stringsIndexes[0].equals("") ? stringsIndexes[0].toString().split(",").length : 0;
            list = Lists.newArrayListWithExpectedSize(listSize);
            if (listSize > 0) {
                indexes = Lists.newArrayListWithExpectedSize(listSize);
                String[] stringIndexes = stringsIndexes[0].toString().split(",");
                for (String index : stringIndexes) {
                    indexes.add(TypeConversionUtil.convertTo(int.class, index));
                }
                for (Integer index : indexes) {
                    String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    String scriptingName = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, componentFormat);
                    Object componentValue = componentDefinition.processBy(this, componentDefinition);
                    if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, componentValue)) {
                        list.add(componentValue);
                    }
                }
            }
            return list;
        }
    }

    @Override
    public Object onMap(MapFormat mapFormat, VariableDefinition variableDefinition) {
        Map<Object, Object> map = null;
        String sizeInputName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        String indexesInputName = variableDefinition.getName() + FormSubmissionUtils.INDEXES_SUFFIX;
        VariableFormat componentKeyFormat = FormatCommons.createComponent(variableDefinition, 0);
        VariableFormat componentValueFormat = FormatCommons.createComponent(variableDefinition, 1);
        String[] stringsIndexes = (String[]) userInput.get(indexesInputName);
        List<Integer> indexes = null;
        if (stringsIndexes == null || stringsIndexes.length != 1) {
            String[] stringsSize = (String[]) userInput.get(sizeInputName);
            if (stringsSize == null || stringsSize.length != 1) {
                log.error("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(stringsSize));
                return FormSubmissionUtils.IGNORED_VALUE;
            }
            int mapSize = TypeConversionUtil.convertTo(int.class, stringsSize[0]);
            map = Maps.newHashMapWithExpectedSize(mapSize);
            indexes = Lists.newArrayListWithExpectedSize(mapSize);
            for (int i = 0; indexes.size() < mapSize && i < 1000; i++) {
                String checkString = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                for (String key : userInput.keySet()) {
                    if (key.startsWith(checkString)) {
                        indexes.add(i);
                        break;
                    }
                }
            }
            if (indexes.size() != mapSize) {
                errors.put(variableDefinition.getName(), ". Not all list items found. Expected:'" + mapSize + "', found:'" + indexes.size());
            }
        } else {
            int mapSize = !stringsIndexes[0].equals("") ? stringsIndexes[0].toString().split(",").length : 0;
            map = Maps.newHashMapWithExpectedSize(mapSize);
            indexes = Lists.newArrayListWithExpectedSize(mapSize);
            if (mapSize > 0) {
                String[] stringIndexes = stringsIndexes[0].toString().split(",");
                for (String index : stringIndexes) {
                    indexes.add(TypeConversionUtil.convertTo(int.class, index));
                }
            }
        }
        for (Integer index : indexes) {
            String nameKey = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END + VariableFormatContainer.KEY_SUFFIX;
            String scriptingNameKey = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END + VariableFormatContainer.KEY_SUFFIX;
            VariableDefinition componentKeyDefinition = new VariableDefinition(nameKey, scriptingNameKey, componentKeyFormat);
            Object componentKeyValue = componentKeyDefinition.processBy(this, componentKeyDefinition);
            String nameValue = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END + VariableFormatContainer.VALUE_SUFFIX;
            String scriptingNameValue = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END + VariableFormatContainer.VALUE_SUFFIX;
            VariableDefinition componentValueDefinition = new VariableDefinition(nameValue, scriptingNameValue, componentValueFormat);
            Object componentValueValue = componentValueDefinition.processBy(this, componentValueDefinition);
            if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, componentKeyValue)) {
                map.put(componentKeyValue, componentValueValue);
            }
        }
        return map;
    }

    @Override
    public Object onProcessId(ProcessIdFormat processIdFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onString(StringFormat stringFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onTextString(TextFormat textFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    @Override
    public Object onUserType(UserTypeFormat userTypeFormat, VariableDefinition variableDefinition) {
        UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
        for (VariableDefinition expandedDefinition : variableDefinition.expandUserType(false)) {
            Object componentValue = expandedDefinition.processBy(this, expandedDefinition);
            if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, componentValue)) {
                String attributeName = expandedDefinition.getName().substring(variableDefinition.getName().length() + 1);
                userTypeMap.put(attributeName, componentValue);
            }
        }
        return userTypeMap;
    }

    @Override
    public Object onOther(VariableFormat variableFormat, VariableDefinition variableDefinition) {
        return defaultFormatProcessing(variableDefinition);
    }

    /**
     * Default value extract algorithm, if no other is specified in on* method.
     *
     * @param variableDefinition
     *            Variable definition which variable value is extracted.
     * @return Returns variable value
     */
    private Object defaultFormatProcessing(VariableDefinition variableDefinition) {
        VariableFormat format = FormatCommons.create(variableDefinition);
        Object value = userInput.get(variableDefinition.getName());
        Object result = format.processBy(objectConverter, new ObjectToVariableValueContext(variableDefinition.getName(), value));
        if (value != null) {
            return result;
        } else {
            return result != null ? result : FormSubmissionUtils.IGNORED_VALUE;
        }
    }
}
