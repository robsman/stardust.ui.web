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

	angular.module('bpm-common.services').provider(
			'sdFavoriteViewService',
			function() {
				this.$get = [
						'$rootScope',
						'$resource',
						'sdLoggerService',
						'$q',
						'$http',
						'sdUtilService',
						function($rootScope, $resource, sdLoggerService, $q, $http, sdUtilService) {
							var service = new FavoriteViewService($rootScope, $resource, sdLoggerService, $q,
									$http, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function FavoriteViewService($rootScope, $resource, sdLoggerService, $q, $http, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/favorites";
		var trace = sdLoggerService.getLogger('bpm-common.services.sdFavoriteViewService');
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.addFavorite = function(preferenceId,preferenceName,value) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:preferenceId/:preferenceName";
			
			var postData = value;

			var tlvStats = $resource(restUrl, {
				preferenceId : '@preferenceId',
				preferenceName : '@preferenceName'
			}, {
				addFavorite : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.preferenceId = preferenceId;
			urlTemplateParams.preferenceName = preferenceName;
			return tlvStats.addFavorite(urlTemplateParams, postData).$promise;

		};
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.updateFavorite = function(preferenceId,preferenceName,value) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:preferenceId/:preferenceName";
			
			var postData = value;

			var tlvStats = $resource(restUrl, {
				preferenceId : '@preferenceId',
				preferenceName : '@preferenceName'
			}, {
				updateFavorite : {
					method : 'PUT'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.preferenceId = preferenceId;
			urlTemplateParams.preferenceName = preferenceName;
			return tlvStats.updateFavorite(urlTemplateParams, postData).$promise;

		};
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.getAllFavorite = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL;

			var tlvStats = $resource(restUrl, {
			}, {
				getAllFavorite : {
					method : 'GET',
					isArray:true
				}
			});
			return tlvStats.getAllFavorite().$promise;

		};
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.getFavoriteByType = function(preferenceId) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:preferenceId';

			var tlvStats = $resource(restUrl, {
				preferenceId : '@preferenceId'
			}, {
				getFavoriteByType : {
					method : 'GET'
				}
			});
			
			var urlTemplateParams = {};
			urlTemplateParams.preferenceId = preferenceId;
			return tlvStats.getFavoriteByType(urlTemplateParams).$promise;

		};
		
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.getFavoriteByName = function(preferenceId, preferenceName) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:preferenceId/:preferenceName';

			var tlvStats = $resource(restUrl, {
				preferenceId : '@preferenceId',
				preferenceName : '@preferenceName'
			}, {
				getFavoriteByType : {
					method : 'GET'
				}
			});
			
			var urlTemplateParams = {};
			urlTemplateParams.preferenceId = preferenceId;
			urlTemplateParams.preferenceName = preferenceName;
			return tlvStats.getFavoriteByType(urlTemplateParams).$promise;

		};
		
		
		/**
		 * 
		 */
		FavoriteViewService.prototype.deleteFavorite = function(preferenceId, preferenceName) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:preferenceId/:preferenceName';

			var tlvStats = $resource(restUrl, {
				preferenceId : '@preferenceId',
				preferenceName : '@preferenceName'
			}, {
				deleteFavorite : {
					method : 'DELETE'
				}
			});
			
			var urlTemplateParams = {};
			urlTemplateParams.preferenceId = preferenceId;
			urlTemplateParams.preferenceName = preferenceName;
			return tlvStats.deleteFavorite(urlTemplateParams).$promise;

		};
		
	}
	;
})();
