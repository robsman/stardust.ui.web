/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Onkar.Borikar
 * 
 */

(function() {
	'use strict';

	angular.module('bcc-ui.services').provider('sdProcessDescriptorService', function() {
		this.$get = [ '$resource', 'sdLoggerService', 'sdUtilService', function($resource, sdLoggerService, sdUtilService) {
			var service = new ProcessDescriptorService($resource, sdLoggerService, sdUtilService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function ProcessDescriptorService($resource, sdLoggerService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() +"services/rest/portal/process-instances/";
		var trace = sdLoggerService.getLogger('bcc-ui.services.sdProcessDescriptorService');
		
		ProcessDescriptorService.prototype.getProcessDescriptors = function(oid){
			var restUrl = REST_BASE_URL+oid+"/process-descriptors";
			return $resource(restUrl).query().$promise;
		};
		
		ProcessDescriptorService.prototype.updateProcessDescriptors = function(oid,descriptorPathId,changedValue,type){
			var restUrl = REST_BASE_URL+oid+"/process-descriptor";
			var request = $resource(restUrl, null,
					{
						'update': { 
							method:'PUT',
							transformRequest: function(data, headers){
				                headers = angular.extend({}, headers, {'Content-Type': 'application/json'});
				                return angular.toJson({'id' : descriptorPathId, 'changedValue' : changedValue, 'type' : type}); 
				            }      
						}
					});
			return request.update().$promise;	
		};
	}
		
})();