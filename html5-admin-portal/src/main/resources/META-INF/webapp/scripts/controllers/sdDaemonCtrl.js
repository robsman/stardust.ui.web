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
			[ 'sdDaemonService', 'sdLoggedInUserService' ,controller ]);

	var _sdDaemonService;
	var _sdLoggedInUserService;

	/*
	 * 
	 */
	function controller(sdDaemonService,sdLoggedInUserService) {
		_sdDaemonService = sdDaemonService;
		_sdLoggedInUserService = sdLoggedInUserService;
		this.initialize();

		this.data = {};
		this.title = "Daemons";

		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ?  'admin' : true; 
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonTypeLabel = function(key) {
		return _sdDaemonService.getDaemonTypeLabel(key);
	}

	/*
	 * 
	 */
	controller.prototype.getDaemonStatus = function(daemon) {
		return _sdDaemonService.getDaemonStatus(daemon);
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
		this.daemonDataTable = null; // This will be set to underline data
		// table instance automatically
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
	controller.prototype.toggleDaemonAction = function(daemonItem) {
		if (daemonItem.running) {
			return _sdDaemonService.stopDaemon(daemonItem.type);
		} else {
			return _sdDaemonService.startDaemon(daemonItem.type);
		}
	};

})();