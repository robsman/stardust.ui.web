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

	angular.module('bpm-common').directive('sdParticipantSelector', ['$parse', '$q', 'sdUtilService', 'sdActivityInstanceService', 
	                                                                 ParticipantSelectorDirective]);

	/*
	 * Directive class
	 */
	function ParticipantSelectorDirective($parse, $q, sdUtilService, sdActivityInstanceService) {
		
		var SUPPORTED_SELECTOR_TYPES = {
				USER: "user",
				PARTICIPANT: "participant"
		};
		
		var DEFAULT_SELECTOR = SUPPORTED_SELECTOR_TYPES.PARTICIPANT;
		
		var directiveDefObject = {
				restrict : 'AE',
				scope: {  // Creates a new sub scope
					dataSelected: "=sdaSelectedMatches",
					allowMultiple: "=sdaAllowMultiple",
					activities: "=sdaActivities",
					showAll: "=sdaShowAll",
					participantType: "=sdaParticipantType"
				},
				transclude: true,
				templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/participantSelector.html',
				controller: ParticipantSelectorController
			};
		
		/*
		 * Controller class
		 */
		function ParticipantSelectorController($attrs, $scope, $element) {
			var self = this;

			initialize();
			
			ParticipantSelectorController.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};
			
			$scope.participantSelectorCtlr = self;

			// Private methods
			/*
			 * Initialize the component
			 */
			function initialize() {
				// Make sure i18n is available in the current scope
				if (!angular.isDefined($scope.i18n)) {
					$scope.i18n = $scope.$parent.i18n;
				}
				
				// Determine selector type (user/participant)
				if (angular.isDefined($attrs.sdaSelectorType) 
						&& (SUPPORTED_SELECTOR_TYPES.USER === $attrs.sdaSelectorType || SUPPORTED_SELECTOR_TYPES.PARTICIPANT === $attrs.sdaSelectorType)) {
					self.selectorType = $attrs.sdaSelectorType;
				} else {
					self.selectorType = DEFAULT_SELECTOR;
				}
				
				if (self.selectorType === SUPPORTED_SELECTOR_TYPES.PARTICIPANT) {
					self.fetchData = fetchParticipants;
				} else {
					self.fetchData = fetchUsers;
				}
				
				self.dataTable = {};
				
				// Initialize scope values for participant selector
				self.data=[];
				self.matchVal="";
				self.textProperty = "name";
				self.tagPreMapper = tagPreMapper;
				
				/*Retrieve data from the service*/
				self.getMatches = function(v){
			      var options = {};
			      var dataPromise = self.fetchData(options);
			      dataPromise.then(function(data) {
			    	  self.data = data;
			      });
			    };
			    
			    $scope.$watch("activities", function(showAllVal) {
			    	self.activities = $scope.activities;
			    });

			    $scope.$watch("showAll", function(showAllVal) {
			    	self.showAll = showAllVal;
			    });
			    
			    $scope.$watch("participantType", function(participantTypeVal) {
			    	self.participantType = participantTypeVal;
			    });
			}
			
			function fetchParticipants(options) {
				var activities = self.activities.map(function(val) {
				    return val.oid;
				});
				return performFetch('getParticipants', self.matchVal, self.participantType, self.showAll, activities);
			};
			
			function fetchUsers(options) {
				var activities = self.activities.map(function(val) {
				    return val.oid;
				});
				return performFetch('getUsers', self.matchVal, self.participantType, self.showAll, activities);
			};
			
			function performFetch(method, matchVal, participantType, showAll, activities) {
				var deferred = $q.defer();
				var searchParam = {
						searchText: matchVal,
						participantType: participantType,
						limitedSearch: !showAll,
						activities: activities, 
						disableAdministrator: false,
						excludeUserType: false
					};
				
				var query = {};
				query.data = searchParam;
				
				sdActivityInstanceService[method](query).then(function(data) {
					deferred.resolve(data);
					self.safeApply();
				}, function(result) {
					deferred.reject(result);
				});

				return deferred.promise;
			};
			
			function tagPreMapper(item,index){
			      var tagClass="";
			      
			      switch(item.type){
			        case "USER":
			          tagClass="sd-particpant-img-tag-user";
			          break;
			        case "ROLE":
			          tagClass="sd-particpant-img-tag-role";
			          break;
			        case "ORGANIZATION":
			          tagClass="sd-particpant-img-tag-org";
			          break;
			        case "DEPARTMENT":
			          tagClass="sd-particpant-img-tag-dept";
			          break;
			        case "USERGROUP":
			          tagClass="sd-particpant-img-tag-ugrp";
			          break;
			      }
			      return tagClass;
		    };
		}
		
		return directiveDefObject;
	}
})();