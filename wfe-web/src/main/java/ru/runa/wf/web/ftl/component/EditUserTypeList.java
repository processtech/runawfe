package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class EditUserTypeList extends AbstractUserTypeList implements FormComponentSubmissionPostProcessor {
    private static final long serialVersionUID = 1L;

    @Override
    protected UserTypeListModel parseParameters() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        boolean allowToAddElements = getParameterAs(boolean.class, 1);
        boolean allowToDeleteElements = getParameterAs(boolean.class, 2);
        boolean allowToChangeElements = getParameterAs(boolean.class, 3);
        List<String> attributeNames = getMultipleParameter(4);
        return new EditUserTypeListModel(variable, attributeNames, allowToAddElements, allowToChangeElements, allowToDeleteElements);
    }

    @Override
    public Object postProcessValue(Object input) throws Exception {
        List<UserTypeMap> newList = (List<UserTypeMap>) input;
        if (newList.isEmpty()) {
            return newList;
        }
        String variableName = getParameterAsString(0);
        List<UserTypeMap> oldList = variableProvider.getValue(List.class, variableName);
        if (oldList == null) {
            return newList;
        }
        String indexesString = webHelper.getRequest().getParameter(variableName + FormSubmissionUtils.INDEXES_SUFFIX);       
        List<Integer> indexesList = Arrays.stream(indexesString.split(","))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        Set<String> hiddenAttributeNames = new HashSet<>(oldList.get(0).keySet());
        hiddenAttributeNames.removeAll(getEditableAttributeNames());
        if (hiddenAttributeNames.isEmpty()) {
            return newList;
        }
        // adjust values for user type hidden attributes (#413)
        for (int i = 0; i < indexesList.size(); i++) {
            UserTypeMap newValue = newList.get(i);
            int oldListIndex = indexesList.get(i);
            for (String attributeName : hiddenAttributeNames) {
                Object value = null;
                if (oldListIndex < oldList.size()) {
                    value = oldList.get(oldListIndex).get(attributeName);
                }
                newValue.put(attributeName, value);
            }
        }
        return newList;
    }

    protected List<String> getEditableAttributeNames() {
        return getMultipleParameter(4);
    }

    public class EditUserTypeListModel extends UserTypeListModel {
        private final boolean allowToAddElements;
        private final boolean allowToChangeElements;
        private final boolean allowToDeleteElements;

        public EditUserTypeListModel(WfVariable variable, List<String> attributeNames, boolean allowToAddElements, boolean allowToChangeElements,
                boolean allowToDeleteElements) {
            super(variable, attributeNames, true);
            this.allowToAddElements = allowToAddElements;
            this.allowToChangeElements = allowToChangeElements;
            this.allowToDeleteElements = allowToDeleteElements;
        }

        public boolean isAllowToAddElements() {
            return allowToAddElements;
        }

        public boolean isAllowToDeleteElements() {
            return allowToDeleteElements;
        }

        @Override
        public String getValue(TemplateModel arg0, TemplateModel arg1, Number index) throws TemplateModelException {
            if (allowToChangeElements) {
                UserTypeMap userTypeMap = (UserTypeMap) BEANS_WRAPPER.unwrap(arg0);
                VariableDefinition attributeDefinition = (VariableDefinition) BEANS_WRAPPER.unwrap(arg1);
                WfVariable variable = getAttributeVariable(userTypeMap, attributeDefinition, index);
                return ViewUtil.getComponentInput(user, webHelper, variable);
            }
            return super.getValue(arg0, arg1, index);
        }

        public String getTemplateValue(TemplateModel arg0) throws TemplateModelException {
            VariableDefinition definition = (VariableDefinition) BEANS_WRAPPER.unwrap(arg0);
            String suffix = VariableFormatContainer.COMPONENT_QUALIFIER_START + VariableFormatContainer.COMPONENT_QUALIFIER_END + "."
                    + definition.getName();
            WfVariable templateComponentVariable = ViewUtil.createComponentVariable(variable, suffix, definition.getFormatNotNull(), null);
            String inputComponentHtml = ViewUtil.getComponentInput(user, webHelper, templateComponentVariable);
            return convertComponentHtml(inputComponentHtml);
        }

        private String convertComponentHtml(String inputComponentHtml) {
            HashMap<String, String> replacements = new HashMap<String, String>(){{
                put("\"", "'");
                put("\n", "");
                put("[]", "{}");
            }};
            String regexp = "\"|\n|\\[]";
            StringBuffer sb = new StringBuffer();
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(inputString);

            while (matcher.find()) {
                matcher.appendReplacement(sb, replacements.get(matcher.group()));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }
    }
}