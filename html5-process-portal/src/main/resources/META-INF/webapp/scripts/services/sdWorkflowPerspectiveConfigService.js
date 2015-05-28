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
	this.$get = ['sdPreferenceService', function( sdPreferenceService) {
	    var service = new ConfigurationService( sdPreferenceService);
	    return service;
	} ];
    });

    var DEFAULTS = {
	activityAbortScope : '',
	processAbortScope : '',
    }
    /*
     * 
     */
    function ConfigurationService( sdPreferenceService) {
	
	 var configCache = null;
	 
	/**
	 * 
	 */
	this.getConfig = function() {
	    
	    if (configCache != null) {
		return configCache;
	    }
	    var moduleId = 'ipp-views-common';
	    var preferenceId = 'preference';
	    var scope = 'USER';
	    var config =  sdPreferenceService.getStore(scope, moduleId, preferenceId);
	    config.fetch();
	    return config;
	};
	
	configCache = this.getConfig();

	/**
	 * Gets the activity abort scope
	 */
	this.getAbortActivityScope = function(scope) {

	    var fromParent = false;
	    if(scope && scope == 'PARTITION'){
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var abortScope = config.getValue('ipp-views-common.workflowExecutionConfigurationPanel.prefs.activityAbortScope', fromParent);
	    if (!abortScope) {
		abortScope = DEFAULTS.activityAbortScope;
	    }
	    return abortScope;
	};
	
	/**
	 * Gets the process abort scope
	 */
	this.getAbortProcessScope = function(scope) {

	    var fromParent = false;
	    if(scope && scope == 'PARTITION'){
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var abortScope = config.getValue('ipp-views-common.workflowExecutionConfigurationPanel.prefs.processAbortScope', fromParent);
	    if (!abortScope) {
		abortScope = DEFAULTS.activityAbortScope;
	    }
	    return abortScope;
	};
    }
})();
