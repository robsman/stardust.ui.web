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
    

    angular.module('bpm-common.services').provider('sdPortalConfigurationService', function() {
	this.$get = [ 'sdPreferenceService', function( sdPreferenceService) {
	    var service = new ConfigurationService( sdPreferenceService);
	    return service;
	} ];
    });

    var DEFAULTS = {
	pageSize : 8,
	maxPages : 4,
	fastStep : 3
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
	    
	    var moduleId = 'ipp-portal-common';
	    var preferenceId = 'preference';
	    var scope = 'USER';
	    var config =  sdPreferenceService.getStore(scope, moduleId, preferenceId);
	    config.fetch();
	    return config;
	}
	
	configCache = this.getConfig();

	/**
	 * Gets the page size
	 */
	this.getPageSize = function( scope ) {
	    var fromParent = false;
	    if(scope && scope == 'PARTITION'){
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var pageSize = config.getValue('ipp-portal-common.configuration.prefs.pageSize', fromParent);
	    if (!pageSize) {
		pageSize = DEFAULTS.pageSize;
	    }
	    return pageSize;
	};
	/**
	 * Gets the Max Pages
	 */
	this.getMaxPages = function(scope) {
	    var fromParent = false;
	    if(scope && scope == 'PARTITION'){
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var maxPages = config.getValue('ipp-portal-common.configuration.prefs.paginatorMaxPages', fromParent);
	    if (!pageSize) {
		maxPages = DEFAULTS.maxPages;
	    }
	    return maxPages;
	};
	
	/**
	 * Gets the fast step size
	 */
	this.getFastStepSize = function(scope) {
	    var fromParent = false;
	    if(scope && scope == 'PARTITION'){
		fromParent = true;
	    }
	    var config = this.getConfig();
	    var fastStep = config.getValue('ipp-portal-common.configuration.prefs.paginatorFastStep', fromParent);
	    if (!fastStep) {
		fastStep = DEFAULTS.fastStep;
	    }
	    return fastStep;
	};
    }

})();
