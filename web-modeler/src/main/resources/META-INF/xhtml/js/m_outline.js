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
				"m_command", "m_session", "m_user", "m_model", "m_process",
				"m_application", "m_participant", "m_typeDeclaration",
				"m_outlineToolbarController", "m_data",
				"m_elementConfiguration", "m_messageDisplay" ],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_communicationController, m_commandsController, m_command,
				m_session, m_user, m_model, m_process, m_application,
				m_participant, m_typeDeclaration, m_outlineToolbarController,
				m_data, m_elementConfiguration, m_messageDisplay) {

			// TODO Find better location
			var viewManagerExtension = m_extensionManager
					.findExtension("viewManager");
			var viewManager = viewManagerExtension.provider.create();

			var isElementCreatedViaOutline = false;
			var hasUnsavedModifications = false;
			function getURL() {
				return require('m_urlUtils').getContextName()
						+ "/services/rest/bpm-modeler/modeler/" + new Date().getTime();
			}

			var readAllModels = function(force) {
				m_model.loadModels(force);

				jQuery
						.each(
								m_model.getModels(),
								function(index, model) {

									jQuery("#outline").jstree("create",
											"#outline", "first", {
												"attr" : {
													"id" : model.uuid,
													"rel" : "model",
													"elementId" : model.id
												},
												"data" : model.name
											}, null, true);

									jQuery("#outline").jstree("set_type",
											"model", "#" + model.uuid);

									jQuery
											.each(
													model.processes,
													function(index, process) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#"
																				+ model.uuid,
																		"last",
																		{
																			"attr" : {
																				"id" : process.uuid,
																				"oid" : process.oid,
																				"fullId" : process
																						.getFullId(),
																				"modelId" : model.id,
																				"modelUUID" : model.uuid,
																				"rel" : "process",
																				"draggable" : true,
																				"elementId" : process.id
																			},
																			"data" : process.name
																		},
																		null,
																		true);
														jQuery("#outline")
																.jstree(
																		"close_node",
																		"#"
																				+ process.id);
													});

									jQuery("#outline").jstree(
											"create",
											"#" + model.uuid,
											"first",
											{
												"attr" : {
													"id" : "participants_"
															+ model.uuid,
													"rel" : "participants",
													"modelUUID" : model.uuid
												},
												"data" : "Participants"
											}, null, true);

									jQuery
											.each(
													model.participants,
													function(index, participant) {
														if (!participant.parentUUID) {
															jQuery("#outline")
																	.jstree(
																			"create",
																			"#participants_"
																					+ model.uuid,
																			"last",
																			{
																				"attr" : {
																					"id" : participant.uuid,
																					"fullId" : participant
																							.getFullId(),
																					"rel" : participant.type,
																					"modelId" : model.id,
																					"modelUUID" : model.uuid,
																					"draggable" : true,
																					"elementId" : participant.id
																				},
																				"data" : participant.name
																			},
																			null,
																			true);
															// Load child
															// participants
															loadChildParticipants(
																	model,
																	participant);

															jQuery("#outline")
																	.jstree(
																			"close_node",
																			"#"
																					+ participant.uuid);
														}
													});
									jQuery("#outline").jstree("close_node",
											"#participants_" + model.uuid);

									// Applications

									jQuery("#outline").jstree(
											"create",
											"#" + model.uuid,
											"first",
											{
												"attr" : {
													"modelId" : model.id,
													"modelUUID" : model.uuid,
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
																				"modelUUID" : model.uuid,
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

									jQuery("#outline")
											.jstree(
													"create",
													"#" + model.uuid,
													"first",
													{
														"attr" : {
															"id" : "data_"
																	+ model.uuid,
															"rel" : "data",
															"modelUUID" : model.uuid
														},
														"data" : "Data"
													}, null, true);

									// Create Data nodes
									jQuery
											.each(
													model.dataItems,
													function(index, data) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#data_"
																				+ model.uuid,
																		"last",
																		{
																			"attr" : {
																				"id" : data.uuid,
																				"modelUUID" : model.uuid,
																				"fullId" : data
																						.getFullId(),
																				"rel" : data.dataType,
																				"elementId" : data.id,
																				"draggable" : true
																			},
																			"data" : data.name
																		},
																		null,
																		true);
														jQuery("#outline")
																.jstree(
																		"close_node",
																		"#data_"
																				+ model.uuid);
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
													"modelUUID" : model.uuid
												},
												"data" : "Structured Types"
											}, null, true);

									// Create structured data type nodes

									jQuery
											.each(
													model.typeDeclarations,
													function(index,
															typeDeclaration) {
														jQuery("#outline")
																.jstree(
																		"create",
																		"#structuredTypes_"
																				+ model.uuid,
																		"last",
																		{
																			"attr" : {
																				"id" : typeDeclaration.uuid,
																				"fullId" : typeDeclaration
																						.getFullId(),
																				"elementId" : typeDeclaration.id,
																				"rel" : "structuredDataType",
																				"modelId" : model.id,
																				"modelUUID" : model.uuid,
																				"draggable" : true
																			},
																			"data" : typeDeclaration.name
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
				hasUnsavedModifications = false;
			};

			var loadChildParticipants = function(model, parentParticipant) {
				if (parentParticipant.childParticipants) {
					jQuery
							.each(
									parentParticipant.childParticipants,
									function(index, participant) {
										jQuery("#outline")
												.jstree(
														"create",
														"#"
																+ parentParticipant.uuid,
														"last",
														{
															"attr" : {
																"id" : participant.uuid,
																"rel" : participant.type,
																"fullId" : participant
																		.getFullId(),
																"modelId" : model.id,
																"modelUUID" : model.uuid,
																"parentUUID" : parentParticipant.uuid,
																"draggable" : true,
																"elementId" : participant.id
															},
															"data" : participant.name
														}, null, true);
										loadChildParticipants(model,
												participant);
										jQuery("#outline").jstree("close_node",
												"#" + participant.uuid);
									});
				}
			}

			var deployModel = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);
				var modeleDeployerLink = jQuery(
						"a[id $= 'model_deployer_link']",
						window.parent.frames['ippPortalMain'].document);
				var modeleDeployerLinkId = modeleDeployerLink.attr('id');
				var form = modeleDeployerLink.parents('form:first');
				var formId = form.attr('id');

				if (model.fileName && model.filePath) {
					window.parent.EventHub.events.publish(
							"SELECT_MODEL_FOR_DEPLOYMENT",
							modeleDeployerLinkId, model.fileName,
							model.filePath, formId);
				} else {
					alert("Cannot deploy: Model file name / path not available");
				}

			};

			var downloadModel = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);

				window.location = require("m_urlUtils").getModelerEndpointUrl()
						+ "/models/" + model.id + "/download";
			}

			var openModelReport = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);

                window.open("../public/reportTest.html?modelId=" + model.id);
			}

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
					var model = m_model.findModelByUuid(data.rslt.obj
							.attr("id"));

					if (model.name != data.rslt.name) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelCommand(model.uuid, {
									"name" : data.rslt.name,
									"id" : m_utils
											.generateIDFromName(data.rslt.name)
								}));
					}
				} else {
					var model = m_model.findModelByUuid(data.rslt.obj
							.attr("modelUUID"));
					var modelElement = model
							.findModelElementByUuid(data.rslt.obj.attr("id"));

					m_commandsController
							.submitCommand(m_command
									.createUpdateModelElementWithUUIDCommand(
											model.id,
											modelElement.uuid,
											{
												"name" : data.rslt.name,
												"id" : m_utils
														.generateIDFromName(data.rslt.name)
											}));
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
					parent.iPopupDialog.openPopup({
						attributes : {
							width : "400px",
							height : "200px",
							src : "../bpm-modeler/popups/outlineRefreshConfirmationDialog.html"
						},
						payload : {
							title : "Confirm",
							message : "All models will be reloaded from their last saved state and the session log will be cleared.<BR><BR>Continue?<BR><BR>",
							acceptButtonText : "Yes",
							cancelButtonText : "No",
							acceptFunction : reloadOutlineTree
						}});
				}
			}

			var reloadOutlineTree = function(saveFirst) {
				if (true == saveFirst) {
					saveAllModels();
				}
				jQuery("#outline").empty();
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
										src : "../bpm-modeler/popups/confirmationPopupDialogContent.html"
									},
									payload : {
										title : "Warning",
										message : "Models have unsaved changes.<BR><BR>Please save models before continuing.",
										acceptButtonText : "Close",
										acceptFunction : function() {
											//Do nothing
										}
									}
								});
					} else {
						alert("Models have unsaved changes. Please save models before continuing.");
					}
				} else {
					var link = jQuery("a[id $= 'open_model_upload_dialog_link']",
							window.parent.frames['ippPortalMain'].document);
					var linkId = link.attr('id');
					var form = link.parents('form:first');
					var formId = form.attr('id');
					window.parent.EventHub.events.publish("OPEN_IMPORT_MODEL_DIALOG", linkId, formId);
				}
			}

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

			// TODO - delete
			// var getTreeNodeId = function (modelId, nodeType, nodeId) {
			// return modelId + "__" + nodeType + "__" + nodeId;
			// };

			// var extractElementIdFromTreeNodeId = function (nodeId) {
			// var index = m_utils.getLastIndexOf(nodeId, "__");
			// return nodeId.substring(index);
			// };

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
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView("modelView",
												"modelId=" + model.id
														+ "&modelName="
														+ model.name,
												model.uuid);
									} else if (data.rslt.obj.attr('rel') == "roleParticipant"
										|| data.rslt.obj.attr('rel') == "teamLeader") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var role = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView("roleView",
												"roleId=" + role.id
														+ "&modelId="
														+ model.id
														+ "&roleName="
														+ role.name
														+ "&fullId="
														+ role.getFullId(),
												role.uuid);
									} else if (data.rslt.obj.attr('rel') == 'organizationParticipant') {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var organization = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"organizationView",
												"organizationId="
														+ organization.id
														+ "&modelId="
														+ model.id
														+ "&organizationName="
														+ organization.name
														+ "&fullId="
														+ organization
																.getFullId(),
												organization.uuid);
									} else if (m_elementConfiguration
											.isValidDataType(data.rslt.obj
													.attr('rel'))) {

										// TODO Above is very ugly!
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var data = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView("dataView",
												"dataId=" + data.id
														+ "&modelId="
														+ model.id
														+ "&dataName="
														+ data.name
														+ "&fullId="
														+ data.getFullId(),
												data.uuid);
									} else if (data.rslt.obj.attr('rel') == 'process') {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var process = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"processDefinitionView",
												"processId=" + process.id
														+ "&modelId="
														+ model.id
														+ "&processName="
														+ process.name
														+ "&fullId="
														+ process.getFullId(),
												process.uuid);
									} else if (data.rslt.obj.attr('rel') == "webservice") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"webServiceApplicationView",
												"modelId="
														+ model.id
														+ "&applicationId="
														+ application.id
														+ "&applicationName="
														+ application.name
														+ "&fullId="
														+ application
																.getFullId(),
												application.uuid);
									} else if (data.rslt.obj.attr('rel') == "messageTransformationBean") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"messageTransformationApplicationView",
														"modelId="
																+ model.id
																+ "&applicationId="
																+ application.id
																+ "&applicationName="
																+ application.name
																+ "&fullId="
																+ application
																		.getFullId(),
														application.uuid);
									} else if (data.rslt.obj.attr('rel') == "camelSpringProducerApplication") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"camelApplicationView",
												"modelId="
														+ model.id
														+ "&applicationId="
														+ application.id
														+ "&applicationName="
														+ application.name
														+ "&fullId="
														+ application
																.getFullId(),
												application.uuid);
									} else if (data.rslt.obj.attr('rel') == "interactive") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"uiMashupApplicationView",
												"modelId="
														+ model.id
														+ "&applicationId="
														+ application.id
														+ "&applicationName="
														+ application.name
														+ "&fullId="
														+ application
																.getFullId(),
												application.uuid);
									} else if (m_elementConfiguration
											.isUnSupportedAppType(data.rslt.obj
													.attr('rel'))) {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"genericApplicationView",
												"modelId="
														+ model.id
														+ "&applicationId="
														+ application.id
														+ "&applicationName="
														+ application.name
														+ "&fullId="
														+ application
																.getFullId(),
												application.uuid);
									} else if (data.rslt.obj.attr('rel') == "structuredDataType") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var structuredDataType = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"xsdStructuredDataTypeView",
														"modelId="
																+ model.id
																+ "&structuredDataTypeId="
																+ structuredDataType.id
																+ "&structuredDataTypeName="
																+ structuredDataType.name
																+ "&fullId="
																+ structuredDataType
																		.getFullId(),
														structuredDataType.uuid);
									} else if (data.rslt.obj.attr('rel') == "conditionalPerformerParticipant") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var conditionalPerformer = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"conditionalPerformerView",
														"modelId="
																+ model.id
																+ "&conditionalPerformerId="
																+ conditionalPerformer.id
																+ "&conditionalPerformerName="
																+ conditionalPerformer.name
																+ "&fullId="
																+ conditionalPerformer
																		.getFullId(),
														conditionalPerformer.uuid);
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
													"createProcess" : {
														"label" : "Create Process",
														"action" : function(obj) {
															createProcess(obj
																	.attr("elementId"));
														}
													},
													"deploy" : {
														"label" : "Deploy",
														"action" : function(obj) {
															deployModel(obj
																	.attr("id"));
														}
													},
													"download" : {
														"label" : "Download Model",
														"action" : function(obj) {
															downloadModel(obj
																	.attr("id"));
														}
													},
													"openModelReport" : {
														"label" : "Open Model Report",
														"action" : function(obj) {
															openModelReport(obj
																	.attr("id"));
														}
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
																						.attr("elementId"),
																				obj
																						.attr("modelUUID"));
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
																	.attr("modelUUID"));
														}
													},
													"createMessageTransformationApplication" : {
														"label" : "Create Transformation",
														"action" : function(obj) {
															createMessageTransformationApplication(obj
																	.attr("modelUUID"));
														}
													},
													"createCamelApplication" : {
														"label" : "Create Camel Route",
														"action" : function(obj) {
															createCamelApplication(obj
																	.attr("modelUUID"));
														}
													},
													"createUiMashupApplication" : {
														"label" : "Create UI Mashup",
														"action" : function(obj) {
															createUiMashupApplication(obj
																	.attr("modelUUID"));
														}
													}
												};
											} else if ('data' == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : false,
													"createPrimitiveData" : {
														"label" : "Create Primitive Data",
														"action" : function(obj) {
															createPrimitiveData(obj
																	.attr("modelUUID"));
														}
													},
													"createDocumentData" : {
														"label" : "Create Document",
														"action" : function(obj) {
															createDocumentData(obj
																	.attr("modelUUID"));
														}
													},
													"createStructuredData" : {
														"label" : "Create Structured Data",
														"action" : function(obj) {
															createStructuredData(obj
																	.attr("modelUUID"));
														}
													}
												};
											} else if (m_elementConfiguration
													.isValidDataType(node
															.attr("rel"))) {
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
													"deleteData" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteData(
																				obj
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
																	});
														}
													}
												};
											} else if ("participants" == node
													.attr('rel')) {
												return {
													"ccp" : false,
													"create" : false,
													"rename" : false,
													"createRole" : {
														"label" : "Create Role",
														"action" : function(obj) {
															createRole(obj
																	.attr("modelUUID"));
														}
													},
													"createOrganization" : {
														"label" : "Create Organization",
														"action" : function(obj) {
															createOrganization(obj
																	.attr("modelUUID"));
														}
													},
													"createConditionalPerformer" : {
														"label" : "Create Conditional Performer",
														"action" : function(obj) {
															createConditionalPerformer(obj
																	.attr("modelUUID"));
														}
													}
												};
											} else if (m_elementConfiguration
													.isValidAppType(node
															.attr("rel"))) {
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
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
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
																	.attr("modelUUID"));
														}
													},
													importTypeDeclarations : {
														"label" : "Import Type Declarations",
														"action" : function(obj) {
															var model = m_model
																	.findModelByUuid(obj
																			.attr("modelUUID"));

															importTypeDeclarations(model);
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
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
																	});
														}
													}
												};
											} else if ('roleParticipant' == node.attr('rel')
													|| 'teamLeader' == node.attr('rel')) {
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
													"deleteParticipant" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteParticipant(
																				obj
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
																	});
														}
													},
													"setAsManager" : {
														"label" : "Set As Manager",
														"_disabled" : ((undefined == node
																.attr("parentUUID")) || ('teamLeader' == node.attr('rel'))),
														"action" : function(obj) {
															setAsManager(
																	node
																			.attr("modelUUID"),
																	node
																			.attr("parentUUID"),
																	node
																			.attr("id"));
														}
													}
												};
											} else if ("conditionalPerformerParticipant" == node
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
													"deleteParticipant" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteParticipant(
																				obj
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
																	});
														}
													}
												};
											} else if ('organizationParticipant' == node
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
													"deleteParticipant" : {
														"label" : "Delete",
														"action" : function(obj) {
															deleteElementAction(
																	obj.context.lastChild.data,
																	function() {
																		deleteParticipant(
																				obj
																						.attr("modelUUID"),
																				obj
																						.attr("elementId"));
																	});
														}
													},
													"createRole" : {
														"label" : "Create Role",
														"action" : function(obj) {
															createRole(
																	obj
																			.attr("modelUUID"),
																	obj
																			.attr("id"));
														}
													},
													"createOrganization" : {
														"label" : "Create Organization",
														"action" : function(obj) {
															createOrganization(
																	obj
																			.attr("modelUUID"),
																	obj
																			.attr("id"));
														}
													}
												};
											}

											return {};
										}
									},
									types : {
										"types" : {
											"model" : {
												"icon" : {
													"image" : "../images/icons/model.png"
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
													"image" : "../images/icons/participants.png"
												}
											},
											"roleParticipant" : {
												"icon" : {
													"image" : "../images/icons/role.png"
												}
											},
											"teamLeader" : {
												"icon" : {
													"image" : "../images/icons/manager.png"
												}
											},
											"organizationParticipant" : {
												"icon" : {
													"image" : "../images/icons/organization.png"
												}
											},
											"conditionalPerformerParticipant" : {
												"icon" : {
													"image" : "../images/icons/conditional.png"
												}
											},
											"process" : {
												"icon" : {
													"image" : "../images/icons/process.png"
												}
											},
											"structuredTypes" : {
												"icon" : {
													"image" : "../images/icons/structured-types.png"
												}
											},
											"structuredDataType" : {
												"icon" : {
													"image" : "../images/icons/structured-type.png"
												}
											},
											"applications" : {
												"icon" : {
													"image" : "../images/icons/applications.png"
												}
											},
											"interactive" : {
												"icon" : {
													"image" : "../images/icons/application-c-ext-web.png"
												}
											},
											"plainJava" : {
												"icon" : {
													"image" : "../images/icons/application-plain-java.png"
												}
											},
											"jms" : {
												"icon" : {
													"image" : "../images/icons/application-jms.png"
												}
											},
											"webservice" : {
												"icon" : {
													"image" : "../images/icons/application-web-service.png"
												}
											},
											"dmsOperation" : {
												"icon" : {
													"image" : "../images/icons/application-dms.png"
												}
											},
											"mailBean" : {
												"icon" : {
													"image" : "../images/icons/application-mail.png"
												}
											},
											"messageParsingBean" : {
												"icon" : {
													"image" : "../images/icons/application-message-p.png"
												}
											},
											"messageSerializationBean" : {
												"icon" : {
													"image" : "../images/icons/application-message-s.png"
												}
											},
											"messageTransformationBean" : {
												"icon" : {
													"image" : "../images/icons/application-message-trans.png"
												}
											},
											"camelSpringProducerApplication" : {
												"icon" : {
													"image" : "../images/icons/application-camel.png"
												}
											},
											"rulesEngineBean" : {
												"icon" : {
													"image" : "../images/icons/application-drools.png"
												}
											},
											"sessionBean" : {
												"icon" : {
													"image" : "../images/icons/application-session.png"
												}
											},
											"springBean" : {
												"icon" : {
													"image" : "../images/icons/application-plain-java.png"
												}
											},
											"xslMessageTransformationBean" : {
												"icon" : {
													"image" : "../images/icons/application-message-trans.png"
												}
											},
											"data" : {
												"icon" : {
													"image" : "../images/icons/data.png"
												}
											},
											"primitive" : {
												"icon" : {
													"image" : "../images/icons/data-primitive.png"
												}
											},
											"hibernate" : {
												"icon" : {
													"image" : "../images/icons/data-hibernate.png"
												}
											},
											"struct" : {
												"icon" : {
													"image" : "../images/icons/data-structured.png"
												}
											},
											"serializable" : {
												"icon" : {
													"image" : "../images/icons/data-serializable.png"
												}
											},
											"entity" : {
												"icon" : {
													"image" : "../images/icons/data-entity.png"
												}
											},
											"dmsDocument" : {
												"icon" : {
													"image" : "../images/icons/data-document.png"
												}
											},
											"dmsDocumentList" : {
												"icon" : {
													"image" : "../images/icons/data-document-list.png"
												}
											},
											"dmsFolder" : {
												"icon" : {
													"image" : "../images/icons/data-folder.png"
												}
											},
											"dmsFolderList" : {
												"icon" : {
													"image" : "../images/icons/data-folder-list.png"
												}
											}
										}
									}
								}).jstree("set_theme", "classic",
								"../css/jstree/classic/style.css");
				// "themes" : {
				// "theme" : "default",
				// "url" : "/xhtml/css/jstree"}}).jstree("set_theme",
				// "default");

				var handleToolbarEvents = function(event, data) {
					if ("createModel" == data.id) {
						createModel();
					} else if ("importModel" == data.id) {
						importModel();
					} else if ("saveAllModels" == data.id) {
						saveAllModels();
					} else if ("refreshModels" == data.id) {
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
							src : "../bpm-modeler/popups/notificationDialog.html"
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
							src : "../bpm-modeler/popups/errorDialog.html"
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
				function createModel() {
					var count = 0;
					var name = 'Model ' + (++count);
					var id = m_utils.generateIDFromName(name);
					while (m_model.findModel(id)) {
						name = 'Model ' + (++count);
						id = m_utils.generateIDFromName(name);
					}

					m_commandsController.submitCommand(m_command
							.createCreateModelCommand({
								"name" : name,
								"id" : id
							}));
					isElementCreatedViaOutline = true;
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
						while (model.findModelElementById(id)) {
							var name = namePrefix + (++suffix);
							var id = m_utils.generateIDFromName(name);
						}
					}

					return name;
				}

				/**
				 *
				 */
				function createProcess(modelId) {
					var name = getUniqueNameForElement(modelId, "Process ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateProcessCommand(modelId, modelId, {
								"name" : name,
								"id" : id
							}));
					isElementCreatedViaOutline = true;
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
							src : "../bpm-modeler/popups/confirmationPopupDialogContent.html"
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
				function createPrimitiveData(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Primitive Data ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController
							.submitCommand(m_command
									.createCreatePrimitiveDataCommand(
											model.id,
											model.id,
											{
												"name" : name,
												"id" : id,
												"primitiveType" : m_constants.STRING_PRIMITIVE_DATA_TYPE
											}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createDocumentData(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Document data ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateDocumentDataCommand(model.id,
									model.id, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createStructuredData(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Structured Data ");
					var id = m_utils.generateIDFromName(name);
					var fullId = model.id + ":" + id;

					m_commandsController.submitCommand(m_command
							.createCreateStructuredDataCommand(model.id,
									model.id, {
										"name" : name,
										"id" : id,
										"structuredDataTypeFullId" : fullId
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createRole(modelUUId, targetUUID) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id, "Role ");
					var id = m_utils.generateIDFromName(name);
					var targetOid = (targetUUID ? m_model
							.findElementInModelByUuid(model.id, targetUUID).oid
							: model.id);

					m_commandsController.submitCommand(m_command
							.createCreateRoleCommand(model.id, targetOid, {
								"name" : name,
								"id" : id
							}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createConditionalPerformer(modelUUId, targetUUID) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Conditional Performer ");
					var id = m_utils.generateIDFromName(name);
					var targetOid = (targetUUID ? m_model
							.findElementInModelByUuid(model.id, targetUUID).oid
							: model.id);

					m_commandsController.submitCommand(m_command
							.createCreateConditionalPerformerCommand(model.id,
									targetOid, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function setAsManager(modelUUId, orgUUID, roleUUID) {
					var model = m_model.findModelByUuid(modelUUId);
					var orgOid = m_model.findElementInModelByUuid(model.id,
							orgUUID).oid;
					var roleUUID = m_model.findElementInModelByUuid(model.id,
							roleUUID).uuid;

					m_commandsController.submitCommand(m_command
							.createUpdateTeamLeaderCommand(model.id, orgOid, {
								"uuid" : roleUUID
							}));
				}

				/**
				 *
				 */
				function createOrganization(modelUUId, targetUUID) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Organization ");
					var id = m_utils.generateIDFromName(name);
					var targetOid = (targetUUID ? m_model
							.findElementInModelByUuid(model.id, targetUUID).oid
							: model.id);

					m_commandsController.submitCommand(m_command
							.createCreateOrganizationCommand(model.id,
									targetOid, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createWebServiceApplication(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id, "Web Service ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateWebServiceAppCommand(model.id,
									model.id, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createMessageTransformationApplication(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"Message Transformation ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateMessageTransfromationAppCommand(
									model.id, model.id, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createCamelApplication(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id, "Camel Route ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateCamelAppCommand(model.id, model.id, {
								"name" : name,
								"id" : id
							}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 */
				function createUiMashupApplication(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id, "UI Mashup ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateUiMashupAppCommand(model.id, model.id,
									{
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
				}

				/**
				 *
				 * @param modelId
				 * @returns
				 */
				function createXsdStructuredDataType(modelUUId) {
					var model = m_model.findModelByUuid(modelUUId);
					var name = getUniqueNameForElement(model.id,
							"XSD Data Structure ");
					var id = m_utils.generateIDFromName(name);

					m_commandsController.submitCommand(m_command
							.createCreateStructuredDataTypeCommand(model.id,
									model.id, {
										"name" : name,
										"id" : id
									}));
					isElementCreatedViaOutline = true;
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

				/**
				 *
				 * @param modelId
				 * @param id
				 * @returns
				 */
				function importTypeDeclarations(model) {
					var popupData = {
						attributes : {
							width : "750px",
							height : "600px",
							src : "../bpm-modeler/views/modeler/importTypeDeclarationsWizard.html"
						},
						payload : {
							model : model
						}
					};

					parent.iPopupDialog.openPopup(popupData);
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
				Outline.prototype.openElementView = function(element) {
					if (isElementCreatedViaOutline) {
						jQuery("#outline").jstree("select_node",
								"#" + element.uuid);
						jQuery("#outline").jstree("deselect_all");
						// Delay of 1000ms is added to avoid issues of node
						// getting out or rename mode if the view takes
						// a little longer to open - observed specifically on
						// first node creation after login,
						window.setTimeout(function() {
							jQuery("#outline").jstree("rename",
									"#" + element.uuid)
						}, 1000);
					}
					isElementCreatedViaOutline = false;
				}

				/**
				 *
				 */
				Outline.prototype.processCommand = function(command) {
					m_utils.debug("===> Outline Process Event");

					var obj = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != obj && null != obj.changes) {
						m_messageDisplay.markModified();
						hasUnsavedModifications = true;
						for ( var i = 0; i < obj.changes.added.length; i++) {
							// Create Process
							if (m_constants.PROCESS == command.changes.added[i].type) {
								this
										.openElementView(this
												.createProcess(command.changes.added[i]));
							} else if (m_constants.MODEL == command.changes.added[i].type) {
								this.openElementView(this
										.createModel(command.changes.added[i]));
							} else if (m_constants.TYPE_DECLARATION_PROPERTY == command.changes.added[i].type) {
								this
										.openElementView(this
												.createStructuredDataType(command.changes.added[i]));
							} else if (m_constants.DATA == command.changes.added[i].type) {
								this.openElementView(this
										.createData(command.changes.added[i]));
							} else if (m_constants.APPLICATION == command.changes.added[i].type) {
								this
										.openElementView(this
												.createApplication(command.changes.added[i]));
							} else if (m_constants.ROLE_PARTICIPANT_TYPE == command.changes.added[i].type
									|| m_constants.TEAM_LEADER_TYPE == command.changes.added[i].type
									|| m_constants.ORGANIZATION_PARTICIPANT_TYPE == command.changes.added[i].type
									|| m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == command.changes.added[i].type) {
								this
										.openElementView(this
												.createParticipant(command.changes.added[i]));
							}
						}
						for ( var i = 0; i < obj.changes.modified.length; i++) {
							if (m_constants.MODEL == obj.changes.modified[i].type) {
								var modelElement = m_model
										.findModelByUuid(obj.changes.modified[i].uuid);
								modelElement.fileName = obj.changes.modified[i].fileName;
								modelElement.filePath = obj.changes.modified[i].filePath;
							} else {
								var modelElement = m_model
										.findElementInModelByUuid(
												obj.changes.modified[i].modelId,
												obj.changes.modified[i].uuid);
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
								if (m_constants.ROLE_PARTICIPANT_TYPE == modelElement.type) {
									node
											.attr(
													m_constants.TEAM_LEADER_KEY,
													obj.changes.modified[i].isTeamLeader);
								}
								renameElementViewLabel(node.attr("rel"), node
										.attr("id"), node.attr("name"));
							}
						}
						for ( var i = 0; i < obj.changes.removed.length; i++) {
							if (m_constants.MODEL == command.changes.removed[i].type) {
								this.deleteModel(command.changes.removed[i]);
							} else if (m_constants.PROCESS == command.changes.removed[i].type) {
								this.deleteProcess(command.changes.removed[i]);
							} else if (m_constants.APPLICATION == command.changes.removed[i].type) {
								this
										.deleteApplication(command.changes.removed[i]);
							} else if (m_constants.PARTICIPANT == command.changes.removed[i].type
									|| m_constants.ROLE_PARTICIPANT_TYPE == command.changes.removed[i].type
									|| m_constants.TEAM_LEADER_TYPE == command.changes.removed[i].type
									|| m_constants.ORGANIZATION_PARTICIPANT_TYPE == command.changes.removed[i].type
									|| m_constants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE == command.changes.removed[i].type) {
								this
										.deleteParticipant(command.changes.removed[i]);
							} else if (m_constants.TYPE_DECLARATION_PROPERTY == command.changes.removed[i].type) {
								this
										.deleteTypeDeclaration(command.changes.removed[i]);
							} else if (m_constants.DATA == command.changes.removed[i].type) {
								this.deleteData(command.changes.removed[i]);
							}
						}
					} else if (command.scope == "all") {
						// @deprecated
						refresh();
					}
				};

				/**
				 *
				 */
				Outline.prototype.createModel = function(data) {
					var outlineObj = this;
					var model = m_model.createModel(data.id, data.name,
							data.uuid);
					jQuery("#outline").jstree("create", "#outline", "last", {
						"attr" : {
							"elementId" : data.id,
							"id" : data.uuid,
							"fullId" : model.getFullId(),
							"rel" : "model"
						},
						"data" : data.name
					}, null, true);
					jQuery("#outline").jstree("set_type", "model",
							"#" + data.uuid);

					jQuery("#outline").jstree("create", "#" + data.uuid,
							"last", {
								"attr" : {
									"id" : "structuredTypes_" + data.uuid,
									"rel" : "structuredTypes",
									"modelId" : data.id,
									"modelUUID" : data.uuid
								},
								"data" : "Structured Types"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.uuid,
							"last", {
								"attr" : {
									"id" : "data_" + data.uuid,
									"rel" : "data",
									"modelUUID" : data.uuid
								},
								"data" : "Data"
							}, null, true);
					jQuery.each(data.dataItems, function(key, value) {
						outlineObj.createData(value, true);
					});
					jQuery("#outline").jstree("close_node",
							"#" + "data_" + data.uuid);
					jQuery("#outline").jstree("create", "#" + data.uuid,
							"last", {
								"attr" : {
									"modelId" : data.id,
									"id" : "applications_" + data.uuid,
									"rel" : "applications",
									"modelUUID" : data.uuid
								},
								"data" : "Applications"
							}, null, true);
					jQuery("#outline").jstree("create", "#" + data.uuid,
							"last", {
								"attr" : {
									"id" : "participants_" + data.uuid,
									"rel" : "participants",
									"modelUUID" : data.uuid
								},
								"data" : "Participants"
							}, null, true);
					jQuery.each(data.participants, function(key, value) {
						outlineObj.createParticipant(value, true);
					});
					jQuery("#outline").jstree("close_node",
							"#" + "participants_" + data.uuid);

					return model;
				}

				/**
				 *
				 */
				Outline.prototype.deleteModel = function(transferObject) {
					m_model.deleteModel(transferObject.id);
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
				}

				/**
				 *
				 */
				Outline.prototype.deleteProcess = function(transferObject) {
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_process.deleteProcess(transferObject.id, model);
				}

				/**
				 *
				 */
				Outline.prototype.deleteApplication = function(transferObject) {
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_application.deleteApplication(transferObject.id, model);
				}

				/**
				 *
				 */
				Outline.prototype.deleteParticipant = function(transferObject) {
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_participant.deleteParticipantRole(transferObject.id,
							model);
				}

				/**
				 *
				 */
				Outline.prototype.deleteTypeDeclaration = function(
						transferObject) {
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
					var model = m_model
							.findModelForElement(transferObject.uuid);
					// TODO add delete type declaration code.
					// m_typeDeclaration.deleteApplication(transferObject.id,
					// model);
				}

				/**
				 *
				 */
				Outline.prototype.deleteData = function(transferObject) {
					jQuery("#outline").jstree("remove",
							"#" + transferObject.uuid)
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_data.deleteData(transferObject.id, model);
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
									"modelUUID" : model.uuid,
									"rel" : "process",
									"fullId" : process.getFullId(),
									"draggable" : true
								},
								"data" : process.name
							}, null, true);

					return process;
				};

				/**
				 *
				 */
				Outline.prototype.createApplication = function(transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var application = m_application.initializeFromJson(model,
							transferObject);
					var parentSelector = '#applications_' + model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : application.applicationType,
									"id" : application.uuid,
									"elementId" : application.id,
									"fullId" : application.getFullId(),
									"modelId" : application.modelId,
									"modelUUID" : model.uuid,
									"draggable" : true
								},
								"data" : application.name
							}, null, true);

					return application;
				};

				/**
				 *
				 */
				Outline.prototype.createData = function(transferObject) {
					var model = m_model
							.findModelByUuid(transferObject.modelUUID);
					var data = m_data.initializeFromJson(model, transferObject);
					var parentSelector = '#data_' + model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : data.dataType,
									"modelId" : model.id,
									"modelUUID" : model.uuid,
									"id" : data.uuid,
									"elementId" : data.id,
									"fullId" : data.getFullId(),
									"draggable" : true
								},
								"data" : data.name
							}, null, true);

					return data;
				};

				/**
				 *
				 */
				Outline.prototype.createStructuredDataType = function(
						transferObject) {
					var model = m_model.findModel(transferObject.modelId);
					var dataStructure = m_typeDeclaration.initializeFromJson(
							model, transferObject);
					var parentSelector = '#structuredTypes_' + model.uuid;

					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"rel" : "structuredDataType",
									"modelId" : model.id,
									"modelUUID" : model.uuid,
									"id" : dataStructure.uuid,
									"elementId" : dataStructure.id,
									"fullId" : dataStructure.getFullId(),
									"draggable" : true
								},
								"data" : dataStructure.name
							}, null, true);

					return dataStructure;
				};

				/**
				 *
				 */
				Outline.prototype.createParticipant = function(transferObject) {
					var model = m_model
							.findModelByUuid(transferObject.modelUUID);
					var participant = m_participant.initializeFromJson(model,
							transferObject);
					var parentSelector = (transferObject.parentUUID ? ("#" + transferObject.parentUUID)
							: ("#participants_" + model.uuid));
					jQuery("#outline").jstree("create", parentSelector, "last",
							{
								"attr" : {
									"id" : participant.uuid,
									"fullId" : participant.getFullId(),
									"rel" : participant.type,
									"modelId" : model.id,
									"modelUUID" : model.uuid,
									"parentUUID" : transferObject.parentUUID,
									"draggable" : true,
									"elementId" : participant.id
								},
								"data" : participant.name
							}, null, true);

					return participant;
				}
			}
		});
