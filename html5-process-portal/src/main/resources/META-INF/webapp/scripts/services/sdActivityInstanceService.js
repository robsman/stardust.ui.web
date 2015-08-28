/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Subodh.Godbole
 */
(function(){
	'use strict';

	angular.module('workflow-ui.services').provider('sdActivityInstanceService', function () {
		this.$get = ['$rootScope', '$http', '$q', '$resource', 'sdDataTableHelperService', 'sdUtilService', function ($rootScope, $http, $q, $resource, sdDataTableHelperService, sdUtilService) {
			var service = new ActivityInstanceService($rootScope, $http, $q, $resource, sdDataTableHelperService, sdUtilService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function ActivityInstanceService($rootScope, $http, $q, $resource, sdDataTableHelperService, sdUtilService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/activity-instances/";
		
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getByProcessOid = function(piOid) {

		    return sdUtilService.ajax(REST_BASE_URL,'' , "process/oid/"+piOid);
		};
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.reactivate = function(activityOID) {

		    var data = {activityOID : activityOID}
		    return sdUtilService.ajax(REST_BASE_URL,'' , data);
		};
		

		/*
		 * 
		 */
		ActivityInstanceService.prototype.activate = function(activityOID) {

		    var data = {activityOID : activityOID}
		    return sdUtilService.ajax(REST_BASE_URL,'activate' , data);
		};
		
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getAllCounts = function() {
			return sdUtilService.ajax(REST_BASE_URL,'' , "allCounts");
		};
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getAllActivities = function(query) {
			var restUrl = REST_BASE_URL+ 'allActivities';
			var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

			if (queryParams.length > 0) {
				restUrl = restUrl + "?" + queryParams.substr(1);
			}
			
			var postData = sdDataTableHelperService.convertToPostParams(query.options);

			return sdUtilService.ajax(restUrl, '', postData);
		};
		
		
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getActivitylistForTLV = function(query) {
		    var restUrl = REST_BASE_URL + "forTLVByCategory";

		    var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

		    if (queryParams.length > 0) {
			var separator = "?";
			if (/[?]/.test(restUrl)) {
			    separator = "&";
			}
			restUrl = restUrl + separator + queryParams.substr(1);
		    }
		    var postData = sdDataTableHelperService.convertToPostParams(query.options);

		    postData.bOids = query.bOids;
		    postData.dateType = query.dateType;
		    postData.dayOffset = query.dayOffset;
		    postData.benchmarkCategory = query.benchmarkCategory;
		    postData.processId = query.processId;
		    postData.state = query.state;
		    postData.activityId = query.activityId;
		    
		    var activityList = $resource(restUrl, {

		    }, {
			fetch : {
			    method : 'POST'
			}
		    });

		    return activityList.fetch({}, postData).$promise;
		};


		/*
		 * 
		 */
		ActivityInstanceService.prototype.getDataMappings = function(oids) {
			return sdUtilService.ajax(REST_BASE_URL, "dataMappings", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.getInData = function(oids) {
			return sdUtilService.ajax(REST_BASE_URL, "inData", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.getTrivialManualActivitiesDetails = function(oids) {
			return sdUtilService.ajax(REST_BASE_URL, "trivialManualActivitiesDetails", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.completeAll = function(activities) {
			return sdUtilService.ajax(REST_BASE_URL, "completeAll", activities);
		};
		
		/*
		 * Get/Search participant
		 * 
		 * options = { options: an object with url params, data: query payload }
		 */
		ActivityInstanceService.prototype.getParticipants = function(query) {
			console.log("Getting participants for:");
			console.log(query);
			
			var options = "";
			if (query.options) {
				if (query.options.skip != undefined) {
					options += "&skip=" + query.options.skip;
				}
				if (query.options.pageSize != undefined) {
					options += "&pageSize=" + query.options.pageSize;
				}
				
				if (query.options.filters != undefined) {
					if (query.options.filters.name != undefined) {
						query.data.searchText = query.options.filters.name.textSearch;
					}
					if (query.options.filters.type != undefined) {
						query.data.participantType = query.options.filters.type.like;
					}
				}
			}

			var restUrl = REST_BASE_URL + 'searchParticipants';

			if (options.length > 0) {
				restUrl = restUrl + "?" + options.substr(1);
			}
			
			return sdUtilService.ajax(restUrl, '', query.data);
		};
		
		
		/*
		 */
		ActivityInstanceService.prototype.getMatchingParticpants = function( searchText , maxItems) {
			
			var restUrl = REST_BASE_URL;
			
			var params = "searchAllParticipants/"+searchText+"/"+maxItems;
			
			return sdUtilService.ajax(restUrl, '', params);
		};
		
		
		/*
		 * Expected data in following format:
		 * {
		 *		activities : [OId1, OId2....],
		 *		participantType:'User'
		 *		participant: {
		 *			qualifiedId: string value,
		 *			OID: long value
		 *		},
		 *		activityOutData : {
		 *			param1 : { value },
		 *			param2 : value, ...
		 *		} //This is optional and only required when delegated from activity panel
		 *	}
		 */
		ActivityInstanceService.prototype.delegateActivities = function(data) {
			console.log("Delegating activities...");
			
			var participantType = data.participant.type;
			var participantData = data.participant.OID;
			switch(participantType) {
				case 'USER':
				case 'DEPARTMENT':
					participantData = data.participant.OID;
					break;
				case 'ROLE':
				case 'ORGANIZATION':
					participantData = data.participant.id;
					break;
			}

			var delegateData = {
					activities: data.activities,
					participant: participantData,
					participantType: participantType
			};
			
			return sdUtilService.ajax(REST_BASE_URL, "delegate", delegateData);
		};
		
		
		/**
		 * Expected data 
		 * [{oid : status} , {..},...]
		 * 
		 */
		ActivityInstanceService.prototype.performDefaultDelegate = function(delegateData) {
			return sdUtilService.ajax(REST_BASE_URL, "performDefaultDelegate", delegateData);
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.abortActivities = function( scope, activities) {
			var requestObj = {
					scope : scope,
					activities : activities
			};
			return sdUtilService.ajax(REST_BASE_URL, "abort", requestObj);
			
		};
		
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getStatsForCompletedActivities = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'statistics/completedActivities');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getPendingActivities = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'pendingActivities');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getRoleColumns = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'allRoleColumns');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getStatsForPostponedActivities = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'statistics/postponedActivities');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getParticipantColumns = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'participantColumns');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getCompletedActivityStatsByTeamLead = function( ) {
			return sdUtilService.ajax(REST_BASE_URL, "", 'statistics/completedActivitiesByTeamLead');
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getByOids = function( query, oidsArray ) {
			var oids = oidsArray.join(',');
			var restUrl = REST_BASE_URL + "oids";
		    restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, 'oids='+oids);
		    
			var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);
			if (queryParams.length > 0) {
			    restUrl = sdDataTableHelperService.appendQueryParamsToURL(restUrl, queryParams.substr(1));
			}

			var postData = sdDataTableHelperService.convertToPostParams(query.options);

			return sdUtilService.ajax(restUrl, '', postData);
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.getRelocationTargets = function(activityInstanceOid) {

			return sdUtilService.ajax(REST_BASE_URL, "relocationTargets", activityInstanceOid);
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.relocate = function(activityInstanceOid, targetAactivityId) {

			return sdUtilService.ajax(REST_BASE_URL, activityInstanceOid + "/relocate", {targetActivityId: targetAactivityId}, activityInstanceOid);
		};
	};
})();
