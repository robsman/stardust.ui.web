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

	angular.module('bpm-common').directive('sdSwitchProcessDialog', ['$parse', '$q', 'sdUtilService', 'sdProcessInstanceService', 'sdLoggerService', 'eventBus', 'sdViewUtilService',
	                                                                    SwitchProcessDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function SwitchProcessDialogDirective($parse, $q, sdUtilService, sdProcessInstanceService, sdLoggerService, eventBus, sdViewUtilService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdSwitchProcessDialog');
		
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
					showDialog: '=sdaShowDialog'
				},
				transclude: true,
				template: // Switch process Dialog
					'<span style="float: left;"' 
					+ ' sd-dialog="switchProcessController.switchProcessDialog"'
					+ ' sda-title="{{switchProcessController.dialogTitle}}"'
					+ ' sda-type="confirm"'
					+ ' sda-scope="this"'
					+ ' sda-on-open="switchProcessController.onOpenDialog(res)"'
					+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/switchProcessDialog.html"'
					+ ' sda-on-confirm="switchProcessController.confirm()"'
					+ ' sda-confirm-action-label="{{i18n(\'views-common-messages.delegation-applyButton-text\')}}"'
					+ ' sda-cancel-action-label="{{i18n(\'views-common-messages.common-cancel\')}}"'
					+ ' class="view-tool-link">'				
					+ '</span>'
					// Notification Dialog
					+ '<span style="float: left;"' 
					+ ' sd-dialog="switchProcessController.notificationDialog"'
					+ ' sda-title="{{switchProcessController.notificationTitle}}"'
					+ ' sda-type="custom"'
					+ ' sda-scope="this"'
					+ ' sda-on-close="switchProcessController.onCloseNotification()"'
					+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/switchProcessNotification.html">'
					+ '</span>',
				controller: SwitchProcessDialogController
			};
		
		/*
		 * Controller class
		 */
		function SwitchProcessDialogController($attrs, $scope, $element) {
			var self = this;

			initialize();

			/*
			 * Initialize the component
			 */
			function initialize() {
				self.onConfirm = $scope.onConfirm;
				self.onOpen = $scope.onOpen;
				
				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
					self.i18n = $scope.i18n;
				}
				
				$scope.$watch('showDialog', function(newVal, oldVal) {
					if (newVal != undefined && newVal != null && newVal != oldVal) {
						if (newVal === true) {
							self.handleSwitchProcess();
						}
						
						$scope.showDialog = false;
					}
				});
				
				self.closeThisDialog = closeThisDialog;
				self.resetValues = resetValues;
				self.confirm = confirm;
				self.handleSwitchProcess = handleSwitchProcess;
				self.okNotification = okNotification;
				self.closeNotification = closeNotification;
				self.onCloseNotification = onCloseNotification;
				self.validateSwitch = validateSwitch;
				self.safeApply = function() {
					sdUtilService.safeApply($scope);
				};

				// Initialize the values
				self.resetValues();
			}
			
			/*
			 * 
			 */
			SwitchProcessDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			/*
			 * 
			 */
			SwitchProcessDialogController.prototype.showErrorMessage = function(key, def, args) {
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
			SwitchProcessDialogController.prototype.resetErrorMessage = function() {
				eventBus.emitMsg("js.error.reset");
			}
			
			/*
			 * 
			 */
			function resetValues() {
				
				self.switchProcess = {
					spawnableProcesses : [],
					linkComment : '',
					switchCompleted: false,
					selectedProcess: undefined,
					switchProcessDialogMsg : self
					.i18n('views-common-messages.views-switchProcessDialog-switchProcessmessage')
				};

				self.abortNotification = {
					totalCount : 0,
					list : []
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
				
				var abortData = {
					linkComment : self.switchProcess.linkComment
				};
				
				abortData.processInstaceOIDs = self.processInstanceOIDs;
				if (angular.isDefined(self.switchProcess.selectedProcess)) {
					abortData.processId = self.switchProcess.selectedProcess.qualifiedId;
				}

				if (self.validateSwitch(abortData)) {
					performSwitch(abortData).then(function(data) {
						self.switchProcess.switchCompleted = true;

						openNotificationDialog(data);
						deferred.resolve();
					}, function(result) {
						// Error occurred
						var message = 'views-common-messages.views-common-process-abortProcess-failureMsg2';
						if (result.message) {
							message = result;
						}
						self.showErrorMessage(
								message,
								'An error occurred while performing abort & start.',
								[ abortData.sourceProcessOID ]);
					});
				}
				
				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function validateSwitch(abortData) {
				if (!angular.isDefined(abortData.processId)) {
					self.showErrorMessage(
							'views-common-messages.views-spawnProcessDialog-spawnedProcess-errorMsg-emptyValue',
							'Please select a process.');
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function performSwitch(abortPayload) {
				var deferred = $q.defer();

				sdProcessInstanceService.switchProcess(abortPayload).then(function(data) {
					// abort & start successful
					deferred.resolve(data);

				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing abort & start.\n Caused by: ' + result);
					deferred.reject(result);
				});

				return deferred.promise;
			}

			/*
			 * 
			 */
			function handleSwitchProcess() {
				self.processInstances = parseAttribute($scope.$parent, $attrs.sdaProcessInstances);
				if (self.processInstances.length < 1) {
					openNotificationDialog(self.i18n(
							'views-common-messages.views-switchProcessDialog-viewSwitch-selectProcessToSwitch',
							'Please select atleast one Process to switch!'));
					return;
				}
				self.resetValues();
				
				self.processInstanceOIDs = getProcessInstanceOIDs();
				
				self.dialogTitle = sdUtilService.format(self.i18n('views-common-messages.views-switchProcessDialog-title'));
				
				checkIfProcessesAbortable('abortandstart').then(function(data) {
					if (data.length > 0) {
						// Un-abortable condition found
						openNotificationDialog(data);
					} else {
						// Can proceed to abort and start
						openSwitchProcessDialog();
					}
				}, function(result) {
					// Error occurred
				})
			}
			
			/*
			 * 
			 */
			function openNotificationDialog(result) {
				// Show notification dialog for abort & start
				if (angular.isArray(result)) {
					self.abortNotification = {
						list : result,
						totalCount : result.length
					};
					
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ getNotificationType(result));

					if (angular.isDefined(self.abortNotificationDataTable)) {
						self.abortNotificationDataTable.refresh();
						self.safeApply();
					}
				} else {
					// A possible error message
					self.abortNotification = result;

					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.ERROR);
				}

				self.notificationDialog.open();
			}
			
			/*
			 * 
			 */
			function openSwitchProcessDialog() {
				fetchSpawnableProcesses().then(function(process) {
					self.switchProcess.spawnableProcesses = process;
					self.switchProcessDialog.open();
				}, function() {
					openNotificationDialog(error);
				});
			}

			/*
			 * 
			 */
			function checkIfProcessesAbortable(abortType) {
				var deferred = $q.defer();

				sdProcessInstanceService
						.checkIfProcessesAbortable(self.processInstanceOIDs, abortType)
						.then(
								function(data) {
									// checkIfProcessesAbortable successful
									deferred.resolve(data);
								},
								function(result) {
									// Error occurred
									trace
											.log('An error occurred while performing "Check If Processes Abortable".\n Caused by: '
													+ result);
									deferred.reject(result);
								});

				return deferred.promise;
			}

			/*
			 * 
			 */
			function fetchSpawnableProcesses() {
				var deferred = $q.defer();

				sdProcessInstanceService.getSpawnableProcesses(self.processInstanceOIDs).then(function(data) {
					deferred.resolve(data);

					self.safeApply();
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while fetching Spawnable Processes.\n Caused by: ' + result);

					deferred.reject(result);
				});

				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function okNotification(scope) {
				self.notificationDialog.confirm();
				
				if (self.switchProcess.switchCompleted) {
					// Switch finished
					var oids = [];
					angular.forEach(self.abortNotification.list, function(abortData) {
						oids.push(abortData.targetProcess.oid);
					});
					
					if (angular.isDefined(self.onConfirm)) {
						self.onConfirm()(oids);
					}
				} else {
					// Proceed to perform abort & start
					openSwitchProcessDialog();
				}
			}
			
			/*
			 * 
			 */
			function getNotificationType(data) {
				var type = SUPPORTED_NOTIFICATION_TYPES.ERROR;
				if (self.switchProcess.switchCompleted === true) {
					type = SUPPORTED_NOTIFICATION_TYPES.INFO;
				} else {
					if (data.length === self.processInstances.length) {
						type = SUPPORTED_NOTIFICATION_TYPES.ERROR;
					} else if (data.length < self.processInstances.length) {
						type = SUPPORTED_NOTIFICATION_TYPES.WARNING;
					}
				}
				return type;
			}

			/*
			 * 
			 */
			function closeNotification(scope) {
				onCloseNotification();
				self.notificationDialog.close();
			}
			
			/*
			 * 
			 */
			function onCloseNotification() {
				if (angular.isDefined(self.onConfirm)) {
					self.onConfirm()();
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


			$scope.switchProcessController = self;
		}
		
		return directiveDefObject;
	}
})();
