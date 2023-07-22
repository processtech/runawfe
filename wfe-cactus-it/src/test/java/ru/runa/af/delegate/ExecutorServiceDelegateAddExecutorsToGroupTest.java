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
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

/*
 */
public class ExecutorServiceDelegateAddExecutorsToGroupTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Actor actor;
    private Group additionalGroup;
    private Actor additionalActor;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        executorService = Delegates.getExecutorService();
        h = new ServiceTestHelper(getClass().getName());
        h.createDefaultExecutorsMap();

        actor = h.getBaseGroupActor();
        h.setPermissionsToAuthorizedActor(readPermissions, actor);

        additionalGroup = h.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActor = h.createActorIfNotExist("additionalA", "Additional Actor");
        h.setPermissionsToAuthorizedActor(readPermissions, additionalActor);
        h.setPermissionsToAuthorizedActor(readPermissions, additionalGroup);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        additionalActor = null;
        additionalGroup = null;
    }

    public void testAddExecutorByAuthorizedUser() {
        assertFalse("Executor not added to group ", h.isExecutorInGroup(additionalActor, additionalGroup));
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), Lists.newArrayList(additionalActor.getId()),
                    additionalGroup.getId());
            fail("Executor added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, additionalActor);
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);

        executorService.addExecutorsToGroup(h.getAuthorizedUser(), Lists.newArrayList(additionalActor.getId()), additionalGroup.getId());

        additionalActor = executorService.getExecutor(h.getAuthorizedUser(), additionalActor.getId());
        additionalGroup = executorService.getExecutor(h.getAuthorizedUser(), additionalGroup.getId());

        assertTrue("Executor not added to group ", h.isExecutorInGroup(additionalActor, additionalGroup));
    }

    public void testAddExecutorByUnAuthorizedUser() {
        try {
            executorService.addExecutorsToGroup(h.getUnauthorizedUser(), Lists.newArrayList(actor.getId()), additionalGroup.getId());
            assertTrue("Executor not added to group ", h.isExecutorInGroup(additionalActor, additionalGroup));
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testAddFakeExecutor() {
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), Lists.newArrayList(h.getFakeActor().getId()),
                    additionalGroup.getId());
            fail("Executor added to group ");
        } catch (AuthorizationException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail("TODO trap");
        }
    }
}
