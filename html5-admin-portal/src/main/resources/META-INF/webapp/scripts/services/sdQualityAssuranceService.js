/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Johnson.Quadras
 */

(function() {
    'use strict';

    angular.module('admin-ui.services').provider('sdQualityAssuranceService', function() {
	this.$get = [ '$resource', 'sgI18nService', function($resource, sgI18nService) {
	    var service = new QualityAssuranceService($resource, sgI18nService);
	    return service;
	} ];
    });

    /*
     * 
     */
    function QualityAssuranceService($resource, sgI18nService) {
	var REST_BASE_URL = 'services/rest/portal/qualityAssuranceManagement';
	var self = this;

	/**
	 * 
	 */
	QualityAssuranceService.prototype.getQaActivities = function(fetchObsoleteActivities) {
	    var restUrl = REST_BASE_URL + "/activities";
	    restUrl = restUrl + "?showObsoleteActivities=" + fetchObsoleteActivities;
	    return $resource(restUrl).query().$promise;
	};

	/**
	 * 
	 */
	QualityAssuranceService.prototype.getQaDepartments = function(processQId, activityQId) {
	    var restUrl = REST_BASE_URL + "/departments";

	    var postData = {
		"processQualifiedId" : processQId,
		"activityQualifiedId" : activityQId
	    };
	    var departments = $resource(restUrl, {}, {
		fetch : {
		    method : 'POST',
		    isArray : true
		}
	    });

	    return departments.fetch({}, postData).$promise;
	};

	/**
	 * 
	 */
	QualityAssuranceService.prototype.updateQaProbabilities = function(activities, departments) {
	    var restUrl = REST_BASE_URL + "/updateQaProbabilities";

	    var postData = {
		"activities" : activities,
		"departments" : departments
	    };
	    var departments = $resource(restUrl, {}, {
		updatePriorities : {
		    method : 'POST'
		}
	    });

	    return departments.updatePriorities({}, postData).$promise;
	};
    }

})();
