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
			['sdLoggerService', 'sdCriticalityConfigService', 'sdDialogService', 'sgI18nService', 'sdUtilService', '$scope', '$sce', Controller]);

	/*
	 * 
	 */
	function Controller(sdLoggerService, sdCriticalityConfigService, sdDialogService, sgI18nService, sdUtilService, $scope, $sce) {
		
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
			
			this.exportAnchor = document.createElement("a");
		};
		
		/*
		 * 
		 */
		Controller.prototype.resetValues = function() {
			this.uploadedFiles = undefined;
			this.showImportDlg = false;
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
				trace.error('Error occured while fetching Criticality Configuration : ', error);
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
					var options = {
						title : sgI18nService.translate('portal-common-messages.common-info')
					};
					sdDialogService.alert($scope, 
							sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-save-success-dialog'),
							options);
					self.resetValues();
					self.fetchCriticalityConfig();
				}, function(error) {
					trace.error('Error occured while saving Criticality Configuration : ', error);
					var options = {
							title : sgI18nService.translate('portal-common-messages.common-error')
						};
					// show error to the user
					sdDialogService.error($scope, 
							sgI18nService.translate('admin-portal-messages.views-criticalityConf-criticality-save-failure-dialog'),
							options);
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
										.translate('admin-portal-messages.views-criticalityConf-criticality-validation-values-oursideRange-message'),
						[ '[' + RANGE_LOWER_LIMIT + ' - ' + RANGE_HIGHER_LIMIT + ']' ]));
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
			
			return $sce.trustAsHtml(errorMessages);
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
		Controller.prototype.showImportDialog = function() {
			this.showImportDlg = true;
		};
		
		/*
		 * 
		 */
		Controller.prototype.importCriticalities = function() {
			if (!angular.isDefined(this.uploadedFiles) || this.uploadedFiles.length == 0) {
				trace.log('No file was found to be uploaded. Please upload a file and try again.');
				return;
			}
			var self = this;
			sdCriticalityConfigService.importCriticalities(this.uploadedFiles[0]).then(function(result) {
				self.resetValues();
				self.fetchCriticalityConfig();
			}, function(error) {
				self.uploadedFiles = undefined;
				trace.error('Error occured while importing Criticality Configuration : ', error);
				var options = {
						title : sgI18nService.translate('portal-common-messages.common-error')
				};
				// show error to the user
				sdDialogService.error($scope, error.data, options);
			});
		};
		
		/*
		 * 
		 */
		Controller.prototype.exportCriticalities = function() {
			var self = this;
			sdCriticalityConfigService.exportCriticalities().then(function(result) {
				var fileName = 'CriticalityCategories.zip';
				downloadDataAsFile(self.exportAnchor, fileName, result.data)
			}, function(error) {
				trace.error('Error occured while exporting Criticality Configuration : ', error);
			});
		};
		
		/*
		 * 
		 */
		function downloadDataAsFile(exportAnchor, fileName, data) {
			var byteArray = new Uint8Array(data);
			var octetStreamMime = 'application/octet-stream; charset=utf-8';
			var urlCreator = window.URL || window.webkitURL || window.mozURL || window.msURL;
			if (urlCreator) {
				if ("download" in exportAnchor) {
					var blob = new Blob([ byteArray ], {
						type : octetStreamMime
					});
					var url = urlCreator.createObjectURL(blob);
					exportAnchor.setAttribute("href", url);
					exportAnchor.setAttribute("download", fileName);
					var event = document.createEvent('MouseEvents');
					event.initMouseEvent('click', true, true, window, 1, 0, 0, 0, 0, false, false, false, false, 0,
							null);
					exportAnchor.dispatchEvent(event);
				} else {
					var blob = new Blob([ byteArray ], {
						type : octetStreamMime
					});
					var url = urlCreator.createObjectURL(blob);
					window.location = url;
				}
			}
		}
		
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
				existingList.push({ editMode: true, count: 1});

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