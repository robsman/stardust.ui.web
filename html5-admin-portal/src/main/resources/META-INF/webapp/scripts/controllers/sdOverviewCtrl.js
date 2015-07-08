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
 */

(function() {
	'use strict';

	angular.module("admin-ui").controller(
			'sdOverviewCtrl',
			[ '$q', 'sdOverviewService', 'sdLoggerService', 'sdUtilService', 'sdUserService',
					'sdProcessInstanceService', 'sdActivityInstanceService', 'sdLoggedInUserService',
					'sdPreferenceService', OverviewCtrl ]);

	var _q;
	var _sdOverviewService
	var trace;
	var _sdUtilService;
	var _sdUserService;
	var _sdProcessInstanceService;
	var _sdActivityInstanceService;
	var _sdLoggedInUserService;
	var _sdPreferenceService;
	/**
	 * 
	 */
	function OverviewCtrl($q, sdOverviewService, sdLoggerService, sdUtilService, sdUserService,
			sdProcessInstanceService, sdActivityInstanceService, sdLoggedInUserService, sdPreferenceService) {
		trace = sdLoggerService.getLogger('admin-ui.sdOverviewCtrl');
		_q = $q;
		_sdOverviewService = sdOverviewService;
		_sdUtilService = sdUtilService;
		_sdUserService = sdUserService;
		_sdProcessInstanceService = sdProcessInstanceService;
		_sdActivityInstanceService = sdActivityInstanceService;
		_sdLoggedInUserService = sdLoggedInUserService;
		_sdPreferenceService = sdPreferenceService;

		this.allLogEntriesTable = null;
		this.showAllLogEntriesTable = true;
		this.columnSelector = _sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.rowSelectionForAllLogEntriesTable = null;
		this.exportFileNameForAllLogEntries = "AllLogEntries";

		this.getAllProcessInstanceCounts();
		this.getAllActivityInstanceCounts();
		this.getAllUserCounts();
	}

	/**
	 * 
	 * @returns
	 */
	OverviewCtrl.prototype.getAllLogEntries = function(options) {
		var deferred = _q.defer();
		var self = this;
		self.logEntries = {};
		_sdOverviewService.getAllLogEntries(options).then(function(data) {
			self.logEntries.list = data.list;
			self.logEntries.totalCount = data.totalCount;
			deferred.resolve(self.logEntries);
		}, function(error) {
			trace.log(error);
			deferred.reject(error);
		});

		return deferred.promise;
	};
	/**
	 * 
	 */
	OverviewCtrl.prototype.getAllUserCounts = function() {
		var self = this;
		_sdUserService.getAllCounts().then(function(data) {
			self.userCounts = data;
		}, function(error) {
			trace.log(error);
		});
	};
	/**
	 * 
	 */
	OverviewCtrl.prototype.getAllProcessInstanceCounts = function() {
		var self = this;
		_sdProcessInstanceService.getProcessInstanceCounts().then(function(data) {
			self.processInstanceCounts = data;
		}, function(error) {
			trace.log(error);
		});
	};
	/**
	 * 
	 */
	OverviewCtrl.prototype.getAllActivityInstanceCounts = function() {
		var self = this;
		_sdActivityInstanceService.getAllCounts().then(function(data) {
			self.activityInstanceCounts = data;
		}, function(error) {
			trace.log(error);
		});
	};

	/**
	 * 
	 */
	OverviewCtrl.prototype.refresh = function() {
		var self = this;
		self.getAllProcessInstanceCounts();
		self.getAllActivityInstanceCounts();
		self.getAllUserCounts();
		self.allLogEntriesTable.refresh();
	};

	/**
	 * 
	 */

	OverviewCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore(prefInfo.scope, 'ipp-administration-perspective', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-administration-perspective.overview.selectedColumns";
		}
		return preferenceStore;
	};

})();