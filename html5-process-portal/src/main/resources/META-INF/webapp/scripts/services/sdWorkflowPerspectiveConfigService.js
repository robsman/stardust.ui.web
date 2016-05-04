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
(function(){
	'use strict';
	angular.module('workflow-ui.services').provider('sdWorkflowPerspectiveConfigService', function() {
		this.$get = ['sdPreferenceService','$q', function( sdPreferenceService, $q) {
			var service = new ConfigurationService( sdPreferenceService, $q);
			return service;
		} ];
	});

	var DEFAULTS = {
			activityAbortScope : '',
			processAbortScope : '',
	}

	var moduleId = 'ipp-views-common';
	var preferenceId = 'preference';
	var scope = 'USER';
	/*
	 * 
	 */
	function ConfigurationService( sdPreferenceService, $q) {
		var configCache = null;
		/**
		 * 
		 */
		this.getCache = function () {
			if (configCache != null) {
				return configCache;
			} else {
				throw "sdWorkflowPerspectiveConfigService not initialized yet."
			}
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
	 * Gets the activity abort scope
	 */
	ConfigurationService.prototype.getAbortActivityScope = function(scope) {

		var fromParent = false;
		if(scope && scope == 'PARTITION'){
			fromParent = true;
		}
		var config = this.getCache();
		var abortScope = config.getValue('ipp-views-common.workflowExecutionConfigurationPanel.prefs.activityAbortScope', fromParent);
		if (!abortScope) {
			abortScope = DEFAULTS.activityAbortScope;
		}
		return abortScope;
	};

	/**
	 * Gets the process abort scope
	 */
	ConfigurationService.prototype.getAbortProcessScope = function(scope) {

		var fromParent = false;
		if(scope && scope == 'PARTITION'){
			fromParent = true;
		}
		var config = this.getCache();
		var abortScope = config.getValue('ipp-views-common.workflowExecutionConfigurationPanel.prefs.processAbortScope', fromParent);
		if (!abortScope) {
			abortScope = DEFAULTS.activityAbortScope;
		}
		return abortScope;
	};
})();
