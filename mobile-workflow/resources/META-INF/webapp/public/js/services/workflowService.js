define(["angularjs"],function(angular){
	
	var srvc,
		ngInjector = angular.injector(["ng"]),
	    $http = ngInjector.get("$http"),
	    $q = ngInjector.get("$q"),
	    $window = ngInjector.get("$window"),
	    baseServiceUrl,
	    href;
	
	href=$window.location.href;
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
			
			"getActivityInstance" : function(activityId){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/activity-instance/" + activityId,
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