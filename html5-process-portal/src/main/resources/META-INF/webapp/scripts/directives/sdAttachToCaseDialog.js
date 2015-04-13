/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdAttachToCaseDialog', ['$parse', '$q', 'sdUtilService', 'sdProcessInstanceService', 'sdLoggerService', 'eventBus', 'sdViewUtilService',
	                                                                    AttachToCaseDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function AttachToCaseDialogDirective($parse, $q, sdUtilService, sdProcessInstanceService, sdLoggerService, eventBus, sdViewUtilService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdAttachToCaseDialog');
		
		var SUPPORTED_NOTIFICATION_TYPES = {
				ERROR: 'error',
				WARNING: 'warning',
				INFO: 'info'
			}

		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					onConfirm: '&sdaOnConfirm',
					disabled: '=ngDisabled',
					showDialog: '=sdaShowDialog',
					skipNotification: '&sdaSkipNotification'
				},
				transclude: true,
				template: // Attach to case process Dialog
					'<span style="float: left;"' 
					+ ' sd-dialog="attachToCaseController.attachToCaseDialog"'
					+ ' sda-title="{{attachToCaseController.dialogTitle}}"'
					+ ' sda-type="confirm"'
					+ ' sda-scope="this"'
					+ ' sda-on-open="attachToCaseController.onOpenDialog(res)"'
					+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/attachToCaseDialog.html"'
					+ ' sda-on-confirm="attachToCaseController.confirm()"'
					+ ' sda-confirm-action-label="{{i18n(\'views-common-messages.views-attachToCase-button-attach\')}}"'
					+ ' sda-cancel-action-label="{{i18n(\'views-common-messages.common-cancel\')}}"'
					+ ' class="view-tool-link">'				
					+ '</span>'
					// Notification Dialog
					+ '<span style="float: left;"' 
					+ ' sd-dialog="attachToCaseController.notificationDialog"'
					+ ' sda-title="{{attachToCaseController.notificationTitle}}"'
					+ ' sda-type="custom"'
					+ ' sda-scope="this"'
					+ ' sda-on-close="attachToCaseController.onCloseNotification()"'
					+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/attachToCaseNotification.html">'
					+ '</span>',
				controller: AttachToCaseDialogController
			};
		
		/*
		 * Controller class
		 */
		function AttachToCaseDialogController($attrs, $scope, $element) {
			var self = this;

			initialize();

			/*
			 * Initialize the component
			 */
			function initialize() {
				self.skipNotification = $scope.skipNotification;
				self.onConfirm = $scope.onConfirm;
				self.onOpen = $scope.onOpen;
				
				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
					self.i18n = $scope.i18n;
				}
				
				$scope.$watch('showDialog', function(newVal, oldVal) {
					if (newVal != undefined && newVal != null && newVal != oldVal) {
						if (newVal === true) {
							self.handleAttachToCase();
						}
						
						$scope.showDialog = false;
					}
				});
				
				self.closeThisDialog = closeThisDialog;
				self.resetValues = resetValues;
				self.confirm = confirm;
				self.handleAttachToCase = handleAttachToCase;
				self.okNotification = okNotification;
				self.closeNotification = closeNotification;
				self.onCloseNotification = onCloseNotification;
				self.validate = validate;
				self.safeApply = function() {
					sdUtilService.safeApply($scope);
				};

				self.showAdvancedSelect = showAdvancedSelect;
				self.showMatchAll = showMatchAll;

				// Initialize the values
				self.resetValues();
			}
			
			/*
			 * 
			 */
			AttachToCaseDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			/*
			 * 
			 */
			AttachToCaseDialogController.prototype.showErrorMessage = function(key, def, args) {
				var msg = key;
				if (angular.isObject(key) && key.message) {
					msg = key.message;
				} else {
					if (def) {
						msg = def;
					}
					msg = self.i18n(key, def);
					
					if (args) {
						msg = sdUtilService.format(msg, args);
					}
				}

				self.resetErrorMessage();
				eventBus.emitMsg("js.error", msg);
			}

			/*
			 * 
			 */
			AttachToCaseDialogController.prototype.resetErrorMessage = function() {
				eventBus.emitMsg("js.error.reset");
			}
			
			/*
			 * 
			 */
			function resetValues() {
				
				self.attachToCase = {
					advancedSelect : false,
					matchAll : true,
					relatedProcesses : {
						totalCount : 0,
						list : [],
						hasAnyCaseInstance: false
					},
					pageSize : 6,
					attachToCaseCompleted: false,
					isCase: false,
					processOID: undefined
				};

				self.notificationMessage = {
					message: '',
				};
			}
			
			/*
			 * 
			 */
			function getProcessInstanceOIDs() {
				var oids = [];
				angular.forEach(self.processInstances, function(pi) {
					oids.push(pi.oid);
				});
				
				return oids;
			}

			/*
			 * 
			 */
			function onOpenDialog(result) {

				if (angular.isDefined(self.onOpen)) {
					self.onOpen();
				}
			}

			/*
			 * 
			 */
			function closeThisDialog(scope) {
				scope.closeThisDialog();
			}

			/*
			 * 
			 */
			function confirm(scope) {
				var deferred = $q.defer();
				
				var attachToCaseData = {};
				
				if (self.processInstanceOIDs.length > 0) {
					attachToCaseData.sourceProcessOIDs = self.processInstanceOIDs;
				}

				if (self.attachToCase.advancedSelect == true) {
					attachToCaseData.targetProcessOID = self.attachToCase.processOID;
				} else {
					if (angular.isDefined(self.attachToCase.relatedProcessDataTable.getSelection())
							&& self.attachToCase.relatedProcessDataTable.getSelection() != null) {
						attachToCaseData.targetProcessOID = self.attachToCase.relatedProcessDataTable.getSelection().oid;
					}
				}

				if (self.validate(attachToCaseData)) {
					performAttachToCase(attachToCaseData).then(function(data) {
						
						deferred.resolve();
						
						if (self.skipNotification == true) {
							okNotification();
						} else {
							openNotificationDialog(data);
						}
					}, function(result) {
						// Error occurred
						self.showErrorMessage(
								'views-common-messages.views-common-process-attachToCase-failure',
								'An error occurred while performing Attach To Case.');
					});
				}
				
				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function performAttachToCase(attachToCasePayload) {
				var deferred = $q.defer();

				sdProcessInstanceService.attachToCase(attachToCasePayload).then(function(data) {
					// Attach To Case successful
					deferred.resolve(data);

				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing Attach to case.\n Caused by: ' + result);
					deferred.reject(result);
				});

				return deferred.promise;
			}

			/*
			 * 
			 */
			function validate(attachToCaseData) {
				if (!angular.isDefined(attachToCaseData.targetProcessOID)) {
					var msgKey = 'views-common-messages.views-attachToCase-caseRequired-message';
					if (self.attachToCase.isCase) {
						msgKey = 'views-common-messages.views-attachToCase-processRequired-message';
					}
					self.showErrorMessage(msgKey, 'Please provide a case/process oid.');
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function handleAttachToCase() {
				self.processInstances = parseAttribute($scope.$parent, $attrs.sdaProcessInstances);
				if (self.processInstances.length < 1) {
					openNotificationDialog({ message: self.i18n(
							'views-common-messages.views-attachToCase-selectProcessToAttachToCase',
							'Please select atleast one Process!')});
					return;
				}
				self.resetValues();
				
				if (validateSourceProcesses()) {
					self.processInstanceOIDs = getProcessInstanceOIDs();
					
					var process = self.processInstances[0];
					self.attachToCase.isCase = process.caseInstance;
					
					self.dialogTitle = getDialogTitle();
					
					openAttachToCaseDialog();
				}
			}
			

			/*
			 * 
			 */
			function getDialogTitle() {
				if (!self.attachToCase.isCase) {
					if (self.processInstances.length == 1) {
						return sdUtilService.format( self.i18n('views-common-messages.views-attachToCase-scope_process-title'),
								[ self.processInstances[0].processName ]);
					} else {
						return self.i18n('views-common-messages.views-attachToCase-title');
					}
				} else {
					return sdUtilService.format( self.i18n('views-common-messages.views-attachToCase-scope_case-title'),
							[ self.processInstances[0].processName ]);
				}
			}
			
			/*
			 * 
			 */
			function validateSourceProcesses() {
				if (!isRootProcessInstances()) {
					openNotificationDialog({message: self.i18n(
							'views-common-messages.views-attachToCase-nonRootProcessSelectedToCreateCase',
							'Only Root Process Instance(s) can be attached to a Case.')});

					return false;
				} else	if (isMixProcessInstances()) {
					openNotificationDialog({message: self.i18n(
							'views-common-messages.views-attachToCase-selectCaseOrProcess-message',
							'Please select either Case or Process(es) to Attach to Case.')});

					return false;
				} else if (isCaseProcessInstances() && self.processInstances.length > 1) {
					
					openNotificationDialog({ message: self.i18n(
							'views-common-messages.views-attachToCase-caseProcess-notSelectMultipleCases',
							'Please select only one Case to Attach To Case.')});
					
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function isRootProcessInstances() {
				var isRootPIs = true;
				angular.forEach(self.processInstances, function(pi) {
					if (pi.oid != pi.processInstanceRootOID) {
						isRootPIs = false;
					}
				})

				return isRootPIs;
			}

			/*
			 * 
			 */
			function isCaseProcessInstances() {
				var isCasePIs = true;
				angular.forEach(self.processInstances, function(pi) {
					if (!pi.caseInstance) {
						isCasePIs = false;
					}
				})

				return isCasePIs;
			}

			/*
			 * 
			 */
			function isMixProcessInstances() {
				var isMixPIs = false;

				if (self.processInstances.length > 1) {
					var containCase = false;
					var containNonCase = false;

					angular.forEach(self.processInstances, function(pi) {
						if (pi.caseInstance) {
							containCase = true;
						} else {
							containNonCase = true;
						}
						if (containCase && containNonCase) {
							isMixPIs = true;
						}
					})
				}
				return isMixPIs;
			}
			
			/*
			 * 
			 */
			function openNotificationDialog(result) {
				// Show notification dialog for Attach to case

				self.notificationMessage = result;
				if (!self.notificationMessage.success) {
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.ERROR);
				} else {
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.INFO);
					self.attachToCase.attachToCaseCompleted = true;
				}
				
				self.notificationDialog.open();
			}
			
			/*
			 * 
			 */
			function openAttachToCaseDialog() {
				loadRelatedProcesses().then(function() {
					self.attachToCaseDialog.open();
				}, function() {
					self.attachToCaseDialog.open();
				});
			}

			/*
			 * 
			 */
			function loadRelatedProcesses() {
				var deferred = $q.defer();

				sdProcessInstanceService.getRelatedProcesses(self.processInstanceOIDs, !self.attachToCase.matchAll,
						!self.attachToCase.isCase).then(function(data) {
					// getRelatedProcesses successful
					self.attachToCase.relatedProcesses = {};
					self.attachToCase.relatedProcesses.list = data;
					self.attachToCase.relatedProcesses.totalCount = data.length;
					
					self.attachToCase.relatedProcesses.hasAnyCaseInstance = false;
					angular.forEach(self.attachToCase.relatedProcesses.list, function(relatedProcess) {
						if (relatedProcess.caseInstance == true) {
							self.attachToCase.relatedProcesses.hasAnyCaseInstance = true;
						}
					});

					if (angular.isDefined(self.attachToCase.relatedProcessDataTable)) {
						self.attachToCase.relatedProcessDataTable.refresh();
						self.safeApply();
					}
					deferred.resolve(data);
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing "Get Related Processes".\n Caused by: ' + result);
					deferred.reject(result);
				});

				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function showAdvancedSelect(val) {
				self.attachToCase.advancedSelect = val;
			}

			/*
			 * 
			 */
			function showMatchAll(val) {
				self.attachToCase.matchAll = val;
				loadRelatedProcesses();
			}
			
			/*
			 * 
			 */
			function okNotification() {
				// Attach to case finished
				self.notificationDialog.confirm();
				if (angular.isDefined(self.onConfirm)) {
					var callback = self.onConfirm();
					if (angular.isFunction(callback)) {
						var caseOID;
						if (!self.attachToCase.isCase) {
							caseOID = self.attachToCase.processOID;
						} else {
							caseOID = self.processInstanceOIDs[0];
						}

						callback(self.attachToCase.attachToCaseCompleted, caseOID);
					}
				}
			}

			/*
			 * 
			 */
			function closeNotification() {
				self.notificationDialog.close();
			}
			
			/*
			 * 
			 */
			function onCloseNotification() {
				if (angular.isDefined(self.onConfirm)) {
					var callback = self.onConfirm();
					if (angular.isFunction(callback)) {
						callback(self.attachToCase.attachToCaseCompleted);
					}
				}
			}
			
			/*
			 * 
			 */
			function parseAttribute(scope, attr) {
				try {
					var evalAttr = $parse(attr)(scope);
					if (!angular.isDefined(evalAttr)) {
						evalAttr = attr;
					}
					return evalAttr;
				} catch (err) {
					return attr;
				}
			}


			$scope.attachToCaseController = self;
		}
		
		return directiveDefObject;
	}
})();
