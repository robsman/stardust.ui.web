/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module('bcc-ui.services')
			.provider(
					'sdTafficLightService',
					function() {
						this.$get = [
								'$resource',
								'sdLoggerService',
								'$q',
								'sdUtilService',
								function($resource, sdLoggerService, $q,
										sdUtilService) {
									var service = new TrafficLightService(
											$resource,
											sdLoggerService, $q,
											sdUtilService);
									return service;
								}];
					});

	/*
	 * 
	 */
	function TrafficLightService( $resource, sdLoggerService,
			$q,sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl()
				+ "services/rest/portal/trafficLight";
		var trace = sdLoggerService
				.getLogger('bcc-ui.services.sdTafficLightService');

		/*
		 * 
		 */
		TrafficLightService.prototype.getProcesses = function() {
			var restUrl = REST_BASE_URL + "/processes";
			var processes = $resource(restUrl);
			return processes.query().$promise;
		};
		
		/*
		 * 
		 */
		TrafficLightService.prototype.getCategories = function(processQId) {
			var restUrl = REST_BASE_URL + "/categories/"+processQId;
			var processes = $resource(restUrl);
			return processes.query().$promise;
		};
		
		/*
		 * 
		 */
		TrafficLightService.prototype.getTrafficLightViewColumns = function(processQId) {
			var restUrl = REST_BASE_URL + "/activityColumns/"+processQId;
			var processes = $resource(restUrl);
			return processes.query().$promise;
		};
		
		

	};
})();
