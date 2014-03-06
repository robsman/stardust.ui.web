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
			
			"getProcessHistory" : function(){
				var deferred = $q.defer();
				
				var data={ "parentProcessInstances": [
						                             {
						                               "oid": 1,
						                               "processId": "Process 1",
						                               "processName": "Process 1",
						                               "startTimestamp": 1391720570809,
						                               "state": "Active",
						                               "priority": 0,
						                               "startingUser": {
						                                 "id": "motu",
						                                 "firstName": "Master",
						                                 "lastName": "Of the Universe",
						                                 "name": "Master",
						                                 "eMail": null,
						                                 "description": null
						                               },
						                               "descriptors": {
						                                 "PolicyHolderFirstName": {
						                                   "id": "PolicyHolderFirstName",
						                                   "name": "PolicyHolderFirstName",
						                                   "value": "John"
						                                 },
						                                 "PolicyNumber": {
						                                   "id": "PolicyNumber",
						                                   "name": "PolicyNumber",
						                                   "value": "123"
						                                 },
						                                 "PolicyHolderLastName": {
						                                   "id": "PolicyHolderLastName",
						                                   "name": "PolicyHolderLastName",
						                                   "value": "Doe"
						                                 }
						                               }
						                             },
						                             {
						                               "oid": 2,
						                               "processId": "Process 2",
						                               "processName": "Process 2",
						                               "startTimestamp": 1391720570809,
						                               "state": "Active",
						                               "priority": 0,
						                               "startingUser": {
						                                 "id": "motu",
						                                 "firstName": "Master",
						                                 "lastName": "Of the Universe",
						                                 "name": "Master",
						                                 "eMail": null,
						                                 "description": null
						                               },
						                               "descriptors": {
						                                 "PolicyHolderFirstName": {
						                                   "id": "PolicyHolderFirstName",
						                                   "name": "PolicyHolderFirstName",
						                                   "value": "John"
						                                 },
						                                 "PolicyNumber": {
						                                   "id": "PolicyNumber",
						                                   "name": "PolicyNumber",
						                                   "value": "123"
						                                 },
						                                 "PolicyHolderLastName": {
						                                   "id": "PolicyHolderLastName",
						                                   "name": "PolicyHolderLastName",
						                                   "value": "Doe"
						                                 }
						                               }
						                             }
						                           ],
						                           "selectedProcessInstance": {
						                             "oid": 3,
						                             "processId": "Process 3",
						                             "processName": "Process 3",
						                             "startTimestamp": 1391720570809,
						                             "state": "Active",
						                             "priority": 0,
						                             "startingUser": {
						                               "id": "motu",
						                               "firstName": "Master",
						                               "lastName": "Of the Universe",
						                               "name": "Master",
						                               "eMail": null,
						                               "description": null
						                             },
						                             "descriptors": {
						                               "PolicyHolderFirstName": {
						                                 "id": "PolicyHolderFirstName",
						                                 "name": "PolicyHolderFirstName",
						                                 "value": "John"
						                               },
						                               "PolicyNumber": {
						                                 "id": "PolicyNumber",
						                                 "name": "PolicyNumber",
						                                 "value": "123"
						                               },
						                               "PolicyHolderLastName": {
						                                 "id": "PolicyHolderLastName",
						                                 "name": "PolicyHolderLastName",
						                                 "value": "Doe"
						                               }
						                             }
						                           },
						                           "activityInstances": [
						                             {
						                               "oid": 140,
						                               "criticality": 0.3300052739236111,
						                               "status": "Suspended",
						                               "lastPerformer": null,
						                               "assignedTo": "motu",
						                               "duration": 612785760,
						                               "activityId": "UIMashup",
						                               "activityName": "UI Mashup",
						                               "processId": "AutoAccident",
						                               "processName": "AutoAccident",
						                               "processInstanceOid": 13,
						                               "startTime": 1393434327995,
						                               "lastModificationTime": 1393434402012,
						                               "activatable": true,
						                               "contexts": {
						                                 "externalWebApp": {
						                                   "carnot:engine:ui:externalWebApp:uri": "http://localhost:8080/pepper-test/plugins/mobile-workflow/public/mashupapp/mashup.html",
						                                   "interactionId": "MTR8MTM5MzQzNDQwMjAxMg=="
						                                 }
						                               },
						                               "implementation": "application",
						                               "processInstance": {
						                                 "descriptors": {
						                                   "PolicyNumber": {
						                                     "id": "PolicyNumber",
						                                     "name": "PolicyNumber",
						                                     "value": "333"
						                                   },
						                                   "FirstName": {
						                                     "id": "FirstName",
						                                     "name": "FirstName",
						                                     "value": "Jane"
						                                   },
						                                   "LastName": {
						                                     "id": "LastName",
						                                     "name": "LastName",
						                                     "value": "Doe"
						                                   }
						                                 },
						                                 "documents": [
						                                   {
						                                     "id": "{jcrUuid}dfe74589-3bea-4a86-97cc-a14ad87db8e4",
						                                     "name": "7-Eleven.jpg",
						                                     "contentType": "application/octet-stream",
						                                     "createdTimestamp": 1393625898000,
						                                     "lastModifiedTimestamp": 1393625898000,
						                                     "size": 7208,
						                                     "downloadToken": "ZGwvMS8xMzk0MDQ3MTE0NjI1L3tqY3JVdWlkfWRmZTc0NTg5LTNiZWEtNGE4Ni05N2NjLWExNGFkODdkYjhlNA=="
						                                   }
						                                 ],
						                                 "notes": [
						                                   {
						                                     "content": "aasas",
						                                     "timestamp": 1393625913736,
						                                     "user": {
						                                       "id": "motu",
						                                       "firstName": "Master",
						                                       "lastName": "Of the Universe",
						                                       "name": "Master",
						                                       "eMail": null,
						                                       "description": null
						                                     }
						                                   }
						                                 ]
						                               }
						                             },
						                             {
						                               "oid": 141,
						                               "criticality": 0.3300052739236111,
						                               "status": "Application",
						                               "lastPerformer": null,
						                               "assignedTo": "motu",
						                               "duration": 612785760,
						                               "activityId": "UIMashup",
						                               "activityName": "UI Mashup",
						                               "processId": "AutoAccident",
						                               "processName": "AutoAccident",
						                               "processInstanceOid": 13,
						                               "startTime": 1393434327995,
						                               "lastModificationTime": 1393434402012,
						                               "activatable": true,
						                               "contexts": {
						                                 "externalWebApp": {
						                                   "carnot:engine:ui:externalWebApp:uri": "http://localhost:8080/pepper-test/plugins/mobile-workflow/public/mashupapp/mashup.html",
						                                   "interactionId": "MTR8MTM5MzQzNDQwMjAxMg=="
						                                 }
						                               },
						                               "implementation": "application",
						                               "processInstance": {
						                                 "descriptors": {
						                                   "PolicyNumber": {
						                                     "id": "PolicyNumber",
						                                     "name": "PolicyNumber",
						                                     "value": "333"
						                                   },
						                                   "FirstName": {
						                                     "id": "FirstName",
						                                     "name": "FirstName",
						                                     "value": "Jane"
						                                   },
						                                   "LastName": {
						                                     "id": "LastName",
						                                     "name": "LastName",
						                                     "value": "Doe"
						                                   }
						                                 },
						                                 "documents": [
						                                   {
						                                     "id": "{jcrUuid}dfe74589-3bea-4a86-97cc-a14ad87db8e4",
						                                     "name": "7-Eleven.jpg",
						                                     "contentType": "application/octet-stream",
						                                     "createdTimestamp": 1393625898000,
						                                     "lastModifiedTimestamp": 1393625898000,
						                                     "size": 7208,
						                                     "downloadToken": "ZGwvMS8xMzk0MDQ3MTE0NjI1L3tqY3JVdWlkfWRmZTc0NTg5LTNiZWEtNGE4Ni05N2NjLWExNGFkODdkYjhlNA=="
						                                   }
						                                 ],
						                                 "notes": [
						                                   {
						                                     "content": "aasas",
						                                     "timestamp": 1393625913736,
						                                     "user": {
						                                       "id": "motu",
						                                       "firstName": "Master",
						                                       "lastName": "Of the Universe",
						                                       "name": "Master",
						                                       "eMail": null,
						                                       "description": null
						                                     }
						                                   }
						                                 ]
						                               }
						                             },
						                             {
						                               "oid": 142,
						                               "criticality": 0.3300052739236111,
						                               "status": "Application",
						                               "lastPerformer": null,
						                               "assignedTo": "motu",
						                               "duration": 612785760,
						                               "activityId": "UIMashup",
						                               "activityName": "UI Mashup",
						                               "processId": "AutoAccident",
						                               "processName": "AutoAccident",
						                               "processInstanceOid": 13,
						                               "startTime": 1393434327995,
						                               "lastModificationTime": 1393434402012,
						                               "activatable": true,
						                               "contexts": {
						                                 "externalWebApp": {
						                                   "carnot:engine:ui:externalWebApp:uri": "http://localhost:8080/pepper-test/plugins/mobile-workflow/public/mashupapp/mashup.html",
						                                   "interactionId": "MTR8MTM5MzQzNDQwMjAxMg=="
						                                 }
						                               },
						                               "implementation": "application",
						                               "processInstance": {
						                                 "descriptors": {
						                                   "PolicyNumber": {
						                                     "id": "PolicyNumber",
						                                     "name": "PolicyNumber",
						                                     "value": "333"
						                                   },
						                                   "FirstName": {
						                                     "id": "FirstName",
						                                     "name": "FirstName",
						                                     "value": "Jane"
						                                   },
						                                   "LastName": {
						                                     "id": "LastName",
						                                     "name": "LastName",
						                                     "value": "Doe"
						                                   }
						                                 },
						                                 "documents": [
						                                   {
						                                     "id": "{jcrUuid}dfe74589-3bea-4a86-97cc-a14ad87db8e4",
						                                     "name": "7-Eleven.jpg",
						                                     "contentType": "application/octet-stream",
						                                     "createdTimestamp": 1393625898000,
						                                     "lastModifiedTimestamp": 1393625898000,
						                                     "size": 7208,
						                                     "downloadToken": "ZGwvMS8xMzk0MDQ3MTE0NjI1L3tqY3JVdWlkfWRmZTc0NTg5LTNiZWEtNGE4Ni05N2NjLWExNGFkODdkYjhlNA=="
						                                   }
						                                 ],
						                                 "notes": [
						                                   {
						                                     "content": "aasas",
						                                     "timestamp": 1393625913736,
						                                     "user": {
						                                       "id": "motu",
						                                       "firstName": "Master",
						                                       "lastName": "Of the Universe",
						                                       "name": "Master",
						                                       "eMail": null,
						                                       "description": null
						                                     }
						                                   }
						                                 ]
						                               }
						                             }
						                           ]
						                         };
				
				deferred.resolve(data);
				return deferred.promise;
			},
			
			"getStartableProcesses" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/startable-processes/",
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
				    url: baseServiceUrl + "/process-instances/" + processInstanceOid + "/notes/create",
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
			}
			
		};

		return function(){
			return srvc;
		};

});