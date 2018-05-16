/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.execution;

import java.util.Date;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.VariableDBSources;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.Variable;

/**
 * Created on 22.10.2005
 *
 */
public class ProcessClassPresentation extends ClassPresentation {
    public static final String PROCESS_ID = "batch_presentation.process.id";
    public static final String DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String PROCESS_START_DATE = "batch_presentation.process.started";
    public static final String PROCESS_END_DATE = "batch_presentation.process.ended";
    public static final String DEFINITION_VERSION = "batch_presentation.process.definition_version";
    public static final String PROCESS_EXECUTION_STATUS = "batch_presentation.process.execution_status";
    public static final String PROCESS_VARIABLE = editable_prefix + "name:batch_presentation.process.variable";

    private static final ClassPresentation INSTANCE = new ProcessClassPresentation();

    private ProcessClassPresentation() {
        super(Process.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(PROCESS_ID, Integer.class.getName(), new DefaultDBSource(Process.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "id" }),
                new FieldDescriptor(DEFINITION_NAME, String.class.getName(), new DefaultDBSource(Process.class, "deployment.name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "name" }),
                new FieldDescriptor(PROCESS_START_DATE, Date.class.getName(), new DefaultDBSource(Process.class, "startDate"), true, 1,
                        BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_END_DATE, Date.class.getName(), new DefaultDBSource(Process.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTDBuilder", new Object[] {}),
                new FieldDescriptor(DEFINITION_VERSION, Integer.class.getName(), new DefaultDBSource(Process.class, "deployment.version"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "version" }),
                new FieldDescriptor(filterable_prefix + "batch_presentation.process.id", String.class.getName(), new SubProcessDBSource(
                        Process.class, "hierarchyIds"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_VARIABLE, Variable.class.getName(), VariableDBSources.get(null), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_EXECUTION_STATUS, String.class.getName(), new DefaultDBSource(Process.class, "executionStatus"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessExecutionStatusTDBuilder", new Object[] {}) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
