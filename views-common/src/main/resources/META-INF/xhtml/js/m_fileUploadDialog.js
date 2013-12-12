/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 * 
 */

define(function() {
	return {
		initialize : function() {
			var fileUploadlg = new FileUploadDialog();
			fileUploadlg.initialize();
		}
	};

	/*
	 * 
	 */
	function FileUploadDialog() {
		var REST_END_POINT = "/services/rest/views-common/ippfileupload/upload";

		/*
		 * 
		 */
		FileUploadDialog.prototype.initialize = function() {
			var urlPrefix = window.location.href;
			urlPrefix = urlPrefix.substring(0, urlPrefix.indexOf("/plugins"));

			var interactionEndpoint = urlPrefix + REST_END_POINT;

			console.log("Interaction Rest End Point: " + interactionEndpoint);

			document.getElementById('titleText').innerHTML = payloadObj.title;
			document.getElementById('cancelButton').value = "CANCEL";
			document.getElementById('cancelButton').onclick = closePopup;
			jQuery("#openDocument").prop("checked", true);
			
			document.getElementById('acceptButton').value = "OK";
			document.getElementById('acceptButton').onclick = function() {
				var details = {};
				details.fileDescription = jQuery("#fileDescription").val();
				details.versionComment = jQuery("#versionComment").val();
				details.openDocument = jQuery("#openDocument").prop("checked");
				details.fileDetails = JSON.parse(jQuery(fileDetails).text());
				payloadObj.acceptFunction(details);
				closePopup();
			};

			jQuery("#fileUploadMsg").html(payloadObj.message);
			jQuery("#documentType").html(payloadObj.documentTypeName);
			
			document.getElementById('dialogCloseIcon').onclick = closePopup;

			jQuery("#fileUploadForm").attr("action", interactionEndpoint);
			var fileDetails = null;
			var options = {
				beforeSend : function() {
					jQuery("#progress").show();
					// clear everything
					jQuery("#bar").width('0%');
					jQuery("#message").html("");
					jQuery("#percent").html("0%");
				},
				uploadProgress : function(event, position, total,
						percentComplete) {
					jQuery("#bar").width(percentComplete + '%');
					jQuery("#percent").html(percentComplete + '%');

				},
				success : function() {
					jQuery("#bar").width('100%');
					jQuery("#percent").html('100%');

				},
				complete : function(response) {
					fileDetails = response.responseText;
					jQuery("#message").html(
							"<font color='green'>" + "File Uploaded Successfully."
									+ "</font>");
				},
				error : function() {
					jQuery("#message")
							.html(
									"<font color='red'> ERROR: unable to upload files</font>");

				}
			};
			jQuery("#fileUploadForm").ajaxForm(options);
		};
	}
});
