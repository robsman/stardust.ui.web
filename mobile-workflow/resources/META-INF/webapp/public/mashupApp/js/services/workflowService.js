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