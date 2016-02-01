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

	angular.module('workflow-ui.services').provider(
			'sdStartableProcessService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new StartableProcessService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function StartableProcessService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/startable-process";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdStartableProcessService');

		/**
		 * 
		 */
		StartableProcessService.prototype.getStartableProcesses = function() {
			return $resource(REST_BASE_URL).get().$promise;
		};

		/**
		 * 
		 */
		StartableProcessService.prototype.startProcessOnSelectDepartment = function(departmentOid, processId) {
			var restUrl = REST_BASE_URL + "/:type/:departmentOid/:processId";
			
			var templateParams = {};
			templateParams.type = "startProcessByDepartment";
			templateParams.departmentOid = departmentOid;
			templateParams.processId = processId;
			
			return $resource(restUrl).get(templateParams).$promise;
		};
		
		
		/**
		 * 
		 */
		StartableProcessService.prototype.startProcess = function(processId) {
			var restUrl = REST_BASE_URL + "/:type/:processId";
			
			var templateParams = {};
			templateParams.type = "startProcess";
			templateParams.processId = processId;
			
			return $resource(restUrl).get(templateParams).$promise;
		};

	}
	;

})();
