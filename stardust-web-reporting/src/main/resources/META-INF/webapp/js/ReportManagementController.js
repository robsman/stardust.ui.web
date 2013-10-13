/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ******************************************************************************/

define(
		[ "js/AngularAdapter", "js/ReportingService" ],
		function(AngularAdapter, ReportingService) {
			return {
				create : function(angular) {
					var controller = new ReportManagementController();
					var angularAdapter = new bpm.portal.AngularAdapter();

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
										self
												.openView(
														"reportDefinitionView",
														"name=Report Definition "
																+ self.newReportDefinitionIndex,
														"name=Report Definition "
																+ self.newReportDefinitionIndex);

										self.newReportDefinitionIndex++;
									});
					jQuery("#saveReports").click(function() {
						self.saveReportDefinitions();
					});
					jQuery("#refreshReports").click(function() {
						self.reloadReportDefinitions();
					});
					window.top.addEventListener('message', function(event) {
						if (event.data === "BPM-REPORTING-REPORT-CREATED") {
							self.reloadReportDefinitions();
						}
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
													valid_children : [ "report" ]
												},
												report : {
													icon : {
														image : this.reportingService
																.getRootUrl()
																+ "/plugins/bpm-reporting/images/icons/report-definition.png"
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
															action : function(
																	obj) {
																document.body.style.cursor = "wait";

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
																							.reloadReportDefinitions();
																					document.body.style.cursor = "default";
																				})
																		.fail(
																				function() {
																					document.body.style.cursor = "default";
																				});
																;
															}
														},
														download : {
															label : "Download", // I18N
															action : function(
																	obj) {
															}
														}
													};
												}
											}
										},
										themes : {
											"theme" : "custom",
											"url" : "../css/jsTreeCustom/style.css"
										}
									});

					jQuery("#reportTree").bind(
							"select_node.jstree",
							function(event, data) {
								if (data.rslt.obj.attr('rel') == 'report') {
									self.openView("reportDefinitionView",
											"name="
													+ data.rslt.obj
															.attr('name')
													+ "&path="
													+ data.rslt.obj
															.attr('path'),
											"name="
													+ data.rslt.obj
															.attr('name')
													+ "&path="
													+ data.rslt.obj
															.attr('path'));
								}
							});
					jQuery("#reportTree").bind(
							"rename_node.jstree",
							function(event, data) {
								if (data.rslt.obj.attr('rel') == 'report') {
									self.reportingService
											.renameReportDefinition(
													data.rslt.obj.attr("path"),
													data.rslt.name);
								}
							});
					this.reloadReportDefinitions();
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
				ReportManagementController.prototype.reloadReportDefinitions = function() {
					var self = this;

					document.body.style.cursor = "wait";

					this.reportingService.loadReportDefinitions().done(
							function(rootFolder) {
								self.rootFolder = rootFolder;

								console.log("Folder Structure");
								console.log(rootFolder);

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
					var folderNodeId = 0;
					var reportNodeId = 0;

					jQuery("#reportTree").empty();

					jQuery
							.each(
									this.rootFolder.subFolders,
									function(index, folder) {
										jQuery("#reportTree").jstree(
												"create",
												"#reportTree",
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
										}

										++folderNodeId;
									});

					jQuery(".showTooltip").tooltip();
					this.initializeDragAndDrop();
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.openView = function(
						viewId, viewParams, viewIdentity) {
					var link = jQuery("a[id $= 'view_management_link']",
							window.parent.frames['ippPortalMain'].document);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');

					link = this.getIPPPortalMainWindow().document
							.getElementById(linkId);

					var linkForm = this.getIPPPortalMainWindow().formOf(link);

					linkForm[formId + ':_idcl'].value = linkId;
					linkForm['viewParams'].value = viewParams;
					linkForm['viewId'].value = viewId;
					linkForm['viewIdentity'].value = viewIdentity;

					this.getIPPPortalMainWindow().iceSubmit(linkForm, link);
				};

				/**
				 * 
				 */
				ReportManagementController.prototype.getIPPPortalMainWindow = function() {
					return window.top.frames['ippPortalMain'];
				};
			}
		});