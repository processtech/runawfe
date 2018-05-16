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
package ru.runa.wfe.security.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToPermissions;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDAO;

/**
 * Permission DAO level implementation via Hibernate.
 * 
 * @author Konstantinov Aleksey 19.02.2012
 */
@SuppressWarnings("unchecked")
public class PermissionDAO extends CommonDAO {
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private SessionFactory sessionFactory;

    private final Map<SecuredObjectType, Set<Executor>> privelegedExecutors = Maps.newHashMap();
    private final Set<Long> privelegedExecutorIds = Sets.newHashSet();

    public PermissionDAO() {
        for (SecuredObjectType type : SecuredObjectType.values()) {
            privelegedExecutors.put(type, new HashSet<Executor>());
        }
    }

    /**
     * Called once after patches are successfully applied
     */
    public void init() throws Exception {
        List<PrivelegedMapping> list = getHibernateTemplate().find("from PrivelegedMapping m");
        for (PrivelegedMapping mapping : list) {
            privelegedExecutors.get(mapping.getType()).add(mapping.getExecutor());
            privelegedExecutorIds.add(mapping.getExecutor().getId());
        }
    }

    public List<Permission> getIssuedPermissions(Executor executor, SecuredObject securedObject) {
        List<Permission> permissions = Lists.newArrayList();
        if (!isPrivilegedExecutor(securedObject, executor)) {
            for (PermissionMapping pm : getOwnPermissionMappings(executor, securedObject)) {
                permissions.add(pm.getPermission());
            }
        }
        return permissions;
    }

    /**
     * Sets permissions for executor on securedObject.
     * 
     * @param executor
     *            Executor, which got permissions.
     * @param permissions
     *            Permissions for executor.
     * @param securedObject
     *            Secured object to set permission on.
     */
    public void setPermissions(Executor executor, Collection<Permission> permissions, SecuredObject securedObject) {
        if (isPrivilegedExecutor(securedObject, executor)) {
            log.debug(permissions + " not granted for privileged " + executor);
            return;
        }
        ApplicablePermissions.check(securedObject, permissions);
        Session session = sessionFactory.getCurrentSession();
        List<PermissionMapping> toDelete = getOwnPermissionMappings(executor, securedObject);
        for (Permission permission : permissions) {
            PermissionMapping pm = new PermissionMapping(executor, securedObject, permission);
            if (toDelete.contains(pm)) {
                toDelete.remove(pm);
            } else {
                session.save(pm);
            }
        }
        if (!toDelete.isEmpty()) {
            session.createQuery("delete from PermissionMapping pm where pm in (:pms)").setParameterList("pms", toDelete).executeUpdate();
        }
    }

    /**
     * Checks whether executor has permission on securedObject.
     * 
     * @param user
     *            Executor, which permission must be check.
     * @param permission
     *            Checking permission.
     * @param securedObject
     *            Secured object to check permission on.
     * @return true if executor has requested permission on secuedObject; false otherwise.
     */
    public boolean isAllowed(final User user, final Permission permission, final SecuredObject securedObject) {
        return isAllowed(user, permission, securedObject.getSecuredObjectType(), securedObject.getIdentifiableId());
    }

