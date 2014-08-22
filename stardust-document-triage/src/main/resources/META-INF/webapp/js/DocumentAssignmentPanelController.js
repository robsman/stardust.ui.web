/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[
				"document-triage/js/Utils",
				"business-object-management/js/BusinessObjectManagementPanelController",
				"document-triage/js/DocumentAssignmentService",
				"document-triage/js/base64" ],
		function(Utils, 
				BusinessObjectManagementPanelController,
				DocumentAssignmentService,
				base64) {
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
					
					/*collection to keep track of which documents have been consumed 
					 *in either tree (pending/startable)*/
					this.sessionLog ={ 
							documents:{}
					};
					
					var self = this;
					
					DocumentAssignmentService.instance()
					.getActivity(self.activityInstanceOid)
					.then(function(data){
						self.processoid = data.processInstanceOid;

					});
					
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

				/**
				 * Opens a confirmation dialog for the case of a split document creation.
				 * Returns a jQuery promise that is resolved or rejected based on the users
				 * choice within the dialog.
				 */
				DocumentAssignmentPanelController.prototype.openSplitDialog = function(docObj,pages){

					var dialogScope = this.$new(),
						deferred = jQuery.Deferred();
					
					dialogScope.document=docObj;
					dialogScope.pages = pages.map(function(num){return num +1}).toString();
					
					this.ngDialog.openConfirm({
					    template: './templates/documentSplitConfirm.html',
					    className: 'ngdialog-theme-default',
					    scope: dialogScope
					})
					.then(function(){
						deferred.resolve(docObj);
					})
					.catch(function(){
						deferred.reject(docObj);
					});
					
					return deferred.promise();

				}
				
				/* Given a document uuid this function will search pending processes until it finds its
				 * match and return the relevant information. This function should always be called
				 * when lookign up a document from the pending process tree as pending processes can
				 * have documents associated with them which are not represented in our scanned documents
				 * collection. This is in contrast to startable processes whose documents will always be staged
				 * from our scanned documents.
				 */
				DocumentAssignmentPanelController.prototype.getPendingDocument = function(uuid){
					var tempProc,
						tempDoc,
						i,j,
						isFound=false;
					
					for(i=0;i < this.pendingProcesses.length && !isFound;i++){
						
						tempProc=this.pendingProcesses[i];
						
						for(j=0;j<tempProc.processAttachments.length;j++){
							tempDoc = tempProc.processAttachments[j];
							if(tempDoc && tempDoc.uuid==uuid){
								isFound =true;
								break;
							}
						}
						
						for(j=0;j<tempProc.specificDocuments.length && !isFound;j++){
							tempDoc = tempProc.specificDocuments[j].document;
							if(tempDoc && tempDoc.uuid==uuid){
								isFound =true;
								break;
							}
						}
					}
					return tempDoc;
				}
				
				/*Maintains a log of documents and a reference count regarding how many instances of 
				 *that document appear in each process or model (in the case of staged documents)
				 *Valid operations:
				 *INSERT: insert or update a document with a process reference, existing = increment ref count
				 *DELETE: decrement the process count for a document, if count reaches 0 then delete property
				 *CONVERT_MODEL: Insert new process for a document, set its ref count to the convertID count
				 *				 and then delete the convertId process proeprty from the document. This is designed
				 *				 specifically to handle the conversion of a startable model to that of a true process.
				 *SET: set a fixed value to a documents reference count
				 *
				 *Arguments:
				 *	op: operation to perform [INSERT,DELETE,CONVERT_MODEL]
				 *  docuuid : uuid of the scanned document
				 *  hashID  : either the processOID for pending or the modelID for startable
				 *  convertID: ID to move the hashIDs value to
				 *  val: Only valid for a SET operation, value to set the reference count to
				 **/
				DocumentAssignmentPanelController.prototype.updateSessionLog = function(op,docuuid,hashID,convertId,val){
					
					var docEntry = this.sessionLog.documents[docuuid],
						procEntry,
						tempEntry;
					
					if(!docEntry){
						this.sessionLog.documents[docuuid]={'processes' : {}};
						docEntry=this.sessionLog.documents[docuuid];
					}
					
					procEntry=docEntry.processes[hashID];
					
					if(!procEntry){
						docEntry.processes[hashID]=0;
					}
					
					if(op=="INSERT"){
						docEntry.processes[hashID]=docEntry.processes[hashID]+1;
					}
					else if(op=="SET"){
						val = parseInt(val);
						docEntry.processes[hashID]=docEntry.processes[hashID]=val;
					}
					else if (op == "CONVERT_MODEL"){
						docEntry.processes[convertId]=docEntry.processes[hashID];
						delete docEntry.processes[hashID];
					}
					else if(op == "DELETE"){
						docEntry.processes[hashID]=docEntry.processes[hashID]-1;
						if(docEntry.processes[hashID]<1){
							delete docEntry.processes[hashID];
						}
					}
				};
				
				
				/*For a given document uuid, check our session log for references and return a sum*/
				DocumentAssignmentPanelController.prototype.getReferenceCount = function(docuuid){
					var docEntry = this.sessionLog.documents[docuuid],
						key,
						counter=0;
					
					if(!docEntry){
						return -1;
					}
					
					for(key in docEntry.processes){
						if(docEntry.processes.hasOwnProperty(key)){
							counter = counter + docEntry.processes[key];
						}
					}
					
					return counter;
					
				}
				
				/*Scrape the pending process tree for existing documents. This must be called any time the 
				 *pending process tree is refreshed in order to keep the respective reference counts of the documents,
				 *as associated with the pending processes, in sync. This will account for documents already associated
				 *with an existing process and keep those values from accumulating inaccurately in the sessionLog.
				 *
				 *Arguments
				 *------------------
				 *	tree: item from our pendingProcessTree array
				 **/
				DocumentAssignmentPanelController.prototype.scrapePendingForDocuments = function(tree){
					var i,j,
						treeObj,
						specDoc,
						procAttch,
						localLog={},
						llProcId,
						llDocId;
					
					for(i=0;i< tree.length;i++){
						
						treeObj=tree[i];
						
						if(treeObj.pendingProcess){
							
							/*create local log entry to keep sum of all occurences per document
							 *this will be used to adjust reference counts in our sessionLog */
							localLog[treeObj.pendingProcess.oid]={documents:{}};
							
							/*scrape specific documents*/
							if(treeObj.pendingProcess.specificDocuments){
								for(j=0;j<treeObj.pendingProcess.specificDocuments.length;j++){
									specDoc=treeObj.pendingProcess.specificDocuments[j];
									if(specDoc.document){
										if(!localLog[treeObj.pendingProcess.oid].documents[specDoc.document.uuid]){
											localLog[treeObj.pendingProcess.oid].documents[specDoc.document.uuid]=0;
										}
										localLog[treeObj.pendingProcess.oid].documents[specDoc.document.uuid]+=1;
									}
								}
							}
							
							/*scrape process attachments*/
							if(treeObj.pendingProcess.processAttachments){
								for(j=0;j<treeObj.pendingProcess.processAttachments.length;j++){
									procAttch=treeObj.pendingProcess.processAttachments[j];
									if(!localLog[treeObj.pendingProcess.oid].documents[procAttch.uuid]){
										localLog[treeObj.pendingProcess.oid].documents[procAttch.uuid]=0;
									}
									localLog[treeObj.pendingProcess.oid].documents[procAttch.uuid]+=1;
								}
							}
						}/*Outer For Loop done, localLog complete*/
						
						/*No set our local log values on their respective documents in the sessionLog*/
						for(llProcId in localLog){
							for(llDocId in localLog[llProcId].documents){
								this.updateSessionLog("SET",llDocId,llProcId,"",localLog[llProcId].documents[llDocId]);
							}
						}
					}
				}
				
				DocumentAssignmentPanelController.prototype.refreshScannedDocuments = function(id){
					
					var deferred = jQuery.Deferred();
						self=this;
					DocumentAssignmentService.instance().getScannedDocuments(id)
					.done(function(scannedDocuments) {
							self.scannedDocuments = scannedDocuments;
							//self.refreshPagesList();
							for (var n = 0; n < self.scannedDocuments.length; ++n) {
								self.scannedDocuments[n].pages = [];

								for (var m = 0; m < self.scannedDocuments[n].pageCount; ++m) {
									self.scannedDocuments[n].pages.push({
										number : m + 1
									});
								}
							}
							window.setTimeout(function() {self.bindDragAndDrop();},1000);
							
							deferred.resolve();
					})
					.fail(function(status){
						deferred.reject(status);
					});
					
					return deferred.promise;
				}
				
				
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
				
				DocumentAssignmentPanelController.prototype.removeSpecificDocumentPending=function(proc,attach,specDocId){
					for(var i=0;i<proc.specificDocuments.length;i++){
						if(proc.specificDocuments[i].id==specDocId){
							delete proc.specificDocuments[i].document;
						}
					}
				}
				
				DocumentAssignmentPanelController.prototype.removeSpecificDocumentStartable=function(proc,attach,specDocId){
					for(var i=0;i<proc.specificDocuments.length;i++){
						if(proc.specificDocuments[i].id==specDocId){
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
				
				
		
				DocumentAssignmentPanelController.prototype.initializeBaseState = function() {
					
					this.pageModel = {};
					this.uiModel = {};
					this.pageModel.currentDocument = "";
					this.pageModel.pageIndex = {};
					this.pageModel.selectedPage={};
					this.uiModel.showChildren = false;
					this.uiModel.docPanelRefreshStamp = new Date().getTime();
					
					
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
					
					var b64 = base64.get();
					encodedId = (encodedId.length % 3 == 0)? encodedId : encodedId + "===".slice(encodedId.length % 3);
					var decodedId = b64.decode(encodedId || '');
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
					this.currentPage=0;
				};
				
				DocumentAssignmentPanelController.prototype.setPageRelative = function(offset){
					var calculatedPos = this.pageModel.selectedPage.number + offset;
					if(calculatedPos >0 && calculatedPos <= this.pageModel.totalPages){
						this.pageModel.selectedPage.number = calculatedPos;
					}
				}
				
				DocumentAssignmentPanelController.prototype.setPageAbsolute = function(index){
					if(index > 0 && index <= this.pageModel.totalPages){
						this.pageModel.selectedPage.number = index;
					}
				}
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
							scannedDocument : angular.copy(this.scannedDocuments[m])
						});
						
						scannedDocumentDivision
							    .droppable({
							    	hoverClass : "highlighted",
							    	drop : function(event, ui){
							    		var ed=jQuery.data(ui.draggable[0],"dragData"),
							    			workService = DocumentAssignmentService.instance();
							    		
							    		if(!ed){return;}
							    		
							    		switch(ed.sourceType){
								    		case "proccessAttachment_startable":	    			
								    			self.removeProcessAttachmentStartable(ed.process,ed.attachment);
								    			self.updateSessionLog("DELETE",ed.attachment.uuid,ed.process.id);
								    			self.refreshStartableProcessesTree();
								    			break;
								    		case "specificDocument_startable":
								    			self.removeSpecificDocumentStartable(ed.process,ed.attachment,ed.specificDocumentId);
								    			self.updateSessionLog("DELETE",ed.attachment.uuid,ed.process.id);
								    			self.refreshStartableProcessesTree();
								    			break;
								    		case "specificDocument_pending":
								    			workService.deleteAttachment(ed.process.oid,ed.specificDocumentId,"")
								    			.done(function(pendingProcesses){
								    				self.pendingProcesses = pendingProcesses.processInstances;
								    				self.updateSessionLog("DELETE",ed.attachment.uuid,ed.process.oid);
									    			self.refreshPendingProcessesTree();
									    			window.setTimeout(function() {
														self.bindDragAndDrop();
													}, 1000);
								    			})
								    			.fail(function(){
								    				//stubbed
								    			});
								    			break;
								    		case "pageReorder":
								    			/* Do nothing, this should be handled by the reorder target div.*/
								    			break;
								    		case "proccessAttachment_pending":			    			
								    			workService.deleteAttachment(ed.process.oid,"PROCESS_ATTACHMENTS",ed.attachment.uuid)
								    			.done(function(pendingProcesses){
								    				self.pendingProcesses = pendingProcesses.processInstances;
								    				self.updateSessionLog("DELETE",ed.attachment.uuid,ed.process.oid);
									    			self.refreshPendingProcessesTree();
								    			}).fail(function(){
								    				//stubbed
								    			});
								    			break;
							    		}
							    		
							    		//self.safeApply();
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

										var scannedDocument = jQuery.data(event.currentTarget,"scannedDocument"),
										    pageNumbers=[],
										    pageString = "",
										    key;
										
										for (var key in self.pageModel.pageIndex) {
										  if (self.pageModel.pageIndex.hasOwnProperty(key)) {
											isRootDoc=false;
										    pageNumbers.push(key);
										  }
										}
										
										if(pageNumbers.length > 0){
											pageString= " Pages: (" + pageNumbers.toString() + ")";
										}
										
										return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
												+ scannedDocument.name + pageString 
												+ "</div>");
									},
									drag : function(event) {
									},
									stop : function(event) {
									}
								});
					}
					
					/*Drag events for individual pages, must mimic container drag*/
					jQuery(".page.reorderable").each(function(){
						var that = this;
						$(this).draggable({
							distance : 20,
							opacity : 0.7,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								
								var docUUID,
									scannedDocument = {};
								    pageNumbers=[],
								    pageString = "",
								    key;
								
								docUUID = $(that).attr("data-doc-uuid");
								scannedDocument = self.getScannedDocument(docUUID);
								
								for (var key in self.pageModel.pageIndex) {
								  if (self.pageModel.pageIndex.hasOwnProperty(key)) {
									isRootDoc=false;
								    pageNumbers.push(key);
								  }
								}
								
								if(pageNumbers.length > 0){
									pageString= " Pages: (" + pageNumbers.toString() + ")";
								}
								
								$(that).data({
									ui : {},
									scannedDocument : angular.copy(scannedDocument),
									dragData :{ sourceType: "pageReorder"},
									pages: pageNumbers
								});
								
								return jQuery("<div class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
										+ scannedDocument.name + pageString
										+ "</div>");
							},
							drag : function(event) {
							},
							stop : function(event) {
							}
						});
					});
					
					/*All elements with a class of 'reorderTarget' are drop targets for a pageReorder drag event
					 *These events are only initiated from the scanned Document panel*/
					jQuery(".reorderTarget").each(function(){
						$(this).droppable({
								hoverClass : "dragover",
						    	drop : function(event, ui){
						    		var ed, /*eventData*/
						    			doc, /*scanned document associated with drag*/
						    			insertAt,
						    			reorderedPages=[];

						    		ed=jQuery.data(ui.draggable[0]);
						    		if(ed.dragData && ed.dragData.sourceType=="pageReorder"){
							    		doc=ed.scannedDocument;
							    		insertAt =event.target.attributes['data-page-number'].value;
							    		
							    		/*Build array of pages, 1 based*/
							    		for(var i=0;i<ed.scannedDocument.pages.length;i++){
							    			reorderedPages.push(i+1 +"");
							    		}
							    		
							    		/*Remove pages that we will be reshuffling so as to avoid dupes*/
							    		reorderedPages=reorderedPages.filter(function(v){
						    			  return ed.pages.indexOf(v) < 0; 
						    			});
							    		
							    		/*Now move our pages to their target position*/
							    		reorderedPages
							    		.splice
							    		.apply(reorderedPages, [reorderedPages.indexOf(insertAt)+1, 0]
							    		.concat(ed.pages));

							    		DocumentAssignmentService.instance().reorderDocument(doc.uuid,reorderedPages)
							    		.then(function(result){
							    			//stubbed	
							    		})
							    		.fail(function(){
							    			
							    		})
							    		.always(function(){
							    			ed.$scope.$parent.document.urlFragStamp = new Date().getTime();
							    			self.refreshPagesList();
							    			jQuery("*").css("cursor","default");
							    			self.safeApply();
								    		window.setTimeout(function() {
												self.bindDragAndDrop();
											}, 1000);
							    		});
							    		
						    		}
						    	}
							}
						);
					});
					
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
												var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument")
												    pendingActivityInstance = jQuery.data(this,"pendingActivityInstance"),
												    pages=self.getSelectedPageNums();
												
												
												if(!scannedDocument){return;}
												
												/*Adjust for Zero based page indexing*/
												pages =	pages.map(function(num){return num -1;});
												
												if(pages.length > 0){
													self.openSplitDialog(scannedDocument,pages)
													.then(function(){
														return DocumentAssignmentService.instance()
															.splitDocument(self.processoid,scannedDocument.uuid,pages);
													})			
													.then(function(result){
														/*swap our scannedDocument for the new document generated on the server.*/
														scannedDocument=result; 
														/*Now pull all our associated documents from the server*/
														return DocumentAssignmentService.instance()
															.getScannedDocuments(self.activityInstanceOid);
													})
													.then(function(scannedDocuments){
														self.scannedDocuments = scannedDocuments;
														return DocumentAssignmentService.instance()
															.completeDocumentRendezvous(pendingActivityInstance,scannedDocument);
													})
													.then(function(pendingProcesses){
														self.pendingProcesses = pendingProcesses;
													})
													.fail(function(){
														//TODO: Error Handling
													})
													.always(function(){
														self.refreshPagesList();
														self.pageModel.pageIndex={};
														self.refreshPendingProcessesTree();
														//self.safeApply();
														window.setTimeout(function() {
															self.bindDragAndDrop();
														},1000);
													});
												}
												else{
													DocumentAssignmentService.instance()
													.completeDocumentRendezvous(pendingActivityInstance,scannedDocument)
													.done(function(pendingProcesses) {
														self.pageModel.pageIndex={};
														self.pendingProcesses = pendingProcesses;
														self.refreshPendingProcessesTree();
														jQuery("*").css("cursor","default");
														//self.safeApply();
														window.setTimeout(function() {
															self.bindDragAndDrop();
														},1000);
													}).fail();
												}
												
												
												
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
												var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument"),
													specificDocument = jQuery.data(this,"specificDocument"),
													processOID = jQuery.data(this, "processOID"),
													pages=self.getSelectedPageNums();
												
												if(!scannedDocument){return;}
												
												/*Adjust for Zero based page indexing*/
												pages =	pages.map(function(num){return num -1;});
												
												/*************/
												if(pages.length > 0){
													self.openSplitDialog(scannedDocument,pages)
													.then(function(){
														return DocumentAssignmentService.instance()
														.splitDocument(self.processoid,scannedDocument.uuid,pages)
													})
													.then(function(result){
														
														scannedDocument=result; 
														self.pushRawDocumentToScanned(result);												
														self.refreshPagesList();
														self.pageModel.pageIndex={};
														return DocumentAssignmentService.instance()
														.addProcessDocument(processOID,scannedDocument,specificDocument.id);

													})
													.then(function(pendingProcesses){
														self.updateSessionLog("INSERT",scannedDocument.uuid,processOID);
														self.pendingProcesses = pendingProcesses;
														self.refreshPendingProcessesTree();
													})
													.fail(function(){
														//TODO:ZZM Error handling
													})
													.always(function(){
														jQuery("*").css("cursor","default");
														self.safeApply();
														jQuery("*").css("cursor","default");
														window.setTimeout(function() {
															self.bindDragAndDrop();
														}, 1000);
													});
													
												}
												else{
													DocumentAssignmentService.instance()
													.addProcessDocument(processOID,scannedDocument,specificDocument.id)
													.done(function(pendingProcesses) {
														self.pageModel.pageIndex={};
														self.pendingProcesses = pendingProcesses;
														self.updateSessionLog("INSERT",scannedDocument.uuid,processOID)
														self.refreshPendingProcessesTree();
														//self.safeApply();
														jQuery("*").css("cursor","default");
														window.setTimeout(
															function() {
																self.bindDragAndDrop();
															},1000);
													}).fail(function(){
														jQuery("*").css("cursor","default");
													});
												}
												/*************/

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

												var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument"),
													processAttachments = jQuery.data(this,"processAttachments"),
													processDetails = jQuery.data(this,"processDetails"),
													processOID = jQuery.data(this, "processOID"),
													pages=self.getSelectedPageNums();
												
												if(!scannedDocument){return;}
												
												/*Adjust for Zero based page indexing*/
												pages =	pages.map(function(num){return num -1;});
												
												jQuery("*").css("cursor","wait");

												if(pages.length > 0){
													self.openSplitDialog(scannedDocument,pages)
													.then(function(){
														return DocumentAssignmentService.instance()
														.splitDocument(self.processoid,scannedDocument.uuid,pages);
													})
													.then(function(result){
														scannedDocument=result; 
														self.pushRawDocumentToScanned(result);
														self.pageModel.pageIndex={};
														
														return DocumentAssignmentService.instance()
														.addProcessDocument(processOID,scannedDocument,"PROCESS_ATTACHMENTS");
													})
													.then(function(pendingProcesses){
														self.updateSessionLog("INSERT",scannedDocument.uuid,processOID);
														self.pendingProcesses = pendingProcesses;
														self.refreshPendingProcessesTree();
													})
													.fail(function(){})
													.always(function(){
														self.refreshPagesList();
														jQuery("*").css("cursor","default");
														window.setTimeout(function() {
															self.bindDragAndDrop();
														}, 1000);
													});
												}
												/*Pages are empty so we are dragging an entire existing document*/
												else{
													DocumentAssignmentService.instance()
													.addProcessDocument(processOID,scannedDocument,"PROCESS_ATTACHMENTS")
													.done(function(pendingProcesses) {
															self.pageModel.pageIndex={};
															self.updateSessionLog("INSERT",scannedDocument.uuid,processOID);
															self.pendingProcesses = pendingProcesses;
															self.refreshPendingProcessesTree();
															//self.safeApply();
															jQuery("*").css("cursor","default");
															window.setTimeout(function() {
																self.bindDragAndDrop();
															},1000);
													})
													.fail(function() {
																jQuery("*").css("cursor","default");
													});
												}
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
											var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument"),
											    processDetails = jQuery.data(this, "processDetails"),
											    specificDocument = jQuery.data(this, "specificDocument"),
											    pages=self.getSelectedPageNums();
											
											if(!scannedDocument){return;}
											
											/*Adjust for Zero based page indexing*/
											pages =	pages.map(function(num){return num -1;});

											if(pages.length>0){
												
												self.openSplitDialog(scannedDocument,pages)
												.then(function(result){
													return DocumentAssignmentService.instance()
													.splitDocument(self.processoid,scannedDocument.uuid,pages);
												})
												.then(function(result){
													scannedDocument=result; 
													self.updateSessionLog("INSERT",scannedDocument.uuid,processDetails.id);
													self.pushRawDocumentToScanned(result);
													specificDocument.scannedDocument=scannedDocument;
												})
												.fail(function(){
													//TODO: Error Handling
												})
												.always(function(){
													self.refreshStartableProcessesTree();
													self.refreshPagesList();
													self.pageModel.pageIndex={};
													//self.safeApply();
													window.setTimeout(function() {
														self.bindDragAndDrop();
													}, 1000);
												});
												
											}
											else{
												self.pageModel.pageIndex={};
												specificDocument.scannedDocument = scannedDocument;
												self.updateSessionLog("INSERT",scannedDocument.uuid,processDetails.id);
												self.refreshStartableProcessesTree();
												//self.safeApply();
												window.setTimeout(function() {
													self.bindDragAndDrop();
												}, 1000);
											}
											
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
											
											var scannedDocument = jQuery.data(ui.draggable[0],"scannedDocument")
											    processDetails = jQuery.data(this, "processDetails"),
											    processAttachments = jQuery.data(this,"processAttachments");
												pages=self.getSelectedPageNums();
											
											if(!scannedDocument){return;}
												
											/*Adjust for Zero based page indexing*/
											pages =	pages.map(function(num){return num -1;});
												
											/*If pages contains any values then we must split into a new document*/	
											if(pages.length > 0){
												
												self.openSplitDialog(scannedDocument,pages)
												.then(function(result){
													return DocumentAssignmentService.instance()
													.splitDocument(self.processoid,scannedDocument.uuid,pages)
												})
												.then(function(result){
													scannedDocument=result; 
													self.updateSessionLog("INSERT",scannedDocument.uuid,processDetails.id);
													processAttachments.push(scannedDocument);
													self.pushRawDocumentToScanned(result);
												})
												.fail(function(){
													//TODO:Error handling
												})
												.always(function(){		
													self.refreshStartableProcessesTree();
													self.refreshPagesList();
													self.pageModel.pageIndex={};
													//self.safeApply();
													window.setTimeout(function() {
														self.bindDragAndDrop();
													}, 1000);
												});
											}
											/*Pages are empty so we are dragging an entire existing document*/
											else{
												self.pageModel.pageIndex={};
												processAttachments.push(scannedDocument);
												self.updateSessionLog("INSERT",scannedDocument.uuid,processDetails.id);
												self.refreshStartableProcessesTree();
												//self.safeApply();
												window.setTimeout(function() {
													self.bindDragAndDrop();
												}, 1000);
											}
											
										},
										tolerance : "pointer"
									});
						}
					}
				};
				
				DocumentAssignmentPanelController.prototype.pushRawDocumentToScanned = function(doc){
					doc.pageCount = doc.numPages;
					doc.url=DocumentAssignmentService.instance().getBaseDocumentURL(doc.uuid);
					doc.pages=[];
					for(var i=0;i<doc.numPages;i++){
						doc.pages.push(i+1);
					}
					this.scannedDocuments.push(doc);			
				}
				
				DocumentAssignmentPanelController.prototype.openDocumentInfoPopup = function(elem,docuuid,e,loc){
					var dialogScope = this.$new(true), /*Create new isolate*/
						document;
					
					if(!docuuid || docuuid===""){
						return;
					}
					
					/*Fish in our local caches for our document of interest*/
					if(loc=="startable"){
						document= this.getScannedDocument(docuuid);
					}else if(loc=="pending"){
						document = this.getPendingDocument(docuuid);
					}
					
					dialogScope.document=document;
					this.ngDialog.open({
					    template: './templates/documentInfoPopover.html',
					    className: 'ngdialog-theme-popup',
					    appendTo: elem,
					    align: "left",
			            position: [e.clientX,e.clientY],
			            scope: dialogScope
					});
					
				}
				
				DocumentAssignmentPanelController.prototype.openDocumentRefPopup = function(elem,docuuid,e){
					
					var dialogScope = this.$new(true); /*Create new isolate*/
					dialogScope.processList = this.getAssociatedProcesses(docuuid);
					
					this.ngDialog.open({
					    template: './templates/associatedProcsPopover.html',
					    className: 'ngdialog-theme-popup',
					    appendTo: elem,
					    align: "right",
			            position: [e.clientX,e.clientY],
			            scope: dialogScope
					});
				}
				
				/**
				 * Given a document UUID return all proceeses associated with that document
				 * from our session log.
				 */
				DocumentAssignmentPanelController.prototype.getAssociatedProcesses = function(docuuid){
					
					var docEntry = this.sessionLog.documents[docuuid],
						procId,
						pattern=/\{[A-z0-9]*\}/,
						tempProc,
						results=[],
						i;

					if(docEntry && docEntry.processes){
						for(var procId in docEntry.processes){
							if(pattern.test(procId)){
								procId = procId.replace(pattern,"");
								results.push(procId);
							}
							else{
								for(i=0;i<this.pendingProcesses.length;i++){
									tempProc=this.pendingProcesses[i];
									if(tempProc.oid==procId){
										results.push(tempProc)
									}
								}
							}
						}
					}
					debugger;
					return results;
					
				}
				
				DocumentAssignmentPanelController.prototype.setStagedDocType = function(treeItem,docTypeId){
					var i,
						docType;
					
					for(i=0;i<this.documentTypes.length;i++){
						if(this.documentTypes[i].documentTypeId==docTypeId){
							docType=this.documentTypes[i];
							docTypeFound=true;
							break;
						}
					}
					treeItem.processAttachment.documentType=docType;
				}
				
				DocumentAssignmentPanelController.prototype.getSelectedPageNums=function(){
					var pageNumbers=[];
					for (var key in this.pageModel.pageIndex) {
						  if (this.pageModel.pageIndex.hasOwnProperty(key)) {
						    pageNumbers.push(key);
						  }
						}
					return pageNumbers;
				}
				
				/**
				 * 
				 */
				DocumentAssignmentPanelController.prototype.selectPage = function(page,url,e,document) {
					
					this.pageModel.selectedPage = page;
					this.pageModel.totalPages = document.pages.length;
					this.pageModel.document = document;
					this.pageModel.selectedDocumentIndex = this.getDocumentIndex(document.uuid);
					
					if(e){
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
					}
					this.pageModel.selectedPage.url = url;
					jQuery("#pageImage").css("width","100%");
				};
				
				/**
				 * Given a document uuid lookup the location within the colelction of scanned documents.
				 * Returns -1 if not found otherwise returns 0 based index.
				 */
				DocumentAssignmentPanelController.prototype.getDocumentIndex = function(uuid){
					
					var currentIndex=-1,
					    located=false;
					for(i=0;i < this.scannedDocuments.length && !located;i++){
						if(this.scannedDocuments[i].uuid==uuid){
							currentIndex=i;
							located=true;
						}
					}
					return currentIndex;
					
				}
				
				/**
				 * Given the uuid of the currently selected document this will imitate selecting the next or 
				 * previous document (+- some offset) from the scanned document division, minus meaningful events.
				 */
				DocumentAssignmentPanelController.prototype.selectDocumentRelative = function(uuid,offset,e){
					
					var document,
						i,
						currentIndex,
						offsetIndex,
						located=false;
					
					currentIndex = this.getDocumentIndex(uuid);
					
					if(currentIndex >=0){
						located =true;
						offsetIndex = currentIndex + offset;
					}
					
					
					/*if we located the current document and our calculated offset is in range*/
					if(located && (offsetIndex >= 0 && offsetIndex < this.scannedDocuments.length)){
						document = this.scannedDocuments[offsetIndex];
						this.selectPage(document.pages[0],document.url,e,document);
						this.pageModel.selectedDocumentIndex = offsetIndex;
						this.safeApply();
					}
				}
				
				DocumentAssignmentPanelController.prototype.resetPageModel = function(){
					this.pageModel.pageIndex={};
				}
				
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
					
					var that = this, 
					    data = {
							processDefinitionId : treeItem.startableProcess.id,
							businessObject : busObj,
							specificDocuments : [],
							processAttachments : treeItem.startableProcess.processAttachments
						}, 
						i;

					for (i = 0; i < treeItem.startableProcess.specificDocuments.length; i++) {
						if (treeItem.startableProcess.specificDocuments[i].scannedDocument) {
							data.specificDocuments
									.push({
										"dataPathId" : treeItem.startableProcess.specificDocuments[i].id,
										"document" : treeItem.startableProcess.specificDocuments[i].scannedDocument
									});
						}
					}

					DocumentAssignmentService.instance()
						.startProcess(data)
						.then(
							function(result) {
								//TODO:ZZM - Convert model entry to process entry in session log
								that.openStartProcessDialog(result.scannedDocument,result.startableProcess);
							})
						.then(function(){
							return DocumentAssignmentService.instance()
							.getStartableProcesses();
						})
						.then(function(startableProcesses){
							that.$parent.startableProcesses = startableProcesses;
						})
						.fail(function(err){
							console.log(err);
						})
						.always(function(){
							that.$parent.refreshStartableProcessesTree();
							//that.safeApply();
							window.setTimeout(function() {
								that.bindDragAndDrop();
							}, 1000);
						});
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
				DocumentAssignmentPanelController.prototype.zoomInPage = function(e) {
					if(e){
						this.zoomFactor=100;
						//this.safeApply();
					}
					else{
						this.zoomFactor += 10;
					}
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
					var that=this,
						scrollTop = jQuery("#pendingTreeContainer").scrollTop();
					
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
					/*Keep reference count in sync*/
					this.scrapePendingForDocuments(this.pendingProcessesTree);
					
					/*Let Angular update DOM so we can apply our draggable and droppable behaivor to dynamice elements*/

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

						doc=that.getPendingDocument(attchId) || {name:'not supported'};
						proc = that.getPendingProcess(procId);
						eventData={
								sourceType:"proccessAttachment_pending",
								process: proc,
								attachment: doc
						}
						
						jQuery(ele).draggable({
							distance : 20,
							opacity : 0.7,
							scroll : true,
							cursor : "move",
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {
								return jQuery("<div style='white-space: nowrap;width:200px;text-overflow:ellipsis;overflow: hidden;' class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
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
						
						/*If no attachment then bail*/
						if(attchId==""){
							return;
						}
						
						doc=that.getPendingDocument(attchId)|| {name:'not supported'};
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
							scroll : true,
							cursorAt : {
								top : 0,
								left : 0
							},
							helper : function(event, ui) {

								return jQuery("<div style='white-space: nowrap;width:200px;text-overflow:ellipsis;overflow: hidden;' class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
										+ doc.name
										+ "</div>");
								
							},
							appendTo: "body",
							drag : function(event) {
							},
							stop : function(event) {
							}
						}).data("dragData",eventData);
						
					});
					
					window.setTimeout(function() {
						jQuery("#pendingTreeContainer").scrollTop(scrollTop);
					}, 50);
					
				};
				
				
				DocumentAssignmentPanelController.prototype.getScannedDocument=function(Id){
					var i=0,result={};
					for(;i<this.scannedDocuments.length;i++){
						if(this.scannedDocuments[i].uuid==Id){
							result= this.scannedDocuments[i];
							break;
						}
					}
					return result;
				}
				
				DocumentAssignmentPanelController.prototype.getSelectedPageUrl=function(){
					var url="#"			
					if(this.pageModel.selectedPage != null && this.pageModel.selectedPage.number){
						url = this.pageModel.selectedPage.url + 
						      (this.pageModel.selectedPage.number-1)+
						      '?' + this.pageModel.document.urlFragStamp || 0;
					}
					console.log(url);
					return url;
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
							debugger;
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
						
						/*If no attachment then bail*/
						if(attchId==""){
							return;
						}
						
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
								return jQuery("<div style='white-space: nowrap;width:200px;text-overflow:ellipsis;overflow: hidden;' class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
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
								return jQuery("<div style='white-space: nowrap;width:200px;text-overflow:ellipsis;overflow: hidden;' class='ui-widget-header dragHelper'><i class='fa fa-files-o' style='font-size: 14px;'></i> "
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
				
				/*Reverse pages in a document*/
				DocumentAssignmentPanelController.prototype.reversePages = function(document){
					var pages=[],
						that=this,
						i;

					for(i=document.pages.length;i>0;i--){
						pages.push(i);
					}
					
					DocumentAssignmentService.instance()
					.reorderDocument(document.uuid,pages)
		    		.then(function(result){
		    			console.log("Reverse Pages Success");
		    			console.log(result);
		    		})
		    		.fail(function(){
		    			//ZZM:TODO: handle error
		    		})
		    		.always(function(){
		    			document.urlFragStamp = new Date().getTime();
		    			that.refreshPagesList();
		    			that.safeApply();
			    		window.setTimeout(function() {
							that.bindDragAndDrop();
						}, 1000);
		    		});
					
				}
				
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
				
				
				DocumentAssignmentPanelController.prototype.getTimeStamp = function(){
					return new Date().getTime();
				}

				
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
