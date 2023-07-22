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
package ru.runa.wfe.graph.image;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.graph.image.figure.bpmn.BpmnFigureFactory;
import ru.runa.wfe.graph.image.figure.uml.UmlFigureFactory;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.GraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.TaskDeadlineUtils;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphImageBuilder {
    private final ProcessDefinition processDefinition;
    private Token highlightedToken;
    private final Map<String, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigure, RenderHits> transitionFigures = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newLinkedHashMap();
    private final boolean smoothTransitions;

    public GraphImageBuilder(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        this.smoothTransitions = DrawProperties.isSmoothLinesEnabled() && processDefinition.getDeployment().getLanguage() == Language.BPMN2;
    }

    public void setHighlightedToken(Token highlightedToken) {
        this.highlightedToken = highlightedToken;
    }

    public byte[] createDiagram(Process process, ProcessLogs logs, Set<String> activeNodeIds) throws Exception {
        AbstractFigureFactory factory;
        if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
            factory = new BpmnFigureFactory();
        } else {
            factory = new UmlFigureFactory();
        }
        for (Node node : processDefinition.getNodes(false)) {
            AbstractFigure nodeFigure = factory.createFigure(node, DrawProperties.useEdgingOnly());
            allNodeFigures.put(node.getNodeId(), nodeFigure);
        }
        for (Node node : processDefinition.getNodes(false)) {
            String nodeId = node.getNodeId();
            AbstractFigure nodeFigure = allNodeFigures.get(node.getNodeId());
            Preconditions.checkNotNull(nodeFigure, "Node figure not found by id " + nodeId);
            if (!DrawProperties.useEdgingOnly()) {
                nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
            }
            int leavingTransitionsCount = node.getLeavingTransitions().size();
            if (node.getNodeType() == NodeType.END_PROCESS) {
                continue;
            }
            for (Transition transition : node.getLeavingTransitions()) {
                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                TransitionFigure transitionFigure = factory.createTransitionFigure();
                transitionFigure.init(transition, nodeFigure, figureTo, smoothTransitions);
                if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
                    NodeType nodeType = node.getNodeType();
                    transitionFigure.setExclusive(
                            nodeType != NodeType.EXCLUSIVE_GATEWAY && nodeType != NodeType.PARALLEL_GATEWAY && leavingTransitionsCount > 1);
                }
                nodeFigure.addTransition(transitionFigure);
                if (!DrawProperties.useEdgingOnly()) {
                    transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
                }
            }
        }
        for (TransitionLog transitionLog : logs.getLogs(TransitionLog.class)) {
            Transition transition = transitionLog.getTransitionOrNull(processDefinition);
            if (transition != null) {
                RenderHits renderHits = new RenderHits(DrawProperties.getHighlightColor(), true);
                // Mark 'from' block as PASSED
                AbstractFigure nodeModelFrom = allNodeFigures.get(transition.getFrom().getTransitionNodeId(false));
                if (!nodeFigures.containsKey(nodeModelFrom) || !nodeFigures.get(nodeModelFrom).isPassed()) {
                    // do not override RenderHits for subprocesses
                    nodeFigures.put(nodeModelFrom, renderHits);
                }
                // Mark 'to' block as PASSED
                AbstractFigure nodeModelTo = allNodeFigures.get(transition.getTo().getTransitionNodeId(true));
                nodeFigures.put(nodeModelTo, renderHits);
                if (nodeModelTo.getNode() instanceof SubprocessNode) {
                    fillSubprocess(nodeModelTo, activeNodeIds);
                }
                if (nodeModelTo.getNode() instanceof BoundaryEventContainer) {
                    for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) nodeModelTo.getNode()).getBoundaryEvents()) {
                        AbstractFigure boundaryEventFigure = allNodeFigures.get(((GraphElement) boundaryEvent).getNodeId());
                        if (boundaryEventFigure == null) {
                            // case for EmbeddedSubprocessEndNode
                            continue;
                        }
                        nodeFigures.put(boundaryEventFigure, new RenderHits(DrawProperties.getHighlightColor(), false));
                    }
                }
                // Mark transition as PASSED
                TransitionFigure transitionFigure = nodeModelFrom.getTransition(transition.getName());
                transitionFigures.put(transitionFigure, renderHits);
            }
        }

        fillTasks(logs);
        GraphImage graphImage = new GraphImage(processDefinition, transitionFigures, nodeFigures);
        return graphImage.getImageBytes();
    }

    private void fillSubprocess(AbstractFigure nodeModelTo, Set<String> activeNodeIds) {
        SubprocessNode subprocessNode = (SubprocessNode) nodeModelTo.getNode();
        String subprocessNodeId = subprocessNode.getNodeId();
        boolean active = activeNodeIds.contains(subprocessNodeId);
        boolean highlighted;
        if (subprocessNode.isEmbedded()) {
            String prefix = processDefinition.getEmbeddedSubprocessByNameNotNull(subprocessNode.getSubProcessName()).getNodeId() + ".";
            if (!active) {
                // correct behavior for in complex case when one of token already leaved out of subprocess
                for (String nodeId : activeNodeIds) {
                    if (nodeId.startsWith(prefix)) {
                        active = true;
                        break;
                    }
                }
            }
            highlighted = highlightedToken != null && highlightedToken.getNodeId().startsWith(prefix);
        } else {
            highlighted = highlightedToken != null && highlightedToken.getNodeId().equals(subprocessNodeId);
        }
        nodeFigures.put(nodeModelTo, new RenderHits(highlighted || !active ? DrawProperties.getHighlightColor() : DrawProperties.getBaseColor(),
                true, active));
    }

    private void fillTasks(ProcessLogs logs) {
        for (Map.Entry<TaskCreateLog, TaskEndLog> entry : logs.getTaskLogs().entrySet()) {
            boolean activeTask = entry.getValue() == null;
            Date deadlineDate = entry.getKey().getDeadlineDate();
            Date endDate = activeTask ? new Date() : entry.getValue().getCreateDate();
            AbstractFigure figure = allNodeFigures.get(entry.getKey().getNodeId());
            if (figure == null) {
                // ru.runa.wfe.audit.TaskCreateLog.getNodeId() = null for old
                // tasks
                continue;
            }
            Date deadlineWarningDate = TaskDeadlineUtils.getDeadlineWarningDate(entry.getKey().getCreateDate(), deadlineDate);
            Color color = null;
            if (activeTask) {
                color = DrawProperties.getBaseColor();
                if (highlightedToken != null && Objects.equal(entry.getKey().getTokenId(), highlightedToken.getId())) {
                    color = DrawProperties.getHighlightColor();
                }
            }
            if (deadlineDate != null && deadlineDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getAlarmColor();
            } else if (deadlineWarningDate != null && deadlineWarningDate.getTime() < endDate.getTime()) {
                color = DrawProperties.getLightAlarmColor();
            }
            if (color != null) {
                nodeFigures.put(figure, new RenderHits(color, true, activeTask));
            }
        }
    }

}
