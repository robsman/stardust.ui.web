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
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('workflow-ui.services').provider('sdProcessDefinitionService', function () {
		this.$get = ['$rootScope', '$resource', 'sdUtilService', function ($rootScope, $resource, sdUtilService) {
			var service = new ProcessDefinitionService($rootScope, $resource, sdUtilService);
			return service;
		}];
	});

	/*
	 *
	 */
	function ProcessDefinitionService($rootScope, $resource, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/process-definitions/";

		/*
		 *
		 */
		ProcessDefinitionService.prototype.getDescriptorColumns = function(onlyFilterable) {
			var restUrl = REST_BASE_URL + "descriptor-columns";
			restUrl += "?onlyFilterable=" + (onlyFilterable === true ? true : false);
			return $resource(restUrl).query().$promise;
		};

		ProcessDefinitionService.prototype.getAllProcesses = function(excludeActivties) {
		var restUrl = REST_BASE_URL + 'all-processes?excludeActivties='+excludeActivties;
			return $resource(restUrl).query().$promise;
		};
		

		/*
		 *
		 */
		ProcessDefinitionService.prototype.getAllUniqueProcesses = function(
				excludeActivties) {
			var restUrl = REST_BASE_URL
					+ 'all-unique-processes?excludeActivties='
					+ excludeActivties;
			return $resource(restUrl).query().$promise;
		};

		/*
		 *
		 */
		ProcessDefinitionService.prototype.getAllBusinessProcesses = function(
				excludeActivties) {
			var restUrl = REST_BASE_URL
					+ 'all-business-processes?excludeActivties='
					+ excludeActivties;
			return $resource(restUrl).query().$promise;
		};

		/*
		 * Get Intersection of descriptors
		 */
		ProcessDefinitionService.prototype.getCommonDescriptors = function(
				procDefids, onlyFilterable) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "descriptors/processes";

			var queryParams = "?onlyFilterable="
					+ (onlyFilterable === true ? true : false);

			if (queryParams.length > 0) {
				restUrl = restUrl + "?" + queryParams.substr(1);
			}

			var postData = {
				procDefIDs : procDefids
			};

			var processDefinition = $resource(restUrl, {}, {
				fetch : {
					method : 'POST',
					isArray : true
				}
			});

			return processDefinition.fetch({}, postData).$promise;
		};
		
		
		/*
		 *Gets Union of descriptors
		 */
		ProcessDefinitionService.prototype.getAllDescriptorsByProcess = function (procDefids, onlyFilterable) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "all-descriptors/processes";

			var postData = {
				procDefIDs : procDefids,
				onlyFilterable : onlyFilterable
			};

			var allDescriptors = $resource(restUrl, {}, {
				fetch : {
					method : 'POST',
					isArray : true
				}
			});

			return allDescriptors.fetch({}, postData).$promise;

		};

	};
})();
