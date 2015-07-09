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

(function() {
	'use strict';

	angular.module('admin-ui').controller('sdDaemonCtrl',
			[ 'sdDaemonService', 'sdLoggedInUserService', 'sgI18nService' ,controller ]);

	var _sdDaemonService;
	var _sdLoggedInUserService;
	var _sgI18nService;

	/*
	 * 
	 */
	function controller(sdDaemonService,sdLoggedInUserService, sgI18nService) {
		_sdDaemonService = sdDaemonService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sgI18nService = sgI18nService;
		this.initialize();

		this.data = {};

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true; 
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonTypeLabel = function(key) {
		var keyPrefix  = "admin-portal-messages.views-daemons";
		var keySuffix = "label";
		var hyphen = "-";
		//Replace charcter '.' and '_' with nothing
		key = key.replace(/\.|_/g, "");
		var words = ["calendar", "daemon", "trigger"];
		for (var i in words) { // Capitalize first character of above words if found.
			key = key.replace(words[i], words[i].charAt(0).toUpperCase() + words[i].slice(1));
		}
		key = keyPrefix + hyphen + key + hyphen + keySuffix;
		return _sgI18nService.translate(key, key);
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonStatus = function(daemon) {
		return (daemon.running) ? _sgI18nService.translate('admin-portal-messages.views-daemons-status-column-running', 'Running')
								: _sgI18nService.translate('admin-portal-messages.views-daemons-status-column-stopped', 'Stopped');
	}

	/*
	 * 
	 */
	controller.prototype.fetchDaemons = function() {
		this.data = _sdDaemonService.fetchDaemons();
		return this.data;
	}

	/*
	 * 
	 */
	controller.prototype.initialize = function() {
		this.daemonDataTable = null; // This will be set to underline data table instance automatically
	}

	/*
	 * 
	 */
	controller.prototype.refresh = function() {
		this.daemonDataTable.refresh(true);
	};

	/*
	 * 
	 */
	controller.prototype.startDaemon = function(daemonItem) {
		var self = this;
		if (!(daemonItem.running)) {
			_sdDaemonService.startDaemon(daemonItem.type).then(function(data) {
				self.refresh();
			});
		}
	};
	
	/*
	 * 
	 */
	controller.prototype.stopDaemon = function(daemonItem) {
		var self = this;
		if (daemonItem.running) {
			_sdDaemonService.stopDaemon(daemonItem.type).then(function(data) {
				self.refresh();
			});
		}
	};
})();