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

	angular.module('bcc-ui.services').provider('sdRoleAssignmentService', function() {
		this.$get = [ '$rootScope', '$resource', 'sdLoggerService', 'sdUtilService', function($rootScope, $resource, sdLoggerService, sdUtilService) {
			var service = new RoleAssignmentService($rootScope, $resource, sdLoggerService, sdUtilService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function RoleAssignmentService($rootScope, $resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() +"services/rest/portal/roleAssignment";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdRoleAssignmentService');

		/**
		 * 
		 */
		RoleAssignmentService.prototype.getRoleAssignments = function() {
			trace.info("Getting available Roles");

			// Prepare URL
			var restUrl = REST_BASE_URL;

			var urlTemplateParams = {};

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

	}
})();
