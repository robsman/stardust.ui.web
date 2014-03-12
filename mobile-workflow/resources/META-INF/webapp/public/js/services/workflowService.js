define(["angularjs"],function(angular){
	
	var srvc,
		ngInjector = angular.injector(["ng"]),
	    $http = ngInjector.get("$http"),
	    $q = ngInjector.get("$q"),
	    $window = ngInjector.get("$window"),    
	    baseServiceUrl,
	    baseUrl,
	    href;
	
	href = $window.location.href;
	baseUrl = href.substring(0,href.indexOf("/plugins"));
	baseServiceUrl = href.substring(0,href.indexOf("/plugins"))+ "/services/rest/mobile-workflow";
	
	srvc = {
			
			"test" : function(){
				return "Hello From Workflow Service";
			},
			
			"login" : function(account,password,partition){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/login",
				    method: "POST",
				    data: {
							"account"   : account,
							"password"  : password,
							"partition" : partition
						}
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"baseHref" : baseServiceUrl,
			
			"activate" : function(activityOid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activity-instances/" + activityOid + "/activation",
				    method: "PUT"
				}).success(function(data, status, headers, config) {
					// SG
					window.currentlyActiveActivityOID = activityOid;
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getWorklist" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/worklist",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"findWorklistItem" : function(processInstanceOid,worklistItems){
				var baseItem={},
					i=0;
				
				itemsLength=worklistItems.length;
				for(i=0;i<itemsLength;i++){
					item=worklistItems[i];
					if(item.processInstanceOid == processInstanceOid){
						baseItem = item;
						break;
					}
				}
				return baseItem;
			},
			
			"getDocuments" : function(processOid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/" + processOid + "/documents",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getRepositoryRoot" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/folders/root",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getRepositoryFolder" : function(folderUid){
				var deferred = $q.defer(),
					url = "/folders/";
				
				if(folderUid){
					url=url + folderUid;
				}
				
				$http({
				    url: baseServiceUrl + url,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					console.log("Error in workflowService.getRepositoryFolder");
					console.log("...dumping data, status, headers, config");
					console.log(data);
					console.log(status);
					console.log(headers);
					console.log(config);
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getRepositoryDocument" : function(folderUid,documentUid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/folders/" + folderUid + "/documents/" + documentUid,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getWorklistCount" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/worklist/count",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getActivityInstance" : function(activityOid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activity-instances/" + activityOid,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getProcessInstance" : function(processInstanceOid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getDocument" : function(docId,processInstanceOid){
				var deferred = $q.defer();
				$http({
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid + 
				    	 "/documents/process-attachments/" + docId,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					console.log("Error in workflowService.getDocument");
					console.log("...dumping data, status, headers, config");
					console.log(data);
					onsole.log(status);
					onsole.log(headers);
					onsole.log(config);
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getDocumentUrl" : function(downloadToken){
				var docUrl = baseUrl + "/dms-content/" + downloadToken;
				return docUrl;
			},
			
			"getNotes" : function(processInstanceOid){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid + "/notes",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"startProcess" : function(processDefinitionId){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/",
				    method: "POST",
				    data: {
							"processDefinitionId" : processDefinitionId
						}
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getProcessHistory" : function(processInstanceOid,selectedProcessInstanceOid){
				
				var deferred = $q.defer(),
					url = "/process-instances/" + processInstanceOid + "/history";
				
				if(selectedProcessInstanceOid){
					url=url + "?selectedProcessInstanceOid=" + selectedProcessInstanceOid;
				}
				
				$http({
				    url: baseServiceUrl + url,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
				
			},
			
			"getStartableProcesses" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-definitions?startable=true",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"createNote" : function(processInstanceOid, content){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid + "/notes",
				    method: "POST",
				    data: {
							"processInstanceOid" : processInstanceOid,
							"content" : content
						}
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			// SG
         "completeActivity" : function(activityInstanceOID) {
            var deferred = $q.defer();
            
            $http({
                url: baseServiceUrl + "/activity-instances/" + activityInstanceOID + "/complete",
                method: "POST",
                data: {}
            }).success(function(data, status, headers, config) {
               deferred.resolve(data);
            }).error(function(data, status, headers, config) {
               deferred.reject(status);
            });
            return deferred.promise;
         },       
         
         "suspendActivity" : function(activityInstanceOID) {
            var deferred = $q.defer();
            
            $http({
                url: baseServiceUrl + "/activity-instances/" + activityInstanceOID + "/suspend",
                method: "POST",
                data: {}
            }).success(function(data, status, headers, config) {
               deferred.resolve(data);
            }).error(function(data, status, headers, config) {
               deferred.reject(status);
            });
            return deferred.promise;
         },       
         
         "suspendAndSaveActivity" : function(activityInstanceOID) {
            var deferred = $q.defer();
            
            $http({
                url: baseServiceUrl + "/activity-instances/" + activityInstanceOID + "/suspendAndSave",
                method: "POST",
                data: {}
            }).success(function(data, status, headers, config) {
               deferred.resolve(data);
            }).error(function(data, status, headers, config) {
               deferred.reject(status);
            });
            return deferred.promise;
         }
			
		};
	
	// SG
	/*
	function receiveMessage(event) {
		if ("complete" == event.data) {
			if (window.currentlyActiveActivityOID) {
				srvc.completeActivity(window.currentlyActiveActivityOID)
				.then(function(e) {
						alert("Activity completed");
					}, function() {
						alert("Activity completion failed");
					});	
			}
		} else if ("suspend" == event.data) {
			if (window.currentlyActiveActivityOID) {
				srvc.suspendActivity(window.currentlyActiveActivityOID)
				.then(function(e) {
						alert("Activity Suspended");
					}, function() {
						alert("Activity suspension failed");
					});	
			}
		} else if ("suspendAndSave" == event.data) {
			if (window.currentlyActiveActivityOID) {
				srvc.suspendAndSaveActivity(window.currentlyActiveActivityOID)
				.then(function(e) {
						alert("Activity saved and suspend");
					}, function() {
						alert("Activity could not be saved and suspended");
					});	
			}
		}
	};*/
	
	/*Angular window message handling setup*/
	$window.onmessage=function(event){
		
		var $rootScope = angular.element(document).scope(),
			hotInstance = $rootScope.appData.hotActivityInstance;
		
		if(!hotInstance.oid){return;}
			
		if(event.data == "complete"){
			srvc.completeActivity(hotInstance.oid)
				.then(function(){
				$rootScope.$broadcast( "activityStatusChange",
									  {"oid" : hotInstance.oid, 
									   "newStatus" : "complete"}
				);
			});
		}
		else if(event.data == "suspend"){
			srvc.suspendActivity(hotInstance.oid)
				.then(function(){
				$rootScope.$broadcast( "activityStatusChange",
									  {"oid" : hotInstance.oid, 
									   "newStatus" : "suspend"}
				);
			});
		}
		else if(event.data == "suspendAndSave"){
			srvc.suspendAndSaveActivity(hotInstance.oid)
				.then(function(){
				$rootScope.$broadcast( "activityStatusChange",
									  {"oid" : hotInstance.oid, 
									   "newStatus" : "suspendAndSave"}
				);
			});
		}	

	};
	
	//window.addEventListener("message", receiveMessage, false);
	
		return function(){
			return srvc;
		};

});