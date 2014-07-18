/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "document-triage/js/Utils" ],
		function(Utils) {
			return {
				instance : function() {

					if (!document.documentAssignmentService) {
						document.documentAssignmentService = new DocumentAssignmentService();
					}

					return document.documentAssignmentService;
				}
			};

			/**
			 * 
			 */
			function DocumentAssignmentService() {
				this.businessObjects = [ {
					id : "4711",
					firstName : "Haile",
					lastName : "Selassie",
					scheme : 0090001,
					schemeName : 0090002,
					nationalID : 80030300030
				}, {
					id : "0815",
					firstName : "Jan",
					lastName : "Smuts",
					scheme : 0090022,
					schemeName : 0090004,
					nationalID : 677700000
				} ];

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.initialize = function() {
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getBusinessObjects = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/businessObject.json",
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
				DocumentAssignmentService.prototype.getScannedDocuments = function(
						activityInstanceOid) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/activities/"
												+ activityInstanceOid
												+ "/attachments.json",
										type : "GET",
										contentType : "application/json"
									})
							.done(
									function(result) {
										console.log("Process Attachments");
										console.log(result);

										for (var n = 0; n < result.processAttachments.length; ++n) {
											result.processAttachments[n].creationTimestamp = new Date()
													.getTime();
											result.processAttachments[n].pageCount = result.processAttachments[n].numPages;
											result.processAttachments[n].url = rootUrl
													+ "/services/rest/document-triage/documents/"
													+ result.processAttachments[n].uuid
													+ "/";
										}

										deferred
												.resolve(result.processAttachments);
									}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getPendingProcesses = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/processes/documentRendezvous.json",
										type : "POST",
										contentType : "application/json",
										data : JSON.stringify({})
									}).done(function(result) {
								console.log("Result");
								console.log(result.processInstances);

								deferred.resolve(result.processInstances);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.completeDocumentRendezvous = function(
						pendingActivityInstance, document) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/activities/completeRendezvous.json",
										type : "POST",
										contentType : "application/json",
										data : JSON
												.stringify({
													pendingActivityInstance : pendingActivityInstance,
													document : document
												})
									}).done(function(result) {
								deferred.resolve(result.processInstances);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};
				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getStartableProcesses = function() {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/processes/startable.json",
										type : "GET",
										contentType : "application/json"
									}).done(function(result) {
								deferred.resolve(result.processDefinitions);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getBusinessObjectInstances = function(
						primaryKeyField, keyFields) {
					var deferred = jQuery.Deferred();
					var queryString = "?";

					if (primaryKeyField && primaryKeyField.filterValue) {
						queryString += primaryKeyField.id;
						queryString += "=";
						queryString += primaryKeyField.filterValue;
						queryString += "&";

					}

					if (keyFields) {
						for (var n = 0; n < keyFields.length; ++n) {
							if (keyFields[n].filterValue) {
								queryString += keyFields[n].id;
								queryString += "=";
								queryString += keyFields[n].filterValue;
								queryString += "&";

							}
						}
					}

					console.log("Filter String");
					console.log(queryString);

					this.delayedResolve(deferred, this.businessObjects);

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.startProcess = function(
						data) {
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										"url" : rootUrl
												+ "/services/rest/document-triage/processes.json",
										"type" : "PUT",
										"contentType" : "application/json",
										"data" : JSON.stringify(data)
									}).done(function(result) {
								deferred.resolve(result);
							}).fail(function(data) {
								deferred.reject(data);
							});

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.addProcessAttachment = function() {
					var deferred = jQuery.Deferred();

					this.delayedResolve(deferred, null);

					return deferred.promise();
				};

				/**
				 * Simulates a delay to test asynchronous behavior and server
				 * roundtrips.
				 */
				DocumentAssignmentService.prototype.delayedResolve = function(
						deferred, data) {
					window.setTimeout(function() {
						deferred.resolve(data);
					}, 500);
				};

				/**
				 * Simulates a delay to test asynchronous behavior and server
				 * roundtrips.
				 */
				DocumentAssignmentService.prototype.delayedReject = function(
						deferred, data) {
					window.setTimeout(function() {
						deferred.reject(data);
					}, 500);
				};

			}
		});
