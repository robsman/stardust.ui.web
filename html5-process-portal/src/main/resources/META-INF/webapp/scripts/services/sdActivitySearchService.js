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
			'sdActivitySearchService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new ActivitySearchService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/**
	 * 
	 */
	function ActivitySearchService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/activity-search";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdActivitySearchService');

		/**
		 * 
		 */
		ActivitySearchService.prototype.getAllResubmissionActivityInstances = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/allResubmissionActivityInstances";

			var urlTemplateParams = {};

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		/**
		 * 
		 */
		ActivitySearchService.prototype.getAllActivityInstances = function(){
			// Prepare URL
			var restUrl = REST_BASE_URL + "/allActivityInstances";

			var urlTemplateParams = {};
			
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		/**
		 * 
		 */
		ActivitySearchService.prototype.getWorklistForUser = function(userOID){
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type/:userOID";

			var urlTemplateParams = {};
			urlTemplateParams.type = "worklistForUser";
			urlTemplateParams.userOID = userOID;
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
        /**
         * 
         */
		ActivitySearchService.prototype.getUsersByCriteria = function(firstName, lastName){
			// Prepare URL
			var queryParams = "";

			if (firstName != undefined) {
				queryParams += "&firstName=" + firstName;
			}

			if (lastName != undefined) {
				queryParams += "&lastName=" + lastName;
			}			
			
			var restUrl = REST_BASE_URL + "/:type";

			restUrl = restUrl + "?" + queryParams;
			
			var urlTemplateParams = {};
			urlTemplateParams.type = "users";
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
	;
})();
