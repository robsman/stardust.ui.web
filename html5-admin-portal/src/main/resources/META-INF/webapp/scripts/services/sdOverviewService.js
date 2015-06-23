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

	angular.module('admin-ui.services').provider(
			'sdOverviewService',
			function() {
				this.$get = [
						'$resource',
						'sdLoggerService',
						'sdDataTableHelperService',
						'sdUtilService',
						function($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
							var service = new OverviewService($resource, sdLoggerService, sdDataTableHelperService,
									sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function OverviewService($resource, sdLoggerService, sdDataTableHelperService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/overview";
		var trace = sdLoggerService.getLogger('admin-ui.services.sdOverviewService');

		/**
		 * 
		 */
		OverviewService.prototype.getAllLogEntries = function(params) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var options = sdDataTableHelperService.convertToQueryParams(params);

			if (options.length > 0) {
				restUrl = restUrl + "?" + options.substr(1);
			}

			var urlTemplateParams = {};
			urlTemplateParams.type = "allLogEntries";

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};
	}
})();
