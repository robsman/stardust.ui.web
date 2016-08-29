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
 * @author Johnson.Quadras
 */

(function() {
	'use strict';

	angular.module('bcc-ui.services').provider('sdGanttChartService',function() {
				this.$get = ['$resource', 'sdLoggerService', 'sdUtilService',
				             function($resource, sdLoggerService, sdUtilService) {
					var service = new GanttChartService(  $resource, sdLoggerService, sdUtilService);
					return service;
				}];
			});
	
	
	var REST_BASE_URL = null;
	var trace = null;
	var _$resource = null;
	/*
	 * 
	 */
	function GanttChartService( $resource, sdLoggerService, sdUtilService) {
		REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/gantt-chart";
		trace = sdLoggerService.getLogger('bcc-ui.services.sdGanttChartService');
		_$resource = $resource;
	};

	/*
	 * 
	 */
	GanttChartService.prototype.getByProcess = function(processOid, findAllChildren, fetchRootProcess ) {
		var restUrl = REST_BASE_URL + "/process/"+processOid;
		
		var qParams = {
				findAllChildren : findAllChildren,
				fetchRootProcess : fetchRootProcess
		}

		var processes = _$resource(restUrl);
		return processes.get(qParams).$promise;
	};

})();
