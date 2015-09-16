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
 * @author Johnson.Quadras
 */

(function() {
	'use strict';
	angular.module('admin-ui').controller( 'sdQaManagementViewCtrl',
			[ '$scope', 'sdQualityAssuranceService', '$q', 'sdDialogService', 'sgI18nService', 'sdLoggerService', 'sdDataTableHelperService', '$filter', Controller ]);

	var _sdQualityAssuranceService = null;
	var _q = null;
	var trace = null;
	var _sdDataTableHelperService = null;
	var _filter = null;
	/*
	 * 
	 */
	function Controller($scope, sdQualityAssuranceService, $q, sdDialogService, sgI18nService, sdLoggerService, sdDataTableHelperService, $filter) {
		var self = this;

		_sdQualityAssuranceService = sdQualityAssuranceService;
		_q = $q;
		trace = sdLoggerService.getLogger('admin-ui.sdQaManagementViewCtrl');
		_sdDataTableHelperService =  sdDataTableHelperService
		_filter = $filter;

		self.isActivitiesPanelExpanded = true;
		self.isDepartmentsPanelExpanded = true;
		self.showObsoleteActivities = false;
		self.activityTable = null;
		self.departmentTable = null;
		self.activities = {
				list : [],
				totalCount : 0
		};
		self.departments = {
				list : [],
				totalCount : 0
		};
		self.searchDepartments = false;
		self.getQaActivities();

		self.activityTableReady = false;

		self.showInValidPercentageError = function() {
			showInValidPercentageError($scope, sdDialogService, sgI18nService);
		}
	}
	/**
	 * 
	 * @param open
	 */
	Controller.prototype.expandActivitiesPanel = function(open) {
		var self = this;
		self.isActivitiesPanelExpanded = open;
	};

	/**
	 * 
	 * @param open
	 */
	Controller.prototype.expandDepartmentsPanel = function(open) {
		var self = this;
		self.isDepartmentsPanelExpanded = open;
	};

	/**
	 * 
	 */
	Controller.prototype.saveChanges = function() {
		var self = this;

		var activityData = self.activityTable.getData();

		var updatedActivityQaPercentages = [];
		var updatedDepartmentQaPercentages = [];

		angular.forEach(activityData, function(data) {
			if (angular.isDefined(data.qaPercentage)) {

				if (angular.isDefined(data.originalQaPercentage)) {

					if (!angular.equals(data.originalQaPercentage, data.qaPercentage)) {
						updatedActivityQaPercentages.push(getActivityObj(data));
					}

				} else {
					updatedActivityQaPercentages.push(getActivityObj(data));
				}
			}
		});

		if (self.departmentTable != null && self.departments.totalCount > 0) {

			if (self.activityTable.getSelection().length < 1) {
				return;
			}

			var selectedActivity = self.selectedActivity;
			var departmentData = self.departmentTable.getData();

			angular.forEach(departmentData, function(data) {
				if (angular.isDefined(data.qaPercentage)) {

					if (angular.isDefined(data.originalQaPercentage)) {

						if (!angular.equals(data.originalQaPercentage, data.qaPercentage)) {

							updatedDepartmentQaPercentages.push(getDepartmentObj(selectedActivity, data));
						}

					} else {

						updatedDepartmentQaPercentages.push(getDepartmentObj(selectedActivity, data));
					}
				}
			});
		}

		if (!validatePercentages(updatedActivityQaPercentages, updatedDepartmentQaPercentages)) {
			self.showInValidPercentageError();
			return;
		}

		if (updatedActivityQaPercentages.length > 0 || updatedDepartmentQaPercentages.length > 0) {
			self.updateQaProbabilities(updatedActivityQaPercentages, updatedDepartmentQaPercentages)
		} else {
			self.refreshTables();
		}
	};
	/**
	 * 
	 */
	Controller.prototype.refreshTables = function() {
		var self = this;
		
		_sdQualityAssuranceService.getQaActivities(self.showObsoleteActivities).then(function(result) {
			angular.forEach(result, function(data) {
				if( data.qaPercentage ){
					data.qaPercentage = parseInt(data.qaPercentage);
				}
				data.originalQaPercentage = data.qaPercentage;
			});
			self.activities.totalCount = result.length;
			self.activities.list = result;
		});
		
		_sdQualityAssuranceService.getQaDepartments(self.selectedActivity.processQualifiedId, self.selectedActivity.activityQualifiedId).then(
				function(result) {
					angular.forEach(result, function(data) {
						if( data.qaPercentage ){
							data.qaPercentage = parseInt(data.qaPercentage);
						}
						data.originalQaPercentage = data.qaPercentage;
					});
					self.departments.totalCount = result.length;
					self.departments.list = result;
				});

		var activityData = self.activityTable.getData();
		angular.forEach(activityData, function(data) {
			data.isEditMode = false;
		});

		if (self.departmentTable != null && self.departments.totalCount > 0) {
			var departmentData = self.departmentTable.getData();
			angular.forEach(departmentData, function(data) {
				data.isEditMode = false;
			});
		}
		

	};
	/**
	 * 
	 */
	Controller.prototype.updateQaProbabilities = function(updatedActivityQaPercentages, updatedDepartmentQaPercentages) {
		var self = this;
		trace.debug("Updating probabilities : ",updatedActivityQaPercentages,updatedDepartmentQaPercentages);
		_sdQualityAssuranceService.updateQaProbabilities(updatedActivityQaPercentages, updatedDepartmentQaPercentages)
		.then(function(successResult) {
			trace.info("Probabilities updated sucessfully.");
			self.refreshTables();
		}, function(error) {
			self.activityTableReady = false;
			trace.error("Error Occured when saving the probabilities : ", error);
			self.departments = {};
			self.getQaActivities();
		});
	};
	/**
	 * 
	 */
	Controller.prototype.showHideObsoleteActivities = function() {
		var self = this;
		self.showObsoleteActivities = !self.showObsoleteActivities;
		trace.debug("Show Obsoloete Activities : " + self.showObsoleteActivities);
		self.departments = {
				list : [],
				totalCount : 0
		};
		self.getQaActivities();

	};

	/**
	 * 
	 */
	Controller.prototype.getQaActivities = function( ) {
		var self = this;
		self.activityTableReady = false;
		trace.debug("Fetching QA activities with obsolete activies : ", self.showObsoleteActivities);
		_sdQualityAssuranceService.getQaActivities(self.showObsoleteActivities).then(function(result) {
			angular.forEach(result, function(data) {
				if( data.qaPercentage ){
					data.qaPercentage = parseInt(data.qaPercentage);
				}
				data.originalQaPercentage = data.qaPercentage;
			});
			self.activities.totalCount = result.length;
			self.activities.list = result;
			self.activityTableReady = true;
		});
	};
	/**
	 * 
	 */
	Controller.prototype.fetchQaActivities = function(options) {
		var self = this;
		var deferred = _q.defer();
		var result = {
				list : self.activities.list,
				totalCount : self.activities.totalCount
		}

		deferred.resolve(result);
		return deferred.promise;
	};


	/**
	 */
	Controller.prototype.applyFilter = function(options, result) {
		if(options.filters.modelName && options.filters.modelName.textSearch !='' ){
			result.list = _filter('filter')( result.list, {"modelName" : options.filters.modelName.textSearch},false);
		}

		if(options.filters.processName && options.filters.processName.textSearch !='' ){
			result.list = _filter('filter')( result.list, {"processName" : options.filters.processName.textSearch},false);
		}

		if(options.filters.activityName && options.filters.activityName.textSearch !='' ){
			result.list = _filter('filter')( result.list, {"activityName" : options.filters.activityName.textSearch},false);
		}

		if(options.filters.defaultPerformer && options.filters.defaultPerformer.textSearch !='' ){
			result.list = _filter('filter')( result.list, {"defaultPerformer" : options.filters.defaultPerformer.textSearch},false);
		}

		if(options.filters.qaPercentage && options.filters.qaPercentage.textSearch !='' ){
			result.list = _filter('filter')( result.list, {"qaPercentage" : options.filters.qaPercentage.textSearch},false);
		}

		return result.list;
	}

	/**
	 * 
	 */
	Controller.prototype.fetchQaDepartments = function(options) {
		var self = this;
		var deferred = _q.defer();

		var result = {
				list : self.departments.list,
				totalCount : self.departments.totalCount
		}

		deferred.resolve(result);

		return deferred.promise;
	};
	/**
	 * 
	 */
	Controller.prototype.onSelect = function(info) {
		var self = this;
		self.departments = {};

		var rowData = self.activityTable.getSelection();

		if (!rowData) {
			self.departments.totalCount = 0;
			self.departments.list = [];

		} else {
			self.selectedActivity = {activityQualifiedId : rowData.activityQualifiedId, processQualifiedId : rowData.processQualifiedId};

			_sdQualityAssuranceService.getQaDepartments(self.selectedActivity.processQualifiedId, self.selectedActivity.activityQualifiedId).then(
					function(result) {
						angular.forEach(result, function(data) {
							if( data.qaPercentage ){
								data.qaPercentage = parseInt(data.qaPercentage);
							}
							data.originalQaPercentage = data.qaPercentage;
						});
						self.departments.totalCount = result.length;
						self.departments.list = result;
						self.searchDepartments = true;
					});
		}

	};
	/**
	 * 
	 */
	Controller.prototype.editActivityQa = function() {
		var self = this;
		var activtySelection = self.activityTable.getSelection();
		activtySelection.isEditMode = true;
	};
	/**
	 * 
	 */
	Controller.prototype.editDepartmentQa = function() {
		var self = this;
		var departmentSelection = self.departmentTable.getSelection();
		departmentSelection.isEditMode = true;
	};
	/**
	 * 
	 */
	function getActivityObj(data) {
		return {
			"processQualifiedId" : data.processQualifiedId,
			"activityQualifiedId" : data.activityQualifiedId,
			"qaPercentage" : data.qaPercentage
		};
	}
	/**
	 * 
	 */
	function getDepartmentObj(selectedActivity, dept) {
		dept["activityQualifiedId"] = selectedActivity.activityQualifiedId;
		dept["processQualifiedId"] = selectedActivity.processQualifiedId;
		return dept;
	}
	/**
	 * 
	 */
	function validatePercentages(updatedActivityQaPercentages, updatedDepartmentQaPercentages) {
		var isActivityValid = true;
		var isDepartmentValid = true;
		if (updatedActivityQaPercentages.length > 0) {
			isActivityValid = isQaPercentageValid(updatedActivityQaPercentages);
		}
		if (updatedDepartmentQaPercentages.length > 0) {
			isDepartmentValid = isQaPercentageValid(updatedDepartmentQaPercentages);
		}
		return isActivityValid && isDepartmentValid;
	}
	/**
	 * 
	 */
	function isQaPercentageValid(values) {
		var isValid = false;
		var numbersOnly = /^\d+$/;
		angular.forEach(values, function(item) {
			if (item.qaPercentage == '') {
				isValid = true;
			} else if (item.qaPercentage <= 100 && item.qaPercentage >= 0 && numbersOnly.test(item.qaPercentage)) {
				isValid = true;
			}
		});
		return isValid;
	}
	/**
	 * 
	 */
	function showInValidPercentageError($scope, sdDialogService, sgI18nService) {
		var options = { title : sgI18nService.translate('views-common-messages.common-error', 'Error')};
		var message = sgI18nService.translate('admin-portal-messages.views-qaManagementView-qaError',
		'Invalid percentage');
		sdDialogService.error($scope, message, options)
	}

})();