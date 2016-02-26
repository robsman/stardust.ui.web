/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * This service holds user supplied / overridden configurations, aka environment configurations
 * 
 * @author Subodh.Godbole
 */

(function() {
	'use strict';
	
	angular.module('bpm-common.services').provider('sdEnvConfigService', function() {
		this.$get = [ '$injector', 'sdLoggerService', function($injector, sdLoggerService) {
			var service = new EnvConfigService($injector, sdLoggerService);
			return service;
		} ];
	});

	/*
	 * 
	 */
	function EnvConfigService($injector, sdLoggerService) {

		var trace = sdLoggerService.getLogger('bpm-common.services.sdEnvConfigService');

		var isConfigured, envConfigs, eventInterceptor;

		initialize();

		/*
		 * 
		 */
		function initialize() {
			isConfigured = $injector.has('sdEnvConfig');
			envConfigs = $injector.has('sdEnvConfig') ? $injector.get('sdEnvConfig') : {};
			trace.info('EnvConfigs:', envConfigs);
		}

		/*
		 * 
		 */
		EnvConfigService.prototype.isConfigured = function() {
			return isConfigured;
		};

		/*
		 * 
		 */
		EnvConfigService.prototype.getBaseUrl = function() {
			return envConfigs.baseUrl;
		};

		/*
		 * 
		 */
		EnvConfigService.prototype.getEventInterceptor = function() {
			if (!eventInterceptor && envConfigs.eventInterceptor) {
				eventInterceptor = angular.isString(envConfigs.eventInterceptor) ? 
						$injector.get(envConfigs.eventInterceptor) : $injector.invoke(envConfigs.eventInterceptor);
			}

			return eventInterceptor;
		};

		/*
		 * 
		 */
		EnvConfigService.prototype.getNavPath = function(id) {
			if (envConfigs.navPaths) {
				return envConfigs.navPaths[id];
			}
		};
	};
})();
