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

	angular.module('viewscommon-ui').directive('sdDocumentDownload', [ 'sdUtilService', DocumentDownload ]);
	/*
	 * 
	 */
	function DocumentDownload(sdUtilService) {

		return {
			restrict : 'EA',
			scope : { // Creates a new sub scope
				documentName : '=sdaDocumentName',
				documentId : '=sdaDocumentId',
				autoIdPrefix: '@sdaAidPrefix'
			},
			transclude : true,
			replace : true,
			templateUrl : sdUtilService.getBaseUrl() + 'plugins/html5-views-common/scripts/directives/partials/documentDownload.html',
			link : function(scope, element, attrs, ctrl) {
				new DocumentDownloadLink(scope, element, attrs, ctrl);
			}
		};

		/**
		 * 
		 */
		function DocumentDownloadLink(scope, element, attrs, ctrl) {

			var self = this;

			scope.documentDownloadCtrl = self;

			initialize();

			/*
			 * 
			 */
			DocumentDownloadLink.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			function initialize() {
				// Make sure i18n is available in the current scope
				if (!angular.isDefined(scope.i18n)) {
					scope.i18n = scope.$parent.i18n;
				}

				self.setShowDocumentDownload = setShowDocumentDownload;
				self.downloadDocument = downloadDocument;
			}

			/**
			 * 
			 */
			function downloadDocument(res) {
				var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/documents";
				window.location = sdUtilService.getRootUrl() + "/" + REST_BASE_URL + "/downloadDocument" + "/"
						+ scope.documentId + "/" + scope.documentName;
			}
			;

			/**
			 * 
			 */
			function setShowDocumentDownload() {
				if (scope.documentId != undefined && scope.documentName != undefined) {
					self.showDoumentDownload = true;
				}
			}
			;
		}
	}
})();
