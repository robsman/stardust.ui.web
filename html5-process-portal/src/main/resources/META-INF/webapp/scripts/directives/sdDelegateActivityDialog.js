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

	angular.module('workflow-ui').directive('sdDelegateActivityDialog', ['$parse', '$q', 'sdUtilService', 'sdActivityInstanceService', 'sdLoggerService', 'sdMessageService', 'sdViewUtilService',
	                                                                    DelegateActivityDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function DelegateActivityDialogDirective($parse, $q, sdUtilService, sdActivityInstanceService, sdLoggerService, sdMessageService, sdViewUtilService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdDelegateActivityDialog');
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					showDelegateDialog: "=sdaShowDialog",
					activityList: "=sdaActivityList",
					onConfirm: "&sdaOnConfirm"
				},
				transclude: true,
				template: '<span sd-dialog'
							+ ' sda-show="showDelegateDialog" sda-enable-key-events = "false"'
							+ ' sda-title="{{i18n(\'views-common-messages.delegation-title\')}}"'
							+ ' sda-type="custom"'
							+ ' sda-scope="this"'
							+ ' sda-on-open="delegateActivityController.onOpenDialog(res)"'
							+ ' sda-template="'
							+  sdUtilService.getBaseUrl() + 'plugins/html5-process-portal/scripts/directives/partials/delegateActivityDialogBody.html"'
							+ ' sda-event-info="{dialogId: \'delegateActivity\', params: activityList}"'
							+ ' class="view-tool-link">'
						+ '</span>',
				controller: DelegateActivityDialogController
			};
		
		/*
		 * Controller class
		 */
		function DelegateActivityDialogController($attrs, $scope, $element) {
			var self = this;

			initialize();

			// Private methods
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
				
				if ($attrs.sdaPageSize) {
					this.sdaPageSize = attr.sdaPageSize;
				}
				
			    // Initialize scope values for participant type select box
			    self.participantTypes = [
			                     {name:self.i18n('views-common-messages.delegation-allTypes', 'All'), value: 'All'},
			                     {name:self.i18n('views-common-messages.delegation-users', 'Users'), value: 'User'},
			                     {name:self.i18n('views-common-messages.delegation-roles', 'Roles'), value: 'Role'},
			                     {name:self.i18n('views-common-messages.delegation-orgs', 'Organizations'), value: 'Organization'},
			                     {name:self.i18n('views-common-messages.delegation-departments', 'Departments'), value: 'Department'}
			                   ];
			    
			    self.participantDataTable = {};
			    self.participants = {};
			    
			    $scope.$watch("activityList", function(activitiesVal) {
			    	self.activities = activitiesVal;
			    });
			    
			    $scope.$watch("delegateActivityController.searchAllParticipant", function(activitiesVal) {
			    	if (angular.isDefined(self.participantDataTable) && angular.isDefined(self.participantDataTable.refresh)) {
			    		if (!self.searchParticipantSectionVisible) {
			    			self.participantDataTable.refresh();
			    		}
			    	}
			    });
			    
			    self.fetchParticipants = fetchParticipants;
			    self.showSearchParticipantSection = showSearchParticipantSection;
			    self.showSelectParticipantSection = showSelectParticipantSection;
			    self.onOpenDialog = onOpenDialog;
			    self.closeThisDialog = closeThisDialog;
			    self.resetValues = resetValues;
			    self.confirm = confirm;
			    self.validate = validate;
			    
			    $scope.tempurl = sdUtilService.getBaseUrl() + "plugins/html5-process-portal/scripts/directives/partials/delegateActivityDialogBody.html";
			}
			
			/*
			 * 
			 */
			DelegateActivityDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			/*
			 * 
			 */
			DelegateActivityDialogController.prototype.showErrorMessage = function(key, def, args) {
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
			function showSearchParticipantSection() {
				self.searchParticipantSectionVisible = true;
			}

			/*
			 * 
			 */
			function showSelectParticipantSection() {
				self.searchParticipantSectionVisible = false;
			}

			/*
			 * 
			 */
			function onOpenDialog(result) {
				self.resetValues();

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
			function resetValues() {
				self.searchParticipantSectionVisible = true;
				self.searchAllParticipant = false;
				self.participantDataSelected = [];

				self.participantType = self.participantTypes[0];
			}

			/*
			 * 
			 */
			function confirm(scope) {
				var selectedParticipant = null;
				if (self.searchParticipantSectionVisible) {
					// Use data from selector
					if (self.participantDataSelected.length > 0) {
						selectedParticipant = self.participantDataSelected[0];
					}
				} else {
					if (angular.isDefined(self.participantDataTable.getSelection())) {
						selectedParticipant = self.participantDataTable.getSelection();
					}
				}

				var activitiesArr = [];
				angular.forEach(self.activities, function(actvty) {
					activitiesArr.push(actvty.activityOID);
				});

				var delegateData = {
					activities : activitiesArr,
					participant : selectedParticipant,
				};
				if (self.validate(delegateData)) {
					performDelegate(delegateData).then(function(data) {
						if(data.failure && data.failure.length > 0) {
							// Error occurred
							self.showErrorMessage(data.failure[0]);
							
							return;
						}

						if (angular.isDefined(self.onConfirm)) {
							self.onConfirm();
						}

						self.closeThisDialog(scope);
					}, function(result) {
						// Error occurred
						self.showErrorMessage('views-common-messages.delegation-error-save', 'An error occurred while performing delegation.');
					});
				}
			}

			/*
			 * 
			 */
			function validate(delegateData) {
				if (!delegateData) {
					self.showErrorMessage('views-common-messages.delegation-error-general', 'An error occurred.');
					return false;
				}
				if (!angular.isDefined(delegateData.participant) || delegateData.participant == null) {
					self.showErrorMessage('views-common-messages.delegation-noParticipantSelected-message', 'No participant selected.');
					return false;
				}
				if (delegateData.activities.length === 0) {
					self.showErrorMessage('views-common-messages.delegation-error-activities', 'No activity selected.');
					return false;
				}
				return true;
			}

			/*
			 * 
			 */
			function fetchParticipants(options) {

				var query = {};

				var deferred = $q.defer();
				self.participants = {};

				if (self.activities == undefined) {
					return {
						totalCount : 0,
						list : [ {
							name : '',
							type : ''
						} ]
					};
				}
				var activities = self.activities.map(function(val) {
					return val.activityOID;
				});

				var matchVal = '';

				var pType = 'All';
				if (self.searchParticipantSectionVisible) {
					matchVal = options;
					pType = self.participantType.value;
				} else {
					query.options = options;
				}

				query.data = {
					searchText : matchVal, // Fetch all
					participantType : pType,
					limitedSearch : !self.searchAllParticipant,
					activities : activities,
					disableAdministrator : false,
					excludeUserType : false
				};

				sdActivityInstanceService.getParticipants(query).then(function(data) {

					self.participants = data;

					if (self.searchParticipantSectionVisible) {
						deferred.resolve(self.participants.list);
					} else {
						deferred.resolve(self.participants);
					}

					self.safeApply();
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while fetching participants.\n Caused by: ' , result);
					self.showErrorMessage('views-common-messages.delegation-error-fetch', 'An error occurred while fetching participants.');

					deferred.reject(result);
				});

				return deferred.promise;
			};

			/*
			 * 
			 */
			function performDelegate(delegatePayload) {
				var deferred = $q.defer();
				sdActivityInstanceService.delegateActivities(delegatePayload).then(function(data) {
					// Delegation successful
					deferred.resolve(data);
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing delegation.\n Caused by: ' , result);
					deferred.reject(result);
				});

				return deferred.promise;
			}

			$scope.delegateActivityController = self;
		}
		
		return directiveDefObject;
	}
})();
