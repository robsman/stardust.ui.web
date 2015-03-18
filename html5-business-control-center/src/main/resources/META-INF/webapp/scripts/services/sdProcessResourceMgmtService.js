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

	angular.module('bcc-ui.services').provider(
			'sdProcessResourceMgmtService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						'sdUtilService',
						function($rootScope, $resource, sdLoggerService, $q,
								$http, sdUtilService) {
							var service = new ProcessResourceMgmtService(
									$rootScope, $resource, sdLoggerService, $q,
									$http, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function ProcessResourceMgmtService($rootScope, $resource, sdLoggerService,
			$q, $http, sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/processResourceManagement";
		var trace = sdLoggerService
				.getLogger('bcc-ui.services.sdProcessResourceMgmtService');

		/**
		 * 
		 */
		ProcessResourceMgmtService.prototype.getProcessResourceRolesAndUsers = function() {
			trace.info("Getting available Roles and Users");

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "availableRolesAndUsers";

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
	;
})();
