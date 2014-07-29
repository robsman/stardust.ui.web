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
					this.initializeBaseState();
					this.initializePageRendering();
					this.businessObjectManagementPanelController = BusinessObjectManagementPanelController
							.create();

					var self = this;

					DocumentAssignmentService.instance().getDocumentTypes()
							.done(
									function(docTypes) {
										self.documentTypes = self.documentTypes
												.concat(docTypes);
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
																				console
																						.log("===> BO initialized");
																				console
																						.log(self.businessObjectManagementPanelController);

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
				
				DocumentAssignmentPanelController.prototype.removeSpecificAttachment = function(data,e){
					var i=0,
					    j=0,
						treeItem,
						proc,
						specAttch;
					for(;i<this.$parent.startableProcesses.length;i++){
						proc=this.$parent.startableProcesses[i];
						if(proc.id==this.treeItem.processDetails.id){
							for(;j<this.treeItem.processDetails.specificDocuments.length;j++){
								specAttch=this.treeItem.processDetails.specificDocuments[j];
								if(specAttch.id==this.treeItem.specificDocument.id){
									//delete specAttch.scannedDocument;
									this.$parent.refreshStartableProcessesTree();
									this.safeApply();
								}
							}
						}
					}
				};
				
				DocumentAssignmentPanelController.prototype.removeSpecificDocumentStartable=function(proc,attach,specDocId){
					for(var i=0;i<proc.specificDocuments.length;i++){
						if(proc.specificDocuments[i].id==specDocId){
							debugger;
							delete proc.specificDocuments[i].scannedDocument;
						}
					}
				}
				
				DocumentAssignmentPanelController.prototype.removeProcessAttachmentStartable=function(proc,attach){
					for(var i=0;i<proc.processAttachments.length;i++){
						if(proc.processAttachments[i].uuid==attach.uuid){
							proc.processAttachments.splice(i,1);
							return true;
						}
					}
					return false;
				}
				
				DocumentAssignmentPanelController.prototype.removeProcessAttachmentPending=function(proc,attach){
					debugger;
					for(var i=0;i<proc.processAttachments.length;i++){
						if(proc.processAttachments[i].uuid==attach.uuid){
							proc.processAttachments.splice(i,1);
							return true;
						}
					}
					return false;
				}
				
				DocumentAssignmentPanelController.prototype._removeProcessAttachment=function(data,e){
					var i=0,
					    j=0,
						treeItem,
						proc,
						procAttch;

					for(;i<this.$parent.startableProcesses.length;i++){
						proc=this.$parent.startableProcesses[i];
						if(proc.id==this.treeItem.processDetails.id){
							for(;j<this.treeItem.processDetails.processAttachments.length;j++){
								procAttch=this.treeItem.processDetails.processAttachments[j];
								if(procAttch.uuid==this.treeItem.processAttachment.uuid){
									this.treeItem.processDetails.processAttachments.splice(j,1);
									this.$parent.refreshStartableProcessesTree();
									this.safeApply();
									break;
								}
							}
						}
					}
					
				};
				
				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.initializeBaseState = function() {
					this.pageModel = {};
					this.uiModel = {};
					
					this.pageModel.currentDocument = "";
					this.pageModel.pageIndex = {};
					this.pageModel.selectedPage={};
					this.uiModel.showChildren = false;
					
					
					this.startableProcesses=[];
					
					this.retrieveActivityInstanceFromUri();
					this.startProcessDialog = {};
					this.businessObjectFilter = {};
					this.selectedBusinessObjectInstances = [];
					this.documentTypes = [ {
						name : "None",
						documentTypeId : "",
						schemaLocation : ""
					} ];
				};

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.setDocumentType = function(docTypeId, proc) {
					var i=0,
						docType,
						docTypeFound=false;
					
					/*Loop through docTypes until we match our ID*/
					for(i=0;i<this.documentTypes.length;i++){
						if(this.documentTypes[i].documentTypeId==docTypeId){
							docType=this.documentTypes[i];
							docTypeFound=true;
							break;
						}
					}
					
					if(docTypeFound){
						DocumentAssignmentService.instance().setDocumentType(
								docType, proc).done(function(result) {
						});
					}else{
						//stubbed;
						console.log("Selected docType could not be matched");
					}
				};

				/**
				 * Clamp a number between a range;
				 */
				DocumentAssignmentPanelController.prototype.clamp = function(
						number, min, max) {
					console.log("Clamping: " + number + "," + min + "," + max);
					return Math.max(min, Math.min(number, max));
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

				DocumentAssignmentPanelController.prototype.openProcessHistory = function(
						workItem) {
					var oid = workItem.pendingProcess.oid;
					var message = {
						"type" : "OpenView",
						"data" : {
							"viewId" : "processInstanceDetailsView",
							"viewKey" : "processInstanceOID=" + oid,
							"params" : {
								"oid" : "" + oid,
								"processInstanceOID" : "" + oid
							}
						}
					};

					parent.postMessage(JSON.stringify(message), "*");
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
							    .droppable({
							    	hoverClass : "highlighted",
							    	drop : function(event, ui){
							    		
							    		var ed=jQuery.data(ui.draggable[0],"dragData");
							    		
							    		switch(ed.sourceType){
								    		case "proccessAttachment_startable":
								    			self.removeProcessAttachmentStartable(ed.process,ed.attachment);
								    			self.refreshStartableProcessesTree();
								    			break;
								    		case "specificDocument_startable":
								    			self.removeSpecificDocumentStartable(ed.process,ed.attachment,ed.specificDocumentId);
								    			self.refreshStartableProcessesTree();
								    			break;
								    		case "specificDocument_pending":
								    			self.removeSpecificDocumentStartable(ed.process,ed.attachment,ed.specificDocumentId);
								    			self.refreshPendingProcessesTree();
								    			break;
								    		case "proccessAttachment_pending":
								    			self.removeProcessAttachmentPending(ed.process,ed.attachment);
								    			self.refreshPendingProcessesTree();
								    			break;
							    		}
							    		/*
							    		if(ed.sourceType=="proccessAttachment_startable"){
							    			self.removeProcessAttachmentStartable(ed.process,ed.attachment);
							    		}
							    		else if(ed.sourceType=="specificDocument_startable"){
							    			self.removeSpecificDocumentStartable(ed.process,ed.attachment,ed.specificDocumentId);
							    		}*/
							    		self.refreshStartableProcessesTree();
							    		self.safeApply();
							    		window.setTimeout(function() {
											self.bindDragAndDrop();
										}, 1000);
							    	}
							    })
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
											specificDocument : this.pendingProcessesTree[n].specificDocument,
											processOID : this.pendingProcessesTree[n].pendingProcessOID
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

												var processOID = jQuery.data(
														this, "processOID");
												DocumentAssignmentService
														.instance()
														.addProcessDocument(
																processOID,
																scannedDocument,
																specificDocument.id)
														.done(
																function(
																		pendingProcesses) {

																	self.pendingProcesses = pendingProcesses;
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
											processDetails : this.pendingProcessesTree[n],
											processOID : this.pendingProcessesTree[n].pendingProcessOID
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
												var processDetails = jQuery
														.data(this,
																"processDetails");
												var processOID = jQuery.data(
														this, "processOID");

												jQuery("*").css("cursor",
														"wait");
												DocumentAssignmentService
														.instance()
														.addProcessDocument(
																processOID,
																scannedDocument,
																"PROCESS_ATTACHMENTS")
														.done(
																function(
																		pendingProcesses) {

																	self.pendingProcesses = pendingProcesses;
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

											specificDocument.url = "bla";
											specificDocument.creationTimestamp = scannedDocument.creationTimestamp;
											specificDocument.type = scannedDocument.type;
											specificDocument.scannedDocument = scannedDocument;
											self
													.refreshStartableProcessesTree();
											self.safeApply();
											window.setTimeout(function() {
												self.bindDragAndDrop();
											}, 1000);

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
											processAttachments
													.push(scannedDocument);
											self
													.refreshStartableProcessesTree();
											self.safeApply();
											window.setTimeout(function() {
												self.bindDragAndDrop();
											}, 1000);
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
						page, url, e) {
					console.log("Calling Selected Page");
					this.pageModel.selectedPage = page;
					if (e.ctrlKey) {
						if (this.pageModel.pageIndex
								.hasOwnProperty(page.number)) {
							delete this.pageModel.pageIndex[page.number];
						} else {
							this.pageModel.pageIndex[page.number] = url;
						}
					} else if (e.shiftKey) {
						// stubbed for later
					} else {
						this.pageModel.pageIndex = {};
						this.pageModel.pageIndex[page.number] = url;
					}
					console.log(this.pageModel.pageIndex);
					this.pageModel.selectedPage.url = url;
				};

				DocumentAssignmentPanelController.prototype.isPageSelected = function(
						page) {

					if (this.pageModel.pageIndex.hasOwnProperty(page.number)
							&& this.pageModel.pageIndex[page.number] == page.url) {
						return true;
					} else {
						return false;
					}
				}

				DocumentAssignmentPanelController.prototype.startProcess = function(
						treeItem, busObj) {

					var that = this, data = {
						processDefinitionId : treeItem.startableProcess.id,
						businessObject : busObj,
						specificDocuments : [],
						processAttachments : treeItem.startableProcess.processAttachments
					}, i;

					for (i = 0; i < treeItem.startableProcess.specificDocuments.length; i++) {
						if (treeItem.startableProcess.specificDocuments[i].scannedDocument) {
							data.specificDocuments
									.push({
										"dataPathId" : treeItem.startableProcess.specificDocuments[i].id,
										"document" : treeItem.startableProcess.specificDocuments[i].scannedDocument
									});
						}
					}

					DocumentAssignmentService.instance().startProcess(data)
							.done(
								function(result) {
									that.openStartProcessDialog(
											result.scannedDocument,
											result.startableProcess);
								})
							.then(function(){
								DocumentAssignmentService
								.instance()
								.getStartableProcesses()
								.done(function(startableProcesses){
									/*TODO:ZZM This is fragile and will break eventually, needs refactoring to avoid $parent references*/
									that.$parent.startableProcesses = startableProcesses;
									that.$parent.refreshStartableProcessesTree();
									that.safeApply();
								});
							})
							.fail();
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
					if (this.pageModel.selectedPage == page) {
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
					var that=this;
					this.pendingProcessesTree = [];

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
										specificDocument : this.pendingProcesses[n].specificDocuments[m],
										pendingProcessOID : this.pendingProcesses[n].oid
									});
						}

						this.pendingProcessesTree
								.push({
									processAttachments : this.pendingProcesses[n].processAttachments,
									pendingProcessOID : this.pendingProcesses[n].oid
								});

						for (m = 0; m < this.pendingProcesses[n].processAttachments.length; ++m) {
							this.pendingProcessesTree
									.push({
										processAttachment : this.pendingProcesses[n].processAttachments[m],
										pendingProcessOID : this.pendingProcesses[n].oid
									});
						}
					}
					
					this.safeApply();
					
					/*DOM 'tree' is rebuilt so query for rows and bind draggable behaivor*/
					var procAttachStartable=jQuery(".data-procAttch-pend").each(function(i,ele){
						var $ele,
							procId,
							attchId,
							eventData,
							doc,
							proc;
						
						$ele =$(this);
						procId = $ele.attr("data-proc-id");
						attchId = $ele.attr("data-attach-id");

						doc=that.getScannedDocument(attchId);
						proc = that.getPendingProcess(procId);
						debugger;
						eventData={
								sourceType:"proccessAttachment_pending",
								process: proc,
								attachment: doc
						}
						
						jQuery(ele).draggable({
							distance : 20,
							opacity : 0.7,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
										+ doc.name
										+ "</div>");
							},
							drag : function(event) {
							},
							stop : function(event) {
							}
						}).data("dragData",eventData);
					});
					
					var specDocStartable = jQuery(".data-specAttch-pend").each(function(i,ele){
						
						var $ele,
							procId,
							attchId,
							eventData,
							doc,
							specDocId,
							proc;
						
						$ele =$(this);
						procId = $ele.attr("data-proc-id");
						attchId = $ele.attr("data-attach-id");
						specDocId = $ele.attr("data-specdoc-id")
						
						doc=that.getScannedDocument(attchId);
						proc = that.getPendingProcess(procId);
						
						eventData={
								sourceType:"specificDocument_pending",
								process: proc,
								attachment: doc,
								specificDocumentId: specDocId
						}
						
						jQuery(ele).draggable({
							distance : 20,
							opacity : 0.7,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;z-index:9999;'></i> "
										+ "TODO.todo"
										+ "</div>");
							},
							drag : function(event) {
							},
							stop : function(event) {
							}
						}).data("dragData",eventData);
						
					});
				};
				
				
				DocumentAssignmentPanelController.prototype.getScannedDocument=function(Id){
					var i=0;
					for(;i<this.scannedDocuments.length;i++){
						if(this.scannedDocuments[i].uuid==Id){
							return this.scannedDocuments[i];
						}
					}
				}
				
				DocumentAssignmentPanelController.prototype.getPendingProcess=function(Id){
					var i=0;
					for(;i<this.pendingProcesses.length;i++){
						if(this.pendingProcesses[i].oid==Id){
							return this.pendingProcesses[i];
						}
					}
				}
				
				DocumentAssignmentPanelController.prototype.getStartableProcess=function(Id){
					var i=0;
					for(;i<this.startableProcesses.length;i++){
						if(this.startableProcesses[i].id==Id){
							return this.startableProcesses[i];
						}
					}
				}
				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.refreshStartableProcessesTree = function() {
					var that=this;
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
					
					this.safeApply();
					
					/*after this point we have our new DOM buit for the tree so now add drag-drop for these as required*/
					var specDocStartable = jQuery(".data-specAttch-start").each(function(i,ele){
						
						var $ele,
							procId,
							attchId,
							eventData,
							doc,
							specDocId,
							proc;
						
						$ele =$(this);
						procId = $ele.attr("data-proc-id");
						attchId = $ele.attr("data-attach-id");
						specDocId = $ele.attr("data-specdoc-id")
						
						doc=that.getScannedDocument(attchId);
						proc = that.getStartableProcess(procId);
						
						eventData={
								sourceType:"specificDocument_startable",
								process: proc,
								attachment: doc,
								specificDocumentId: specDocId
						}
						
						jQuery(ele).draggable({
							distance : 20,
							opacity : 0.7,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
										+ doc.name
										+ "</div>");
							},
							drag : function(event) {
							},
							stop : function(event) {
							}
						}).data("dragData",eventData);
						
					});
					
					var procAttachStartable=jQuery(".data-procAttch-start").each(function(i,ele){
						var $ele,
							procId,
							attchId,
							eventData,
							doc,
							proc;
						
						$ele =$(this);
						procId = $ele.attr("data-proc-id");
						attchId = $ele.attr("data-attach-id");

						doc=that.getScannedDocument(attchId);
						proc = that.getStartableProcess(procId);
						
						eventData={
								sourceType:"proccessAttachment_startable",
								process: proc,
								attachment: doc
						}
						
						jQuery(ele).draggable({
							distance : 20,
							opacity : 0.7,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
										+ doc.name
										+ "</div>");
							},
							drag : function(event) {
							},
							stop : function(event) {
							}
						}).data("dragData",eventData);
					});
					
				};
				
				DocumentAssignmentPanelController.prototype.processCanStart = function(
						proc) {
					var hasBusinessObject = false, hasProcessAttachment = false, hasSpecificDocument = false, i;

					hasBusinessObject = this.selectedBusinessObjectInstances.length > 0;
					
					if(proc.startableProcess){
						hasProcessAttachment = proc.startableProcess.processAttachments.length > 0;
					}

					if(proc.startableProcess && proc.startableProcess.specificDocuments){
						for (i = 0; i < proc.startableProcess.specificDocuments.length; i++) {
							if (proc.startableProcess.specificDocuments[i].scannedDocument) {
								hasSpecificDocument = true;
								break;
							}
						}
					}
					if (hasBusinessObject
							&& (hasProcessAttachment || hasSpecificDocument)) {
						return true;
					} else {
						return false;
					}

				}

				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.onBusinessObjectInstanceSelectionChange = function() {
					var self = this;

					jQuery("*").css("cursor", "wait");
					DocumentAssignmentService
							.instance()
							.getPendingProcesses()
							.done(
									function(pendingProcesses) {
										self.pendingProcesses = pendingProcesses;
										self.selectedBusinessObjectInstances = self.businessObjectManagementPanelController.selectedBusinessObjectInstances;
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
