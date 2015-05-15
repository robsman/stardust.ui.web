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
	angular.module('workflow-ui.services').provider( 'sdStatusService', function() {
		this.$get = [ '$q', '$resource', 'sdUtilService', function ( $q, $resource, sdUtilService) {
			var service = new StatusService($q, $resource, sdUtilService);
			return service;
		}];
	});
	/**
	 *
	 */
	function StatusService( $q, $resource, sdUtilService) {
		StatusService.prototype.getAllActivityStates = function() {
			return  $resource(sdUtilService.getBaseUrl() + 'services/rest/portal/activity-instances/allActivityStates').query().$promise;
		};
		
		StatusService.prototype.getAllProcessStates = function() {
			return  $resource(sdUtilService.getBaseUrl() + 'services/rest/portal/process-instances/allProcessStates').query().$promise;
		};
	};
})();

