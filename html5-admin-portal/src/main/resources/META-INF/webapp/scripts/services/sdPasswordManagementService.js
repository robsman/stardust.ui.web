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
 * 
 */
'use strict';

angular.module('admin-ui.services').provider('sdPasswordManagementService', function() {

    this.$get = [ '$resource', 'sgI18nService', function($resource, sgI18nService) {
	var service = new PasswordManagementService($resource, sgI18nService);
	return service;
    } ];
});

function PasswordManagementService($resource, sgI18nService) {
    var REST_BASE_URL = 'services/rest/portal/passwordManagement';
    var self = this;

    /**
     * 
     */
    PasswordManagementService.prototype.getPasswordRules = function() {
	var restUrl = REST_BASE_URL + "/rules";
	return $resource(restUrl).get().$promise;
    };

    /**
     * 
     */
    PasswordManagementService.prototype.savePasswordRules = function(passwordRules) {
	var restUrl = REST_BASE_URL + "/rules";
	var postData = passwordRules;
	var departments = $resource(restUrl, {}, {
	    fetch : {
		method : 'POST'
	    }
	});
	return departments.fetch({}, postData).$promise;
    };

}