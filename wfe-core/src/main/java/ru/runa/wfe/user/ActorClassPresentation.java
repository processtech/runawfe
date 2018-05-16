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
package ru.runa.wfe.user;

import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.security.Permission;

/**
 * Class presentation for actors.
 * 
 * @author dofs
 * @since 4.0
 */
public class ActorClassPresentation extends ClassPresentation {
    public static final String NAME = "batch_presentation.actor.name";
    public static final String FULL_NAME = "batch_presentation.actor.full_name";
    public static final String DESCRIPTION = "batch_presentation.actor.description";

    private static final ClassPresentation INSTANCE = new ActorClassPresentation();

    private ActorClassPresentation() {
        super(Actor.class, "", true, new FieldDescriptor[] {
                new FieldDescriptor(NAME, String.class.getName(), new DefaultDBSource(Actor.class, "name"), true,1, BatchPresentationConsts.ASC, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "name" }),
                new FieldDescriptor(FULL_NAME, String.class.getName(), new DefaultDBSource(Actor.class, "fullName"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "fullName" }),
                new FieldDescriptor(DESCRIPTION, String.class.getName(), new DefaultDBSource(Actor.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { Permission.NO_PERMISSION, "description" }) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
