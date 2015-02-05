/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdAbortPopover', ['$q', 'sdUtilService', 'sdActivityInstanceService', 'sdLoggerService', 'eventBus', 'ngDialog', '$parse', 'sgViewPanelService',
	                                                          AbortPopoverDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function AbortPopoverDirective($q, sdUtilService, sdActivityInstanceService, sdLoggerService, eventBus, ngDialog, $parse, sgViewPanelService) {
		
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
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					/*activities: "=sdaActivities",*/
					onOpen: "&sdaOnOpen",
					onConfirm: "&sdaOnConfirm"
				},
				transclude: true,
				template: '<span id="abortPopoverSpan" class="sd-abort-popover-link" ng-click="abortPopoverController.handlePopoverClick()" ng-transclude></span>'
				+ '<div id="abortPopoverDiv" ng-show="showPopover" class="popup-dlg sd-abort-popover-box">'
					+ '<div><a href="#" ng-click="abortPopoverController.handleAbort(\'' + ACTION_TYPE.ABORT_AND_START +'\')" >{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandstart\')}}</a></div>'
					+ '<div><a href="#" ng-click="abortPopoverController.handleAbort(\'' + ACTION_TYPE.ABORT_AND_JOIN +'\')" >{{i18n(\'views-common-messages.views-switchProcessDialog-Menu-abortandjoin\')}}</a></div>'
				+ '</div>'
				+ '<span style="display:none;"' 
				+ ' sd-dialog'
				+ ' sd-show-dialog="abortPopoverController.showAbortPopoverDialog"'
				+ ' sd-title="{{i18n(\'views-common-messages.views-switchProcessDialog-title\')}}"'
				+ ' sd-is-moveable="false"'
				+ ' sd-show-overlay="true"'
				+ ' sd-dialog-type="info"'
				+ ' sd-dialog-scope="this"'
				+ ' sd-template="plugins/html5-process-portal/scripts/directives/partials/abortPopoverDialogBody.html"'
				+ ' ng-dialog-close-by-document="false"'
				+ ' class="view-tool-link">'				
				+ '</span>',
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
			    self.validate = validate;
			    self.performAbort = performAbort;
			    self.okNotification = okNotification;
			    self.closeNotification = closeNotification;
				
				$(document).bind('click', function(event) {
					if (event.target.parentElement.id !== 'abortPopoverSpan' || (event.target.firstElementChild != undefined && event.target.firstElementChild.id !== 'abortPopoverSpan')) {
						$scope.$apply(function() {
							$scope.showPopover = false;
						});
					}
				});
			}
			
			function resetValues() {
				self.showAbortAndStart = false;
				self.showAbortAndJoin = false;
				self.selectedProcess = undefined;
				self.spawnableProcesses = [];
				self.switchCompleted = false;
				self.linkComment = '';
			}
			
			function handlePopoverClick() {
				
				var disabled = false;
				if (angular.isDefined($attrs.ngDisabled)) {
					disabled = parseAttribute($scope.$parent, $attrs.ngDisabled);
				}
				
				if (!disabled) {
					self.resetValues();
					
					if (angular.isDefined(self.onOpen)) {
						self.onOpen();
					}
					
					self.activities = parseAttribute($scope.$parent, $attrs.sdaActivities);
					
					$scope.showPopover = true;
					
					self.processInstOIDs = [];
					
					angular.forEach(self.activities, function(actvty) {
						self.processInstOIDs.push(actvty.processInstance.oid);
					});
					
					//TODO remove
					//self.processInstOIDs = [45, 47];
				}
			}
			
			function handleAbort(actionType) {
				self.showAbortAndStart = false;
				self.showAbortAndJoin = false;
				if (ACTION_TYPE.ABORT_AND_START === actionType) {
					handleAbortAndStart();
					
				} else if (ACTION_TYPE.ABORT_AND_JOIN === actionType) {
					handleAbortAndJoin();
				}
			}
			
			function handleAbortAndStart() {
				
				checkIfProcessesAbortable(self.activities).then(function(data) {
					if (data.length > 0) {
						// Un-abortable condition found
						openInfoDialog(data);
					} else {
						// Can proceed to abort and start
						openAbortPopoverDialog();
					}
				}, function(result) {
					// Error occurred
					//TODO
					
				})
			}
			
			function openAbortPopoverDialog() {
				self.showAbortPopoverDialog = true;
				self.showAbortAndStart = true;
				
				fetchSpawnableProcesses(self.activities).then(function(process) {
					self.spawnableProcesses = process;
				}, function() {
					self.spawnableProcesses = [];
				});
			}
			
			function handleAbortAndJoin() {
				// TODO
				
				var options = {
						template: 'To Be Implemented...',
						plain: true,
						scope: $scope ,
						showOverlay:  true,
						title: self.i18n('admin-portal-messages.common-notification-title'),
					};
				ngDialog.open(options);
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
				scope.closeThisDialog();
			}
			
			function confirm(scope) {
				var abortData = {
					processInstaceOIDs: self.processInstOIDs,
					processId: undefined,
					linkComment: self.linkComment
				};
				
				if (self.showAbortAndStart === true) {
					if (angular.isDefined(self.selectedProcess)) {
						abortData.processId = self.selectedProcess.qualifiedId;
					}
				} else if (self.showAbortAndJoin === true) {
					// TODO
				}
				
				if (self.validate(abortData)) {
					performAbort(abortData).then(function(data) {
						self.switchCompleted = true;
						
						openInfoDialog(data);
						self.closeThisDialog(scope);
					}, function(result) {
						// Error occurred
						self.showErrorMessage('save', 'An error occurred while performing abort & start.');
					});
				}
			}
			
			function validate(abortData) {
				// TODO
				if (!angular.isDefined(abortData.processId)) {
					self.showErrorMessage('abortandstart', 'Please select a process.');
					return false;
				}
				return true;
			}
			
			function performAbort(abortPayload) {
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
			
			function checkIfProcessesAbortable(activities) {
				var deferred = $q.defer();
				
				sdActivityInstanceService.checkIfProcessesAbortable(self.processInstOIDs).then(function(data) {
					// abort & start successful
					deferred.resolve(data);
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing "Check If Processes Abortable".\n Caused by: ' + result);
					deferred.reject(result);
				});
				
				return deferred.promise;
			}
			
			function okNotification(scope) {
				closeThisDialog(scope);
				
				if (self.switchCompleted) {
					// Switch finished
					// Go to view spawned activitoes
					// TODO use sgViewPanelService to open the required view
					//sgViewPanelService.open
					
				} else {
					// Proceed to perform abort
					openAbortPopoverDialog();
				}
			}
			
			function closeNotification(scope) {
				closeThisDialog(scope);
			}
			
			function openInfoDialog(result) {
				self.abortNotification = result;
				self.abortNotificationType = getNotificationType(result);
				
				var title = self.i18n('portal-common-messages.common-' + self.abortNotificationType);
				
				var options = {
					template: 'plugins/html5-common/scripts/directives/dialogs/templates/info.html',
					userTemplate : 'plugins/html5-process-portal/scripts/directives/partials/abortPopoverNotification.html',
					controller: this,
					scope: $scope ,
					showOverlay:  true,
					title: title,
					closeByDocument: false
				};
				
				var infoDialog = ngDialog.open(options);
				
				infoDialog.closePromise.then(function(res, one, two, the) {
					BridgeUtils.View.syncLaunchPanels();
					
					if (angular.isDefined(self.onConfirm)) {
						self.onConfirm();
					}
				});
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