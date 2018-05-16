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
package ru.runa.wfe.service;

import java.util.Collection;
import java.util.List;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Service for authorization.
 * 
 * @since 2.0
 */
public interface AuthorizationService {

    /**
     * Checks whether user has permission on securedObject.
     */
    boolean isAllowed(User user, Permission permission, SecuredObject securedObject);

    /**
     * Checks whether user has permission on object.
     */
    boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId);

    /**
     * Checks whether user has permission on secured objects of the same secured object type.
     */
    <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects);

    /**
     * Checks if user has parmission on any object of specified type.
     */
    boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType);

    /**
     * Sets permissions for executor specified by id on securedObject.
     */
    void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject);

    /**
     * Sets permissions for executors specified by ids on securedObject.
     */
    void setPermissions(User user, List<Long> executorsId, List<Collection<Permission>> permissions, SecuredObject securedObject);

    /**
     * Sets permissions for executors specified by ids on securedObject.
     */
    void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, SecuredObject securedObject);

    /**
     * Returns permissions that executor himself has on securedObject.
     * Permissions by privilege will not return.
     * 
     * @return Map of {Permission, Is permission can be modifiable}, not <code>null</code>
     */
    List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject);

    /**
     * Load executor's which already has (or not has) some permission on
     * specified securedObject. This query using paging.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or
     *            not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain SecuredObject}; false to load executors without
     *            permissions.
     * @return Executors with or without permission on {@linkplain SecuredObject}
     */
    List<Executor> getExecutorsWithPermission(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Load executor's count which already has (or not has) some permission on
     * specified securedObject.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or
     *            not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain SecuredObject}; false to load executors without
     *            permissions.
     * @return Count of executors with or without permission on
     *         {@linkplain SecuredObject}.
     */
    int getExecutorsWithPermissionCount(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Loads secured objects with permission filtering.
     */
    <T> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
                                                           Permission permission, SecuredObjectType[] securedObjectTypes, boolean enablePaging);

}
