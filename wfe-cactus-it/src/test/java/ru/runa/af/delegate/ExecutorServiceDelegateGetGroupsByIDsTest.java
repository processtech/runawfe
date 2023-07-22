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
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.*;

import java.util.List;

/**
 * Created on 16.02.2005
 */
public class ExecutorServiceDelegateGetGroupsByIDsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private List<Group> additionalGroups;
    private List<Long> additionalGroupsIDs;

    private final List<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        additionalGroups = h.createGroupArray("additionalG", "Additional Group");
        h.setPermissionsToAuthorizedActor(readPermissions, additionalGroups);

        additionalGroupsIDs = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIDs.add(group.getId());
        }
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        additionalGroupsIDs = null;
        additionalGroups = null;
    }

    public void testGetGroupsByAuthorizedUser() {
        List<Group> returnedGroups = h.getExecutors(h.getAuthorizedUser(), additionalGroupsIDs);
        ArrayAssert.assertWeakEqualArrays("Groups retuned by businessDelegate differes with expected", returnedGroups, additionalGroups);
    }

    public void testGetGroupsByUnauthorizedUser() {
        try {
            h.getExecutors(h.getUnauthorizedUser(), additionalGroupsIDs);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedGroupByAuthorizedUser() {
        additionalGroupsIDs = Lists.newArrayList(-1L, -2L, -3L);
        try {
            h.getExecutors(h.getAuthorizedUser(), additionalGroupsIDs);
            fail("businessDelegate does not throw Exception to getGroups() for unexisting groups");
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetActorsInsteadOfGroups() {
        List<Executor> additional = h.createMixedActorsGroupsArray("mixed", "Additional mixed");
        h.setPermissionsToAuthorizedActor(readPermissions, additional);

        additionalGroupsIDs = Lists.newArrayList();
        for (Executor executor : additional) {
            additionalGroupsIDs.add(executor.getId());
        }
        try {
            List<Actor> actors = h.<Actor>getExecutors(h.getAuthorizedUser(), additionalGroupsIDs);
            // TODO assertTrue("businessDelegate allow to getGroup() where the actor really is returned.", false);
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
            fail("TODO trap");
        }
    }
}
