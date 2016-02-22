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
	function benchmarkService($q, $http, $location, sdUtilService){
		
		//Add dependencies to self so we can reference them in prototypes.
		this.$q = $q;
		this.$http = $http;
		this.$location = $location;
		this.sdUtilService = sdUtilService;
		
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
		
		url = this.absRoot + this.portalCommon + this.portalBDComponent + '/run-time';
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
	 * Returns the URL of our file upload endpoint. The service does not handle file upload
	 * as this will be implemented as part of a file upload directive implemented using sdDialog.
	 * See html5-common -> Dialogs -> sdFileUploadDialog
	 * @returns {String}
	 */
	benchmarkService.prototype.getFileUploadUrl = function(){
		return this.absRoot + this.portalCommon + this.portalBDComponent + '/design-time/files';
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
	
	benchmarkService.prototype.__fileDownload = function(benchmark){
		
		if(!benchmark && ! benchmark.content){return;}
		
		var exportAnchor = document.createElement("a");
		var downloadMetaData = 'data:application/csv;charset=utf-8,';
		var downloadUrl = downloadMetaData + encodeURIComponent(JSON.stringify(benchmark));
		var fileName = benchmark.content.id + ".json";
		
		try {
			if (exportAnchor.download != undefined) {
				exportAnchor.download = fileName;
				exportAnchor.href = downloadUrl;
				exportAnchor.target = '_blank';

				var mouseEvent = document.createEvent("MouseEvents");
				mouseEvent.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
				exportAnchor.dispatchEvent(mouseEvent);
			} else if (navigator.msSaveBlob) {
				var index = downloadUrl.indexOf(',');
				var contentType = downloadUrl.substr(0, index);
				var data = downloadUrl.substr(index + 1);
				data = decodeURIComponent(data);

				var blob = new Blob([data], {type : contentType});
				navigator.msSaveBlob(blob, fileName);
			} else {
				//not supported
			}
		} catch (e) {
			trace.error(theTableId + ': Failed to download as file', e);
		}
	}
	
	/**
	 * Retrieve the specified benchmark from the server (we don't publish unsaved design-time benchmarks)
	 * and save as a file.
	 * @param benchmark
	 * @param mode
	 */
	benchmarkService.prototype.downloadBenchmarkAsFile = function(benchmark,mode){
		var that = this;
		
		if(!benchmark){return;}
		
		this.getBenchmarkDefinitions(mode)
		.then(function(bmarks){
			var bmark = bmarks.benchmarkDefinitions.filter(function(bm){
				return bm.content.id === benchmark.id;
			});
			
			if(bmark.length > 0){
				that.sdUtilService.downloadAsFile(JSON.stringify(bmark[0].content),bmark[0].content.name + ".json",false,document);
			}
		});
	}
	
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
	
	/**
	 * Deletes a run-time benchmark from the document repository
	 * @param id - runtime artifact oid
	 * @returns - promise
	 */
	benchmarkService.prototype.deletePublishedBenchmark = function(runtimeOid){
		var url,
			deferred = this.$q.defer();
	
		url = this.absRoot + this.portalCommon + this.portalBDComponent + '/run-time';
		url += "/" + runtimeOid;
		
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
	 * Deletes a design time benchmark from the document repository
	 * @param id - Benchmark definition id
	 * @returns - promise
	 */
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
			url = this.absRoot + this.portalCommon + "/models?allActive=true";
		
		this.$http.get(url)
		.success(function(data){
			deferred.resolve({models: data});
		})
		.error(function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};
	
	//our dependencies injected by the Angular DI system.
	benchmarkService.$inject = ["$q", "$http", "$location","sdUtilService"];
	
	angular.module("benchmark-app.services")
	.service("benchmarkService",benchmarkService);
	
})();