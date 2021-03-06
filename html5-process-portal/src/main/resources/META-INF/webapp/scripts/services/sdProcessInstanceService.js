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
		this.$get = ['$resource', '$http','$q', 'sdUtilService','sdDataTableHelperService', 'sdLoggerService',
		             function ( $resource, $http, $q, sdUtilService, sdDataTableHelperService,  sdLoggerService) {
			var service = new ProcessInstanceService($resource, $http, $q, sdUtilService, sdDataTableHelperService, sdLoggerService);
			return service;
		}];
	});

	/*
	 *
	 */
	function ProcessInstanceService( $resource, $http, $q, sdUtilService, sdDataTableHelperService, sdLoggerService) {
		var REST_BASE_URL = sdUtilService.getBaseUrl() + "services/rest/portal/process-instances/";
		
		var trace = sdLoggerService.getLogger("bpm-common.sdProcessInstanceService")
		/**
		 * 
		 */
		ProcessInstanceService.prototype.getBenchmarkDetailsByBenchmarkOid = function(bOid) {
			var deferred = $q.defer();
			
			var restUrl = sdUtilService.getBaseUrl() + "services/rest/portal/benchmark-definitions/run-time/"+bOid;
			
			var result = {}
			
			$resource(restUrl).get().$promise.then(function(data){ 
				result.name  = data.content.name,
				result.categories = data.content.categories
				result.processDefinitions = [];

				var processDefs = [];
				
				angular.forEach(data.content.models, function (model) {
					angular.forEach(model.processDefinitions, function (processDef) {
						processDefs.push(processDef);
					});
				});
				result.processDefinitions = processDefs;
				deferred.resolve(result);
			});
			
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
		ProcessInstanceService.prototype.getProcessByStartingActivityOid = function(aiOid) {
			var restUrl = REST_BASE_URL+"startingActivityOID/"+ aiOid;
			return $resource(restUrl).get().$promise;
		};

		
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
		    postData.drillDownType = query.drillDownType;
            if(query.drillDownType == "PROCESS_WORKITEM"){
            	postData.bOids = query.bOids;
    		    postData.dateType = query.dateType;
    		    postData.dayOffset = query.dayOffset;
    		    postData.benchmarkCategory = query.benchmarkCategory;
    		    postData.processIds = query.processIds;
    		    postData.state = query.state;
            }else{
            	postData.oids = query.oids;
            }
		    
		    
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
		 */
		ProcessInstanceService.prototype.getProcessInstanceDocuments = function(oid) {
			var deferred = $q.defer();
			
			var restUrl = REST_BASE_URL + oid + "/documents";
			
			$http({method: 'GET', url: restUrl}).
			  success(function(data, status, headers, config) {
				  deferred.resolve(data);
			  }).
			  error(function(data, status, headers, config) {
				  // TODO
			  });
			
			return deferred.promise;
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
			trace.log("Aborting & spawning new process for:", payload);
			
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
			trace.debug("Calling checkIfProcessesAbortable for: ",activityProInstanceOids," and abort type:", abortType);
			
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
			
			var restUrl = REST_BASE_URL + 'getRelatedProcesses?';
			
			if (matchAny != undefined) {
				restUrl += 'matchAny=' + matchAny;
			} else {
				restUrl += 'matchAny=false';
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
			trace.log("Aborting & joining new process for: ", payload);
			
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
		
		/**
		 * get correspondence folder - containing correspondence out folders 
		 */
    ProcessInstanceService.prototype.getCorrespondenceFolder = function(processOid) {
      var url = REST_BASE_URL + processOid + "/correspondence";
      return $resource(url).get().$promise;
    };
		
	};
})();
