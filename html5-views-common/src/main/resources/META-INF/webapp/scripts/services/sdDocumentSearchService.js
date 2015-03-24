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
 * @author Abhay.Thappan
 */

(function() {
	'use strict';

	angular.module('viewscommon-ui.services').provider(
			'sdDocumentSearchService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						'sdUtilService',
						function($rootScope, $resource, sdLoggerService, $q,
								$http, sdUtilService) {
							var service = new DocumentSearchService($rootScope,
									$resource, sdLoggerService, $q, $http,
									sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function DocumentSearchService($rootScope, $resource, sdLoggerService, $q,
			$http, sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/documentSearch";
		var trace = sdLoggerService
				.getLogger('viewscommon-ui.services.sdDocumentSearchServic');

		/**
		 * 
		 */
		DocumentSearchService.prototype.searchUsers = function(searchValue) {
			trace.info("Getting authors for:", searchValue);

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type/:searchValue";

			var urlTemplateParams = {};

			urlTemplateParams.type = "searchUsers";
			urlTemplateParams.searchValue = searchValue;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.searchAttributes = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "searchAttributes";

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.performSearch = function(query) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var options = sdUtilService.prepareUrlParams(query.options);

			if (options.length > 0) {
				restUrl = restUrl + "?" + options.substr(1);
			}

			var postData = {
				filters : query.options.filters,
				documentSearchCriteria : query.documentSearchCriteria
			};

			var documentSearch = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "searchByCriteria";

			return documentSearch.fetch(urlTemplateParams, postData).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.fetchProcessDialogData = function(
				documentId) {

			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadProcessByDocument";
			urlTemplateParams.documentId = documentId;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.getUserDetails = function(documentOwner) {
			var restUrl = REST_BASE_URL + "/:type/:documentOwner";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadUserDetails";
			urlTemplateParams.documentOwner = documentOwner;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.getAvailableProcessDefns = function() {
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadAvailableProcessDefinitions";

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.getDocumentVersions = function(
				documentId) {
			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadDocumentVersions";
			urlTemplateParams.documentId = documentId;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.attachDocumentsToProcess = function(
				processOID, documentId) {
			var restUrl = REST_BASE_URL + "/:type/:processOID/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "attachDocumentsToProcess";
			urlTemplateParams.documentId = documentId;
			urlTemplateParams.processOID = processOID;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
	;
})();