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

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.initialize = function() {
				};
				
				DocumentAssignmentService.prototype.deleteAttachment = function(oid,dataPathId,urn){
					
					var deferred = jQuery.Deferred(),
						rootUrl = location.href.substring(0, location.href.indexOf("/plugins")),
						self = this,
						urlFrag;
					
					urlFrag=+ oid + "/documents/" + dataPathId;
					
					if(dataPathId=="PROCESS_ATTACHMENTS"){
						urlFrag += "/" + urn
					}
					
					jQuery.ajax({
								url : rootUrl + "/services/rest/document-triage/processes/" + urlFrag,
								type : "DELETE",
								contentType : "application/json"
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
				
				DocumentAssignmentService.prototype.setDocumentType = function(docType,proc){
					debugger;
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					
					jQuery.ajax(
							{
								url : rootUrl
										+ "/services/rest/document-triage/documents/"+ proc.processAttachment.uuid +"/document-type",
								type : "PUT",
								data: JSON.stringify(docType),
								contentType : "application/json"
							}).fail(function(err){
								debugger;
								deferred.reject(err);
							}).done(function(data){
								debugger;
								deferred.resolve(data);
							});
					
					return deferred.promise();
					
				};
				
				DocumentAssignmentService.prototype.getDocumentTypes = function(){
					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					
					jQuery.ajax(
							{
								url : rootUrl
										+ "/services/rest/document-triage/document-types.json",
								type : "GET",
								contentType : "application/json"
							}).done(function(data){
								deferred.resolve(data);
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
						modelOid, businessObjectId, primaryKeyField, keyFields) {
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

					var deferred = jQuery.Deferred();
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;

					jQuery
							.ajax(
									{
										url : rootUrl
												+ "/services/rest/document-triage/businessObject/"
												+ modelOid + "/"
												+ businessObjectId
												+ "/instances.json",
										type : "GET",
										contentType : "application/json"
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
				DocumentAssignmentService.prototype.startProcess = function(data) {
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
				DocumentAssignmentService.prototype.addProcessDocument = function(processOID,scannedDocument,dataPathId) {
					var rootUrl = location.href.substring(0, location.href
							.indexOf("/plugins"));
					var self = this;
					var deferred = jQuery.Deferred();
					var data;
					debugger;
					/*
					 * if datapathID == process_attachments
					 * then data is []
					 * else
					 * data is {}
					 * */
					if(dataPathId=="PROCESS_ATTACHMENTS"){
						data={"data" : [scannedDocument]};
					}else{
						data={"data" : scannedDocument};
					}
					jQuery.ajax(
									{
										"url" : rootUrl
												+ "/services/rest/document-triage/processes/" + processOID + "/documents/" + dataPathId,
										"type" : "POST",
										"contentType" : "application/json",
										"data" : JSON.stringify(data)
									}).done(function(result) {
								deferred.resolve(result.processInstances);
							}).fail(function(data) {
								deferred.reject(data);
							});
					
					//this.delayedResolve(deferred, null);

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
