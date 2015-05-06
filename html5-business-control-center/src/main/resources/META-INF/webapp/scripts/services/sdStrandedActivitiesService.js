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
 * @author Abhay.Thappan
 * 
 */

(function() {
	'use strict';

	angular.module('bcc-ui.services').provider('sdStrandedActivitiesService', function() {
		this.$get = [ '$resource', 'sdLoggerService', 'sdDataTableHelperService','sdUtilService', function($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
			var service = new StrandedActivitiesService($resource, sdLoggerService, sdDataTableHelperService, sdUtilService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function StrandedActivitiesService($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/strandedActivities";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdStrandedActivitiesService');

		/**
		 * 
		 */
		StrandedActivitiesService.prototype.getStrandedActivities = function(query) {
			var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);
			var restUrl = REST_BASE_URL
			if (queryParams.length > 0) {
				restUrl = restUrl + "?" + queryParams.substr(1);
			}

			var postData = sdDataTableHelperService.convertToPostParams(query.options);

			return sdUtilService.ajax(restUrl, '', postData);

		};

	}
	;
})();
