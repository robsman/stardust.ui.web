(function(){
	
	'use strict';
	
	/**
	 * Constructor for the benchmarkService. Expose required injected
	 * dependencies and set up our URL values we will need.
	 */
	function benchmarkService($q, $http, $location){
		
		//Add dependencies to self so we can reference them in prototypes.
		this.$q = $q;
		this.$http = $http;
		this.$location = $location;
		
		//Calculate URLs we will need.
		this.absUrl = $location.absUrl();
		
		//TODO: plugins is not part of base url, find out why and fix
		this.absRoot =this.absUrl.substring(0,this.absUrl.indexOf("/plugins"));
		
		this.rootUrl = this.absRoot + "/services/rest/benchmark";
		
	}
	
	/**
	 * Retrieve all deployed models.
	 * @returns
	 */
	benchmarkService.prototype.getModels = function(){
		var deferred = this.$q.defer(),
			url = 'http://localhost:8080/ipp82rc1' + this.rootUrl + "/models.json";
		
		this.$http.get(url)
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};
	
	/**
	 * retrieve all user defined benchmarks
	 * @returns
	 */
	benchmarkService.prototype.getBenchmarks = function(){
		var deferred = this.$q.defer();
		
		var mockBenchmarks = [{"benchmark":{"id":"uuid","name":"General Processing","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC","categories":[{"id":1,"name":"On Time","color":"#00FF00","range":{"low":0,"high":333}},{"id":3,"name":"Almost Late","color":"#FFFF00","range":{"low":334,"high":666}},{"id":3,"name":"Late","color":"#FF0000","range":{"low":667,"high":1000}}],"models":[{"id":"modelId","oid":"modelOid","benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"process-definitions":[{"id":"process-definition Id","oid":1,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[{"id":"activityId","oid":3,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]}}]},{"id":"process-definition Id","oid":2,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[]}]}]}}];
		
		deferred.resolve(mockBenchmarks);
		return deferred.promise;
	}
	
	//our dependencies injected by the Angular DI system.
	benchmarkService.$inject = ["$q", "$http", "$location"];
	
	angular.module("benchmark-app.services")
	.service("benchmarkService",benchmarkService);
	
})();