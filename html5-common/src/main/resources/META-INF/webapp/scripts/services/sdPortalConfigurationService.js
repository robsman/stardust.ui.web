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

	angular.module('bpm-common.services').provider('sdPortalConfigurationService', function () {
		this.$get = ['sdPreferenceService','$q', function (sdPreferenceService, $q) {
			var service = new ConfigurationService(sdPreferenceService, $q);
			return service;
		}];
	});

	var DEFAULTS = {
			pageSize : 8,
			maxPages : 4,
			fastStep : 3
	}

	var moduleId = 'ipp-portal-common';
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
		
		
		/**
		 * 
		 */
		this.getConfig = function () {
			var deferred = $q.defer();
			if (configCache != null) {
				deferred.resolve(configCache);
			} else {
				this.loadConfig().then(function () {
					deferred.resolve(configCache);
				});
			}
			return deferred.promise;
		};

		/**
		 * 
		 */
		this.getCache = function () {
			if (configCache != null) {
				return configCache;
			} else {
				throw "sdPortalConfigurationService not initialized yet."
			}
		}
	}

	/**
	 * Gets the page size
	 */
	ConfigurationService.prototype.getPageSize = function (scope) {
		var fromParent = false;
		if (scope && scope == 'PARTITION') {
			fromParent = true;
		}
		var config = this.getCache();
		var pageSize = config.getValue('ipp-portal-common.configuration.prefs.pageSize', fromParent);
		if (!pageSize) {
			pageSize = DEFAULTS.pageSize;
		}
		return pageSize;
	};
	/**
	 * Gets the Max Pages
	 */
	ConfigurationService.prototype.getMaxPages = function (scope) {
		var fromParent = false;
		if (scope && scope == 'PARTITION') {
			fromParent = true;
		}
		var config = this.getCache();
		var maxPages = config.getValue('ipp-portal-common.configuration.prefs.paginatorMaxPages', fromParent);
		if (!pageSize) {
			maxPages = DEFAULTS.maxPages;
		}
		return maxPages;
	};

	/**
	 * Gets the fast step size
	 */
	ConfigurationService.prototype.getFastStepSize = function (scope) {
		var fromParent = false;
		if (scope && scope == 'PARTITION') {
			fromParent = true;
		}
		var config = this.getCache();
		var fastStep = config.getValue('ipp-portal-common.configuration.prefs.paginatorFastStep', fromParent);
		if (!fastStep) {
			fastStep = DEFAULTS.fastStep;
		}
		return fastStep;
	};

})();
