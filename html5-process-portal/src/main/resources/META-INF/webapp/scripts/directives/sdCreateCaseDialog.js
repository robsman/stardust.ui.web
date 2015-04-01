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

	angular.module('bpm-common').directive('sdCreateCaseDialog', ['$parse', '$q', 'sdUtilService', 'sdActivityInstanceService', 'sdProcessInstanceService', 'sdLoggerService', 'eventBus', 'sdViewUtilService',
	                                                                    CreateCaseDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function CreateCaseDialogDirective($parse, $q, sdUtilService, sdActivityInstanceService, sdProcessInstanceService, sdLoggerService, eventBus, sdViewUtilService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdCreateCaseDialog');
		
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
				template: // Create case process Dialog
					'<span style="float: left;"' 
					+ ' sd-dialog="createCaseController.createCaseDialog"'
					+ ' sda-title="{{createCaseController.dialogTitle}}"'
					+ ' sda-type="confirm"'
					+ ' sda-scope="this"'
					+ ' sda-on-open="createCaseController.onOpenDialog(res)"'
					+ ' sda-template="plugins/html5-process-portal/scripts/directives/partials/createCaseDialog.html"'
					+ ' sda-on-confirm="createCaseController.confirm()"'
					+ ' sda-confirm-action-label="{{i18n(\'views-common-messages.common-ok\')}}"'
					+ ' sda-cancel-action-label="{{i18n(\'views-common-messages.common-cancel\')}}"'
					+ ' class="view-tool-link">'				
					+ '</span>'
					// Notification Dialog
					+ '<span style="float: left;"' 
					+ ' sd-dialog="createCaseController.notificationDialog"'
					+ ' sda-title="{{createCaseController.notificationTitle}}"'
					+ ' sda-type="alert"'
					+ ' sda-scope="this">'
					+ '		<span style="padding-right: 11px;">'
					+ '			<i style="color: red;" class="glyphicon glyphicon-remove-sign"></i>'
					+ '		</span>'
					+ ' 	<span>{{createCaseController.notificationMessage.message}}</span>'
					+ '</span>',
				controller: CreateCaseDialogController
			};
		
		/*
		 * Controller class
		 */
		function CreateCaseDialogController($attrs, $scope, $element) {
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
							self.handleCreateCase();
						}
						
						$scope.showDialog = false;
					}
				});
				
				self.closeThisDialog = closeThisDialog;
				self.resetValues = resetValues;
				self.confirm = confirm;
				self.handleCreateCase = handleCreateCase;
				self.closeNotification = closeNotification;
				self.validate = validate;
				self.safeApply = function() {
					sdUtilService.safeApply($scope);
				};

				// Initialize the values
				self.resetValues();
			}
			
			/*
			 * 
			 */
			CreateCaseDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			/*
			 * 
			 */
			CreateCaseDialogController.prototype.showErrorMessage = function(key, def, args) {
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
			CreateCaseDialogController.prototype.resetErrorMessage = function() {
				eventBus.emitMsg("js.error.reset");
			}
			
			/*
			 * 
			 */
			function resetValues() {
				
				self.createCase = {
					caseName: undefined,
					description: undefined,
					note: undefined,
					openCaseDetail: true,
					createCaseCompleted: false
				};
				
				self.createCase.caseOwner = 'Motu';// TODO remove hard code and use the correct service.

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
				
				var createCaseData = {};
				
				createCaseData.caseName = self.createCase.caseName;
				createCaseData.descripton = self.createCase.descripton;
				createCaseData.note = self.createCase.note;
				
				if (self.processInstanceOIDs.length > 0) {
					createCaseData.sourceProcessOIDs = self.processInstanceOIDs;
				}

				if (self.validate(createCaseData)) {
					performCreateCase(createCaseData).then(function(data) {
						
						deferred.resolve();
						
						openNotificationDialog(data);
					}, function(result) {
						// Error occurred
						self.showErrorMessage(
								'views-common-messages.views-common-process-createCase-failure',
								'An error occurred while performing Create Case.');
					});
				}
				
				return deferred.promise;
			}
			
			/*
			 * 
			 */
			function performCreateCase(createCasePayload) {
				var deferred = $q.defer();

				sdProcessInstanceService.createCase(createCasePayload).then(function(data) {
					// Create Case successful
					deferred.resolve(data);

				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing Create case.\n Caused by: ' + result);
					deferred.reject(result);
				});

				return deferred.promise;
			}

			/*
			 * 
			 */
			function validate(createCaseData) {
				if (!angular.isDefined(createCaseData.caseName)) {
					var msgKey = 'views-common-messages.views-attachToCase-caseRequired-message';
					self.showErrorMessage(msgKey, 'Please provide a case name.');
					return false;
				}
				return true;
			}
			
			/*
			 * 
			 */
			function handleCreateCase() {
				self.processInstances = parseAttribute($scope.$parent, $attrs.sdaProcessInstances);
				if (self.processInstances.length < 1) {
					openNotificationDialog({ message: self.i18n(
							'views-common-messages.views-attachToCase-selectProcessToCreateCase',
							'Please select at least one process to Create Case.')});
					return;
				}
				self.resetValues();
				
				if (validateSourceProcesses()) {
					self.processInstanceOIDs = getProcessInstanceOIDs();
					
					var process = self.processInstances[0];
					self.createCase.isCase = process.caseInstance;
					
					self.dialogTitle = getDialogTitle();
					
					openCreateCaseDialog();
				}
			}
			

			/*
			 * 
			 */
			function getDialogTitle() {
				return self.i18n('views-common-messages.views-createCase-title');
			}
			
			/*
			 * 
			 */
			function validateSourceProcesses() {
				if (!isRootProcessInstances() || !isNonCaseProcessInstances()) {
					openNotificationDialog({message: self.i18n(
							'views-common-messages.views-attachToCase-selectOnlyProcess-message',
							'Cases can only be created from Root Processes.')});

					return false;
				} else	if (!isActiveProcessInstances()) {
					openNotificationDialog({message: self.i18n(
							'views-common-messages.views-attachToCase-selectActiveProcesses',
							'Please select only active process(es) to Create Case.')});

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
			function isNonCaseProcessInstances() {
				var isNonCasePIs = true;
				angular.forEach(self.processInstances, function(pi) {
					if (pi.caseInstance) {
						isNonCasePIs = false;
					}
				})

				return isNonCasePIs;
			}

			/*
			 * 
			 */
			function isActiveProcessInstances() {
				var isActivePIs = true;
				angular.forEach(self.processInstances, function(pi) {
					if (pi.status != 'Active') {
						isActivePIs = false;
					}
				})

				return isActivePIs;
			}

			/*
			 * 
			 */
			function openNotificationDialog(result) {
				// Show notification dialog for Create case

				if (result.success == true) {
					if (angular.isDefined(self.onConfirm)) {
						var callback = self.onConfirm();
						if (angular.isFunction(callback)) {
							// Open case if required in the calling scope
							callback(result.message, self.createCase.openCaseDetail);
						}
						self.createCase.createCaseCompleted = true;
					}
				} else {
					self.notificationMessage = result;
					self.notificationTitle = self.i18n('portal-common-messages.common-'
							+ SUPPORTED_NOTIFICATION_TYPES.ERROR);
					self.notificationDialog.open();
				}
			}
			
			/*
			 * 
			 */
			function openCreateCaseDialog() {
				self.createCaseDialog.open();
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


			$scope.createCaseController = self;
		}
		
		return directiveDefObject;
	}
})();
