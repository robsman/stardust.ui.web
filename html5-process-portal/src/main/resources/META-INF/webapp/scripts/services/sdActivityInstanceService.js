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
 * @author Subodh.Godbole
 */

'use strict';

angular.module('workflow-ui.services').provider('sdActivityInstanceService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var REST_BASE_URL = "services/rest/portal/activity-instances/";

		var service = {};

		/*
		 * 
		 */
		service.getDataMappings = function(oids) {
			console.log("Getting Data Mappings for: ");
			console.log(oids);
			return ajax(REST_BASE_URL, "dataMappings", oids);
		};

		/*
		 * 
		 */
		service.getInData = function(oids) {
			console.log("Getting In Data for: ");
			console.log(oids);
			return ajax(REST_BASE_URL, "inData", oids);
		};

		/*
		 * 
		 */
		service.getTrivialManualActivitiesDetails = function(oids) {
			console.log("Getting Trivial Manual Activities Details for: ");
			console.log(oids);
			return ajax(REST_BASE_URL, "trivialManualActivitiesDetails", oids);
		};

		/*
		 * 
		 */
		service.completeAll = function(activities) {
			console.log("Completing Activities: ");
			console.log(activities);
			return ajax(REST_BASE_URL, "completeAll", activities);
		};

		/*
		 * 
		 */
		function ajax(restUrl, extension, value) {
			var deferred = jQuery.Deferred();

			var type;
			var data;
			if (angular.isObject(value) || angular.isArray(value)) {
				restUrl += extension;
				type = "POST";
				data = JSON.stringify(value);
			} else {
				restUrl += value + "/" + extension;
				type = "GET";
			}
			
			// TODO: Use Angular $resource
			jQuery.ajax({
			  	url: restUrl,
				type: type,
		        contentType: "application/json",
		        data : data
			}).done(function(result) {
				deferred.resolve(result);
			}).fail(function(data) {
				deferred.reject(data);
		    });

			return deferred.promise();
		};

		return service;
	}];
});
