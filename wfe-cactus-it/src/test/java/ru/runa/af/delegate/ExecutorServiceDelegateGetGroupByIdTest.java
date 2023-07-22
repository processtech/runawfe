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
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

public class ExecutorServiceDelegateGetGroupByIdTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        executorsMap = h.getDefaultExecutorsMap();
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, group);
        h.setPermissionsToAuthorizedActor(pp, h.getSubGroup());
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroupActor());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        group = null;
        executorsMap = null;
    }

    public void testGetExecutorByAuthorizedUser() {
        Group returnedBaseGroup = executorService.getExecutor(h.getAuthorizedUser(), group.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
        Group subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        Group returnedSubGroup = executorService.getExecutor(h.getAuthorizedUser(), subGroup.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", subGroup, returnedSubGroup);
    }

    public void testGetExecutorByUnauthorizedUser() {
        try {
            executorService.getExecutor(h.getUnauthorizedUser(), group.getId());
            fail("businessDelegate allow to getExecutor()");
        } catch (AuthorizationException e) {
            // Expected.
        }
        try {
            executorService.getExecutor(h.getUnauthorizedUser(), h.getSubGroup().getId());
            fail("businessDelegate allow to getSubGroup()");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedGroupByAuthorizedUser() {
        try {
            executorService.getExecutor(h.getAuthorizedUser(), -1L);
            fail();
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetActorInsteadOfGroup() {
        try {
            Group actor = executorService.<Group>getExecutor(h.getAuthorizedUser(), h.getBaseGroupActor().getId());
            fail("businessDelegate allow to getExecutor() where the actor really is returned.");
        } catch (ClassCastException e) {
            // Expected.
        }
    }
}
