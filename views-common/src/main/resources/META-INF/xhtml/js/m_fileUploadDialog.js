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
		var REST_END_POINT_I18N = "/services/rest/views-common/ippfileupload/i18n";

		/*
		 * 
		 */
		FileUploadDialog.prototype.initialize = function() {
			
			var urlPrefix = window.location.href;
			urlPrefix = urlPrefix.substring(0, urlPrefix.indexOf("/plugins"));

			var interactionEndpoint = urlPrefix + REST_END_POINT;

			console.log("Interaction Rest End Point: " + interactionEndpoint);

			InfinityBPMI18N.initPluginProps({
				pluginName : "fileUploadBundle",
				singleEndPoint : urlPrefix + REST_END_POINT_I18N
			});

			var messageBundle = InfinityBPMI18N.fileUploadBundle;
			
			var cancelButton = jQuery("#cancelButton"); 
			cancelButton.click(closePopup);
			jQuery("#dialogCloseIcon").click(closePopup);

			var acceptButton = jQuery("#acceptButton");
			acceptButton.click(function() {
				var details = {};
				details.fileDescription = jQuery("#fileDescription").val();
				details.versionComment = jQuery("#versionComment").val();
				details.openDocument = jQuery("#openDocument").prop("checked");
				details.fileDetails = JSON.parse(jQuery(fileDetails).text());
				payloadObj.acceptFunction(details);
				closePopup();
			});
			
			jQuery("#openDocument").prop("checked", true);
			jQuery("#fileUploadForm").attr("action", interactionEndpoint);
			jQuery("#documentType").html(payloadObj.documentTypeName);

			//set labels and other data
			if (payloadObj.title) {
				jQuery("#titleText").html(payloadObj.title);
			} else {
				jQuery("#titleText").html(messageBundle.getProperty("title"));
			}
			if (payloadObj.message) {
				jQuery("#fileUploadMsg").html(payloadObj.message);
			}
			
			jQuery("#dialogCloseIcon").attr("title", messageBundle.getProperty("close"));
			jQuery("#documentTypeLabel").html(messageBundle.getProperty("documentType"));
			jQuery("#fileDescriptionLabel").html(messageBundle.getProperty("desciription"));
			jQuery("#versionCommentLabel").html(messageBundle.getProperty("comments"));
			jQuery("#fileUploadFormLabel").html(messageBundle.getProperty("formLabel"));
			jQuery("#openDocumentLabel").html(messageBundle.getProperty("openDocument"));
			
			cancelButton.attr("value", messageBundle.getProperty("close"));
			acceptButton.attr("value", messageBundle.getProperty("ok"));
			
			jQuery("#uploadButton").val(messageBundle.getProperty("upload"));
			
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
							"<span>" + messageBundle.getProperty("confirmationMsg")
									+ "</span>");
				},
				error : function() {
					jQuery("#confirmationMessage")
							.html(
									"<font color='red'>" + messageBundle.getProperty("errorMsg") + "</font>");
				}
			};
			jQuery("#fileUploadForm").ajaxForm(options);
		};
	}
});
