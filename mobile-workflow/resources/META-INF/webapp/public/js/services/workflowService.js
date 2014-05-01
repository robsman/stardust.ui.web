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
			
			"getProcessStates" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances/states",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getActivityStates" : function(){
				
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activity-instances/states",
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
			
			"getReportRoot" : function(){
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
			
			"getReportFolder" : function(folderUid){
				var deferred = $q.defer(),
				url = "/folders/";
			
				if(folderUid){
					url=url + folderUid.replace(/[{}]/g, encodeURIComponent);
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
					url=url + folderUid.replace(/[{}]/g, encodeURIComponent);
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
			
			"getActivitesByProcess" : function(procs){
				
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activities?processDefinitionIds=" + procs,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getDescriptorInstersection" : function(procs){
				//TODO: $http - /process-definitions/descriptors
				var deferred = $q.defer(),
					data=[
						{"id" : 0, "name" : "descriptor1"},
						{"id" : 1, "name" : "descriptor2"},
						{"id" : 2, "name" : "descriptor3"},
						{"id" : 3, "name" : "descriptor4"},
						{"id" : 4, "name" : "descriptor5"}];
				
				deferred.resolve(data);
				return deferred.promise;
			},
			
			"getActivitesByProcessIDs" : function(processId){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activities?" + activityOid,
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
			
			"getParticipantMatches" : function(val){
				var deferred = $q.defer(),
					results=[],
					data=[
					      {"name" : "Alan", "type" : "user"},
					      {"name" : "Amy", "type" : "user"},
					      {"name" : "Aaron", "type" : "user"},
					      {"name" : "Anistasia", "type" : "user"},
					      {"name" : "Arthur", "type" : "user"},
					      {"name" : "Anissa", "type" : "user"},
					      {"name" : "Alex", "type" : "user"},
					      {"name" : "Arnold", "type" : "user"},
					      {"name" : "Alexis", "type" : "user"},
					      {"name" : "Anne", "type" : "user"},
					      {"name" : "Alfonse", "type" : "user"},
					      {"name" : "Annie", "type" : "user"},
					      {"name" : "Architect", "type" : "role"},
					      {"name" : "Auditor", "type" : "role"},
					      {"name" : "Accounts", "type" : "role"},
					      {"name" : "Approval", "type" : "role"},
					      {"name" : "Adjuster", "type" : "role"},
					      {"name" : "Analysis", "type" : "role"},
					      {"name" : "Accounts-l2", "type" : "role"},
					      {"name" : "Accounting", "type" : "organization"},
					      {"name" : "Advertising", "type" : "organization"},
					      {"name" : "Asia", "type" : "organization"},
					      {"name" : "Audits - Internal", "type" : "organization"},
					      {"name" : "America - North", "type" : "organization"},
					      {"name" : "America - South", "type" : "organization"},
					      {"name" : "Audits - External", "type" : "organization"}
					];
				
				data.forEach(function(v){
					if(v.name.indexOf(val) > -1){
						console.log("matched:" + v.name + " - " + val);
						results.push(v);
					}
				});
				deferred.resolve(results);
				return deferred.promise;
			},
			
			"getFilteredDocuments" : function(name,start,end,ids){
				var deferred = $q.defer(),
				 	ids=ids.replace(/[{}]/g, encodeURIComponent);
				
				$http({
				    url: baseServiceUrl + "/documents?" + 
				    					  "searchText=" + name +
				    					  "createFromTimestamp=" + start +
				    					  "createToTimestamp=" + end + 
				    					  "documentTypeIds=" + ids,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				
				return deferred.promise;
			},
			
			"getFilteredActivities" : function(start,end,ids,states){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activity-instances?" + 
				    					  "startedFromTimestamp=" + start +
				    					  "&startedToTimestamp=" + end +
				    					  "&activityIds=" + ids + 
				    					  "&states=" + states,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				
				return deferred.promise;
			},
			
			"getFilteredProcesses" : function(start,end,ids,states){
				//process-instances?startedFromTimestamp=&startedToTimestamp=&processDefinitionIds=&states=
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/process-instances?" + 
				    					  "startedFromTimestamp=" + start +
				    					  "&startedToTimestamp=" + end +
				    					  "&processDefinitionIds=" + ids +
				    					  "&states=" + states,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getProcessDefinitions" : function(isStartable){
				var deferred = $q.defer(),
					params="";
				
				if(isStartable==true){
					params="?startable=true";
				}
				
				$http({
				    url: baseServiceUrl + "/process-definitions" + params,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},

			"getDocumentTypes" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/document-types",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				
				return deferred.promise;
			},
			
			"getDocument" :function(docId){
				var deferred = $q.defer(),
				    docId=docId.replace(/[{}]/g, encodeURIComponent);
				
				$http({
				    url: baseServiceUrl + "/documents/" + docId,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				
				return deferred.promise;
			},
			
			"getProcessDocument" : function(docId,processInstanceOid){
				var deferred = $q.defer();
				
				docId=docId.replace(/[{}]/g, encodeURIComponent);
				
				$http({
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid + 
				    	 "/documents/process-attachments/" + docId,
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"getRepositoryDocument" : function(folderUid,documentUid){
				var deferred = $q.defer();
				
				folderUid=folderUid.replace(/[{}]/g, encodeURIComponent);
				documentUid=documentUid.replace(/[{}]/g, encodeURIComponent);
				
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
			
			"setProcessPriority" : function(processInstanceOid,priority){
				var deferred = $q.defer();
				deferred.resolve();
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
			
			"saveMobileSettings" : function(userId,settings){
				var deferred = $q.defer();
				deferred.resolve();
				return deferred.promise;
			},
			
			"saveUserProfile" : function(userId,profile){
				var deferred = $q.defer();
				deferred.resolve();
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
	
	/*Angular window message handling setup*/
	$window.onmessage=function(event){
		
		var $rootScope = angular.element(document).scope(),
			hotInstance = $rootScope.appData.hotActivityInstance;
		
		if(!hotInstance.oid){return;}
			
		if(event.data == "complete"){
			srvc.completeActivity(hotInstance.oid)
				.then(function(){
					$rootScope.$broadcast( 
						"activityStatusChange",
						 {"oid" : hotInstance.oid, "newStatus" : "complete"}
					);
			}).catch();
		}
		else if(event.data == "suspend"){
			srvc.suspendActivity(hotInstance.oid)
				.then(function(){
					$rootScope.$broadcast( 
							"activityStatusChange",
							 {"oid" : hotInstance.oid, 
							  "newStatus" : "suspend"}
					);
			}).catch();
		}
		else if(event.data == "suspendAndSave"){
			srvc.suspendAndSaveActivity(hotInstance.oid)
				.then(function(){
					$rootScope.$broadcast( 
							"activityStatusChange",
							 {"oid" : hotInstance.oid, 
							  "newStatus" : "suspendAndSave"}
					);
			}).catch();
		}	

	};
	
	//window.addEventListener("message", receiveMessage, false);
	
		return function(){
			return srvc;
		};

});