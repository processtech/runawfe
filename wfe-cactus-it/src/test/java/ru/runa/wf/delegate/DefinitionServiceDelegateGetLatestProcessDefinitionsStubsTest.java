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
package ru.runa.wf.delegate;

import java.util.List;

import lombok.val;
import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetLatestProcessDefinitionsStubsTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        batchPresentation = h.getProcessDefinitionBatchPresentation();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
        batchPresentation = null;
    }

    public void testGetLatestProcessDefinitionsStubsByAuthorizedUser() {
        List<WfDefinition> processes = definitionService.getProcessDefinitions(h.getAuthorizedUser(), batchPresentation, false);

        assertEquals("definitionDelegate.getLatestDefinitionStub() returned not expected list", 1, processes.size());
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", processes.get(0).getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestProcessDefinitionsStubsByUnauthorizedUser() {
        try {
            val processes = definitionService.getProcessDefinitions(h.getUnauthorizedUser(), batchPresentation, false);
            assertEquals(0, processes.size());
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetLatestProcessDefinitionsStubsByFakeUser() {
        try {
            definitionService.getProcessDefinitions(h.getFakeUser(), batchPresentation, false);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
