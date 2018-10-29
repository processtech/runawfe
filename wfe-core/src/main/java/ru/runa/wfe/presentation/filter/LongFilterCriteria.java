package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

/**
 * Created on 01.09.2005 TODO add BETWEEN support
 */
public class LongFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 642103915780987672L;

    public LongFilterCriteria() {
        super(1);
    }

    public LongFilterCriteria(Long value) {
        super(new String[] { value != null ? value.toString() : "" });
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        try {
            String[] values = newTemplates[0].split(",");
            if (values.length == 1) {
                values = newTemplates[0].split("-");
            }
            for (String value : values) {
                Long.parseLong(value.trim());
            }
        } catch (NumberFormatException nfe) {
            throw new FilterFormatException(nfe.getMessage());
        }
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        final String template = getFilterTemplate(0);
        if (template.contains(",")) {
            return buildInOperator(aliasedFieldName);
        } else if (template.contains("-")) {
            return buildBetweenOperator(aliasedFieldName);
        }
        final String placeHolderName = makePlaceHolderName(aliasedFieldName);
        final StringBuilder sb = new StringBuilder(aliasedFieldName);
        sb.append(" = :").append(placeHolderName);
        sb.append(" ");
        placeholders.add(placeHolderName, Long.valueOf(template));
        return sb.toString();
    }

}
