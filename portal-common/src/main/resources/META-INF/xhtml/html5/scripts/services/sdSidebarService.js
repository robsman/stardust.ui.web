/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Subodh.Godbole
 */

'use strict';

angular.module('bpm-ui.services').provider('sdSidebarService', function () {
	var self = this;
	var REST_BASE_URL = 'services/rest/portal/';

	self.$get = ['$q', 'sdUtilService', 'sdViewUtilService', function ($q, sdUtilService, sdViewUtilService) {

		var service = {};

		var perspectives;
		var activePerspective;

		/*
		 * 
		 */
		service.initialize = function() {
			var deferred = $q.defer();

			sdUtilService.ajax(REST_BASE_URL, '', 'perspectives').then(function(result) {
				perspectives = result;
				angular.forEach(result, function(perspective) {
					if (perspective.active) {
						activePerspective = perspective;
					}										
				});
				deferred.resolve();
				
			});			
			return deferred.promise;
		}

		/*
		 * 
		 */
		service.getPerspectives = function() {
			return perspectives;
		}

		/*
		 * 
		 */
		service.activatePerspective = function(perspective) {
			sdViewUtilService.changePerspective(perspective.name);
			activePerspective = perspective;
			return activePerspective;
		}

		/*
		 * 
		 */
		service.getActivePerspectiveName = function() {
			return activePerspective.name;
		}
		
		/*
		 * 
		 */
		service.getActivePerspective = function() {
			return activePerspective;
		}

		return service;
	}];
});