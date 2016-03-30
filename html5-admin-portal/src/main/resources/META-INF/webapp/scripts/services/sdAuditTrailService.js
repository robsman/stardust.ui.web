/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 * @author Yogesh.Manware
 */

(function() {
	'use strict';

	angular.module('admin-ui.services').provider(
			'sdAuditTrailService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new AuditTrailService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function AuditTrailService($resource, sdLoggerService, sdUtilService) {

		var trace = sdLoggerService.getLogger('admin-ui.services.sdAuditTrailService');
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/audit-trail";

		/**
		 * 
		 */
		AuditTrailService.prototype.recoverWorkflowEngine = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var recoverWorkflowEngine = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'GET'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "workflowEngineRecovery";

			return recoverWorkflowEngine.fetch(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		AuditTrailService.prototype.cleanupAuditTrailDatabase = function(retainUsersAndDepts) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			if(retainUsersAndDepts === true){
				restUrl = restUrl + "?retainUsersAndDepts=" + retainUsersAndDepts;
			}
			var cleanupAuditTrailDatabase = $resource(restUrl, {
				type : '@type'
			}, {
				clean : {
					method : 'DELETE'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "database";

			return cleanupAuditTrailDatabase.clean(urlTemplateParams).$promise;

		};

		/**
		 * 
		 */
		AuditTrailService.prototype.cleanupAuditTrailDatabaseWithModel = function() {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var cleanupAuditTrailDatabase = $resource(restUrl, {
				type : '@type'
			}, {
				clean : {
					method : 'DELETE'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "databaseWithModel";

			return cleanupAuditTrailDatabase.clean(urlTemplateParams).$promise;

		};
	}
})();
