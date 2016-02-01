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
			'sdWorkflowOverviewService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new WorkflowOverviewService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/**
	 * 
	 */
	function WorkflowOverviewService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/workflow-overview";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdWorkflowOverviewService');

		/**
		 * 
		 */
		WorkflowOverviewService.prototype.getWorkflowOverviewCounts = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};
			urlTemplateParams.type = "counts"

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
	;
})();
