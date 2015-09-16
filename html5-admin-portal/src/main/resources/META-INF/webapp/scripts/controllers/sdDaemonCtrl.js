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

	angular.module('admin-ui').controller(
			'sdDaemonCtrl',
			['sdDaemonService', 'sdLoggedInUserService', 'sgI18nService', '$filter', '$q', 'sdDataTableHelperService',
					'sdLoggerService', controller]);

	var _sdDaemonService;
	var _sdLoggedInUserService;
	var _sgI18nService;
	var _filter;
	var _q;
	var _sdDataTableHelperService;
	var trace;

	/*
	 * 
	 */
	function controller(sdDaemonService, sdLoggedInUserService, sgI18nService, $filter, $q, sdDataTableHelperService,
			sdLoggerService) {
		_sdDaemonService = sdDaemonService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sgI18nService = sgI18nService;
		_filter = $filter;
		_q = $q;
		_sdDataTableHelperService = sdDataTableHelperService;
		trace = sdLoggerService.getLogger('admin-ui.sdDaemonCtrl');

		this.initialize();

		// this.data = {};
		this.lookup = {};

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		
		this.fetchDaemonsData();
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonTypeLabel = function(key) {
		var keyPrefix = "admin-portal-messages.views-daemons";
		var keySuffix = "label";
		var hyphen = "-";
		// Replace charcter '.' and '_' with nothing
		key = key.replace(/\.|_/g, "");
		var words = ["calendar", "daemon", "trigger"];
		for ( var i in words) { // Capitalize first character of above words if
			// found.
			key = key.replace(words[i], words[i].charAt(0).toUpperCase() + words[i].slice(1));
		}
		key = keyPrefix + hyphen + key + hyphen + keySuffix;
		return _sgI18nService.translate(key, key);
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonStatus = function(daemon) {
		return (daemon.running) ? _sgI18nService.translate('admin-portal-messages.views-daemons-status-column-running',
				'Running') : _sgI18nService.translate('admin-portal-messages.views-daemons-status-column-stopped',
				'Stopped');
	}

	/*
	 * 
	 */
	controller.prototype.fetchDaemonsData = function() {
		var self = this;
		_sdDaemonService.fetchDaemons().then(function(data) {
			self.data = data;
			if (self.daemonDataTable != null) {
				self.daemonDataTable.refresh();
			} else {
				self.showDaemonTable = true;
			}

		}, function(error) {
			trace.log(error);
		});
	};

	/*
	 * 
	 */
	controller.prototype.fetchDaemons = function(options) {
		var self = this;
		var result = {
			list : self.data.list,
			totalCount : self.data.totalCount
		}

		for (var i = 0, len = result.list.length; i < len; i++) {
			self.lookup[result.list[i].type] = i;
		}

		return result;
	};

	/*
	 * 
	 */
	controller.prototype.initialize = function() {
		this.daemonDataTable = null; // This will be set to underline data
		// table instance automatically
	}

	/*
	 * 
	 */
	controller.prototype.refresh = function() {
		//this.showDaemonTable = false;
		this.fetchDaemonsData();
	};

	/*
	 * 
	 */
	controller.prototype.startDaemon = function(daemonItem) {
		var self = this;
		if (!(daemonItem.running)) {
			_sdDaemonService.startDaemon(daemonItem.type).then(function(result) {
				self.updateDaemonStatus(result);
			});
		}
	};

	/*
	 * 
	 */
	controller.prototype.stopDaemon = function(daemonItem) {
		var self = this;
		if (daemonItem.running) {
			_sdDaemonService.stopDaemon(daemonItem.type).then(function(result) {
				self.updateDaemonStatus(result);
			});
		}
	};

	/*
	 * 
	 */
	controller.prototype.updateDaemonStatus = function(resultDaemon) {
		var itemRowIndex = this.lookup[resultDaemon.type];
		var tableDaemonData = this.daemonDataTable.getData(itemRowIndex);
		tableDaemonData.running = resultDaemon.running;
		tableDaemonData.startTime = resultDaemon.startTime;
		tableDaemonData.lastExecutionTime = resultDaemon.lastExecutionTime;
		//tableDaemonData.acknowledgementState = resultDaemon.acknowledgementState;
		tableDaemonData.daemonExecutionState = resultDaemon.daemonExecutionState;
	};

})();