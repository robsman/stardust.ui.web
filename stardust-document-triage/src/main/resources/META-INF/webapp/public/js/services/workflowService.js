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
	baseServiceUrl = href.substring(0,href.indexOf("/plugins"))+ "/services/rest/document-triage";
	
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
			
		    "getTripCost": function(data){
		    	var deferred = $q.defer(),
    				projectId = 'secure-site-603', modelId = 'trip-estimator';
    	
	    		request2 = gapi.client.request({
	               'path': '/prediction/v1.6/projects/' + projectId + '/trainedmodels/' + modelId + '/predict',
	               'method': 'POST',
	               'body': {
	                   'input': {
	                     'csvInstance': [
	                       data.depDate,"BHM","NYC",data.duration
	                     ]}
		               }
		             });
	    		
		            try{
		            request2.execute(function(resp) {
		            	deferred.resolve(resp);
		            });
		            }catch(err){
		            	deferred.reject(err);
		            }
		            
		            return deferred.promise;
		    },
		    
		    "getClientTemp": function(data){
		    	var deferred = $q.defer(),
	    			projectId = 'secure-site-603', modelId = 'client-temp';
	    	
	    		request2 = gapi.client.request({
	               'path': '/prediction/v1.6/projects/' + projectId + '/trainedmodels/' + modelId + '/predict',
	               'method': 'POST',
	               'body': {
	                   'input': {
	                     'csvInstance': [
	                       "Banking",27,"EMEA","Ambit Omni", "Li Choy"
	                     ]}
		               }
		             });
	    		
		            try{
		            request2.execute(function(resp) {
		            	deferred.resolve(resp);
		            });
		            }catch(err){
		            	deferred.reject(err);
		            }
		            
		            return deferred.promise;
		    },
		    
		    "getDiningPrediction2": function(long,lat,radius){
		    	var deferred = $q.defer(),
		    		projectId = 'secure-site-603', modelId = 'meal-planning';

	    		request2 = gapi.client.request({
	               'path': '/prediction/v1.6/projects/' + projectId + '/trainedmodels/' + modelId + '/predict',
	               'method': 'POST',
	               'body': {
	                   'input': {
	                     'csvInstance': [
	                       "Yes","NYC","Jane Smith",6
	                     ]}
		               }
		             });
	    		
		            try{
		            request2.execute(function(resp) {
		            	deferred.resolve(resp);
		            });
		            }catch(err){
		            	deferred.reject(err);
		            }
		            
		            return deferred.promise;

		    },
		    
		    "getDiningLocation" : function(long,lat,radius){
		    	//hardcoded long lat for new york demo
		    	var url="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.7538624,-73.9768098&sensor=true&key=AIzaSyC96GOaqqSg-QixnqguPSamYlV8yKQwnl4&radius=350&types=restaurant&minprice=4";
		        var deferred = $q.defer();
		    	
		    	$http({
				    "url" : url,
				    "method" : "GET"
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
			
			
			"logout" : function(){
				var deferred = $q.defer();
				
				$http({
				    url: baseServiceUrl + "/logout",
				    method: "POST",
				    data: {}
				}).success(function(data, status, headers, config) {
					deferred.resolve(data);
				}).error(function(data, status, headers, config) {
					deferred.reject(status);
				});
				return deferred.promise;
			},
			
			"baseHref" : baseServiceUrl
			
			
			
			
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