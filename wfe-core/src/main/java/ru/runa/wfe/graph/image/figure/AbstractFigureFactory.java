package ru.runa.wfe.graph.image.figure;

import ru.runa.wfe.lang.Node;

public abstract class AbstractFigureFactory {

    public abstract AbstractFigure createFigure(Node node, boolean useEdgingOnly);

    public abstract TransitionFigure createTransitionFigure();
}
