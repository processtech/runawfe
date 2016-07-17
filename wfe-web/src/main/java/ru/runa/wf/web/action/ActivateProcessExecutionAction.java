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
package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @since 4.3.0
 */
public class ActivateProcessExecutionAction extends ActionBase {
    public static final String ACTION_PATH = "/activateProcessExecution";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdForm form = (IdForm) actionForm;
        try {
            Delegates.getExecutionService().activateProcess(getLoggedUser(request), form.getId());
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        addMessage(request, new ActionMessage(MessagesProcesses.PROCESS_ACTIVATED.getKey()));
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }
}
