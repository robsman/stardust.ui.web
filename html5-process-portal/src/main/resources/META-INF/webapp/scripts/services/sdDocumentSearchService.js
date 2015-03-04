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

	angular.module('workflow-ui.services').provider(
			'sdDocumentSearchService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						function($rootScope, $resource, sdLoggerService, $q,
								$http) {
							var service = new DocumentSearchService($rootScope,
									$resource, sdLoggerService, $q, $http);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function DocumentSearchService($rootScope, $resource, sdLoggerService, $q,
			$http) {
		var REST_BASE_URL = "services/rest/portal/documentSearch";
		var trace = sdLoggerService.getLogger('bpm-common.sdDataTable');
		DocumentSearchService.prototype.searchUsers = function(searchValue) {
			trace.info("Getting authors for:", searchValue);

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type/:searchValue";

			var urlTemplateParams = {};

			urlTemplateParams.type = "searchUsers";
			urlTemplateParams.searchValue = searchValue;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		DocumentSearchService.prototype.searchAttributes = function() {
			trace.info("inside searchAttributes function");
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "searchAttributes";

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		DocumentSearchService.prototype.performSearch = function(query) {
			trace.info("inside performSearch function");
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var options = "";
			if (query.options.skip != undefined) {
				options += "&skip=" + query.options.skip;
			}
			if (query.options.pageSize != undefined) {
				options += "&pageSize=" + query.options.pageSize;
			}
			if (query.options.order != undefined) {
				// Supports only single column sort
				var index = query.options.order.length - 1;
				options += "&orderBy=" + query.options.order[index].name;
				options += "&orderByDir=" + query.options.order[index].dir;
			}

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

		DocumentSearchService.prototype.fetchProcessDialogData = function(
				documentId) {

			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadProcessByDocument";
			urlTemplateParams.documentId = documentId;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		DocumentSearchService.prototype.getUserDetails = function(documentOwner) {
			var restUrl = REST_BASE_URL + "/:type/:documentOwner";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadUserDetails";
			urlTemplateParams.documentOwner = documentOwner;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		this.getAvailableProcessDefns = function() {
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadAvailableProcessDefns";

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		this.getDocumentVersions = function(documentId) {
			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadDocumentVersions";
			urlTemplateParams.documentId = documentId;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		}

		this.attachDocumentsToProcess = function(processOID, documentId) {
			var restUrl = REST_BASE_URL + "/:type/:processOID/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "attachDocumentsToProcess";
			urlTemplateParams.documentId = documentId;
			urlTemplateParams.processOID = processOID;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		}

		this.getRootUrl = function() {
			return window.location.href.substring(0, location.href
					.indexOf("/main.html"));
		};
	};
})();
