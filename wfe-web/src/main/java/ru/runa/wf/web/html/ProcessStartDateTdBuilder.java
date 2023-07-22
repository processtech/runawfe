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
package ru.runa.wf.web.html;

import java.util.Date;

import ru.runa.common.web.html.BaseDateTdBuilder;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.execution.dto.WfProcess;

/**
 * Created on 16.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class ProcessStartDateTdBuilder extends BaseDateTdBuilder<WfProcess> {

    @Override
    protected Date getDate(WfProcess process) {
        return process.getStartDate();
    }

    @Override
    protected Long getId(WfProcess process) {
        return process.getId();
    }

    @Override
    protected String getActionMapping() {
        return ShowGraphModeHelper.getManageProcessAction();
    }

}
