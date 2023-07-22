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

public class ExecutorServiceDelegateGetGroupTest extends ServletTestCase {
    private final String PREFIX = getClass().getName();
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
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

    public void testGetExecutorByNameByAuthorizedUser() {
        Group returnedBaseGroup = executorService.getExecutorByName(h.getAuthorizedUser(), PREFIX + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
        Group returnedSubGroup = executorService.getExecutorByName(h.getAuthorizedUser(), PREFIX + ServiceTestHelper.SUB_GROUP_NAME);
        Group subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", subGroup, returnedSubGroup);
    }

    public void testGetExecutorByNameByUnauthorizedUser() {
        try {
            executorService.getExecutorByName(h.getUnauthorizedUser(), PREFIX + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName()");
        } catch (AuthorizationException e) {
            // Expected.
        }
        try {
            executorService.getExecutorByName(h.getUnauthorizedUser(), PREFIX + ServiceTestHelper.SUB_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName()");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedGroupByAuthorizedUser() {
        try {
            executorService.getExecutorByName(h.getAuthorizedUser(), PREFIX + "unexistent group name");
            fail();
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetActorInsteadOfGroup() {
        try {
            Group group = executorService.<Group>getExecutorByName(h.getAuthorizedUser(),
                    PREFIX + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() where the actor really is returned.");
        } catch (ClassCastException e) {
            // Expected.
        }
    }
}
