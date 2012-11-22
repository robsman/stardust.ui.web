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
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_dataTraversal", "bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_codeEditor", "bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_accessPoint, m_dataTraversal, m_dialog,
				m_modelElementView, m_codeEditor, m_i18nUtils) {
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
							

			function i18nmessageTransformationproperties() {
                
				$("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));
								
				$("label[for='idOutput']")
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
				jQuery("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));
				jQuery("#sourcemessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.sourceMessage"));
				jQuery("#addInputDataButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.sourceMessage.addInput"));
				jQuery("#filterHighlightedSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.infoMsg"));
				jQuery("#element")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.element"));
				jQuery("#type")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				jQuery("#advancedMapping")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.sourceMessage.advancedMapping"));
				jQuery("#targetmessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.targetMessage"));
				jQuery("#element1")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.element"));
				jQuery("#type1")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				jQuery("#mapping")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.mapping"));
				jQuery("#problem")
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
				jQuery("#filterFieldsWithMappingInvalid")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.invalidMapping"));
				jQuery("#showAllSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.highlighted"));
				jQuery("#testdata")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.tab"));
				$("label[for='inputDataTextArea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.inputData"));
				$("label[for='outputDataTable']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.outputData"));
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
					this.inputTable = jQuery("#sourceTable");
					this.inputTableBody = jQuery("table#sourceTable tbody");
					this.sourceFilterInput = jQuery("#sourceFilterInput");
					this.targetFilterInput = jQuery("#targetFilterInput");
					this.outputTable = jQuery("#targetTable");
					this.outputTableBody = jQuery("table#targetTable tbody");
					this.expressionTextArea = jQuery("#expressionTextArea");
					this.filterFieldsWithMappingInput = jQuery("#filterFieldsWithMappingInput");
					this.filterFieldsWithNoMappingInput = jQuery("#filterFieldsWithNoMappingInput");
					this.filterHighlightedSourceFieldsInput = jQuery("#filterHighlightedSourceFieldsInput");
					this.filterHighlightedTargetFieldsInput = jQuery("#filterHighlightedTargetFieldsInput");
					this.showAllSourceFieldsInput = jQuery("#showAllSourceFieldsInput");
					this.showAllTargetFieldsInput = jQuery("#showAllTargetFieldsInput");

					this.selectedOutputTableRow = null;

					this.inputTableBody.empty();
					this.outputTableBody.empty();

					this.inputTableRows = [];
					this.outputTableRows = [];

					this.expressionEditor = m_codeEditor.getCodeEditor(jQuery("#expressionTextArea")[0]);
					this.expressionEditor.disable();

					this.sourceFilterInput.keypress({
						"view" : this
					}, function(event) {
						event.data.view
								.filterSource(event.data.view.sourceFilterInput
										.val());
					});

					this.targetFilterInput.keypress({
						"view" : this
					}, function(event) {
						event.data.view
								.filterTarget(event.data.view.targetFilterInput
										.val());
					});

					this.filterHighlightedSourceFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterHighlightedSourceFields();
					});

					this.filterHighlightedTargetFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterHighlightedTargetFields();
					});

					this.showAllSourceFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.showAllSourceFields();
					});

					this.showAllTargetFieldsInput.click({
						"view" : this
					}, function(event) {
						event.data.view.showAllTargetFields();
					});

					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);

					this.filterFieldsWithNoMappingInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterFieldsWithNoMapping();
					});

					this.filterFieldsWithMappingInput.click({
						"view" : this
					}, function(event) {
						event.data.view.filterFieldsWithMapping();
					});

					jQuery(this.expressionEditor.getWrapper())
							.droppable({
								accept : ".data-element",
								drop : function(e, ui) {
									var view = ui.draggable.data("view");
									var outputTableRow = view.selectedOutputTableRow;

									if (outputTableRow != null) {
										var mappingExpression = outputTableRow.mappingExpression;

										if (mappingExpression != null
												&& mappingExpression != "") {
											mappingExpression += " + ";
										} else {
											mappingExpression = "";
										}

										var inputTableRow = ui.draggable
												.data("tableRow")
										mappingExpression += inputTableRow.path;

										outputTableRow.mappingExpression = mappingExpression;

										view.expressionEditor.setValue(outputTableRow.mappingExpression);
										view.expressionEditor.save();

										var rowId = outputTableRow.path
												.replace(/\./g, "-");

										// Set mapping column content

										var mappingCell = jQuery("#targetTable tr#"
												+ rowId + " .mapping");

										mappingCell.empty();
										mappingCell
												.append(outputTableRow.mappingExpression);

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

					jQuery("#inputDataDialog").dialog({
						autoOpen : false,
						draggable : true,
										title : m_i18nUtils
												.getProperty("modeler.propertyView.messageTransformation.configurationProperties.addInput.popUp")
									});
					jQuery("#inputDataDialog #name")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.name"));
					jQuery("#inputDataDialog #type")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.type"));

					jQuery("#inputDataDialog #applyButton")
							.attr(
									"value",
									m_i18nUtils
											.getProperty("modeler.propertyView.messageTransformation.configurationProperties.apply"));
					jQuery("#inputDataDialog #closeButton")
							.attr(
									"value",
									m_i18nUtils
											.getProperty("modeler.propertyView.messageTransformation.configurationProperties.close"))
					  
					  
				      /*Comment*/
					jQuery("#inputDataDialog #closeButton").click(function() {
						jQuery("#inputDataDialog").dialog("close");
					});

					
					jQuery("#inputDataDialog #applyButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										event.data.view
												.addInputAccessPoint(
														jQuery(
																"#inputDataDialog #nameTextInput")
																.val(),
														m_model
																.findTypeDeclaration(jQuery(
																		"#inputDataDialog #typeSelectInput")
																		.val()));
										event.data.view.resume();
										jQuery("#inputDataDialog").dialog(
												"close");
									});

					jQuery("#addInputDataButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										var inputDataTypeSelectInput = jQuery("#inputDataDialog #typeSelectInput");
										// var models = m_model.getModels();
										//
										// for ( var m in models) {
										// var model = models[m];
										var model = event.data.view.application.model;
										// TODO: Do not show Enumerations
										for ( var n in model.typeDeclarations) {
											var typeDeclaration = model.typeDeclarations[n];
											inputDataTypeSelectInput
													.append("<option value='"
															+ typeDeclaration
																	.getFullId()
															+ "'>"
															+ typeDeclaration.name
															+ "</option>");
										}
										// }

										jQuery("#inputDataDialog").dialog(
												"open");
									});

					jQuery("#outputDataDialog").dialog({
						autoOpen : false,
						draggable : true,
										title : m_i18nUtils
												.getProperty("modeler.propertyView.messageTransformation.configurationProperties.addOutput.popUp")
									});
					jQuery("#outputDataDialog #name")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.name"));
					jQuery("#outputDataDialog #type")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.type"));

					jQuery("#outputDataDialog #applyButton")
							.attr(
									"value",
									m_i18nUtils
											.getProperty("modeler.propertyView.messageTransformation.configurationProperties.apply"));
					jQuery("#outputDataDialog #closeButton")
							.attr(
									"value",
									m_i18nUtils
											.getProperty("modeler.propertyView.messageTransformation.configurationProperties.close"));

					jQuery("#outputDataDialog #closeButton").click(function() {
						jQuery("#outputDataDialog").dialog("close");
					});

					jQuery("#outputDataDialog #applyButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										event.data.view
												.addOutputAccessPoint(
														jQuery(
																"#outputDataDialog #nameTextInput")
																.val(),
														m_model
																.findTypeDeclaration(jQuery(
																		"#outputDataDialog #typeSelectInput")
																		.val()));
										event.data.view.resume();
										jQuery("#outputDataDialog").dialog(
												"close");
									});

					jQuery("#addOutputDataButton")
							.click(
									{
										view : this
									},
									function(event) {
										var outputDataTypeSelectInput = jQuery("#outputDataDialog #typeSelectInput");

										// var models = m_model.getModels();
										//
										// for ( var m in models) {
										// var model = models[m];
										var model = event.data.view.application.model;
										for ( var n in model.typeDeclarations) {
											var typeDeclarations = model.typeDeclarations[n];
											outputDataTypeSelectInput
													.append("<option value='"
															+ typeDeclarations
																	.getFullId()
															+ "'>"
															+ typeDeclarations.name
															+ "</option>");
										}
										// }

										jQuery("#outputDataDialog").dialog(
												"open");
									});

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
												outputData = "(No Mapping)";
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
											inputData += JSON.stringify(
													typeDeclaration
															.createInstance(),
													null, 3);
											inputData += ";";
										}

										inputDataTextarea.append(inputData);
									});

					this.initializeModelElementView(application);
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.setModelElement = function(
						application) {
					this.initializeModelElement(application);

					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(application);

					this
							.convertFromMappingsXml(this.application.attributes["messageTransformation:TransformationProperty"]);

					m_utils.debug("===> Mapping Expressions");
					m_utils.debug(this.mappingExpressions);

					this.inputData = {};
					this.outputData = {};
					this.inputTableBody.empty();
					this.outputTableBody.empty();
					this.inputTableRows = [];
					this.outputTableRows = [];

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

					this.populateTableRows(this.inputTableBody,
							this.inputTableRows, true);
					this.populateTableRows(this.outputTableBody,
							this.outputTableRows, false);
					this.resume();

					// Global variables for Code Editor auto-complete / validation
					var globalVariables = {};
					var typeDeclaration;
					for (var id in this.inputData) {
						typeDeclaration = this.inputData[id];
						globalVariables[id] = typeDeclaration.createInstance();
					}

					for (var id in this.outputData) {
						typeDeclaration = this.outputData[id];
						globalVariables[id] = typeDeclaration.createInstance();
					}

					this.expressionEditor.setGlobalVariables(globalVariables);
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.convertFromMappingsXml = function(
						xml) {
					var xmlDoc = jQuery.parseXML(xml);
					var xmlObject = jQuery(xmlDoc);

					var view = this;

					jQuery(xmlObject).find("fieldMappings").each(
							function() {

								var fieldPath = jQuery(this).attr("fieldPath")

								fieldPath = fieldPath.replace(/\//g, ".");

								view.mappingExpressions[fieldPath] = jQuery(
										this).attr("mappingExpression");
							});
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.convertToMappingsXml = function() {
					var xml = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;ASCII&quot;?&gt;&#13;&#10;&lt;mapping:TransformationProperty xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:mapping=&quot;java://com.infinity.bpm.messaging.model&quot; xsi:schemaLocation=&quot;java://com.infinity.bpm.messaging.model java://com.infinity.bpm.messaging.model.mapping.MappingPackage&quot;&gt;&#13;&#10;";

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						var outputTableRow = this.outputTableRows[n];

						xml += "&lt;fieldMappings";
						xml += " fieldPath=&quot;";
						xml += outputTableRow.path;
						xml += "&quot; mappingExpression=&quot;";
						xml += outputTableRow.mappingExpression;
						xml += "&quot;/&gt;&#13;&#10;";
					}

					return xml;
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

										jQuery("table#targetTable tr.selected").removeClass(
												"selected");
										jQuery(this).addClass("selected");

										view.selectedOutputTableRow = jQuery(
												this).data("tableRow");

										jQuery("#elementIndicatorText").empty();
										jQuery("#elementIndicatorText")
												.append(
														view.selectedOutputTableRow.path
																+ " = ");

										view.expressionEditor.enable();
										view.expressionEditor
												.setValue(view.selectedOutputTableRow.mappingExpression);
										view.expressionEditor.save();
										view.expressionEditor.setJSValidationPrefix(view.selectedOutputTableRow.path + " = ");
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
				MessageTransformationApplicationView.prototype.addInputAccessPoint = function(
						dataName, dataStructure) {
					this.application.contexts.application.accessPoints.push(m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.IN_ACCESS_POINT));

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
					m_utils.debug("Access Point:");
					m_utils.debug(accessPoint);

					// TODO Move to m_accessPoint
					var typeDeclaration = m_accessPoint
							.retrieveTypeDeclaration(accessPoint, this
									.getModel());

					if (typeDeclaration == null) {
						this.errorMessages
								.push("Unsupported Type for Source Message Element "
										+ accessPoint.name + ".");
						this.showErrorMessages();

						return;
					}

					this.inputData[accessPoint.id] = typeDeclaration;

					this.initializeInputTableRowsRecursively(accessPoint,
							typeDeclaration.getBody(), null,
							typeDeclaration.model);
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.addOutputAccessPoint = function(
						dataName, dataStructure) {
					this.application.contexts.application.accessPoints.push(m_accessPoint
							.createFromDataStructure(dataStructure, dataName,
									dataName, m_constants.OUT_ACCESS_POINT));

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
					// TODO Move to m_accessPoint
					var typeDeclaration = m_accessPoint
							.retrieveTypeDeclaration(accessPoint, this
									.getModel());

					if (typeDeclaration == null) {
						this.errorMessages
								.push("Unsupported Type for Target Message Element "
										+ accessPoint.name + ".");
						this.showErrorMessages();

						return;
					}

					this.outputData[accessPoint.id] = typeDeclaration;

					this.initializeOutputTableRowsRecursively(accessPoint,
							typeDeclaration.getBody(), null,
							typeDeclaration.model);
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
					tableRow.typeName = parentPath == null ? m_accessPoint
							.retrieveTypeDeclaration(accessPoint,
									this.getModel()).getSchemaName()
							: element.type;

					// Embedded structure

					var childElements = element.elements;

					// Recursive resolution

					if (childElements == null && element.type != null) {
						var typeDeclaration = scopeModel
								.findTypeDeclarationBySchemaName(element.type);

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
					tableRow.typeName = parentPath == null ? m_accessPoint
							.retrieveTypeDeclaration(accessPoint,
									this.getModel()).getSchemaName()
							: element.type;
					tableRow.mappingExpression = this.mappingExpressions[path] == null ? ""
							: this.mappingExpressions[path];
					tableRow.problems = "";

					// Embedded structure

					var childElements = element.elements;

					// Recursive resolution

					if (childElements == null && element.type != null) {
						var typeDeclaration = scopeModel
								.findTypeDeclarationBySchemaName(element.type);

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

						content += "<td>";
						content += "<span class=\"data-element\">"
								+ tableRows[tableRow].name + "</span>";
						content += "</td>";
						content += "<td>" + tableRows[tableRow].typeName;
						+"</td>";

						if (source) {
							content += "</tr>";

							tableBody.append(content);

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
							content += "<td class=\"mapping\">";
							content += tableRows[tableRow].mappingExpression;
							content += "<td/>";
							content += "<td>";
							content += "</td>";
							content += "</tr>";

							tableBody.append(content);

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
											var outputTableRow = tableRows[tableRow];
											// jQuery(this)
											// .data("tableRow");
											var inputTableRow = ui.draggable
													.data("tableRow");

											outputTableRow.mappingExpression = inputTableRow.path;

											var mappingCell = jQuery(this)
													.children(".mapping");
											mappingCell.empty();
											mappingCell
													.append(outputTableRow.mappingExpression);

											// Update expression text
											// area if needed

											if (view.selectedOutputTableRow == outputTableRow) {
												view.expressionEditor
														.setValue(outputTableRow.mappingExpression);
												view.expressionEditor.save();
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
				MessageTransformationApplicationView.prototype.filterFieldsWithNoMapping = function() {
					jQuery("table#targetTable tbody tr").addClass("invisible");
					m_utils
							.debug(jQuery("table#targetTable tbody tr .mapping:empty"));
					jQuery("table#targetTable tbody tr .mapping:empty")
							.parent().removeClass("invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMapping = function() {
					jQuery("table#targetTable tbody tr").addClass("invisible");
					m_utils
							.debug(jQuery("table#targetTable tbody tr .mapping:not(:empty)"));
					jQuery("table#targetTable tbody tr .mapping:not(:empty)")
							.parent().removeClass("invisible");
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
				MessageTransformationApplicationView.prototype.determineTransformationChanges = function() {
					var transformationProperty = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;ASCII&quot;?&gt;&#13;&#10;&lt;mapping:TransformationProperty xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:mapping=&quot;java://com.infinity.bpm.messaging.model&quot; xsi:schemaLocation=&quot;java://com.infinity.bpm.messaging.model java://com.infinity.bpm.messaging.model.mapping.MappingPackage&quot;&gt;&#13;&#10;";

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						var outputTableRow = this.outputTableRows[n];

						transformationProperty += "&lt;fieldMappings";
						transformationProperty += " fieldPath=&quot;";
						transformationProperty += outputTableRow.path;
						transformationProperty += "&quot; mappingExpression=&quot;";
						transformationProperty += outputTableRow.mappingExpression;
						transformationProperty += "&quot;/&gt;&#13;&#10;";
					}

					transformationProperty += ";&lt;/mapping:TransformationProperty&gt;&#13;&#10;";

					return {
						attributes : {
							"messageTransformation:TransformationProperty" : transformationProperty
						}
					};
				};

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