    public boolean isAllowed(final User user, final Permission permission, final SecuredObjectType securedObjectType, final Long identifiableId) {
        if (permission == Permission.NO_PERMISSION) {
            // Optimization; see comments at NO_PERMISSION definition.
            return false;
        }
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
        if (isPrivilegedExecutor(securedObjectType, executorWithGroups)) {
            return true;
        }
        return getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) {
                return session.createQuery("select 0 from PermissionMapping where executor in (:executors) and objectType=:objectType and objectId=:objectId and permission=:permission")
                        .setParameterList("executors", executorWithGroups)
                        .setParameter("objectType", securedObjectType)
                        .setParameter("objectId", identifiableId)
                        .setParameter("permission", permission)
                        .setMaxResults(1)
                        .uniqueResult();
            }
        }) != null;
    }

    public boolean isAllowedForAny(final User user, final Permission permission, final SecuredObjectType securedObjectType) {
        if (permission == Permission.NO_PERMISSION) {
            // Optimization; see comments at NO_PERMISSION definition.
            return false;
        }
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
        if (isPrivilegedExecutor(securedObjectType, executorWithGroups)) {
            return true;
        }
        return getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) {
                return session.createQuery("select 0 from PermissionMapping where executor in (:executors) and objectType=:objectType and permission=:permission")
                        .setParameterList("executors", executorWithGroups)
                        .setParameter("objectType", securedObjectType)
                        .setParameter("permission", permission)
                        .setMaxResults(1)
                        .uniqueResult();
            }
        }) != null;
    }

    /**
     * Checks whether executor has permission on securedObject's. Create result array in same order, as securedObject's.
     * 
     * @param user
     *            Executor, which permission must be check.
     * @param permission
     *            Checking permission.
     * @param securedObjects
     *            Secured objects to check permission on.
     * @return Array of: true if executor has requested permission on securedObject; false otherwise.
     */
    public <T extends SecuredObject> boolean[] isAllowed(final User user, final Permission permission, final List<T> securedObjects) {
        boolean[] result = new boolean[securedObjects.size()];
        if (securedObjects.size() == 0) {
            return result;
        }
        if (permission == Permission.NO_PERMISSION) {
            // Optimization; see comments at NO_PERMISSION definition.
            for (int i = 0; i < securedObjects.size(); i++) {
                result[i] = false;
            }
            return result;
        }
        final Set<Executor> executorWithGroups = getExecutorWithAllHisGroups(user.getActor());
        if (isPrivilegedExecutor(securedObjects.get(0).getSecuredObjectType(), executorWithGroups)) {
            for (int i = 0; i < securedObjects.size(); i++) {
                result[i] = true;
            }
            return result;
        }
        final SecuredObjectType securedObjectType = securedObjects.get(0).getSecuredObjectType();
        List<PermissionMapping> permissions = new ArrayList<>();
        int window = SystemProperties.getDatabaseParametersCount() - executorWithGroups.size() - 2;
        Preconditions.checkArgument(window > 100);
        for (int i = 0; i <= (securedObjects.size() - 1) / window; ++i) {
            final int start = i * window;
            final int end = (i + 1) * window > securedObjects.size() ? securedObjects.size() : (i + 1) * window;
            final List<Long> identifiableIds = new ArrayList<>(end - start);
            for (int j = start; j < end; j++) {
                SecuredObject securedObject = securedObjects.get(j);
                identifiableIds.add(securedObject.getIdentifiableId());
                if (securedObjectType != securedObject.getSecuredObjectType()) {
                    throw new InternalApplicationException("Secured objects should be of the same secured object type (" + securedObjectType + ")");
                }
            }
            if (identifiableIds.isEmpty()) {
                break;
            }
            List<PermissionMapping> mappings = getHibernateTemplate().executeFind(new HibernateCallback<List<PermissionMapping>>() {

                @Override
                public List<PermissionMapping> doInHibernate(Session session) {
                    return session
                            .createQuery("from PermissionMapping where executor in (:executors) and objectType=:objectType and objectId in (:objectIds) and permission=:permission")
                            .setParameterList("executors", executorWithGroups)
                            .setParameter("objectType", securedObjectType)
                            .setParameterList("objectIds", identifiableIds)
                            .setParameter("permission", permission)
                            .list();
                }
            });
            permissions.addAll(mappings);
        }
        Set<Long> allowedIdentifiableIdsSet = new HashSet<>(permissions.size());
        for (PermissionMapping pm : permissions) {
            allowedIdentifiableIdsSet.add(pm.getObjectId());
        }
        for (int i = 0; i < securedObjects.size(); i++) {
            result[i] = allowedIdentifiableIdsSet.contains(securedObjects.get(i).getIdentifiableId());
        }
        return result;
    }

    /**
     * Loads all permission mappings on specified secured object belongs to specified executor.
     * 
     * @param executor
     *            Executor, which permissions is loading.
     * @param securedObject
     *            Secured object, which permissions is loading.
     * @return Loaded permissions.
     */
    private List<PermissionMapping> getOwnPermissionMappings(Executor executor, SecuredObject securedObject) {
        return getHibernateTemplate().find("from PermissionMapping where objectId=? and objectType=? and executor=?",
                securedObject.getIdentifiableId(), securedObject.getSecuredObjectType(), executor);
    }

    private Set<Executor> getExecutorWithAllHisGroups(Executor executor) {
        Set<Executor> set = new HashSet<Executor>(executorDAO.getExecutorParentsAll(executor, false));
        set.add(executor);
        return set;
    }

    /**
     * Deletes all permissions for executor.
     * 
     * @param executor
     *            executor
     */
    public void deleteOwnPermissions(Executor executor) {
        getHibernateTemplate().bulkUpdate("delete from PermissionMapping where executor=?", executor);
    }

    /**
     * Deletes all permissions for securedObject.
     * 
     * @param securedObject
     *            securedObject
     */
    public void deleteAllPermissions(SecuredObject securedObject) {
        getHibernateTemplate().bulkUpdate("delete from PermissionMapping where objectType=? and objectId=?", securedObject.getSecuredObjectType(),
                securedObject.getIdentifiableId());
    }

    /**
     * Load {@linkplain Executor}'s, which have permission on {@linkplain SecuredObject}. <br/>
     * <b>Paging is not enabled.</b>
     * 
     * @param securedObject
     *            {@linkplain SecuredObject} to load {@linkplain Executor}'s.
     * @return List of {@linkplain Executor}'s with permission on {@linkplain SecuredObject}.
     */
    public Set<Executor> getExecutorsWithPermission(SecuredObject securedObject) {
        List<Executor> list = getHibernateTemplate().find(
                "select distinct(pm.executor) from PermissionMapping pm where pm.objectId=? and pm.objectType=?", securedObject.getIdentifiableId(),
                securedObject.getSecuredObjectType());
        Set<Executor> result = Sets.newHashSet(list);
        result.addAll(getPrivilegedExecutors(securedObject.getSecuredObjectType()));
        return result;
    }

    /**
     * Return array of privileged {@linkplain Executor}s for given (@linkplain SecuredObject) type (i.e. executors whose permissions on SecuredObject
     * type can not be changed).
     * 
     * @return Privileged {@linkplain Executor}'s array.
     */
    public Collection<Executor> getPrivilegedExecutors(SecuredObjectType securedObjectType) {
        return privelegedExecutors.get(securedObjectType);
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean isPrivilegedExecutor(Executor executor) {
        for (Set<Executor> executors : privelegedExecutors.values()) {
            if (executors.contains(executor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if executor is privileged executor for any secured object type.
     */
    public boolean hasPrivilegedExecutor(List<Long> executorIds) {
        for (Long executorId : executorIds) {
            if (privelegedExecutorIds.contains(executorId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if executor is privileged executor for given securedObject.
     * 
     * @param executor
     *            {@linkplain Executor}, to check if privileged.
     * @param securedObject
     *            {@linkplain SecuredObject} object, to check if executor is privileged to it.
     * @return true if executor is privileged for given securedObject and false otherwise.
     */
    private boolean isPrivilegedExecutor(SecuredObject securedObject, Executor executor) {
        Collection<Executor> executorWithGroups = getExecutorWithAllHisGroups(executor);
        return isPrivilegedExecutor(securedObject.getSecuredObjectType(), executorWithGroups);
    }

    private boolean isPrivilegedExecutor(SecuredObjectType securedObjectType, Collection<Executor> executorWithGroups) {
        for (Executor executor : executorWithGroups) {
            if (getPrivilegedExecutors(securedObjectType).contains(executor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds new record in <i>dictionary</i> tables describing new SecuredObject type.
     * 
     * @param type
     *            Type of SecuredObject.
     * @param executors
     *            Privileged(?) executors for target class.
     */
    public void addType(SecuredObjectType type, List<? extends Executor> executors) {
        for (Executor executor : executors) {
            PrivelegedMapping mapping = new PrivelegedMapping(type, executor);
            getHibernateTemplate().save(mapping);
            privelegedExecutors.get(mapping.getType()).add(mapping.getExecutor());
            privelegedExecutorIds.add(mapping.getExecutor().getId());
        }
    }

    /**
     * Load list of {@linkplain SecuredObject} for which executors have permission on.
     * 
     * @param user
     *            User which must have permission on loaded {@linkplain SecuredObject} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading {@linkplain SecuredObject}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must has on {@linkplain SecuredObject}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObjectType} types, used to check permissions.
     * @param enablePaging
     *            Flag, equals true, if paging must be enabled and false otherwise.
     * @return List of {@link SecuredObject}'s for which executors have permission on.
     */
    public List<? extends SecuredObject> getPersistentObjects(User user, BatchPresentation batchPresentation, Permission permission,
            SecuredObjectType[] securedObjectTypes, boolean enablePaging) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(logger, 1000);
        timeMeasurer.jobStarted();
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, permission, securedObjectTypes);
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addPermissions(permissions);
        List<? extends SecuredObject> result = new PresentationCompiler(batchPresentation).getBatch(parameters);
        timeMeasurer.jobEnded("getObjects: " + result.size());
        if (result.size() == 0 && enablePaging && batchPresentation.getPageNumber() > 1) {
            logger.debug("resetting batch presentation to first page due to 0 results");
            batchPresentation.setPageNumber(1);
            result = getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
        }
        return result;
    }

    /**
     * Load count of {@linkplain SecuredObject} for which executors have permission on.
     * 
     * @param user
     *            User which must have permission on loaded {@linkplain SecuredObject} (at least one).
     * @param batchPresentation
     *            {@linkplain BatchPresentation} with parameters for loading {@linkplain SecuredObject}'s.
     * @param permission
     *            {@linkplain Permission}, which executors must have on {@linkplain SecuredObject}.
     * @param securedObjectTypes
     *            {@linkplain SecuredObjectType} types, used to check permissions.
     * @return Count of {@link SecuredObject}'s for which executors have permission on.
     */
    public int getPersistentObjectCount(User user, BatchPresentation batchPresentation, Permission permission, SecuredObjectType[] securedObjectTypes) {
        TimeMeasurer timeMeasurer = new TimeMeasurer(logger, 1000);
        timeMeasurer.jobStarted();
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, permission, securedObjectTypes);
        CompilerParameters parameters = CompilerParameters.createNonPaged().addPermissions(permissions);
        int count = new PresentationCompiler(batchPresentation).getCount(parameters);
        timeMeasurer.jobEnded("getCount: " + count);
        return count;
    }

    public boolean permissionExists(final Permission permission, final SecuredObject resource) {
        return getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) {
                return session.createQuery("select 0 from PermissionMapping where objectType=:objectType and objectId=:objectId and permission=:permission")
                        .setParameter("objectType", resource.getSecuredObjectType())
                        .setParameter("objectId", resource.getIdentifiableId())
                        .setParameter("permission", permission)
                        .setMaxResults(1)
                        .uniqueResult();
            }
        }) != null;
    }

    public boolean permissionExists(final Executor executor, final Permission permission, final SecuredObject resource) {
        return getHibernateTemplate().execute(new HibernateCallback<Object>() {
            @Override
            public Object doInHibernate(Session session) {
                return session.createQuery("select 0 from PermissionMapping where executor = :executor and objectType=:objectType and objectId=:objectId and permission=:permission")
                        .setParameter("executor", executor)
                        .setParameter("objectType", resource.getSecuredObjectType())
                        .setParameter("objectId", resource.getIdentifiableId())
                        .setParameter("permission", permission)
                        .setMaxResults(1)
                        .uniqueResult();
            }
        }) != null;
    }
}
