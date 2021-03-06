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

(function() {
	'use strict';

	angular.module('admin-ui.services').provider(
			'sdDaemonService',
			function() {
				this.$get = [
						'$resource',
						'sgI18nService',
						'sdUtilService',
						function($resource, sgI18nService, sdUtilService) {
							var service = new DaemonService($resource,
									sgI18nService, sdUtilService);
							return service;
						} ];
			});

	/*
	 * 
	 */
	function DaemonService($resource, sgI18nService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + 'services/rest/portal/daemons';

		/**
		 * Returns all daemon data other than the acknowledge state which can be acquired 
		 * individually per daemon using the getDaemon function.
		 * @return {[type]}            [description]
		 */
		DaemonService.prototype.fetchDaemons = function() {
			return $resource(REST_BASE_URL + "/all").get().$promise;
		}

		/**
		 * Returns daemon data including ack state.
		 * @param  {[type]} daemonType [description]
		 * @return {[type]}            [description]
		 */
		DaemonService.prototype.getDaemon = function(daemonType) {
			return $resource(REST_BASE_URL + "/" + daemonType).get().$promise;
		}

		/*
		 * 
		 */
		DaemonService.prototype.startDaemon = function(daemonType) {
			var daemon = $resource(REST_BASE_URL + '/:daemon/start', { daemon : '@daemon' }, {
				update : {
					method : 'PUT' // this method issues a PUT request
				}
			});
			var urlTemplateParams = {};
			urlTemplateParams.daemon = daemonType; 
			return daemon.update(urlTemplateParams).$promise;
		}

		/*
		 * 
		 */
		DaemonService.prototype.stopDaemon = function(daemonType) {
			var daemon = $resource(REST_BASE_URL + '/:daemon/stop', { daemon : '@daemon' }, {
				update : {
					method : 'PUT'
				}
			});
			var urlTemplateParams = {};
			urlTemplateParams.daemon = daemonType;
			return daemon.update(urlTemplateParams).$promise;
		}

	}

})();
