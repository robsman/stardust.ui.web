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
		[ "m_utils", "m_urlUtils", "m_constants", "m_extensionManager",
				"m_communicationController", "m_commandsController",
				"m_command", "m_session", "m_model", "m_process",
				"m_application", "m_dataStructure", "m_participant",
				"m_outlineToolbarController", "m_data" ],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_communicationController, m_commandsController, m_command,
				m_session, m_model, m_process, m_application, m_dataStructure,
				m_participant, m_outlineToolbarController, m_data) {
			var modelCounter = 0;
			var processCounter = 0;

			// TODO Find better location

			var viewManagerExtension = m_extensionManager
					.findExtension("viewManager");
			var viewManager = require(viewManagerExtension.moduleUrl).create();

			function getURL() {
				return require('m_urlUtils').getContextName()
						+ "/services/rest/modeler/" + new Date().getTime();
			}

			var readAllModels = function() {
				jQuery("#outline").jstree("create", "#models_root", "last", {
					"attr" : {
						"id" : "Models",
						"rel" : "models"
					},
					"data" : "Models"
				}, null, true);

				jQuery("#outline").jstree("set_type", "models", "#Models");

				m_model.loadModels();

				jQuery
						.each(
								m_model.getModels(),
								function(index, model) {
									modelCounter++;

									jQuery("#outline").jstree("create",
											"#Models", "last", {
												"attr" : {
													"id" : model.uuid,
													"rel" : "model",
													"elementId" : model.id
												},
												"data" : model.name
											}, null, true);

									jQuery("#outline").jstree("set_type",
											"model", "#" + model.uuid);

									jQuery.each(model.processes, function(
											index, process) {
										processCounter++;
										jQuery("#outline").jstree(
												"create",
												"#" + model.uuid,
												"last",
												{
													"attr" : {
														"id" : process.uuid,
														"oid" : process.oid,
														"fullId" : process
																.getFullId(),
														"modelId" : model.id,
														"modelUUId" : model.uuid,
														"rel" : "process",
														"draggable" : true,
														"elementId" : process.id
													},
													"data" : process.name
												}, null, true);
										jQuery("#outline").jstree("close_node",
												"#" + process.id);
									});

									jQuery("#outline").jstree(
											"create",
											"#" + model.uuid,
											"first",
											{
												"attr" : {
													"id" : "participants_"
															+ model.id,
													"rel" : "participants"
												},
												"data" : "Participants"
											}, null, true);

									jQuery
											.each(
													model.participants,
													function(index, participant) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#participants_"
																				+ model.id,
																		"last",
																		{
																			"attr" : {
																				"id" : participant.uuid,
																				"fullId" : participant
																						.getFullId(),
																				"rel" : participant.participantType,
																				"modelId" : model.id,
																				"modelUUId" : model.uuid,
																				"draggable" : true,
																				"elementId" : participant.id
																			},
																			"data" : participant.name
																		},
																		null,
																		true);
														jQuery("#outline")
																.jstree(
																		"close_node",
																		"#participants_"
																				+ model.id);
													});

									// Applications

									jQuery("#outline").jstree(
											"create",
											"#" + model.uuid,
											"first",
											{
												"attr" : {
													"modelId" : model.id,
													"modelUUId" : model.uuid,
													"id" : "applications_"
															+ model.uuid,
													"rel" : "applications"
												},
												"data" : "Applications"
											}, null, true);

									// Create application nodes

									jQuery
											.each(
													model.applications,
													function(index, application) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#applications_"
																				+ model.uuid,
																		"last",
																		{
																			"attr" : {
																				"id" : application.uuid,
																				"modelId" : model.id,
																				"modelUUId" : model.uuid,
																				"fullId" : application
																						.getFullId(),
																				"rel" : application.applicationType,
																				"draggable" : true,
																				"elementId" : application.id,
																				// TODO
																				// Likely
																				// a
																				// bug
																				"accessPoint" : application.accessPoint
																			},
																			"data" : application.name
																		},
																		null,
																		true);
														jQuery("#outline")
																.jstree(
																		"close_node",
																		"#applications_"
																				+ model.uuid);
													});

									// TODO - remove hard-coding for primitive
									// data and add nodes of specific data types

									jQuery("#outline").jstree("create",
											"#" + model.uuid, "first", {
												"attr" : {
													"id" : "data_" + model.id,
													"rel" : "data"
												},
												"data" : "Data"
											}, null, true);

									// Create Data nodes
									jQuery.each(model.dataItems, function(
											index, data) {
										jQuery("#outline").jstree(
												"create",
												"#data_" + model.id,
												"last",
												{
													"attr" : {
														"id" : data.uuid,
														"fullId" : data
																.getFullId(),
														"rel" : data.type,
														"elementId" : data.id,
														"draggable" : true
													},
													"data" : data.name
												}, null, true);
										jQuery("#outline").jstree("close_node",
												"#data_" + model.id);
									});

									// Structured Data Types

									jQuery("#outline").jstree(
											"create",
											"#" + model.uuid,
											"first",
											{
												"attr" : {
													"id" : "structuredTypes_"
															+ model.uuid,
													"rel" : "structuredTypes",
													"modelId" : model.id,
													"modelUUId" : model.uuid
												},
												"data" : "Structured Types"
											}, null, true);

									// Create structured data type nodes

									jQuery
											.each(
													model.structuredDataTypes,
													function(index,
															structuredDataType) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#structuredTypes_"
																				+ model.uuid,
																		"last",
																		{
																			"attr" : {
																				"id" : structuredDataType.uuid,
																				"fullId" : structuredDataType
																						.getFullId(),
																				"elementId" : structuredDataType.id,
																				"rel" : "structuredDataType",
																				"modelId" : model.id,
																				"modelUUId" : model.uuid,
																				"draggable" : true
																			},
																			"data" : structuredDataType.name
																		},
																		null,
																		true);
														jQuery("#outline")
																.jstree(
																		"close_node",
																		"#structuredTypes_"
																				+ model.uuid);
													});

									jQuery("#outline").jstree("close_node",
											"#" + model.uuid);
								});
			};

			var deployModel = function(modelId) {
				var modeleDeployerLink = jQuery(
						"a[id $= 'model_deployer_link']",
						window.parent.frames['ippPortalMain'].document);
				var modeleDeployerLinkId = modeleDeployerLink.attr('id');
				var form = modeleDeployerLink.parents('form:first');
				var formId = form.attr('id');

				window.parent.EventHub.events.publish(
						"SELECT_MODEL_FOR_DEPLOYMENT", modeleDeployerLinkId,
						modelId + ".xpdl", "/process-models/" + modelId
								+ ".xpdl", formId);

			};

			// TODO Is this still needed? Delete after verifying
			var elementCreationHandler = function(id, name, type, parent) {
				if (type == 'activity') {
					var parentSelector = '#' + parent;
					jQuery("#outline").jstree("create", parentSelector, "last",
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
					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : id,
									"rel" : "sub_process_activity"
								},
								"data" : name
							}, null, true);
				} else if (type == 'primitiveDataType') {
					var parentSelector = '#' + parent;
					jQuery("#outline").jstree("create", parentSelector, "last",
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
					jQuery("#outline").jstree("create", parentSelector, "last",
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
				if (data.rslt.obj.attr('rel') == 'model') {
					var model = m_model.findModelByUuid(data.rslt.obj.attr("id"));

					if (model.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelCommand(model.uuid, {
									"name" : data.rslt.name,
									"id" : m_utils.generateIDFromName(data.rslt.name)
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "process") {
					var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
					var process = model.findModelElementByUuid(data.rslt.obj.attr("id"));
					if (process.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelElementCommand(model.id, process.oid, {
									"name" : data.rslt.name,
									"id" : m_utils.generateIDFromName(data.rslt.name)
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "webservice"
						|| data.rslt.obj.attr("rel") == "messageTransformationBean"
						|| data.rslt.obj.attr("rel") == "camelBean"
						|| data.rslt.obj.attr("rel") == "interactive") {
					var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
					var application = model.findModelElementByUuid(data.rslt.obj.attr("id"));

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementWithUUIDCommand(model.id, application.uuid, {
								"name" : data.rslt.name,
								"id" : m_utils.generateIDFromName(data.rslt.name)
							}));
				} else if (data.rslt.obj.attr("rel") == "structuredDataType") {
					var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
					var dataType = model.findModelElementByUuid(data.rslt.obj.attr("id"));

					if (dataType.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelElementWithUUIDCommand(model.id, dataType.uuid, {
									"name" : data.rslt.name,
									"id" : m_utils.generateIDFromName(data.rslt.name)
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "roleParticipant"
						|| data.rslt.obj.attr("rel") == "organizationParticipant"
						|| data.rslt.obj.attr("rel") == "conditionalPerformerParticipant") {
					var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
					var participant = model.findModelElementByUuid(data.rslt.obj.attr("id"));

					if (participant.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createRenameCommand("/models/" + model.id
										+ "/participants/" + participant.id, {
									"id" : participant.id,
									"name" : participant.name
								}, {
									"name" : data.rslt.name
								}));
					}
				}
			};

			var refresh = function() {
				modelCounter = 0;
				processCounter = 0;
				jQuery("#outline").jstree("remove", "#Models");
				readAllModels();
			};

			//TODO - delete
//			var getTreeNodeId = function (modelId, nodeType, nodeId) {				
//				return modelId + "__" + nodeType + "__" + nodeId;
//			};
			
//			var extractElementIdFromTreeNodeId = function (nodeId) {
//				var index = m_utils.getLastIndexOf(nodeId, "__");
//				return nodeId.substring(index);
//			};			
			
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

				jQuery("#outline")
						.bind(
								"select_node.jstree",
								function(event, data) {
									if (data.rslt.obj.attr('rel') == 'model') {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("id"));

										viewManager.openView("modelView",
												"modelId=" + model.id
														+ "&modelName="
														+ model.name, model.id);
									} else if (data.rslt.obj.attr('rel') == "roleParticipant") {
										var roleId = data.rslt.obj.attr("id");
										var roleName = data.inst.get_text();
										var modelId = data.rslt.obj
												.attr("modelId");
										var fullId = data.rslt.obj
										.attr("fullId");

										viewManager.openView("roleView",
												"roleId=" + roleId
														+ "&modelId=" + modelId
														+ "&roleName="
														+ roleName + "&fullId="
															+ fullId, fullId);
									} else if (data.rslt.obj.attr('rel') == 'organizationParticipant') {
										var organizationId = data.rslt.obj
												.attr("elementId");
										var organizationName = data.inst
												.get_text();
										var modelId = data.rslt.obj
												.attr("modelId");
										var fullId = data.rslt.obj
										.attr("fullId");

										viewManager.openView(
												"organizationView",
												"organizationId="
														+ organizationId
														+ "&modelId=" + modelId
														+ "&organizationName="
														+ organizationName + "&fullId="
														+ fullId,
												fullId);
									} else if (data.rslt.obj.attr('rel') == 'primitive'
											|| data.rslt.obj.attr('rel') == 'serializable'
											|| data.rslt.obj.attr('rel') == 'entity'
											|| data.rslt.obj.attr('rel') == 'struct'
											|| data.rslt.obj.attr('rel') == 'dmsDocumentList') {

										// TODO Above is very ugly!
										var dataId = data.rslt.obj.attr("elementId");
										var dataName = data.inst.get_text();
										var modelId = data.rslt.obj
												.attr("modelId");
										var fullId = data.rslt.obj
										.attr("fullId");

										viewManager.openView("dataView",
												"dataId=" + dataId
														+ "&modelId=" + modelId
														+ "&dataName="
														+ dataName + "&fullId="
														+ fullId, fullId);
									} else if (data.rslt.obj.attr('rel') == 'process') {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var process = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager.openView("processDefinitionView",
												"processId=" + process.id
														+ "&modelId=" + model.id
														+ "&processName="
														+ process.name + "&fullId="
														+ process.getFullId(),
														process.getFullId());
									} else if (data.rslt.obj.attr('rel') == "webservice") {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var application = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager.openView(
												"webServiceApplicationView",
												"modelId=" + model.id
														+ "&applicationId="
														+ application.id + "&fullId="
														+ application.getFullId(),
														application.getFullId());
									} else if (data.rslt.obj.attr('rel') == "messageTransformationBean") {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var application = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager
												.openView(
														"messageTransformationApplicationView",
														"modelId="
																+ model.id
																+ "&applicationId="
																+ application.id + "&fullId="
																+ application.getFullId(),
																application.getFullId());
									} else if (data.rslt.obj.attr('rel') == "camelBean") {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var application = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager.openView(
												"camelApplicationView",
												"modelId=" + model.id
														+ "&applicationId="
														+ application.id + "&fullId="
														+ application.getFullId(),
														application.getFullId());
									} else if (data.rslt.obj.attr('rel') == "interactive") {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var application = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager.openView(
												"uiMashupApplicationView",
												"modelId=" + model.id
														+ "&applicationId="
														+ application.id + "&fullId="
														+ application.getFullId(),
														application.getFullId());
									} else if (data.rslt.obj.attr('rel') == "structuredDataType") {
										var model = m_model.findModelByUuid(data.rslt.obj.attr("modelUUID"));
										var structuredDataType = model.findModelElementByUuid(data.rslt.obj.attr("id"));

										viewManager
												.openView(
														"xsdStructuredDataTypeView",
														"modelId="
																+ model.id
																+ "&structuredDataTypeId="
																+ structuredDataType.id + "&fullId="
																+ structuredDataType.getFullId(),
																structuredDataType.getFullId());
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
											if ('model' == node.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : "Rename",
														"action" : function(obj) {
															jQuery("#outline")
																	.jstree(
																			"rename",
																			"#"
																					+ obj
																							.attr("id"));
														}
													},
													"deleteModel" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteModel(obj
																				.attr("elementId"));
																	});
														}
													},
													"deploy" : {
														"label" : "Deploy",
														"action" : function(obj) {
															deployModel(obj
																	.attr("elementId"));
														}
													},
													"createProcess" : {
														"label" : "Create Process",
														"action" : function(obj) {
															createProcess(obj
																	.attr("elementId"));
														}
													}
												};
											} else if ('models' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : false,
													"createModel" : {
														"label" : "Create Model",
														"action" : createModel
													}
												};
											} else if ('process' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : "Rename",
														"action" : function(obj) {
															jQuery("#outline")
																	.jstree(
																			"rename",
																			"#"
																					+ obj
																							.attr("id"));
														}
													},
													"deleteProcess" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteProcess(
																				obj
																						.attr("id"),
																				obj
																						.attr("modelId"));
																	});
														}
													}
												};

											} else if ('applications' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : false,
													"createWebServiceApplication" : {
														"label" : "Create Web Service",
														"action" : function(obj) {
															createWebServiceApplication(obj
																	.attr("modelUUId"));
														}
													},
													"createMessageTransformationApplication" : {
														"label" : "Create Transformation",
														"action" : function(obj) {
															createMessageTransformationApplication(obj
																	.attr("modelUUId"));
														}
													},
													"createCamelApplication" : {
														"label" : "Create Camel Route",
														"action" : function(obj) {
															createCamelApplication(obj
																	.attr("modelUUId"));
														}
													},
													"createUiMashupApplication" : {
														"label" : "Create UI Mashup",
														"action" : function(obj) {
															createUiMashupApplication(obj
																	.attr("modelUUId"));
														}
													}
												};
											} else if ("webservice" == node
													.attr("rel")
													|| "messageTransformationBean" == node
															.attr("rel")
													|| "camelBean" == node
															.attr("rel")
													|| "interactive" == node
															.attr("rel")) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : "Rename",
														"action" : function(obj) {
															jQuery("#outline")
																	.jstree(
																			"rename",
																			"#"
																					+ obj
																							.attr("id"));
														}
													},
													"createWrapperProcess" : {
														"label" : "Create Wrapper Process",
														"action" : function(obj) {
															var application = m_model
																	.findApplication(obj
																			.attr("fullId"));
															m_utils
																	.debug("Application");
															m_utils
																	.debug(application);

															createWrapperProcess(application);
														}
													},
													"deleteApplication" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteApplication(
																				obj
																						.attr("modelId"),
																				obj
																						.attr("id"));
																	});
														}
													}
												};
											} else if ('structuredTypes' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : false,
													"createXSDStructuredDataType" : {
														"label" : "Create Data Type",
														"action" : function(obj) {
//															createXsdStructuredDataType(obj
//																	.attr("modelId"));
															createXsdStructuredDataType(obj
																	.attr("modelUUId"));
														}
													}
												};
											} else if ('structuredDataType' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : "Rename",
														"action" : function(obj) {
															jQuery("#outline")
																	.jstree(
																			"rename",
																			"#"
																					+ obj
																							.attr("id"));
														}
													},
													"deleteStructuredDataType" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteStructuredDataType(
																				obj
																						.attr("modelId"),
																				obj
																						.attr("elementId"));
																	});
														}
													}
												};
											} else if ('roleParticipant' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : {
														"label" : "Rename",
														"action" : function(obj) {
															jQuery("#outline")
																	.jstree(
																			"rename",
																			"#"
																					+ obj
																							.attr("id"));
														}
													},
													"deleteParticipantRole" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteParticipantRole(
																				obj
																						.attr("modelId"),
																				obj
																						.attr("elementId"));
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
											"models" : {
												"icon" : {
													"image" : "../images/icons/model.gif"
												},
												"valid_children" : [ "model" ]
											},
											"model" : {
												"icon" : {
													"image" : "../images/icons/model.gif"
												},
												"valid_children" : [
														"participants",
														"process",
														"applications",
														"structuredTypes",
														"data" ]
											},
											"participants" : {
												"icon" : {
													"image" : "../images/icons/world.png"
												},
												"valid_children" : [
														"roleParticipant",
														"organizationParticipant",
														"conditionalPerformerParticipant" ]
											},
											"roleParticipant" : {
												"icon" : {
													"image" : "../images/icons/role.png"
												}
											},
											"organizationParticipant" : {
												"icon" : {
													"image" : "../images/icons/organization.png"
												}
											},
											"conditionalPerformerParticipant" : {
												"icon" : {
													"image" : "../images/icons/role.png"
												}
											},
											"process" : {
												"icon" : {
													"image" : "../images/icons/process.png"
												},
												"valid_children" : [
														"manual_activity",
														"sub_process_activity" ]
											},
											"structuredTypes" : {
												"icon" : {
													"image" : "../images/icons/struct_types.gif"
												}
											},
											"structuredDataType" : {
												"icon" : {
													"image" : "../images/icons/struct_type.gif"
												}
											},
											"applications" : {
												"icon" : {
													"image" : "../images/icons/applications.gif"
												},
												// TODO Check for node object
												// type should suffice
												"valid_children" : [
														"plainJava",
														"JMS_Application",
														"webservice",
														"messageTransformationBean",
														"camelBean",
														"interactive",
														"DMS_Operation",
														"mailBean",
														"Message_Parsing_Application",
														"Message_Serialization_Application",
														"Drools_Rules_Engine",
														"Session_Bean_Application",
														"Spring_Bean_Application",
														"Visual_Rules_Engine_Application",
														"XSL_Message_Transformation_Application" ]
											},
											"interactive" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"plainJava" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"jms" : {
												"icon" : {
													"image" : "../images/icons/jms_application.gif"
												}
											},
											"webservice" : {
												"icon" : {
													"image" : "../images/icons/webservice_application.gif"
												}
											},
											"DMS_Operation" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"mailBean" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Message_Parsing_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Message_Serialization_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"messageTransformationBean" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"camelBean" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Drools_Rules_Engine" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Session_Bean_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Spring_Bean_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"Visual_Rules_Engine_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"XSL_Message_Transformation_Application" : {
												"icon" : {
													"image" : "../images/icons/application_java.gif"
												}
											},
											"data" : {
												"icon" : {
													"image" : "../images/icons/data.gif"
												},
												"valid_children" : [
														"Entity_Bean",
														"primitive",
														"Serializable_Data",
														"struct", "Document",
														"Document_List",
														"Folder",
														"Folder_List",
														"Hibernate_Data",
														"XML_Document" ]
											},
											"primitive" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"struct" : {
												"icon" : {
													"image" : "../images/icons/struct_data.gif"
												}
											},
											"Serializable_Data" : {
												"icon" : {
													"image" : "../images/icons/serializable_data.gif"
												}
											},
											"Entity_Bean" : {
												"icon" : {
													"image" : "../images/icons/entity_data.gif"
												}
											},
											"Document" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"Document_List" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"Folder" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"Folder_List" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"Hibernate_Data" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											},
											"XML_Document" : {
												"icon" : {
													"image" : "../images/icons/primitive_data.gif"
												}
											}
										}
									}
								}).jstree("set_theme", "default");
				// "themes" : {
				// "theme" : "default",
				// "url" : "/xhtml/css/jstree"}}).jstree("set_theme",
				// "default");

				var handleToolbarEvents = function(event, data) {
					if ("createModel" == data.id) {
						createModel();
					} else if ("importModel" == data.id) {
						alert("Funtionality not implemented yet");
					} else if ("saveAllModels" == data.id) {
						saveAllModels();
					} else if ("refreshModels" == data.id) {
						refresh();
					}
				};

				function saveAllModels() {
					m_communicationController
							.syncGetData(
									{
										url : require("m_urlUtils")
												.getModelerEndpointUrl()
												+ "/models/save"
									},
									new function() {
										return {
											success : function(data) {
												alert("All models have been saved successfully.");
											},
											failure : function(data) {
											}
										}
									});
				}

				/**
				 * 
				 */
				function createModel() {
					var number = (++modelCounter);
					var name = 'Model ' + number;
					var id = 'Model' + number;

					m_commandsController.submitCommand(m_command
							.createCreateModelCommand({
								"name" : name,
								"id" : id
							}));
				}

				/**
				 * TODO - WRONG
				 */
				function renameModel() {
					var number = (++modelCounter);
					var name = 'Model ' + number;
					var id = 'Model' + number;

					m_commandsController.submitCommand(m_command
							.createCreateCommand("/models/", {
								"name" : name,
								"id" : id
							}));
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
				function createProcess(modelId) {
					var number = (++processCounter);
					var name = "Process " + number;
					var id = "Process" + number;

					m_commandsController.submitCommand(m_command
							.createCreateProcessCommand(modelId, modelId, {
								"name" : name,
								"id" : id
							}, modelId));
				}

				function deleteProcess(processId, modelId) {
					m_commandsController.submitCommand(m_command
							.createDeleteCommand("/models/" + modelId
									+ "/processes/" + processId, {
								"processId" : processId,
								"modelId" : modelId
							}));
				}

				/**
				 */
				function deleteStructuredDataType(modelId, id) {
					m_commandsController.submitCommand(m_command
							.createDeleteCommand("/models/" + modelId
									+ "/structuredDataTypes/" + id, {
								"modelId" : modelId,
								"structuredDataTypeId" : id
							}));
				}

				/**
				 */
				function deleteParticipantRole(modelId, id) {
					m_commandsController.submitCommand(m_command
							.createDeleteCommand("/models/" + modelId
									+ "/participants/" + id, {
								"modelId" : modelId,
								"participantId" : id
							}));
				}

				/**
				 */
				function deleteApplication(modelId, id) {
					m_commandsController.submitCommand(m_command
							.createDeleteCommand("/models/" + modelId
									+ "/applications/" + id, {
								"modelId" : modelId,
								"applicationId" : id
							}));
				}

				function prepareDeleteElementData(name, callback) {
					var popupData = {
						attributes : {
							width : "400px",
							height : "200px",
							src : "../bpm-modeler/confirmationPopupDialogContent.html"
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
				function createWebServiceApplication(modelUUId) {
					var number = (++processCounter);
					var name = "Web Service " + number;
					var id = "WebService" + number;
					var model = m_model.findModelByUuid(modelUUId);
					var modelId = model.id;

					m_commandsController.submitCommand(m_command
							.createCreateWebServiceAppCommand(modelId, modelId,
									{
										"name" : name,
										"id" : id
									}, modelId));
				}

				/**
				 * 
				 */
				function createMessageTransformationApplication(modelUUId) {
					var number = (++processCounter);
					var name = "Message Transformation " + number;
					var id = "MessageTransformation" + number;
					var model = m_model.findModelByUuid(modelUUId);
					var modelId = model.id;

					m_commandsController.submitCommand(m_command
							.createCreateMessageTransfromationAppCommand(
									modelId, modelId, {
										"name" : name,
										"id" : id
									}, modelId));
				}

				/**
				 * 
				 */
				function createCamelApplication(modelUUId) {
					var number = (++processCounter);
					var name = "Camel Route " + number;
					var id = "CamelRoute" + number;
					var model = m_model.findModelByUuid(modelUUId);
					var modelId = model.id;

					m_commandsController.submitCommand(m_command
							.createCreateCamelAppCommand(modelId, modelId, {
								"name" : name,
								"id" : id
							}, modelId));
				}

				/**
				 * 
				 */
				function createUiMashupApplication(modelUUId) {
					var number = (++processCounter);
					var name = "UI Mashup " + number;
					var id = "UIMashup" + number;
					var model = m_model.findModelByUuid(modelUUId);
					var modelId = model.id;

					m_commandsController.submitCommand(m_command
							.createCreateUiMashupAppCommand(modelId, modelId, {
								"name" : name,
								"id" : id
							}, modelId));
				}

				/**
				 * 
				 * @param modelId
				 * @returns
				 */
				function createXsdStructuredDataType(modelUUId) {
					var number = (++processCounter);
					// TODO obtain number from model
					var name = "XSD Data Structure " + number;
					var id = "XSDDataStructure" + number;
					var model = m_model.findModelByUuid(modelUUId);
					var modelId = model.id;
					m_commandsController.submitCommand(m_command
							.createCreateStructuredDataTypeCommand(modelId,
									modelId, {
										"name" : name,
										"id" : id
									}, modelId));
				}

				/**
				 * 
				 * @param modelId
				 * @param id
				 * @returns
				 */
				function createWrapperProcess(application) {
					var popupData = {
						attributes : {
							width : "700px",
							height : "500px",
							src : "../bpm-modeler/views/modeler/serviceWrapperWizard.html"
						},
						payload : {
							application : application,
							createCallback : function(parameter) {
								m_commandsController
										.submitCommand(m_command
												.createCreateCommand(
														"/models/"
																+ application.model.id
																+ "/processes/createWrapperProcess",
														parameter));
							}
						}
					};

					parent.iPopupDialog.openPopup(popupData);
				}

				// TODO Should be encapsulated in module
				//TODO - check and delete
				if (window.parent.EventHub != null) {
					window.parent.EventHub.events.subscribe("ELEMENT_CREATED",
							elementCreationHandler);
					window.parent.EventHub.events.subscribe("ELEMENT_RENAMED",
							elementRenamingHandler);
				}

				readAllModels();
			};

			var outline;

			return {
				init : function() {
					setupEventHandling();

					outline = new Outline();

					outline.initialize();
					m_outlineToolbarController.init("outlineToolbar");
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
					return "Lightdust.Outline";
				};

				/**
				 * 
				 */
				Outline.prototype.initialize = function() {
					// Register with Event Bus

					m_commandsController.registerCommandHandler(this);
				};

				/**
				 * 
				 */
				Outline.prototype.processCommand = function(command) {
					m_utils.debug("===> Outline Process Event");

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != obj && null != obj.changes) {
						for ( var i = 0; i < obj.changes.added.length; i++) {
							// Create Process
							if (m_constants.PROCESS == command.changes.added[i].type) {
								this.createProcess(command.changes.added[i]);
							} else if (m_constants.MODEL == command.changes.added[i].type) {
								this.createModel(command.changes.added[i]);
							} else if (m_constants.STRUCTURED_DATA_TYPE == command.changes.added[i].type) {
								this
										.createStructuredDataType(command.changes.added[i]);
							} else if (m_constants.DATA_SYMBOL == command.changes.added[i].type) {
								this
										.createData(command.changes.added[i].data);
							} else if (m_constants.APPLICATION == command.changes.added[i].type) {
								this
										.createApplication(command.changes.added[i]);
							}
						}
						for ( var i = 0; i < obj.changes.modified.length; i++) {
							if (m_constants.MODEL == obj.changes.modified[i].type) {
								var modelElement = m_model.findModelByUuid(obj.changes.modified[i].uuid);
							} else {
								if (undefined == obj.changes.modified[i].oid
										|| 0 == obj.changes.modified[i].oid) {
									var modelElement = m_model
											.findModelElementInModelByUuid(
													obj.changes.modified[i].modelId,
													obj.changes.modified[i].uuid);
								} else {
									var modelElement = m_model
											.findModelElementInModelByGuid(
													obj.changes.modified[i].modelId,
													obj.changes.modified[i].oid);
								}
							}
							m_utils.debug("Models:");
							m_utils.debug(m_model.getModels());
							m_utils.debug("Model Element:");
							m_utils.debug(modelElement);

							if (modelElement != null) {
								modelElement.rename(obj.changes.modified[i].id,
										obj.changes.modified[i].name);

								var uuid = modelElement.uuid;
								var link = jQuery("li#" + uuid + " a")[0];
								var node = jQuery("li#" + uuid);

								node.attr("elementId", modelElement.id);
								node.attr("fullId", modelElement.getFullId());
								node.attr("name", modelElement.name);

								var textElem = jQuery(link.childNodes[1])[0];

								textElem.nodeValue = modelElement.name;
							}
						}
						for ( var i = 0; i < obj.changes.removed.length; i++) {
//							if (m_constants.MODEL == command.changes.removed[i].type) {
//								this.deleteModel(command.changes.removed[i]);
//							}
						}
					} else if (command.scope == "all") {
						// @deprecated
						refresh();
					} else if (command.type == m_constants.CREATE_COMMAND) {
						var type = m_model.findElementTypeByPath(command.path);

						m_utils.debug("Object Type: " + type);

						if (type == m_constants.MODEL) {
							this.createModel(command.newObject);
						} else if (type == m_constants.PROCESS_DEFINITION) {
							this.createProcess(command.newObject);
						} else if (type == m_constants.APPLICATION) {
							this.createApplication(command.newObject);
							m_utils.debug("Newly created applicaton:");
							m_utils.debug(m_model.getModels());
						} else if (type == m_constants.STRUCTURED_DATA_TYPE) {
							this.createStructuredDataType(command.newObject);
						} else if (type == m_constants.DATA) {
							this.createData(command.newObject);
						} else if (type == m_constants.PARTICIPANT) {
							this.createParticipant(command.newObject);
						}
					} else if (command.type == m_constants.DELETE_COMMAND) {
						var type = m_model.findElementTypeByPath(command.path);

						if (type == m_constants.MODEL) {
							m_model.deleteModel(command.oldObject.id);
							jQuery("#outline").jstree("remove",
									"#" + command.oldObject.id)
						} else if (type == m_constants.PROCESS_DEFINITION) {
							jQuery("#outline").jstree("remove",
									"#" + command.oldObject.processId)
							var model = m_model
									.findModel(command.oldObject.modelId);
							m_process.deleteProcess(
									command.oldObject.processId, model);
						} else if (type == m_constants.APPLICATION) {
							jQuery("#outline").jstree("remove",
									"#" + command.oldObject.applicationId)
							var model = m_model
									.findModel(command.oldObject.modelId);
							m_application.deleteApplication(
									command.oldObject.applicationId, model);
						} else if (type == m_constants.STRUCTURED_DATA_TYPE) {
							jQuery("#outline")
									.jstree(
											"remove",
											"#"
													+ command.oldObject.structuredDataTypeId)
							var model = m_model
									.findModel(command.oldObject.modelId);
							m_dataStructure.deleteStructuredType(
									command.oldObject.structuredDataTypeId,
									model);
						} else if (type == m_constants.PARTICIPANT) {
							jQuery("#outline").jstree("remove",
									"#" + command.oldObject.participantId)
							var model = m_model
									.findModel(command.oldObject.modelId);
							m_participant.deleteParticipantRole(
									command.oldObject.participantId, model);
						}
					}
				};

				/**
				 * 
				 */
				Outline.prototype.createModel = function(data) {
					var model = m_model.createModel(data.id, data.name, data.uuid);
					jQuery("#outline").jstree("create", "#Models", "last", {
						"attr" : {
							"elementId" : data.id,
							"id" : data.uuid,
							"fullId" : model.getFullId(),
							"rel" : "model"
						},
						"data" : data.name
					}, null, false);
					jQuery("#outline").jstree("set_type", "model",
							"#" + data.uuid);
					jQuery("#outline").jstree("create", "#" + data.uuid, "first",
							{
								"attr" : {
									"id" : "structuredTypes_" + data.uuid,
									"rel" : "structuredTypes",
									"modelId" : data.id,
									"modelUUId" : data.uuid
								},
								"data" : "Structured Types"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.uuid, "first",
							{
								"attr" : {
									"id" : "data_" + data.id,
									"rel" : "data",
									"modelUUId" : data.uuid
								},
								"data" : "Data"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.uuid, "first",
							{
								"attr" : {
									"modelId" : data.id,
									"id" : "applications_" + data.uuid,
									"rel" : "applications",
									"modelUUId" : data.uuid
								},
								"data" : "Applications"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.uuid, "first",
							{
								"attr" : {
									"id" : "participants_" + data.id,
									"rel" : "participants",
									"modelUUId" : data.uuid
								},
								"data" : "Participants"
							}, null, true);
					jQuery("#outline").jstree("create",
							"#" + "participants_" + data.id, "first", {
								"attr" : {
									"id" : data.adminId,
									"rel" : "participant_role",
									"draggable" : true
								},
								"data" : "Administrator"
							}, null, true);
				}

				Outline.prototype.deleteModel = function(transferObject) {
					m_model.deleteModel(transferObject.id);
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
				}

				/**
				 * 
				 */
				Outline.prototype.createProcess = function(transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var process = m_process.createProcessFromJson(model,
							transferObject);
					var parentSelector = '#' + model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : process.uuid,
									"oid" : process.oid,
									"elementId" : process.id,
									"modelId" : model.id,
									"rel" : "process",
									"fullId" : process.getFullId(),
									"draggable" : true
								},
								"state" : "open",
								"data" : process.name
							}, null, false);
				};

				/**
				 * 
				 */
				Outline.prototype.createApplication = function(transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var application = m_application.initializeFromJson(model,
							transferObject);
					var parentSelector = '#applications_'
							+ model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : application.applicationType,
									"id" : application.uuid,
									"elementId" : application.id,
									"fullId" : application.getFullId(),
									"modelId" : application.modelId,
									"draggable" : true
								},
								"state" : "open",
								"data" : application.name
							}, null, false);
				};

				/**
				 * 
				 */
				Outline.prototype.createData = function(transferObject) {
					try{
						var model = m_model.findModelByUuid(transferObject.modelUUID);
	
					}
					catch (e) {
						alert("error"+e);
					}
										var data = m_data.initializeFromJson(
							model, transferObject);
					var parentSelector = '#data_' + model.id;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : data.type,
									"modelId" : model.id,
									"id" : data.uuid,
									"elementId" : data.id,
									"fullId" : data.getFullId(),
									"draggable" : true
								},
								"state" : "open",
								"data" : data.name
							}, null, false);
				};

				/**
				 * 
				 */
				Outline.prototype.createStructuredDataType = function(
						transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var dataStructure = m_dataStructure.initializeFromJson(
							model, transferObject);
					var parentSelector = '#structuredTypes_' + model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : "structuredDataType",
									"modelId" : model.id,
									"id" : dataStructure.uuid,
									"elementId" : dataStructure.id,
									"fullId" : dataStructure.getFullId(),
									"draggable" : true
								},
								"state" : "open",
								"data" : dataStructure.name
							}, null, false);
				};

				/**
				 * 
				 */
				Outline.prototype.createParticipant = function(transfermObject) {
					var model = m_model.findModel(transferObject.modelId);
					// TODO Add
				};
			}
		});
