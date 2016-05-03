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

(function () {
	'use strict';

	angular.module('workflow-ui.services').provider('sdWorklistViewConfigService', function () {
		this.$get = ['sdPreferenceService','$q', function (sdPreferenceService, $q) {
			var service = new ConfigurationService(sdPreferenceService, $q);
			return service;
		}];
	});

	var DEFAULTS = {
		refreshInterval : 0
	}
	var REFRESH_INTERVAL_MULTIPLIER = 60000;
	var moduleId = 'ipp-workflow-perspective';
	var preferenceId = 'preference';
	var scope = 'USER';
	/*
	 * 
	 */
	function ConfigurationService (sdPreferenceService, $q) {

		var configCache = null;

		/**
		 * 
		 */
		this.getCache = function () {
			if (configCache != null) {
				return configCache;
			} else {
				throw "sdWorklistViewConfigService not initialized yet."
			}
		};

		/**
		 * 
		 */
		this.loadConfig = function () {
			var deferred = $q.defer();
			if (configCache != null) {
				deferred.resolve(configCache);
			}
			var config = sdPreferenceService.getStore(scope, moduleId, preferenceId);
			config.init().then(function(){
				configCache = config;
				deferred.resolve(configCache);
			});
			return deferred.promise;
		};
	}
	
	/**
	 * Gets the refresh interval in mins
	 */
	ConfigurationService.prototype.getRefreshIntervalInMins = function(scope) {

		var fromParent = false;
		if (scope && scope == 'PARTITION') {
			fromParent = true;
		}
		
		var refreshInterval = this.getCache()
		.getValue('ipp-workflow-perspective.worklist.prefs.refreshInterval', fromParent);
		if (!refreshInterval) {
			refreshInterval = DEFAULTS.refreshInterval;
		}
		return refreshInterval;
	};

	/**
	 * Gets the refresh interval in milis
	 */
	ConfigurationService.prototype.getRefreshIntervalInMillis = function(scope) {
		return this.getRefreshIntervalInMins(scope) * REFRESH_INTERVAL_MULTIPLIER;
	};
})();
