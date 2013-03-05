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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_dataTypeSelector", "bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_accessPoint, m_dataTypeSelector, m_dataTraversal, m_dialog,
				m_modelElementView, m_codeEditorAce, m_i18nUtils) {
			return {
				initialize : function(fullId) {
					var view = new MessageTransformationApplicationView();
					i18nmessageTransformationproperties();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
					}
				};

			/**
			 *
			 */
			function i18nmessageTransformationproperties() {

				// Common properties
				jQuery("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				jQuery("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));
				jQuery("#application")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				jQuery("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));

				// Configuration
				jQuery("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));

				// Configuration - Source
				jQuery("#sourcemessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.sourceMessage"));
				jQuery("#addInputDataButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.sourceMessage.addInput"));
				jQuery("#inputElementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.element"));
				jQuery("#inputTypeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				jQuery("#filterHighlightedSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.infoMsg"));
				jQuery("#showAllSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.highlighted"));

				// Configuration - Target
				jQuery("#targetmessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.targetMessage"));
				jQuery("#outputElementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.element"));
				jQuery("#outputTypeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				jQuery("#mappingColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.mapping"));
				jQuery("#problemsColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.problem"));
				jQuery("#addOutputDataButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.addOutput"));
				jQuery("#filterFieldsWithMappingInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.mapping"));
				jQuery("#filterFieldsWithNoMappingInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.noMapping"));
				jQuery("#filterHighlightedTargetFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.infoMsg"));
				jQuery("#filterFieldsWithMappingInvalidInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.invalidMapping"));

				// Code Editor
				jQuery("#advancedMapping")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.sourceMessage.advancedMapping"));

				// Test
				jQuery("#testdata")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.tab"));
				jQuery("#runButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.runButton"));
				jQuery("#resetButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.resetButton"));
				jQuery("label[for='inputDataTextArea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.inputData"));
				jQuery("label[for='outputDataTable']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.outputData"));

				// Data Type Selector
				jQuery("label[for='dataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.datatType"));
				jQuery("label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				jQuery("label[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));
				jQuery("label[for='nameTextInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));

				// Add Input Message dialog
				jQuery("#inputDataDialog #applyButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.apply"));
				jQuery("#inputDataDialog #closeButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.close"));

				// Add Output Message dialog
				jQuery("#outputDataDialog #applyButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.apply"));
				jQuery("#outputDataDialog #closeButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.close"));

				// Index Configuration pop-up dialog
				jQuery("#idx-sourcemessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.sourceMessage"));
				jQuery("#idx-targetmessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.targetMessage"));
				jQuery("#idx-sourceTable-name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				jQuery("#idx-sourceTable-index")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.index"));
				jQuery("#idx-targetTable-name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				jQuery("#idx-targetTable-index")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.index"));
				jQuery("label[for='idx-showAffectedTreePaths']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.showAffectedPaths"));
				jQuery("#idx-mappingStrategy")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.mappingStrategy"));
				jQuery("label[for='idx-mappingStrategy-append']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.append"));
				jQuery("label[for='idx-mappingStrategy-overwrite']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.overwrite"));
				jQuery("#idx-okButton")
						.prop('value',
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.ok"));
				jQuery("#idx-cancelButton")
						.prop('value',
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.cancel"));
			}

			/**
			 *
			 */
			function MessageTransformationApplicationView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(
						MessageTransformationApplicationView.prototype, view);

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.initialize = function(
						application) {
					this.id = "messageTransformationApplicationView";
					this.inputData = {};
					this.outputData = {};
					this.mappingExpressions = {};
					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.inputTable = jQuery("#sourceTable");
					this.inputTableBody = jQuery("table#sourceTable tbody");
					this.sourceFilterInput = jQuery("#sourceFilterInput");
					this.targetFilterInput = jQuery("#targetFilterInput");
					this.outputTable = jQuery("#targetTable");
					this.outputTableBody = jQuery("table#targetTable tbody");
					this.filterFieldsWithMappingInput = jQuery("#filterFieldsWithMappingInput");
					this.filterFieldsWithNoMappingInput = jQuery("#filterFieldsWithNoMappingInput");
					this.filterHighlightedSourceFieldsInput = jQuery("#filterHighlightedSourceFieldsInput");
					this.filterHighlightedTargetFieldsInput = jQuery("#filterHighlightedTargetFieldsInput");
					this.filterFieldsWithMappingInvalidInput = jQuery("#filterFieldsWithMappingInvalidInput");
					this.showAllSourceFieldsInput = jQuery("#showAllSourceFieldsInput");
					this.showAllTargetFieldsInput = jQuery("#showAllTargetFieldsInput");

					this.filterFieldsWithMappingInput.data('enabled', false);
					this.filterFieldsWithNoMappingInput.data('enabled', false);
					this.filterFieldsWithMappingInvalidInput.data('enabled', false);

					this.selectedOutputTableRow = null;

					this.inputTableBody.empty();
					this.outputTableBody.empty();

					this.inputTableRows = [];
					this.outputTableRows = [];

					this.inputDataTypeSelector = m_dataTypeSelector.create({
						scope : "inputDataDialog",
						hideEnumerations : true
					});

					this.outputDataTypeSelector = m_dataTypeSelector.create({
						scope : "outputDataDialog",
						hideEnumerations : true
					});

					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);

					this.expressionEditor = m_codeEditorAce.getJSCodeEditor("expressionTextDiv");
					this.expressionEditor.disable();

					this.bindEventHandlers();

					this.initializeModelElementView(application);
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.setModelElement = function(
						application) {
					// Store the tree nodes that are expanded as
					// this information will be lost after the trees are refreshed

					// TODO - needs review
					// Employs direct manipulations of classes
					// hence subject to problems in case of version change etc.
					var inputRowExpandedStatus = [];
					var outputRowExpandedStatus = [];
					this.inputTableBody.find("tr").each(function(index) {
						inputRowExpandedStatus[this.id] = $(this).hasClass("expanded")
					});
					this.outputTableBody.find("tr").each(function(index) {
						outputRowExpandedStatus[this.id] = $(this).hasClass("expanded")
					});

					// Store the table rows that are selected
					var selectedInputRowId = jQuery("table#sourceTable tr.selected").first().attr('id');
					var selectedOutputRowId = jQuery("table#targetTable tr.selected").first().attr('id');

					this.initializeModelElement(application);
					this.application = application;

					if (!this.application.attributes["carnot:engine:visibility"]
							|| "Public" == this.application.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					this.inputData = {};
					this.outputData = {};
					this.mappingExpressions = {};
					this.inputTableBody.empty();
					this.outputTableBody.empty();
					this.inputTableRows = [];
					this.outputTableRows = [];
					this.selectedOutputTableRow = null;
					jQuery("#elementIndicatorText").empty();
					this.expressionEditor.setValue("");
					this.expressionEditor.disable();

					this
							.convertFromMappingsXml(this.application.attributes["messageTransformation:TransformationProperty"]);

					for ( var key in this.application.contexts) {
						var context = this.application.contexts[key];
						for ( var m = 0; m < context.accessPoints.length; ++m) {
							var accessPoint = context.accessPoints[m];

							if (accessPoint.direction == "IN") {
								this.addInputData(accessPoint);
							} else {
								this.addOutputData(accessPoint);
							}
						}
					}

					this.inputTable.tableScroll("undo");
					this.outputTable.tableScroll("undo");

					this.populateTableRows(this.inputTableBody,
							this.inputTableRows, true);
					this.populateTableRows(this.outputTableBody,
							this.outputTableRows, false);

					// Restore the expanded tree nodes

					// TODO - needs review
					// Employs direct manipulations of classes
					// hence subject to problems in case of version change etc.
					this.inputTableBody.find("tr").each(function(index) {
						if (inputRowExpandedStatus[this.id]) {
							jQuery(this).addClass("expanded");
							jQuery(this).removeClass("ui-helper-hidden");
						}
					});
					this.outputTableBody.find("tr").each(function(index) {
						if (outputRowExpandedStatus[this.id]) {
							jQuery(this).addClass("expanded");
							jQuery(this).removeClass("ui-helper-hidden");
						}
					});

					// Restore the selected tree nodes
					jQuery("#sourceTable #" + selectedInputRowId).addClass("selected");
					jQuery("#targetTable #" + selectedOutputRowId).addClass("selected");

					this.selectedOutputTableRow = jQuery("#targetTable tr.selected").data("tableRow");

					// Initialize the state of the code editor
					if (this.selectedOutputTableRow != null) {
						jQuery("#elementIndicatorText").append(this.selectedOutputTableRow.path + " = ");
						this.expressionEditor.setValue(this.selectedOutputTableRow.mappingExpression);
						this.expressionEditor.enable();
					}

					// Initialize the Input / Output Data Type Selector dialogs
					this.inputDataTypeSelector.setScopeModel(this.getModel());
					this.inputDataTypeSelector.populatePrimitivesSelectInput();
					this.inputDataTypeSelector.setDataTypeSelectVal({dataType: m_constants.PRIMITIVE_DATA_TYPE});
					this.inputDataTypeSelector.setPrimitiveDataType();

					this.outputDataTypeSelector.setScopeModel(this.getModel());
					this.outputDataTypeSelector.populatePrimitivesSelectInput();
					this.outputDataTypeSelector.setDataTypeSelectVal({dataType: m_constants.PRIMITIVE_DATA_TYPE});
					this.outputDataTypeSelector.setPrimitiveDataType();

					// Global variables for Code Editor auto-complete / validation
					var globalVariables = {};
					var typeDeclaration;
					for (var id in this.inputData) {
						typeDeclaration = this.inputData[id];
						if (typeDeclaration != null) {
							globalVariables[id] = typeDeclaration.createInstance();
						}
						else {
							globalVariables[id] = "";
						}
					}

					for (var id in this.outputData) {
						typeDeclaration = this.outputData[id];
						if (typeDeclaration != null) {
							globalVariables[id] = typeDeclaration.createInstance();
						}
						else {
							globalVariables[id] = "";
						}
					}

					//this.expressionEditor.setGlobalVariables(globalVariables);

					// Perform mapping expression validation
//					var source, errors;
//					for (var n = 0; n < this.outputTableRows.length; ++n) {
//						if (this.outputTableRows[n].mappingExpression === "") continue;
//						source = this.outputTableRows[n].path + " = " + this.outputTableRows[n].mappingExpression;
//						errors = this.expressionEditor.getErrors(source, globalVariables);
//						this.showMappingError(this.outputTableRows[n].path, errors);
//					}

					this.resume();

					// Show View-related error messages
					this.showErrorMessages();
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.resume = function() {
					this.inputTable.tableScroll({
						height : 200
					});
					this.inputTable.treeTable({
						indent: 14
					});

					this.outputTable.tableScroll({
						height : 200
					});
					this.outputTable.treeTable({
						indent: 14
					});

					jQuery("table#sourceTable tbody tr").mousedown({
						"view" : this
					}, function() {
						jQuery("table#sourceTable tr.selected").removeClass("selected");
						jQuery(this).addClass("selected");
					});

					jQuery("table#sourceTable tbody tr span").mousedown(
							function() {
								jQuery(jQuery(this).parents("tr")[0]).trigger(
										"mousedown");
							});

					jQuery("table#targetTable tbody tr")
							.mousedown(
									function() {
										var view = jQuery(this).data("view");

										var self = this;

										// Using setTimeout so that blur event of code editor (if applicable) is called first
										setTimeout(function() {
											jQuery("table#targetTable tr.selected").removeClass(
													"selected");
											jQuery(self).addClass("selected");

											view.selectedOutputTableRow = jQuery(
													self).data("tableRow");

											jQuery("#elementIndicatorText").empty();
											jQuery("#elementIndicatorText")
													.append(
															view.selectedOutputTableRow.path
																	+ " = ");

											view.expressionEditor.enable();
											view.expressionEditor
													.setValue(view.selectedOutputTableRow.mappingExpression);
											//view.expressionEditor.save();

											// Register showMappingError as a callback function after JS validation occurs
											//view.expressionEditor.setJavaScriptValidationOptions(view.showMappingError, view.selectedOutputTableRow.path);
										}, 0);
									});

					jQuery("table#targetTable tbody tr span").mousedown(
							function() {
								jQuery(jQuery(this).parents("tr")[0]).trigger(
										"mousedown");
							});
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.deleteAccessPoint = function(accessPoint) {
					for (var i in this.application.contexts.application.accessPoints) {
						if (this.application.contexts.application.accessPoints[i] === accessPoint) {
							this.application.contexts.application.accessPoints.splice(i, 1);
							break;
						}
					}

					this.submitChanges({
							contexts : {
								application : {
									accessPoints : this.application.contexts.application.accessPoints
								}
							}
					});
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.addInputAccessPoint = function(
						mappingName, data) {
					var accessPoint = null;
					if (data.dataType === m_constants.PRIMITIVE_DATA_TYPE) {
						accessPoint = m_accessPoint.createFromPrimitive(data.primitiveDataType, mappingName, mappingName, m_constants.IN_ACCESS_POINT)
					}
					else if (data.dataType === m_constants.STRUCTURED_DATA_TYPE) {
						accessPoint = m_accessPoint.createFromDataStructure(m_model.findTypeDeclaration(data.structuredDataTypeFullId), mappingName, mappingName, m_constants.IN_ACCESS_POINT)
					}

					if (accessPoint == null) {
						return;
					}

					this.application.contexts.application.accessPoints.push(accessPoint);

					this.submitChanges({
							contexts : {
								application : {
									accessPoints : this.application.contexts.application.accessPoints
								}
							}
					});
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.addInputData = function(
						accessPoint) {
					if (accessPoint.dataType === m_constants.STRUCTURED_DATA_TYPE) {
						var typeDeclaration = m_accessPoint.retrieveTypeDeclaration(accessPoint, this.getModel());
						this.inputData[accessPoint.id] = typeDeclaration;
						this.initializeInputTableRowsRecursively(accessPoint,
								typeDeclaration.getBody(), null,
								typeDeclaration.model);
					}
					else { // accessPoint.dataType === m_constants.PRIMITIVE_DATA_TYPE
						this.inputData[accessPoint.id] = null; // TODO
						this.initializeInputTableRowsRecursively(accessPoint, null, null, null);
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.addOutputAccessPoint = function(
						mappingName, data) {
					var accessPoint = null;
					if (data.dataType === m_constants.PRIMITIVE_DATA_TYPE) {
						accessPoint = m_accessPoint.createFromPrimitive(data.primitiveDataType, mappingName, mappingName, m_constants.OUT_ACCESS_POINT)
					}
					else if (data.dataType === m_constants.STRUCTURED_DATA_TYPE) {
						accessPoint = m_accessPoint.createFromDataStructure(m_model.findTypeDeclaration(data.structuredDataTypeFullId), mappingName, mappingName, m_constants.OUT_ACCESS_POINT)
					}

					if (accessPoint == null) {
						return;
					}

					this.application.contexts.application.accessPoints.push(accessPoint);

					this.submitChanges({
						contexts : {
							application : {
								accessPoints : this.application.contexts.application.accessPoints
							}
						}
					});
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.addOutputData = function(
						accessPoint) {
					if (accessPoint.dataType === m_constants.STRUCTURED_DATA_TYPE) {
						var typeDeclaration = m_accessPoint.retrieveTypeDeclaration(accessPoint, this.getModel());
						this.outputData[accessPoint.id] = typeDeclaration;
						this.initializeOutputTableRowsRecursively(accessPoint,
								typeDeclaration.getBody(), null,
								typeDeclaration.model);
					}
					else { // accessPoint.dataType === m_constants.PRIMITIVE_DATA_TYPE
						this.inputData[accessPoint.id] = null; // TODO
						this.initializeOutputTableRowsRecursively(accessPoint, null, null, null);
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.initializeInputTableRowsRecursively = function(
						accessPoint, element, parentPath, scopeModel) {
					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + element.name);
					var tableRow = {};

					this.inputTableRows.push(tableRow);

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.id
							: element.name;

					tableRow.typeName = parentPath == null ?
							(accessPoint.dataType == m_constants.STRUCTURED_DATA_TYPE ? m_accessPoint.retrieveTypeDeclaration(accessPoint, this.getModel()).name : accessPoint.primitiveDataType)
							: element.type;

					// Embedded structure
					if (element == null) {
						return;
					}

					var childElements = element.elements;

					// Recursive resolution
					if (childElements == null && element.type != null) {
						var typeDeclaration = scopeModel
								.findTypeDeclarationBySchemaName(m_model.stripElementId(element.type));

						if (typeDeclaration != null
								&& typeDeclaration.isSequence()) {
							childElements = typeDeclaration.getBody().elements;
						}
					}

					if (childElements == null) {
						return;
					}

					for ( var childElement in childElements) {
						this.initializeInputTableRowsRecursively(accessPoint,
								childElements[childElement], path, scopeModel);
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.initializeOutputTableRowsRecursively = function(
						accessPoint, element, parentPath, scopeModel) {
					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + element.name);
					var tableRow = {};

					this.outputTableRows.push(tableRow);

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.id
							: element.name;
					tableRow.typeName = parentPath == null ?
							(accessPoint.dataType == m_constants.STRUCTURED_DATA_TYPE ? m_accessPoint.retrieveTypeDeclaration(accessPoint, this.getModel()).name : accessPoint.primitiveDataType)
							: element.type;

					tableRow.mappingExpression = this.mappingExpressions[path] == null ? ""
							: this.mappingExpressions[path];
					tableRow.problems = "";

					// Embedded structure
					if (element == null) {
						return;
					}

					var childElements = element.elements;

					// Recursive resolution

					if (childElements == null && element.type != null) {
						var typeDeclaration = scopeModel
								.findTypeDeclarationBySchemaName(m_model.stripElementId(element.type));

						if (typeDeclaration != null
								&& typeDeclaration.isSequence()) {
							childElements = typeDeclaration.getBody().elements;
						}
					}

					if (childElements == null) {
						return;
					}

					for ( var childElement in childElements) {
						this.initializeOutputTableRowsRecursively(accessPoint,
								childElements[childElement], path, scopeModel);
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.populateTableRows = function(
						tableBody, tableRows, source) {
					tableBody.empty();

					for ( var tableRow in tableRows) {
						var rowId = tableRows[tableRow].path
								.replace(/\./g, "-");

						var content = "<tr id=\""
								+ rowId
								+ "\" "
								+ (tableRows[tableRow].parentPath != null ? ("class=\"child-of-"
										+ tableRows[tableRow].parentPath
												.replace(/\./g, "-") + "\"")
										: "") + ">";

						content += "<td class='elementCell'>";
						content += "<span class=\"data-element\">"
								+ tableRows[tableRow].name + "</span>";
						content += "</td>";
						content += "<td class='typeCell'>" + tableRows[tableRow].typeName;
						+"</td>";

						var deleteTooltip = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.delete");
						if (source) {
							content += "<td class='inputActionsCell'>";
							if (tableRows[tableRow].parentPath == null) {
								content += "<div class='deleteAction' title='" + deleteTooltip + "'></div>";
							}
							content += "</td>";
							content += "</tr>";

							tableBody.append(content);

							// Add click event handler for "delete" action
							if (tableRows[tableRow].parentPath == null) {
								var deleteIcon = jQuery("#sourceTable #" + rowId + " .deleteAction");
								deleteIcon.click({
									"view" : this,
									"accessPoint" : tableRows[tableRow].accessPoint
								}, function(event) {
									event.data.view.deleteAccessPoint(event.data.accessPoint);
								});
							}

							var dataElement = jQuery("#sourceTable #" + rowId
									+ " .data-element");

							dataElement.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});
							dataElement.draggable({
								helper : "clone",
								opacity : .75,
								refreshPositions : true,
								revert : "invalid",
								revertDuration : 300,
								scroll : true
							});

							var row = jQuery("#sourceTable #" + rowId);

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view
										.highlightSource(event.data.tableRow);
							});

						} else {
							var clearMappingTooltip = m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.clearMapping");
							content += "<td class='mappingCell' />";
							content += "<td class='problemCell' />";
							content += "<td class='outputActionsCell'>";
							content += "<div class='clearMappingAction' title='" + clearMappingTooltip + "'></div>";
							if (tableRows[tableRow].parentPath == null) {
								content += "<div class='deleteAction' title='" + "Delete" + "'></div>";
							}
							content += "</td>";
							content += "</tr>";

							tableBody.append(content);

							this.populateMappingCell(tableRows[tableRow]);

							// Add click event handler for "clearMapping" action
							var clearMappingIcon = jQuery("#targetTable #" + rowId + " .clearMappingAction");
							if (tableRows[tableRow].mappingExpression != "") {
								clearMappingIcon.click({
									"view" : this,
									"outputTableRow" : tableRows[tableRow]
								}, function(event) {
									event.data.view.clearMappingExpression(event.data.outputTableRow);
								});
							}
							else {
								clearMappingIcon.addClass("disabled");
							}

							// Add click event handler for "delete" action
							if (tableRows[tableRow].parentPath == null) {
								var deleteIcon = jQuery("#targetTable #" + rowId + " .deleteAction");
								deleteIcon.click({
									"view" : this,
									"accessPoint" : tableRows[tableRow].accessPoint
								}, function(event) {
									event.data.view.deleteAccessPoint(event.data.accessPoint);
								});
							}

							var row = jQuery("#targetTable #" + rowId);
							row.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});

							row
									.droppable({
										accept : ".data-element",
										drop : function(e, ui) {
											var view = jQuery(this)
													.data("view");
											var outputTableRow = jQuery(this).data("tableRow");
											var inputTableRow = ui.draggable
													.data("tableRow");

											// TODO: @Anoop - Refactor
											if (view.isStructuredType(inputTableRow) && view.isStructuredType(outputTableRow) &&
													(inputTableRow.typeName === outputTableRow.typeName)) {
												var prefix = outputTableRow.path + ".";
												for (var n = 0; n < view.outputTableRows.length; ++n) {
													if (view.outputTableRows[n].path.indexOf(prefix) == 0) {
														if (view.outputTableRows[n].typeName.indexOf("xsd:") == 0) {
															view.outputTableRows[n].mappingExpression = view.outputTableRows[n].path.replace(prefix, inputTableRow.path + ".");
															view.populateMappingCell(view.outputTableRows[n]);
														}
													}
												}
											}
											else {
												outputTableRow.mappingExpression = inputTableRow.path;
												view.populateMappingCell(outputTableRow);

												// Update expression text area if needed
												if (view.selectedOutputTableRow == outputTableRow) {
													view.expressionEditor
															.setValue(outputTableRow.mappingExpression);
													//view.expressionEditor.save();
												}
											}

											// Remove the drag helper
											ui.helper.remove();

											view
													.submitChanges(view
															.determineTransformationChanges());

										},
										hoverClass : "accept",
										over : function(e, ui) {
										}
									});

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view
										.highlightTarget(event.data.tableRow);
							});
						}
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.highlightSource = function(
						tableRow) {
					jQuery("#sourceTable .data-element").removeClass(
							"highlighted");
					jQuery("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						if (this.outputTableRows[n].mappingExpression
								.indexOf(this.inputTableRows[tableRow].path) != -1) {
							jQuery(
									"#targetTable #"
											+ this.outputTableRows[n].path
													.replace(/\./g, "-")
											+ " .data-element").addClass(
									"highlighted");
						}
					}

					/*if (this.isPrimitive(this.inputTableRows[tableRow])) {
						alert("primitive");
					}
					else if (this.isStructuredType()) {

					}
					else if (this.isEnumeration()) {

					}*/
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.highlightTarget = function(
						tableRow) {
					jQuery("#sourceTable .data-element").removeClass(
							"highlighted");
					jQuery("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.inputTableRows.length; ++n) {
						if (this.outputTableRows[tableRow].mappingExpression
								.indexOf(this.inputTableRows[n].path) != -1) {
							jQuery(
									"#sourceTable #"
											+ this.inputTableRows[n].path
													.replace(/\./g, "-")
											+ " .data-element").addClass(
									"highlighted");
						}
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterSource = function(
						filter) {
					if (filter == null || filter == "") {
						jQuery("table#sourceTable tbody tr").removeClass(
								"invisible");
					} else {
						jQuery("table#sourceTable tbody tr").addClass(
								"invisible");

						jQuery("table#sourceTable tbody tr:contains('" + filter + "')").each(function() {
							jQuery(this).removeClass("invisible");
							jQuery(ancestorsOf(jQuery(this))).removeClass("invisible");
						});
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterTarget = function(
						filter) {
					if (filter == null || filter == "") {
						jQuery("table#targetTable tbody tr").removeClass(
								"invisible");
					} else {
						jQuery("table#targetTable tbody tr").addClass(
								"invisible");

						jQuery("table#targetTable tbody tr:contains('" + filter + "')").each(function() {
							jQuery(this).removeClass("invisible");
							jQuery(ancestorsOf(jQuery(this))).removeClass("invisible");
						});

					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithNoMapping = function(enabled) {
					if (enabled) {
						jQuery("table#targetTable tbody tr").addClass("invisible");
						jQuery("table#targetTable tbody tr .mappingCell:empty").parent().each(function() {
							jQuery(this).removeClass("invisible");
							jQuery(ancestorsOf(jQuery(this))).removeClass("invisible");
						});
					}
					else {
						jQuery("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMapping = function(enabled) {
					if (enabled) {
						jQuery("table#targetTable tbody tr").addClass("invisible");
						jQuery("table#targetTable tbody tr .mappingCell:not(:empty)").parent().each(function() {
							jQuery(this).removeClass("invisible");
							jQuery(ancestorsOf(jQuery(this))).removeClass("invisible");
						});
					}
					else {
						jQuery("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMappingInvalid = function(enabled) {
					if (enabled) {
						jQuery("table#targetTable tbody tr").addClass("invisible");
						jQuery("table#targetTable tbody tr .mappingError").parent().each(function() {
							jQuery(this).removeClass("invisible");
							jQuery(ancestorsOf(jQuery(this))).removeClass("invisible");
						});
					}
					else {
						jQuery("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedSourceFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeVisible(this.showAllSourceFieldsInput);

					jQuery("table#sourceTable tbody tr").addClass("invisible");
					jQuery("table#sourceTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedTargetFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeVisible(this.showAllTargetFieldsInput);

					jQuery("table#targetTable tbody tr").addClass("invisible");
					jQuery("table#targetTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.showAllSourceFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					jQuery("table#sourceTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.showAllTargetFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);
					jQuery("table#targetTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.populateMappingCell = function(outputTableRow) {
					var maxLength = 35;

					var rowId = outputTableRow.path.replace(/\./g, "-");
					var mappingCell = jQuery("#targetTable tr#" + rowId + " .mappingCell");
					var trimmedString = (outputTableRow.mappingExpression != null && outputTableRow.mappingExpression.length) > maxLength ?
											outputTableRow.mappingExpression.substring(0, maxLength - 3) + "..." :
											outputTableRow.mappingExpression;

					mappingCell.empty();
					mappingCell.append(trimmedString);
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.clearMappingExpression = function(outputTableRow) {
					outputTableRow.mappingExpression = "";
					this.populateMappingCell(outputTableRow);

					this.submitChanges(this.determineTransformationChanges());
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.showMappingError = function(
						path, errors) {
					var rowId = path.replace(/\./g, "-");
					var problemCell = jQuery("#targetTable tr#" + rowId + " .problemCell");
					var mappingCell = jQuery("#targetTable tr#" + rowId + " .mappingCell");

					// Ignore any errors due to "Missing semicolon."
					var hasError = false;
					if (!jQuery.isEmptyObject(errors) && mappingCell.text().trim() != "") {
						for (var lineNumber in errors) {
							for (var i = 0; i < errors[lineNumber].length; i++) {
								// TODO: Hard-coded reference to "Missing semicolon" error from JS validator
								if (!(errors[lineNumber][i] == "Expected ';' and instead saw '(end)'.")) {
									hasError = true;
									break;
								}
							}
							if (hasError) break;
						}

						if (hasError) {
							problemCell.addClass("mappingError");
						}
					}
					else {
						problemCell.removeClass("mappingError");
					}
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.openIndexConfigurationDialog = function() {
					this.initializeIndexConfigurationDialog();
					jQuery("#indexConfigurationDialog").dialog("open");

					// Note: tableScroll must be added after the dialog is visible for correct behavior
					var indexConfigSourceTable = jQuery("#idx-sourceTable");
					var indexConfigTargetTable = jQuery("#idx-targetTable");

					indexConfigSourceTable.tableScroll({
						height : 100
					});
					indexConfigSourceTable.treeTable({
						indent: 14
					});

					indexConfigTargetTable.tableScroll({
						height : 100
					});
					indexConfigTargetTable.treeTable({
						indent: 14
					});

					var dragSource = jQuery("#idx-sourceTable tbody tr#n-New1-one");
					var dropTarget = jQuery("#idx-targetTable tbody tr#n2-New2-three");

					// Make the affected nodes highlighted
					jQuery(dragSource).find(".data-element").addClass("highlighted");
					jQuery(ancestorsOf(jQuery(dragSource))).find(".data-element").addClass("highlighted");

					// Make the affected nodes highlighted
					jQuery(dropTarget).find(".data-element").addClass("highlighted");
					jQuery(ancestorsOf(jQuery(dropTarget))).find(".data-element").addClass("highlighted");

					// Make all rows invisible
					jQuery("#idx-sourceTable tbody tr").addClass("invisible");
					// Make the affected nodes visible
					jQuery(dragSource).removeClass("invisible");
					jQuery(ancestorsOf(jQuery(dragSource))).removeClass("invisible");

					// Make all rows invisible
					jQuery("#idx-targetTable tbody tr").addClass("invisible");
					// Make the affected nodes visible
					jQuery(dropTarget).removeClass("invisible");
					jQuery(ancestorsOf(jQuery(dropTarget))).removeClass("invisible");
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.initializeIndexConfigurationDialog = function() {
					var indexConfigSourceTable = jQuery("#idx-sourceTable");
					var indexConfigTargetTable = jQuery("#idx-targetTable");

					indexConfigSourceTable.tableScroll("undo");
					indexConfigTargetTable.tableScroll("undo");

					this.populateIndexConfigurationTableRows(jQuery("#idx-sourceTable tbody"), this.inputTableRows);
					this.populateIndexConfigurationTableRows(jQuery("#idx-targetTable tbody"), this.outputTableRows);

					jQuery("#idx-sourceTable tbody tr").mousedown(
							function() {
								jQuery("#idx-sourceTable tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
							});

					jQuery("#idx-targetTable tbody tr").mousedown(
							function() {
								jQuery("#idx-targetTable tr.selected").removeClass("selected");
								jQuery(this).addClass("selected");
							});

					jQuery("#idx-showAffectedTreePaths").click(
							function() {
								var dragSource = jQuery("#idx-sourceTable tbody tr#n-New1-one");
								var dropTarget = jQuery("#idx-targetTable tbody tr#n2-New2-three");

								if (jQuery(this).is(':checked')) {
									// Make all rows invisible
									jQuery("#idx-sourceTable tbody tr").addClass("invisible");
									// Make the affected node visible
									jQuery(dragSource).removeClass("invisible");
									jQuery(ancestorsOf(jQuery(dragSource))).removeClass("invisible");

									// Make all rows invisible
									jQuery("#idx-targetTable tbody tr").addClass("invisible");
									// Make the affected nodes visible
									jQuery(dropTarget).removeClass("invisible");
									jQuery(ancestorsOf(jQuery(dropTarget))).removeClass("invisible");
								}
								else {
									// Make all rows visible
									jQuery("#idx-sourceTable tbody tr").removeClass("invisible");
									jQuery("#idx-targetTable tbody tr").removeClass("invisible");
								}
							});
				}
				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.populateIndexConfigurationTableRows = function(tableBody, tableRows) {
					tableBody.empty();

					for (var tableRow in tableRows) {
						var rowId = tableRows[tableRow].path.replace(/\./g, "-");

						var content = '<tr id="' + rowId + '" '
								+ (tableRows[tableRow].parentPath != null ?
										('class="child-of-' + tableRows[tableRow].parentPath .replace(/\./g, "-") + '"') : '')
								+ '>';

						content += '<td>';
						content += '<span class="data-element">' + tableRows[tableRow].name + '</span>';
						content += '</td>';
						content += '<td>' + '' + '</td>';
						content += '</tr>';

						tableBody.append(content);

						/*var dataElement = jQuery("#sourceTable #" + rowId + " .data-element");

						dataElement.data({
							"view" : this,
							"tableRow" : tableRows[tableRow]
						});*/
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.convertFromMappingsXml = function(
						xml) {
					var xmlDoc;
					try {
						xmlDoc = jQuery.parseXML(xml);
						var xmlObject = jQuery(xmlDoc);

						var view = this; // required since jQuery.find() below changes context of 'this'

						jQuery(xmlObject).find("fieldMappings").each(
								function() {

									var fieldPath = jQuery(this).attr("fieldPath")

									fieldPath = fieldPath.replace(/\/$/g, ""); // Remove trailing slash(es)
									fieldPath = fieldPath.replace(/\//g, "."); // Replace slash(es) with "."

									view.mappingExpressions[fieldPath] = jQuery(
											this).attr("mappingExpression");
								});
					} catch(e) {
						this.errorMessages
							.push(m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.invalidXml"));
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.determineTransformationChanges = function() {
					var transformationProperty = '<?xml version="1.0" encoding="ASCII"?>\r\n';
					transformationProperty += '<mapping:TransformationProperty xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
						'xmlns:mapping="java://org.eclipse.stardust.engine.extensions.transformation.model" ' +
						'xsi:schemaLocation="java://org.eclipse.stardust.engine.extensions.transformation.model ' +
						'java://org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage">\r\n';

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						var outputTableRow = this.outputTableRows[n];

						if (outputTableRow.mappingExpression && 0 !== outputTableRow.mappingExpression.length) {
							transformationProperty += '  <fieldMappings fieldPath="';
							var fieldPath = outputTableRow.path.replace(/\./g, "/"); // Replace "." with "/"
							if (outputTableRow.accessPoint.dataType === m_constants.PRIMITIVE_DATA_TYPE) {
								fieldPath += "/"; // Primitive access points must have a trailing slash
							}
							transformationProperty += fieldPath;
							transformationProperty += '" mappingExpression="';
							transformationProperty += outputTableRow.mappingExpression.replace(/&/g, '&amp;')
																						.replace(/</g, '&lt;')
																						.replace(/>/g, '&gt;')
																						.replace(/"/g, '&quot;')
																						.replace(/\n/g, '&#xA;');
							transformationProperty += '"/>\r\n';
						}
					}

					transformationProperty += '</mapping:TransformationProperty>\r\n';

					return {
						attributes : {
							"messageTransformation:TransformationProperty" : transformationProperty
						}
					};
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.bindEventHandlers = function() {
					// Common
					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"]
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});

					// Code Editor
					var view = this;
					this.expressionEditor.getEditor().on('blur', function(event){
						if (view.selectedOutputTableRow != null) {
							var mappingExpression = view.expressionEditor.getValue();

							// Update "mappingExpression" for selectedOutputTableRow and corresponding row in outputTableRows[] array
							for (var n = 0; n < view.outputTableRows.length; ++n) {
								if (view.selectedOutputTableRow === view.outputTableRows[n]) {
									view.selectedOutputTableRow.mappingExpression = mappingExpression;
									view.outputTableRows[n].mappingExpression = mappingExpression;
									break;
								}
							}

							view.populateMappingCell(view.selectedOutputTableRow);

							view.submitChanges(view.determineTransformationChanges());
						}
					});

					jQuery("#expressionTextDiv")
							.droppable({
								accept : ".data-element",
								drop : function(e, ui) {
									var view = ui.draggable.data("view");

									if (view.selectedOutputTableRow != null) {
										var mappingExpression = view.selectedOutputTableRow.mappingExpression;

										if (mappingExpression != null
												&& mappingExpression != "") {
											mappingExpression += " + ";
										} else {
											mappingExpression = "";
										}

										var inputTableRow = ui.draggable
												.data("tableRow")
										mappingExpression += inputTableRow.path;

										// Update "mappingExpression" for selectedOutputTableRow and corresponding row in outputTableRows[] array
										for (var n = 0; n < view.outputTableRows.length; ++n) {
											if (view.selectedOutputTableRow === view.outputTableRows[n]) {
												view.selectedOutputTableRow.mappingExpression = mappingExpression;
												view.outputTableRows[n].mappingExpression = mappingExpression;
												break;
											}
										}

										view.expressionEditor.setValue(view.selectedOutputTableRow.mappingExpression);
										//view.expressionEditor.save();

										view.populateMappingCell(view.selectedOutputTableRow);

										// Remove the drag helper
										ui.helper.remove();

										view
												.submitChanges(view
														.determineTransformationChanges());
									}
								},
								hoverClass : "accept",
								over : function(e, ui) {
									var view = ui.draggable.data("view");
									var outputTableRow = view.selectedOutputTableRow;
								}
							});

					// Source Message
					this.sourceFilterInput.keypress({
						"view" : this
					}, function(event) {
						event.data.view
								.filterSource(event.data.view.sourceFilterInput
										.val());
					});

					this.filterHighlightedSourceFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterHighlightedSourceFields();
					});

					this.showAllSourceFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.showAllSourceFields();
					});

					// Target Message
					this.targetFilterInput.keypress({
						"view" : this
					}, function(event) {
						event.data.view
								.filterTarget(event.data.view.targetFilterInput
										.val());
					});

					this.filterHighlightedTargetFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterHighlightedTargetFields();
					});

					this.showAllTargetFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.showAllTargetFields();
					});

					this.filterFieldsWithNoMappingInput.click({
						"view" : this
					}, function(event) {
						var enabled = event.data.view.filterFieldsWithNoMappingInput.data('enabled');
						event.data.view.filterFieldsWithNoMappingInput.data('enabled', !enabled);
						event.data.view.filterFieldsWithNoMapping(!enabled);
					});

					this.filterFieldsWithMappingInput.click({
						"view" : this
					}, function(event) {
						var enabled = event.data.view.filterFieldsWithMappingInput.data('enabled');
						event.data.view.filterFieldsWithMappingInput.data('enabled', !enabled);
						event.data.view.filterFieldsWithMapping(!enabled);
					});

					this.filterFieldsWithMappingInvalidInput.click({
						"view" : this
					}, function(event) {
						var enabled = event.data.view.filterFieldsWithMappingInvalidInput.data('enabled');
						event.data.view.filterFieldsWithMappingInvalidInput.data('enabled', !enabled);
						event.data.view.filterFieldsWithMappingInvalid(!enabled);
					});

					// Add Input Message dialog
					jQuery("#inputDataDialog").dialog({
						autoOpen : false,
						draggable : true,
						resizable : false,
						minWidth : 400,
						title : m_i18nUtils
									.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.addInput.popUp")
									});

					jQuery("#inputDataDialog").bind("dialogclose", {view : this},function(event, ui) {
						// Clear the 'name' field and any errors
						var nameInput = jQuery("#inputDataDialog #nameTextInput");
						nameInput.val('');
						nameInput.removeClass("error");
						event.data.view.clearErrorMessages();
									});

					jQuery("#addInputDataButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										jQuery("#inputDataDialog").dialog(
												"open");

//										event.data.view.openIndexConfigurationDialog();
									});

					jQuery("#inputDataDialog #applyButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										var selectedData = {};
										event.data.view.inputDataTypeSelector.getDataType(selectedData);

										// Validate if a concrete Structured Type was selected
										if (selectedData.structuredDataTypeFullId === m_constants.TO_BE_DEFINED) {
											var msg = m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.invalidType");
											event.data.view.errorMessages.push(msg);
											event.data.view.showErrorMessages();
											return;
										}

										// Validate message name
										var nameTextInput = jQuery("#inputDataDialog #nameTextInput");
										var isValidName = event.data.view.validateMessageName(nameTextInput);
										if (isValidName) {
											event.data.view.addInputAccessPoint(nameTextInput.val(), selectedData);
											event.data.view.resume();
											jQuery("#inputDataDialog").dialog("close");
										}
									});

					jQuery("#inputDataDialog #closeButton").click(function() {
						jQuery("#inputDataDialog").dialog("close");
					});

					// Add Output Message dialog
					jQuery("#outputDataDialog").dialog({
						autoOpen : false,
						draggable : true,
						resizable : false,
						minWidth : 400,
						title : m_i18nUtils
									.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.addOutput.popUp")
									});

					jQuery("#outputDataDialog").bind("dialogclose", {view : this}, function(event, ui) {
						// Clear the 'name' field and any errors
						var nameInput = jQuery("#outputDataDialog #nameTextInput");
						nameInput.val('');
						nameInput.removeClass("error");
						event.data.view.clearErrorMessages();
									});

					jQuery("#addOutputDataButton")
							.click(
									{
										view : this
									},
									function(event) {
										jQuery("#outputDataDialog").dialog(
												"open");
									});

					jQuery("#outputDataDialog #applyButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										var selectedData = {};
										event.data.view.outputDataTypeSelector.getDataType(selectedData);

										var nameTextInput = jQuery("#outputDataDialog #nameTextInput");
										var isValidName = event.data.view.validateMessageName(nameTextInput);
										if (isValidName) {
											event.data.view.addOutputAccessPoint(nameTextInput.val(), selectedData);
											event.data.view.resume();
											jQuery("#outputDataDialog").dialog("close");
										}
									});

					jQuery("#outputDataDialog #closeButton").click(function() {
						jQuery("#outputDataDialog").dialog("close");
					});

					// Index Configuration Dialog
					jQuery("#indexConfigurationDialog").dialog({
						autoOpen : false,
						draggable : true,
						resizable : false,
						title : m_i18nUtils
									.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.title"),
						width : 'auto'
									});

					jQuery("#idx-okButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										if (true) {
											jQuery("#indexConfigurationDialog").dialog("close");
										}
									});

					jQuery("#idx-cancelButton").click(function() {
						jQuery("#indexConfigurationDialog").dialog("close");
					});

					// Test
					jQuery("#runButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTable = jQuery("#outputDataTable");

										outputDataTable.empty();

										for ( var n = 0; n < view.outputTableRows.length; ++n) {
											var tableRow = view.outputTableRows[n];

											var outputRow = jQuery("<tr></tr>");

											outputDataTable.append(outputRow);
											outputRow.append("<td>"
													+ tableRow.path + "</td>");

											var outputData;

											if (tableRow.mappingExpression != null
													&& tableRow.mappingExpression.length != 0) {
												try {
													var functionBody = inputDataTextarea
															.val()
															+ " return "
															+ tableRow.mappingExpression
															+ ";";

													var mappingFunction = new Function(
															functionBody);

													var result = mappingFunction();

													outputData = result;
												} catch (exception) {
													outputRow
															.addClass("errorRow");

													outputData = exception;
												}
											} else {
												outputRow.addClass("emptyRow");
												outputData = m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.testProperties.noMapping");
											}

											outputRow.append("<td>"
													+ outputData + "</td>");
										}
									});

					jQuery("#resetButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;
										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTable = jQuery("#outputDataTable");

										inputDataTextarea.empty();
										outputDataTable.empty();

										var inputData = "";

										for ( var id in view.inputData) {
											var typeDeclaration = view.inputData[id];

											inputData += "var ";
											inputData += id;
											inputData += " = ";
											if (typeDeclaration != null) {
												inputData += JSON.stringify(
														typeDeclaration
																.createInstance(),
														null, 3);
											}
											else {
												inputData += '""';
											}
											inputData += ";\r\n";
										}

										inputDataTextarea.append(inputData);
									});

				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.validateMessageName = function(nameInput) {
					this.clearErrorMessages();

					nameInput.removeClass("error");

					if ((nameInput.val() == null) || (nameInput.val().trim() === "")) {
						this.errorMessages
								.push(m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.emptyName"));
					} else {
						var name = nameInput.val().trim();
						// name must be valid name according to XML rules
						try {
							jQuery.parseXML('<' + name + '/>');
						} catch (e) {
							this.errorMessages
								.push(m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.invalidName"));
						}

						// Check for duplicate message names
						for (var key in this.application.contexts) {
							var context = this.application.contexts[key];
							for ( var m = 0; m < context.accessPoints.length; ++m) {
								var accessPoint = context.accessPoints[m];

								if (name === accessPoint.id) {
									var msg = m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.duplicateName")
															.replace("{0}", name);
									this.errorMessages.push(msg);
								}
							}
						}
					}

					if (this.errorMessages.length > 0) {
						nameInput.addClass("error");
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.isPrimitive = function(tableRow) {
					if (tableRow.accessPoint.dataType == m_constants.PRIMITIVE_DATA_TYPE) return true;
					if (tableRow.element != null && m_dataTraversal.isBuiltInXsdDataType(tableRow.element.type)) return true;
				}

				/**
				 *
				 */
				// TODO: @Anoop - Refactor
				MessageTransformationApplicationView.prototype.isStructuredType = function(tableRow) {
					if (tableRow.accessPoint.dataType != m_constants.STRUCTURED_DATA_TYPE) return false;

					var element = tableRow.element;

					// Embedded structure
					if (element == null) {
						return false;
					}

					var childElements = element.elements;

					// Recursive resolution

					if (childElements == null && element.type != null) {
						var typeDeclaration = this.getModel()
								.findTypeDeclarationBySchemaName(m_model.stripElementId(element.type));

						if (typeDeclaration != null
								&& typeDeclaration.isSequence()) {
							return true;
						}
					}

					if (childElements == null) {
						return false;
					}

					return true;
				}

				// TODO: Helper methods - review code location?
				function ancestorsOf(node) {
					var ancestors = [];
					while(node = parentOf(node)) {
						ancestors[ancestors.length] = node[0];
					}
					return ancestors;
				};

				function parentOf(node) {
					var classNames = node[0].className.split(' ');

					var childPrefix = "child-of-";

					for(var key=0; key<classNames.length; key++) {
						if(classNames[key].match(childPrefix)) {
							return $(node).siblings("#" + classNames[key].substring(childPrefix.length));
						}
					}

					return null;
				};
			}
		});