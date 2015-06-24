/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Zachary Z McCain
 */

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
		
		//Record rest Common Benchmark Definition component for calls into common
		this.portalCommon = "/services/rest/portal";
		
		//Benchmark Definitions Portal Component EndPoint 
		this.portalBDComponent = "/benchmark-definitions";
		
		//TODO: plugins is not part of base url, find out why and fix
		this.absRoot = this.absUrl.substring(0,this.absUrl.indexOf("/main.html#"));
		
		this.rootUrl = this.absRoot + "/services/rest/benchmark";
		
	}
	
	/**
	 * Posts a benchmark to published state where it is now available to the system.
	 */
	benchmarkService.prototype.publishBenchmark = function(id){
		var url,
			data = {id: ''},
			deferred = this.$q.defer();	
		
		url = url = this.absRoot + this.portalCommon + this.portalBDComponent + '/run-time';
		data.id = id;
		
		this.$http.post(url,JSON.stringify(data))
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(data);
		});
		
		return deferred.promise;
	};
	
	/**
	 * Save an already created benchmark to the server.
	 * Creates a deep copy and cleans transient properties from the JSON
	 * before the PUT operation is executed.
	 */
	benchmarkService.prototype.saveBenchmarks = function(benchmark){
		var url,
			deferred;
		
		url = url = this.absRoot + this.portalCommon + this.portalBDComponent + '/design-time';
		url += "/" + benchmark.id;
		deferred = this.$q.defer();
		
		this.$http.put(url,JSON.stringify(benchmark))
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(data);
		});
		
		return deferred.promise;
	};
	
	/**
	 * Retrieve all benchmark definitions from rest-common
	 * @param filter - [('design' | 'publish')], defaults to design if undefined
	 * @param id - optional
	 */
	benchmarkService.prototype.getBenchmarkDefinitions = function(filter,id){
		
		var url,
			endPoint,
			deferred = this.$q.defer();
		
		filter=filter.toUpperCase();
		filter = filter ? filter : 'DESIGN'; //design || publish
		
		endPoint = filter==='DESIGN' ? '/design-time' : '/run-time';
		
		url = this.absRoot + this.portalCommon + this.portalBDComponent + endPoint;

		
		if(id){
			url += "/" + id;
		}
		
		this.$http.get(url)
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(err);
		});
	
		return deferred.promise;
	}
	
	
	benchmarkService.prototype.deleteBenchmark = function(id){
		var url,
			deferred = this.$q.defer();
	
		url = this.absRoot + this.portalCommon + this.portalBDComponent + '/design-time';
		url += "/" + id;
		
		this.$http["delete"](url)
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(err);
		});
		
		return deferred.promise;
	};
	
	/**
	 * Creates a new benchmark definition on the servers document repository.
	 * This operation is only valid for design time.
	 * @param content
	 * @returns
	 */
	benchmarkService.prototype.createBenchmarkDefinition = function(content){
		var url,
			deferred = this.$q.defer();
		
		url = this.absRoot + this.portalCommon + this.portalBDComponent + '/design-time';

		this.$http.post(url,JSON.stringify(content))
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(err);
		});
		
		return deferred.promise;
	};
	
	/**
	 * Retrieve calendars from the calendar plugin.
	 * @param pluginId - Id of the calendar plugin, defaults to 'timeOffCalendar'
	 */
	benchmarkService.prototype.getCalendars = function(pluginId){
		var url,
			deferred = this.$q.defer();
		
		pluginId = pluginId || 'timeOffCalendar';
		
		url = this.absRoot + '/services/rest/business-calendar/groups/' + pluginId + ".json";
	
		this.$http.get(url)
		.success(function(data){
			deferred.resolve(data);
		})
		.error(function(err){
			deferred.reject(err);
		});
	
		return deferred.promise;
		
	}
	
	/**
	 * Retrieve all deployed models.
	 * @returns
	 */
	benchmarkService.prototype.getModels = function(){
		var deferred = this.$q.defer(),
			//url = this.rootUrl + "/models.json";
			url = this.absRoot + this.portalCommon + "/models";
		
		this.$http.get(url)
		.success(function(data){
			deferred.resolve({models: data});
		})
		.error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};
	
	
	/**
	 * retrieve all benchmarks
	 * @returns
	 */
	benchmarkService.prototype.getBenchmarks = function(id){
		var deferred = this.$q.defer(),
		    mockBenchmarks = [];
		
		mockBenchmarks = {"benchmarkDefinitions":[{"metadata":{"author":"motu","lastModifiedDate":1433780896084,"runtimeOid":0},"content":{"id":"d57dc569-f538-4aeb-afce-f4220c26225c","name":"Default Benchmark","description":"","categories":[{"id":"4abf1631-918a-48ce-9369-d8877c692338","name":"Default Category","color":"#00FF00","index":0}],"models":[]}}]};
		
		deferred.resolve(mockBenchmarks);

		return deferred.promise;
	}
	
	/**
	 * retrieve a specific defined benchmarks
	 * @returns
	 */
	benchmarkService.prototype.getBenchmark = function(id){
		var deferred = this.$q.defer(),
		    mockBenchmarks = {};
		
		//TODO: remove mock data and pull from server
		mockBenchmarks.uuid1 = {"benchmark":{"id":"uuid1","name":"General Processing 1","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC","categories":[{"id":1,"name":"On Time","color":"#00FF00","range":{"low":0,"high":333}},{"id":3,"name":"Almost Late","color":"#FFFF00","range":{"low":334,"high":666}},{"id":3,"name":"Late","color":"#FF0000","range":{"low":667,"high":1000}}],"models":[{"id":"modelId","oid":"modelOid","benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},processDefinitions:[{"id":"process-definition Id","oid":1,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[{"id":"activityId","oid":3,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]}}]},{"id":"process-definition Id","oid":2,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[]}]}]}};
		mockBenchmarks.uuid2 = {"benchmark":{"id":"uuid2","name":"General Processing 2","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC","categories":[{"id":1,"name":"On Time","color":"#00FF00","range":{"low":0,"high":333}},{"id":3,"name":"Almost Late","color":"#FFFF00","range":{"low":334,"high":666}},{"id":3,"name":"Late","color":"#FF0000","range":{"low":667,"high":1000}}],"models":[{"id":"modelId","oid":"modelOid","benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},processDefinitions:[{"id":"process-definition Id","oid":1,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[{"id":"activityId","oid":3,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]}}]},{"id":"process-definition Id","oid":2,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[]}]}]}};
		mockBenchmarks.uuid3 = {"benchmark":{"id":"uuid3","name":"General Processing 3","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC","categories":[{"id":1,"name":"On Time","color":"#00FF00","range":{"low":0,"high":333}},{"id":3,"name":"Almost Late","color":"#FFFF00","range":{"low":334,"high":666}},{"id":3,"name":"Late","color":"#FF0000","range":{"low":667,"high":1000}}],"models":[{"id":"modelId","oid":"modelOid","benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},processDefinitions:[{"id":"process-definition Id","oid":1,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[{"id":"activityId","oid":3,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]}}]},{"id":"process-definition Id","oid":2,"benchmarkData":{"enableDueDate":true,"isFreeForm":false,"freeFormScript":"","dueDate":{"dataReference":"businessData","dayType":"businessDays","offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},"categories":[{"categoryId":1,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}},{"categoryId":2,"condition":{"lhs":"Current Time","operator":"NotLaterThan","rhs":"Business Date"},"useBusinessDays":"true","applyOffset":true,"offset":{"amount":10,"unit":"d","offsetTime":"12:01 AM"}}]},"activities":[]}]}]}};
		
		if(mockBenchmarks[id]){
			deferred.resolve(mockBenchmarks[id])
		}
		else{
			deferred.reject(id)
		}
		
		return deferred.promise;
	}
	
	/**
	 * Returns the basic information for all benchmarks without the 
	 * information regarding associated models,processes,and activities.
	 */
	benchmarkService.prototype.getBenchmarkStubs = function(){
		var deferred = this.$q.defer();
		
		//TODO: remove mock data and pull from server.
		var stubs = [{"benchmark":{"id":"uuid1","name":"General Processing 1","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC"}},
		             {"benchmark":{"id":"uuid2","name":"General Processing 2","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC"}},
		             {"benchmark":{"id":"uuid3","name":"General Processing 3","description":"General Benchmarks","author":"motu","lastModified":"2015-03-12 00:00 | UTC","validFrom":"2015-03-12 00:00 | UTC"}}];
		
		deferred.resolve(stubs);
		return deferred.promise;
	}
	
	//our dependencies injected by the Angular DI system.
	benchmarkService.$inject = ["$q", "$http", "$location"];
	
	angular.module("benchmark-app.services")
	.service("benchmarkService",benchmarkService);
	
})();