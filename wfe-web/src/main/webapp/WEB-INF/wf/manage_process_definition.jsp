<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ page import="ru.runa.common.web.form.IdForm" %>

<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">
	<script type="text/javascript" src="<html:rewrite page="/js/processgraphutils.js" />">c=0;</script>
</tiles:put>

<tiles:put name="body" type="string" >
<%
	String parameterName = IdForm.ID_INPUT_NAME;
	long id = Long.parseLong(request.getParameter(parameterName));
%>

<wf:processDefinitionInfoForm identifiableId='<%= id %>'>	
<table width="100%">
	<tr>
		<td align="right">
			<wf:updatePermissionsOnIdentifiableLink identifiableId='<%=id %>' href='<%= "/manage_process_definition_permissions.do?" + parameterName+ "=" + id %>'  />
		</td>
	<tr>
</table>
</wf:processDefinitionInfoForm>

<wf:redeployDefinitionForm identifiableId='<%= id %>'  />
<wf:definitionGraphForm identifiableId='<%= id %>' />	


</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>