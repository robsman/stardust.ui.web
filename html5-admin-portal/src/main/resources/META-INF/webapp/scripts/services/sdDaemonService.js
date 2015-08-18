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
						function($resource, sgI18nService) {
							var service = new DaemonService($resource,
									sgI18nService);
							return service;
						} ];
			});

	var DEF_MESSAGES = {
		'admin-portal-messages.views-daemons-eventDaemon-label' : 'Event Daemon',
		'admin-portal-messages.views-daemons-mailDaemon-label' : 'Mail Trigger Daemon',
		'admin-portal-messages.views-daemons-criticalityDaemon-label' : 'Prioritization Daemon',
		'admin-portal-messages.views-daemons-reportingDaemon-label' : 'Reporting Daemon',
		'admin-portal-messages.views-daemons-systemDaemon-label' : 'System Daemon',
		'admin-portal-messages.views-daemons-timeDaemon-label' : 'Timer Trigger Daemon',
		'admin-portal-messages.views-daemons-businessCalendarDaemon-label' : 'Business Calendar Daemon',
		'admin-portal-messages.views-daemons-status-column-running' : 'Running',
		'admin-portal-messages.views-daemons-status-column-stopped' : 'Stopped'
	}

	/*
	 * 
	 */
	function DaemonService($resource, sgI18nService) {
		var REST_BASE_URL = 'services/rest/portal/daemons';

		// TODO refactor
		this.daemonType = new Object();
		this.daemonType['event.daemon'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-eventDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['mail.trigger'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-mailDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['criticality.daemon'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-criticalityDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['reporting.daemon'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-reportingDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['system.daemon'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-systemDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['timer.trigger'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-timeDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);
		this.daemonType['business_calendar.daemon'] = sgI18nService
				.translate(
						'admin-portal-messages.views-daemons-businessCalendarDaemon-label',
						DEF_MESSAGES['admin-portal-messages.views-daemons-eventDaemon-label']);

		/*
		 * 
		 */
		DaemonService.prototype.getDaemonTypeLabel = function(key) {
			return this.daemonType[key];
		}

		/*
		 * 
		 */
		DaemonService.prototype.getDaemonStatus = function(daemon) {
			return (daemon.running) ? sgI18nService
					.translate(
							'admin-portal-messages.views-daemons-status-column-running',
							 DEF_MESSAGES['admin-portal-messages.views-daemons-status-column-running'])
					: sgI18nService
							.translate(
									'admin-portal-messages.views-daemons-status-column-stopped',
									 DEF_MESSAGES['admin-portal-messages.views-daemons-status-column-stopped']);
		}

		/*
		 * 
		 */
		DaemonService.prototype.fetchDaemons = function() {
			return $resource(REST_BASE_URL + "/all").query().$promise;
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
