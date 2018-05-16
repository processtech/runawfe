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
package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.GrantPermissionsOnBotStationAction;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.SecuredObject;

/**
 * Created on 31.08.2004
 * 
 * @author stan79
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "ListExecutorsWithoutPermissionsOnBotStationFormTag")
public class ListExecutorsWithoutPermissionsOnBotStationFormTag extends ListExecutorsWithoutPermissionsBase {

    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return GrantPermissionsOnBotStationAction.ACTION_PATH;
    }

    @Override
    protected SecuredObject getSecuredObject() {
        return BotStation.INSTANCE;
    }
}
