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
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('admin-ui.services').provider('sdCriticalityConfigService', function () {
		this.$get = ['$resource', 'sdUtilService', function ($resource, sdUtilService) {
			var service = new CriticalityConfigService($resource, sdUtilService);
			return service;
		}];
	});

	/*
	 *
	 */
	function CriticalityConfigService($resource, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/criticality-config/";

		/*
		 *
		 */
		CriticalityConfigService.prototype.getCriticalityConfig = function() {
			var restUrl = REST_BASE_URL + "fetch";
			var criticalityConfig = $resource(restUrl);
			return criticalityConfig.get().$promise;
		};

		/**
		 * 
		 */
		CriticalityConfigService.prototype.saveCriticalityConfig = function(criticalityConfig) {
			var restUrl = REST_BASE_URL + "save";
			var saveRes = $resource(restUrl, {}, {
				save : {
					method : 'POST'
				}
			});

			return saveRes.save({}, criticalityConfig).$promise;
		};
		
		/**
		 * 
		 */
		CriticalityConfigService.prototype.importCriticalities = function(fileId) {
			var restUrl = REST_BASE_URL + "import";
			var saveRes = $resource(restUrl, {}, {
				import : {
					method : 'POST'
				}
			});

			return saveRes.import({}, fileId).$promise;
		};
		
		/*
		 *
		 */
		CriticalityConfigService.prototype.exportCriticalities = function() {
			var restUrl = REST_BASE_URL + "export";
			var criticalityConfig = $resource(restUrl, {}, {
				get : {
					method : 'GET',
					isArray:false
				}
			});
			return criticalityConfig.get().$promise;
		};
	};
})();
