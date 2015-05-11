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
		this.$get = ['$rootScope', '$http', '$q', 'sdDataTableHelperService', function ($rootScope, $http, $q, sdDataTableHelperService) {
			var service = new ActivityInstanceService($rootScope, $http, $q, sdDataTableHelperService);
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
		ActivityInstanceService.prototype.reactivate = function(activityOID) {

		    var data = {activityOID : activityOID}
		    return ajax(REST_BASE_URL,'reactivate' , data);
		};
		
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
			
			return ajax(restUrl, '', query.data);
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
		
		
		/**
		 * Expected data 
		 * [{oid : status} , {..},...]
		 * 
		 */
		ActivityInstanceService.prototype.performDefaultDelegate = function(delegateData) {
			return ajax(REST_BASE_URL, "performDefaultDelegate", delegateData);
		};
		
		/**
		 * 
		 */
		ActivityInstanceService.prototype.abortActivities = function( scope, activities) {
			var requestObj = {
					scope : scope,
					activities : activities
			};
			return ajax(REST_BASE_URL, "abort", requestObj);
			
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
