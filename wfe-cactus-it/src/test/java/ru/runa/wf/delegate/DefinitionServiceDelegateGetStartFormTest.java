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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetStartFormTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;
    private Long definitionId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        definitionId = definitionService.getLatestProcessDefinition(h.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME).getId();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetStartFormTestByAuthorizedUser() {
        Interaction startForm = definitionService.getStartInteraction(h.getAuthorizedUser(), definitionId);

        // / TO DO : xml read from forms.xml & processdefinition.xml
        // TODO assertEquals("start form name differ from original",
        // "request a payraise", startForm.getStateName());
        if (false) {
            assertEquals("start form name differ from original", "html", startForm.getType());
            Map<String, VariableDefinition> vars = startForm.getVariables();
            val actual = new ArrayList<String>();
            for (VariableDefinition var : vars.values()) {
                actual.add(var.getName());
            }

            List<String> expected = Lists.newArrayList("reason", "amount.asked", "time", "file", "actor");
            ArrayAssert.assertWeakEqualArrays("Variables from start from differ from declaration", expected, actual);
        }
    }

    public void testGetStartFormTestByUnauthorizedUser() {
        definitionService.getStartInteraction(h.getUnauthorizedUser(), definitionId);
    }

    public void testGetStartFormTestByFakeUser() {
        try {
            definitionService.getStartInteraction(h.getFakeUser(), definitionId);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetStartFormTestByAuthorizedUserWithInvalidDefinitionId() {
        try {
            definitionService.getStartInteraction(h.getAuthorizedUser(), -1L);
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }
}
