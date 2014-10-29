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

(function(){
	'use strict';

	/*
	 * 
	 */
	function WorklistService($rootScope, $resource) {
		var REST_BASE_URL = "services/rest/portal/worklist/";

		/*
		 * 
		 */
		WorklistService.prototype.getWorklist = function(query) {
			console.log("Getting worklist for:");
			console.log(query);

			// Prepare URL
			var restUrl = REST_BASE_URL + ":type/:id";

			// Add Query String Params
			var options = "";
			angular.forEach(query.options, function(value, key){
				options += "&" + key + "=" + value;
			});

			if (options.length > 1) {
				restUrl = restUrl + "?" + options.substr(1);
			}

			// Prepare Query Params
			var urlTemplateParams = {};
			if (query.participantQId) {
				urlTemplateParams.type = "participant";
				urlTemplateParams.id = query.participantQId;
			} else if (query.userId) {
				urlTemplateParams.type = "user";
				urlTemplateParams.id = query.userId;
			}

			return $resource(restUrl).get(urlTemplateParams).$promise;
		};
	};

	angular.module('workflow-ui.services').provider('sdWorklistService', function () {
		this.$get = ['$rootScope', '$resource', function ($rootScope, $resource) {
			var service = new WorklistService($rootScope, $resource);
			return service;
		}];
	});
})();
