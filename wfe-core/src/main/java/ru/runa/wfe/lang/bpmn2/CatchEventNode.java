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
package ru.runa.wfe.lang.bpmn2;

import java.util.List;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.collect.Lists;

public class CatchEventNode extends BaseMessageNode implements BoundaryEventContainer, BoundaryEvent {
    private static final long serialVersionUID = 1L;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();
    private Boolean boundaryEventInterrupting;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    @Override
    public Boolean getBoundaryEventInterrupting() {
        return boundaryEventInterrupting;
    }

    @Override
    public void setBoundaryEventInterrupting(Boolean boundaryEventInterrupting) {
        this.boundaryEventInterrupting = boundaryEventInterrupting;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.RECEIVE_MESSAGE;
    }

    @Override
    public void cancelBoundaryEvent(Token token) {
    }

    @Override
    public TaskCompletionInfo getTaskCompletionInfoIfInterrupting() {
        return TaskCompletionInfo.createForHandler(getEventType().name());
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
    }
}
