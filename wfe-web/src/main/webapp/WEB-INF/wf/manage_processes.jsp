<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/wf.tld" prefix="wf" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<tiles:insert page="/WEB-INF/af/main_layout.jsp" flush="true">

<tiles:put name="head" type="string">

	<script type="text/javascript">
	var activeLabel = "<bean:message key="process.execution.status.active" />";
	var failedLabel = "<bean:message key="process.execution.status.failed" />";
	var suspendedLabel = "<bean:message key="process.execution.status.suspended" />";
	var endedLabel = "<bean:message key="process.execution.status.ended" />";
	$(document).ready(function() {
		var input = $("table.view-setup tr[field='batch_presentation.process.execution_status'] input[name='fieldsToFilterCriterias']");
		var select = $("<select />", { name: input.attr("name") });
		$("<option />", {val: "", text: ""}).appendTo(select);
		$("<option />", {val: "ACTIVE", text: activeLabel}).appendTo(select);
		$("<option />", {val: "FAILED", text: failedLabel}).appendTo(select);
		$("<option />", {val: "SUSPENDED", text: suspendedLabel}).appendTo(select);
		$("<option />", {val: "ENDED", text: endedLabel}).appendTo(select);
		select.val(input.val());
		input.replaceWith(select);
	});
	</script>

</tiles:put>
<tiles:put name="body" type="string">
<%
	String returnAction = "/manage_processes.do";
%>

<wf:listProcessesForm batchPresentationId="listProcessesForm" returnAction="<%= returnAction %>">
	<div>
		<wf:viewControlsHideableBlock hideableBlockId="listProcessesForm"  returnAction="<%= returnAction %>">
			<wf:tableViewSetupForm batchPresentationId="listProcessesForm" returnAction="<%= returnAction %>" excelExportAction="/exportExcelProcesses" />
		</wf:viewControlsHideableBlock>
	</div>
</wf:listProcessesForm>
</tiles:put>
<tiles:put name="messages" value="../common/messages.jsp" />
</tiles:insert>