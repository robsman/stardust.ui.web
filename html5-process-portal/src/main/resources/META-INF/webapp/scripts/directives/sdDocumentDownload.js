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
 * @author Abhay.Thappan
 */
(function() {
	'use strict';

	angular.module('bpm-common').directive('sdDocumentDownload', DocumentDownload);
	/*
	 *
	 */
	function DocumentDownload() {

		return {
			restrict : 'EA',
			transclude : true,
			replace : true,
			templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/documentDownload.html',
			controller : [ '$scope', '$parse', '$attrs', 'sdUtilService', DocumentDownloadController ]
		};
	}
	
	/**
	 *
	 */
	function DocumentDownloadController($scope, $parse, $attrs, sdUtilService) {
		this.i18n = $scope.i18n;
		
		/**
		 * 
		 */
		DocumentDownloadController.prototype.downloadDocument = function(res) {
			var REST_BASE_URL = "services/rest/portal/documents";
			var self = this;
			window.location = sdUtilService.getRootUrl() + "/" + REST_BASE_URL
			+ "/downloadDocument" + "/" + self.documentDownload.documentId + "/"
			+ self.documentDownload.documentName;
			delete self.documentDownload;

		};
		
		/**
		 * 
		 */
		DocumentDownloadController.prototype.setShowDocumentDownload = function() {
			var self = this;
			if($attrs.sdaDocumentId != undefined && $attrs.sdaDocumentName != undefined){
		    var documentId = $parse($attrs.sdaDocumentId);
		    var documentName = $parse($attrs.sdaDocumentName);
			self.showDoumentDownload = true;
			var documentDownload = {
				documentId : documentId($scope),
				documentName : documentName($scope)
			};
			self.documentDownload = documentDownload;
			}

		};

		/**
		 * 
		 */
		DocumentDownloadController.prototype.downloadDocumentClose = function() {
			var self = this;
			delete self.documentDownload;
		};
		
		$scope.documentDownloadCtrl = this;
	}
})();
