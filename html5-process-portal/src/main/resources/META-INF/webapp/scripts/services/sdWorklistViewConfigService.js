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

    angular.module('workflow-ui.services').provider('sdWorklistViewConfigService', function() {
	this.$get = [ 'sdPreferenceService', function(sdPreferenceService) {
	    var service = new ConfigurationService(sdPreferenceService);
	    return service;
	} ];
    });

    var DEFAULTS = {
	refreshInterval : 0
    }
    var REFRESH_INTERVAL_MULTIPLIER = 60000;

    /*
     * 
     */
    function ConfigurationService(sdPreferenceService) {

	var configCache = null;

	/**
	 * 
	 */
	this.getConfig = function() {

	    if (configCache != null) {
		return configCache;
	    }

	    var moduleId = 'ipp-workflow-perspective';
	    var preferenceId = 'preference';
	    var scope = 'USER';
	    var config = sdPreferenceService.getStore(scope, moduleId, preferenceId);
	    config.fetch();
	    return config;
	};

	configCache = this.getConfig();

	/**
	 * Gets the refresh interval in mins
	 */
	this.getRefreshIntervalInMins = function(scope) {

	    var fromParent = false;
	    if (scope && scope == 'PARTITION') {
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var refreshInterval = config
		    .getValue('ipp-workflow-perspective.worklist.prefs.refreshInterval', fromParent);
	    if (!refreshInterval) {
		refreshInterval = DEFAULTS.refreshInterval;
	    }
	    return refreshInterval;
	};

	/**
	 * Gets the refresh interval in milis
	 */
	this.getRefreshIntervalInMillis = function(scope) {
	    return this.getRefreshIntervalInMins(scope) * REFRESH_INTERVAL_MULTIPLIER;
	};
    }
})();
