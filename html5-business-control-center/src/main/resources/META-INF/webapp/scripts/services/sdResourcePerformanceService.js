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

	angular.module('bcc-ui.services').provider('sdResourcePerformanceService', function() {
		this.$get = [ 'sdLoggerService', 'sdUtilService', function(sdLoggerService, sdUtilService) {
			var service = new ResourcePerformanceService(sdLoggerService, sdUtilService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function ResourcePerformanceService(sdLoggerService, sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/resourcePerformance";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdResourcePerformanceService');

		/**
		 * 
		 */
		ResourcePerformanceService.prototype.getResourcePerformanceData = function(roleId) {
			var restUrl = REST_BASE_URL
			var params = "/loadStatastics/" + roleId;

			return sdUtilService.ajax(restUrl, '', params);

		};

	}
	;
})();
