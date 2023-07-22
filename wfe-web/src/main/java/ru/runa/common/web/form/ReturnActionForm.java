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
package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

public class ReturnActionForm extends IdForm {
    private static final long serialVersionUID = 8416054409153112757L;

    public static final String RETURN_ACTION = "returnAction";

    private String returnAction;

    public String getReturnAction() {
        return returnAction;
    }

    public void setReturnAction(String forwardName) {
        returnAction = forwardName;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (returnAction == null || returnAction.length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
