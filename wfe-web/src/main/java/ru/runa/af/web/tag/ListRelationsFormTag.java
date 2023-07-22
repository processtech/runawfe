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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveRelationAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.PropertyTdBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TdBuilder.Env;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listRelationsForm")
public class ListRelationsFormTag extends BatchReturningTitledFormTag {
    
    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.DELETE, SecuredObjectType.RELATION);
    }
    
    @Override
    protected boolean isSubmitButtonVisible() {
        return Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.DELETE, SecuredObjectType.RELATION);
    }   
    
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ, SecuredSingleton.RELATIONS);
        List<Relation> relations = Delegates.getRelationService().getRelations(getUser(), getBatchPresentation());
        Set<Long> allowedIds = Delegates.getAuthorizationService().filterAllowedIds(getUser().getActor(), Permission.READ, SecuredObjectType.RELATION, relations.stream().map( (r)-> r.getId() ).collect(Collectors.toList()));
        relations.removeIf( (r) -> !allowedIds.contains(r.getId()));
        
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder checkboxBuilder = new CheckboxTdBuilder(null, null) {

            @Override
            protected String getIdValue(Object object) {
                return String.valueOf(((Relation) object).getId());
            }

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return true;
            }
        };
        BatchPresentation batchPresentation = getBatchPresentation();
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { checkboxBuilder }, batchPresentation, null);
        for (TdBuilder td: builders) {
            if (td instanceof BaseTdBuilder) {
                ((BaseTdBuilder) td).setPermission(Permission.READ);
            }
        }
        RowBuilder rowBuilder = new ReflectionRowBuilder(relations, batchPresentation, pageContext, WebResources.ACTION_MAPPING_MANAGE_RELATION,
                getReturnAction(), new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_RELATIONS.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveRelationAction.ACTION_PATH;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            return Commons.getActionUrl(baseUrl, RelationForm.RELATION_ID, ((Relation) item).getId(), pageContext, PortletUrlType.Action);
        }

    }
}
