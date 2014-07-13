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
				this.pendingActivities = [ {
					activityInstance : {
						activity : {
							name : "Receive Doctor's Certificate"
						},
						processInstance : {
							processDefinition : {
								name : "Disability Claim Processing"
							}
						}
					},
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
					activityInstance : {
						activity : {
							name : "Receive Signed Claim"
						},
						processInstance : {
							processDefinition : {
								name : "Disability Claim Processing"
							}
						}
					},
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
				DocumentAssignmentService.prototype.getPendingActivities = function() {
					var deferred = jQuery.Deferred();

					this.delayedResolve(deferred, this.pendingActivities);

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
