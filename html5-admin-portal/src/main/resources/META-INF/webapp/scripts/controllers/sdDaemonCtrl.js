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
 * @author Aditya.Gaikwad
 */

'use strict';

(function() {

	//Closures for our dependencies injected by the DI subsystem.
	var _sdViewUtilService, _sdUserService, _sdDaemonService, _eventBus;

	var controller = function(sdViewUtilService, sdUserService,
			sdDaemonService, $http, eventBus) {
		_sdViewUtilService = sdViewUtilService;
		_sdUserService = sdUserService;
		_sdDaemonService = sdDaemonService, _eventBus = eventBus;

		this.initialize();

		this.data = {};
		this.title = "Daemons";

		this.columnSelector = "admin"; //TODO

		//TODO refactor
		this.daemonType = new Object();
		this.daemonType['event.daemon'] = "admin-portal-messages.views-daemons-eventDaemon-label";
		this.daemonType['mail.trigger'] = "admin-portal-messages.views-daemons-mailDaemon-label";
		this.daemonType['criticality.daemon'] = "admin-portal-messages.views-daemons-criticalityDaemon-label";
		this.daemonType['reporting.daemon'] = "admin-portal-messages.views-daemons-reportingDaemon-label";
		this.daemonType['system.daemon'] = "admin-portal-messages.views-daemons-systemDaemon-label";
		this.daemonType['timer.trigger'] = "admin-portal-messages.views-daemons-timeDaemon-label";
		this.daemonType['business_calendar.daemon'] = "admin-portal-messages.views-daemons-businessCalendarDaemon-label";

		this.getDaemonTypeLabel = function(key) {
			return this.daemonType[key];
		}

		this.fetchDaemons = function() {
			this.data = sdDaemonService.fetchDaemons();
			return this.data;
		}

	}

	controller.prototype.initialize = function() {
		this.daemonDataTable = null; // This will be set to underline data table instance automatically	
	}

	/*
	 * 
	 */
	controller.prototype.refresh = function() {
		this.daemonDataTable.refresh(true);
		//Refresh for sda-mode local is not working
	};

	/*
	 * 
	 */
	controller.prototype.toggleDaemonAction = function(daemonItem) {
		if (daemonItem.running) {
			_sdDaemonService.stopDaemon(daemonItem.type);
		} else {
			_sdDaemonService.startDaemon(daemonItem.type);
		}
	};

	angular.module('admin-ui').controller(
			'sdDaemonCtrl',
			[ 'sdViewUtilService', 'sdUserService', 'sdDaemonService', '$http',
					'eventBus', controller ]);

})();