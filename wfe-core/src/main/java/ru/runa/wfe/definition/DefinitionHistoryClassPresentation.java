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
package ru.runa.wfe.definition;

import java.util.Date;

import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.user.Actor;

/**
 * Class presentation for process definition history.
 */
public class DefinitionHistoryClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.process_definition.name";
    public static final String DESCRIPTION = "batch_presentation.process_definition.description";
    public static final String VERSION = "batch_presentation.process_definition.version";
    public static final String TYPE = "batch_presentation.process_definition.process_type";
    public static final String CREATE_DATE = "batch_presentation.process_definition.create_date";
    public static final String CREATE_ACTOR = "batch_presentation.process_definition.create_actor";
    public static final String UPDATE_DATE = "batch_presentation.process_definition.update_date";
    public static final String UPDATE_ACTOR = "batch_presentation.process_definition.update_actor";
    public static final String PROCESSES_COUNT = "batch_presentation.process_definition.processes_count";

    private static final ClassPresentation INSTANCE = new DefinitionHistoryClassPresentation();

    private DefinitionHistoryClassPresentation() {
        super(Deployment.class, null, true,
                new FieldDescriptor[] {
                        // display name field type DB source isSort filter mode
                        // get value/show in web getter parameters
                        new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(Deployment.class, "name"), true,
                                FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                                        DefinitionPermission.START_PROCESS, "name" }),
                        new FieldDescriptor(VERSION, Integer.class.getName(), new DefaultDBSource(Deployment.class, "version"), true,
                                FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { DefinitionPermission.READ,
                                        "version" }),
                        new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDBSource(Deployment.class, "description"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DescriptionProcessTDBuilder", new Object[] {}),
                        new FieldDescriptor(TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(Deployment.class, "category"),
                                true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTDBuilder", new Object[] {}, true),
                        new FieldDescriptor(CREATE_DATE, Date.class.getName(), new DefaultDBSource(Deployment.class, "createDate"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionCreateDateTDBuilder", new Object[] {}),
                        new FieldDescriptor(CREATE_ACTOR, Actor.class.getName(), new DefaultDBSource(Deployment.class, "createActor"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionCreateActorTDBuilder", new Object[] {}),
                        new FieldDescriptor(UPDATE_DATE, Date.class.getName(), new DefaultDBSource(Deployment.class, "updateDate"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionUpdateDateTDBuilder", new Object[] {}),
                        new FieldDescriptor(UPDATE_ACTOR, Actor.class.getName(), new DefaultDBSource(Deployment.class, "updateActor"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionUpdateActorTDBuilder", new Object[] {}),
                        new FieldDescriptor(PROCESSES_COUNT, String.class.getName(), new DefaultDBSource(Deployment.class, "name"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionProcessesCountTDBuilder", new Object[] {}) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
