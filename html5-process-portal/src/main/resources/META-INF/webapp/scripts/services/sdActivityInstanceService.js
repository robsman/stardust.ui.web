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
		this.$get = ['$rootScope', '$http', '$q', 'sdDataTableHelperService', function ($rootScope, $http, $q, sdActivityTableUtilService) {
			var service = new ActivityInstanceService($rootScope, $http, $q, sdActivityTableUtilService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function ActivityInstanceService($rootScope, $http, $q, sdDataTableHelperService) {
		var REST_BASE_URL = "services/rest/portal/activity-instances/";
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getAllCounts = function() {
			return ajax(REST_BASE_URL,'' , "allCounts");
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

			return ajax(restUrl, '', postData);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.getDataMappings = function(oids) {
			return ajax(REST_BASE_URL, "dataMappings", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.getInData = function(oids) {
			return ajax(REST_BASE_URL, "inData", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.getTrivialManualActivitiesDetails = function(oids) {
			return ajax(REST_BASE_URL, "trivialManualActivitiesDetails", oids);
		};

		/*
		 * 
		 */
		ActivityInstanceService.prototype.completeAll = function(activities) {
			return ajax(REST_BASE_URL, "completeAll", activities);
		};
		
		/*
		 * Get/Search participant
		 * 
		 * options = { options: an object with url params, data: query payload }
		 */
		ActivityInstanceService.prototype.getParticipants = function(query) {
			console.log("Getting participants for:");
			console.log(query);
			
			var restUrl = REST_BASE_URL;
			
			return ajax(restUrl, "searchParticipants", query.data);
		};
		
		
		/*
		 */
		ActivityInstanceService.prototype.getMatchingParticpants = function( searchText , maxItems) {
			
			var restUrl = REST_BASE_URL;
			
			var params = "searchAllParticipants/"+searchText+"/"+maxItems;
			
			return ajax(restUrl, '', params);
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
			
			return ajax(REST_BASE_URL, "delegate", delegateData);
		};
		
		/*
		 * Get Spawnable Processes
		 * 
		 * id activity id
		 */
		ActivityInstanceService.prototype.getSpawnableProcesses = function(activityProInstanceOids) {
			console.log("Getting spawnable process for:");
			console.log(activityProInstanceOids);
			
			var restUrl = REST_BASE_URL;
			
			return ajax(restUrl, "spawnableProcesses", activityProInstanceOids);
		};
		
		/*
		 *
		 *	{
		 *	  processId: string value,  // qualified id
		 *	  linkComment: string value
		 *	}
		 * 
		 */
		ActivityInstanceService.prototype.switchProcess = function(payload) {
			console.log("Aborting & spawning new process for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL;
			
			return ajax(restUrl, "switchProcess", payload);
		};
		
		/*
		 * Check If Processes are Abortable
		 * 
		 * activityProInstanceOids : activity process instance id
		 */
		ActivityInstanceService.prototype.checkIfProcessesAbortable = function(activityProInstanceOids, abortType) {
			console.log("Calling checkIfProcessesAbortable for:");
			console.log(activityProInstanceOids);
			console.log(" and abort type:");
			console.log(abortType);
			
			var restUrl = REST_BASE_URL;
			
			return ajax(restUrl, 'checkIfProcessesAbortable?type=' + abortType, activityProInstanceOids);
		};
		
		/*
		 * 
		 */
		ActivityInstanceService.prototype.getRelatedProcesses = function(proInstanceOids, matchAny, searchCases) {
			console.log("Calling getRelatedProcesses for:");
			console.log(proInstanceOids);
			
			var restUrl = REST_BASE_URL + 'getRelatedProcesses?';
			
			if (matchAny != undefined) {
				restUrl += '?matchAny=' + matchAny;
			} else {
				restUrl += '?matchAny=false';
			}
			
			if (searchCases != undefined) {
				restUrl += '&searchCases=' + searchCases;
			}
			
			return ajax(restUrl, '', proInstanceOids);
		};
		
		/*
		 *
		 *	{
		 *	  sourceProcessOID: oid,
		 *	  targetProcessOID: oid,
		 *	  linkComment: string value
		 *	}
		 * 
		 */
		ActivityInstanceService.prototype.abortAndJoinProcess = function(payload) {
			console.log("Aborting & joining new process for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL;
			
			return ajax(restUrl, "abortAndJoinProcess", payload);
		};

		/*
		 * 
		 */
		function ajax(restUrl, extension, value) {
			var deferred = $q.defer();

			var type;
			var data;
			if (angular.isObject(value) || angular.isArray(value)) {
				restUrl += extension;
				type = "POST";
				data = JSON.stringify(value);
			} else {
				restUrl += value + "/" + extension;
				type = "GET";
			}
			
			var httpResponse;
			if(type == "GET") {
				httpResponse = $http.get(restUrl);
			} else {
				httpResponse = $http.post(restUrl, data);
			}

			httpResponse.success(function(data){
				deferred.resolve(data);
			}).error(function(data) {
				deferred.reject(data);
			});
			
			return deferred.promise;
		};
	};
})();
