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
				"bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_communicationController",
				"rules-manager/js/m_outlineToolbarController",
				"rules-manager/js/CommandsDispatcher",
				"rules-manager/js/RuleSet" ],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_session, m_user, m_model, m_process, m_application,
				m_participant, m_typeDeclaration, m_data, m_elementConfiguration, m_jsfViewManager,
				m_messageDisplay, m_i18nUtils, m_communicationController, m_outlineToolbarController, CommandsDispatcher, RuleSet) {
			var isElementCreatedViaOutline = false;
			var hasUnsavedModifications = false;
			function getURL() {
				return m_urlUtils.getContextName()
						+ "/services/rest/bpm-modeler/modeler/"
						+ new Date().getTime();
			}

			var displayScope = "";
			var viewManager;

			var readAllModels = function(force) {

				// Needed for types

				m_model.loadModels(false);

				jQuery("#lastsave").text(m_i18nUtils
										.getProperty("modeler.outline.lastSavedMessage.title"));
	
				jQuery.each(RuleSet.getRuleSets(),
						function(index, ruleSet) {

							
							jQuery(displayScope + "#outline").jstree("create", displayScope + "#outline",
									"first", {
										"attr" : {
											"id" : ruleSet.uuid,
											"rel" : ruleSet.type,
											"elementId" : ruleSet.id
										},
										"data" : ruleSet.name
									}, null, true);
							jQuery(displayScope + "#outline").jstree("set_type", "ruleSet",
									"#" + ruleSet.uuid);
							
							if (ruleSet.rules) {
								jQuery.each(ruleSet.rules, function(index, rule) {
									jQuery(displayScope + "#outline").jstree("create",
											"#" + ruleSet.uuid, "last", {
												"attr" : {
													"id" : rule.uuid,
													"ruleSetId" : ruleSet.id,
													"ruleSetUuid" : ruleSet.uuid,
													"rel" : rule.type,
													"draggable" : true,
													"elementId" : rule.id
												},
												"data" : rule.name
											}, null, true);
									jQuery(displayScope + "#outline").jstree("close_node",
											"#" + rule.id);
								});	
							}
							
							if (ruleSet.technicalRules) {
								jQuery.each(ruleSet.technicalRules, function(index, techRule) {
									jQuery(displayScope + "#outline").jstree("create",
											"#" + ruleSet.uuid, "last", {
												"attr" : {
													"id" : techRule.uuid,
													"ruleSetId" : ruleSet.id,
													"ruleSetUuid" : ruleSet.uuid,
													"rel" : "TechnicalRule",
													"draggable" : true,
													"elementId" : techRule.id
												},
												"data" : techRule.name
											}, null, true);
								});	
							}
							
							if (ruleSet.decisionTables) {
								jQuery.each(ruleSet.decisionTables, function(index, decTable) {
									jQuery(displayScope + "#outline").jstree("create",
											"#" + ruleSet.uuid, "last", {
												"attr" : {
													"id" : decTable.uuid,
													"ruleSetId" : ruleSet.id,
													"ruleSetUuid" : ruleSet.uuid,
													"rel" : "DecisionTable",
													"draggable" : true,
													"elementId" : decTable.id
												},
												"data" : decTable.name
											}, null, true);
								});	
							}
						});
				m_utils.debug("Tree initialized");

				hasUnsavedModifications = false;
				jQuery("#undoChange").addClass("toolDisabled");
				jQuery("#redoChange").addClass("toolDisabled");
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
					CommandsDispatcher.submitCommand({
						name:"TechnicalRule.Rename",
						techRule:techRule,
						ruleSet:ruleSet,
						changes:[oldName,newName]
					});
					
				}
				else {
					var model = m_model.findModelByUuid(data.rslt.obj
							.attr("modelUUID"));
					var modelElement = model
							.findModelElementByUuid(data.rslt.obj.attr("id"));

					if (modelElement && (modelElement.name != data.rslt.name)) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelElementWithUUIDCommand(
										model.id, modelElement.uuid, {
											"name" : data.rslt.name
										}));
					}
				}
			};

			var renameElementViewLabel = function(type, uuid, name) {
				if (type == 'model') {
					renameView("modelView", uuid, "modelName", name);
				} else if (type == 'process') {
					renameView("processDefinitionView", uuid, "processName",
							name);
				} else if (type == "roleParticipant" || type == "teamLeader") {
					renameView("roleView", uuid, "roleName", name)
				} else if (type == 'organizationParticipant') {
					renameView("organizationView", uuid, "organizationName",
							name)
				} else if (m_elementConfiguration.isValidDataType(type)) {
					renameView("dataView", uuid, "dataName", name)
				} else if (type == "webservice") {
					renameView("webServiceApplicationView", uuid,
							"applicationName", name)
				} else if (type == "messageTransformationBean") {
					renameView("messageTransformationApplicationView", uuid,
							"applicationName", name)
				} else if (type == "camelSpringProducerApplication") {
					renameView("camelApplicationView", uuid, "applicationName",
							name)
				} else if (type == "interactive") {
					renameView("uiMashupApplicationView", uuid,
							"applicationName", name)
				} else if (m_elementConfiguration.isUnSupportedAppType(type)) {
					renameView("genericApplicationView", uuid,
							"applicationName", name)
				} else if (type == "structuredDataType") {
					renameView("xsdStructuredDataTypeView", uuid,
							"structuredDataTypeName", name)
				} else if (type == "conditionalPerformerParticipant") {
					renameView("conditionalPerformerView", uuid,
							"conditionalPerformerName", name)
				}
			}

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
									message : "All models will be reloaded from their last saved state and the session log will be cleared.<BR><BR>Continue?<BR><BR>",
									acceptButtonText : "Yes",
									cancelButtonText : "No",
									acceptFunction : reloadOutlineTree
								}
							});
				}
			}

			var reloadOutlineTree = function(saveFirst) {
				if (true == saveFirst) {
					saveAllRules();
				}
				jQuery(displayScope + "#outline").empty();
				readAllModels(true);
			};

			var importModel = function() {
				if (true == hasUnsavedModifications) {
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
										message : "Models have unsaved changes.<BR><BR>Please save models before continuing.",
										acceptButtonText : "Close",
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert("Models have unsaved changes. Please save models before continuing.");
					}
				} else {
					var link = jQuery(
							"a[id $= 'open_model_upload_dialog_link']",
							window.parent.frames['ippPortalMain'].document);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');
					window.parent.EventHub.events.publish(
							"OPEN_IMPORT_MODEL_DIALOG", linkId, formId);
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
			
			function saveAllRules() {
				var ruleSetArray = getRuleSets();
				
				m_communicationController
						.postData(
								{
									url : m_urlUtils.getContextName() + "/services/rest/rules-manager/rules/" + new Date().getTime() + "/save"
								},
								JSON.stringify(ruleSetArray),
								new function() {
									return {
										success : function(data) {
											m_messageDisplay.markSaved();
											hasUnsavedModifications = false;
										},
										failure : function(data) {
											if (parent.iPopupDialog) {
												parent.iPopupDialog
														.openPopup(prepareErrorDialogPoupupData(
																"Error saving models.",
																"OK"));
											} else {
												alert("Error saving models.");
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
										var ruleSet = RuleSet
												.findRuleSetByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView("ruleSetView",
												"id=" + ruleSet.id + "&name="
														+ ruleSet.name
														+ "&uuid="
														+ ruleSet.uuid,
												ruleSet.uuid);
									} 
									else if (data.rslt.obj.attr('rel') == "rule") {
										var ruleSet = RuleSet
												.findRuleSetByUuid(data.rslt.obj
														.attr("ruleSetUuid"));

										var rule = ruleSet
												.findRuleByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView("ruleView", "id="
												+ rule.id + "&ruleSetId="
												+ ruleSet.id + "&name="
												+ rule.name + "&uuid="
												+ rule.uuid + "&ruleSetUuid="
												+ ruleSet.uuid, rule.uuid);
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
												+ ruleSet.uuid, techRule.uuid);
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
												+ ruleSet.uuid, decTable.uuid);
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
											"ui","sort" ],
									contextmenu : {
										"items" : function(node) {
											var nodeType=node.attr('rel');
											if("DecisionTable"===nodeType || "TechnicalRule"===nodeType){
												return{
													"ccp":false,
													"create":false,										
													"delete":{
														"label":"Delete",
														"action": function(obj){
															deleteElementAction(obj.context.lastChild.data,
																function(){
																	var ruleSet = RuleSet.findRuleSetByUuid(obj.attr("ruleSetUuid")),
																	    id=obj.attr("id"),
																	    myBridge;
																	if(nodeType==="DecisionTable"){
																		ruleSet.deleteDecisionTable(id);
																	}else if(nodeType==="TechnicalRule"){
																		ruleSet.deleteTechnicalRule(id);
																	}
																	myBridge=parent.window["BridgeUtils"];
																	if(myBridge){
																		/*TODO: ID not matching to viewPanel in function call*/
																		myBridge.View.closeView("decisionTableView/"+id);
																	}
																	jQuery(displayScope + "#outline")
																		.jstree("delete_node","#"+ id);
																});
														}
													},
													"rename":{
														"label":"Rename",
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
														"label" : "Create Rule",
														"action" : function(obj) {
															var techRule;
															techRule=createTechnicalRule(obj.attr("id"));
															/*set new tree node for user editing*/
															jQuery(displayScope + "#outline").jstree("rename","#" + techRule.uuid);
														}
													},
													"createDecisionTable" : {
														"label" : "Create Decision Table",
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
														"label" : "Rename",
														"action" : function(obj) {
															jQuery(displayScope + "#outline")
																	.jstree("rename","#"+ obj.attr("id"));
														}
													},
													"deleteRuleSet" : {
														"label" : m_i18nUtils
																.getProperty("modeler.element.properties.commonProperties.delete"),
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteModel(obj.attr("elementId"));
																	});
														}
													},
													"export RuleSet" : {
														"label": "Export Rule Set",
														"action": function(obj){
															alert("Not Implemented.");
															console.log(obj);
														},
														"_class" : "ipp-text-red"
													}
												};
											} else if ('rule' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : m_i18nUtils
																.getProperty("modeler.outline.contextMenu.rename"),
														"action" : function(obj) {
															jQuery(displayScope + "#outline").jstree("rename","#"+ obj.attr("id"));
														}
													},
													"deleteProcess" : {
														"label" : m_i18nUtils
																.getProperty("modeler.element.properties.commonProperties.delete"),
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteProcess(
																				obj
																						.attr("elementId"),
																				obj
																						.attr("modelUUID"));
																	});
														}
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
					} else if ("importModel" == data.id) {
						importModel();
					} else if ("undoChange" == data.id) {
						undoMostCurrent();
					} else if ("redoChange" == data.id) {
						redoLastUndo();
					} else if ("saveAllRules" == data.id) {
						saveAllRules();
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

				/**
				 * 
				 */
				function deleteModel(modelId) {
					var model = m_model.findModel(modelId);
					m_commandsController.submitCommand(m_command
							.createDeleteModelCommand(model.uuid, {}));
				}

				/**
				 * 
				 */
				function getUniqueNameForElement(modelId, namePrefix) {
					var suffix = 0;
					var name = namePrefix + (++suffix);
					var id = m_utils.generateIDFromName(name);
					var model = m_model.findModel(modelId);
					if (model) {
						while (model.findModelElementById(id.toUpperCase())) {
							var name = namePrefix + (++suffix);
							var id = m_utils.generateIDFromName(name);
						}
					}

					return name;
				}

				/**
				 * 
				 */
				function deleteProcess(processId, modelUUID) {
					var model = m_model.findModelByUuid(modelUUID);
					m_commandsController.submitCommand(m_command
							.createDeleteProcessCommand(model.id, model.id, {
								"id" : processId
							}));
				}

				/**
				 */
				function deleteStructuredDataType(modelUUID, structTypeId) {
					var model = m_model.findModelByUuid(modelUUID);
					m_commandsController.submitCommand(m_command
							.createDeleteStructuredDataTypeCommand(model.id,
									model.id, {
										"id" : structTypeId
									}));
				}

				/**
				 * 
				 */
				function deleteParticipant(modelUUID, id) {
					var model = m_model.findModelByUuid(modelUUID);
					m_commandsController.submitCommand(m_command
							.createDeleteParticipantCommand(model.id, model.id,
									{
										"id" : id
									}));
				}

				/**
				 * 
				 */
				function deleteApplication(modelUUID, appId) {
					var model = m_model.findModelByUuid(modelUUID);
					m_commandsController.submitCommand(m_command
							.createDeleteApplicationCommand(model.id, model.id,
									{
										"id" : appId
									}));
				}

				/**
				 * 
				 */
				function deleteData(modelUUID, id) {
					var model = m_model.findModelByUuid(modelUUID);
					m_commandsController.submitCommand(m_command
							.createDeleteDataCommand(model.id, model.id, {
								"id" : id
							}));
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

				/**
				 * 
				 */
				function createRule(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var name = "Rule " + ruleSet.getRulesCount();
					var id = "Rule" + ruleSet.getRulesCount();

					ruleSet.addRule(id, name)
					
					CommandsDispatcher.submitCommand();

					viewManager.openView("ruleView", "id=" + rule.id
							+ "&ruleSetId=" + ruleSet.id + "&name=" + rule.name
							+ "&uuid=" + rule.uuid + "&ruleSetUuid="
							+ ruleSet.uuid, rule.uuid);
				}
				
				function createDecisionTable(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	decTableCount=ruleSet.getDecisionTableCount();
					var	name ="Decision Table " + decTableCount;
					var	id="DecisionTable" + decTableCount;
					var decTable=ruleSet.addDecisionTable(id,name);
					CommandsDispatcher.submitCommand();
					
					viewManager.openView("decisionTableView", "id="
							+ decTable.id + "&ruleSetId="
							+ ruleSet.id + "&name="
							+ decTable.name + "&uuid="
							+ decTable.uuid + "&ruleSetUuid="
							+ ruleSet.uuid, decTable.uuid);
					return decTable;
				}
				
				function createTechnicalRule(ruleSetUuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(ruleSetUuid);
					var	techRuleCount=ruleSet.getTechnicalRuleCount();
					var	name ="Rule " + techRuleCount;
					var	id="Rule" + techRuleCount;
					var techRule=ruleSet.addTechnicalRule(id,name);
					
					CommandsDispatcher.submitCommand();
					viewManager.openView("technicalRuleView", "id="
							+ techRule.id + "&ruleSetId="
							+ ruleSet.id + "&name="
							+ techRule.name + "&uuid="
							+ techRule.uuid + "&ruleSetUuid="
							+ ruleSet.uuid, techRule.uuid);
					
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
					window.parent.EventHub.events.subscribe("RELOAD_MODELS",
							reloadOutlineTree);
				}

				readAllModels();
			};

			var i18nStaticLabels = function() {
				jQuery("#createModel")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.createModel"));
				jQuery("#importModel")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.importModel"));
				jQuery("#undoChange")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.undo"));
				jQuery("#redoChange")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.redo"));
				jQuery("#saveAllModels")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.saveAllModel"));
				jQuery("#refreshModels")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.refreshModels"));

			};

			var outline;

			return {
				init : function(newViewManager, newDisplayScope) {

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

					// i18nStaticLabels();
					
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
					var name = "Rule Set " + RuleSet.getRuleSetsCount();
					var id = "RuleSet" + RuleSet.getRuleSetsCount();
					var ruleSet = RuleSet.create(id, name);
					CommandsDispatcher.submitCommand();
					viewManager.openView("ruleSetView",
							"id=" + ruleSet.id + "&name=" + ruleSet.name
									+ "&uuid=" + ruleSet.uuid, ruleSet.uuid);
					jQuery(displayScope + "#outline").jstree("rename","#" + ruleSet.uuid);
				}

				/**
				 * 
				 */
				Outline.prototype.processCommand = function(command) {
					// TODO Dummy
					reloadOutlineTree(false);
				};
			}
		});
