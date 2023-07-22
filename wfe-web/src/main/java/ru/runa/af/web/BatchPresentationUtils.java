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
package ru.runa.af.web;

import java.util.List;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class BatchPresentationUtils {
    private static final SecuredObjectType[] ACTOR_GROUP_CLASSESS = { SecuredObjectType.EXECUTOR };

    public static boolean isExecutorPermissionAllowedForAnyone(User user, List<? extends Executor> executors, BatchPresentation batchPresentation,
            Permission permission) {
        List<Executor> executorsWithPermission = Delegates.getAuthorizationService().getPersistentObjects(user, batchPresentation, Executor.class,
                permission, ACTOR_GROUP_CLASSESS, false);
        for (Executor e : executors) {
            if (executorsWithPermission.contains(e)) {
                return true;
            }
        }
        return false;
    }

    public static TdBuilder[] getBuilders(TdBuilder[] prefix, BatchPresentation batchPresentation, TdBuilder[] suffix) {
        int displayed = batchPresentation.getDisplayFields().length;
        for (FieldDescriptor field : batchPresentation.getDisplayFields()) {
            if (field.isPrototype() || field.groupableByProcessId || field.fieldState != FieldState.ENABLED) {
                --displayed;
            }
        }
        int prefixBuildersLength = prefix != null ? prefix.length : 0;
        int suffixBuildersLength = suffix != null ? suffix.length : 0;
        TdBuilder[] builders = new TdBuilder[prefixBuildersLength + displayed + suffixBuildersLength];
        if (prefixBuildersLength != 0) {
            for (int i = 0; i < prefix.length; ++i) {
                builders[i] = prefix[i];
            }
        }
        int idx = 0;
        for (int i = 0; i < batchPresentation.getDisplayFields().length; ++i) {
            FieldDescriptor field = batchPresentation.getDisplayFields()[i];
            if (!field.isPrototype() && !field.groupableByProcessId && field.fieldState == FieldState.ENABLED) {
                builders[idx++ + prefixBuildersLength] = (TdBuilder) field.getTdBuilder();
            }
        }
        if (suffixBuildersLength != 0) {
            for (int i = 0; i < suffix.length; ++i) {
                builders[i + prefixBuildersLength + displayed] = suffix[i];
            }
        }
        return builders;
    }

}
