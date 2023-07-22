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

package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

public class ExecutorServiceDelegateRemoveExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Actor actor;
    private Group group;
    private Group subGroup;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        executorService = Delegates.getExecutorService();
        h = new ServiceTestHelper(getClass().getName());
        h.createDefaultExecutorsMap();

        actor = h.getBaseGroupActor();
        group = h.getBaseGroup();
        subGroup = h.getSubGroup();

        h.setPermissionsToAuthorizedActor(readPermissions, actor);
        h.setPermissionsToAuthorizedActor(updatePermissions, group);
        h.setPermissionsToAuthorizedActor(readPermissions, subGroup);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        group = null;
    }

    private Actor getActor() {
        return executorService.getExecutor(h.getAdminUser(), actor.getId());
    }

    private Group getGroup() {
        return executorService.getExecutor(h.getAdminUser(), group.getId());
    }

    private Group getSubGroup() {
        return executorService.getExecutor(h.getAdminUser(), subGroup.getId());
    }

    public void testRemoveActorFromGroupByAuthorizedUser() {
        assertTrue("Executor is not in group before removing", h.isExecutorInGroup(actor, group));

        h.setPermissionsToAuthorizedActor(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), Lists.newArrayList(actor.getId()), group.getId());
            fail("Actor removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, group);
        executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), Lists.newArrayList(actor.getId()), group.getId());
        assertFalse("Executor not removed from group ", h.isExecutorInGroup(getActor(), getGroup()));
    }

    public void testRemoveSubGroupFromGroupByAuthorizedUserWithReadPermissionOnGroup() {
        assertTrue("Executor is not in group before removing", h.isExecutorInGroup(subGroup, group));

        h.setPermissionsToAuthorizedActor(readPermissions, group);
        try {
            executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), Lists.newArrayList(subGroup.getId()), group.getId());
            fail("Subgroup removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, group);
        executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), Lists.newArrayList(subGroup.getId()), group.getId());
        assertFalse("Executor not removed from group ", h.isExecutorInGroup(getSubGroup(), getGroup()));
    }

    public void testRemoveActorFromGroupByUnAuthorizedUser() {
        try {
            executorService.removeExecutorsFromGroup(h.getUnauthorizedUser(), Lists.newArrayList(actor.getId()), group.getId());
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testRemoveSubGroupFromGroupByUnAuthorizedUser() {
        try {
            executorService.removeExecutorsFromGroup(h.getUnauthorizedUser(), Lists.newArrayList(subGroup.getId()), group.getId());
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
