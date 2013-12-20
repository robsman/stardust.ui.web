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

			var cancelButton = jQuery("#cancelButton"); 
			cancelButton.val("CANCEL");
			cancelButton.click(closePopup);
			jQuery("#openDocument").prop("checked", true);
			
			//document.getElementById('acceptButton').value = "OK";
			var acceptButton = jQuery("#acceptButton"); 
			acceptButton.val("OK");
			
			acceptButton.click(function() {
				var details = {};
				details.fileDescription = jQuery("#fileDescription").val();
				details.versionComment = jQuery("#versionComment").val();
				details.openDocument = jQuery("#openDocument").prop("checked");
				details.fileDetails = JSON.parse(jQuery(fileDetails).text());
				payloadObj.acceptFunction(details);
				closePopup();
			});

			jQuery("#fileUploadMsg").html(payloadObj.message);
			jQuery("#documentType").html(payloadObj.documentTypeName);

			//document.getElementById('dialogCloseIcon').onclick = closePopup;
			jQuery("#dialogCloseIcon").click(closePopup);

			jQuery("#fileUploadForm").attr("action", interactionEndpoint);
			var fileDetails = null;
			var options = {
				beforeSend : function() {
					jQuery("#fileUploadprogress").show();
					// clear everything
					jQuery("#progressBar").width('0%');
					jQuery("#confirmationMessage").html("");
					jQuery("#fileUploadPercent").html("0%");
				},
				uploadProgress : function(event, position, total,
						percentComplete) {
					jQuery("#progressBar").width(percentComplete + '%');
					jQuery("#fileUploadPercent").html(percentComplete + '%');

				},
				success : function() {
					jQuery("#progressBar").width('50%');
					jQuery("#fileUploadPercent").html('100%');

				},
				complete : function(response) {
					fileDetails = response.responseText;
					jQuery("#confirmationMessage").html(
							"<span>" + "File Uploaded Successfully."
									+ "</span>");
				},
				error : function() {
					jQuery("#confirmationMessage")
							.html(
									"<font color='red'> ERROR: unable to upload files</font>");
				}
			};
			jQuery("#fileUploadForm").ajaxForm(options);
		};
	}
});
