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

	angular.module('bcc-ui.services').provider('sdDeputyManagementService', function() {
		this.$get = [ '$resource', 'sdLoggerService', function($resource, sdLoggerService) {
			var service = new DeputyManagementService($resource, sdLoggerService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function DeputyManagementService($resource, sdLoggerService) {
		var REST_BASE_URL = "services/rest/portal/deputyManagement";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdDeputyManagementService');

		/**
		 * 
		 */
		DeputyManagementService.prototype.loadUsers = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:type';

			var urlTemplateParams = {};
			urlTemplateParams.type = 'users';
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DeputyManagementService.prototype.loadDeputiesForUser = function(userOID) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:type/:userOID';

			var urlTemplateParams = {};
			urlTemplateParams.type = 'deputiesForUser';
			urlTemplateParams.userOID = userOID;
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DeputyManagementService.prototype.getDeputyUsersData = function(userOID, searchValue, searchMode) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:type/:userOID/:searchValue/:searchMode';

			var urlTemplateParams = {};
			urlTemplateParams.type = 'deputiesForUser';
			urlTemplateParams.userOID = userOID;
			urlTemplateParams.searchValue = searchValue;
			urlTemplateParams.searchMode = searchMode;
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};

		/**
		 * 
		 */
		DeputyManagementService.prototype.getAuthorizations = function(userOID) {
			// Prepare URL
			var restUrl = REST_BASE_URL + '/:type/:userOID/';

			var urlTemplateParams = {};
			urlTemplateParams.type = 'authorizations';
			urlTemplateParams.userOID = userOID;
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		/**
		 * 
		 */
		DeputyManagementService.prototype.addDeputy = function(userOID, deputyOID, validFrom, validTo,
				modelParticipantIds, mode) {
			var restUrl = REST_BASE_URL + "/:type";

			var postData = {
				userOID : userOID,
				deputyOID : deputyOID,
				validFrom : validFrom,
				validTo : validTo,
				modelParticipantIds : modelParticipantIds,
				mode : mode
			};

			var addDeputy = $resource(restUrl, {
				type : '@type',
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "addDeputy";

			return addDeputy.fetch(urlTemplateParams, postData).$promise;
		};
		
		/**
		 * 
		 */
		DeputyManagementService.prototype.removeUserDeputy = function(userOID, deputyOID) {
			var restUrl = REST_BASE_URL + "/:type";

			var postData = {
				userOID : userOID,
				deputyOID : deputyOID
			};

			var removeUserDeputy = $resource(restUrl, {
				type : '@type',
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "removeUserDeputy";

			return removeUserDeputy.fetch(urlTemplateParams, postData).$promise;
		};		
	}
	;
})();
