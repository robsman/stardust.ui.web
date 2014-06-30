/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "bpm-reporting/public/js/report/AngularAdapter",
				"bpm-reporting/public/js/report/ReportingService",
				"bpm-reporting/public/js/report/I18NUtils"],
		function(AngularAdapter, ReportingService, I18NUtils) {
			return {
				create : function(angular) {
					var controller = new ReportManagementController();
					var angularAdapter = new bpm.portal.AngularAdapter();

					//initialize controller and services
			        var angularModule = angularAdapter.initializeModule(angular);

			        //bootstrap module
			        angularAdapter.initialize(angular);
			        
					controller = angularAdapter
							.mergeControllerWithScope(controller);

					controller.initialize();
					
					return controller;
				}
			};

			/**
			 * 
			 */
			function ReportManagementController() {
				this.reportingService = ReportingService.instance();

				/**
				 * 
				 */
				ReportManagementController.prototype.initialize = function() {
					this.newReportDefinitionIndex = 1;

					var self = this;

					this.descriptionPopupDialog = jQuery("#descriptionPopupDialog");

					this.descriptionPopupDialog.dialog({
						autoOpen : false,
						draggable : false,
						model : true,
						resizable : false,
						title : "<div></div>"
					});

					jQuery("#createReport")
							.click(
									function() {
									   var reportUID = new Date().getTime(); 
										self
												.openView(
														"reportDefinitionView",
														"name=Report Definition "
																+ self.newReportDefinitionIndex
																+ "&reportUID=" + reportUID,
														"&reportUID=" + reportUID);

										self.newReportDefinitionIndex++;
									});
					jQuery("#saveReports").click(function() {
						self.saveReportDefinitions();
					});
					jQuery("#refreshReports").click(function() {
						self.loadReportDefinitionsFolderStructure();
					});
					window.parent.EventHub.events.subscribe("BPM-REPORTING-REPORT-CREATED", function(event) {
							self.loadReportDefinitionsFolderStructure();
					}, false);
					window.parent.EventHub.events.subscribe("BPM-REPORTING-REPORT-UPDATED", function(reportUID, newName) {
                  self.updateViewInfo(reportUID, "name=" + newName + "&reportUID=" + reportUID);
					   self.loadReportDefinitionsFolderStructure();
					}, false);
					jQuery("#reportTree")
							.jstree(
									{
										plugins : [ "themes", "html_data",
												"types", "ui", "crrm",
												"contextmenu" ],
										core : {
											"initially_open" : []
										},
										types : {
											"types" : {
												folder : {
													icon : {
														image : this.reportingService
																.getRootUrl()
																+ "/plugins/bpm-reporting/images/icons/folder.png"
													},
													valid_children : ["report", "folder"]
												},
												report : {
													icon : {
														image : this.reportingService
																.getRootUrl()
																+ "/plugins/bpm-reporting/public/css/images/report.png"
													},
													valid_children : []
												}
											}
										},
										contextmenu : {
											"items" : function(node) {
												if ('report' == node
														.attr('rel')) {
													return {
														ccp : false,
														create : false,
														rename : {
															label : "Rename", // I18N
															icon : self.reportingService.getRootUrl()
                                                         + "/plugins/views-common/images/icons/rename.png",
															action : function(
																	obj) {
																jQuery(
																		"#reportTree")
																		.jstree(
																				"rename",
																				"#"
																						+ obj
																								.attr("id"));
															}
														},
														deleteReport : {
															label : "Delete", // I18N
															icon : self.reportingService.getRootUrl()
                                                         + "/plugins/views-common/images/icons/delete.png",
															action : function(
																	obj) {
				                                       self.deleteElementAction(
				                                             obj.context.lastChild.data,
				                                             function() {
				                                                self.closeView(obj.attr("name"), obj.attr("path"));
				                                                self.reportingService
		                                                      .deleteReportDefinition(
		                                                            obj
		                                                                  .attr("path"))
		                                                      .done(
		                                                            function() {
		                                                               // TODO
		                                                               // Remove
		                                                               // locally
		                                                               // and
		                                                               // just
		                                                               // refresh
		                                                               // tree
		                                                               self
		                                                                     .loadReportDefinitionsFolderStructure();
		                                                               document.body.style.cursor = "default";
		                                                            })
		                                                      .fail(
		                                                            function() {
		                                                               document.body.style.cursor = "default";
		                                                            });
				                                             });
															         }
														},
														download : {
															label : "Download", // I18N
															icon : self.reportingService.getRootUrl()
                                                         + "/plugins/views-common/images/icons/page_white_put.png",
															action : function(
																	obj) {
																var path = obj.attr("path")
																self.reportingService.downloadReportDefinition(path);
															}
														},
														clone : {
                                             label : "Clone", // I18N
                                             icon : self.reportingService.getRootUrl()
                                                         + "/plugins/bpm-reporting/images/icons/report_add.png",
                                             action : function(
                                                   obj) {
                                                self.openView("reportDefinitionView",
                                                         "name="
                                                               + "Copy " + obj.attr("name")
                                                               + "&path=" + obj.attr("path")
                                                               + "&reportUID=" + new Date().getTime()
                                                               + "&isClone=" + true,
                                                         "&reportUID=" + new Date().getTime());
                                             }
                                          }
													};
												} else if ('folder' == node.attr('rel')) {
                                       return {
                                          ccp : false,
                                          create : false,
                                          rename : {
                                             label : "Upload", // I18N
                                             icon : self.reportingService.getRootUrl()
                                                         + "/plugins/views-common/images/icons/rename.png",
                                             action : function(
                                                   obj) {
                                                scope = angular.element(document).scope();
                                                parent.iPopupDialog.openPopup({
                                                   attributes : {
                                                      width : "50%",
                                                      height : "60%",
                                                      src : "../views-common/popups/fileUploadPopupDialogContent.html"
                                                   },
                                                   payload : {
                                                      title : "Upload a Document", //i18n
                                                      message : "Upload a Document", //i18n
                                                      documentTypeName : "Reference",
                                                      acceptFunction : function(fileUploadData)
                                                      {
                                                         scope.runInAngularContext(function(scope)
                                                         {
                                                            //Form Data
                                                            fileStorageUUID = fileUploadData.fileDetails.uuid;

                                                            self.reportingService.uploadReport(fileStorageUUID)
                                                            .done(
                                                                  function() {
                                                                     self
                                                                           .loadReportDefinitionsFolderStructure();
                                                                     document.body.style.cursor = "default";
                                                                  })
                                                         });

                                                         /*if(fileUploadData.openDocument){
                                                            self.openDocumentViewer(currentBindings[lastPart], false);
                                                         }*/
                                                      }
                                                   }
                                                });
                                             }
                                          }
                                       };
                                    
                                    }
											}
										},
										themes : {
											"theme" : "custom",
											"url" : "../public/css/jsTreeCustom/style.css"
										}
									});

					jQuery("#reportTree").bind(
							"select_node.jstree",
							function(event, data) {
								if (data.rslt.obj.attr('rel') == 'report') {
									self.openView("reportDefinitionView",
									         "name="
                                    + data.rslt.obj.attr('name')
                                    + "&path=" + data.rslt.obj.attr("path")
                                    + "&reportUID=" + data.rslt.obj.attr('reportUID'),
                                    "&reportUID=" + data.rslt.obj.attr('reportUID'));
								}
							});
					jQuery("#reportTree").bind(
							"rename_node.jstree",
							function(event, data) {
							   if (data.rslt.obj.attr('rel') == 'report')
							   {
							      self.data = data;

							      //Fetch the Report, update name and save it.
							      var deferred = jQuery.Deferred();
							      self.reportingService.retrieveReportDefinition(self.data.rslt.obj.attr("path")).done(
							               function(report)
							               {
							                  self.report = report;
							                  self.reportingService.renameReportDefinition(data.rslt.obj.attr("path"),
							                           data.rslt.name).done(
							                           function(updatedReportPath)
							                           {
							                              self.updatedReportPath = updatedReportPath;
							                              console.log(self.report);
							                              self.report.name = self.data.rslt.name;
							                              self.reportingService.saveReportDefinition(self.report).done(
							                                       function(report)
							                                       {
							                                          //Update View 
							                                          var newName = self.data.rslt.name;
							                                          var reportUID = self.data.rslt.obj.attr("reportUID");
							                                          
							                                          self.updateViewInfo(reportUID, "name="
							                                                   + newName + "&reportUID="
							                                                   + reportUID);
							                                          
							                                          window.parent.EventHub.events.publish("BPM-REPORTING-REPORT-NAME-UPDATED",
							                                                   newName, self.updatedReportPath);

							                                          self.loadReportDefinitionsFolderStructure();
							                                          self.updateView();
							                                       });

							                              deferred.resolve();
							                           }).fail(function()
							                  {
							                     deferred.reject();
							                  });

							                  document.body.style.cursor = "default";
							               }).fail(function()
							      {
							         document.body.style.cursor = "default";
							      });
							   }

							});
					this.loadReportDefinitionsFolderStructure();
					this.loadCustomTheme();
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.loadCustomTheme = function() {
				    var self = this;
				
				    jQuery.ajax({
				        type: 'GET',
				        url: self.reportingService.getRootUrl() + "/services/rest/common/html5/api/themes/current/custom",
				        async: true
				    }).done(function(json) {
				        var head = document.getElementsByTagName('head')[0];
				
				        for (var i in json.stylesheets) {
				            var link = document.createElement('link');
				            link.href = self.reportingService.getRootUrl() + "/" + json.stylesheets[i];
				            link.rel = 'stylesheet';
				            link.type = 'text/css';
				            head.appendChild(link);
				        }
				    }).fail(function(err) {
				        m_utils.debug("Failed in loading custom theme");
				    });
				};
				
				/**
				 * 
				 */
				ReportManagementController.prototype.initializeDragAndDrop = function() {
					var IE = document.all ? true : false;

					if (!IE) {
						document.captureEvents(Event.MOUSEMOVE);
					} else {
						document.ondragstart = function() {
							return false;
						};
					}

					document.onmousemove = function(e) {
						// TODO Make portable/modularize

						if (parent != null && parent.iDnD != null) {
							if (e) {
								parent.iDnD.setIframeXY(e, window.name);
							} else {
								parent.iDnD.setIframeXY(window.event,
										window.name);
							}
						}
					};

					document.onmouseup = function() {
						// TODO Make portable/modularize

						if (parent != null && parent.iDnD != null) {
							parent.iDnD.dragMode = false;
							parent.iDnD.hideIframe();
						}
					};

					jQuery("a")
							.mousedown(
									function(e) {
										if (jQuery(this).parent().attr(
												'draggable')) {

											if (e.preventDefault) {
												e.preventDefault();
											}

											var insElem = this.childNodes[0];
											var textElem = jQuery(this.childNodes[1])[0];
											var bgImage = jQuery(insElem).css(
													'background-image');
											bgImage = bgImage.substring(4,
													(bgImage.length - 1));

											// Strip double
											// quotes (for
											// FF and
											// IE)

											bgImage = bgImage
													.replace(/\"/g, "");

											parent.iDnD.setDrag();
											parent.iDnD.dragMode = true;

											var transferObj = {
												'type' : "report",
												'path' : jQuery(insElem)
														.parent().parent()
														.attr("path"),
												'attr' : {}
											};

											parent.iDnD
													.setTransferObject(transferObj);
											parent.iDnD.setImageToDrag(bgImage,
													textElem.nodeValue);
										}
									});
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.saveReportDefinitions = function() {
					var self = this;

					document.body.style.cursor = "wait";

					this.reportingService.saveReportDefinitions().done(
							function() {
								self.refreshTree();

								document.body.style.cursor = "default";
							}).fail(function() {
						document.body.style.cursor = "default";
					});
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.loadReportDefinitionsFolderStructure = function() {
					var self = this;

					document.body.style.cursor = "wait";

					this.reportingService
							.loadReportDefinitionsFolderStructure()
							.done(function() {
								console.log("Folder Structure");
								console.log(self.reportingService.rootFolder);

								self.refreshTree();

								document.body.style.cursor = "default";
							}).fail(function() {
								document.body.style.cursor = "default";
							});
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.refreshTree = function() {
					jQuery("#reportTree").empty();
					
					createTree1(this.reportingService.rootFolder, {"folderNodeId" : 0, "reportNodeId": 0}, "#reportTree");
					
					jQuery(".showTooltip").tooltip();
					this.initializeDragAndDrop();
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.openView = function(
						viewId, viewParams, viewIdentity) {
					viewIdentity = viewIdentity.replace(/\//g, "");
					viewIdentity = viewIdentity.replace(/ /g, "");

					var portalWinDoc = this.getOutlineWindowAndDocument();
					var link = jQuery("a[id $= 'view_management_link']",
							portalWinDoc.doc);

					console.debug("Link");
					console.debug(link);

					var linkId = link.attr('id');
					var form = link.parents('form:first');

					console.debug("Form");
					console.debug(form);
					
					var formId = form.attr('id');

					console.debug("Form ID");
					console.debug(formId);

					link = portalWinDoc.doc
							.getElementById(linkId);

					var linkForm = portalWinDoc.win
							.formOf(link);

					console.debug("Link Form");
					console.debug(linkForm);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					portalWinDoc.win
							.iceSubmit(linkForm, link);
				};

				/*
				 * 
				 */
				ReportManagementController.prototype.getOutlineWindowAndDocument = function() {
					return {
						win : parent.document
								.getElementById("portalLaunchPanels").contentWindow,
						doc : parent.document
								.getElementById("portalLaunchPanels").contentDocument
					};
//					return {
//						win : window.top.frames['ippPortalMain'],
//						doc : window.top.frames['ippPortalMain'].document
//					};
				};
				
		      ReportManagementController.prototype.deleteElementAction = function (name, callback) {
	               if (parent.iPopupDialog) {
	                  parent.iPopupDialog.openPopup(this.prepareDeleteElementData(
	                        name, callback));
	               } else {
	                  
	                  callback();
	               }
	            };
	         
	         ReportManagementController.prototype.prepareDeleteElementData = function(name, callback) {
	            var popupData = {
	               attributes : {
	                  width : "400px",
	                  height : "200px",
	                  src : this.reportingService.getRootUrl()
	                        + "/plugins/bpm-reporting/popups/confirmationPopupDialogContent.html"
	               },
	               payload : {
	                  title : "Confirm"/*m_i18nUtils
	                        .getProperty("modeler.messages.confirm")*/,
                     message : I18NUtils.getProperty(
                                 'reporting.messages.confirm.deleteElement').replace(
                                 "{0}", name),
	                     
	                  acceptButtonText : "Yes"/*m_i18nUtils
	                        .getProperty("modeler.messages.confirm.yes")*/,
	                  cancelButtonText : "Cancel"/*m_i18nUtils
	                        .getProperty("modeler.messages.confirm.cancel")*/,
	                  acceptFunction : callback
	               }
	            };

	            return popupData;
	         };
	         
	         /**
             * 
             */
            ReportManagementController.prototype.closeView = function(
                     name, path) {
               var link = jQuery("a[id $= 'views_close_link']", this.getOutlineWindowAndDocument().doc);
               var linkId = link.attr('id');
               var form = link.parents('form:first');
               var formId = form.attr('id');
               
               var portalWinDoc = this.getOutlineWindowAndDocument();
               
               var link = portalWinDoc.doc.getElementById(linkId);
               var linkForm = portalWinDoc.win.formOf(link);

               linkForm[formId + ':_idcl'].value = linkId;
               linkForm['name'].value = name;
               linkForm['path'].value = path;

               portalWinDoc.win.iceSubmit(linkForm, link);
            };
            
            
            /**
             * 
             */
            ReportManagementController.prototype.updateViewInfo = function(
                     reportUID, viewParams) {
               var link = jQuery("a[id $= 'view_updater_link']", this.getOutlineWindowAndDocument().doc);
               var linkId = link.attr('id');
               var form = link.parents('form:first');
               var formId = form.attr('id');
               
               var portalWinDoc = this.getOutlineWindowAndDocument();
               
               var link = portalWinDoc.doc.getElementById(linkId);
               var linkForm = portalWinDoc.win.formOf(link);

               linkForm[formId + ':_idcl'].value = linkId;
               linkForm['reportUID'].value = reportUID;
               linkForm['viewParams'].value = viewParams;

               portalWinDoc.win.iceSubmit(linkForm, link);
            };
            
			}
			
			function createTree1(folder, trackIds, parent) {
				jQuery.each(folder.subFolders,
					function(index, folder) {
						trackIds = createTree2(index, folder, trackIds, parent);
					});
				return trackIds;
			}
			
			function createTree2(index, folder, trackIds, parent) {
				var folderNodeId = trackIds.folderNodeId;
				var reportNodeId = trackIds.reportNodeId;
								
				jQuery("#reportTree").jstree(
						"create",
						 parent,
						"first",
						{
							attr : {
								id : "folder"
										+ folderNodeId,
								rel : "folder",
								elementId : folder.id
							},
							data : folder.name
						}, null, true);

				if (folder.reportDefinitions) {
					jQuery
							.each(
									folder.reportDefinitions,
									function(index,
											reportDefinition) {
										jQuery(
												"#reportTree")
												.jstree(
														"create",
														"#folder"
																+ folderNodeId,
														"last",
														{
															attr : {
															   reportUID : reportDefinition.reportUID,
																id : "report"
																		+ reportNodeId,
																rel : "report",
																elementId : reportDefinition.id,
																name : reportDefinition.name,
																path : reportDefinition.path,
																draggable : true
															},
															data : {
																attr : {
																	"class" : "showTooltip",
																	title : reportDefinition.description
																},
																title : reportDefinition.name
															}
														},
														null,
														true);
										++reportNodeId;
									});
				} else if (folder.subFolders) {
					var parent = "#folder"+ folderNodeId;
					++folderNodeId;
					trackIds  = createTree1(folder, {"folderNodeId" : folderNodeId, "reportNodeId": ++reportNodeId}, parent);
					folderNodeId = trackIds.folderNodeId; 
					reportNodeId = trackIds.reportNodeId;
				}

				++folderNodeId;
				
				return {"folderNodeId" : folderNodeId, "reportNodeId": reportNodeId};
			};
			
		});