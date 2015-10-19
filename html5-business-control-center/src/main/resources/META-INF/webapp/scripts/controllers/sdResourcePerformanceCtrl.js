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
					'sdUtilService', ResourcePerformanceCtrl ]);

	var _sdActivityInstanceService = null;
	var _sdCommonViewUtilService = null;
	var _q = null;
	var trace = null;
	var _filter = null;
	var _sgI18nService = null;
	var _sdPreferenceService = null;
	var _sdResourcePerformanceService;
	var _sdUtilService;

	/**
	 * 
	 */
	function ResourcePerformanceCtrl(sdResourcePerformanceService, sdActivityInstanceService, sdCommonViewUtilService,
			$q, sdLoggerService, $filter, sgI18nService, sdLoggedInUserService, sdPreferenceService, sdUtilService) {

		_sdActivityInstanceService = sdActivityInstanceService;
		_sdCommonViewUtilService = sdCommonViewUtilService;
		_q = $q;
		trace = sdLoggerService.getLogger('bcc-ui.sdResourcePerformanceCtrl');
		_filter = $filter;
		_sgI18nService = sgI18nService;
		_sdPreferenceService = sdPreferenceService;
		_sdResourcePerformanceService = sdResourcePerformanceService;
		_sdUtilService = sdUtilService;

		this.resourcePerformance = {
			totalCount : 0,
			list : []
		}
		this.userQualifierId = sdLoggedInUserService.getUserInfo().qualifiedId;
		this.columnSelector = sdLoggedInUserService.getUserInfo().isAdministrator ? 'admin' : true;
		this.exportFileName = "Resource Performance";
		this.columns = [];
		this.ready = false;
		this.dataTable = null;
		this.dateTypes = [ {
			'label' : 'Days',
			'value' : 1
		}, {
			'label' : 'Weeks',
			'value' : 2
		}, {
			'label' : 'Months',
			'value' : 3
		}, {
			'label' : 'Years',
			'value' : 4
		} ];
		this.getRoles();
	}
	;

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getConfig = function(prefScope) {
		var moduleId = 'ipp-business-control-center';
		var preferenceId = 'preference';
		var config = _sdPreferenceService.getStore(prefScope, moduleId, preferenceId);
		config.fetch();
		return config;
	}

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getRoles = function() {
		var self = this;
		_sdActivityInstanceService.getRoleColumns().then(function(result) {
			self.roles = result;
			if (self.roles.length > 0) {
				self.selectedRole = self.roles[0].value;
				self.getResourcePerformance(self.selectedRole);
			} else {
				self.getResourcePerformance(null);
			}
			trace.log('Columns retrieved :' + self.roles);
		}).then(function(failure) {
			trace.log(failure);
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
		    trace.log(self.resourcePerformance)
		}).then(function(failure) {
			trace.log('Failed to retrive Resource Performance Data.');

		});
	};

	ResourcePerformanceCtrl.prototype.getResourcePerformanceData = function(options) {
		var self = this;
		return self.resourcePerformance;
	};

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.roleChanged = function() {
		var self = this;
		self.ready = false;
		self.getResourcePerformance(self.selectedRole);
	};

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.getExportValue = function(data) {
		var stateTitle = '';
		if (data.state == 1) {
			stateTitle = _sgI18nService.translate('business-control-center-messages.views-trafficLightView-critical');
		} else if (data.state == 2) {

			stateTitle = _sgI18nService.translate('business-control-center-messages.views-trafficLightView-warning');
		} else if (data.state == 3) {
			stateTitle = _sgI18nService.translate('business-control-center-messages.views-trafficLightView-normal');
		}
		return (_sgI18nService
				.translate('business-control-center-messages.views-resourcePerformance-column-waitingTime')
				+ ": "
				+ data.averageTime
				+ " "
				+ _sgI18nService
						.translate('business-control-center-messages.views-resourcePerformance-column-processingTime')
				+ ": "
				+ data.averageWaitingTime
				+ " "
				+ _sgI18nService.translate('business-control-center-messages.views-common-column-status') + ": " + stateTitle);
	};

	/**
	 * 
	 */
	ResourcePerformanceCtrl.prototype.refresh = function() {
		var self = this;
		self.ready = false;
		self.getResourcePerformance(self.selectedRole);
	};

	/**
	 * 
	 */

	ResourcePerformanceCtrl.prototype.preferenceDelegate = function(prefInfo) {
		var self = this;
		self.prefScope = prefInfo.scope;
		var preferenceStore = _sdPreferenceService
				.getStore(prefInfo.scope, "ipp-business-control-center", 'preference');
		preferenceStore.marshalName = function(scope) {
			return "ipp-business-control-center.ResourcePerformance.selectedColumns";
		}
		return preferenceStore;
	};


	ResourcePerformanceCtrl.prototype.openAddCustomColumnDlg = function() {
		var self = this;
		self.startNumOfDaysRange = 31;
		self.durationNumOfDaysRange = 31;
		self.columnDefinition = {};

		var preferenceStore = this.getConfig(self.prefScope);
		var allColumns = preferenceStore.getValue('ipp-business-control-center.ResourcePerformance.allColumns', false);
		var index = 0;
		if (_sdUtilService.isEmpty(allColumns)) {
			index++;
			var columnTitle = _sgI18nService.translate('business-control-center-messages.views-customColumn-label')
					+ index;
			var columnId = _sgI18nService.translate('business-control-center-messages.views-customColumn-property')
					+ index++;
			self.columnDefinition = {
				"columnId" : columnId,
				"columnTitle" : columnTitle,
				"startNumOfDays" : 0,
				"startDateType" : 1,
				"durationNumOfDays" : 1,
				"durationDateType" : 1,
				"showDatePicker" : false,
				"userQualifierId" : self.userQualifierId,
				"readOnly" : false
			};
			allColumns = [];
		} else {

			try {
				allColumns = JSON.parse(allColumns);
				// Do nothing
			} catch (e) {
				if (allColumns == '$#$') {
					allColumns = [];
				} else {
					var customCols = allColumns.split('$#$');
					allColumns = [];
					angular.forEach(customCols, function(customCol) {
						var customColDef = customCol.split('#');
						allColumns.push(JSON.parse(customColDef[1]));
					});
				}
			}

			angular.forEach(allColumns, function(customCol) {
				var columnId = customCol.columnId;
				var numOccurance = columnId.search(/\d/);
				var newIndex = parseInt(columnId.substring(numOccurance)) + 1;
				if (newIndex > index) {
					index = newIndex;
				}
			});

			var columnTitle = _sgI18nService.translate('business-control-center-messages.views-customColumn-label')
					+ index;
			var columnId = _sgI18nService.translate('business-control-center-messages.views-customColumn-property')
					+ index++;
			self.columnDefinition = {
				"columnId" : columnId,
				"columnTitle" : columnTitle,
				"startNumOfDays" : 0,
				"startDateType" : 1,
				"durationNumOfDays" : 1,
				"durationDateType" : 1,
				"showDatePicker" : false,
				"userQualifierId" : self.userQualifierId,
				"readOnly" : false
			};
			//allColumns.push(columnDefinition);
		}
		self.allColumns = allColumns;
		self.showOpenAddCustomColumnDlg = true;
	};

	ResourcePerformanceCtrl.prototype.setStartNumOfDaysRange = function(dateType) {
		var self = this;
		switch (dateType) {
		case 1:
			self.startNumOfDaysRange = 31;
			break;
		case 2:
			self.startNumOfDaysRange = 5;
			break;
		case 3:
			self.startNumOfDaysRange = 12;
			break;
		case 4:
			self.startNumOfDaysRange = 31;
			break;
		}
	};
	ResourcePerformanceCtrl.prototype.setDurationNumOfDaysRange = function(dateType) {
		var self = this;
		switch (dateType) {
		case 1:
			self.durationNumOfDaysRange = 31;
			break;
		case 2:
			self.durationNumOfDaysRange = 5;
			break;
		case 3:
			self.durationNumOfDaysRange = 12;
			break;
		case 4:
			self.durationNumOfDaysRange = 31;
			break;
		}
	};

	ResourcePerformanceCtrl.prototype.onConfirmFromAddColumn = function(res) {
		var self = this;
		var error = false;

		if (self.addColumnForm.$valid) {
			if (_sdUtilService.isEmpty(self.columnDefinition.columnTitle)) {
				error = true;
				self.addColumnForm.$error.columnName = true;
			} else {
				self.addColumnForm.$error.columnName = false;
			}
			if (self.columnDefinition.showDatePicker) {
				if (_sdUtilService.isEmpty(self.columnDefinition.startDate)
						|| _sdUtilService.isEmpty(self.columnDefinition.endDate)) {
					error = true;
					self.addColumnForm.$error.datesMandatory = true;
				} else {
					self.addColumnForm.$error.datesMandatory = false;
					if (self.columnDefinition.startDate > new Date().getTime()) {
						error = true;
						self.addColumnForm.$error.startDate = true;
					} else {
						self.addColumnForm.$error.startDate = false;
					}

					// validating the createDateTo and createDateFrom
					if (!_sdUtilService.validateDateRange(self.columnDefinition.startDate,
							self.columnDefinition.endDate)) {
						error = true;
						self.addColumnForm.$error.dateBetween = true;
					} else {
						self.addColumnForm.$error.dateBetween = false;
					}
				}
			}

			if (error) {
				// validation error in search criteria, then don't perform
				// search
				return false;
			} else {
				if (self.columnDefinition.showDatePicker) {
					self.columnDefinition.startDate = moment(self.columnDefinition.startDate).format("M/D/YYYY h:mm a");

					self.columnDefinition.endDate = moment(self.columnDefinition.endDate).format("M/D/YYYY h:mm a");
				} else {
					delete self.columnDefinition.startDate;
					delete self.columnDefinition.endDate;
				}

				self.allColumns.push(self.columnDefinition);
				var preferenceStore = this.getConfig(self.prefScope);
				preferenceStore.setValue('ipp-business-control-center.ResourcePerformance.allColumns', self.allColumns);
				preferenceStore.save();
				self.refresh();
				return true;
			}
		} else {
			return false;
		}
	};

	angular.module("bcc-ui").filter('range', function() {
		return function(input, min, max) {
			min = parseInt(min);
			max = parseInt(max);
			for (var i = min; i <= max; i++)
				input.push(i);
			return input;
		};
	});
})();
