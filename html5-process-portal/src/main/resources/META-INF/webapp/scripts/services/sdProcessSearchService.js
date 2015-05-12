/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Aditya.Gaikwad
 */

(function() {
	'use strict';

	angular.module('workflow-ui.services')
			.provider(
					'sdProcessSearchService',
					function() {
						this.$get = [
								'$rootScope',
								'$resource',
								'sdProcessDefinitionService',
								'sdPriorityService',
								'sdStatusService',
								'$q',
								'sgI18nService',
								'sdCriticalityService',
								'sdDataTableHelperService',
								function($rootScope, $resource,
										sdProcessDefinitionService,
										sdPriorityService, sdStatusService, $q,
										sgI18nService, sdCriticalityService,
										sdDataTableHelperService) {
									var service = new ProcessSearchService(
											$rootScope, $resource,
											sdProcessDefinitionService,
											sdPriorityService, sdStatusService,
											$q, sgI18nService,
											sdCriticalityService,
											sdDataTableHelperService);
									return service;
								} ];
					});

	/*
	 * 
	 */
	function ProcessSearchService($rootScope, $resource,
			sdProcessDefinitionService, sdPriorityService, sdStatusService, $q,
			sgI18nService, sdCriticalityService, sdDataTableHelperService) {

		this.hierarchyTypes = [
				{
					"value" : "PROCESS",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-processHierarchy-options-allPIs'),
					"name" : "PROCESS"
				},
				{
					"value" : "PROCESS_AND_CASE",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-processHierarchy-options-allPIsCases'),
					"name" : "PROCESS_AND_CASE"
				},
				{
					"value" : "CASE",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-processHierarchy-options-cases'),
					"name" : "CASE"
				},
				{
					"value" : "ROOT_PROCESS",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-processHierarchy-options-rootProc'),
					"name" : "ROOT_PROCESS"
				} ];

		this.searchFor = [
				{
					"value" : "0",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-searchCriteria-processes'),
					"name" : "Processes"
				},
				{
					"value" : "1",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-searchCriteria-activities'),
					"name" : "Activities"
				} ];

		this.processStates = [
				{
					"value" : "1",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-chooseProcess-options-alive-label'),
					"name" : "Alive"
				},
				{
					"value" : "2",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-chooseProcess-options-completed-label'),
					"name" : "Completed"
				},
				{
					"value" : "3",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-chooseProcess-options-aborted-label'),
					"name" : "Aborted"
				},
				{
					"value" : "4",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-chooseProcess-options-interrupted-label'),
					"name" : "Interrupted"
				},
				{
					"value" : "5",
					"label" : sgI18nService
							.translate('business-control-center-messages.views-processSearchView-chooseProcess-options-all-label'),
					"name" : "All"
				} ];

		this.descBoolOptions = [
				{
					"value" : "0",
					"label" : sgI18nService
							.translate('portal-common-messages.common-true'),
					"name" : "true"
				},
				{
					"value" : "1",
					"label" : sgI18nService
							.translate('portal-common-messages.common-false'),
					"name" : "false"
				} ];

		var REST_BASE_URL = "services/rest/portal/processActivity";

		/*
		 * 
		 */
		ProcessSearchService.prototype.performSearch = function(query) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var options = sdDataTableHelperService
					.convertToQueryParams(query.options);

			if (options.length > 0) {
				restUrl = restUrl + "?" + options.substr(1);
			}
			
			

			var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

			if (queryParams.length > 0) {
				var separator = "?";
				if (/[?]/.test(restUrl)) {
					separator = "&";
				}
				restUrl = restUrl + separator + queryParams.substr(1);
			}
			var postData = sdDataTableHelperService.convertToPostParams(query.options);
			postData['processSearchCriteria'] = query.processSearchCriteria;

			var sdProcessSearchService = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "performSearch";

			return sdProcessSearchService.fetch(urlTemplateParams, postData).$promise;

		};

		/*
		 * 
		 */
		ProcessSearchService.prototype.getAllUniqueProcesses = function() {
			return sdProcessDefinitionService.getAllUniqueProcesses(false);
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getAllBusinessProcesses = function() {
			return sdProcessDefinitionService.getAllBusinessProcesses(false);
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getCommonDescriptors = function(
				procDefIds, onlyFilterable) {
			return sdProcessDefinitionService.getCommonDescriptors(procDefIds,
					onlyFilterable);
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getAllPriorities = function() {
			return sdPriorityService.getAllPriorities();
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getProcessStates = function() {
			var deferred = $q.defer();
			deferred.resolve(this.processStates);
			return deferred.promise;
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getProcessHierarchy = function() {
			var deferred = $q.defer();
			deferred.resolve(this.hierarchyTypes);
			return deferred.promise;
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.searchAttributes = function() {
			var deferred = $q.defer();
			deferred.resolve(this.searchFor);
			return deferred.promise;
		};

		/*
		 * 
		 */
		ProcessSearchService.prototype.getDescBoolOptions = function() {
			var deferred = $q.defer();
			deferred.resolve(this.descBoolOptions);
			return deferred.promise;
		}

		/*
		 * 
		 */
		ProcessSearchService.prototype.getAllCriticalities = function() {
			return sdCriticalityService.getAllCriticalities();
		};

		/*
		 * 
		 */
		ProcessSearchService.prototype.getI18MessageString = function(key) {
			return sgI18nService.translate(key);
		};

		/*
		 * 
		 */
		ProcessSearchService.prototype.getAllActivityStates = function() {
			return sdStatusService.getAllActivityStates();
		}

	}
	;
})();