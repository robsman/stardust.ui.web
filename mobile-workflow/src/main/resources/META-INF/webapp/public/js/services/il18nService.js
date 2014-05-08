define(["angularjs"],function(angular){
	
	var srvc,
		ngInjector = angular.injector(["ng"]),
	    $http = ngInjector.get("$http"),
	    $q = ngInjector.get("$q"),
	    $window = ngInjector.get("$window"),    
	    baseServiceUrl,
	    baseUrl,
	    propertyHashMap,
	    href;
	
	href = $window.location.href;
	baseUrl = href.substring(0,href.indexOf("/plugins"));
	baseServiceUrl = href.substring(0,href.indexOf("/plugins"))+ "/services/rest/mobile-workflow";
	
	var getLanguageCode =function(){
		var deferred = $q.defer();
		
		$http({
		    //url: baseServiceUrl + "/" + (new Date()).getTime() + "/language",
			url: baseServiceUrl + "/language",
		    method: "GET"
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		
		return deferred.promise;
	};
	
	var getProperties = function(p_lang){
		var deferred = $q.defer();
		console.log("HTTP.....................................GetProps");
		$http({
		    //url: baseServiceUrl + "/" + (new Date()).getTime() + "/mobile-workflow-client-messages/" + p_lang,
		    url: baseServiceUrl + "/mobile-workflow-client-messages/" + p_lang,
		    method: "GET"
		}).success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		
		return deferred.promise;
	};
	
	var replaceUicodeChars=function(str) {
		var index = -1;
		if ((index = str.search(/\\u([a-fA-F0-9]){4}/)) >= 0) {
			return replaceUicodeChars(str.substring(0, index) + String.fromCharCode(parseInt(str.substring(index + 2, index + 6), 16)) + str.substring(index + 6));
		} else {
			return str;
		}
	};
	
	var lTrim = function(val)
	{
		return val.replace(/\s*((\S+\s*)*)/, "$1");
	};
	
	var tTrim =function(val)
	{
		return val.replace(/((\s*\S+)*)\s*/, "$1");
	};
	
	var trim=function(val)
	{
		return lTrim(tTrim(val));
	};
	
	var populatePropertiesMap=function(val) {
		var deferred = $q.defer(),
		    propertiesMap = {},
		    propText;
		
		propText = val;
		if (propText && "" != propText)
		{
		    var lines = propText.split(/\r\n|\r|\n/);
		    for (var i = 0; i < lines.length; i++) {
		    	var keyVals = lines[i].split('=');
		    	if (keyVals && "" != keyVals
		    			&& keyVals.length == 2
		    			&& trim(keyVals[0]).indexOf('#') != 0) {
		    		propertiesMap[trim(keyVals[0])] = replaceUicodeChars(keyVals[1]);	
		    	}
		    }
		}	
		deferred.resolve(propertiesMap);
		return deferred.promise;
	};
	
	
	var  init =function() {
		var deferred = $q.defer();
		getLanguageCode()
			.then(function(data){
				data = data || "en";
				return getProperties(data);
			})
			.then(function(data){
				return populatePropertiesMap(data);
			})
			.then(function(data){
				deferred.resolve(data);
			})
			.catch(deferred.reject);
		
		return deferred.promise;
	}
	

	
	var srvc = function(){
		
		var that = this;
		
		var $$getProperty = function(key,defaultValue){
			if(!that.hashMap){
				return defaultValue || key;
			}
			else if(that.hashMap[key]){
				return that.hashMap[key];
			}
			else{
				return defaultValue || key;
			}
		};
		
		this.hashMap = undefined;
		
		this.init = function(){
			var deferred = $q.defer();
			
			init()
				.then(function(data){
					that.hashMap=data;
				})
				.finally(deferred.resolve);
			
			return deferred.promise;
		};
		
		this.getProperty=function(key,defaultValue){
			if(!that.hashMap){
				init()
					.then(function(data){
						that.hashMap=data;
					})
					.finally(function(){
						return $$getProperty(key,defaultValue);
					});
			}
			else{
				return $$getProperty(key,defaultValue);
			}
		};
	}
	
	var instance = new srvc();
	
	return function(){
		return instance;
	};
	
	
});