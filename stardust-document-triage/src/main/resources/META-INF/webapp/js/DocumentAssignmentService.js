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
				this.nase = "Propase";
				this.pendingProcesses = [ {
					processDefinition : {
						name : "Disability Claim Processing"
					},
					pendingActivityInstances : [ {
						activity : {
							name : "Receive Doctor's Certificate"
						}
					} ],
					specificDocuments : [ {
						name : "Doctor's Certificate",
						document : null
					}, {
						name : "Pay Slip",
						document : {
							uri : "xyz"
						}
					} ],
					processAttachments : [ {
						creationTimestamp : new Date().getTime()
					}, {
						creationTimestamp : new Date().getTime()
					}, {
						creationTimestamp : new Date().getTime()
					} ]
				}, {
					processInstance : {
						processDefinition : {
							name : "Disability Claim Processing"
						}
					},
					pendingActivityInstances : [ {
						activity : {
							name : "Receive Signed Claim"
						}
					} ],
					specificDocuments : [ {
						name : "Claim",
						document : null
					}, {
						name : "Pay Slip",
						document : {
							uri : "xyz"
						}
					} ],
					processAttachments : [ {
						creationTimestamp : new Date().getTime()
					}, {
						creationTimestamp : new Date().getTime()
					} ]
				} ];

				this.scannedDocuments = [ {
					creationTimestamp : new Date().getTime(),
					pageCount : 5
				}, {
					creationTimestamp : new Date().getTime(),
					pageCount : 3
				} ];

				this.startableProcesses = [ {
					name : "Disability Claim Processing",
					specificDocuments : [ {
						name : "Doctor's Certificate"
					}, {
						name : "Pay Slip"
					} ]
				} ],

				this.businessObjects = [ {
					memberId : "4711",
					firstName : "Haile",
					lastName : "Selassie"
				}, {
					memberId : "0815",
					firstName : "Jan",
					lastName : "Smuts"
				} ];

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.initialize = function() {
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getScannedDocuments = function() {
					var deferred = jQuery.Deferred();

					this.delayedResolve(deferred, this.scannedDocuments);

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
									}).done(function() {
								deferred.resolve();
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

					this.delayedResolve(deferred, this.startableProcesses);

					return deferred.promise();
				};

				/**
				 * 
				 */
				DocumentAssignmentService.prototype.getBusinessObjects = function() {
					var deferred = jQuery.Deferred();

					this.delayedResolve(deferred, this.businessObjects);

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
