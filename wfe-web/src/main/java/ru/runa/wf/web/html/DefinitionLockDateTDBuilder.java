package ru.runa.wf.web.html;

import java.util.Date;

import ru.runa.common.WebResources;
import ru.runa.common.web.html.BaseDateTDBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;

public class DefinitionLockDateTDBuilder extends BaseDateTDBuilder<WfDefinition> {

    @Override
    protected Date getDate(WfDefinition object) {
        return object.getLockDate();
    }

    @Override
    protected Long getId(WfDefinition process) {
        return process.getId();
    }

    @Override
    protected String getActionMapping() {
        return WebResources.ACTION_MAPPING_MANAGE_DEFINITION;
    }
}
