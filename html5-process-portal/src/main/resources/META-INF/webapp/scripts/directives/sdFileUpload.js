/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Nikhil.Gahlot
 */
(function() {
   'use strict';

   angular.module('bpm-common').directive('sdFileUpload', [ FileUpload ]);

   /*
	 * Directive class
	 * 
	 * 		Attributes			Type	Required	Default
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * 		nd-model			=		Yes
	 * 		sda-title 			@		No
	 * 		sda-end-point 		@		No
	 * 		sda-multiple 		@ 		No			false
	 */
   function FileUpload() {

      return {
         restrict : 'A',
         template :
        	 '<span>' 
        	 	+ '<span class="iPopupDialogContentText" id="fileUploadFormLabel" ng-bind="title"></span>'
        	 	+ '<form id="fileUploadForm" method="post" enctype="multipart/form-data" style="width: 100%">'
	        	 	+ '<input id="file" type="file" size="35" name="upload" class="iPopupDialogControlButton" aid="ChooseFile"/>'
	        	 	+ '<input id="uploadButton"	ng-disabled="true" type="submit" value="Upload" class="iPopupDialogControlButton button" aid="Upload"/>'
	        	 	+ '<span  id="confirmationMessage" style="color: green; margin: 10px" class="iPopupDialogContentText"></span>'
	        	+ '</form>'
	        	+ '<div id="fileUploadprogress">'
	        	+ '<div id="progressBar">'
	        		+ '<div id="fileUploadPercentFill"></div>'
	        		+ '<div id="fileUploadPercent" class="iPopupDialogContentText">0%</div>'
	        		+ '</div>'
	        	+ '</div>'
        	 + '</span>',
		 scope : {
			 title: '@sdaTitle',
			 bindModel: '=ngModel'
		 },
         controller : [ '$scope', '$attrs', '$element', 'sgI18nService', 'sdLoggerService', 'sdUtilService', FileUploadController ]
      };

   }

	function FileUploadController($scope, $attrs, $element, sgI18nService, sdLoggerService, sdUtilService) {
		var trace = sdLoggerService.getLogger('bpm-common.sdFileUpload');
		
		var self = this;
		var REST_END_POINT = sdUtilService.getBaseUrl() + "services/rest/portal/file-upload/upload";

		FileUploadController.prototype.initialise = function() {
			
			if (!$attrs.ngModel) {
				trace.error("ngModel is not defined.");
				return;
		    }
			
			var endPoint = REST_END_POINT;
			if (angular.isDefined($attrs.sdaEndPoint)) {
				endPoint = parseAttribute($scope.$parent, $attrs.sdaEndPoint);
			}
			
			var interactionEndpoint = endPoint;
			$element.find("#fileUploadForm").attr("action", interactionEndpoint);
			
			var options = {
				beforeSend : function() {
					$element.find("#fileUploadprogress").show();
					// clear everything
					$element.find("#confirmationMessage").html("");
					setUploadProgress(0);
				},
				uploadProgress : function(event, position, total,
						percentComplete) {
					setUploadProgress(percentComplete);
				},
				success : function() {
					$element.find("#progressBar").width('50%');
					setUploadProgress(100);

				},
				complete : function(response) {
					var result = JSON.parse(response.responseText);
					if(result){
						$element.find("#confirmationMessage").html(
								"<span>" + sgI18nService.translate('file-upload-dialog.confirmationMsg', 'File Uploaded Successfully')
										+ "</span>");
						$scope.$apply(function() {
							$scope.bindModel = result;
						});
					}else{
						$element.find("#confirmationMessage").html(
								"<span>" + sgI18nService.translate('file-upload-dialog.errorMsg', 'Unable to upload file...')
										+ "</span>");
					}
					
				},
				error : function() {
					$element.find("#confirmationMessage")
							.html(
									"<font color='red'>" + sgI18nService.translate('file-upload-dialog.errorMsg', 'Unable to upload file...') + "</font>");
				}
			};
			$element.find("#fileUploadForm").ajaxForm(options);
			
			$element.find("#file").change(function(event) {
				$element.find("#uploadButton").attr('disabled', event.target.value == '');
				setUploadProgress(0);
			});
			
			var multiple = parseAttribute($scope.$parent, $attrs.sdaMultiple);
			if (multiple == 'true') {
				$element.find("#file").attr('multiple', 'multiple');
			}
		};
		
		/*
		 * 
		 */
		function setUploadProgress(percentComplete) {
			var percentage = $element.find("#fileUploadPercent");
			var percentageFill = $element.find("#fileUploadPercentFill");
			percentageFill.width(percentComplete + '%');
			percentage.html(percentComplete + '%');
		}

		self.initialise();
		$scope.fileUploadCtrl = this;
	};
	
	/*
	 * 
	 */
	function parseAttribute(scope, attr) {
		try {
			var evalAttr = $parse(attr)(scope);
			if (!angular.isDefined(evalAttr)) {
				evalAttr = attr;
			}
			return evalAttr;
		} catch (err) {
			return attr;
		}
	}
})();
