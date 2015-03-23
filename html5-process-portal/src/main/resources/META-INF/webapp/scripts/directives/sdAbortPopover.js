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

	angular.module('bpm-common').directive('sdAbortPopover', ['$q', 'sdUtilService', 'sdActivityInstanceService', 'sdLoggerService', 'eventBus', 
	                                                          '$timeout', '$parse', 
	                                                          AbortPopoverDirective]);

	var trace;
	
	/*
	 * Directive class
	 * Attributes supported:
	 * 		sda-process-instances {@}	REQUIRED 
	 * 			- Interpolated expression expected to return array ([...])
	 * 		sda-on-open (@)	NOT REQUIRED
	 * 			- Function string
	 * 		sda-on-confirm (@)	NOT REQUIRED
	 * 			- Function string
	 * 		ng-disabled
	 * Usage:
	 * 	<button sd-abort-popover sda-process-instances="..." ...
	 */
	function AbortPopoverDirective($q, sdUtilService, sdActivityInstanceService, sdLoggerService, eventBus, $timeout, $parse) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdAbortPopover');
				
		var ACTION_TYPE = {
			ABORT_AND_START : 'abortandstart',
			ABORT_AND_JOIN : 'abortandjoin'
		}
		
		var SUPPORTED_NOTIFICATION_TYPES = {
			ERROR: 'error',
			WARNING: 'warning',
			INFO: 'info'
		}
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					onOpen: '&sdaOnOpen',
					onConfirm: '&sdaOnConfirm',
					disabled: '=ngDisabled'
				},
				transclude: true,
				template: 
				  '<span sd-popover="abortPopoverController.popoverDirective" sda-on-open="abortPopoverController.handlePopoverClick()" id="abortPopoverSpan" class="sd-abort-popover-link" ng-disabled="disabled" >'
					+ '<span ng-transclude></span>'
					+ '<div id="abortPopoverDiv" class="popover-body">'
						+ '<div><a href="#" ng-click="abortPopoverController.handleAbort(\'' + ACTION_TYPE.ABORT_AND_START +'\')" >'
							+ '{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandstart\')}}</a>'
						+ '</div>'
						+ '<div><a ng-hide="abortPopoverController.disableStartJoin()" href="#"' 
							+ ' ng-click="abortPopoverController.handleAbort(\'' 
							+ ACTION_TYPE.ABORT_AND_JOIN +'\')" >'
							+ '{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandjoin\')}}</a>'
						+ '</div>'
					+ '</div>'					
				+ '</span>'
				// Abort popover Dialog
				+ '<span style="float: left;"' 
				+ ' sd-dialog="abortPopoverController.abortPopoverDialog"'
				+ ' sda-title="{{abortPopoverController.dialogTitle}}"'
				+ ' sda-type="custom"'
				+ ' sda-scope="this"'
				+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/abortPopoverDialogBody.html"'
				+ ' class="view-tool-link">'				
				+ '</span>'
				// Notification Dialog
				+ '<span style="float: left;"' 
				+ ' sd-dialog="abortPopoverController.abortNotificationDialog"'
				+ ' sda-title="{{abortPopoverController.abortNotificationTitle}}"'
				+ ' sda-type="custom"'
				+ ' sda-scope="this"'
				+ ' sda-on-close="abortPopoverController.onCloseNotification()"'
				+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/abortPopoverNotification.html">'
				+ '</span>',
				controller: AbortPopoverController
			};
		
		/*
		 * Controller class
		 */
		function AbortPopoverController($attrs, $scope, $element) {

			var self = this;

			initialize();
			$timeout(function() {
				$scope.$apply(function() {
					exposeAPI($attrs.sdAbortPopover, $scope.$parent);
				});
			});

			/*
			 * Initialize the component
			 */
			function initialize() {
				$element.addClass('sd-abort-popover')

				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
					self.i18n = $scope.i18n;
				}

				self.onConfirm = $scope.onConfirm;
				self.onOpen = $scope.onOpen;

				self.switchProcessDialogMsg = self
						.i18n('views-common-messages.views-switchProcessDialog-switchProcessmessage');

				self.handleAbort = handleAbort;
				self.handlePopoverClick = handlePopoverClick;
				self.fetchSpawnableProcesses = fetchSpawnableProcesses;
				self.closeThisDialog = closeThisDialog;
				self.initializeValues = initializeValues;
				self.confirm = confirm;
				self.validateAbortStart = validateAbortStart;
				self.validateAbortJoin = validateAbortJoin;
				self.performAbortSwitch = performAbortSwitch;
				self.okNotification = okNotification;
				self.closeNotification = closeNotification;
				self.onCloseNotification = onCloseNotification;
				self.disableStartJoin = disableStartJoin;
				self.safeApply = function() {
					sdUtilService.safeApply($scope);
				};

				// Abort & Join scope functions
				self.showAdvancedSelect = showAdvancedSelect;
				self.showMatchAll = showMatchAll;

				self.showAbortAndStart = function() {
					return ACTION_TYPE.ABORT_AND_START == self.actionType;
				};

				self.showAbortAndJoin = function() {
					return ACTION_TYPE.ABORT_AND_JOIN == self.actionType;
				};

				// Initialize the values
				self.initializeValues();
			}
			
			/*
			 * 
			 */
			AbortPopoverController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};
			
			/*
			 * 
			 */
			AbortPopoverController.prototype.showErrorMessage = function(key, def) {
				var msg = key;
				if (def) {
					msg = def;
				}
				var prefix = 'views-common-messages.abort.dialog.error.';
				key = prefix + key;
				msg = self.i18n(key, def);
				
				self.resetErrorMessage();
				eventBus.emitMsg('js.error', msg);
			};
			
			/*
			 * 
			 */
			AbortPopoverController.prototype.resetErrorMessage = function() {
				eventBus.emitMsg('js.error.reset');
			};
			
			// API with open & close functions
			function AbortPopoverApi() {
				this.show = function(event) {
					self.popoverDirective.show(event);
				}
			}
			
			// Expose the api to allow show event from other targets
			// Attribute value sd-abort-popover='someObject' will be assigned the api object
			// A button with ng-click='someObject.show(event)' would show the popover at the target location
			function exposeAPI(popoverAttr, scope) {
				if (angular.isDefined(popoverAttr) && popoverAttr != '') {
					var popoverAttrAssignable = $parse(popoverAttr).assign;
					if (popoverAttrAssignable) {
						popoverAttrAssignable(scope, new AbortPopoverApi());
					} else {
						trace.info('Could not expose API for: ' + popoverAttr + ', expression is not an assignable.');
					}
				}
			}
			
			/*
			 * 
			 */
			function initializeValues() {
				self.actionType = undefined;
				resetValues();
			}
			
			/*
			 * 
			 */
			function resetValues() {
				// Abort & Start scope variables
				self.selectedProcess = undefined;
				self.spawnableProcesses = [];
				self.switchCompleted = false;
				self.linkComment = '';

				// Abort & Join scope variables
				self.abortAndJoin = {
					advancedSelect : false,
					matchAll : true,
					relatedProcesses : {
						totalCount : 0,
						list : []
					},
					pageSize : 6,
					dialogMsg : self.i18n('views-common-messages.views-switchProcessDialog-joinProcessMessage-message'),
					joinScope : 'Process', // Other alternative is 'Case'
					notificationMsg : '',
					linkComment : ''
				};

				self.abortNotification = {
					totalCount : 0,
					list : []
				};
			}
			
			/*
			 * 
			 */
			function disableStartJoin() {
				return !(self.processInstances != undefined && self.processInstances.length === 1);
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
			function handlePopoverClick() {

				resetValues();

				if (angular.isDefined(self.onOpen)) {
					self.onOpen();
				}

				self.processInstances = parseAttribute($scope.$parent, $attrs.sdaProcessInstances);
				
				self.processInstanceOIDs = getProcessInstanceOIDs();
			}
			
			/*
			 * 
			 */
			function handleAbort(actionType) {
				self.actionType = actionType;
				if (ACTION_TYPE.ABORT_AND_START === actionType) {
					handleAbortAndStart();

				} else if (ACTION_TYPE.ABORT_AND_JOIN === actionType) {
					handleAbortAndJoin();
				}
			}

			/*
			 * 
			 */
			function handleAbortAndStart() {
				self.dialogTitle = self.i18n('views-common-messages.views-switchProcessDialog-title');

				checkIfProcessesAbortable().then(function(data) {
					if (data.length > 0) {
						// Un-abortable condition found
						openInfoDialog(data);
					} else {
						// Can proceed to abort and start
						openAbortPopoverDialog();
					}
				}, function(result) {
					// Error occurred
				})
			}

			/*
			 * 
			 */
			function openAbortPopoverDialog() {
				if (self.showAbortAndStart()) {
					fetchSpawnableProcesses().then(function(process) {
						self.spawnableProcesses = process;
						self.abortPopoverDialog.open();
					}, function(error) {
						openInfoDialog(error);
					});
				} else if (self.showAbortAndJoin()) {
					loadRelatedProcesses().then(function() {
						self.abortPopoverDialog.open();
					}, function() {
						self.abortPopoverDialog.open();
					});
				}
			}

			/*
			 * 
			 */
			function handleAbortAndJoin() {
				self.dialogTitle = sdUtilService.format(self
						.i18n('views-common-messages.views-joinProcessDialog-title'), [ self.processInstances[0].processName ]);
				openAbortPopoverDialog();
			}

			/*
			 * 
			 */
			function fetchSpawnableProcesses() {
				var deferred = $q.defer();

				sdActivityInstanceService.getSpawnableProcesses(self.processInstanceOIDs).then(function(data) {
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
			function closeThisDialog(scope) {
				self.abortPopoverDialog.close();
			}
			
			/*
			 * 
			 */
			function confirm(scope) {
				var abortData = {
					linkComment : self.linkComment
				};

				if (self.showAbortAndStart()) {
					abortData.processInstaceOIDs = self.processInstanceOIDs;
					if (angular.isDefined(self.selectedProcess)) {
						abortData.processId = self.selectedProcess.qualifiedId;
					}

					if (self.validateAbortStart(abortData)) {
						performAbortSwitch(abortData).then(function(data) {
							self.switchCompleted = true;

							openInfoDialog(data);
							self.closeThisDialog(scope);
						}, function(result) {
							// Error occurred
							self.showErrorMessage('save', 'An error occurred while performing abort & start.');
						});
					}
				} else if (self.showAbortAndJoin()) {
					if (self.processInstanceOIDs.length > 0) {
						abortData.sourceProcessOID = self.processInstanceOIDs[0];
					}

					if (self.abortAndJoin.advancedSelect == true) {
						abortData.targetProcessOID = self.abortAndJoin.processOID;
					} else {
						if (angular.isDefined(self.abortAndJoin.relatedProcessDataTable.getSelection())
								&& self.abortAndJoin.relatedProcessDataTable.getSelection() != null) {
							abortData.targetProcessOID = self.abortAndJoin.relatedProcessDataTable.getSelection().oid;
						}
					}

					if (self.validateAbortJoin(abortData)) {
						performAbortJoin(abortData).then(function(data) {
							// 
							self.closeThisDialog(scope);

							openInfoDialog(data);
						}, function(result) {
							// Error occurred
							self.showErrorMessage('save', 'An error occurred while performing abort & join.');
						});
					}
				}
			}
			
			/*
			 * 
			 */
			function validateAbortStart(abortData) {
				if (!angular.isDefined(abortData.processId)) {
					self.showErrorMessage('abortandstart', 'Please select a process.');
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function validateAbortJoin(abortData) {
				if (!angular.isDefined(abortData.targetProcessOID)) {
					self.showErrorMessage('abortandjoin', 'Please select a process.');
					return false;
				}
				return true;
			}

			/*
			 * 
			 */
			function performAbortSwitch(abortPayload) {
				var deferred = $q.defer();

				sdActivityInstanceService.switchProcess(abortPayload).then(function(data) {
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
			function performAbortJoin(abortPayload) {
				var deferred = $q.defer();

				sdActivityInstanceService.abortAndJoinProcess(abortPayload).then(function(data) {
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
			function checkIfProcessesAbortable() {
				var deferred = $q.defer();

				sdActivityInstanceService
						.checkIfProcessesAbortable(self.processInstanceOIDs)
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

				sdActivityInstanceService
						.getRelatedProcesses(self.processInstanceOIDs, !self.abortAndJoin.matchAll, false)
						.then(
								function(data) {
									// getRelatedProcesses successful
									self.abortAndJoin.relatedProcesses = {};
									self.abortAndJoin.relatedProcesses.list = data;
									self.abortAndJoin.relatedProcesses.totalCount = data.length;

									if (angular.isDefined(self.abortAndJoin.relatedProcessDataTable)) {
										self.abortAndJoin.relatedProcessDataTable.refresh();
										self.safeApply();
									}
									deferred.resolve(data);
								},
								function(result) {
									// Error occurred
									trace
											.log('An error occurred while performing "Get Related Processes".\n Caused by: '
													+ result);
									deferred.reject(result);
								});

				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function okNotification(scope) {
				self.abortNotificationDialog.confirm();

				if (self.switchCompleted || self.showAbortAndJoin()) {
					// Switch/Join finished
					var oids = [];
					angular.forEach(self.abortNotification.list, function(abortData) {
						oids.push(abortData.targetProcess.oid);
					});
					
					if (angular.isDefined(self.onConfirm)) {
						self.onConfirm()(self.actionType, oids);
					}
				} else {
					// Proceed to perform abort
					openAbortPopoverDialog();
				}
			}

			/*
			 * 
			 */
			function closeNotification(scope) {
				onCloseNotification();
				self.abortNotificationDialog.close();
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
			function openInfoDialog(result) {
				if (self.showAbortAndStart()) {
					// Show notification dialog for abort & start
					if (angular.isArray(result)) {
						self.abortNotification = {
							list : result,
							totalCount : result.length
						};
						
						self.abortNotificationType = getNotificationType(result);
						self.abortNotificationTitle = self.i18n('portal-common-messages.common-'
								+ self.abortNotificationType);

						if (angular.isDefined(self.abortNotificationDataTable)) {
							self.abortNotificationDataTable.refresh();
							self.safeApply();
						}
					} else {
						// A possible error message
						self.abortNotification = result;
						
						self.abortNotificationTitle = self.i18n('portal-common-messages.common-'
								+ SUPPORTED_NOTIFICATION_TYPES.ERROR);
					}
					
				} else if (self.showAbortAndJoin()) {
					// Show notification dialog for abort & Join
					if (angular.isDefined(result)) {
						self.abortAndJoin.notificationMsg = self
								.i18n('views-common-messages.views-joinProcessDialog-processJoined');
						if (angular.isDefined(result.abortedProcess) && angular.isDefined(result.targetProcess)) {
							self.abortAndJoin.notificationMsg = sdUtilService.format(self.abortAndJoin.notificationMsg,
									[ result.abortedProcess.processName, result.targetProcess.processName ]);
						}
						self.abortNotificationTitle = self.i18n('portal-common-messages.common-'
								+ SUPPORTED_NOTIFICATION_TYPES.INFO);
					}
				}

				self.abortNotificationDialog.open();
			}

			/*
			 * 
			 */
			function getNotificationType(data) {
				var type = SUPPORTED_NOTIFICATION_TYPES.ERROR;
				if (self.switchCompleted === true) {
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

			/*
			 * 
			 */
			function showAdvancedSelect(val) {
				self.abortAndJoin.advancedSelect = val;
			}

			/*
			 * 
			 */
			function showMatchAll(val) {
				self.abortAndJoin.matchAll = val;
				loadRelatedProcesses();
			}
			
			$scope.abortPopoverController = self;
		}
		
		return directiveDefObject;
	}
})();
