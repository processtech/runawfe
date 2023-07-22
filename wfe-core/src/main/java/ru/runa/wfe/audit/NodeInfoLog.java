package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;

/**
 * Info log from Groovy Action.
 * 
 * @author vromav
 */
@Entity
@DiscriminatorValue(value = "J")
public class NodeInfoLog extends NodeLog {
    private static final long serialVersionUID = 1L;

    public NodeInfoLog() {
    }

    public NodeInfoLog(Node node, Severity severity, String data) {
        super(node);
        addAttributeWithTruncation(ATTR_PARAM, data);
        setSeverity(severity);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeInfoLog(this);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttribute(ATTR_PARAM) };
    }
}
