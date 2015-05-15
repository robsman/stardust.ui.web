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
    angular.module('bpm-common.services').provider('sdLocalizationService', function() {
	this.$get = [ 'sdUtilService', function(sdUtilService) {
	    var service = new LocalizationService(sdUtilService);
	    return service;
	} ];
    });

    var localizationInfo = null;
    /**
     * 
     */
    function LocalizationService(sdUtilService) {

    	var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/localization";
	/**
	 * 
	 */
	LocalizationService.prototype.getInfo = function() {
	    var restUrl = REST_BASE_URL + "/info";
	    var self = this;

	    if (!localizationInfo) {
		localizationInfo = sdUtilService.syncAjax(restUrl);
	    }
	    return localizationInfo;
	};

    }
})();
