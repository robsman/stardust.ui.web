/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "benchmark/js/Utils" ],
		function(Utils) {
			return {
				instance : function() {

					if (!document.BenchmarkService) {
						document.BenchmarkService = new BenchmarkService();
					}

					return document.BenchmarkService;
				}
			};

			/**
			 * 
			 */
			function BenchmarkService() {
				this.benchmarks = [ {
					name : "Criticality",
					categories : [ {
						name : "Normal",
						color : "#00FF00",
						low : 0,
						high : 300
					}, {
						name : "At Risk",
						color : "#FFFB00",
						low : 301,
						high : 500
					}, {
						name : "Critical",
						color : "#FF0000",
						low : 501,
						high : 999
					} ],
					trafficLights : [ {
						process : {
							name : "Daily Fund Processing Europe"
						},
						model : {
							name : "Daily Fund Processing"
						}
					}, {
						process : {
							name : "Daily Fund Processing Europe"
						},
						activity : {
							name : "Sweep and Translate"
						},
						model : {
							name : "Daily Fund Processing"
						}
					}, {
						process : {
							name : "Daily Fund Processing US"
						},
						model : {
							name : "Daily Fund Processing"
						}
					} ]
				}, {
					name : "Standard Funds Processing",
					categories : [ {
						name : "Normal",
						color : "#FF0000"
					}, {
						name : "At Risk",
						color : "#00FFFF"
					}, {
						name : "Critical",
						color : "#0000FF"
					} ],
					trafficLights : [ {
						process : {
							name : "Monthly Tax Report"
						},
						model : {
							name : "Report Dissimination"
						}
					}, {
						process : {
							name : "Monthly Tax Report"
						},
						activity : {
							name : "Retrieve Report Data"
						},
						model : {
							name : "Report Dissimination"
						}
					} ]
				} ];

				/**
				 * 
				 */
				BenchmarkService.prototype.initialize = function() {
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getBenchmarks = function() {
					var deferred = jQuery.Deferred();

					deferred.resolve(this.benchmarks);

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getBenchmark = function(id) {
					var deferred = jQuery.Deferred();

					// TODO Dummy
					
					deferred.resolve(this.benchmarks[0]);

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getModels = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery.ajax({
						url : rootUrl + "/services/rest/benchmark/models.json",
						type : "GET",
						contentType : "application/json"
					}).done(function(result) {
						console.log("Models: " , result.models);
						deferred.resolve(result.models);
					}).fail(function(data) {
						deferred.reject(data);
					});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getActivityInstances = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/activityInstances.json",
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								console.log("Activity: " , result.activityInstances);
								deferred.resolve(result.activityInstances);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getProcessInstance = function(oid) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/processInstances/"
												+ oid + ".json",
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								console.log("Process Instance: " , result);
								deferred.resolve(result);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getBusinessObject = function(
						modelOid, businessObjectId) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/businessObject/"
												+ modelOid + "/"
												+ businessObjectId + ".json",
										type : "GET",
										contentType : "application/json"
									})
							.done(
									function(businessObject) {
										self
												.calculateBusinessObjectFields(businessObject);
										deferred.resolve(businessObject);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.calculateBusinessObjectFields = function(
						businessObject) {
					console.log(businessObject);

					// Calculate primary key field,
					// keyFields and topLevelFields
					// TODO Do on server?

					businessObject.keyFields = [];
					businessObject.topLevelFields = [];

					// Create labels for all used types

					if (businessObject.types) {
						for ( var type in businessObject.types) {
							for (var n = 0; businessObject.types[type].fields
									&& n < businessObject.types[type].fields.length; ++n) {
								businessObject.types[type].fields[n].label = this
										.createLabel(businessObject.types[type].fields[n].name);
							}
						}
					}

					for (var n = 0; n < businessObject.fields.length; ++n) {
						// TODO Retrieve labels from
						// annotations

						businessObject.fields[n].label = this
								.createLabel(businessObject.fields[n].name);

						if (!businessObject.types) {
							businessObject.types = {};
						}

						if (businessObject.types[businessObject.fields[n].type]) {
							continue;
						}

						if (businessObject.fields[n].primaryKey) {
							businessObject.primaryKeyField = businessObject.fields[n];
						} else if (businessObject.fields[n].key) {
							businessObject.keyFields
									.push(businessObject.fields[n]);
						}

						businessObject.topLevelFields
								.push(businessObject.fields[n]);
					}

					console.log(businessObject);
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.createLabel = function(str) {
					return str
					// Insert a space between lower & upper
					.replace(/([a-z])([A-Z])/g, '$1 $2')
					// Space before last upper in a sequence followed by lower
					.replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
					// Uppercase the first character
					.replace(/^./, function(str) {
						return str.toUpperCase();
					})
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getBusinessObjectInstances = function(
						businessObject) {
					var queryString = "?";

					if (businessObject.primaryKeyField
							&& businessObject.primaryKeyField.filterValue) {
						queryString += businessObject.primaryKeyField.id;
						queryString += "=";
						queryString += businessObject.primaryKeyField.filterValue;
						queryString += "&";
					}

					if (businessObject.keyFields) {
						for (var n = 0; n < businessObject.keyFields.length; ++n) {
							if (businessObject.keyFields[n].filterValue) {
								queryString += businessObject.keyFields[n].id;
								queryString += "=";
								queryString += businessObject.keyFields[n].filterValue;
								queryString += "&";
							}
						}
					}

					console.log("Filter String : " , queryString);

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;
					var url = rootUrl
							+ "/services/rest/benchmark/businessObject/"
							+ businessObject.modelOid + "/" + businessObject.id
							+ "/instance.json" + queryString;

					jQuery.ajax({
						url : url,
						type : "GET",
						contentType : "application/json"
					}).done(function(result) {
						deferred.resolve(result.businessObjectInstances);
					}).fail(function(data) {
						deferred.reject(data);
					});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.createBusinessObjectInstance = function(
						modelOid, businessObjectId, primaryKey,
						businessObjectInstance) {
					console.log("Model OID: " , modelOid);
					console.log("Business Object ID: " , businessObjectId);
					console.log("Primary Key: " , primaryKey);
					console.log("Business Object Instance: " , businessObjectInstance);

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instance/" + primaryKey
												+ ".json",
										type : "PUT",
										contentType : "application/json",
										data : JSON
												.stringify(businessObjectInstance)
									})
							.done(
									function(result) {
										deferred
												.resolve(result.businessObjectInstances);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.updateBusinessObjectInstance = function(
						modelOid, businessObjectId, primaryKey,
						businessObjectInstance) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instance/" + primaryKey
												+ ".json",
										type : "POST",
										contentType : "application/json",
										data : JSON
												.stringify(businessObjectInstance)
									})
							.done(
									function(result) {
										deferred
												.resolve(result.businessObjectInstances);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * Retrieves a checklist process instance
				 */
				BenchmarkService.prototype.getChecklists = function(processId,
						businessObjectName, businessObjectValue) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));

					var queryString = processId ? ("?processId=" + processId)
							: "";

					if (businessObjectName) {
						queryString += "&businessObjectName="
								+ businessObjectName;
						queryString += "&businessObjectValue="
								+ businessObjectValue;
					}

					console.log("Query-String: " , queryString);

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/simple-modeler/checklist.json"
												+ queryString,
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								deferred.resolve(result.checklists);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getProcessInstances = function(
						businessObject, businessObjectInstance) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;
					var url = rootUrl
							+ "/services/rest/benchmark/businessObject/"
							+ businessObject.modelOid
							+ "/"
							+ businessObject.id
							+ "/"
							+ businessObjectInstance[businessObject.primaryKeyField.id]
							+ "/" + "processInstances.json";

					console.log("URL: " , url);

					jQuery.ajax({
						url : url,
						type : "GET",
						contentType : "application/json"
					}).done(function(result) {
						deferred.resolve(result);
					}).fail(function(data) {
						deferred.reject(data);
					});

					return deferred.promise();
				};
			}
		});
