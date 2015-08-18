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

	angular.module('bcc-ui.services').provider(
			'sdUserManagerDetailService',
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
							var service = new UserManagerDetailService($rootScope, $resource, sdLoggerService, $q,
									$http, sdDataTableHelperService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function UserManagerDetailService($rootScope, $resource, sdLoggerService, $q, $http, sdDataTableHelperService,
			sdUtilService) {
		var REST_BASE_URL = "services/rest/portal/userManagerDetails";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdUserManagerDetailService');

		/**
		 * 
		 */
		UserManagerDetailService.prototype.getUserManagerDetails = function(userOid) {

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:userOid";

			var urlTemplateParams = {};

			urlTemplateParams.userOid = userOid;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		UserManagerDetailService.prototype.removeRoleFromUser = function(roleIds, userOid) {
			var restUrl = REST_BASE_URL + "/:type/:userOid";

			var postData = {
				roleIds : roleIds
			};

			var removeRoleFromUser = $resource(restUrl, {
				type : '@type',
				userOid : '@userOid'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "removeRoleFromUser";
			urlTemplateParams.userOid = userOid;
			return removeRoleFromUser.fetch(urlTemplateParams, postData).$promise;
		};

		/**
		 * 
		 */

		UserManagerDetailService.prototype.addRoleToUser = function(roleIds, userOid) {
			var restUrl = REST_BASE_URL + "/:type/:userOid";
			var postData = {
				roleIds : roleIds
			};

			var addRoleToUser = $resource(restUrl, {
				type : '@type',
				userOid : '@userOid'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "addRoleToUser";
			urlTemplateParams.userOid = userOid;

			return addRoleToUser.fetch(urlTemplateParams, postData).$promise;
		};

		/**
		 * 
		 */

		UserManagerDetailService.prototype.getAllActivitiesForUser = function(query, userOid) {
			var restUrl = REST_BASE_URL + "/allActivities";

			var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

			if (queryParams.length > 0) {
				restUrl = restUrl + "?" + queryParams.substr(1);
			}

			var postData = sdDataTableHelperService.convertToPostParams(query.options);

			postData.userOid = userOid;

			return sdUtilService.ajax(restUrl, '', postData);
		};

	}
	;
})();
