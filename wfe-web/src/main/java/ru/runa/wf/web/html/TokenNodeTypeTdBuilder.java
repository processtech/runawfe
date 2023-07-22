package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.error.dto.WfTokenError;

public class TokenNodeTypeTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTokenError error = (WfTokenError) object;
        if (error.getNodeType() == null) {
            return "";
        }
        if (env.getPageContext() != null) {
            return Messages.getMessage(error.getNodeType().getLabelKey(), env.getPageContext());
        }
        return error.getNodeType().name();
    }
}
