package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = AddPermissionsOnReportsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnReportsOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnReports";

    public AddPermissionsOnReportsOperation() {
        super(ReportsSecure.INSTANCE, ChangePermissionType.ADD);
    }
}
