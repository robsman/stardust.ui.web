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
				/**
				 * 
				 */
				BenchmarkService.prototype.initialize = function() {
				};

				/**
				 * 
				 */
				BenchmarkService.prototype.getBusinessObjects = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/benchmark/businessObject.json",
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								console.log("=======> Models");
								console.log(result.models);

								deferred.resolve(result.models);
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
				BenchmarkService.prototype.createLabel = function(
						str) {
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

					console.log("Filter String");
					console.log(queryString);

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
					console.log("Model OID: " + modelOid);
					console.log("Business Object ID: " + businessObjectId);
					console.log("Primary Key: " + primaryKey);
					console.log("Business Object Instance:");
					console.log(businessObjectInstance);

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

					console.log("URL");
					console.log(url);

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
