define(["angularjs"],function(angular){
	
	var utils,
		ngInjector = angular.injector(["ng"]),
		$q = ngInjector.get("$q"),
		$http = ngInjector.get("$http");
		
	utils = {
			
			"test" : function(){
				return "Hello From Util Service";
			},
			
			/**
			 * Promise based wrapper for navigator.geolocation.getCurrentPosition
			 * @param timeout
			 * @returns
			 */
			"getCurrentPosition" : function(timeout){
			    var deferred = $q.defer();
			    
			    if(!navigator.geolocation){
			      deferred.reject("navigator undefined");
			    }
			    else{
			    navigator.geolocation.getCurrentPosition(function(pos){
			          deferred.resolve(pos);
		          },
			      function(err){
			          deferred.reject(err);
			      },
			          {timeout: timeout || 10000}
			      );
			    }
			    
			    return deferred.promise;
		    },
			
		    "getAddress" : function(lon,lat){
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
		    
			/**
			 * Trigger a navigateRequest event on a target.
			 * @param root : Obj event will be triggered on.
			 * @param target : target URL to navigate to.
			 * @param data : data to send with the event
			 */
			"navigateTo" : function(root,target,data){
				var deferred = $q.defer();
				$(root).trigger("navigateRequest",{
					"target": target,
					"payload" : data,
					"promise" : deferred.promise
				});
				return deferred;
			},
			
			/**
			 * Generates a UUID as per RFC-4122 section 4.4
			 * @see http://www.ietf.org/rfc/rfc4122.txt
			 * @returns 
			 */
			"generateUUID" : function(){
				  var uuid='xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
				      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
				      return v.toString(16);
				  });
				  return uuid;
			},
			
			/**
			 * Given an activity type, return as class or classes for that type.
			 * @param activityType
			 * @param isSubProcess
			 */
			"getActivityTypeClass" : function(activityType,isSubProcess){
				var cssClass;
				if(isSubProcess){
					activityType="SubprocessActivity";
				}
				switch(activityType){
					case "Auxiliary":
						cssClass="fa-cog ipp-stroked";//TODO update with auxiliary stacked class
						break;
					case "SubprocessActivity":
						cssClass="fa-cogs ipp-stroked";
						break;
					case "ManualActivity":
						cssClass="fa-user ipp-stroked";//TODO update with Interactive stacked class
						break;
					case "ApplicationActivity": 
						cssClass="fa-desktop ipp-stroked"; //TODO update with application stacked class
						break;
					default:
						cssClass="fa-cog";
				}
				return cssClass;
			},
			
			/**
			 * Given a state descriptor, return  corresponding class(s)
			 * for that descriptor.
			 * @param state
			 * @returns
			 */
			"getStateClass" : function(state){
				var cssClass;
				switch(state){
				case "Application":
					cssClass="fa-spinner fa-spin";
					break;
				case "Alive":
					cssClass="fa-spinner fa-spin";
					break;
				case "Completed":
					cssClass="fa-check-square-o";
					break;
				case "Aborted":
					cssClass="fa-times";
					break;
				case "Suspended":
					cssClass="fa-coffee";
					break;
				case "Hibernated":
					cssClass="fa-clock-o";
					break;
				case "Interrupted":
					cssClass="fa-exclamation-triangle";
					break;
				case "Created":
					cssClass="fa-magic";
					break;
				default:
					cssClass="fa-question-circle";
				}
				
				return cssClass;
			}
			
		};

		return function(){
			return utils;
		};

});