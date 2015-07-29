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

	angular.module('bpm-common').directive('sdJoinProcessDialog', ['$parse', '$q', 'sdUtilService', 'sdProcessInstanceService', 'sdLoggerService', 'sdMessageService', 'sdViewUtilService', '$sce', 
	                                                               '$filter','sgI18nService',
	                                                                    JoinProcessDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function JoinProcessDialogDirective($parse, $q, sdUtilService, sdProcessInstanceService, sdLoggerService, sdMessageService, sdViewUtilService, $sce, $filter, sgI18nService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdJoinProcessDialog');
		var interpolate = $filter('interpolate');
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
				template: // Join process Dialog
					'<span style="float: left;"' 
					+ ' sd-dialog="joinProcessController.joinProcessDialog"'
					+ ' sda-title="{{joinProcessController.dialogTitle}}"'
					+ ' sda-type="confirm"'
					+ ' sda-scope="this"'
					+ ' sda-on-open="joinProcessController.onOpenDialog(res)"'
					+ ' sda-template="'
					+  sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/joinProcessDialog.html"'
					+ ' sda-on-confirm="joinProcessController.confirm()"'
					+ ' sda-confirm-action-label="{{i18n(\'views-common-messages.views-joinProcessDialog-abortAndJoin-button-label\')}}"'
					+ ' sda-cancel-action-label="{{i18n(\'views-common-messages.common-cancel\')}}"'
					+ ' class="view-tool-link">'				
					+ '</span>'
					// Notification Dialog
					+ '<span style="float: left;"' 
					+ ' sd-dialog="joinProcessController.notificationDialog"'
					+ ' sda-title="{{joinProcessController.notificationTitle}}"'
					+ ' sda-type="custom"'
					+ ' sda-scope="this"'
					+ ' sda-on-close="joinProcessController.onCloseNotification()"'
					+ ' sda-template="'
					+  sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/joinProcessNotification.html">'
					+ '</span>',
				controller: JoinProcessDialogController
			};
		
		/*
		 * Controller class
		 */
		function JoinProcessDialogController($attrs, $scope, $element) {
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
							self.handleJoinProcess();
						}
						
						$scope.showDialog = false;
					}
				});
				
				self.closeThisDialog = closeThisDialog;
				self.resetValues = resetValues;
				self.confirm = confirm;
				self.handleJoinProcess = handleJoinProcess;
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
			JoinProcessDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			/*
			 * 
			 */
			JoinProcessDialogController.prototype.showErrorMessage = function(key, def, args) {
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

				sdMessageService.showMessage(msg);
			}
			
			/*
			 * 
			 */
			function resetValues() {
				
				self.joinProcess = {
					advancedSelect : false,
					matchAll : true,
					relatedProcesses : {
						totalCount : 0,
						list : []
					},
					pageSize : 6,
					dialogMsg : $sce.trustAsHtml(self.i18n('views-common-messages.views-switchProcessDialog-joinProcessMessage-message')),
					notificationMsg : '',
					linkComment : '',
					joinCompleted: false,
					isCase: false,
					processOID: undefined
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
					linkComment : self.joinProcess.linkComment
				};
				
				if (self.processInstanceOIDs.length > 0) {
					abortData.sourceProcessOID = self.processInstanceOIDs[0];
				}

				if (self.joinProcess.advancedSelect == true) {
					abortData.targetProcessOID = self.joinProcess.processOID;
				} else {
					if (angular.isDefined(self.joinProcess.relatedProcessDataTable.getSelection())
							&& self.joinProcess.relatedProcessDataTable.getSelection() != null) {
						abortData.targetProcessOID = self.joinProcess.relatedProcessDataTable.getSelection().oid;
					}
				}

				if (self.validate(abortData)) {
					performAbortJoin(abortData).then(function(data) {
						if (angular.isDefined(data.targetProcess)) {
							self.joinProcess.joinCompleted = true;
							openNotificationDialog(data);
							deferred.resolve();
						} else {
							self.showErrorMessage({message: data.statusMessage});
						}
					}, function(result) {
						// Error occurred
						var message = 'views-common-messages.views-common-process-abortProcess-failureMsg2';
						if (result.message) {
							message = result;
						}
						self.showErrorMessage(
								message,
								'An error occurred while performing abort & join.',
								[ abortData.sourceProcessOID ]);
					});
				}
				
				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function performAbortJoin(abortPayload) {
				var deferred = $q.defer();

				sdProcessInstanceService.abortAndJoinProcess(abortPayload).then(function(data) {
					// abort & start successful
					deferred.resolve(data);

				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing abort & join.\n Caused by: ' + result);
					deferred.reject(result);
				});

				return deferred.promise;
			}

			/*
			 * 
			 */
			function validate(abortData) {
				if (!angular.isDefined(abortData.targetProcessOID)) {
					var msgKey = 'views-common-messages.views-joinProcessDialog-inputProcess-message';
					if (self.joinProcess.isCase) {
						msgKey = 'views-common-messages.views-joinCaseDialog-inputProcess-message';
					}
					self.showErrorMessage(msgKey, 'Please select a case/process oid.');
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function handleJoinProcess() {
				self.processInstances = parseAttribute($scope.$parent, $attrs.sdaProcessInstances);
				if (self.processInstances.length != 1) {
					openNotificationDialog(self.i18n(
							'views-common-messages.views-joinProcessDialog-selectProcessToAbort',
							'Please select exactly one Process!'));
					return;
				}
				self.resetValues();
				
				self.processInstanceOIDs = getProcessInstanceOIDs();
				var process = self.processInstances[0];
				self.joinProcess.isCase = process.caseInstance;
				
				self.dialogTitle = sdUtilService.format(self
						.i18n('views-common-messages.views-joinProcessDialog-title'), [ self.processInstances[0].processName ]);
				
				checkIfProcessesAbortable('abortandjoin').then(function(data) {
					if (data.length > 0) {
						// Un-abortable condition found
						openNotificationDialog(data);
					} else {
						// Can proceed to abort and start
						openJoinProcessDialog();
					}
				}, function(result) {
					// Error occurred
				})
			}
			
			/*
			 * 
			 */
			function openNotificationDialog(result) {
				// Show notification dialog for abort & Join
				if (self.joinProcess.joinCompleted == true && angular.isDefined(result)) {
					
					if (angular.isDefined(result.abortedProcess) && angular.isDefined(result.targetProcess)) {
						  self.joinProcess.notificationMsg = interpolate(sgI18nService.translate(
								    'views-common-messages.views-joinProcessDialog-processJoined', 'Error'),
								    [ result.abortedProcess.processName, result.targetProcess.processName]);
					}
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.INFO);
					if(result.targetProcess) {
						self.abortNotification.targetProcessOid = result.targetProcess.oid
					}
					
					
				} else if (angular.isArray(result)) {
					self.abortNotification = {
						list : result,
						totalCount : result.length
					};
					
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.ERROR);

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
			function openJoinProcessDialog() {
				loadRelatedProcesses().then(function() {
					self.joinProcessDialog.open();
				}, function() {
					self.joinProcessDialog.open();
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
			function loadRelatedProcesses() {
				var deferred = $q.defer();

				sdProcessInstanceService.getRelatedProcesses(self.processInstanceOIDs, !self.joinProcess.matchAll,
						self.joinProcess.isCase).then(function(data) {
					// getRelatedProcesses successful
					self.joinProcess.relatedProcesses = {};
					self.joinProcess.relatedProcesses.list = data;
					self.joinProcess.relatedProcesses.totalCount = data.length;

					if (angular.isDefined(self.joinProcess.relatedProcessDataTable)) {
						self.joinProcess.relatedProcessDataTable.refresh();
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
				self.joinProcess.advancedSelect = val;
			}

			/*
			 * 
			 */
			function showMatchAll(val) {
				self.joinProcess.matchAll = val;
				loadRelatedProcesses();
			}
			
			/*
			 * 
			 */
			function okNotification(scope) {
				self.notificationDialog.confirm();

				// Join finished
				if (angular.isDefined(self.abortNotification.targetProcessOid)) {
					self.onConfirm()(self.abortNotification.targetProcessOid);
				}else {
					var oids = [];
					angular.forEach(self.abortNotification.list, function(abortData) {
						oids.push(abortData.targetProcess.oid);
					});

					if (angular.isDefined(self.onConfirm)) {
						self.onConfirm()(oids);
					}
				}
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


			$scope.joinProcessController = self;
		}
		
		return directiveDefObject;
	}
})();
