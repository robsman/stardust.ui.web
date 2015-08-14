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
 * @author Nikhil.Gahlot
 */

(function(){
	'use strict';

	angular.module('workflow-ui.services').provider('sdProcessInstanceService', function () {
		this.$get = ['$rootScope', '$resource', '$filter', 'sdUtilService','sdDataTableHelperService','$q', function ($rootScope, $resource, $filter, sdUtilService, sdDataTableHelperService, $q) {
			var service = new ProcessInstanceService($rootScope, $resource, $filter, sdUtilService, sdDataTableHelperService, $q);
			return service;
		}];
	});

	/*
	 *
	 */
	function ProcessInstanceService($rootScope, $resource, $filter, sdUtilService, sdDataTableHelperService, $q) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/process-instances/";
		
		
		/**
		 * 
		 */
		ProcessInstanceService.prototype.getBenchmarkCategories = function(bOid) {
			var deferred = $q.defer();
			
			var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/benchmark-definitions/run-time/"+bOid;
			
			$resource(restUrl).get().$promise.then(function(data){ 
				var result = {
						name  :  data.content.name,
						categories : data.content.categories
				}
				deferred.resolve(result);
			});;
			return deferred.promise;
		}
		
		/**
		 * 
		 */
		ProcessInstanceService.prototype.getProcessByOid = function(oid, fetchDescriptors) {
			var restUrl = REST_BASE_URL+ oid;
			if(fetchDescriptors) {
				restUrl = sdDataTableHelperService.appendQueryParamsToURL("fetchDescriptors="+fetchDescriptors)
			}
			
			return $resource(restUrl).get().$promise;
		}
		
		/**
		 * 
		 */
		ProcessInstanceService.prototype.getProcessByStartingActivityOid = function(aiOid, sync) {
			var restUrl = REST_BASE_URL+"startingActivityOID/"+ aiOid;

			if(!sync) {
				return $resource(restUrl).get().$promise;
			} else {
				 return sdUtilService.syncAjax(restUrl);
			}
		}

		
		/*
		 * 
		 */
		ProcessInstanceService.prototype.getProcesslist = function(query) {
		    var restUrl = REST_BASE_URL + "search";

		    // Add Query String Params. TODO: Can this be sent as stringified
		    // JSON?
		    var queryParams = sdDataTableHelperService.convertToQueryParams(query.options);

		    if (queryParams.length > 0) {
			var separator = "?";
			if (/[?]/.test(restUrl)) {
			    separator = "&";
			}
			restUrl = restUrl + separator + queryParams.substr(1);
		    }
		    var postData = sdDataTableHelperService.convertToPostParams(query.options);

		    var processList = $resource(restUrl, {

		    }, {
			fetch : {
			    method : 'POST'
			}
		    });

		    return processList.fetch({}, postData).$promise;
		};
		
		
		/*
		 * 
		 */
		ProcessInstanceService.prototype.getProcesslistForTLV = function(query) {
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
		    
		    var processList = $resource(restUrl, {

		    }, {
			fetch : {
			    method : 'POST'
			}
		    });

		    return processList.fetch({}, postData).$promise;
		};


		/*
		 * 
		 */
		ProcessInstanceService.prototype.getProcessInstanceCounts = function(query) {
			var restUrl = REST_BASE_URL + "allCounts";
			var processCounts = $resource(restUrl);
			return processCounts.get().$promise;
		};
		

		/**
		 * 
		 */
		ProcessInstanceService.prototype.abortProcesses = function(scope, processes) {
			var restUrl = REST_BASE_URL + "abort";
			var requestObj = {
				scope : scope,
				processes : processes
			};

			var processList = $resource(restUrl, {}, {
				abort : {
					method : 'POST'
				}
			});

			return processList.abort({}, requestObj).$promise;
		};
		
		/**
		 * 
		 */
		ProcessInstanceService.prototype.recoverProcesses = function(processes) {
			var restUrl = REST_BASE_URL + "recover";
			var res = $resource(restUrl, {}, {
				recover : {
					method : 'POST'
				}
			});

			return res.recover({}, processes).$promise;
		};
		
		/*
		 *
		 *	{
		 *	  sourceProcessOIDs: [oid],
		 *	  targetProcessOID: oid,
		 *	}
		 * 
		 */
		ProcessInstanceService.prototype.attachToCase = function(payload) {
			console.log("Attaching to case for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL + 'attachToCase';
			
			var res = $resource(restUrl, {}, {
				attachToCase : {
					method : 'POST'
				}
			});

			return res.attachToCase({}, payload).$promise;
		};
		
		/*
		 *
		 *	{
		 *	  sourceProcessOIDs: [oid],
		 *	  caseName: String,
		 *	  description: String,
		 *    note: String
		 *	}
		 * 
		 */
		ProcessInstanceService.prototype.createCase = function(payload) {
			console.log("Creating case for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL + 'createCase';
			
			var res = $resource(restUrl, {}, {
				createCase : {
					method : 'POST'
				}
			});

			return res.createCase({}, payload).$promise;
		};
		
		/*
		 * Get Spawnable Processes for switchspwan
		 */
		ProcessInstanceService.prototype.getSpawnableProcesses = function() {
			console.log("Getting spawnable process");
			var restUrl = REST_BASE_URL + ':type';
			var urlTemplateParams = {};
			urlTemplateParams.type = "spawnableProcesses";
			return $resource(restUrl).query(urlTemplateParams).$promise;
		};
		
		/*
		 *
		 *	{
		 *	  processId: string value,  // qualified id
		 *	  linkComment: string value
		 *	}
		 * 
		 */
		ProcessInstanceService.prototype.switchProcess = function(payload) {
			console.log("Aborting & spawning new process for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL + 'switchProcess';
			var res = $resource(restUrl, {}, {
				switchProcess : {
					method : 'POST',
					isArray: true
				}
			});
			
			return res.switchProcess({}, payload).$promise;
		};
		
		/*
		 * Check If Processes are Abortable
		 * 
		 * activityProInstanceOids : activity process instance id
		 */
		ProcessInstanceService.prototype.checkIfProcessesAbortable = function(activityProInstanceOids, abortType) {
			console.log("Calling checkIfProcessesAbortable for:");
			console.log(activityProInstanceOids);
			console.log(" and abort type:");
			console.log(abortType);
			
			var restUrl = REST_BASE_URL + 'checkIfProcessesAbortable?type=' + abortType;
			var res = $resource(restUrl, {}, {
				checkIfProcessesAbortable : {
					method : 'POST',
					isArray: true
				}
			});
			return res.checkIfProcessesAbortable({}, activityProInstanceOids).$promise;
		};
		
		/*
		 * 
		 */
		ProcessInstanceService.prototype.getRelatedProcesses = function(proInstanceOids, matchAny, searchCases) {
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
			
			var res = $resource(restUrl, {}, {
				getRelatedProcesses : {
					method : 'POST',
					isArray: true
				}
			});
			
			return res.getRelatedProcesses({}, proInstanceOids).$promise;
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
		ProcessInstanceService.prototype.abortAndJoinProcess = function(payload) {
			console.log("Aborting & joining new process for:");
			console.log(payload);
			
			var restUrl = REST_BASE_URL + 'abortAndJoinProcess';
			
			var res = $resource(restUrl);
			
			return res.save({}, payload).$promise;
		};
		
		
		/*
		 * 
		 */
		ProcessInstanceService.prototype.getProcessColumns = function() {
			var restUrl = REST_BASE_URL + "allProcessColumns";
			var processes = $resource(restUrl);
			return processes.query().$promise;
		};
	};
})();
