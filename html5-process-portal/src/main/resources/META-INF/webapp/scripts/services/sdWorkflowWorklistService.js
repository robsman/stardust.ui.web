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
			'sdWorkflowWorklistService',
			function() {
				this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService',
						function($resource, sdLoggerService, sdUtilService) {
							var service = new WorkflowWorklistService($resource, sdLoggerService, sdUtilService);
							return service;
						} ];
			});

	/**
	 * 
	 */
	function WorkflowWorklistService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/worklist";

		var trace = sdLoggerService.getLogger('workflow-ui.services.sdWorkflowWorklistService');

		/**
		 * 
		 */
		WorkflowWorklistService.prototype.getUserAssignments = function(showEmptyWorklist, reload) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:showEmptyWorklist/:reload";

			var urlTemplateParams = {};
			urlTemplateParams.showEmptyWorklist = showEmptyWorklist;
			urlTemplateParams.reload = reload;
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		
		WorkflowWorklistService.prototype.getNextAssemblyLineActivity = function(){
			// Prepare URL
			var restUrl = REST_BASE_URL + "/nextAssemblyLineActivity";

			var urlTemplateParams = {};
			
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
		
		WorkflowWorklistService.prototype.getUserProcesses = function(){
			// Prepare URL
			var restUrl = REST_BASE_URL + "/userProcesses";

			var urlTemplateParams = {};
			
			return $resource(restUrl).get(urlTemplateParams).$promise;
		};


	}
	;
})();
