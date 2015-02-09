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
 * @author Aditya.Gaikwad
 */

'use strict';

angular.module('admin-ui.services').provider('sdDaemonService', function() {
	var self = this;

	self.$get = [ '$rootScope', '$q', '$http', function($rootScope, $q, $http) {

		var service = {};

		service.fetchDaemons = function() {
			var restUrl = 'services/rest/portal/daemons/all';
			return ajax(restUrl);
		}
		
		service.startDaemon = function(daemonType) {
			var restUrl = 'services/rest/portal/daemons/' + daemonType + '/start';
			return ajaxPut(restUrl);
		}
		
		service.stopDaemon = function(daemonType) {
			var restUrl = 'services/rest/portal/daemons/' + daemonType + '/stop';
			return ajaxPut(restUrl);
		}

		/*
		 * 
		 */
		function ajax(restUrl) {
			var deferred = $q.defer();

			var httpResponse = $http.get(restUrl);
			httpResponse.success(function(data) {
				deferred.resolve(data);
			}).error(function(data) {
				deferred.reject(data);
			});

			return deferred.promise;
		}
		
		/*
		 * 
		 */
		function ajaxPut(restUrl) {
			var deferred = $q.defer();

			var httpResponse = $http.put(restUrl);
			httpResponse.success(function(data) {
				deferred.resolve(data);
			}).error(function(data) {
				deferred.reject(data);
			});

			return deferred.promise;
		}
		
		

		return service;
	} ];
});
