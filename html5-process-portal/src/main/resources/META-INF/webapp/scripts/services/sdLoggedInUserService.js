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
    angular.module('workflow-ui.services').provider('sdLoggedInUserService', function() {
	this.$get = [ 'sdUtilService', function(sdUtilService) {
	    var service = new LoggedInUserService(sdUtilService);
	    return service;
	} ];
    });

    var user = null;
    /**
     * 
     */
    function LoggedInUserService(sdUtilService) {
    	
    	var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/user";

	/**
	 * 
	 */
	LoggedInUserService.prototype.getUserInfo = function() {
	    var restUrl = REST_BASE_URL + "/whoAmI";
	    var self = this;

	    if (!user) {
		user = sdUtilService.syncAjax(restUrl);
	    }
	    return user;
	};

    }
})();
