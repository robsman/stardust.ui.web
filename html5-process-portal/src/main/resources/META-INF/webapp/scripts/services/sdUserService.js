/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Nikhil.Gahlot
 */
(function() {
	'use strict';

	angular.module('workflow-ui.services').provider(
			'sdUserService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new UserService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function UserService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/user";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdUserService');

		/**
		 * 
		 */
		UserService.prototype.searchUsers = function(searchValue, active, max) {
			trace.info("Getting authors for:", searchValue);

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type/:searchValue?";

			if (active != undefined) {
				restUrl += 'active=:active&';
			}

			if (max != undefined) {
				restUrl += 'max=:max';
			}

			var urlTemplateParams = {};
			urlTemplateParams.type = "search"
			urlTemplateParams.searchValue = searchValue;
			urlTemplateParams.active = active;
			urlTemplateParams.max = max;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		UserService.prototype.getAllCounts = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var urlTemplateParams = {};
			urlTemplateParams.type = "allCounts"

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		
		/**
		 * 
		 */
		UserService.prototype.getUserDetails = function(userOID) {
			var restUrl = REST_BASE_URL + "/:type/:userOID";

			var urlTemplateParams = {};

			urlTemplateParams.type = "loadUserDetails";
			urlTemplateParams.userOID = userOID;

			return $resource(restUrl).get(urlTemplateParams).$promise;

		};

	}
	;
})();
