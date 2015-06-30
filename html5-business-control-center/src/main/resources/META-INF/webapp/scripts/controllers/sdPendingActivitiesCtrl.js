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

	angular.module("bcc-ui").controller(
			'sdPendingActivitiesCtrl',
			[ 'sdActivityInstanceService', 'sdCommonViewUtilService', '$q', 'sdProcessInstanceService',
					'sdLoggerService', '$filter', 'sgI18nService', 'sdLoggedInUserService', 'sdPreferenceService',
					PendingActivitiesCtrl ]);

	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var _sdProcessInstanceService = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;

	/**
	 * 
	 */
	function PendingActivitiesCtrl(sdActivityInstanceService, sdCommonViewUtilService, $q, sdProcessInstanceService,
			sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService) {

		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		_sdProcessInstanceService = sdProcessInstanceService;
		trace = sdLoggerService.getLogger('bcc-ui.sdPendingActivitiesCtrl');
		_filter = $filter;
		_sgI18nService = sgI18nService;
		_sdPreferenceService = sdPreferenceService;

		this.pendingActivities = {
			totalCount : 0,
			data : []
		}

		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileName = "Pending Activities";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		this.activityTable = null;

		// Getting columns for the data table
		this.getColumns();
	}
	;

	/**
	 * 
	 */
	PendingActivitiesCtrl.prototype.getColumns = function() {
		var self = this;
		_sdActivityInstanceService.getRoleColumns().then(function(result) {
			self.columns = result;
			self.ready = true;
			trace.log('Columns retrieved :' + self.columns);
		});
	};
	/**
	 * 
	 */
	PendingActivitiesCtrl.prototype.getPendingActivities = function(options) {
		trace.log('Fetching Pending activities.');
		var self = this;
		var deferred = _q.defer();
		_sdActivityInstanceService.getPendingActivities().then(function(result) {
			trace.log('Pending activities retreived successfully.');
			self.pendingActivities.list = result;
			self.pendingActivities.totalCount = result.length;
			deferred.resolve(self.pendingActivities);
			console.log(self.pendingActivities)
		}).then(function(failure) {
			trace.log('Failed to retrive Pending activities.');
			deferred.reject(self.pendingActivities);
		});
		return deferred.promise;
	};

	/**
	 * 
	 */
	PendingActivitiesCtrl.prototype.getExportValue = function(data) {
		return (_sgI18nService.translate('business-control-center-messages.views-common-column-today') + ": "
				+ data.today + " "
				+ _sgI18nService.translate('business-control-center-messages.views-pendingActivities-column-yesterday') + " "
				+ data.yesterday + " "
				+ _sgI18nService.translate('business-control-center-messages.views-common-column-dayMonth') + " " + data.month + " "
				+ _sgI18nService.translate('business-control-center-messages.views-common-column-hibernated') + " " + data.hibernated);
	};

	/**
	 * 
	 */
	PendingActivitiesCtrl.prototype.refresh = function() {
		this.dataTable.refresh();
	};

	/**
	 * 
	 */
	
	PendingActivitiesCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore('USER',
				'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) { 
			return "ipp-business-control-center.pendingActivities.selectedColumns"; 
		}
		return preferenceStore; 
	};
	  
	  /**
	   * 
	   */
	  PendingActivitiesCtrl.prototype.setDataForActivityTable = function(oids){
		 var self = this; 
		 self.selectedOids = oids;
		 
		 if(self.activityTable != undefined){
			 self.activityTable.refresh();
		 }else{
			 self.showActivityTable = true;
		 }
		 
	  }
	  /**
		 * 
		 */
	  PendingActivitiesCtrl.prototype.getActivitiesByOids = function(params) {
			var self = this;
			var deferred = _q.defer();
			self.activities = {};
			_sdActivityInstanceService.getByOids(params, self.selectedOids).then(function(data) {
				self.activities.list = data.list;
				self.activities.totalCount = data.totalCount;
				deferred.resolve(self.activities);
			}, function(error) {
				deferred.reject(error);
			});

			return deferred.promise;
		};
	 
})();