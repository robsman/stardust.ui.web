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
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('workflow-ui.services').provider('sdProcessDefinitionService', function () {
		this.$get = ['$rootScope', '$resource', function ($rootScope, $resource) {
			var service = new ProcessDefinitionService($rootScope, $resource);
			return service;
		}];
	});

	/*
	 *
	 */
	function ProcessDefinitionService($rootScope, $resource) {
		var REST_BASE_URL = "services/rest/portal/process-definitions/";

		/*
		 *
		 */
		ProcessDefinitionService.prototype.getDescriptorColumns = function(onlyFilterable) {
			var restUrl = REST_BASE_URL + "descriptor-columns";
			restUrl += "?onlyFilterable=" + (onlyFilterable === true ? true : false);
			return $resource(restUrl).query().$promise;
		};

		ProcessDefinitionService.prototype.getAllProcesses = function(excludeActivties) {
		var restUrl = REST_BASE_URL + 'all-processes?excludeActivties='+excludeActivties;
			return $resource(restUrl).query().$promise;
		};

	};
})();
