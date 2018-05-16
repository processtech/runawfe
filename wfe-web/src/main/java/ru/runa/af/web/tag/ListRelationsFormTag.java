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
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveRelationAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SecuredObjectCheckboxTDBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listRelationsForm")
public class ListRelationsFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;
    private boolean formButtonVisible;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        formButtonVisible = Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE_RELATION, RelationsGroupSecure.INSTANCE);
        List<Relation> relations = Delegates.getRelationService().getRelations(getUser(), getBatchPresentation());
        TableBuilder tableBuilder = new TableBuilder();
        TDBuilder checkboxBuilder = new SecuredObjectCheckboxTDBuilder(Permission.UPDATE_RELATION) {

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return formButtonVisible;
            }
        };
        TDBuilder[] builders = BatchPresentationUtils.getBuilders(new TDBuilder[] { checkboxBuilder }, getBatchPresentation(), null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(relations, getBatchPresentation(), pageContext, WebResources.ACTION_MAPPING_MANAGE_RELATION,
                getReturnAction(), new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(getBatchPresentation(), 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_RELATIONS.message(pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return formButtonVisible;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return formButtonVisible;
    }

    @Override
    protected boolean isMultipleSubmit() {
        return false;
    }

    @Override
    protected String getFormButtonName() {
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
