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

import ru.runa.wfe.presentation.BatchPresentationConsts;

import java.util.Date;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDbSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;

/**
 * Class presentation for process definition history.
 */
public class DefinitionHistoryClassPresentation extends ClassPresentation {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String VERSION = "version";
    public static final String TYPE = "type";
    public static final String CREATE_DATE = "createDate";
    public static final String CREATE_ACTOR = "createActor";
    public static final String UPDATE_DATE = "updateDate";
    public static final String UPDATE_ACTOR = "updateActor";
    public static final String SUBPROCESS_BINDING_DATE = "subprocessBindingDate";
    public static final String PROCESSES_COUNT = "processesCount";

    private static final ClassPresentation INSTANCE = new DefinitionHistoryClassPresentation();

    private DefinitionHistoryClassPresentation() {
        super(Deployment.class, null, true,
                new FieldDescriptor[] {
                        // display name field type DB source isSort filter mode
                        // get value/show in web getter parameters
                        new FieldDescriptor(NAME, String.class.getName(), new DefaultDbSource(Deployment.class, "name"), true,
                                FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", new Object[] {
                                        Permission.START_PROCESS, "name" }),
                        new FieldDescriptor(VERSION, Integer.class.getName(), new DefaultDbSource(Deployment.class, "version"), true,
                                1, BatchPresentationConsts.DESC, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTdBuilder", 
                                new Object[] { Permission.READ, "version" }),
                        new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDbSource(Deployment.class, "description"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DescriptionProcessTdBuilder", new Object[] {}),
                        new FieldDescriptor(TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDbSource(Deployment.class, "category"),
                                true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.CategoryTdBuilder", new Object[] {}),
                        new FieldDescriptor(CREATE_DATE, Date.class.getName(), new DefaultDbSource(Deployment.class, "createDate"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionCreateDateTdBuilder", new Object[] {}),
                        new FieldDescriptor(CREATE_ACTOR, Actor.class.getName(), new DefaultDbSource(Deployment.class, "createActor"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionCreateActorTdBuilder", new Object[] {}),
                        new FieldDescriptor(UPDATE_DATE, Date.class.getName(), new DefaultDbSource(Deployment.class, "updateDate"), true,
                                FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DefinitionUpdateDateTdBuilder", new Object[] {}),
                        new FieldDescriptor(UPDATE_ACTOR, Actor.class.getName(), new DefaultDbSource(Deployment.class, "updateActor"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionUpdateActorTdBuilder", new Object[] {}),
                        new FieldDescriptor(PROCESSES_COUNT, String.class.getName(), new DefaultDbSource(Deployment.class, "name"), false,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.DefinitionProcessesCountTdBuilder", new Object[] {}) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
