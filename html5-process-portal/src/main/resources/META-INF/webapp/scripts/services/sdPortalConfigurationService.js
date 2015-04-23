/*****************************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ****************************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('workflow-ui.services').provider('sdPortalConfigurationService', function() {
	this.$get = [ '$resource', function($resource) {
	    var service = new WorklistService($resource);
	    return service;
	} ];
    });
    /*
     * 
     */
    function WorklistService($resource) {
	var REST_BASE_URL = "services/rest/portal/portalConfiguration/";
	/*
	 * 
	 */
	WorklistService.prototype.getConfiguration = function(scope) {
	    var restUrl = REST_BASE_URL + "configuration/"+scope;
	    return $resource(restUrl).get().$promise;
	};
    }

})();
