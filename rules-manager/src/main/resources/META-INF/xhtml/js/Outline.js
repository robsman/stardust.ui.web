/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_process",
				"bpm-modeler/js/m_application", "bpm-modeler/js/m_participant",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_data",
				"bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_jsfViewManager",
				"bpm-modeler/js/m_messageDisplay",
				"rules-manager/js/m_i18nUtils",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_jsfViewManagerHelper",
				"rules-manager/js/m_outlineToolbarController",
				"rules-manager/js/CommandsDispatcher",
				"rules-manager/js/RuleSet",
				"rules-manager/js/m_i18nMapper"],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_session, m_user, m_model, m_process, m_application,
				m_participant, m_typeDeclaration, m_data, m_elementConfiguration, m_jsfViewManager,
				m_messageDisplay, m_i18nUtils, m_communicationController, m_jsfViewManagerHelper,
				m_outlineToolbarController, CommandsDispatcher, RuleSet, m_i18nMapper) {
			var isElementCreatedViaOutline = false;
			var hasUnsavedModifications = false;

			var displayScope = "";
			var viewManager;

			var readAllRuleSets = function(force) {

				// Needed for types

				m_model.loadModels(false);

				jQuery("#lastsave").text(m_i18nUtils
										.getProperty("rules.outline.labels.lastSave"));
	
				jQuery.each(RuleSet.getRuleSets(force),
					function(index, ruleSet) {

						if(ruleSet.state.isDeleted===false){
							
							createRuleSetNode(ruleSet);
						
							if (ruleSet.technicalRules) {
								jQuery.each(ruleSet.technicalRules, function(index, techRule) {
									createTechinicalRuleNode(ruleSet, techRule);
								});	
							}
							
							if (ruleSet.decisionTables) {
								jQuery.each(ruleSet.decisionTables, function(index, decTable) {
									createDecisionTableNode(ruleSet, decTable);
								});	
							}
							
							m_utils.jQuerySelect(displayScope + "#outline").jstree("close_node", "#" + ruleSet.uuid);
						} /*If ruleSet.state.isdeleted condition check end*/
					}); /*JQUERY Each->RuleSet loop end*/
				
				m_utils.debug("Tree initialized");

				hasUnsavedModifications = false;
				jQuery("#undoChange").addClass("toolDisabled");
				jQuery("#redoChange").addClass("toolDisabled");
			};
			
			var createRuleSetNode = function(ruleSet) {
				jQuery(displayScope + "#outline").jstree("create", displayScope + "#outline",
						"first", {
							"attr" : {
								"id" : ruleSet.uuid,
								"rel" : ruleSet.type,
								"elementId" : ruleSet.id,
								"title": ruleSet.description
							},
							"data" : ruleSet.name
						}, null, true);
				jQuery(displayScope + "#outline").jstree("set_type", "ruleSet",
						"#" + ruleSet.uuid);
			};
			
			var createDecisionTableNode = function(ruleSet, decTable) {
				jQuery(displayScope + "#outline").jstree("create",
						"#" + ruleSet.uuid, "last", {
							"attr" : {
								"id" : decTable.uuid,
								"title" : decTable.description,
								"ruleSetId" : ruleSet.id,
								"ruleSetUuid" : ruleSet.uuid,
								"rel" : "DecisionTable",
								"draggable" : true,
								"elementId" : decTable.id
							},
							"data" : decTable.name
						}, null, true);
			};
			
			var createTechinicalRuleNode = function(ruleSet, techRule) {
				jQuery(displayScope + "#outline").jstree("create",
						"#" + ruleSet.uuid, "last", {
							"attr" : {
								"id" : techRule.uuid,
								"title": techRule.description,
								"ruleSetId" : ruleSet.id,
								"ruleSetUuid" : ruleSet.uuid,
								"rel" : "TechnicalRule",
								"draggable" : true,
								"elementId" : techRule.id
							},
							"data" : techRule.name
						}, null, true);
			};
			
			var exportRuleSet = function(uuid) {
				var ruleSet = RuleSet.findRuleSetByUuid(uuid);

				if (!areRuleSetsSaved()) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog
								.openPopup({
									attributes : {
										width : "400px",
										height : "200px",
										src : m_urlUtils.getPlugsInRoot()
												+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
									},
									payload : {
										title : "Warning",
										message : "Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing.",
										acceptButtonText : "Close",
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert("Rule-sets have unsaved changes. Please save rule-sets before continuing.");
					}
				} else {
					if (ruleSet) {
						window.location = m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/ruleSet/" + encodeURIComponent(ruleSet.uuid) + "/download"
					}	
				}
			}

			var areRuleSetsSaved = function() {
				var saved = true;
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					if (ruleSet.state &&
							(ruleSet.state.isDirty == true
									|| ruleSet.state.isPersisted == false)) {
						saved = false;
					}
				});
				
				return saved;
			};
			
			// TODO Is this still needed? Delete after verifying
			var elementCreationHandler = function(id, name, type, parent) {
				if (type == 'activity') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "manual_activity",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == "subProcessActivity") {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "sub_process_activity"
								},
								"data" : name
							}, null, true);
				} else if (type == 'primitiveDataType') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "Primitive_Data",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == 'role') {
					var parentSelector = '#' + parent;
					jQuery(displayScope + "#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									rel : "participant_role",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				}
			};

			var elementRenamingHandler = function(attrs) {
				if (attrs.action == 'Rename') {
					var rLink = jQuery("li#" + attrs.id + " a")[0];
					var textElem = jQuery(rLink.childNodes[1])[0];
					textElem.nodeValue = attrs.props.completetext;
				}
			};

			var renameNodeHandler = function(event, data) {
				var nodeType=data.rslt.obj.attr('rel');
				if (nodeType == 'ruleSet') {
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("id"));
					var oldName = ruleSet.name;
					var newName = data.rslt.name;
					ruleSet.name = newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					CommandsDispatcher.submitCommand({
						name:"RuleSet.Rename",
						ruleSet:ruleSet,
						changes:[oldName,newName]
					});
				} 
				else if(nodeType=="DecisionTable"){
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("ruleSetUuid"));
					var decTable = ruleSet.findDecisionTableByUuid(data.rslt.obj.attr("id"));
					var oldName=decTable.name;
					var newName=data.rslt.name;
					decTable.name=newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					CommandsDispatcher.submitCommand({
						name:"DecisionTable.Rename",
						decTable:decTable,
						ruleSet:ruleSet,
						changes:[oldName,newName]
					});
					
				}
				else if(nodeType=="TechnicalRule"){
					var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("ruleSetUuid"));
					var techRule = ruleSet.findTechnicalRuleByUuid(data.rslt.obj.attr("id"));
					var oldName=techRule.name;
					var newName=data.rslt.name;
					techRule.name=newName;
					if(oldName != newName){
						ruleSet.state.isDirty=true;
					}
					CommandsDispatcher.submitCommand({
						name:"TechnicalRule.Rename",
						techRule:techRule,
						ruleSet:ruleSet,
						changes:[oldName,newName]
					});
					
				}
			};

			var renameView = function(viewId, viewIdentifier, nameParamName,
					newName) {
				viewManager.updateView(viewId, nameParamName + "=" + newName,
						viewIdentifier);
			}

			var refresh = function() {
				if (parent.iPopupDialog) {
					parent.iPopupDialog
							.openPopup({
								attributes : {
									width : "400px",
									height : "200px",
									src : m_urlUtils.getPlugsInRoot()
											+ "bpm-modeler/popups/outlineRefreshConfirmationDialog.html"
								},
								payload : {
									title : "Confirm",
									message : "All rule-sets will be reloaded from their last saved state and the session log will be cleared.<BR><BR>Continue?<BR><BR>",
									acceptButtonText : "Yes",
									cancelButtonText : "No",
									acceptFunction : reloadOutlineTreeReset
								}
							});
				}
			};
			
			var reloadOutlineTreeReset = function(saveFirst) {
				if (true == saveFirst) {
					saveRuleSets();
				}

				// close all rules views, if open
				closeAllRulesViews();

				reloadOutlineTree(true);
			};
			
			var closeAllRulesViews = function() {
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					viewManager.closeViewsForElement(ruleSet.uuid);
				});
			};
			
			var reloadOutlineTree = function(force) {
				jQuery(displayScope + "#outline").empty();
				readAllRuleSets(force);
			};

			var importRuleSet = function() {
				if (!areRuleSetsSaved()) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog
								.openPopup({
									attributes : {
										width : "400px",
										height : "200px",
										src : m_urlUtils.getPlugsInRoot()
												+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
									},
									payload : {
										title : "Warning",
										message : "Rule-sets have unsaved changes.<BR><BR>Please save rule-sets before continuing.",
										acceptButtonText : "Close",
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert("Rule-sets have unsaved changes. Please save rule-sets before continuing.");
					}
				} else {
					var link = m_utils.jQuerySelect(
							"a[id $= 'open_rules_upload_dialog_link']",
							m_utils.getOutlineWindowAndDocument().doc);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');
					m_jsfViewManagerHelper
							.openImportModelDialog(linkId, formId);
				}
			}

			var undoMostCurrent = function() {
				m_communicationController.postData({
					url : m_communicationController.getEndpointUrl()
							+ "/sessions/changes/mostCurrent/navigation"
				}, "undoMostCurrent", {
					success : function(data) {
						m_utils.debug("Undo");
						m_utils.debug(data);

						m_commandsController.broadcastCommandUndo(data);

						if (null != data.pendingUndo) {
							jQuery("#undoChange").removeClass("toolDisabled");
						} else {
							jQuery("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							jQuery("#redoChange").removeClass("toolDisabled");
						} else {
							jQuery("#redoChange").addClass("toolDisabled");
						}
					}
				});
			}

			var redoLastUndo = function() {
				m_communicationController.postData({
					url : m_communicationController.getEndpointUrl()
							+ "/sessions/changes/mostCurrent/navigation"
				}, "redoLastUndo", {
					success : function(data) {
						m_utils.debug("Redo");
						m_utils.debug(data);

						m_commandsController.broadcastCommand(data);

						if (null != data.pendingUndo) {
							jQuery("#undoChange").removeClass("toolDisabled");
						} else {
							jQuery("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							jQuery("#redoChange").removeClass("toolDisabled");
						} else {
							jQuery("#redoChange").addClass("toolDisabled");
						}
					}
				});
			}

			function getRuleSets() {
				var ruleSetArray = [];
				jQuery.each(RuleSet.getRuleSets(), function(index, ruleSet) {
					var ruleSetClone = {};
					jQuery.each(ruleSet, function(i, member) {
						if (typeof member != "function") {
							ruleSetClone[i] = member;	
						}						
					});
					ruleSetArray.push(ruleSetClone);
				});
				
				return ruleSetArray;
			}
			
			function saveRuleSets(deletedOnly,virginOnly) {
				var rsArray=[], /* transformed ruleSets we will post to the server*/
					refRsArray=[]; /* ref to original ruleSets we will operate on within the success callback*/
				
				deletedOnly=deletedOnly || false;
				virginOnly=virginOnly || false;
				
				//Convert each RuleSet to its transformed JSON object
				jQuery.each(RuleSet.getRuleSets(),function(){
					
					/* Detect rulesets that need to be deleted from persistant storage
					 * No matter our flags we will always try and delete ruleSets marked
					 * as such.*/
					if(this.state.isDeleted===true && 
					   this.state.isPersisted===true){
						refRsArray.push(this);
						rsArray.push({
							id: this.id,
							uuid: this.uuid,
							name: this.name,
							deleted: true
						});
					}
					
					/* Now, if we arent in a delete only mode, look for ruleSets that need to be persisted.
					 * More exactly..., if in virginOnly mode we will only look for ruleSets that have not been
					 * persisted as of yet and are not marked for deletion, else we look for all dirty ruleSets
					 * not marked for deletion.*/
					if(deletedOnly===false){
						/* Detect only those rulesets that are not persistent and not deleted (virgin).*/
						if(this.state.isPersisted===false && 
								this.state.isDeleted===false){
							refRsArray.push(this);
							rsArray.push(this.toJSON("PRE-DRL"));
						}
						/* Finally, rulesets that are persistent and have changes (are dirty)*/
						if(virginOnly===false){
							if(this.state.isPersisted===true && 
									 this.state.isDirty===true && 
									 this.state.isDeleted===false){
								refRsArray.push(this);
								rsArray.push(this.toJSON("PRE-DRL"));
							}
						}
					}
				});
				console.log("--RSArray--");
				console.log(rsArray);
				m_communicationController
						.syncPostData(
								{
									url : m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/save"
								},
								JSON.stringify(rsArray),
								new function() {
									return {
										success : function(data) {
											console.log(data);
											m_messageDisplay.markSaved();
											hasUnsavedModifications = false;
											/*very important we iterate over the refRSArray as it is prefiltered.*/
											jQuery.each(refRsArray,function(){
												/*Server responded with success, hard delete ruleSets
												 *marked for deletion and clean the states of 
												 *all other Rulesets.*/
												if(this.state.isDeleted===true){
													RuleSet.deleteRuleSet(this.uuid);
												}
												else{
													this.state.isPersisted=true;
													this.state.isDirty=false;
												}
											});
										},
										failure : function(data) {
											if (parent.iPopupDialog) {
												parent.iPopupDialog
														.openPopup(prepareErrorDialogPoupupData(
																"Error saving rule-sets.",
																"OK"));
											} else {
												alert("Error saving rule-sets.");
											}
										}
									}
								});
			}

			var setupEventHandling = function() {
				/* Listen to toolbar events */
				jQuery(document).bind('TOOL_CLICKED_EVENT',
						function(event, data) {
							handleToolbarEvents(event, data);
						});

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
							parent.iDnD.setIframeXY(window.event, window.name);
						}
					}
				};

				document.onmouseup = function() {
					// TODO Make portable/modularize

					if (parent != null && parent.iDnD != null) {
						parent.iDnD.hideIframe();
					}
				};

				// Tree Node Selection

				jQuery(displayScope + "#outline")
						.bind(
								"select_node.jstree",
								function(event, data) {
									if (data.rslt.obj.attr('rel') == 'ruleSet') {
										var ruleSet = RuleSet.findRuleSetByUuid(data.rslt.obj.attr("id"));
										viewManager.openView("ruleSetView",
												"id=" + ruleSet.id + "&name="
														+ ruleSet.name
														+ "&uuid="
														+ ruleSet.uuid,
												ruleSet.uuid);
									}
									else if (data.rslt.obj.attr('rel') == "TechnicalRule") {
										var ruleSet = RuleSet
												.findRuleSetByUuid(data.rslt.obj
														.attr("ruleSetUuid"));

										var techRule = ruleSet
												.findTechnicalRuleByUuid(data.rslt.obj.attr("id"));

										viewManager.openView("technicalRuleView", "id="
												+ techRule.id + "&ruleSetId="
												+ ruleSet.id + "&name="
												+ techRule.name + "&uuid="
												+ techRule.uuid + "&ruleSetUuid="
												+ ruleSet.uuid + "&parentUUID=" + ruleSet.uuid, techRule.uuid);
									} 
									else if (data.rslt.obj.attr('rel') == "DecisionTable") {
										var ruleSet = RuleSet
												.findRuleSetByUuid(data.rslt.obj
														.attr("ruleSetUuid"));

										var decTable = ruleSet
												.findDecisionTableByUuid(data.rslt.obj.attr("id"));

										viewManager.openView("decisionTableView", "id="
												+ decTable.id + "&ruleSetId="
												+ ruleSet.id + "&name="
												+ decTable.name + "&uuid="
												+ decTable.uuid + "&ruleSetUuid="
												+ ruleSet.uuid + "&parentUUID=" + ruleSet.uuid, decTable.uuid);
									} 
									else {
										m_utils.debug("No View defined for "
												+ data.rslt.obj.attr('rel'));
									}

									jQuery("a")
											.mousedown(
													function(e) {

														if (jQuery(this)
																.parent()
																.attr(
																		'draggable')) {
															if (e.preventDefault) {
																e
																		.preventDefault();
															}
															var insElem = this.childNodes[0];
															var textElem = jQuery(this.childNodes[1])[0];
															var bgImage = jQuery(
																	insElem)
																	.css(
																			'background-image');
															bgImage = bgImage
																	.substring(
																			4,
																			(bgImage.length - 1));

															// Strip double
															// quotes (for FF
															// and IE)
															bgImage = bgImage
																	.replace(
																			/\"/g,
																			"");

															// parent.iDnD.drawIframeAt(e,
															// window.name);
															parent.iDnD
																	.setDrag();
															var transferObj = {
																'elementType' : jQuery(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				'rel'),
																'elementId' : jQuery(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				"elementId"),
																'attr' : {}
															};

															if (transferObj.elementType == "Plain_Java_Application") {
																transferObj.attr.accessPoint = jQuery(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				'accessPoint');
															}

															transferObj.attr.fullId = jQuery(
																	insElem)
																	.parent()
																	.parent()
																	.attr(
																			"fullId");

															parent.iDnD
																	.setTransferObject(transferObj);
															parent.iDnD
																	.setImageToDrag(
																			bgImage,
																			textElem.nodeValue);
														}
													});
								})
						.bind("rename_node.jstree", function(event, data) {
							renameNodeHandler(event, data);
						})
						.jstree(
								{
									core : {
										animation : 0
									},
									"plugins" : [ "themes", "html_data",
											"crrm", "contextmenu", "types",
											"ui" ],
									contextmenu : {
										"items" : function(node) {
											var nodeType=node.attr('rel');
											if("DecisionTable"===nodeType || "TechnicalRule"===nodeType){
												return{
													"ccp":false,
													"create":false,										
													"delete":{
														"label":m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.delete","Delete"),
														"action": function(obj){
															deleteElementAction(obj.context.lastChild.data,
																function(){
																	var ruleSet = RuleSet.findRuleSetByUuid(obj.attr("ruleSetUuid")),
																	    id=obj.attr("id");
																	if(nodeType==="DecisionTable"){
																		ruleSet.deleteDecisionTable(id);
																	}else if(nodeType==="TechnicalRule"){
																		ruleSet.deleteTechnicalRule(id);
																	}
																	viewManager.closeViewsForElement(obj.attr("id"));
																	jQuery(displayScope + "#outline")
																		.jstree("delete_node","#"+ id);
																});
														}
													},
													"rename":{
														"label":m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.rename","Rename"),
														"action":function(obj){
															/*TODO:Rename Portal Label*/
															/*Rename of View is handled on rename_node event of tree*/
															jQuery(displayScope + "#outline").jstree("rename","#"+ obj.attr("id"));
														}
													}
												}
											}
											if ('ruleSet' == node.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,													
													"createTechnicalRule" : {
														"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.createRule","Create Rule"),
														"action" : function(obj) {
															var techRule;
															techRule=createTechnicalRule(obj.attr("id"));
															/*set new tree node for user editing*/
															jQuery(displayScope + "#outline").jstree("rename","#" + techRule.uuid);
														}
													},
													"createDecisionTable" : {
														"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.createDecisionTable","Create Decision Table"),
														"action" : function(obj) {
															var decTable;
															decTable=createDecisionTable(obj.attr("id"));
															/*set new tree node for user editing*/
															jQuery(displayScope + "#outline").jstree("rename","#" + decTable.uuid);
															//var inst=jQuery.jstree._reference(displayScope + "#outline");
															//inst.sort(jQuery(displayScope + "#outline"));//asdads//
														}
													},
													"rename" : {
														"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.rename","Rename"),
														"action" : function(obj) {
															jQuery(displayScope + "#outline")
																	.jstree("rename","#"+ obj.attr("id"));
														}
													},
													"deleteRuleSet" : {
														"label" : m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.delete","Delete"),
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteRuleSet(obj.attr("id"));
																		viewManager.closeViewsForElement(obj.attr("id"));
																	});
														}
													},
													"export RuleSet" : {
														"label": m_i18nUtils.getProperty("rules.outline.ruleSet.contextMenu.export","Export Rule Set"),
														"action": function(obj){
															exportRuleSet(obj.attr("id"))
														},
														"_class" : "ipp-text-red"
													}
												};
											}

											return {};
										}
									},
									types : {
										"types" : {
											"ruleSet" : {
												"icon" : {
													"image" : m_urlUtils
															.getPlugsInRoot()
															+ "rules-manager/images/icons/rule-set.png"
												},
												"valid_children" : [ "rule","TechnicalRule","DecisionTable" ]
											},
											"rule" : {
												"icon" : {
													"image" : m_urlUtils
															.getPlugsInRoot()
															+ "rules-manager/images/icons/rule.png"
												}
											},
											"TechnicalRule" : {
												"icon" : {
													"image" : m_urlUtils
															.getPlugsInRoot()
															+ "rules-manager/images/icons/script_gear.png"
												}
											},
											"DecisionTable":{
												"icon" : {
													"image" : m_urlUtils
															.getPlugsInRoot()
															+ "rules-manager/images/icons/dt_icon.png"
												}
											}
										}
									},
									"themes" : {
										"theme" : "custom",
										"url" : m_urlUtils.getPlugsInRoot() + "bpm-modeler/css/jsTreeCustom/style.css"
									}
								});
				// "themes" : {
				// "theme" : "default",
				// "url" : "/xhtml/css/jstree"}}).jstree("set_theme",
				// "default");

				var handleToolbarEvents = function(event, data) {
					if ("createRuleSet" == data.id) {
						createRuleSet();
					} else if ("importRuleSet" == data.id) {
						importRuleSet();
					} else if ("undoChange" == data.id) {
						undoMostCurrent();
					} else if ("redoChange" == data.id) {
						redoLastUndo();
					} else if ("saveAllRules" == data.id) {
						saveRuleSets();
					} else if ("refreshRules" == data.id) {
						refresh();
					}
				};

				/**
				 * 
				 */
				function prepareInfoDialogPoupupData(msg, okText) {
					return {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/notificationDialog.html"
						},
						payload : {
							title : "Info",
							message : msg,
							okButtonText : okText
						}
					}
				}

				/**
				 * 
				 */
				function prepareErrorDialogPoupupData(msg, okText) {
					return {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/errorDialog.html"
						},
						payload : {
							title : "Error",
							message : msg,
							okButtonText : okText
						}
					}
				}

				/* Marks a rulesets state object as isDeleted=true.
				 * calls saveAllRules with the deleteOnly flag set. THis will cause the method to only
				 * send ruleSets we have marked for deletion. The hard delete is performed by the 
				 * success callback intenal to the method.*/
				function deleteRuleSet(ruleSetUUID) {
					RuleSet.markRuleSetForDeletion(ruleSetUUID);
					jQuery(displayScope + "#outline").jstree("delete_node", "#"+ ruleSetUUID);
					saveRuleSets(true); /*deleteOnly=true*/
				}

				function prepareDeleteElementData(name, callback) {
					var popupData = {
						attributes : {
							width : "400px",
							height : "200px",
							src : m_urlUtils.getPlugsInRoot()
									+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
						},
						payload : {
							title : "Confirm",
							message : "Are you sure you want to delete "
									+ name
									+ "?<BR>This change cannot be undone.<BR><BR>Continue?",
							acceptButtonText : "Yes",
							cancelButtonText : "Cancel",
							acceptFunction : callback
						}
					};

					return popupData;
				}

				function deleteElementAction(name, callback) {
					if (parent.iPopupDialog) {
						parent.iPopupDialog.openPopup(prepareDeleteElementData(
								name, callback));
					} else {
						callback();
					}
				}
				
				function createDecisionTable(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	decTableCount=ruleSet.getDecisionTableCount();
					var decName =m_i18nUtils.getProperty("rules.object.decisiontable.name","Decision Table");
					var	name =decName + " " + decTableCount;
					var	id=decName.replace(/\s/g,"") + decTableCount;
					var decTable=ruleSet.addDecisionTable(id,name);
					
					//CommandsDispatcher.submitCommand();					
					createDecisionTableNode(ruleSet, decTable);
					
					viewManager.openView("decisionTableView", "id="
							+ decTable.id + "&ruleSetId="
							+ ruleSet.id + "&name="
							+ decTable.name + "&uuid="
							+ decTable.uuid + "&ruleSetUuid="
							+ ruleSet.uuid + "&parentUUID=" + ruleSet.uuid, decTable.uuid);
					return decTable;
				}
				
				function createTechnicalRule(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	techRuleCount=1+ ruleSet.getTechnicalRuleCount();
					var trName=m_i18nUtils.getProperty("rules.object.technicalrule.name","Rule");
					var	name =trName + " " + techRuleCount;
					var	id=trName.replace(/\s/g,"") + techRuleCount;
					var techRule=ruleSet.addTechnicalRule(id,name);
					
					//CommandsDispatcher.submitCommand();					
					createTechinicalRuleNode(ruleSet, techRule);
					
					viewManager.openView("technicalRuleView", "id="
							+ techRule.id + "&ruleSetId="
							+ ruleSet.id + "&name="
							+ techRule.name + "&uuid="
							+ techRule.uuid + "&ruleSetUuid="
							+ ruleSet.uuid + "&parentUUID=" + ruleSet.uuid, techRule.uuid);
					
					return techRule;
				}
				
				function changeProfileHandler(profile) {
					m_user.setCurrentRole(profile);
					m_commandsController.broadcastCommand(m_command
							.createUserProfileChangeCommand(profile));
				}

				if (window.parent.EventHub != null) {
					window.parent.EventHub.events.subscribe("CHANGE_PROFILE",
							changeProfileHandler);
					window.parent.EventHub.events.subscribe("RELOAD_RULES",
							reloadOutlineTreeReset);
				}

				readAllRuleSets();
			};

			
			var outline;

			return {
				init : function(newViewManager, newDisplayScope,options) {
					var uiElements={
							createRuleSetButton:  m_utils.jQuerySelect(options.selectors.createRuleSetButton),
							importRuleSet:  m_utils.jQuerySelect(options.selectors.importRuleSet),
							undoChange:  m_utils.jQuerySelect(options.selectors.undoChange),
							redoChange:  m_utils.jQuerySelect(options.selectors.redoChange),
							saveAllRules:  m_utils.jQuerySelect(options.selectors.saveAllRules),
							refreshRules:  m_utils.jQuerySelect(options.selectors.refreshRules),
							lastSavelLabel:  m_utils.jQuerySelect(options.selectors.lastsave)
					};
					
					m_i18nMapper.map(options,uiElements,true);
					
					if (newDisplayScope) {
						displayScope = "#" + newDisplayScope + " ";
					}

					if (newViewManager != null) {
						viewManager = newViewManager;
					} else {
						viewManager = m_jsfViewManager.create();
					}

					setupEventHandling();

					outline = new Outline();

					outline.initialize();

					m_outlineToolbarController.init("rulesOutlineToolbar");
					
					
					
					return outline;
				},
				refresh : function() {
					refresh();
				}
			};

			/**
			 * 
			 */
			function Outline() {
				/**
				 * 
				 */
				Outline.prototype.toString = function() {
					return "rules-manager.Outline";
				};

				/**
				 * 
				 */
				Outline.prototype.initialize = function() {
					CommandsDispatcher.registerCommandHandler(this);
					this.createRuleSetButton = jQuery("#createRuleSetButton");

					this.createRuleSetButton.click({
						outline : this
					}, function(event) {
						event.data.outline.createRuleSet()
					});
				};

				/**
				 * 
				 */
				Outline.prototype.createRuleSet = function() {
					var rsName=m_i18nUtils.getProperty("rules.object.ruleset.name","Rule Set");
					var name = rsName + " " + RuleSet.getRuleSetsCount();
					var id = rsName.replace(/\s/g,"") + RuleSet.getRuleSetsCount();
					var ruleSet = RuleSet.create(id, name);
					//CommandsDispatcher.submitCommand();
					createRuleSetNode(ruleSet);
					
					viewManager.openView("ruleSetView",
							"id=" + ruleSet.id + "&name=" + ruleSet.name
									+ "&uuid=" + ruleSet.uuid, ruleSet.uuid);
					jQuery(displayScope + "#outline").jstree("rename","#" + ruleSet.uuid);
					/*save our virgin Rule Set to the server.*/
					saveRuleSets(false,true);
				}

				/**
				 * 
				 */
				Outline.prototype.processCommand = function(command) {
					// TODO Dummy
//					reloadOutlineTree();
					
					// Handling renaming of nodes
					var uuid;
					if (command.name === "RuleSet.Rename") {
						uuid = command.ruleSet.uuid;
					} else if (command.name === "DecisionTable.Rename") {
						uuid = command.decTable.uuid;
					} else if (command.name === "TechnicalRule.Rename") {
						uuid = command.techRule.uuid;
					}
					
					if (uuid) {
						var link = m_utils.jQuerySelect("li#" + uuid + " a")[0];
						var node = m_utils.jQuerySelect("li#" + uuid);

						// TODO - improve the command / changes structure						
						if (node.attr("name") != command.changes[1]) {
							node.attr("name", command.changes[1]);
							var textElem = m_utils.jQuerySelect(link.childNodes[1])[0];
							textElem.nodeValue = command.changes[1];
						}
					}
				};
			}
		});
