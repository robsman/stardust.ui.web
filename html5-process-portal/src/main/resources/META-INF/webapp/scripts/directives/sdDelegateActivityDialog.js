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

	angular.module('bpm-common').directive('sdDelegateActivityDialog', ['$parse', '$q', 'sdUtilService', 'sdActivityInstanceService', 'sdLoggerService', 'eventBus',
	                                                                    DelegateActivityDialogDirective]);

	var trace;
	
	/*
	 * Directive class
	 */
	function DelegateActivityDialogDirective($parse, $q, sdUtilService, sdActivityInstanceService, sdLoggerService, eventBus) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdDelegateActivityDialog');
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					showDelegateDialog: "=sdaShowDialog",
					activityList: "=sdaActivityList"
				},
				transclude: true,
				templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/delegateActivityDialog.html',
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
				self.searchParticipantSectionVisible = false;
				self.searchAllParticipant = false;
				self.participantDataSelected=[];
				self.fetchParticipants = fetchPage;
				self.participants = [];
				
				self.participantDataTable = {};
				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
					self.i18n = $scope.i18n;
				}
				
			    // Initialize scope values for participant type select box
				// TODO Use the correct values once rest is available
			    self.participantTypes = [
			                     {name:'All', id: '-1'},
			                     {name:'Users', id: '1'},
			                     {name:'Roles', id: '2'},
			                     {name:'Organisations', id: '3'},
			                     {name:'Departments', id: '4'}
			                   ];
			    self.participantType = self.participantTypes[0];
			    
			    $scope.$watch("activityList", function(activitiesVal) {
			    	self.activities = activitiesVal;
			    });
			}
			
			DelegateActivityDialogController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};
			
			DelegateActivityDialogController.prototype.showSearchParticipantSection = function() {
				self.searchParticipantSectionVisible = true;
			}
			
			DelegateActivityDialogController.prototype.showSelectParticipantSection = function() {
				self.searchParticipantSectionVisible = false;
			}
			
			DelegateActivityDialogController.prototype.onOpen = function(result) {
				// TODO
			}
			
			DelegateActivityDialogController.prototype.closeThisDialog = function(scope) {
				scope.closeThisDialog();
			}
			
			DelegateActivityDialogController.prototype.confirm = function(scope) {
				var participantData = [];
				var participantType = "-1";
				if (self.searchParticipantSectionVisible) {
					// Use data from selector
					participantData = self.participantDataSelected;
					participantType = self.participantType;
				} else {
					participantData = self.participantDataTable.getSelection();
					//TODO participation type
				}
				
				var delegateData = {
						activities: self.activities,
						participant: participantData,
						participantType: participantType
				};
				if (self.validate(delegateData)) {
					performDelegate(delegateData).then(function(data) {
						self.closeThisDialog();
					}, function(result) {
						// Error occurred
						self.showErrorMessage('save', 'An error occurred while performing delegation.');
					});
				}
			}
			
			DelegateActivityDialogController.prototype.validate = function(delegateData) {
				if (!delegateData) {
					self.showErrorMessage('general', 'An error occurred.');
					return false;
				}
				if (delegateData.participant.length === 0) {
					self.showErrorMessage('participant', 'Please select a participant.');
					return false;
				}
				if (delegateData.activities.length === 0) {
					self.showErrorMessage('activities', 'Please select an activity.');
					return false;
				}
				if (!angular.isDefined(delegateData.participantType) || !angular.isDefined(delegateData.participantType.id)
						|| delegateData.participantType.id == -1) {
					self.showErrorMessage('participantType', 'Please select a participant type.');
					return false;
				}
				return true;
			}
			
			DelegateActivityDialogController.prototype.showErrorMessage = function(key, def) {
				var msg = key;
				if (def) {
					msg = def;
				}
				var prefix = 'views-common-messages.delegation.error.';
				key = prefix + key;
				msg = self.i18n(key, def);
				
				self.resetErrorMessage();
				eventBus.emitMsg("js.error", msg);
			}
			
			DelegateActivityDialogController.prototype.resetErrorMessage = function() {
				// TODO reset the error
				eventBus.emitMsg("js.error", '');
			}
			
			function fetchPage(options) {
				var deferred = $q.defer();
				self.participants = {};
				
				var activities = self.activities.map(function(val) {
				    return val.oid;
				});

				var query = {
						searchText: '', // Fetch all
						participantType: 'All',
						limitedSearch: !self.searchAllParticipant,
						activities: activities,
						disableAdministrator: false,
						excludeUserType: false
					};
				
				sdActivityInstanceService.getParticipants(query).then(function(data) {
					
					self.participants.list = data;
					self.participants.totalCount = data.length;
					
					deferred.resolve(self.participants);

					self.safeApply();
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while fetching participants.\n Caused by: ' + result);
					self.showErrorMessage('fetch', 'An error occurred while fetching participants.');
					
					deferred.reject(result);
				});

				return deferred.promise;
			};
			
			function performDelegate(delegatePayload) {
				var deferred = $q.defer();
				sdActivityInstanceService.delegateActivities(delegatePayload).then(function(data) {
					// Delegation successful
					deferred.resolve(data);
				}, function(result) {
					// Error occurred
					trace.log('An error occurred while performing delegation.\n Caused by: ' + result);
					deferred.reject(result);
				});
				
				return deferred.promise;
			}

			$scope.delActivityDialogCtlr = self;
		}
		
		return directiveDefObject;
	}
})();