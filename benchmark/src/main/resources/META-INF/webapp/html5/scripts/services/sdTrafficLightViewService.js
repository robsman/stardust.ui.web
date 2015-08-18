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

	angular.module('benchmark-app.services').provider(
			'sdTrafficLightViewService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						'sdDataTableHelperService',
						'sdUtilService',
						function($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService,
								sdUtilService) {
							var service = new TrafficLightViewService($rootScope, $resource, sdLoggerService, $q,
									$http, sdDataTableHelperService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function TrafficLightViewService($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService,
			sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/trafficLightView";
		var trace = sdLoggerService.getLogger('benchmark-app.services.sdTrafficLightViewService');

		/**
		 * 
		 */
		TrafficLightViewService.prototype.getTLVStatastic = function(query) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var postData = query;

			var tlvStats = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "stats";

			return tlvStats.fetch(urlTemplateParams, postData).$promise;

		};
		
		
		/**
		 * 
		 */
		TrafficLightViewService.prototype.getTLVActivityStatastic = function(query) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var postData = query;

			var tlvStats = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "activityStats";

			return tlvStats.fetch(urlTemplateParams, postData).$promise;

		};

	}
	;
})();
