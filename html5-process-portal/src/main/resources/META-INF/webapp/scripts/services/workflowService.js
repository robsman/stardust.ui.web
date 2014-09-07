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

angular.module('workflow-ui.services').provider('workflowService', function () {
	var self = this;
	
	self.$get = ['$rootScope', function ($rootScope) {

		var service = {};

		/*
		 * 
		 */
		service.getWorklist = function(query) {
			var deferred = jQuery.Deferred();

			console.log("getting worklist for:");
			console.log(query);

			// TODO: Use Angular $resource and call real rest end point
			jQuery.ajax({
				url: "plugins/html5-process-portal/jsons/worklist.json?" + JSON.stringify(query),
				type: "GET",
		        contentType: "application/json"
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
