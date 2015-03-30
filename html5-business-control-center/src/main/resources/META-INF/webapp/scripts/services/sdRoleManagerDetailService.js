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
			'sdRoleManagerDetailService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						'sdDataTableHelperService',
						function($rootScope, $resource, sdLoggerService, $q,
								$http, sdDataTableHelperService) {
							var service = new RoleManagerDetailService(
									$rootScope, $resource, sdLoggerService, $q,
									$http, sdDataTableHelperService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function RoleManagerDetailService($rootScope, $resource, sdLoggerService,
			$q, $http, sdDataTableHelperService) {
		var REST_BASE_URL = "services/rest/portal/roleManagerDetails";
		var trace = sdLoggerService
				.getLogger('bcc-ui.services.sdRoleManagerDetailService');

		/**
		 * 
		 */
		RoleManagerDetailService.prototype.getRoleManagerDetails = function(
				roleId, departmentOid) {

			// Prepare URL
			var restUrl = REST_BASE_URL + "/:roleId/:departmentOid";

			var urlTemplateParams = {};

			urlTemplateParams.roleId = roleId;
			urlTemplateParams.departmentOid = departmentOid;

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */

		RoleManagerDetailService.prototype.removeUserFromRole = function(
				userIds, roleId, departmentOid) {
			var restUrl = REST_BASE_URL + "/:type/:roleId/:departmentOid";

			var postData = {
				userIds : userIds
			};

			var removeUserFromRole = $resource(restUrl, {
				type : '@type',
				roleId : '@roleId',
				departmentOid : '@departmentOid'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "removeUserFromRole";
			urlTemplateParams.roleId = roleId;
			urlTemplateParams.departmentOid = departmentOid;

			return removeUserFromRole.fetch(urlTemplateParams, postData).$promise;
		};

		/**
		 * 
		 */

		RoleManagerDetailService.prototype.addUserToRole = function(userIds,
				roleId, departmentOid) {
			var restUrl = REST_BASE_URL + "/:type/:roleId/:departmentOid";

			var postData = {
				userIds : userIds
			};

			var addUserToRole = $resource(restUrl, {
				type : '@type',
				roleId : '@roleId',
				departmentOid : '@departmentOid'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "addUserToRole";
			urlTemplateParams.roleId = roleId;
			urlTemplateParams.departmentOid = departmentOid;

			return addUserToRole.fetch(urlTemplateParams, postData).$promise;
		};

		/**
		 * 
		 */
		RoleManagerDetailService.prototype.getAllActivitiesForRole = function(
				query, roleId, departmentOid) {
			var restUrl = REST_BASE_URL + "/allActivities";

			var queryParams = sdDataTableHelperService
					.convertToQueryParams(query.options);

			if (queryParams.length > 0) {
				restUrl = restUrl + "?" + queryParams.substr(1);
			}

			var postData = sdDataTableHelperService
					.convertToPostParams(query.options);

			postData.roleId = roleId;
			postData.departmentOid = departmentOid;

			return ajax(restUrl, '', postData);
		};

		/**
		 * 
		 */
		function ajax(restUrl, extension, value) {
			var deferred = $q.defer();

			var type;
			var data;
			if (angular.isObject(value) || angular.isArray(value)) {
				restUrl += extension;
				type = "POST";
				data = JSON.stringify(value);
			} else {
				restUrl += value + "/" + extension;
				type = "GET";
			}

			var httpResponse;
			if (type == "GET") {
				httpResponse = $http.get(restUrl);
			} else {
				httpResponse = $http.post(restUrl, data);
			}

			httpResponse.success(function(data) {
				deferred.resolve(data);
			}).error(function(data) {
				deferred.reject(data);
			});

			return deferred.promise;
		}
		;

	}
	;
})();
