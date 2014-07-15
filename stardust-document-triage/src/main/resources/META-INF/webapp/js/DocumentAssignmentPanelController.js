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
					this.queryParameters = Utils.getQueryParameters();

					// TODO Standardized API

					var pattern = "/services/rest/engine/interactions/";
					var encodedId = this.queryParameters["ippInteractionUri"]
							.substring(this.queryParameters["ippInteractionUri"]
									.indexOf(pattern)
									+ pattern.length);
					var decodedId = atob(encodedId || '');
					var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
					var decodedParts = partsMatcher.exec(decodedId);

					this.activityInstanceOid = decodedParts[1];
					this.startProcessDialog = {};
					this.businessObjectFilter = {};
					this.selectedBusinessObjects = [];
					this.mode = "normal";
					this.pageRotation = 0;
					this.zoomFactor = 100;
					this.pageInverted = false;

					var self = this;

					DocumentAssignmentService
							.instance()
							.getScannedDocuments(this.activityInstanceOid)
							.done(
									function(scannedDocuments) {
										self.scannedDocuments = scannedDocuments;

										self.refreshPagesList();

										DocumentAssignmentService
												.instance()
												.getStartableProcesses()
												.done(
														function(
																startableProcesses) {
															self.startableProcesses = startableProcesses;
															self
																	.refreshStartableProcessesTree();
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
									helper : function(event, ui) {
										var scannedDocument = jQuery.data(
												event.currentTarget,
												"scannedDocument");

										return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
												+ scannedDocument.name
												+ " "
												+ self
														.formatTimestamp(scannedDocument.creationTimestamp)
												+ " "
												+ scannedDocument.contentType
												+ "</div>");
									},
									drag : function(event) {
									},
									stop : function(event) {
									}
								});
					}

					if (this.pendingProcessesTree) {
						for (var n = 0; n < this.pendingProcessesTree.length; ++n) {
							if (this.pendingProcessesTree[n].pendingActivityInstance) {
								var pendingActivityInstanceRow = jQuery("#pendingProcessesTreeRow"
										+ n);
								pendingActivityInstanceRow
										.data({
											ui : pendingActivityInstanceRow,
											pendingActivityInstance : this.pendingProcessesTree[n].pendingActivityInstance
										});
								pendingActivityInstanceRow
										.droppable({
											hoverClass : "highlighted",
											drop : function(event, ui) {
												var scannedDocument = jQuery
														.data(ui.draggable[0],
																"scannedDocument");
												var pendingActivityInstance = jQuery
														.data(this,
																"pendingActivityInstance");

												DocumentAssignmentService
														.instance()
														.completeDocumentRendezvous(
																pendingActivityInstance,
																scannedDocument)
														.done(
																function(
																		pendingProcesses) {
																	self.pendingProcesses = pendingProcesses;
																	self
																			.refreshPendingProcessesTree();
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
											tolerance : "pointer"
										});
							} else if (this.pendingProcessesTree[n].specificDocument) {
								var specificDocumentRow = jQuery("#pendingProcessesTreeRow"
										+ n);
								specificDocumentRow
										.data({
											ui : specificDocumentRow,
											specificDocument : this.pendingProcessesTree[n].specificDocument
										});
								specificDocumentRow
										.droppable({
											hoverClass : "highlighted",
											drop : function(event, ui) {
												var scannedDocument = jQuery
														.data(ui.draggable[0],
																"scannedDocument");
												var specificDocument = jQuery
														.data(this,
																"specificDocument");

												DocumentAssignmentService
														.instance()
														.addProcessAttachment()
														.done(
																function() {
																	specificDocument.url = "bla";
																	specificDocument.creationTimestamp = scannedDocument.creationTimestamp;
																	specificDocument.type = scannedDocument.type;

																	self
																			.refreshPendingProcessesTree();
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
											tolerance : "pointer"
										});
							} else if (this.pendingProcessesTree[n].processAttachments) {
								var processAttachmentsRow = jQuery("#pendingProcessesTreeRow"
										+ n);
								processAttachmentsRow
										.data({
											ui : processAttachmentsRow,
											processAttachments : this.pendingProcessesTree[n].processAttachments
										});
								processAttachmentsRow
										.droppable({
											hoverClass : "highlighted",
											drop : function(event, ui) {
												var scannedDocument = jQuery
														.data(ui.draggable[0],
																"scannedDocument");
												var processAttachments = jQuery
														.data(this,
																"processAttachments");

												jQuery("*").css("cursor",
														"wait");

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
																			.refreshPendingProcessesTree();
																	self
																			.safeApply();

																	jQuery("*")
																			.css(
																					"cursor",
																					"default");

																	window
																			.setTimeout(
																					function() {
																						self
																								.bindDragAndDrop();
																					},
																					1000);
																})
														.fail(
																function() {
																	jQuery("*")
																			.css(
																					"cursor",
																					"default");
																});
											},
											tolerance : "pointer"
										});
							}
						}
					}

					for (var n = 0; n < this.startableProcessesTree.length; ++n) {
						if (this.startableProcessesTree[n].specificDocument) {
							var specificDocumentRow = jQuery("#startableProcessesTreeRow"
									+ n);
							specificDocumentRow
									.data({
										ui : specificDocumentRow,
										processDetails : this.startableProcessesTree[n].processDetails,
										specificDocument : this.startableProcessesTree[n].specificDocument
									});
							specificDocumentRow
									.droppable({
										hoverClass : "highlighted",
										drop : function(event, ui) {
											var scannedDocument = jQuery.data(
													ui.draggable[0],
													"scannedDocument");
											var processDetails = jQuery.data(
													this, "processDetails");
											var specificDocument = jQuery.data(
													this, "specificDocument");

											DocumentAssignmentService
													.instance()
													.startProcess(
															scannedDocument,
															processDetails,
															specificDocument)
													.done(
															function(result) {
																self
																		.openStartProcessDialog(
																				result.scannedDocument,
																				result.startableProcess,
																				result.specificDocument);
																self
																		.safeApply();
															}).fail();
										},
										tolerance : "pointer"
									});
						} else if (this.startableProcessesTree[n].processAttachments) {
							var processAttachmentsRow = jQuery("#startableProcessesTreeRow"
									+ n);
							processAttachmentsRow
									.data({
										ui : processAttachmentsRow,
										processDetails : this.startableProcessesTree[n].processDetails,
										processAttachments : this.startableProcessesTree[n].processAttachments
									});
							processAttachmentsRow
									.droppable({
										hoverClass : "highlighted",
										drop : function(event, ui) {
											var scannedDocument = jQuery.data(
													ui.draggable[0],
													"scannedDocument");
											var processDetails = jQuery.data(
													this, "processDetails");

											DocumentAssignmentService
													.instance()
													.startProcess(
															scannedDocument,
															processDetails)
													.done(
															function(result) {
																self
																		.openStartProcessDialog(
																				result.scannedDocument,
																				result.startableProcess);
																self
																		.safeApply();
															}).fail();
										},
										tolerance : "pointer"
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
				DocumentAssignmentPanelController.prototype.rotatePage = function() {
					this.pageRotation += 90;

					jQuery("#pageImage").css("transform",
							"rotate(" + this.pageRotation + "deg)");
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.zoomInPage = function() {
					this.zoomFactor += 10;

					jQuery("#pageImage").css("width", this.zoomFactor + "%");
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.zoomOutPage = function() {
					this.zoomFactor = Math.max(0, this.zoomFactor - 10);

					jQuery("#pageImage").css("width", this.zoomFactor + "%");
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.invertPage = function() {
					if (this.pageInverted) {
						jQuery("#pageImage").css("-webkit-filter", "none");

						this.pageInverted = false;
					} else {
						jQuery("#pageImage").css("-webkit-filter",
								"invert(100%)");

						this.pageInverted = true;
					}
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
				DocumentAssignmentPanelController.prototype.refreshPendingProcessesTree = function() {
					this.pendingProcessesTree = [];

					for (var n = 0; n < this.pendingProcesses.length; ++n) {
						this.pendingProcessesTree.push({
							pendingProcess : this.pendingProcesses[n]
						});

						for (var m = 0; m < this.pendingProcesses[n].pendingActivityInstances.length; ++m) {
							this.pendingProcessesTree
									.push({
										pendingActivityInstance : this.pendingProcesses[n].pendingActivityInstances[m]
									});
						}

						for (var m = 0; m < this.pendingProcesses[n].specificDocuments.length; ++m) {
							this.pendingProcessesTree
									.push({
										specificDocument : this.pendingProcesses[n].specificDocuments[m]
									});
						}

						this.pendingProcessesTree
								.push({
									processAttachments : this.pendingProcesses[n].processAttachments
								});

						for (var m = 0; m < this.pendingProcesses[n].processAttachments.length; ++m) {
							this.pendingProcessesTree
									.push({
										processAttachment : this.pendingProcesses[n].processAttachments[m]
									});
						}
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.refreshStartableProcessesTree = function() {
					this.startableProcessesTree = [];

					for (var n = 0; n < this.startableProcesses.length; ++n) {
						this.startableProcessesTree.push({
							startableProcess : this.startableProcesses[n]
						});

						if (!this.startableProcesses[n].specificDocuments) {
							this.startableProcesses[n].specificDocuments = [];
						}

						for (var m = 0; m < this.startableProcesses[n].specificDocuments.length; ++m) {
							this.startableProcessesTree
									.push({
										processDetails : this.startableProcesses[n],
										specificDocument : this.startableProcesses[n].specificDocuments[m]
									});
						}

						this.startableProcessesTree.push({
							processDetails : this.startableProcesses[n],
							processAttachments : {}
						// Dummy
						});
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
				DocumentAssignmentPanelController.prototype.onBusinessObjectSelectionChange = function() {
					console.log("Business Object Selection Changed");
					console.log(this.selectedBusinessObjects);

					var self = this;

					jQuery("*").css("cursor", "wait");

					DocumentAssignmentService.instance().getPendingProcesses()
							.done(function(pendingProcesses) {
								self.pendingProcesses = pendingProcesses;

								self.refreshPendingProcessesTree();
								self.safeApply();

								jQuery("*").css("cursor", "default");

								window.setTimeout(function() {
									self.bindDragAndDrop();
								}, 1000);
							}).fail(function() {
								jQuery("*").css("cursor", "default");
							});

				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.openStartProcessDialog = function(
						scannedDocument, startableProcess, specificDocument) {
					this.startProcessDialog.scannedDocument = scannedDocument;
					this.startProcessDialog.startableProcess = startableProcess;
					this.startProcessDialog.specificDocument = specificDocument;

					this.startProcessDialog.dialog("option", "width", 400);
					this.startProcessDialog.dialog("option", "modal", true);
					this.startProcessDialog.dialog("open");
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.closeStartProcessDialog = function() {
					this.startProcessDialog.dialog("close");
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.formatTimestamp = function(
						timestamp) {
					return Utils.formatDateTime(timestamp);
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
