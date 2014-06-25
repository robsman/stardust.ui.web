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
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_session",
				"bpm-modeler/js/m_user", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_process", "bpm-modeler/js/m_application",
				"bpm-modeler/js/m_participant",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_outlineToolbarController",
				"bpm-modeler/js/m_data",
				"bpm-modeler/js/m_elementConfiguration",
				"bpm-modeler/js/m_jsfViewManager",
				"bpm-modeler/js/m_messageDisplay",
				"bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_modelerUtils",
				"bpm-modeler/js/m_jsfViewManagerHelper", "bpm-modeler/js/m_modelsSaveStatus" ],
		function(m_utils, m_urlUtils, m_constants, m_extensionManager,
				m_communicationController, m_commandsController, m_command,
				m_session, m_user, m_model, m_process, m_application,
				m_participant, m_typeDeclaration, m_outlineToolbarController,
				m_data, m_elementConfiguration, m_jsfViewManager,
				m_messageDisplay, m_i18nUtils, m_modelerUtils, m_jsfViewManagerHelper, m_modelsSaveStatus) {
			var isElementCreatedViaOutline = false;
			var displayScope = "";

			function getURL() {
				return m_urlUtils.getContextName()
						+ "/services/rest/bpm-modeler/modeler/"
						+ new Date().getTime();
			}

      function DomTreeBuilder() {
      }

      DomTreeBuilder.prototype.buildNode = function(nodeConfig, parentNode) {
        var node = jQuery("<li>");
        node.attr(nodeConfig.attr);

        jQuery("<a href='#'>" + nodeConfig.data + "</a>").appendTo(node);

        if (parentNode) {
          var parentSelector = jQuery(parentNode);
          if (!parentSelector.children("ul").length) {
            parentSelector.append("<ul>");
          }
          parentSelector.children("ul").append(node);
        }

        return node;
      };

      function TreeNodeBuilder(jsTree) {
        this.jsTree = jsTree;
      }

      TreeNodeBuilder.prototype.buildNode = function(nodeConfig, parentNode) {
        return this.jsTree.create_node(parentNode, "last", nodeConfig, null, true);
      };

      function OutlineUiModelBuilder(model, nodeBuilder) {
        this.model = model;
        this.nodeBuilder = nodeBuilder;
      }

      OutlineUiModelBuilder.prototype.buildModelNode = function(parent) {
        // alias to be used from jQuery.each callbacks
        var self = this;
        	modelTreeType="model",
        	isLocked=this.model.isReadonly(),
        	isEditLocked=this.model.isReadonly() &&
				         this.model.editLock &&
				         ("lockedByOther" === this.model.editLock.lockStatus);

        if(isLocked){
        	modelTreeType="lockedModel";
        	if(isEditLocked){
        		modelTreeType="lockedModelForEdit";
        	}
        }
        // Model
        var modelNode = this.nodeBuilder.buildNode({
          attr : {
            "id" : this.model.uuid,
            "rel" : modelTreeType,
            "elementId" : this.model.id
          },
          data : this.model.name
        }, parent);

        // Structured Data Types
        var structTypesNode = self.buildContainerNode("structuredTypes", modelNode);
        jQuery.each(this.model.typeDeclarations, function(index, typeDeclaration) {
          self.buildTypeDeclarationNode(typeDeclaration, structTypesNode);
        });

        // Data
        var globalDataNode = self.buildContainerNode("data", modelNode);
        jQuery.each(this.model.dataItems, function(index, data) {
          self.buildDataNode(data, globalDataNode);
        });

        // Applications
        var globalAppsNode = self.buildContainerNode("applications", modelNode);
        jQuery.each(this.model.applications, function(index, application) {
          self.buildApplicationNode(application, globalAppsNode);
        });

        // Participants
        var globalParticipantsNode = self.buildContainerNode("participants", modelNode);
        jQuery.each(this.model.participants, function(index, participant) {
          // start with top level participants
          if (!participant.parentUUID) {
            self.buildParticipantNode(participant, globalParticipantsNode);
          }
        });

        // Processes
        jQuery.each(this.model.processes, function(index, process) {
          self.buildProcessNode(process, modelNode);
        });

        return modelNode;
      };

      OutlineUiModelBuilder.prototype.buildErroredModelNode = function(parent) {
        var modelNode = this.nodeBuilder.buildNode({
          attr : {
            "id" : this.model.uuid,
            "rel" : "erroredModel",
            "elementId" : this.model.id
          },
          data : this.model.id
        }, parent);

        // skip any details for errored models

        return modelNode;
      };

      OutlineUiModelBuilder.prototype.buildTypeDeclarationNode = function(
          typeDeclaration, parent) {
        return this.nodeBuilder.buildNode({
          attr : {
            "id" : typeDeclaration.uuid,
            "rel" : typeDeclaration.getType(),
            "elementId" : typeDeclaration.id,
            "modelId" : this.model.id,
            "oid" : typeDeclaration.oid,
            "draggable" : true,
            "modelUUID" : this.model.uuid,
            "fullId" : typeDeclaration.getFullId()
          },
          data : typeDeclaration.name
        }, parent);
      };

      OutlineUiModelBuilder.prototype.buildDataNode = function(data, parent) {
        if (data[m_constants.EXTERNAL_REFERENCE_PROPERTY]) {
          return undefined;
        }

        var showAsPrimitive = null;
        if (data.structuredDataTypeFullId) {
        	var typeDeclaration = m_model.findModel(m_model
					.stripModelId(data.structuredDataTypeFullId)).typeDeclarations[m_model
					.stripElementId(data.structuredDataTypeFullId)];
        	showAsPrimitive = typeDeclaration ? typeDeclaration.isEnumeration():false;
		}

        return this.nodeBuilder.buildNode({
          attr : {
            "id" : data.uuid,
            "rel" : showAsPrimitive != true ? data.dataType : "primitive",
            "elementId" : data.id,
            "modelId" : this.model.id,
            "oid" : data.oid,
            "draggable" : true,
            "modelUUID" : this.model.uuid,
            "fullId" : data.getFullId()
          },
          data : data.name
        }, parent);
      };

      OutlineUiModelBuilder.prototype.buildApplicationNode = function(application, parent) {
        return this.nodeBuilder.buildNode({
          attr : {
            "id" : application.uuid,
            "rel" : application.applicationType,
            "elementId" : application.id,
            "modelId" : this.model.id,
            "oid" : application.oid,
            "draggable" : true,
            "modelUUID" : this.model.uuid,
            "fullId" : application.getFullId()
          },
          data : application.name
        }, parent);
      };

      OutlineUiModelBuilder.prototype.buildParticipantNode = function(participant, parent) {
        if (participant[m_constants.EXTERNAL_REFERENCE_PROPERTY]) {
          return undefined;
        }

        var nodeConfig = {
          attr : {
            "id" : participant.uuid,
            "rel" : participant.type,
            "elementId" : participant.id,
            "modelId" : this.model.id,
            "oid" : participant.oid,
            "draggable" : true,
            "modelUUID" : this.model.uuid,
            "fullId" : participant.getFullId()
          },
          data : participant.name
        };
        if (participant.parentUUID) {
          nodeConfig.attr.parentUUID = participant.parentUUID;
        }

        var participantNode = this.nodeBuilder.buildNode(nodeConfig, parent);

        if (participant.childParticipants) {
          var self = this;
          jQuery.each(participant.childParticipants, function(index, childParticipant) {
            self.buildParticipantNode(childParticipant, participantNode);
          });
        }

        return participantNode;
      };

      OutlineUiModelBuilder.prototype.buildProcessNode = function(process, parentNode) {
        return this.nodeBuilder.buildNode({
          "attr" : {
            "id" : process.uuid,
            "rel" : "process",
            "elementId" : process.id,
            "modelId" : this.model.id,
            "draggable" : true,
            "oid" : process.oid,
            "modelUUID" : this.model.uuid,
            "fullId" : process.getFullId()
          },
          "data" : process.name
        }, parentNode);
      };

      OutlineUiModelBuilder.prototype.buildContainerNode = function(containerType, parent) {
        return this.nodeBuilder.buildNode({
          attr : {
            "id" : containerType + "_" + this.model.uuid,
            "rel" : containerType,
            "modelUUID" : this.model.uuid
          },
          data : m_i18nUtils.getProperty("modeler.outline." + containerType + ".name")
        }, parent);
      };

      function newOutlineTreeDomBuilder(model) {
        return new OutlineUiModelBuilder(model, new DomTreeBuilder());
      }

      function newJsTreeOutlineBuilder(model) {
        var outlineTree = jQuery.jstree._reference(displayScope + "#outline");

        return new OutlineUiModelBuilder(model, new TreeNodeBuilder(outlineTree));
      }

      function refreshModelStatus(model) {
        var modelNode = m_utils.jQuerySelect("li#" + model.uuid, displayScope + " #outline");
        modelNode.attr("rel", model.isReadonly() ? "lockedModel" : "model");

        if (model.isReadonly() && model.editLock
            && ("lockedByOther" === model.editLock.lockStatus)) {

        	modelNode.attr("rel", model.isReadonly() ? "lockedModelForEdit" : "model");
          modelNode.attr("title", m_i18nUtils
              .getProperty("modeler.outline.model.statusLocked")
              + " " + model.editLock.ownerName);
          modelNode.addClass("show_tooltip");
        } else {
          modelNode.removeClass("show_tooltip");
        }
      }

      var readAllModels = function(force, dontReloadStrategy) {
        jQuery("div#outlineLoadingMsg").show();
        jQuery("div#outlineLoadingMsg").html(
            m_i18nUtils.getProperty("modeler.outline.loading.message"));
        console.time("###################################### Load models");
        m_model.loadModels(force, dontReloadStrategy);
        console.timeEnd("###################################### Load models");

        m_utils.jQuerySelect("#lastsave").text(
            m_i18nUtils.getProperty("modeler.outline.lastSavedMessage.title"));

        console.time("###################################### Tree formation");

        var outline = this;
        var outlineRoot = jQuery(displayScope + "#outline");

        jQuery.each(m_utils.convertToSortedArray(m_model.getModels(), "name", false),
            function(index, model) {
              newOutlineTreeDomBuilder(model).buildModelNode(outlineRoot);
              refreshModelStatus(model);
            });

        // Errored models
        jQuery.each(m_utils.convertToSortedArray(m_model.getErroredModels(), "name",
            false), function(index, model) {
          newOutlineTreeDomBuilder(model).buildErroredModelNode(outlineRoot);
          //refreshModelStatus(model);
        });

        console.timeEnd("###################################### Tree formation");

        jQuery("div#outlineLoadingMsg").hide();
        runHasModelsCheck();

        m_messageDisplay.markSaved();
        m_modelsSaveStatus.setModelsSaved();
        m_utils.jQuerySelect("#undoChange").addClass("toolDisabled");
        m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
      };

			/**
       *
       */
			var runHasModelsCheck = function() {
				var models = m_model.getModels();
				var hasModels = false;
				for (var mod in models) {
					hasModels = true;
					break;
				}

				if (!hasModels) {
					jQuery("div#outlineMessageDiv").show();
					jQuery("div#outlineMessageDiv").html(m_i18nUtils.getProperty("modeler.outline.noModelsFound.message"));
				} else {
					jQuery("div#outlineMessageDiv").hide();
				}
			}

			var deployModel = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);
				var modeleDeployerLink = m_utils.jQuerySelect(
						"a[id $= 'model_deployer_link']",
						m_utils.getOutlineWindowAndDocument().doc);
				var modeleDeployerLinkId = modeleDeployerLink.attr('id');
				var form = modeleDeployerLink.parents('form:first');
				var formId = form.attr('id');

				if (model.fileName && model.filePath) {
					m_jsfViewManagerHelper.openModelDeploymentDialog(
							modeleDeployerLinkId, model.fileName,
							model.filePath, formId);
				} else {
					alert("Cannot deploy: Model file name / path not available");
				}

			};

			var downloadModel = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);

				if (!model) {
					for (var i = 0; i < m_model.getErroredModels().length; i++) {
						if (m_model.getErroredModels()[i].uuid === modelUUID) {
							model = m_model.getErroredModels()[i];
						}
					}
				}
				if (model) {
					window.location = require("bpm-modeler/js/m_urlUtils")
						.getModelerEndpointUrl()
						+ "/models/"
						+ encodeURIComponent(model.id)
						+ "/download";
				}
			}

			var openModelReport = function(modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);

				window.open("../public/reportTest.html?modelId=" + model.id);
			}

			// TODO Is this still needed? Delete after verifying
			var elementCreationHandler = function(id, name, type, parent) {
				if (type == 'activity') {
					var parentSelector = '#' + parent;
					m_utils.jQuerySelect(displayScope + "#outline").jstree("create",
							parentSelector, "last", {
								"attr" : {
									"id" : id,
									"rel" : "manual_activity",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == "subProcessActivity") {
					var parentSelector = '#' + parent;
					m_utils.jQuerySelect(displayScope + "#outline").jstree("create",
							parentSelector, "last", {
								"attr" : {
									"id" : id,
									"rel" : "sub_process_activity"
								},
								"data" : name
							}, null, true);
				} else if (type == 'primitiveDataType') {
					var parentSelector = '#' + parent;
					m_utils.jQuerySelect(displayScope + "#outline").jstree("create",
							parentSelector, "last", {
								"attr" : {
									"id" : id,
									"rel" : "Primitive_Data",
									"draggable" : true
								},
								"data" : name
							}, null, true);
				} else if (type == 'role') {
					var parentSelector = '#' + parent;
					m_utils.jQuerySelect(displayScope + "#outline").jstree("create",
							parentSelector, "last", {
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
					var rLink = m_utils.jQuerySelect("li#" + attrs.id + " a")[0];
					var textElem = m_utils.jQuerySelect(rLink.childNodes[1])[0];
					textElem.nodeValue = attrs.props.completetext;
				}
			};

			var renameNodeHandler = function(event, data) {
				if (data.rslt.obj.attr('rel') == 'model'
					|| data.rslt.obj.attr('rel') == 'lockedModel'
					|| data.rslt.obj.attr('rel') == 'lockedModelForEdit') {
					var model = m_model.findModelByUuid(data.rslt.obj
							.attr("id"));

					if (model && (model.name != data.rslt.name)) {
						m_commandsController.submitCommand(m_command
								.createUpdateModelElementCommand(model.id, model.id, {
									"name" : data.rslt.name
								}));
					}
				} else {
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
				if (type == 'model' || type == 'lockedModel' || type == "lockedModelForEdit" ) {
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
				} else if (type == "structuredDataType"
						|| type == "compositeStructuredDataType"
						|| type == "enumStructuredDataType"
						|| type == "importedStructuredDataType") {
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
									title : m_i18nUtils
											.getProperty("modeler.messages.confirm"),
									message : m_i18nUtils
											.getProperty("modeler.messages.confirm.modelReload"),
									acceptButtonText : m_i18nUtils
											.getProperty("modeler.messages.confirm.yes"),
									cancelButtonText : m_i18nUtils
											.getProperty("modeler.messages.confirm.no"),
									acceptFunction : reloadOutlineTree ,
									checkboxLabelText:	m_i18nUtils
										.getProperty("modeler.messages.confirm.saveModelsPriorToReload")
								}
							});
				}
			}

			var reloadOutlineTree = function(saveFirst) {
				if (true == saveFirst) {
					saveAllModels();
				}

				// Close all modeler-related views This is to
				// avoid having any open view in inconsistent
				// state.
				m_modelerUtils.closeAllModelerViews();

				m_utils.jQuerySelect(displayScope + "#outline").empty();
				readAllModels(true);
				setupJsTree();
				//jQuery(displayScope + "#outline").jstree("create");
			};

			/**
			 * This function will not reload the models in model management strategy
			 */
			var loadOutlineTree = function() {
				// Close all modeler-related views This is to
				// avoid having any open view in inconsistent
				// state.
				m_modelerUtils.closeAllModelerViews();

				m_utils.jQuerySelect(displayScope + "#outline").empty();
				readAllModels(true, true);
				setupJsTree();
				// jQuery(displayScope + "#outline").jstree("create");
			};
			
			var importModel = function() {
				if (false == m_modelsSaveStatus.areModelsSaved()) {
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
										title : m_i18nUtils
												.getProperty("modeler.messages.warning"),
										message : m_i18nUtils
												.getProperty("modeler.messages.info.modelNotSaved"),
										acceptButtonText : m_i18nUtils
												.getProperty("modeler.messages.confirm.close"),
										acceptFunction : function() {
											// Do nothing
										}
									}
								});
					} else {
						alert("Models have unsaved changes. Please save models before continuing.");
					}
				} else {
					var link = m_utils.jQuerySelect(
							"a[id $= 'open_model_upload_dialog_link']",
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
							m_utils.jQuerySelect("#undoChange").removeClass("toolDisabled");
						} else {
							m_utils.jQuerySelect("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							m_utils.jQuerySelect("#redoChange").removeClass("toolDisabled");
						} else {
							m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
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
							m_utils.jQuerySelect("#undoChange").removeClass("toolDisabled");
						} else {
							m_utils.jQuerySelect("#undoChange").addClass("toolDisabled");
						}

						if (null != data.pendingRedo) {
							m_utils.jQuerySelect("#redoChange").removeClass("toolDisabled");
						} else {
							m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
						}
					}
				});
			}

			function saveAllModels() {
				m_communicationController
						.syncGetData(
								{
									url : require("bpm-modeler/js/m_urlUtils")
											.getModelerEndpointUrl()
											+ "/models/save"
								},
								new function() {
									return {
										success : function(data) {
											m_messageDisplay.markSaved();
											m_modelsSaveStatus.setModelsSaved();
											m_utils.jQuerySelect("#undoChange").addClass("toolDisabled");
											m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
											
											window.parent.EventHub.events.publish("CONTEXT_UPDATED");
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

			var setupJsTree = function() {
				jQuery(displayScope + "#outline").jstree(
						{
							core : {
								animation : 0
							},
							"plugins" : [ "themes", "html_data",
									"crrm", "contextmenu", "types",
									"ui" ],
							contextmenu : {
								"items" : function(node) {
									if ('model' == node.attr('rel')
											|| 'lockedModel' == node.attr('rel')
											|| 'lockedModelForEdit' == node.attr('rel')) {
										var ctxMenu =  {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteModel" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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
												"label" : m_i18nUtils
														.getProperty("modeler.outline.model.contextMenu.createProcess"),
												"action" : function(obj) {
													createProcess(obj
															.attr("elementId"));
												}
											},
											"deploy" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.model.contextMenu.deploy"),
												"action" : function(obj) {
													deployModel(obj
															.attr("id"));
												}
											},
											"download" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.model.contextMenu.download"),
												"action" : function(obj) {
													downloadModel(obj
															.attr("id"));
												}
											}

										// openModelReport options is
										// Commented out as,
										// this will not be supported in
										// 7.1.1
										// Uncomment when needed.
										// ,
										// "openModelReport" : {
										// "label" : m_i18nUtils
										// .getProperty("modeler.outline.model.contextMenu.openModelReport"),
										// "action" : function(obj) {
										// openModelReport(obj
										// .attr("id"));
										// }
										// }
										};

										addMenuOptions(ctxMenu, "model");

										var mod = m_model.findModelByUuid(node.attr('id'))
										if (mod.isReadonly()) {
											ctxMenu.rename = false;
											ctxMenu.deleteModel = false;
											ctxMenu.deleteModel = false;
											ctxMenu.createProcess = false;
										}

										return ctxMenu;
									} else if ('erroredModel' == node.attr('rel')) {
										var ctxMenu =  {
											"ccp" : false,
											"create" : false,
											"rename" : false,
											"deleteModel" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
												"action" : function(obj) {
													deleteElementAction(
															obj.context.lastChild.data,
															function() {
																deleteModel(obj
																		.attr("elementId"));
															});
												}
											},
											"download" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.model.contextMenu.download"),
												"action" : function(obj) {
													downloadModel(obj
															.attr("id"));
												}
											}
										}

										return ctxMenu;
									} else if ('process' == node
											.attr('rel')) {
										var options = {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
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

										addMenuOptions(options,
												"process");

										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										return elem.isReadonly() ? {} : options;
									} else if ('applications' == node
											.attr('rel')) {
										var options = {
											"ccp" : false,
											"create" : false,
											"rename" : false,
											// Options to create
											// webservice and UI mashup
											// applications to be
											// disabled
											// as they are not fully
											// supported in 7.1.1
											"createWebServiceApplication" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.applications.contextMenu.createWebService"),
												"action" : function(obj) {
													createWebServiceApplication(obj
															.attr("modelUUID"));
												}
											},
											"createMessageTransformationApplication" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.applications.contextMenu.createTransformation"),
												"action" : function(obj) {
													createMessageTransformationApplication(obj
															.attr("modelUUID"));
												}
											},
											"createUiMashupApplication" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.applications.contextMenu.createUIMashup"),
												"action" : function(obj) {
													createUiMashupApplication(obj
															.attr("modelUUID"));
												}
											}
										};

										addCamelOverlayMenuOptions(options);

										var mod = m_model.findModelByUuid(node.attr('modeluuid'));
										return mod.isReadonly() ? {} : options;
									} else if ('data' == node
											.attr('rel')) {
										var mod = m_model.findModelByUuid(node.attr('modeluuid'));
										var ctxMenu = mod.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : false,
											"createPrimitiveData" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.data.contextMenu.createPrimitiveData"),
												"action" : function(obj) {
													createPrimitiveData(obj
															.attr("modelUUID"));
												}
											},
											"createDocumentData" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.data.contextMenu.createDocument"),
												"action" : function(obj) {
													createDocumentData(obj
															.attr("modelUUID"));
												}
											},
											"createStructuredData" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.data.contextMenu.createStructuredData"),
												"action" : function(obj) {
													createStructuredData(obj
															.attr("modelUUID"));
												}
											}
										};

										return ctxMenu
									} else if (m_elementConfiguration
											.isValidDataType(node
													.attr("rel"))) {
										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										var ctxMenu = elem.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteData" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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

										return ctxMenu;
									} else if ("participants" == node
											.attr('rel')) {
										var mod = m_model.findModelByUuid(node.attr('modeluuid'));
										var ctxMenu = mod.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : false,
											"createRole" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.contextMenu.createRole"),
												"action" : function(obj) {
													createRole(obj
															.attr("modelUUID"));
												}
											},
											"createOrganization" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.contextMenu.createOrganization"),
												"action" : function(obj) {
													createOrganization(obj
															.attr("modelUUID"));
												}
											},
											"createConditionalPerformer" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.contextMenu.createConditionalPerformer"),
												"action" : function(obj) {
													createConditionalPerformer(obj
															.attr("modelUUID"));
												}
											}
										};

										return ctxMenu;
									} else if (m_elementConfiguration
											.isValidAppType(node
													.attr("rel"))) {
										var options = {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteApplication" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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

										addMenuOptions(options,
												"application");

										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										return elem.isReadonly() ? {} : options;
									} else if ('structuredTypes' == node
											.attr('rel')) {
										var mod = m_model.findModelByUuid(node.attr('modeluuid'));
										var ctxMenu = mod.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : false,
											"createXSDStructuredDataType" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.structureDataType.contextMenu.createDataType"),
												"action" : function(obj) {
													createXsdStructuredDataType(obj
															.attr("modelUUID"));
												}
											},
											importTypeDeclarations : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.structureDataType.contextMenu.importTypeDeclarations"),
												"action" : function(obj) {
													var model = m_model
															.findModelByUuid(obj
																	.attr("modelUUID"));

													importTypeDeclarations(model);
												}
											}
										};

										return ctxMenu;
									} else if ("structuredDataType" == node
											.attr('rel')
											|| "compositeStructuredDataType" == node
													.attr('rel')
											|| "enumStructuredDataType" == node
													.attr('rel')
											|| "importedStructuredDataType" == node
													.attr('rel')) {
										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										var ctxMenu = elem.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteStructuredDataType" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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

										return ctxMenu;
									} else if ('roleParticipant' == node
											.attr('rel')
											|| 'teamLeader' == node
													.attr('rel')) {
										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										var ctxMenu = elem.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteParticipant" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.role.contextMenu.setAsManager"),
												"_disabled" : ((undefined == node
														.attr("parentUUID")) || ('teamLeader' == node
														.attr('rel'))),
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

										return ctxMenu;
									} else if ("conditionalPerformerParticipant" == node
											.attr('rel')) {
										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										var ctxMenu = elem.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteParticipant" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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

										return ctxMenu;
									} else if ('organizationParticipant' == node
											.attr('rel')) {
										var elem = m_model.findElementInModelByUuid(node.attr('modelid'), node.attr('id'));
										var ctxMenu = elem.isReadonly() ? {} : {
											"ccp" : false,
											"create" : false,
											"rename" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.contextMenu.rename"),
												"action" : function(obj) {
													m_utils.jQuerySelect(
															displayScope
																	+ "#outline")
															.jstree(
																	"rename",
																	"#"
																			+ obj
																					.attr("id"));
												}
											},
											"deleteParticipant" : {
												"label" : m_i18nUtils
														.getProperty("modeler.element.properties.commonProperties.delete"),
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
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.contextMenu.createRole"),
												"action" : function(obj) {
													createRole(
															obj
																	.attr("modelUUID"),
															obj
																	.attr("id"));
												}
											},
											"createOrganization" : {
												"label" : m_i18nUtils
														.getProperty("modeler.outline.participants.contextMenu.createOrganization"),
												"action" : function(obj) {
													createOrganization(
															obj
																	.attr("modelUUID"),
															obj
																	.attr("id"));
												}
											}
										};

										return ctxMenu;
									}

									return {};
								}
							},
							types : {
								"types" : {
									"model" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/model.png"
										},
										"valid_children" : [
												"participants",
												"process",
												"applications",
												"structuredTypes",
												"data" ]
									},
									"lockedModel" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/model-locked.png"
										},
										"valid_children" : [
												"participants",
												"process",
												"applications",
												"structuredTypes",
												"data" ]
									},
									"lockedModelForEdit" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/model-locked-for-edit.png"
										},
										"valid_children" : [
												"participants",
												"process",
												"applications",
												"structuredTypes",
												"data" ]
									},
									"erroredModel" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/model-error.png"
										},
										"valid_children" : []
									},
									"participants" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/participants.png"
										}
									},
									"roleParticipant" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/role.png"
										}
									},
									"teamLeader" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/manager.png"
										}
									},
									"organizationParticipant" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/organization.png"
										}
									},
									"conditionalPerformerParticipant" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/conditional.png"
										}
									},
									"process" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/process.png"
										}
									},
									"structuredTypes" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/structured-types.png"
										}
									},
									"structuredDataType" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/structured-type.png"
										}
									},
									"compositeStructuredDataType" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/bricks.png"
										}
									},
									"enumStructuredDataType" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/structured-type-enum.png"
										}
									},
									"importedStructuredDataType" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/structured-type-import.png"
										}
									},
									"applications" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/applications-blue.png"
										}
									},
									"interactive" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-c-ext-web.png"
										}
									},
									"plainJava" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-plain-java.png"
										}
									},
									"jms" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-jms.png"
										}
									},
									"webservice" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-web-service.png"
										}
									},
									"dmsOperation" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-dms.png"
										}
									},
									"mailBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-mail.png"
										}
									},
									"messageParsingBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-message-p.png"
										}
									},
									"messageSerializationBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-message-s.png"
										}
									},
									"messageTransformationBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-message-trans.png"
										}
									},
									"camelSpringProducerApplication" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-camel.png"
										}
									},
									"camelConsumerApplication" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-camel.png"
										}
									},
									"rulesEngineBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-drools.png"
										}
									},
									"sessionBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-session.png"
										}
									},
									"springBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-plain-java.png"
										}
									},
									"xslMessageTransformationBean" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/application-message-trans.png"
										}
									},
									"data" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data.png"
										}
									},
									"primitive" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-primitive.png"
										}
									},
									"hibernate" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-hibernate.png"
										}
									},
									"struct" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-structured.png"
										}
									},
									"serializable" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-serializable.png"
										}
									},
									"entity" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-entity.png"
										}
									},
									"dmsDocument" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-document.png"
										}
									},
									"dmsDocumentList" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-document-list.png"
										}
									},
									"dmsFolder" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-folder.png"
										}
									},
									"dmsFolderList" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-folder-list.png"
										}
									},
									"plainXML" : {
										"icon" : {
											"image" : m_urlUtils
													.getPlugsInRoot()
													+ "bpm-modeler/images/icons/data-xml.png"
										}
									}
								}
							},
							"themes" : {
								"theme" : "custom",
								"url" : "../css/jsTreeCustom/style.css"
							}
						});

				// Rename node handler
				jQuery(displayScope + "#outline").bind("rename_node.jstree", function(event, data) {
					renameNodeHandler(event, data);
				});

				// Tree Node Selection handler
				m_utils.jQuerySelect(displayScope + "#outline")
						.bind(
								"select_node.jstree",
								function(event, data) {
									if (data.rslt.obj.attr('rel') == 'model'
											|| data.rslt.obj.attr('rel') == 'lockedModel'
											|| data.rslt.obj.attr('rel') == 'lockedModelForEdit') {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"modelView",
														"modelId="
																+ encodeURIComponent(model.id)
																+ "&modelName="
																+ encodeURIComponent(model.name)
																+ "&uuid="
																+ model.uuid,
														model.uuid);
									} else if (data.rslt.obj.attr('rel') == 'erroredModel') {
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
															title : m_i18nUtils
																	.getProperty("modeler.messages.warning"),
															message : m_i18nUtils
																	.getProperty("modeler.outline.erroredModels.open.message"),
															acceptButtonText : m_i18nUtils
																	.getProperty("modeler.messages.confirm.close"),
															acceptFunction : function() {
																// Do nothing
															}
														}
													});
										} else {
											alert(m_i18nUtils.getProperty("modeler.outline.erroredModels.open.message"));
										}
									} else if (data.rslt.obj.attr('rel') == "roleParticipant"
											|| data.rslt.obj.attr('rel') == "teamLeader") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var role = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"roleView",
														"roleId="
																+ encodeURIComponent(role.id)
																+ "&modelId="
																+ encodeURIComponent(model.id)
																+ "&roleName="
																+ encodeURIComponent(role.name)
																+ "&fullId="
																+ encodeURIComponent(role
																		.getFullId())
																+ "&uuid="
																+ role.uuid
																+ "&modelUUID="
																+ model.uuid,
														role.uuid);
									} else if (data.rslt.obj.attr('rel') == 'organizationParticipant') {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var organization = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"organizationView",
														"organizationId="
																+ encodeURIComponent(organization.id)
																+ "&modelId="
																+ encodeURIComponent(model.id)
																+ "&organizationName="
																+ encodeURIComponent(organization.name)
																+ "&fullId="
																+ encodeURIComponent(organization
																		.getFullId())
																+ "&uuid="
																+ organization.uuid
																+ "&modelUUID="
																+ model.uuid,
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

										viewManager
												.openView(
														"dataView",
														"dataId="
																+ encodeURIComponent(data.id)
																+ "&modelId="
																+ encodeURIComponent(model.id)
																+ "&dataName="
																+ encodeURIComponent(data.name)
																+ "&fullId="
																+ encodeURIComponent(data
																		.getFullId())
																+ "&uuid="
																+ data.uuid
																+ "&modelUUID="
																+ model.uuid,
														data.uuid);
									} else if (data.rslt.obj.attr('rel') == 'process') {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var process = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager
												.openView(
														"processDefinitionView",
														"processId="
																+ encodeURIComponent(process.id)
																+ "&modelId="
																+ encodeURIComponent(model.id)
																+ "&processName="
																+ encodeURIComponent(process.name)
																+ "&fullId="
																+ encodeURIComponent(process
																		.getFullId())
																+ "&uuid="
																+ process.uuid
																+ "&modelUUID="
																+ model.uuid,
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
														+ encodeURIComponent(model.id)
														+ "&applicationId="
														+ encodeURIComponent(application.id)
														+ "&applicationName="
														+ encodeURIComponent(application.name)
														+ "&fullId="
														+ encodeURIComponent(application
																.getFullId())
														+ "&uuid="
														+ application.uuid
														+ "&modelUUID="
														+ model.uuid,
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
																+ encodeURIComponent(model.id)
																+ "&applicationId="
																+ encodeURIComponent(application.id)
																+ "&applicationName="
																+ encodeURIComponent(application.name)
																+ "&fullId="
																+ encodeURIComponent(application
																		.getFullId())
																+ "&uuid="
																+ application.uuid
																+ "&modelUUID="
																+ model.uuid,
														application.uuid);
									} else if (data.rslt.obj.attr('rel') == "camelSpringProducerApplication" ||
											data.rslt.obj.attr('rel') == "camelConsumerApplication") {
										var model = m_model
												.findModelByUuid(data.rslt.obj
														.attr("modelUUID"));
										var application = model
												.findModelElementByUuid(data.rslt.obj
														.attr("id"));

										viewManager.openView(
												"camelApplicationView",
												"modelId="
														+ encodeURIComponent(model.id)
														+ "&applicationId="
														+ encodeURIComponent(application.id)
														+ "&applicationName="
														+ encodeURIComponent(application.name)
														+ "&fullId="
														+ encodeURIComponent(application
																.getFullId())
														+ "&uuid="
														+ application.uuid
														+ "&modelUUID="
														+ model.uuid,
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
														+ encodeURIComponent(model.id)
														+ "&applicationId="
														+ encodeURIComponent(application.id)
														+ "&applicationName="
														+ encodeURIComponent(application.name)
														+ "&fullId="
														+ encodeURIComponent(application
																.getFullId())
														+ "&uuid="
														+ application.uuid
														+ "&modelUUID="
														+ model.uuid,
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
														+ encodeURIComponent(model.id)
														+ "&applicationId="
														+ encodeURIComponent(application.id)
														+ "&applicationName="
														+ encodeURIComponent(application.name)
														+ "&fullId="
														+ encodeURIComponent(application
																.getFullId())
														+ "&uuid="
														+ application.uuid
														+ "&modelUUID="
														+ model.uuid,
												application.uuid);
									} else if (data.rslt.obj.attr('rel') == "structuredDataType"
											|| data.rslt.obj.attr('rel') == "compositeStructuredDataType"
											|| data.rslt.obj.attr('rel') == "enumStructuredDataType"
											|| data.rslt.obj.attr('rel') == "importedStructuredDataType") {
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
																+ encodeURIComponent(model.id)
																+ "&structuredDataTypeId="
																+ encodeURIComponent(structuredDataType.id)
																+ "&structuredDataTypeName="
																+ encodeURIComponent(structuredDataType.name)
																+ "&fullId="
																+ encodeURIComponent(structuredDataType
																		.getFullId())
																+ "&uuid="
																+ structuredDataType.uuid
																+ "&modelUUID="
																+ model.uuid,
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
																+ encodeURIComponent(model.id)
																+ "&conditionalPerformerId="
																+ encodeURIComponent(conditionalPerformer.id)
																+ "&conditionalPerformerName="
																+ encodeURIComponent(conditionalPerformer.name)
																+ "&fullId="
																+ encodeURIComponent(conditionalPerformer
																		.getFullId())
																+ "&uuid="
																+ conditionalPerformer.uuid
																+ "&modelUUID="
																+ model.uuid,
														conditionalPerformer.uuid);
									}

									else {
										m_utils.debug("No View defined for "
												+ data.rslt.obj.attr('rel'));
									}

									m_utils.jQuerySelect("a")
											.mousedown(
													function(e) {

														if (m_utils.jQuerySelect(this)
																.parent()
																.attr(
																		'draggable')) {
															if (e.preventDefault) {
																e
																		.preventDefault();
															}
															var insElem = this.childNodes[0];
															var textElem = m_utils.jQuerySelect(this.childNodes[1])[0];
															var bgImage = m_utils.jQuerySelect(
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
															var model = m_model
																	.findModelByUuid(m_utils.jQuerySelect(
																			insElem)
																			.parent()
																			.parent()
																			.attr(
																					"modelUUID"));
															var element = model
																	.findModelElementByUuid(m_utils.jQuerySelect(
																			insElem)
																			.parent()
																			.parent()
																			.attr(
																					"id"));
															parent.iDnD
																	.setDrag();
															parent.iDnD.dragMode = true;
															var transferObj = {
																'elementType' : m_utils.jQuerySelect(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				'rel'),
																'elementId' : m_utils.jQuerySelect(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				"elementId"),
																'type' : element.type,
																'uuid' : element.uuid,
																'modelUUID' : model.uuid,
																'attr' : {}
															};

															if (transferObj.elementType == "Plain_Java_Application") {
																transferObj.attr.accessPoint = m_utils.jQuerySelect(
																		insElem)
																		.parent()
																		.parent()
																		.attr(
																				'accessPoint');
															}

															transferObj.attr.fullId = m_utils.jQuerySelect(
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
								});
			};

			/**
			 *
			 */
			function createModel() {
				var modelName = m_i18nUtils
						.getProperty("modeler.outline.newModel.namePrefix");
				var count = 0;
				var name = modelName + " " + (++count);

				// Check if model name exists already.
				while (modelNameExists(name)) {
					name = modelName + " " + (++count);
				}

				m_commandsController.submitCommand(m_command
						.createCreateModelCommand({
							"name" : name,
							"modelFormat" : "xpdl"
						}));
				isElementCreatedViaOutline = true;
			};

			function modelNameExists(name) {
				for (m in m_model.getModels()) {
					if (m_model.getModels()[m].name == name) {
						return true;
					}
				}

				return false;
			};

			/**
			 *
			 */
			function deleteModel(modelId) {
				var model = m_model.findModel(modelId);
				if (!model) {
					for (var i = 0; i < m_model.getErroredModels().length; i++) {
						if (m_model.getErroredModels()[i].id === modelId) {
							model = m_model.getErroredModels()[i];
						}
					}
				}

				if (model) {
					m_commandsController.submitCommand(m_command
							.createDeleteModelCommand(model.uuid, model.id, {})).done(function() {
								window.parent.EventHub.events.publish("CONTEXT_UPDATED");								
							});
				}
			};

			/**
			 *
			 */
			function createProcess(modelId) {
				var procNamePrefix = m_i18nUtils
						.getProperty("modeler.outline.newProcess.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(modelId, procNamePrefix);

				// TODO
				// Temporarily added I18n FOR default pool and lane names on
				// client side,
				// till this is handled on server side.
				m_commandsController
						.submitCommand(m_command
								.createCreateProcessCommand(
										modelId,
										modelId,
										{
											"name" : name,
											"defaultPoolName" : m_i18nUtils
													.getProperty("modeler.diagram.defaultPoolName"),
											"defaultLaneName" : m_i18nUtils
													.getProperty("modeler.diagram.defaultLaneName")
										}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function deleteProcess(processId, modelUUID) {
				var model = m_model.findModelByUuid(modelUUID);
				m_commandsController.submitCommand(m_command
						.createDeleteProcessCommand(model.id, model.id, {
							"id" : processId
						}));
			};

			/**
			 */
			function deleteStructuredDataType(modelUUID, structTypeId) {
				var model = m_model.findModelByUuid(modelUUID);
				m_commandsController.submitCommand(m_command
						.createDeleteStructuredDataTypeCommand(model.id,
								model.id, {
									"id" : structTypeId
								}));
			};

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
			};

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
			};

			/**
			 *
			 */
			function deleteData(modelUUID, id) {
				var model = m_model.findModelByUuid(modelUUID);
				m_commandsController.submitCommand(m_command
						.createDeleteDataCommand(model.id, model.id, {
							"id" : id
						}));
			};

			function prepareDeleteElementData(name, callback) {
				var popupData = {
					attributes : {
						width : "400px",
						height : "200px",
						src : m_urlUtils.getPlugsInRoot()
								+ "bpm-modeler/popups/confirmationPopupDialogContent.html"
					},
					payload : {
						title : m_i18nUtils
								.getProperty("modeler.messages.confirm"),
						message : m_i18nUtils.getProperty(
								"modeler.messages.confirm.deleteElement")
								.replace("{0}", name),
						acceptButtonText : m_i18nUtils
								.getProperty("modeler.messages.confirm.yes"),
						cancelButtonText : m_i18nUtils
								.getProperty("modeler.messages.confirm.cancel"),
						acceptFunction : callback
					}
				};

				return popupData;
			};

			function deleteElementAction(name, callback) {
				if (parent.iPopupDialog) {
					parent.iPopupDialog.openPopup(prepareDeleteElementData(
							name, callback));
				} else {
					callback();
				}
			};

			/**
			 *
			 */
			function createPrimitiveData(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newPrimitivedata.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController
						.submitCommand(m_command
								.createCreatePrimitiveDataCommand(
										model.id,
										model.id,
										{
											"name" : name,
											"primitiveType" : m_constants.STRING_PRIMITIVE_DATA_TYPE
										}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createDocumentData(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newDocumentdata.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateDocumentDataCommand(model.id,
								model.id, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createStructuredData(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newStructureddata.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateStructuredDataCommand(model.id,
								model.id, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createRole(modelUUId, targetUUID) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newRole.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);
				var targetOid = (targetUUID ? m_model
						.findElementInModelByUuid(model.id, targetUUID).oid
						: model.id);

				m_commandsController.submitCommand(m_command
						.createCreateRoleCommand(model.id, targetOid, {
							"name" : name
						}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createConditionalPerformer(modelUUId, targetUUID) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newConditionalperformer.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);
				var targetOid = (targetUUID ? m_model
						.findElementInModelByUuid(model.id, targetUUID).oid
						: model.id);

				m_commandsController.submitCommand(m_command
						.createCreateConditionalPerformerCommand(model.id,
								targetOid, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

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
			};

			/**
			 *
			 */
			function createOrganization(modelUUId, targetUUID) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newOrganization.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);
				var targetOid = (targetUUID ? m_model
						.findElementInModelByUuid(model.id, targetUUID).oid
						: model.id);

				m_commandsController.submitCommand(m_command
						.createCreateOrganizationCommand(model.id,
								targetOid, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createWebServiceApplication(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newWebservice.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateWebServiceAppCommand(model.id,
								model.id, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createMessageTransformationApplication(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newMsgTransformation.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateMessageTransfromationAppCommand(
								model.id, model.id, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createCamelApplication(modelUUId, name, extIdKey, attributes) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils.getProperty(extIdKey);

				//if (!name) {
				var genName = m_modelerUtils.getUniqueNameForElement(
							model.id, titledata);
				//}

				m_commandsController.submitCommand(m_command
						.createCreateCamelAppCommand(model.id, model.id, {
							name : genName,
							attributes : attributes
						}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 */
			function createUiMashupApplication(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newUimashup.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateUiMashupAppCommand(model.id, model.id,
								{
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 * @param modelId
			 * @returns
			 */
			function createXsdStructuredDataType(modelUUId) {
				var model = m_model.findModelByUuid(modelUUId);
				var titledata = m_i18nUtils
						.getProperty("modeler.outline.newXsddatastructure.namePrefix");
				var name = m_modelerUtils.getUniqueNameForElement(model.id, titledata);

				m_commandsController.submitCommand(m_command
						.createCreateStructuredDataTypeCommand(model.id,
								model.id, {
									"name" : name
								}));
				isElementCreatedViaOutline = true;
			};

			/**
			 *
			 * @param modelId
			 * @param id
			 * @returns
			 */
			function createWrapperProcess(application) {
				m_utils.debug("Application to wrap");
				m_utils.debug(application);

				var popupData = {
					attributes : {
						width : "700px",
						height : "500px",
						src : m_urlUtils.getPlugsInRoot()
								+ "bpm-modeler/views/modeler/serviceWrapperWizard.html"
					},
					payload : {
						callerWindow : window,
						application : application,
						viewManager : viewManager,
						createCallback : function(parameter) {
						jQuery
									.ajax({
										type : "POST",
										url : m_urlUtils
												.getModelerEndpointUrl()
												+ "/models/"
												+ encodeURIComponent(application.model.id)
												+ "/processes/createWrapperProcess",
										contentType : "application/json",
										data : JSON.stringify(parameter)
									});

							m_commandsController.submitCommand(m_command
									.createCreateWrapperServiceCommand(model.id,
											model.id, {
											data : JSON.stringify(parameter)
											}));
						}
					}
				};

				parent.iPopupDialog.openPopup(popupData);
			};

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
						src : m_urlUtils.getPlugsInRoot()
								+ "bpm-modeler/views/modeler/importTypeDeclarationsWizard.html"
					},
					payload : {
						model : model
					}
				};

				parent.iPopupDialog.openPopup(popupData);
			};


			/**
			 *
			 */
			function addMenuOptions(options, nodeType) {
				var menuOptionExtensions = m_extensionManager
						.findExtensions("menuOption", "nodeType", nodeType);

				m_utils.debug("Menu Options");
				m_utils.debug(menuOptionExtensions);

				for ( var m = 0; m < menuOptionExtensions.length; ++m) {
					var menuOptionExtension = menuOptionExtensions[m];

					if (!m_session.initialize().technologyPreview
							&& menuOptionExtension.visibility == "preview") {
						continue;
					}

					options[menuOptionExtension.id] = {
						label : menuOptionExtension.label,
						action : function(node) {
							menuOptionExtension.provider[menuOptionExtension.handlerMethod]
									(node);
						}
					};
				}
			}

			/**
			 *
			 */
			function addCamelOverlayMenuOptions(options, nodeType) {
				var applicationIntegrationOverlayExtensions = m_extensionManager
						.findExtensions("applicationIntegrationOverlay"),
				    createTxt=m_i18nUtils.getProperty("modeler.element.properties.commonProperties.create");

				for ( var m = 0; m < applicationIntegrationOverlayExtensions.length; ++m) {
					var applicationIntegrationOverlayExtension = applicationIntegrationOverlayExtensions[m];

					if (!m_session.initialize().technologyPreview
							&& applicationIntegrationOverlayExtension.visibility == "preview") {
						continue;
					}

					options[applicationIntegrationOverlayExtension.id] = {
						label : createTxt + " " +
								m_i18nUtils.getProperty("modeler.integrationoverlays.application." +
								applicationIntegrationOverlayExtension.id),

						// This code requires the following patch in
						// jquery.jstree
						// if($.isFunction($.vakata.context.func[i])) {
						// // Patched to add the function name as a
						// parameter
						// $.vakata.context.func[i].call($.vakata.context.data,
						// $.vakata.context.par, i);
						// return true;
						// }

						action : function(node, id) {
							var applicationIntegrationOverlayExtensions = m_extensionManager
									.findExtensions(
											"applicationIntegrationOverlay",
											"id", id);

							createCamelApplication(
									node.attr("modelUUID"),
									applicationIntegrationOverlayExtensions[0].name,
									m_i18nUtils.getProperty("modeler.integrationoverlays.application." + 
		                              applicationIntegrationOverlayExtensions[0].id),
									{
										"carnot:engine:camel::applicationIntegrationOverlay" : id
									});
						}
					};
				}
			};

			var setupEventHandling = function() {
				/* Listen to toolbar events */
				m_utils.jQuerySelect(document).bind('TOOL_CLICKED_EVENT',
						function(event, data) {
							handleToolbarEvents(event, data);
						});

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

				var outlineDiv = document.getElementById("outlineDiv");
				outlineDiv.onmouseup = function() {
					// TODO Make portable/modularize

					if (parent != null && parent.iDnD != null) {
						parent.iDnD.dragMode = false;
						parent.iDnD.hideIframe();
					}
				};

				readAllModels();

				setupJsTree();

				var handleToolbarEvents = function(event, data) {
					if ("createModel" == data.id) {
						createModel();
					} else if ("importModel" == data.id) {
						importModel();
					} else if ("undoChange" == data.id) {
						undoMostCurrent();
					} else if ("redoChange" == data.id) {
						redoLastUndo();
					} else if ("saveAllModels" == data.id) {
						saveAllModels();
					} else if ("refreshModels" == data.id) {
						refresh();
					}
				};

				function Callback(id, name) {
					this.id = id;
					this.name = name;
					this.action = function(node) {
						m_utils.debug("Invoke holder with " + this.id + " "
								+ this.name);
					}

				}

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
							title : m_i18nUtils
									.getProperty("modeler.messages.error"),
							message : msg,
							okButtonText : okText
						}
					}
				}

				function changeProfileHandler(profile) {
					m_session.getInstance().currentProfile = profile;
					m_commandsController.broadcastCommand(m_command
							.createUserProfileChangeCommand(profile));
				}

				if (window.parent.EventHub != null) {
					window.parent.EventHub.events.subscribe("CHANGE_PROFILE",
							changeProfileHandler);
					window.parent.EventHub.events.subscribe("RELOAD_MODELS",
							reloadOutlineTree);

					window.parent.EventHub.events.subscribe("CONTEXT_UPDATED", function(releaseId) {
						if (releaseId != undefined) {
							reloadOutlineTree();
						}
					});
					
					//do not refresh the strategy
					window.parent.EventHub.events.subscribe("LOAD_MODELS", function(releaseId) {
							loadOutlineTree();
					});
				}

				//jQuery(displayScope + "#outline").jstree("create");
//				m_utils.jQuerySelect(displayScope + "#outline").jstree(
//						"close_node", "#" + model.uuid);
			};

			var i18nStaticLabels = function() {
				m_utils.jQuerySelect("#createModel")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.createModel"));
				m_utils.jQuerySelect("#importModel")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.importModel"));
				m_utils.jQuerySelect("#undoChange")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.undo"));
				m_utils.jQuerySelect("#redoChange")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.redo"));
				m_utils.jQuerySelect("#saveAllModels")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.saveAllModel"));
				m_utils.jQuerySelect("#refreshModels")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.outline.toolbar.tooltip.refreshModels"));

			};

			var outline;

			return {
				init : function(newViewManager, newDisplayScope) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					if (newDisplayScope) {
						displayScope = "#" + newDisplayScope + " ";
					}

					if (newViewManager != null) {
						viewManager = newViewManager;
					} else {
						viewManager = m_jsfViewManager.create();
					}

					setupEventHandling();

					m_modelerUtils.fixDivTop(jQuery(".outlineFixedDiv"), jQuery('.outlineScrollableDiv'));

					outline = new Outline();

					outline.initialize();

					m_messageDisplay.updateLastSavedLabel(m_i18nUtils.getProperty("modeler.outline.unSavedMessage.title"));

					m_outlineToolbarController.init("outlineToolbar");
					i18nStaticLabels();
					m_utils.jQuerySelect("#outlineDiv").css("visibility", "visible");

					m_utils.hideWaitCursor();

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
					return "Lightdust.Outline";
				};

				/**
				 *
				 */
				Outline.prototype.initialize = function() {
					m_session.initialize();

					// Register with Event Bus
					m_commandsController.registerCommandHandler(this);
				};

				/**
				 *
				 */
				Outline.prototype.openElementView = function(element, openView) {
					if (isElementCreatedViaOutline || openView) {
						m_utils.jQuerySelect(displayScope + "#outline").jstree("select_node",
								"#" + element.uuid);
						m_utils.jQuerySelect(displayScope + "#outline")
								.jstree("deselect_all");
						// Delay of 1000ms is added to avoid issues of node
						// getting out or rename mode if the view takes
						// a little longer to open - observed specifically on
						// first node creation after login,
						if (!openView) {
							window.setTimeout(function() {
								m_utils.jQuerySelect(displayScope + "#outline").jstree(
										"rename", "#" + element.uuid)
							}, 1000);
						}
					}
					isElementCreatedViaOutline = false;
				}

				/**
				 *
				 */
				Outline.prototype.fireCloseViewCommand = function(uuid) {
					viewManager.closeViewsForElement(uuid);
				}

				/**
				 *
				 */
				Outline.prototype.processCommand = function(command) {
					m_utils.debug("===> Outline Process Event");
					var modelTreeType="model";
					command = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != command && null != command.changes) {
						for ( var i = 0; i < command.changes.added.length; i++) {
							// Create Process
							if (m_constants.PROCESS == command.changes.added[i].type) {
								this
										.openElementView(
												this
														.createProcess(command.changes.added[i]),
												(command.isRedo || command.isUndo));
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
						for ( var i = 0; i < command.changes.modified.length; i++) {
							if (m_constants.MODEL == command.changes.modified[i].type) {
								var modelElement = m_model
										.findModelByUuid(command.changes.modified[i].uuid);
							} else {
								var modelElement = m_model
										.findElementInModelByUuid(
												command.changes.modified[i].modelId,
												command.changes.modified[i].uuid);
							}
							m_utils.debug("Models:");
							m_utils.debug(m_model.getModels());
							m_utils.debug("Model Element:");
							m_utils.debug(modelElement);

							if (modelElement != null) {
								modelElement.rename(command.changes.modified[i].id,
										command.changes.modified[i].name);
								var uuid = modelElement.uuid;
								var link = m_utils.jQuerySelect("li#" + uuid + " a")[0];
								var node = m_utils.jQuerySelect("li#" + uuid);

								node.attr("elementId", modelElement.id);
								node.attr("fullId", modelElement.getFullId());
								node.attr("name", modelElement.name);

								var textElem = m_utils.jQuerySelect(link.childNodes[1])[0];

								textElem.nodeValue = modelElement.name;
								m_utils.inheritFields(modelElement,
										command.changes.modified[i]);
								if (m_constants.ROLE_PARTICIPANT_TYPE == command.changes.modified[i].type
										|| m_constants.TEAM_LEADER_TYPE == command.changes.modified[i].type) {
									node.attr("rel",
											command.changes.modified[i].type);
								}

								// Change icon in case the date type changes
								if (m_constants.DATA === modelElement.type) {
									if (modelElement.structuredDataTypeFullId) {
										var typeDeclaration = m_model.findModel(m_model
												.stripModelId(modelElement.structuredDataTypeFullId)).typeDeclarations[m_model
												.stripElementId(modelElement.structuredDataTypeFullId)];
										if(typeDeclaration.isEnumeration()){
											node.attr("rel", m_constants.PRIMITIVE_DATA_TYPE);
										}else{
											node.attr("rel",
													command.changes.modified[i].dataType);
										}
									} else {
										node.attr("rel",
												command.changes.modified[i].dataType);
									}
								}

								// Change struct type icon in case the type
								// changes
								if (m_constants.TYPE_DECLARATION_PROPERTY === modelElement.type) {
									node.attr("rel", modelElement.getType());
								}

								// Change model icon in case the read-only factor has changed.
								if (m_constants.MODEL === modelElement.type) {
									modelTreeType="model";
									if(modelElement.isReadonly()){
										modelTreeType="lockedModel";
										if(modelElement.editLock && modelElement.editLock.lockStatus=="lockedByOther"){
											modelTreeType="lockedModelForEdit";
										}
									}
									node.attr("rel", modelTreeType);
									if (command.commandId === "modelLockStatus.update" && modelElement.isReadonly()) {
										var isModelLockCommand = true;
									}
								}

								renameElementViewLabel(node.attr("rel"), node
										.attr("id"), node.attr("name"));
							}
						}
						for ( var i = 0; i < command.changes.removed.length; i++) {
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
							if (command.changes.removed[i].uuid) {
								this
										.fireCloseViewCommand(command.changes.removed[i].uuid);
							}
						}

						if (command.isUndo) {
							this
									.processPendingUndo(command.pendingUndoableChange);
							this
									.processPendingRedo(command.pendingRedoableChange);
						} else if (command.isRedo) {
							this
									.processPendingUndo(command.pendingUndoableChange);
							this
									.processPendingRedo(command.pendingRedoableChange);
						} else {
							this.processPendingUndo(command);
							m_utils.jQuerySelect("#undoChange").removeClass("toolDisabled");
							m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
						}

						if (command.uiState) {
						  if (command.uiState.modelLocks) {
                m_utils.jQuerySelect(command.uiState.modelLocks).each(function(i, lockInfo) {
                  var model = m_model.findModel(lockInfo.modelId);
                  if (model) {
                    model.editLock = model.editLock || {};
                    m_utils.inheritFields(model.editLock, lockInfo);

                    refreshModelStatus(model);
                  }
                });
              }
						}

						if (command.commandId === "modelLockStatus.update") {
							if (isModelLockCommand) {
								m_utils.jQuerySelect("#undoChange").addClass("toolDisabled");
								m_utils.jQuerySelect("#redoChange").addClass("toolDisabled");
							}
						} else {
							m_messageDisplay.markModified();
							m_modelsSaveStatus.setModelsModified();
						}
					} else if (command.scope == "all") {
						// @deprecated
						refresh();
					}
				};

        Outline.prototype.processCommandError = function(command, response) {
          m_utils.debug("===> Outline - Processing Command Error");

          command = ("string" == typeof (command)) ? jQuery.parseJSON(command) : command;

          if (409 === response.status) {
            m_utils.debug("Refreshing model lock status");

            var model = m_model.findModel(command.modelId);

            m_communicationController.syncGetData({
              url : m_communicationController.getEndpointUrl() + "/sessions/editLock/"
                  + encodeURIComponent(model.id)
            }, {
              "success" : function(lockInfoJson) {
                model.editLock = model.editLock || {};

                m_utils.inheritFields(model.editLock, lockInfoJson);

                // refresh UI state
                refreshModelStatus(model);
              },
              "error" : function(e) {
                m_utils.debug("Failed refreshing lock status");
              }
            });
          }
        };

				/**
				 * TODO - temporary
				 */
				Outline.prototype.processPendingUndo = function(command) {
					if (command) {
						var action;
						var element;
						if (-1 != command.commandId.indexOf(".create")) {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.created");
							element = this
									.getChangedElementsText(command.changes.added);
						} else if (-1 != command.commandId.indexOf(".delete")) {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.deleted");
							element = this
									.getChangedElementsText(command.changes.removed);
						} else {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.modified");
							element = this
									.getChangedElementsText(command.changes.modified);
						}
						m_utils.jQuerySelect("#undoChange")
								.attr(
										"title",
										m_i18nUtils
												.getProperty("modeler.outline.toolbar.tooltip.undo")
												+ ": " + element + " " + action);
					} else {
						m_utils.jQuerySelect("#undoChange")
								.attr(
										"title",
										m_i18nUtils
												.getProperty("modeler.outline.toolbar.tooltip.undo"));
					}
				};

				/**
				 * TODO - temporary
				 */
				Outline.prototype.processPendingRedo = function(command) {
					if (command) {
						var action;
						var element;
						if (-1 != command.commandId.indexOf(".create")) {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.create");
							element = this
									.getChangedElementsText(command.changes.removed);
						} else if (-1 != command.commandId.indexOf(".delete")) {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.delete");
							element = this
									.getChangedElementsText(command.changes.added);
						} else {
							action = m_i18nUtils
									.getProperty("modeler.outline.toolbar.tooltip.modify");
							element = this
									.getChangedElementsText(command.changes.modified);
						}
						m_utils.jQuerySelect("#redoChange")
								.attr(
										"title",
										m_i18nUtils
												.getProperty("modeler.outline.toolbar.tooltip.redo")
												+ ": " + element + " " + action);
					} else {
						m_utils.jQuerySelect("#redoChange")
								.attr(
										"title",
										m_i18nUtils
												.getProperty("modeler.outline.toolbar.tooltip.redo"));
					}
				};

				/**
				 * TODO - temporary
				 */
				Outline.prototype.getChangedElementsText = function(
						elementArray) {
					if (elementArray.length > 2) {
						return "Multiple elements";
					} else if (elementArray.length == 2) {
						for ( var i = 0; i < elementArray.length; i++) {
							var txt = this
									.getChangedElementText(elementArray[i]);
							if (txt) {
								return txt;
							}
						}

						return elementArray[0].type;
					} else {
						return this.getChangedElementText(elementArray[0]);
					}
				};

				Outline.prototype.getChangedElementText = function(element) {
					if (element) {
						if (element.name) {
							return element.name;
						} else if (element.id) {
							return element.id;
						} else {
							if (-1 == element.type.indexOf(".")) {
								return element.type;
							}
						}
					}
				};

        /**
         *
         */
        Outline.prototype.createModel = function(data) {
          var outlineObj = this;

          var model = m_model.initializeFromJson(data);
          // register the new model with the model cache
          m_model.attachModel(model);

          var outlineTree = jQuery.jstree._reference(displayScope + "#outline");

          var modelNode = newJsTreeOutlineBuilder(model).buildModelNode(
              outlineTree.get_container());
          outlineTree.open_node(modelNode);

          jQuery("div#outlineMessageDiv").hide();

          runHasModelsCheck();

          window.parent.EventHub.events.publish("CONTEXT_UPDATED");
          
          return model;
        };

				/**
				 *
				 */
				Outline.prototype.deleteModel = function(transferObject) {
					m_model.deleteModel(transferObject.id);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					runHasModelsCheck();
				};

				/**
				 *
				 */
				Outline.prototype.deleteProcess = function(transferObject) {
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_process.deleteProcess(transferObject.id, model);
				};

				/**
				 *
				 */
				Outline.prototype.deleteApplication = function(transferObject) {
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_application.deleteApplication(transferObject.id, model);
				};

				/**
				 *
				 */
				Outline.prototype.deleteParticipant = function(transferObject) {
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_participant.deleteParticipantRole(transferObject.id,
							model);
				};

				/**
				 *
				 */
				Outline.prototype.deleteTypeDeclaration = function(
						transferObject) {
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_typeDeclaration.deleteTypeDeclaration(transferObject.id,
							model);
				};

				/**
				 *
				 */
				Outline.prototype.deleteData = function(transferObject) {
					m_utils.jQuerySelect(displayScope + "#outline").jstree("deselect_node",
							"#" + transferObject.uuid);
					m_utils.jQuerySelect(displayScope + "#outline").jstree("remove",
							"#" + transferObject.uuid);
					var model = m_model
							.findModelForElement(transferObject.uuid);
					m_data.deleteData(transferObject.id, model);
				};

        /**
         *
         */
        Outline.prototype.createProcess = function(transferObject) {
          var model = m_model.findModel(transferObject.modelId);
          var process = m_process.createProcessFromJson(model, transferObject);

          newJsTreeOutlineBuilder(model).buildProcessNode(process, "#" + model.uuid);
          this.expandNode("#" + model.uuid);

          return process;
        };

        /**
         *
         */
        Outline.prototype.createApplication = function(transferObject) {
          var model = m_model.findModel(transferObject.modelId);
          var application = m_application.initializeFromJson(model, transferObject);

          newJsTreeOutlineBuilder(model).buildApplicationNode(application,
              "#" + model.uuid + " #applications_" + model.uuid);
          this.expandNode("#" + model.uuid + " #applications_" + model.uuid);

          return application;
        };

        /**
         *
         */
        Outline.prototype.createData = function(transferObject) {
          var model = m_model.findModelByUuid(transferObject.modelUUID);
          var data = m_data.initializeFromJson(model, transferObject);

          newJsTreeOutlineBuilder(model).buildDataNode(data,
              "#" + model.uuid + " #data_" + model.uuid);
          this.expandNode("#" + model.uuid + " #data_" + model.uuid);

          return data;
        };

        /**
         *
         */
        Outline.prototype.createStructuredDataType = function(transferObject) {
          var model = m_model.findModel(transferObject.modelId);
          var dataStructure = m_typeDeclaration.initializeFromJson(model, transferObject);

          newJsTreeOutlineBuilder(model).buildTypeDeclarationNode(dataStructure,
              "#" + model.uuid + " #structuredTypes_" + model.uuid);
          this.expandNode("#" + model.uuid + " #structuredTypes_" + model.uuid);

          return dataStructure;
        };

        Outline.prototype.createParticipant = function(transferObject) {
          var model = m_model.findModelByUuid(transferObject.modelUUID);
          var participant = m_participant.initializeFromJson(model, transferObject);

          var parentSelector = (transferObject.parentUUID ? ("#" + transferObject.parentUUID)
              : ("#participants_" + model.uuid));

          newJsTreeOutlineBuilder(model).buildParticipantNode(participant,
              "#" + model.uuid + " " + parentSelector);

          this.expandNode("#" + model.uuid + " " + parentSelector);

          return participant;
        };

        /**
         *
         */
        Outline.prototype.expandNode = function(nodeSelector) {
        	jQuery.jstree._reference(displayScope + "#outline").open_node(nodeSelector);
        };
			}
		});
