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
 * @author Nikhil.Gahlot
 */

(function() {
	'use strict';

	angular.module('admin-ui').controller('sdCriticalityConfigCtrl',
			['sdLoggerService', 'sdCriticalityConfigService', 'sdDialogService', 'sgI18nService', 'sdUtilService', '$scope', Controller]);

	/*
	 * 
	 */
	function Controller(sdLoggerService, sdCriticalityConfigService, sdDialogService, sgI18nService, sdUtilService, $scope) {
		
		var trace = sdLoggerService.getLogger('admin-ui.sdCriticalityConfigCtrl');
		
		var RANGE_LOWER_LIMIT = 0;
		var RANGE_HIGHER_LIMIT = 1000;

		/*
		 * 
		 */
		Controller.prototype.initialize = function() {
			this.resetValues();
			this.dataTable = null; // This will be set to underline data
			this.selection = null;
			this.fetchCriticalityConfig();
		};
		
		/*
		 * 
		 */
		Controller.prototype.resetValues = function() {
			this.errorMessages = [];
			this.criticalityConfig = {
				criticalities : {
					list : [],
					totalCount : 0
				},
				defaultCriticalityFormula : '',
				activityCreation : '',
				activitySuspendAndSave : '',
				processPriorityChange : ''
			};
		};
		
		/**
		 * 
		 */
		Controller.prototype.fetchCriticalityConfig = function() {
			var self = this;

			sdCriticalityConfigService.getCriticalityConfig().then(function(result) {
				self.criticalityConfig.criticalities = {};
				
				self.criticalityConfig = angular.extend({}, result);
				self.criticalityConfig.criticalities.list = result.criticalities;
				self.criticalityConfig.criticalities.totalCount = result.criticalities.length;
				
				self.refresh();
			}, function(error) {
				trace.error('Error occured while fetching Critical Configuration : ', error);
				self.resetValues();
				// TODO show error to the user
			});
		}
		
		/*
		 * 
		 */
		Controller.prototype.refresh = function() {
			if (angular.isDefined(this.dataTable)) {
				this.dataTable.refresh(true);
			}
			this.resetErrorMessages();
		};
		
		/*
		 * 
		 */
		Controller.prototype.save = function() {
			var self = this;
			
			var payload = {};
			payload = angular.extend({}, self.criticalityConfig);
			payload.criticalities = self.dataTable.getData();
			
			if (self.validate(payload.criticalities)) {
				sdCriticalityConfigService.saveCriticalityConfig(payload).then(function(result) {
					self.criticalityConfig = result;
					sdDialogService.alert($scope, 
							sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-save-success-dialog'),
							sgI18nService.translate('portal-common-messages.common-info'));
					self.fetchCriticalityConfig();
				}, function(error) {
					trace.error('Error occured while saving Critical Configuration : ', error);
					// show error to the user
					sdDialogService.error($scope, 
							sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-save-failure-dialog'),
							sgI18nService.translate('portal-common-messages.common-error'));
				});
			}
		};
		
		/*
		 * 
		 */
		Controller.prototype.validate = function(criticalities) {
			var self = this;
			
			self.resetErrorMessages();
			
			var uniqueLabels = new Array();
			var uniqueRangeList = new Array();
			angular.forEach(criticalities, function(data) {
				if (!angular.isDefined(data.color)) {
					var mesg = sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-validation-iconNotSelected-message'); 
					self.addToErrorMessages(mesg);
				}
				if (!angular.isDefined(data.label)) {
					var mesg = sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-validation-labelEmpty-message'); 
					self.addToErrorMessages(mesg);
				}
				
				self.processValueRangeValidations(uniqueRangeList, data.rangeFrom, data.rangeTo);
				
				if (uniqueLabels.indexOf(data.label) == -1) {
					uniqueLabels.push(data.label);
				}
			});
			
			if (uniqueRangeList.length < (RANGE_HIGHER_LIMIT - RANGE_LOWER_LIMIT + 1)) {
				self.addToErrorMessages(sgI18nService
								.translate('admin-portal-messages.views-criticalityConf-criticality-validation-values-missing-message'));
			}

			if (uniqueLabels.length < criticalities.length) {
				self.addToErrorMessages(sgI18nService
								.translate('admin-portal-messages.views-criticalityConf-criticality-validation-labelNotUnique-message'));

			}
			return this.errorMessages.length == 0;
		};
		
		/*
		 * 
		 */
		Controller.prototype.processValueRangeValidations = function(uniqueRangeList, minRange, maxRange) {
			var self = this;
			
			if (minRange >= RANGE_LOWER_LIMIT && maxRange >= RANGE_LOWER_LIMIT
		            && minRange <= RANGE_HIGHER_LIMIT && maxRange <= RANGE_HIGHER_LIMIT)
		      {
		         if (minRange <= maxRange)
		         {
		            for (var i = minRange; i <= maxRange; i++)
		            {
		               if (uniqueRangeList.indexOf(i) != -1)
		               {
		            	   self.addToErrorMessages(sgI18nService
		            			   .translate('admin-portal-messages.views-criticalityConf-criticality-validation-values-overlap-message'));
		               } else {
		            	   uniqueRangeList.push(i);
		               }
		            }
		         }
		         else
		         {
		        	 self.addToErrorMessages(sgI18nService
	            			   .translate('admin-portal-messages.views-criticalityConf-criticality-validation-minMaxReverse-message'));
		         }
		      }
		      else
		      {
				self
				.addToErrorMessages(
						sdUtilService
								.format(sgI18nService
										.translate('admin-portal-messages.views.criticalityConf.criticality.validation.values.oursideRange.message')),
						[ '[' + RANGE_LOWER_LIMIT + ' - ' + RANGE_HIGHER_LIMIT + ']' ]);
		      }
		};
		
		/*
		 * 
		 */
		Controller.prototype.addToErrorMessages = function(mesg) {
			if (this.errorMessages.indexOf(mesg) == -1) {
				this.errorMessages.push(mesg);
			}
		};
		
		/*
		 * 
		 */
		Controller.prototype.resetErrorMessages = function(mesg) {
			this.errorMessages = [];
		};
		
		
		/*
		 * 
		 */
		Controller.prototype.getValidationMsg = function(mesg) {
			if (this.errorMessages.length == 0) {
				return '';
			}
			var errorMessages = '';
			angular.forEach(this.errorMessages, function(mesg) {
				errorMessages = errorMessages.concat("- ");
				errorMessages = errorMessages.concat(mesg);
				errorMessages = errorMessages.concat("<br/>");
			});
			
			return errorMessages;
		};

		/*
		 * 
		 */
		Controller.prototype.removeRows = function() {
			var self = this;
			angular.forEach(self.dataTable.getSelection(), function(selected) {
				selected.remove = true;
			});
			
			var existingList = self.dataTable.getData();
			var remainingList = new Array();
			angular.forEach(existingList, function(item, index) {
				if (item.remove != true) {
					remainingList.push(item);
				}
			});
			
			self.criticalityConfig.criticalities.list = remainingList;
			self.criticalityConfig.criticalities.totalCount = remainingList.length;
			self.refresh();
		};
		
		/*
		 * 
		 */
		Controller.prototype.importVariables = function() {
			// TDOD
			sdDialogService.alert($scope, 'To be implemented!', 'Alert');
		};
		
		/*
		 * 
		 */
		Controller.prototype.exportVariables = function() {
			// TDOD
			sdDialogService.alert($scope, 'To be implemented!', 'Alert');
		};
		
		/*
		 * 
		 */
		Controller.prototype.editRows = function() {
			angular.forEach(this.dataTable.getSelection(), function(selected) {
				selected.editMode = true;
			});
		};
		
		/*
		 * 
		 */
		Controller.prototype.addRow = function() {
			var existingList = this.dataTable.getData();
			if (angular.isDefined(existingList)) {
				existingList.unshift({ editMode: true});

				this.criticalityConfig.criticalities.list = existingList;
				this.criticalityConfig.criticalities.totalCount = existingList.length;
				this.refresh();
			}
		};
		
		/*
		 * 
		 */
		Controller.prototype.rowEditable = function(row) {
			return row.editMode === true;
		};
		
		this.initialize();
	}
})();