/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[
				"document-triage/js/Utils",
				"business-object-management/js/BusinessObjectManagementPanelController",
				"document-triage/js/DocumentAssignmentService" ],
		function(Utils, BusinessObjectManagementPanelController,
				DocumentAssignmentService) {
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
					this.pageModel={};
					this.pageModel.currentDocument="";
					this.pageModel.pageIndex={};
					this.retrieveActivityInstanceFromUri();
					this.startProcessDialog = {};
					this.businessObjectFilter = {};
					this.selectedBusinessObjectInstances = [];					
					this.initializePageRendering();

					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					var self = this;
										
					DocumentAssignmentService.instance().getDocumentTypes()
					.done(function(docTypes){
						self.documentTypes=docTypes;
					});
					
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
															self.businessObjectManagementPanelController
																	.initialize(
																			self)
																	.done(
																			function() {
																				console.log("===> BO initialized");
																				console.log(self.businessObjectManagementPanelController);
																				
																				self
																						.safeApply();
																				window
																						.setTimeout(
																								function() {
																									self
																											.bindDragAndDrop();
																								},
																								1000);
																			})
																	.fail();
														}).fail();
									}).fail();
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.retrieveActivityInstanceFromUri = function() {
					this.queryParameters = Utils.getQueryParameters();

					// TODO Standardized API
					// TODO: retrieve a proper base url and replace pepper-test
					var pattern = "/services/rest/engine/interactions/";
					var encodedId = this.queryParameters["ippInteractionUri"]
							.substring(this.queryParameters["ippInteractionUri"]
									.indexOf(pattern)
									+ pattern.length);
					var decodedId = atob(encodedId || '');
					var partsMatcher = new RegExp('^(\\d+)\\|(\\d+)$');
					var decodedParts = partsMatcher.exec(decodedId);
					this.activityInstanceOid = decodedParts[1];
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.initializePageRendering = function() {
					this.mode = "normal";
					this.pageRotation = 0;
					this.zoomFactor = 100;
					this.pageInverted = false;
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
											processAttachments : this.pendingProcessesTree[n].processAttachments,
											processDetails : this.pendingProcessesTree[n]
											
										});
								processAttachmentsRow
										.droppable({
											hoverClass : "highlighted",
											drop : function(event, ui) {
												
												var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument");
												var processAttachments = jQuery.data(this,"processAttachments");
												var processDetails = jQuery.data(this,"processDetails");

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
							var specificDocumentRow = jQuery("#startableProcessesTreeRow" + n);
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
											.addProcessAttachment()
											.done(
													function() {
														specificDocument.url = "bla";
														specificDocument.creationTimestamp = scannedDocument.creationTimestamp;
														specificDocument.type = scannedDocument.type;

														self
																.refreshStartableProcessesTree();
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
											
											debugger;
											/*
											var trPrev = jQuery(this), isBranch = false, rootProcTD = jQuery("<td></td>"), startProcBtn, removeProcBtn;
											while (trPrev && !isBranch) {
												if (trPrev
														.hasClass("treeBranch")) {
													isBranch = true;
												} else {
													trPrev = jQuery(trPrev)
															.prev('tr');
												}
											}
											removeProcBtn = jQuery("<i style='color:red;font-size:1.5em;padding-left:1em;' class='fa fa-times-circle'>");
											startProcBtn = jQuery("<i style='color:green;font-size:1.5em;padding-left:0.25em;' class='fa fa-check-circle'>");

											rootProcTD.append(removeProcBtn);
											rootProcTD.append(startProcBtn);

											removeProcBtn.bind("click",
													function() {
														rootProcTD.remove();
													});

											startProcBtn
													.bind(
															"click",
															function() {
																var rawScope = angular
																		.element(
																				"#docRendevous")
																		.scope(), businessObjs = rawScope.selectedBusinessObjects;
																if (businessObjs
																		&& businessObjs.length > 0) {
																	DocumentAssignmentService
																			.instance()
																			.startProcess(
																					scannedDocument,
																					processDetails,
																					specificDocument)
																			.done(
																					function(
																							result) {
																						self
																								.openStartProcessDialog(
																										result.scannedDocument,
																										result.startableProcess,
																										result.specificDocument);
																						self
																								.safeApply();
																					})
																			.fail();
																} else {
																	alert("Please associate a business object with the document.");
																}
															});

											trPrev.append(rootProcTD);
											*/
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
																self
																		.refreshStartableProcessesTree();
																self
																		.safeApply();
																window
																		.setTimeout(
																				function() {
																					self
																							.bindDragAndDrop();
																				},
																				1000);
															});

											return;
											var trPrev = jQuery(this), isBranch = false, rootProcTD = jQuery("<td></td>"), startProcBtn, removeProcBtn;
											while (trPrev && !isBranch) {
												if (trPrev
														.hasClass("treeBranch")) {
													isBranch = true;
												} else {
													trPrev = jQuery(trPrev)
															.prev('tr');
												}
											}

											removeProcBtn = jQuery("<i style='color:red;font-size:1.5em;padding-left:1em;' class='fa fa-times-circle'>");
											startProcBtn = jQuery("<i style='color:green;font-size:1.5em;padding-left:0.25em;' class='fa fa-check-circle'>");

											jQuery(this)
													.append(
															"<tr><td>text here<td><tr>");
											rootProcTD.append(removeProcBtn);
											rootProcTD.append(startProcBtn);

											removeProcBtn.bind("click",
													function() {
														rootProcTD.remove();
													});

											startProcBtn
													.bind(
															"click",
															function() {
																DocumentAssignmentService
																		.instance()
																		.startProcess(
																				scannedDocument,
																				processDetails)
																		.done(
																				function(
																						result) {
																					self
																							.openStartProcessDialog(
																									result.scannedDocument,
																									result.startableProcess);
																					self
																							.safeApply();
																				})
																		.fail();
															});

											trPrev.append(rootProcTD);

										},
										tolerance : "pointer"
									});
						}
					}
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.selectPage = function(page, url, e) {
					this.selectedPage = page;
					if(e.ctrlKey){
						if(this.pageModel.pageIndex.hasOwnProperty(page.number)){
							delete this.pageModel.pageIndex[page.number];
						}else{
							this.pageModel.pageIndex[page.number]=url;
						}
					}
					else if(e.shiftKey){
						//stubbed for later
					}
					else{
						this.pageModel.pageIndex={};
						this.pageModel.pageIndex[page.number]=url;
					}
					console.log(this.pageModel.pageIndex);
					this.selectedPage.url = url;
				};
				
				DocumentAssignmentPanelController.prototype.isPageSelected = function(page){
					
					if(this.pageModel.pageIndex.hasOwnProperty(page.number) &&
					   this.pageModel.pageIndex[page.number]==page.url){
						return true;
					}else{
						return false;
					}
				}

				DocumentAssignmentPanelController.prototype.startProcess = function(
						treeItem, busObj) {
					debugger;
					var that = this;
					var data = {
						"businessObject" : busObj,
						"startableProcess" : treeItem.startableProcess
					};
					console.log(JSON.stringify(data));
					DocumentAssignmentService.instance().startProcess(data)
							.done(
									function(result) {
										that.openStartProcessDialog(
												result.scannedDocument,
												result.startableProcess);
										that.safeApply();
									}).fail();
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

					console.log("Pending Processes");
					console.log(this.pendingProcesses);

					for (var n = 0; n < this.pendingProcesses.length; ++n) {
						this.pendingProcessesTree.push({
							pendingProcess : this.pendingProcesses[n]
						});

						for (var m = 0; m < this.pendingProcesses[n].descriptors.length; ++m) {
							this.pendingProcessesTree
									.push({
										processDescriptor : this.pendingProcesses[n].descriptors[m]
									});
						}

						for (m = 0; m < this.pendingProcesses[n].pendingActivityInstances.length; ++m) {
							this.pendingProcessesTree
									.push({
										pendingActivityInstance : this.pendingProcesses[n].pendingActivityInstances[m]
									});
						}

						for (m = 0; m < this.pendingProcesses[n].specificDocuments.length; ++m) {
							this.pendingProcessesTree
									.push({
										specificDocument : this.pendingProcesses[n].specificDocuments[m]
									});
						}

						this.pendingProcessesTree
								.push({
									processAttachments : this.pendingProcesses[n].processAttachments
								});

						for (m = 0; m < this.pendingProcesses[n].processAttachments.length; ++m) {
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

						if (!this.startableProcesses[n].processAttachments) {
							this.startableProcesses[n].processAttachments = [];
						}

						this.startableProcessesTree
								.push({
									processDetails : this.startableProcesses[n],
									processAttachments : this.startableProcesses[n].processAttachments
								});

						for (m = 0; m < this.startableProcesses[n].processAttachments.length; ++m) {
							this.startableProcessesTree
									.push({
										processDetails : this.startableProcesses[n],
										processAttachment : this.startableProcesses[n].processAttachments[m]
									});
						}
					}
				};
				
				DocumentAssignmentPanelController.prototype.processCanStart = function(proc){
					return this.selectedBusinessObjectInstances.length > 0 && 
						   (proc.startableProcess.processAttachments.length >0 || proc.startableProcess.specificDocuments.length >0 );
				}
				
				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.onBusinessObjectInstanceSelectionChange = function() {
					var self = this;

					jQuery("*").css("cursor", "wait");
					DocumentAssignmentService.instance().getPendingProcesses()
							.done(function(pendingProcesses) {
								self.pendingProcesses = pendingProcesses;
								self.selectedBusinessObjectInstances=self.businessObjectManagementPanelController.selectedBusinessObjectInstances;
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
