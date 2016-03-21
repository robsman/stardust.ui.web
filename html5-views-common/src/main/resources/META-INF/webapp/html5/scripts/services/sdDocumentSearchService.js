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
						'sdDataTableHelperService',
						'sdUtilService',
						function($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService, sdUtilService) {
							var service = new DocumentSearchService($rootScope, $resource, sdLoggerService, $q, $http,
									sdDataTableHelperService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function DocumentSearchService($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/documentSearch";
		var trace = sdLoggerService.getLogger('viewscommon-ui.services.sdDocumentSearchService');

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
			var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/repository/:type";

			var postData = {
				documentDataTableOption : query.options,
				name : query.documentSearchCriteria.documentName,
				id : query.documentSearchCriteria.documentId,
				dateCreatedFrom : query.documentSearchCriteria.createDateFrom,
				dateCreateTo : query.documentSearchCriteria.createDateTo,
				dateLastModifiedFrom : query.documentSearchCriteria.modificationDateFrom,
				dateLastModifiedTo : query.documentSearchCriteria.modificationDateTo,
				owner : query.documentSearchCriteria.author,
				contentTypeIn : query.documentSearchCriteria.selectedFileTypes,
				contentTypeLike : query.documentSearchCriteria.advancedFileType,
				documentTypeIdIn : query.documentSearchCriteria.selectedDocumentTypes,
				repositoryIn : query.documentSearchCriteria.selectedRepository,
				documentDetailLevelDTO : {"userDetailsLevel" : "minimal",
			                              "DocumentDataDetailsLevel" : "minimal"}
			};

			if(query.documentSearchCriteria.searchContent){
				postData.contentLike = query.documentSearchCriteria.containingText;
			}
			
			if(query.documentSearchCriteria.searchContent){
				postData.metaDataLike = query.documentSearchCriteria.containingText;
			}
			
			var documentSearch = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "search";

			return documentSearch.fetch(urlTemplateParams, postData).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.fetchProcessDialogData = function(documentId) {

			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadProcessByDocument";
			urlTemplateParams.documentId = documentId;

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
		DocumentSearchService.prototype.getDocumentVersions = function(documentId) {
			var restUrl = REST_BASE_URL + "/:type/:documentId";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadDocumentVersions";
			urlTemplateParams.documentId = documentId;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		DocumentSearchService.prototype.attachDocumentsToProcess = function(processOID, documentIds) {
			var restUrl = REST_BASE_URL + "/:type/:processOID";

			var postData = {
				documentIds : documentIds,
			};

			var attachDocumentsToProcess = $resource(restUrl, {
				type : '@type',
				processOID : '@processOID'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};

			urlTemplateParams.type = "attachDocumentsToProcess";
			urlTemplateParams.processOID = processOID;

			return attachDocumentsToProcess.fetch(urlTemplateParams, postData).$promise;
		};

	}
	;
})();
