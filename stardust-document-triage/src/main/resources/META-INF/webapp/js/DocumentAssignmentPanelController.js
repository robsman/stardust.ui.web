/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "document-triage/js/Utils",
				"document-triage/js/DocumentAssignmentService" ],
		function(Utils, DocumentAssignmentService) {
			return {
				create : function() {
					var controller = new DocumentAssignmentPanelController();

					return controller;
				}
			};

			/**
			 * 
			 */
			function DocumentAssignmentPanelController() {
				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.initialize = function() {
					this.businessObjectFilter = {};
					this.selectedBusinessObjects = [];
					this.mode = "normal";

					var self = this;

					DocumentAssignmentService
							.instance()
							.getScannedDocuments()
							.done(
									function(scannedDocuments) {
										self.scannedDocuments = scannedDocuments;

										self.refreshPagesList();
										DocumentAssignmentService
												.instance()
												.getPendingActivities()
												.done(
														function(
																pendingActivities) {
															self.pendingActivities = pendingActivities;

															self
																	.refreshPendingActivitiesTree();
															self.safeApply();

															window
																	.setTimeout(
																			function() {
																				self
																						.bindDragAndDrop();
																			},
																			1000);
														}).fail();
									}).fail();
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.refreshPagesList = function() {
					for (var n = 0; n < this.scannedDocuments.length; ++n) {
						this.scannedDocuments[n].pages = [];

						for (var m = 0; m < this.scannedDocuments[n].pageCount; ++m) {
							this.scannedDocuments[n].pages.push({
								number : m + 1
							});
						}
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.bindDragAndDrop = function() {
					var self = this;

					for (var m = 0; m < this.scannedDocuments.length; ++m) {
						var scannedDocumentDivision = jQuery("#scannedDocumentDivision"
								+ m);
						scannedDocumentDivision.data({
							ui : scannedDocumentDivision,
							scannedDocument : this.scannedDocuments[m]
						});
						scannedDocumentDivision
								.draggable({
									distance : 20,
									opacity : 0.7,
									cursor : "move",
									cursorAt : {
										top : 0,
										left : 0
									},
									helper : function(event) {
										return jQuery("<div class='ui-widget-header dragHelper'>Bla Barbara<i class='fa fa-files'></i></div>");
									},
									drag : function(event) {
									},
									stop : function(event) {
									}
								});
					}

					for (var n = 0; n < this.pendingActivitiesTree.length; ++n) {
						if (this.pendingActivitiesTree[n].specificDocument) {
							var specificDocumentRow = jQuery("#pendingActivitiesTreeRow"
									+ n);
							specificDocumentRow
									.data({
										ui : specificDocumentRow,
										specificDocument : this.pendingActivitiesTree[n].specificDocument
									});
							specificDocumentRow
									.droppable({
										hoverClass : "highlighted",
										drop : function(event, ui) {
											var scannedDocument = jQuery.data(
													ui.draggable[0],
													"scannedDocument");
											var specificDocument = jQuery.data(
													this, "specificDocument");

											DocumentAssignmentService
													.instance()
													.addProcessAttachment()
													.done(
															function() {
																specificDocument.url = "bla";
																specificDocument.creationTimestamp = scannedDocument.creationTimestamp;
																specificDocument.type = scannedDocument.type;

																self
																		.refreshPendingActivitiesTree();
																self
																		.safeApply();

																window
																		.setTimeout(
																				function() {
																					self
																							.bindDragAndDrop();
																				},
																				1000);
															}).fail();
										},
										tolerance : "touch"
									});
						} else if (this.pendingActivitiesTree[n].processAttachments) {
							var processAttachmentsRow = jQuery("#pendingActivitiesTreeRow"
									+ n);
							processAttachmentsRow
									.data({
										ui : processAttachmentsRow,
										processAttachments : this.pendingActivitiesTree[n].processAttachments
									});
							processAttachmentsRow
									.droppable({
										hoverClass : "highlighted",
										drop : function(event, ui) {
											var scannedDocument = jQuery.data(
													ui.draggable[0],
													"scannedDocument");
											var processAttachments = jQuery
													.data(this,
															"processAttachments");

											DocumentAssignmentService
													.instance()
													.addProcessAttachment()
													.done(
															function() {
																processAttachments
																		.push(scannedDocument);

																console
																		.log("Extended Process Attachments");
																console
																		.log(processAttachments);

																self
																		.refreshPendingActivitiesTree();
																self
																		.safeApply();

																window
																		.setTimeout(
																				function() {
																					self
																							.bindDragAndDrop();
																				},
																				1000);
															}).fail();
										},
										tolerance : "touch"
									});
						}
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.selectPage = function(
						page) {
					this.selectedPage = page;

					console.log("Selected Page: ");
					console.log(this.selectedPage);
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.selectionClass = function(
						page) {
					if (this.selectedPage == page) {
						return "selected";
					} else {
						return "unselected";
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.getTreeItemClass = function(
						treeItem) {
					if (treeItem.processAttachments) {
						return "processAttachmentsRow";
					} else if (treeItem.specificDocument) {
						return "specificDocumentRow";
					} else {
						return "";
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.refreshPendingActivitiesTree = function() {
					this.pendingActivitiesTree = [];

					for (var n = 0; n < this.pendingActivities.length; ++n) {
						this.pendingActivitiesTree
								.push({
									pendingActivity : this.pendingActivities[n],
									activityInstance : this.pendingActivities[n].activityInstance
								});

						for (var m = 0; m < this.pendingActivities[n].specificDocuments.length; ++m) {
							this.pendingActivitiesTree
									.push({
										pendingActivity : this.pendingActivities[n],
										specificDocument : this.pendingActivities[n].specificDocuments[m]
									});
						}

						this.pendingActivitiesTree
								.push({
									pendingActivity : this.pendingActivities[n],
									processAttachments : this.pendingActivities[n].processAttachments
								});

						for (var m = 0; m < this.pendingActivities[n].processAttachments.length; ++m) {
							this.pendingActivitiesTree
									.push({
										pendingActivity : this.pendingActivities[n],
										processAttachment : this.pendingActivities[n].processAttachments[m]
									});
						}
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.filterBusinessObjects = function() {
					var self = this;

					DocumentAssignmentService.instance().getBusinessObjects()
							.done(function(businessObjects) {
								self.businessObjects = businessObjects;

								self.safeApply();
							}).fail();
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.formatTimestamp = function(
						timestamp) {
					return Utils.formatDateTime(new Date(timestamp));
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.safeApply = function(
						fn) {
					var phase = this.$root.$$phase;

					if (phase == '$apply' || phase == '$digest') {
						if (fn && (typeof (fn) === 'function')) {
							fn();
						}
					} else {
						this.$apply(fn);
					}
				};
			}
		});
