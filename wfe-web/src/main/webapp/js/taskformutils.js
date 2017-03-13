var ie6compatibility = $.browser.msie && $.browser.version < 8;

$(function() {
	$(document).bind('drop dragover', function (e) {
		e.preventDefault();
	});
	$('.dropzone').each(function () {
		initFileInput($(this));
	});
	$(".dropzone").bind("dragleave dragend drop", function (e) {
		$(this).removeClass("dropzonehover");
	});
	$(".dropzone").bind('dragover', function (e) {
		$(this).addClass("dropzonehover");
	});
	if (ie6compatibility) {
		$(".inputFileContainer").css("width", "400px");
		$(".inputFileAjax").css({"width": "77px", "height": "26px", "cursor": "pointer", "filter": "alpha(opacity=50)"});
		$(".inputFileAttachButtonDiv").css("width", "170px");
		$(".inputFileAttach").css("cursor", "default");
	}
	$(document).delegate(".inputFileDelete", "click", function() {
		deleteFile($(this).attr("inputId"));
	});
	$(".tabs").tabs();
	$(".inputText").jqte();
});

function initFileInput(dropzone) {
	var progressBar = dropzone.parent().find(".progressbar");
	var progressBarLine = progressBar.find(".line");
	var fileInput = dropzone.find(".inputFile");
	var inputId = fileInput.attr("name");	
	dropzone.fileupload({
		dataType: "json",
		url: "/wfe/upload?id=" + id,
		fileInput: fileInput,
		done: function (e, data) {
			var statusText = progressBar.find(".statusText");
			var statusImg = progressBar.find("img");
			var label = data.result.name + "<span style='color: #888'> - " + data.result.size + "</span>";
			statusImg.attr("src", "/wfe/images/delete.png");
			statusImg.addClass("inputFileDelete");
			statusText.html("<a href='/wfe/upload?action=view&inputId=" + inputId + "&id=" + id + "'>" + label + "</a>");
			func1095(true, id, inputId);
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			progressBarLine.css("width", progress + "%");
		},
		dropZone: dropzone
	}).bind("fileuploadsubmit", function (e, data) {
		data.formData = {
			inputId: inputId
		};
		dropzone.hide();
		progressBar.show();
		$(".inputFileContainer").focus();
	}).bind('fileuploadfail', function (e, data) {
		var statusText = progressBar.find(".statusText");
		var statusImg = progressBar.find("img");
		statusImg.attr("src", "/wfe/images/error.gif");
		statusImg.addClass("inputFileDelete");
		statusText.html(data.textStatus);
		$(".inputFileContainer").focus();
	});
}

function deleteFile(inputId) {
	var dropzone = $("input[name='" + inputId + "'][current]").parent().parent();
	dropzone.show();
	jQuery.ajax({
		type: "GET",
		url: "/wfe/upload",		
		data: {
			action: "delete", 
			id: id,
	    	inputId: inputId,
	    	timestamp: new Date().getTime()
		},
		dataType: "html",
		success: function(msg) {
			var progressBar = dropzone.parent().find(".progressbar");
			progressBar.hide();
			var statusText = progressBar.find(".statusText");
			statusText.html(loadingMessage);
			var statusImg = progressBar.find("img");
			statusImg.attr("src", "/wfe/images/loading.gif");
			statusImg.removeClass("inputFileDelete");
			var progressBarLine = progressBar.find(".line");
			progressBarLine.css("width", "0%");
			if (ie6compatibility) {
				//alert("inputFileAjax visibility = " + $(".inputFileAjax").css("visibility"));
			}
		}
	});
}

/**
 * Bug fix #1095
 */
function func1095(flag, id, inputId) {
	jQuery.ajax({
		type: "GET",
		url: "/wfe/upload1095",
		data: {
			upload: flag,
			id: id,
			file: inputId
		}
	}).fail(function() {
		alert("Didn't work fix for bug 1095")
	})
}
