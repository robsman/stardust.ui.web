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
		[ "m_utils", "m_urlUtils", "m_constants", "m_communicationController",
				"m_commandsController", "m_command", "m_session", "m_model",
				"m_process", "m_application", "m_dataStructure",
				"m_participant", "m_outlineToolbarController" ],
		function(m_utils, m_urlUtils, m_constants, m_communicationController,
				m_commandsController, m_command, m_session, m_model, m_process,
				m_application, m_dataStructure, m_participant,
				m_outlineToolbarController) {
			var modelCounter = 0;
			var processCounter = 0;

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
													"id" : model.id,
													"rel" : "model"
												},
												"data" : model.name
											}, null, true);

									jQuery("#outline").jstree("set_type",
											"model", "#" + model.id);

									jQuery.each(model.processes, function(
											index, process) {
										processCounter++;
										jQuery("#outline").jstree(
												"create",
												"#" + model.id,
												"last",
												{
													"attr" : {
														"id" : process.id,
														"fullId" : process
																.getFullId(),
														"modelId" : model.id,
														"rel" : "process",
														"draggable" : true
													},
													"data" : process.name
												}, null, true);
										jQuery("#outline").jstree("close_node",
												"#" + process.id);
									});

									jQuery("#outline").jstree(
											"create",
											"#" + model.id,
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
																				"id" : participant.id,
																				"fullId" : participant
																						.getFullId(),
																				"rel" : "participant_role",
																				"modelId" : model.id,
																				"draggable" : true
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
											"#" + model.id,
											"first",
											{
												"attr" : {
													"modelId" : model.id,
													"id" : "applications_"
															+ model.id,
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
																				+ model.id,
																		"last",
																		{
																			"attr" : {
																				"id" : application.id,
																				"fullId" : application
																						.getFullId(),
																				"rel" : application.applicationType,
																				"draggable" : true,
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
																				+ model.id);
													});

									// TODO - remove hard-coding for primitive
									// data and add nodes of specific data types

									jQuery("#outline").jstree("create",
											"#" + model.id, "first", {
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
														"id" : data.id,
														"fullId" : data
																.getFullId(),
														"rel" : data.type,
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
											"#" + model.id,
											"first",
											{
												"attr" : {
													"id" : "structuredTypes_"
															+ model.id,
													"rel" : "structuredTypes",
													"modelId" : model.id
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
																				+ model.id,
																		"last",
																		{
																			"attr" : {
																				"id" : structuredDataType.id,
																				"fullId" : structuredDataType
																						.getFullId(),
																				"rel" : "structuredDataType",
																				"modelId" : model.id,
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
																				+ model.id);
													});

									jQuery("#outline").jstree("close_node",
											"#" + model.id);
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

			// TODO Is this still needed?
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
					var modelId = data.rslt.obj.attr('id');
					var model = m_model.findModel(modelId);

					if (model.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createRenameCommand("/models/" + model.id, {
									"id" : model.id,
									"name" : model.name
								}, {
									"name" : data.rslt.name
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "process") {
					var modelId = data.inst._get_parent(data.rslt.obj).attr(
							"id");
					var processId = data.rslt.obj.attr('id');
					var model = m_model.findModel(modelId);
					var process = m_model.findProcess(processId);

					if (process.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createRenameCommand("/models/" + modelId
										+ "/processes/" + processId, {
									"id" : process.id,
									"name" : process.name
								}, {
									"name" : data.rslt.name
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "webservice"
						|| data.rslt.obj.attr("rel") == "messageTransformationBean"
						|| data.rslt.obj.attr("rel") == "camelBean"
						|| data.rslt.obj.attr("rel") == "interactive") {
					var modelId = data.rslt.obj.attr("modelId");
					var applicationId = data.rslt.obj.attr('id');
					var model = m_model.findModel(modelId);
					var application = model.applications[applicationId];

					if (application.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createRenameCommand("/models/" + model.id
										+ "/applications/" + application.id, {
									"id" : application.id,
									"name" : application.name
								}, {
									"name" : data.rslt.name
								}));
					}
				} else if (data.rslt.obj.attr("rel") == "structuredDataType") {
					var modelId = data.rslt.obj.attr("modelId");
					var dataTypeId = data.rslt.obj.attr('id');
					var model = m_model.findModel(modelId);
					var dataType = model.structuredDataTypes[dataTypeId];

					if (dataType.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createRenameCommand(
										"/models/" + model.id
												+ "/structuredDataTypes/"
												+ dataType.id, {
											"id" : dataType.id,
											"name" : dataType.name
										}, {
											"name" : data.rslt.name
										}));
					}
				} else if (data.rslt.obj.attr("rel") == "participant_role") {
					var modelId = data.rslt.obj.attr("modelId");
					var participantId = data.rslt.obj.attr('id');
					var model = m_model.findModel(modelId);
					var participant = model.participants[participantId];

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
					if (e) {
						parent.iDnD.setIframeXY(e, window.name);
					} else {
						parent.iDnD.setIframeXY(window.event, window.name);
					}
				};

				document.onmouseup = function() {
					parent.iDnD.hideIframe();
				};

				// Tree Node Selection

				jQuery("#outline")
						.bind(
								"select_node.jstree",
								function(event, data) {
									if (data.rslt.obj.attr('rel') == 'model') {
										var modelId = data.rslt.obj.attr('id');
										var modelName = data.inst.get_text();
										// data.inst gives you the actual tree
										// instance
										var link = jQuery(
												"a[id $= 'model_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events.publish(
												"OPEN_VIEW", linkId, formId,
												"modelView", "modelId="
														+ modelId
														+ "&modelName="
														+ modelName, modelId);
									} else if (data.rslt.obj.attr('rel') == 'process') {
										var processId = data.rslt.obj
												.attr('id');
										var processName = data.inst.get_text();
										// data.inst gives you the actual tree
										// instance
										var modelId = data.inst._get_parent(
												data.rslt.obj).attr('id');
										var link = jQuery(
												"a[id $= 'modeler_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events.publish(
												"OPEN_VIEW", linkId, formId,
												"modelerView", "processId="
														+ processId
														+ "&modelId=" + modelId
														+ "&processName="
														+ processName,
												processId);
									} else if (data.rslt.obj.attr('rel') == "webservice") {
										var link = jQuery(
												"a[id $= 'webservice_application_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events.publish(
												"OPEN_VIEW", linkId, formId,
												"webServiceApplicationView");
									} else if (data.rslt.obj.attr('rel') == "messageTransformationBean") {
										var applicationId = data.rslt.obj
												.attr('id');
										var modelId = data.inst._get_parent(
												data.rslt.obj).attr('modelId');
										var link = jQuery(
												"a[id $= 'message_transformation_application_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events
												.publish(
														"OPEN_VIEW",
														linkId,
														formId,
														"messageTransformationApplicationView",
														"modelId="
																+ modelId
																+ "&applicationId="
																+ applicationId,
														applicationId); // TODO
										// Review
										// View
										// ID
									} else if (data.rslt.obj.attr('rel') == "camelBean") {
										var applicationId = data.rslt.obj
												.attr('id');
										var modelId = data.inst._get_parent(
												data.rslt.obj).attr('modelId');
										var link = jQuery(
												"a[id $= 'camel_application_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events.publish(
												"OPEN_VIEW", linkId, formId,
												"camelApplicationView",
												"modelId=" + modelId
														+ "&applicationId="
														+ applicationId,
												applicationId); // TODO
										// Review
										// View
										// ID
									} else if (data.rslt.obj.attr('rel') == "interactive") {
										var link = jQuery(
												"a[id $= 'ui_mashup_application_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events.publish(
												"OPEN_VIEW", linkId, formId,
												"uiMashupApplicationView");
									} else if (data.rslt.obj.attr('rel') == "structuredDataType") {
										var structuredDataTypeId = data.rslt.obj
												.attr('id');
										var modelId = data.inst._get_parent(
												data.rslt.obj).attr('modelId');
										var link = jQuery(
												"a[id $= 'xsd_structured_data_type_view_link']",
												window.parent.frames['ippPortalMain'].document);
										var linkId = link.attr('id');
										var form = link.parents('form:first');
										var formId = form.attr('id');

										window.parent.EventHub.events
												.publish(
														"OPEN_VIEW",
														linkId,
														formId,
														"xsdStructuredDataTypeView",
														"modelId="
																+ modelId
																+ "&structuredDataTypeId="
																+ structuredDataTypeId,
														structuredDataTypeId);
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
																				'id'),
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
																				.attr('id'));
																	});
														}
													},
													"deploy" : {
														"label" : "Deploy",
														"action" : function(obj) {
															deployModel(obj
																	.attr('id'));
														}
													},
													"createProcess" : {
														"label" : "Create Process",
														"action" : function(obj) {
															createProcess(obj
																	.attr('id'));
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
																	.attr("modelId"));
														}
													},
													"createMessageTransformationApplication" : {
														"label" : "Create Transformation",
														"action" : function(obj) {
															createMessageTransformationApplication(obj
																	.attr("modelId"));
														}
													},
													"createCamelApplication" : {
														"label" : "Create Camel Route",
														"action" : function(obj) {
															createCamelApplication(obj
																	.attr("modelId"));
														}
													},
													"createUiMashupApplication" : {
														"label" : "Create UI Mashup",
														"action" : function(obj) {
															createUiMashupApplication(obj
																	.attr("modelId"));
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
															createXsdStructuredDataType(obj
																	.attr("modelId"));
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
																						.attr('id'));
																	});
														}
													}
												};
											} else if ('participant_role' == node
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
																						.attr('id'));
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
														"participant_role",
														"participant_org",
														"participant_cp" ]
											},
											"participant_role" : {
												"icon" : {
													"image" : "../images/icons/role.png"
												}
											},
											"participant_org" : {
												"icon" : {
													"image" : "../images/icons/organization.png"
												}
											},
											"participant_cp" : {
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
											"manual_activity" : {
												"icon" : {
													"image" : "../images/icons/activity_manual.png"
												}
											},
											"sub_process_activity" : {
												"icon" : {
													"image" : "../images/icons/activity_subprocess.gif"
												}
											},
											"application_activity" : {
												"icon" : {
													"image" : "../images/icons/activity_application.gif"
												}
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
				 * 
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

				function deleteModel(modelId) {
					m_commandsController.submitCommand(m_command
							.createDeleteCommand("/models/" + modelId, {
								"id" : modelId
							}));
				}

				/**
				 * 
				 */
				function createProcess(modelId) {
					var number = (++modelCounter);
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
				function createWebServiceApplication(modelId) {
					var number = (++processCounter);
					var name = "Web Service " + number;
					var id = "WebService" + number;

					m_commandsController.submitCommand(m_command
							.createCreateCommand("/models/" + modelId
									+ "/applications/webServiceApplications", {
								"name" : name,
								"id" : id
							}));
				}

				/**
				 * 
				 */
				function createMessageTransformationApplication(modelId) {
					var number = (++processCounter);
					var name = "Message Transformation " + number;
					var id = "MessageTransformation" + number;

					m_commandsController
							.submitCommand(m_command
									.createCreateCommand(
											"/models/"
													+ modelId
													+ "/applications/messageTransformationApplications",
											{
												"name" : name,
												"id" : id
											}));
				}

				/**
				 * 
				 */
				function createCamelApplication(modelId) {
					var number = (++processCounter);
					var name = "Camel Route " + number;
					var id = "CamelRoute" + number;

					m_commandsController.submitCommand(m_command
							.createCreateCommand("/models/" + modelId
									+ "/applications/camelApplications", {
								"name" : name,
								"id" : id
							}));
				}

				/**
				 * 
				 */
				function createUiMashupApplication(modelId) {
					var number = (++processCounter);
					var name = "UI Mashup " + number;
					var id = "UIMashup" + number;

					m_commandsController.submitCommand(m_command
							.createCreateCommand("/models/" + modelId
									+ "/applications/externalWebApplications",
									{
										"name" : name,
										"id" : id
									}));
				}

				/**
				 * 
				 * @param modelId
				 * @returns
				 */
				function createXsdStructuredDataType(modelId) {
					var number = (++processCounter);
					// TODO obtain number from model
					var name = "XSD Data Structure " + number;
					var id = "XSDDataStructure" + number;

					m_commandsController.submitCommand(m_command
							.createCreateCommand("/models/" + modelId
									+ "/structuredDataTypes", {
								"name" : name,
								"id" : id
							}));
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

				window.parent.EventHub.events.subscribe("ELEMENT_CREATED",
						elementCreationHandler);
				window.parent.EventHub.events.subscribe("ELEMENT_RENAMED",
						elementRenamingHandler);

				readAllModels();
			};

			var outline;

			return {
				"init" : function() {
					setupEventHandling();

					outline = new Outline();

					outline.initialize();

					m_outlineToolbarController.init("outlineToolbar");
				},
				"refresh" : function() {
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
						for (var i = 0; i < obj.changes.added.length; i++) {
							//Create Process
							if ("process" == command.changes.added[i].type) {
								this.createProcess(command.changes.added[i]);
							} else if ("model" == command.changes.added[i].type) {
								this.createModel(command.changes.added[i]);
							}
						}
						for ( var i = 0; i < obj.changes.modified.length; i++) {
							var modelElement = m_model.findModelElementByGuid(obj.changes.modified[i].oid);

							m_utils.debug("Models:");
							m_utils.debug(m_model.getModels());
							m_utils.debug("Model Element:");
							m_utils.debug(modelElement);

							var oldId = modelElement.id;
							
							if (modelElement != null) {
								modelElement.rename(obj.changes.modified[i].id,
										obj.changes.modified[i].name);

								// TODO Improve! This must find nodes uniquely and
								// by
								// type! May be nodes should save the REST URI as
								// id?
								var link = jQuery("li#" + oldId + " a")[0];
								var node = jQuery("li#" + oldId);

								node.attr("id", modelElement.id);
								node.attr("fullId", modelElement.getFullId());
								node.attr("name", modelElement.name);

								var textElem = jQuery(link.childNodes[1])[0];

								textElem.nodeValue = modelElement.name;
							}
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
					m_model.createModel(data.id, data.name);
					jQuery("#outline").jstree("create", "#Models", "last", {
						"attr" : {
							"id" : data.id,
							"rel" : "model"
						},
						"data" : data.name
					}, null, false);
					jQuery("#outline").jstree("set_type", "model",
							"#" + data.id);
					jQuery("#outline").jstree("create", "#" + data.id, "first",
							{
								"attr" : {
									"id" : "structuredTypes_" + data.id,
									"rel" : "structuredTypes",
									"modelId" : data.id
								},
								"data" : "Structured Types"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.id, "first",
							{
								"attr" : {
									"id" : "data_" + data.id,
									"rel" : "data"
								},
								"data" : "Data"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.id, "first",
							{
								"attr" : {
									"modelId" : data.id,
									"id" : "applications_" + data.id,
									"rel" : "applications"
								},
								"data" : "Applications"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.id, "first",
							{
								"attr" : {
									"id" : "participants_" + data.id,
									"rel" : "participants"
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
				/**
				 * 
				 */
				Outline.prototype.createProcess = function(transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var process = m_process.createProcessFromJson(model,
							transferObject);
					var parentSelector = '#' + model.id;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : process.id,
									"modelId" : model.id,
									"rel" : "process",
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
							+ transferObject.modelId;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : application.applicationType,
									"id" : application.id,
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
					var model = m_model.findModel(transferObject.modelId);
					// TODO Add
				};

				/**
				 * 
				 */
				Outline.prototype.createStructuredDataType = function(
						transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var dataStructure = m_dataStructure.initializeFromJson(
							model, transferObject);
					var parentSelector = '#structuredTypes_' + model.id;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : "structuredDataType",
									"modelId" : model.id,
									"id" : dataStructure.id,
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
