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
			
			"getCurrentPosition" : function(timeout){
			    var deferred = $q.defer();
			    
			    if(!navigator.geolocation){
			      deferred.reject("navigator undefined");
			    }
			  
			    navigator.geolocation.getCurrentPosition(function(pos){
			          deferred.resolve(pos);
		          },
			      function(err){
			          deferred.reject(err);
			      },
			          {timeout: timeout || 10000}
			      );
			  
			  return deferred.promise;
		    },
		    
		    "getAddress" : function(lon,lat){
		    	
		    	var deferred = $q.defer();
		    	
		    	$http({
				    url: "http://nominatim.openstreetmap.org/reverse?format=json" +
				    	 "&lat=" + lat + "&lon=" + lon + "&zoom=18&addressdetails=1",
				    method: "GET"
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
		    	
				return deferred.promise;
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
			},
			
	      "complete" : function(activityInstance, outData){
	         var deferred = $q.defer();
	      
	         $http({
	             url: baseServiceUrl + "/activity-instances/" + activityInstance.oid + "/complete",
	             method: "POST",
	             data: {
	                  "activityInstance" : activityInstance,
	                  "outData" : outData
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