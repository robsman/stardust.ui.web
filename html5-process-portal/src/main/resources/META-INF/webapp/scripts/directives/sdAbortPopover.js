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
	                                                          '$timeout', '$parse', 'sdViewUtilService',
	                                                          AbortPopoverDirective]);

	var trace;
	
	/*
	 * Directive class
	 * Attributes supported:
	 * 		sda-activities {@}	REQUIRED 
	 * 			- Interpolated expression expected to return array ([...])
	 * 		sda-on-open (@)	NOT REQUIRED
	 * 			- Function string
	 * 		sda-on-confirm (@)	NOT REQUIRED
	 * 			- Function string
	 * 		ng-disabled
	 * Usage:
	 * 	<button sd-abort-popover sda-activities="..." ...
	 */
	function AbortPopoverDirective($q, sdUtilService, sdActivityInstanceService, sdLoggerService, eventBus, $timeout, $parse, sdViewUtilService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdAbortPopover');
		
		var ACTION_TYPE = {
				ABORT_AND_START: 'abortandstart',
				ABORT_AND_JOIN: 'abortandjoin'
		}
		
		var SUPPORTED_NOTIFICATION_TYPES = {
				ERROR: 'error',
			    WARNING: 'warning',
			    INFO: 'info'
		}
		
		// Adding utility method to the String to parse i18n messages with arguments
		// Usage: 'some string with {0} and {1}'.format(arg1, arg2)
		// TODO Should be moved to a common utility script
		if (!String.prototype.format) {
		  String.prototype.format = function() {
		    var args = arguments;
		    return this.replace(/{(\d+)}/g, function(match, number) { 
		      return typeof args[number] != 'undefined'
		        ? args[number]
		        : match
		      ;
		    });
		  };
		}
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					/*activities: "=sdaActivities",*/
					onOpen: "&sdaOnOpen",
					onConfirm: "&sdaOnConfirm"
				},
				transclude: true,
				template: '<span id="abortPopoverSpan" class="sd-abort-popover-link" ng-click="abortPopoverController.handlePopoverClick()" ng-transclude></span>'
				// Popover Div
				+ '<div id="abortPopoverDiv" ng-show="showPopover" class="popup-dlg sd-abort-popover-box">'
					+ '<div><a href="#" ng-click="abortPopoverController.handleAbort(\'' + ACTION_TYPE.ABORT_AND_START +'\')" >{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandstart\')}}</a></div>'
					+ '<div><a ng-hide="abortPopoverController.disableStartJoin()" href="#" ng-click="abortPopoverController.handleAbort(\'' + ACTION_TYPE.ABORT_AND_JOIN +'\')" >{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandjoin\')}}</a></div>'
				+ '</div>'
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
				+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/abortPopoverNotification.html">'
				+ '</span>'
				,
				controller: AbortPopoverController
			};
		
		/*
		 * Controller class
		 */
		function AbortPopoverController($attrs, $scope, $element) {
			
			var self = this;

			initialize();

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
				
				self.switchProcessDialogMsg = self.i18n('views-common-messages.views-switchProcessDialog-switchProcessmessage');
				
				self.handleAbort = handleAbort;
				self.handlePopoverClick = handlePopoverClick;
				self.fetchSpawnableProcesses = fetchSpawnableProcesses;
				self.closeThisDialog = closeThisDialog;
			    self.resetValues = resetValues;
			    self.confirm = confirm;
			    self.validateAbortStart = validateAbortStart;
			    self.validateAbortJoin = validateAbortJoin;
			    self.performAbortSwitch = performAbortSwitch;
			    self.okNotification = okNotification;
			    self.closeNotification = closeNotification;
			    self.disableStartJoin = disableStartJoin;
			    self.safeApply = function() {
					sdUtilService.safeApply($scope);
				};
			    
			    // Abort & Join scope functions
			    self.showAdvancedSelect = showAdvancedSelect;
			    self.showMatchAll = showMatchAll;
			    
			    self.showAbortAndStart = function () {
					return ACTION_TYPE.ABORT_AND_START == self.actionType;
				};
				
				self.showAbortAndJoin = function () {
					return ACTION_TYPE.ABORT_AND_JOIN == self.actionType;
				};
			}
			
			function resetValues() {
				self.actionType = undefined;
				
				// Abort & Start scope variables
				self.selectedProcess = undefined;
				self.spawnableProcesses = [];
				self.switchCompleted = false;
				self.linkComment = '';
				
				// Abort & Join scope variables
				self.abortAndJoin = {
					advancedSelect: false,
					matchAll: true,
					relatedProcesses: {totalCount: 0, list: []},
					pageSize: 6,
					dialogMsg: self.i18n('views-common-messages.views-switchProcessDialog-joinProcessMessage-message'),
					joinScope: 'Process', // Other alternative is 'Case'
					notificationMsg: '',
					linkComment: ''
				};
				
				self.abortNotification = {totalCount: 0, list: []};
			}
			
			function disableStartJoin() {
				return !(self.activities != undefined && self.activities.length === 1);
			}
			
			function handlePopoverClick() {
				// In case of ng-disabled, make sure the click is not activated
				var disabled = false;
				if (angular.isDefined($attrs.ngDisabled)) {
					disabled = parseAttribute($scope.$parent, $attrs.ngDisabled);
				}
				
				if (!disabled) {
					// Reset the values first
					self.resetValues();
					
					// Handle close by click on document event
					var popoverCloseEvent = function(event) {
						if (event.target.parentElement.id !== 'abortPopoverSpan' || (event.target.firstElementChild != undefined && event.target.firstElementChild.id !== 'abortPopoverSpan')) {
							$scope.$apply(function() {
								$scope.showPopover = false;
								// this is important since we want this to be called exactly once
								$(document).unbind('click', popoverCloseEvent);
							});
						}
					};
					$(document).bind('click', popoverCloseEvent);
					
					if (angular.isDefined(self.onOpen)) {
						self.onOpen();
					}
					
					self.activities = parseAttribute($scope.$parent, $attrs.sdaActivities);
					
					$scope.showPopover = true;
					
					self.processInstOIDs = [];
					
					angular.forEach(self.activities, function(actvty) {
						self.processInstOIDs.push(actvty.processInstance.oid);
					});
				}
			}
			
			function handleAbort(actionType) {
				self.actionType = actionType;
				if (ACTION_TYPE.ABORT_AND_START === actionType) {
					handleAbortAndStart();
					
				} else if (ACTION_TYPE.ABORT_AND_JOIN === actionType) {
					handleAbortAndJoin();
				}
			}
			
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
			
			function openAbortPopoverDialog() {
				if (self.showAbortAndStart()) {
					fetchSpawnableProcesses(self.activities).then(function(process) {
						self.spawnableProcesses = process;
						self.abortPopoverDialog.open();
					}, function() {
						self.spawnableProcesses = [];
						self.abortPopoverDialog.open();
					});
				} else if (self.showAbortAndJoin()) {
					loadRelatedProcesses().then(function() {
						self.abortPopoverDialog.open();
					}, function() {
						self.abortPopoverDialog.open();
					});
				}
			}
			
			function handleAbortAndJoin() {
				self.dialogTitle = self.i18n('views-common-messages.views-joinProcessDialog-title').format(self.activities[0].processInstance.processName);
				openAbortPopoverDialog();
			}
			
			function fetchSpawnableProcesses(activities) {
				var deferred = $q.defer();
				
				sdActivityInstanceService.getSpawnableProcesses(self.processInstOIDs).then(function(data) {
					deferred.resolve(data);

					self.safeApply();
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while fetching Spawnable Processes.\n Caused by: ' + result);
					self.showErrorMessage('fetch', 'An error occurred while fetching Spawnable Processes.');
					
					deferred.reject(result);
				});
				
				return deferred.promise;
			}
			
			function closeThisDialog(scope) {
				self.abortPopoverDialog.close();
			}
			
			function confirm(scope) {
				var abortData = {
					linkComment: self.linkComment
				};
				
				if (self.showAbortAndStart()) {
					abortData.processInstaceOIDs = self.processInstOIDs;
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
					if (self.processInstOIDs.length > 0) {
						abortData.sourceProcessOID = self.processInstOIDs[0];
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
			
			function validateAbortStart(abortData) {
				if (!angular.isDefined(abortData.processId)) {
					self.showErrorMessage('abortandstart', 'Please select a process.');
					return false;
				}
				return true;
			}
			
			function validateAbortJoin(abortData) {
				if (!angular.isDefined(abortData.targetProcessOID)) {
					self.showErrorMessage('abortandjoin', 'Please select a process.');
					return false;
				}
				return true;
			}
			
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
			
			function checkIfProcessesAbortable() {
				var deferred = $q.defer();
				
				sdActivityInstanceService.checkIfProcessesAbortable(self.processInstOIDs).then(function(data) {
					// checkIfProcessesAbortable successful
					deferred.resolve(data);
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing "Check If Processes Abortable".\n Caused by: ' + result);
					deferred.reject(result);
				});
				
				return deferred.promise;
			}
			
			function loadRelatedProcesses() {
				var deferred = $q.defer();
				
				sdActivityInstanceService.getRelatedProcesses(self.processInstOIDs, 
						!self.abortAndJoin.matchAll, false).then(function(data) {
					// getRelatedProcesses successful
					self.abortAndJoin.relatedProcesses = {};
					self.abortAndJoin.relatedProcesses.list = data;
					self.abortAndJoin.relatedProcesses.totalCount = data.length;
					
					if (angular.isDefined(self.abortAndJoin.relatedProcessDataTable)) {
						self.abortAndJoin.relatedProcessDataTable.refresh();
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
			
			function okNotification(scope) {
				self.abortNotificationDialog.confirm();
				
				if (self.switchCompleted || self.showAbortAndJoin()) {
					// Switch/Join finished
					// Go to view activitoes
					openWorklistView()
					
					BridgeUtils.View.syncLaunchPanels();
					if (angular.isDefined(self.onConfirm)) {
						self.onConfirm();
					}
				} else {
					// Proceed to perform abort
					openAbortPopoverDialog();
				}
			}
			
			function openWorklistView() {
				// TODO open spawned/joined activities
				// Use self.targetActivities
				sdViewUtilService.openView("worklistViewHtml5", true);
			}
			
			function closeNotification(scope) {
				self.abortNotificationDialog.close();
				BridgeUtils.View.syncLaunchPanels();
				if (angular.isDefined(self.onConfirm)) {
					self.onConfirm();
				}
			}
			
			function openInfoDialog(result) {
				if (self.showAbortAndStart()) {
					// Show notification dialog for abort & start
					self.targetActivities = result;
					self.abortNotification = {list: result, totalCount: result.length};
					self.abortNotificationType = getNotificationType(result);
					self.abortNotificationTitle = self.i18n('portal-common-messages.common-' + self.abortNotificationType);
					
					if (angular.isDefined(self.abortNotificationDataTable)) {
						self.abortNotificationDataTable.refresh();
						self.safeApply();
					}
				} else if (self.showAbortAndJoin()) {
					// Show notification dialog for abort & Join
					self.targetActivities = [result];
					if (angular.isDefined(result)) {
						self.abortAndJoin.notificationMsg = self.i18n('views-common-messages.views-joinProcessDialog-processJoined');
						if (angular.isDefined(result.abortedProcess) && angular.isDefined(result.targetProcess)) {
							self.abortAndJoin.notificationMsg = self.abortAndJoin.notificationMsg.format(result.abortedProcess.processName, result.targetProcess.processName);
						}
						self.abortNotificationTitle = self.i18n('portal-common-messages.common-' + SUPPORTED_NOTIFICATION_TYPES.INFO);
					}
				}
				
				self.abortNotificationDialog.open();
			}
			
			function getNotificationType(data) {
				var type = SUPPORTED_NOTIFICATION_TYPES.ERROR;
				if (self.switchCompleted === true) {
					type = SUPPORTED_NOTIFICATION_TYPES.INFO;
				} else {
					if (data.length === self.activities.length) {
						type = SUPPORTED_NOTIFICATION_TYPES.ERROR;
					} else if (data.length < self.activities.length) {
						type = SUPPORTED_NOTIFICATION_TYPES.WARNING;
					}
				}
				return type;
			}
			
			function parseAttribute(scope, attr) {
	        	try {
	        		var evalAttr = $parse(attr)(scope);
	        		if (!angular.isDefined(evalAttr)) {
	        			evalAttr = attr;
	        		}
	        		return evalAttr;
	            } catch( err ) {
	                return attr;
	            }
	        }
			
			function showAdvancedSelect(val) {
				self.abortAndJoin.advancedSelect = val;
			}
			
			function showMatchAll(val) {
				self.abortAndJoin.matchAll = val;
				loadRelatedProcesses();
			}
			
			AbortPopoverController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};
			
			AbortPopoverController.prototype.showErrorMessage = function(key, def) {
				var msg = key;
				if (def) {
					msg = def;
				}
				var prefix = 'views-common-messages.abort.dialog.error.';
				key = prefix + key;
				msg = self.i18n(key, def);
				
				self.resetErrorMessage();
				eventBus.emitMsg("js.error", msg);
			}
			
			AbortPopoverController.prototype.resetErrorMessage = function() {
				// TODO reset the error
				eventBus.emitMsg("js.error", '');
			}
			
			$scope.abortPopoverController = self;
		}
		
		return directiveDefObject;
	}
})();
