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

	angular.module('bcc-ui.services').provider('sdProcessResourceMgmtService', function() {
		this.$get = [ '$rootScope', '$resource', 'sdLoggerService', function($rootScope, $resource, sdLoggerService) {
			var service = new ProcessResourceMgmtService($rootScope, $resource, sdLoggerService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function ProcessResourceMgmtService($rootScope, $resource, sdLoggerService) {
		var REST_BASE_URL = "services/rest/portal/processResourceManagement";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdProcessResourceMgmtService');

		/**
		 * 
		 */
		ProcessResourceMgmtService.prototype.getProcessResourceRoles = function() {
			trace.info("Getting available Roles");

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "availableRoles";

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		ProcessResourceMgmtService.prototype.getProcessResourceUsers = function() {
			trace.info("Getting available Users");

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};

			urlTemplateParams.type = "availableUsers";

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
	;
})();
