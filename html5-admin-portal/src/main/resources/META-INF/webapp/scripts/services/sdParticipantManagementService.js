/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Abhay.Thappan
 * 
 */

(function() {
	'use strict';

	angular.module('admin-ui.services').provider(
			'sdParticipantManagementService',
			function() {
				this.$get = [
						'$resource',
						'sdLoggerService',
						'sdDataTableHelperService',
						function($resource, sdLoggerService, sdDataTableHelperService) {
							var service = new ParticipantManagementService($resource, sdLoggerService,
									sdDataTableHelperService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function ParticipantManagementService($resource, sdLoggerService, sdDataTableHelperService) {
		var REST_BASE_URL = "services/rest/portal/participantManagement";
		var trace = sdLoggerService.getLogger('admin-ui.services.sdParticipantManagementService');

		/**
		 * 
		 */
		ParticipantManagementService.prototype.getAllUsers = function(query) {
			// Prepare URL
			var restUrl = REST_BASE_URL + "/:type";

			var options = sdDataTableHelperService.convertToQueryParams(query.options);

			if (options.length > 0) {
				restUrl = restUrl + "?" + options.substr(1);
			}

			var postData = {
				filters : query.options.filters,
				hideInvalidatedUsers : query.hideInvalidatedUsers
			};

			var allUsers = $resource(restUrl, {
				type : '@type'
			}, {
				fetch : {
					method : 'POST'
				}
			});

			var urlTemplateParams = {};
			urlTemplateParams.type = "allUsers";

			return allUsers.fetch(urlTemplateParams, postData).$promise;

		};

	}
	;
})();
