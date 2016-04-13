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
 * @author Johnson.Quadras
 */
(function() {
	'use strict';

	/**
	 * 
	 */
	angular.module('workflow-ui.services').provider('sdLoggedInUserService', function() {
		this.$get = [ 'sdUtilService', '$resource' ,'$q', function( sdUtilService, $resource, $q) {
			var service = new LoggedInUserService( sdUtilService, $resource, $q);
			return service;
		} ];
	});

	/**
	 * 
	 */
	function LoggedInUserService(sdUtilService, $resource, $q) {
		var userCache = null;
		var permissionCache = null;

		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/user";
		/**
		 * 
		 */
		this.getUserInfo = function() {
			if (!userCache) {
				throw 'User Info not loaded';
			}
			return userCache;
		};

		/**
		 * 
		 */
		this.getRuntimePermissions = function() {
			if (!permissionCache) {
				throw 'Run time permission not loaded';
			}
			return permissionCache;
		};

		/**
		 * 
		 */
		this.loadUserInfo = function() {
			var deferred = $q.defer();

			var restUrl = REST_BASE_URL + "/whoAmI";

			$resource(restUrl).get().$promise.then(function(result){
				userCache = result;
				deferred.resolve( userCache );
			});

			return deferred.promise;
		};

		/**
		 * 
		 */
		this.loadRuntimePermissions = function() {
			var deferred = $q.defer();

			var restUrl = REST_BASE_URL + "/whoAmI/runtime-permissions";

			$resource(restUrl).get().$promise.then(function(result){
				permissionCache = result;
				deferred.resolve( permissionCache );
			});

			return deferred.promise;
		};

	}
})();
