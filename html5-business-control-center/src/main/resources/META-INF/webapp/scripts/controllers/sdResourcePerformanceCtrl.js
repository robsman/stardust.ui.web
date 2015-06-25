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
			'sdResourcePerformanceCtrl',
			[ 'sdResourcePerformanceService', 'sdActivityInstanceService', 'sdCommonViewUtilService', '$q',
					'sdLoggerService', '$filter', 'sgI18nService', 'sdLoggedInUserService', 'sdPreferenceService',
					ResourcePerformanceCtrl ]);

	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;
	var _sdResourcePerformanceService;

	/**
	 * 
	 */
	function ResourcePerformanceCtrl(sdResourcePerformanceService, sdActivityInstanceService, sdCommonViewUtilService,
			$q, sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService) {

		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		trace = sdLoggerService.getLogger('bcc-ui.sdResourcePerformanceCtrl');
		_filter = $filter;
		_sgI18nService = sgI18nService;
		_sdPreferenceService = sdPreferenceService;
		_sdResourcePerformanceService = sdResourcePerformanceService;

		this.resourcePerformance = {
			totalCount : 0,
			list : []
		}

		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileName = "Resource Performance";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		this.activityTable = null;

		// Getting columns for the data table
		this.getRoles();
	}
	;

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getRoles = function() {
		var self = this;
		_sdActivityInstanceService.getRoleColumns().then(function(result) {
			self.roles = result;
			self.getResourcePerformance(self.roles[0].value);
			trace.log('Columns retrieved :' + self.roles);
		}).then(function(failure) {
			trace.log(failure);
			//deferred.reject(failure);
		});
	};
	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getResourcePerformance = function(roleId) {
		trace.log('Fetching Resource Performance Data.');
		var self = this;
		_sdResourcePerformanceService.getResourcePerformanceData(roleId).then(function(result) {
			trace.log('Resource Performance Data retreived successfully.');
			self.resourcePerformance.list = result.list;
			self.resourcePerformance.totalCount = result.totalCount;
			self.columns = result.columns;
			self.columnsDefinition = result.columnsDefinition;
			self.ready = true;
			//deferred.resolve(self.pendingActivities);
			console.log(self.resourcePerformance)
		}).then(function(failure) {
			trace.log('Failed to retrive Resource Performance Data.');
			//deferred.reject(failure);
		});
		//return deferred.promise;
	};
	
	ResourcePerformanceCtrl.prototype.getResourcePerformanceData = function(options){
		var self = this;
		return self.resourcePerformance;
	};

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getExportValue = function(data) {
		return (_sgI18nService.translate('business-control-center-messages.views-common-column-today') + ": "
				+ data.today + " "
				+ _sgI18nService.translate('business-control-center-messages.views-pendingActivities-column-yesterday')
				+ " " + data.yesterday + " "
				+ _sgI18nService.translate('business-control-center-messages.views-common-column-dayMonth') + " "
				+ data.month + " "
				+ _sgI18nService.translate('business-control-center-messages.views-common-column-hibernated') + " " + data.hibernated);
	};

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.refresh = function() {
		this.dataTable.refresh();
	};

	/**
	 * 
	 */

	ResourcePerformanceCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var preferenceStore = _sdPreferenceService.getStore('USER', 'ipp-business-control-center', 'preference'); // Override
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ResourcePerformance.selectedColumns";
		}
		return preferenceStore;
	};

})();
