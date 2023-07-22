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
package ru.runa.wfe.execution.dto;

import com.google.common.base.Objects;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.NonNull;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.user.Executor;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfSwimlane implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private SwimlaneDefinition definition;
    private Executor executor;

    /**
     * for web services
     */
    public WfSwimlane() {
    }

    public WfSwimlane(@NonNull SwimlaneDefinition definition, Swimlane swimlane, Executor assignedExecutor) {
        this.definition = definition;
        this.id = swimlane != null ? swimlane.getId() : null;
        this.executor = assignedExecutor;
    }
    
    public SwimlaneDefinition getDefinition() {
        return definition;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Long getId() {
        return id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(definition.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfSwimlane) {
            return Objects.equal(definition.getName(), ((WfSwimlane) obj).definition.getName());
        }
        return super.equals(obj);
    }

}
