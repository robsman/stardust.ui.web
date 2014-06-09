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
				"bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_codeEditorAce", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_parameterDefinitionsPanel","bpm-modeler/js/m_parsingUtils","bpm-modeler/js/m_autoCompleters", "bpm-modeler/js/m_typeDeclaration"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_model, m_accessPoint, m_dataTypeSelector, m_dataTraversal, m_dialog,
				m_modelElementView, m_codeEditorAce, m_i18nUtils, m_parameterDefinitionsPanel,
				m_parsingUtils,m_autoCompleters, m_typeDeclaration) {
			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					m_utils.jQuerySelect("#hideGeneralProperties").hide();
					initViewCollapseClickHandlers();

					var view = new MessageTransformationApplicationView();
					i18nmessageTransformationproperties();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
					m_utils.hideWaitCursor();
					}
				};

			/**
			 *
			 */
			function initViewCollapseClickHandlers() {
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
				m_utils.jQuerySelect("#showAllProperties").hide();
				m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
				m_utils.jQuerySelect("#showAllProperties").show();
				m_utils.jQuerySelect("#hideGeneralProperties").hide();
				});
			}

			/**
			 *
			 */
			function i18nmessageTransformationproperties() {

				// Common properties
				m_utils.jQuerySelect("#hideGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

				m_utils.jQuerySelect("#showGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));


				m_utils.jQuerySelect("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));
				m_utils.jQuerySelect("#application")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));

				// Configuration
				m_utils.jQuerySelect("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));

				// Configuration - Source
				m_utils.jQuerySelect("#sourcemessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.sourceMessage"));
				m_utils.jQuerySelect("#inputElementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.element"));
				m_utils.jQuerySelect("#inputTypeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				m_utils.jQuerySelect("#filterHighlightedSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.infoMsg"));
				m_utils.jQuerySelect("#showAllSourceFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.highlighted"));

				// Configuration - Target
				m_utils.jQuerySelect("#targetmessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.heading.targetMessage"));
				m_utils.jQuerySelect("#outputElementColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.element"));
				m_utils.jQuerySelect("#outputTypeColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.type"));
				m_utils.jQuerySelect("#mappingColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.mapping"));
				m_utils.jQuerySelect("#problemsColumn")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.problem"));
				m_utils.jQuerySelect("#filterFieldsWithMappingInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.mapping"));
				m_utils.jQuerySelect("#filterFieldsWithNoMappingInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.noMapping"));
				m_utils.jQuerySelect("#filterHighlightedTargetFieldsInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.infoMsg"));
				m_utils.jQuerySelect("#filterFieldsWithMappingInvalidInput")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.toolTip.invalidMapping"));

				// Code Editor
				m_utils.jQuerySelect("#advancedMapping")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.sourceMessage.advancedMapping"));

				// Test
				m_utils.jQuerySelect("#testdata")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.tab"));
				m_utils.jQuerySelect("#runButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.runButton"));
				m_utils.jQuerySelect("#resetButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.resetButton"));
				m_utils.jQuerySelect("label[for='inputDataTextArea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.inputData"));
				m_utils.jQuerySelect("label[for='outputDataTable']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.testProperties.outputData"));
				m_utils.jQuerySelect("#paramDef")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.parameterDefinitions"));

				// Data Type Selector
				m_utils.jQuerySelect("label[for='dataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.datatType"));
				m_utils.jQuerySelect("label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				m_utils.jQuerySelect("label[for='structuredDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.structuredType"));
				m_utils.jQuerySelect("label[for='nameTextInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));

				// Index Configuration pop-up dialog
				m_utils.jQuerySelect("#idx-sourcemessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.sourceMessage"));
				m_utils.jQuerySelect("#idx-targetmessage")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.targetMessage"));
				m_utils.jQuerySelect("#idx-sourceTable-name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#idx-sourceTable-index")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.index"));
				m_utils.jQuerySelect("#idx-targetTable-name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#idx-targetTable-index")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.index"));
				m_utils.jQuerySelect("label[for='idx-showAffectedTreePaths']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.showAffectedPaths"));
				m_utils.jQuerySelect("#idx-mappingStrategy")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.mappingStrategy"));
				m_utils.jQuerySelect("label[for='idx-mappingStrategy-append']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.append"));
				m_utils.jQuerySelect("label[for='idx-mappingStrategy-overwrite']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.overwrite"));
				m_utils.jQuerySelect("#idx-okButton")
						.prop('value',
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.ok"));
				m_utils.jQuerySelect("#idx-cancelButton")
						.prop('value',
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.cancel"));

				var primitiveDataTypeSelect = m_utils.jQuerySelect("#primitiveDataTypeSelect");
				var selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
				primitiveDataTypeSelect.append("<option value=\"String\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
				primitiveDataTypeSelect.append("<option value=\"boolean\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
				primitiveDataTypeSelect.append("<option value=\"int\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
				primitiveDataTypeSelect.append("<option value=\"long\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
				primitiveDataTypeSelect.append("<option value=\"double\">"
						+ selectdata + "</option>");

				// Commented as we don't support Money values yet.
				// selectdata = m_i18nUtils
				// .getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
				// primitiveDataTypeSelect.append("<option value=\"Decimal\">"
				// + selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
				primitiveDataTypeSelect.append("<option value=\"Calendar\">"
						+ selectdata + "</option>");

				m_utils.jQuerySelect("label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));

				var parameterDefinitionDirectionSelect = m_utils.jQuerySelect("#parameterDefinitionDirectionSelect");

				selectdata = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.in");
				parameterDefinitionDirectionSelect
						.append("<option value=\"IN\">" + selectdata
								+ "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.out");
				parameterDefinitionDirectionSelect
						.append("<option value=\"OUT\">" + selectdata
								+ "</option>");
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
					this.view = m_utils.jQuerySelect("#" + this.id);
					this.inputData = {};
					this.outputData = {};
					this.mappingExpressions = {};
					this.advancedMappings = {};
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.inputTable = m_utils.jQuerySelect("#sourceTable");
					this.inputTableBody = m_utils.jQuerySelect("table#sourceTable tbody");
					this.sourceFilterInput = m_utils.jQuerySelect("#sourceFilterInput");
					this.targetFilterInput = m_utils.jQuerySelect("#targetFilterInput");
					this.outputTable = m_utils.jQuerySelect("#targetTable");
					this.outputTableBody = m_utils.jQuerySelect("table#targetTable tbody");
					this.filterFieldsWithMappingInput = m_utils.jQuerySelect("#filterFieldsWithMappingInput");
					this.filterFieldsWithNoMappingInput = m_utils.jQuerySelect("#filterFieldsWithNoMappingInput");
					this.filterHighlightedSourceFieldsInput = m_utils.jQuerySelect("#filterHighlightedSourceFieldsInput");
					this.filterHighlightedTargetFieldsInput = m_utils.jQuerySelect("#filterHighlightedTargetFieldsInput");
					this.filterFieldsWithMappingInvalidInput = m_utils.jQuerySelect("#filterFieldsWithMappingInvalidInput");
					this.showAllSourceFieldsInput = m_utils.jQuerySelect("#showAllSourceFieldsInput");
					this.showAllTargetFieldsInput = m_utils.jQuerySelect("#showAllTargetFieldsInput");

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
					.create({
						scope : "messageTransformationApplicationView",
						submitHandler : this,
						supportsOrdering : false,
						supportsDataMappings : false,
						supportsDescriptors : false,
						supportsDataTypeSelection : true,
						hideEnumerations : true,
						supportsDocumentTypes : false,
						tableWidth : "500px",
						directionColumnWidth : "50px",
						nameColumnWidth : "250px",
						typeColumnWidth : "200px"
					});

					this.filterFieldsWithMappingInput.data('enabled', false);
					this.filterFieldsWithNoMappingInput.data('enabled', false);
					this.filterFieldsWithMappingInvalidInput.data('enabled', false);

					this.selectedOutputTableRow = null;

					this.inputTableBody.empty();
					this.outputTableBody.empty();

					this.inputTableRows = [];
					this.outputTableRows = [];

					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);

					this.advancedMappingCheckbox = m_utils.jQuerySelect("#advancedMappingCheckbox");
					this.advancedMappingCheckbox.attr("disabled", true);

					this.editorAnchor = m_utils.jQuerySelect("#expressionTextDiv").get(0);
					this.editorAnchor.id = "expressionText" + Math.floor((Math.random()*100000) + 1) + "Div";
					this.expressionEditor = m_codeEditorAce.getJSCodeEditor(this.editorAnchor.id);
					this.expressionEditor.disable();
					var that=this;
					$(this.expressionEditor).on("moduleLoaded",function(event,module){
						var sessionCompleter;
						if(module.name==="ace/ext/language_tools"){
							sessionCompleter=m_autoCompleters.getSessionCompleter();
							that.expressionEditor.addCompleter(sessionCompleter);
						}
					});
					this.expressionEditor.loadLanguageTools();

					this.bindEventHandlers();

					this.initializeModelElementView(application);
					this.view.css("visibility", "visible");
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
						inputRowExpandedStatus[this.id] = m_utils.jQuerySelect(this).hasClass("expanded")
					});
					this.outputTableBody.find("tr").each(function(index) {
						outputRowExpandedStatus[this.id] = m_utils.jQuerySelect(this).hasClass("expanded")
					});

					// Store the table rows that are selected
					var selectedInputRowId = m_utils.jQuerySelect("table#sourceTable tr.selected").first().attr('id');
					var selectedOutputRowId = m_utils.jQuerySelect("table#targetTable tr.selected").first().attr('id');

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
					this.advancedMappings = {};
					this.inputTableBody.empty();
					this.outputTableBody.empty();
					this.inputTableRows = [];
					this.outputTableRows = [];
					this.selectedOutputTableRow = null;
					m_utils.jQuerySelect("#elementIndicatorText").empty();
					this.expressionEditor.setValue("");
					this.expressionEditor.disable();

					this.parameterDefinitionsPanel
							.setScopeModel(this.application.model);
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.application.contexts["application"].accessPoints);

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
							m_utils.jQuerySelect(this).addClass("expanded");
							m_utils.jQuerySelect(this).removeClass("ui-helper-hidden");
						}
					});
					this.outputTableBody.find("tr").each(function(index) {
						if (outputRowExpandedStatus[this.id]) {
							m_utils.jQuerySelect(this).addClass("expanded");
							m_utils.jQuerySelect(this).removeClass("ui-helper-hidden");
						}
					});

					// Restore the selected tree nodes
					m_utils.jQuerySelect("#sourceTable #" + selectedInputRowId).addClass("selected");
					m_utils.jQuerySelect("#targetTable #" + selectedOutputRowId).addClass("selected");

					this.selectedOutputTableRow = m_utils.jQuerySelect("#targetTable tr.selected").data("tableRow");

					// Initialize the state of the code editor
					if (this.selectedOutputTableRow != null) {
						m_utils.jQuerySelect("#elementIndicatorText").append(this.selectedOutputTableRow.path + " = ");
						this.expressionEditor.setValue(this.selectedOutputTableRow.mappingExpression);
						this.expressionEditor.enable();
					}

					// Global variables for Code Editor auto-complete / validation
					//var globalVariables = {};
					var completerStrings=[];
					var typeDeclaration;
					for (var id in this.inputData) {
						typeDeclaration = this.inputData[id];
						if (typeDeclaration) {
							/*Structured type branch*/
							completerStrings=completerStrings.concat(m_parsingUtils.parseTypeToStringFrags(typeDeclaration,id));
						}
						else{
							/*Primitive type branch*/
							completerStrings=completerStrings.concat([id]);
						}
					}

					for (var id in this.outputData) {
						typeDeclaration = this.outputData[id];
						if (typeDeclaration) {
							/*Structured type branch*/
							completerStrings=completerStrings.concat(m_parsingUtils.parseTypeToStringFrags(typeDeclaration,id));
						}
						else{
							/*Primitive type branch*/
							completerStrings=completerStrings.concat([id]);
						}
					}
					this.expressionEditor.setSessionData("$keywordList",completerStrings);
					var that=this;




					// TODO - these things below were possible with CodeMirror editor out of box
					// But not in case of Ace editor hence temporarily commented out

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
						height : 400
					});
					this.inputTable.treeTable({
						indent: 14
					});

					this.outputTable.tableScroll({
						height : 400
					});
					this.outputTable.treeTable({
						indent: 14
					});

					m_utils.jQuerySelect("table#sourceTable tbody tr").mousedown({
						"view" : this
					}, function() {
						m_utils.jQuerySelect("table#sourceTable tr.selected").removeClass("selected");
						m_utils.jQuerySelect(this).addClass("selected");
					});

					m_utils.jQuerySelect("table#sourceTable tbody tr span").mousedown(
							function() {
								m_utils.jQuerySelect(m_utils.jQuerySelect(this).parents("tr")[0]).trigger(
										"mousedown");
							});

					m_utils.jQuerySelect("table#targetTable tbody tr")
							.mousedown(
									function() {
										var view = m_utils.jQuerySelect(this).data("view");

										var self = this;

										// Using setTimeout so that blur event of code editor (if applicable) is called first
										setTimeout(function() {
											m_utils.jQuerySelect("table#targetTable tr.selected").removeClass(
													"selected");
											m_utils.jQuerySelect(self).addClass("selected");

											view.selectedOutputTableRow = m_utils.jQuerySelect(
													self).data("tableRow");

											view.advancedMappingCheckbox.removeAttr("disabled");
											if (view.selectedOutputTableRow.advancedMapping) {
												view.advancedMappingCheckbox.attr("checked", true);
												m_utils.jQuerySelect("#elementIndicatorText").empty();
											} else {
												view.advancedMappingCheckbox.attr("checked", false);
												m_utils.jQuerySelect("#elementIndicatorText").empty();
												m_utils.jQuerySelect("#elementIndicatorText")
														.append(
																view.selectedOutputTableRow.path
																		+ " = ");
											}

											view.expressionEditor.enable();
											view.expressionEditor
													.setValue(view.selectedOutputTableRow.mappingExpression);

											// TODO - these things below were possible with CodeMirror editor out of box
											// But not in case of Ace editor hence temporarily commented out

											// Register showMappingError as a callback function after JS validation occurs
											//view.expressionEditor.setJavaScriptValidationOptions(view.showMappingError, view.selectedOutputTableRow.path);
										}, 0);
									});

					m_utils.jQuerySelect("table#targetTable tbody tr span").mousedown(
							function() {
								m_utils.jQuerySelect(m_utils.jQuerySelect(this).parents("tr")[0]).trigger(
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
						var childElementsArray = [];
						m_utils.insertArrayAt(childElementsArray, typeDeclaration.getBody());
						m_utils.insertArrayAt(childElementsArray, typeDeclaration.getTypeDeclaration().attributes);
						this.inputData[accessPoint.id] = typeDeclaration;
						this.initializeTableRowsRecursively(false, accessPoint,
								childElementsArray, null,
								typeDeclaration.model);
					}
					else { // accessPoint.dataType === m_constants.PRIMITIVE_DATA_TYPE
						this.inputData[accessPoint.id] = null; // TODO
						this.initializeTableRowsRecursively(false, accessPoint, null, null, null);
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
						var childElementsArray = [];
						m_utils.insertArrayAt(childElementsArray, typeDeclaration.getBody());
						m_utils.insertArrayAt(childElementsArray, typeDeclaration.getTypeDeclaration().attributes);
						this.initializeTableRowsRecursively(true, accessPoint,
								childElementsArray, null,
								typeDeclaration.model);
					}
					else { // accessPoint.dataType === m_constants.PRIMITIVE_DATA_TYPE
						this.inputData[accessPoint.id] = null; // TODO
						this.initializeTableRowsRecursively(true, accessPoint, null, null, null);
					}
				};


				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					// Context is regenerated on the server - hence, all data
					// need to be provided

					this.submitChanges({
						contexts : {
							application : {
								accessPoints : parameterDefinitionsChanges
							}
						}
					});
				};

				/**
				 * This method handles both Input and Output data Mapping
				 */
				MessageTransformationApplicationView.prototype.initializeTableRowsRecursively = function(
						output, accessPoint, element, parentPath, scopeModel,
						elementName, elementType, schema) {

					elementName = elementName ? elementName
							: element ? element.name : null;
					elementType = elementType ? elementType
							: element ? element.type : null;

					var path = parentPath == null ? accessPoint.id
							: (parentPath + "." + elementName);

					var tableRow = {};

					tableRow.accessPoint = accessPoint;
					tableRow.element = element;
					tableRow.path = path;
					tableRow.parentPath = parentPath;
					tableRow.name = parentPath == null ? accessPoint.id
							: elementName;

					tableRow.typeName = parentPath == null ? (accessPoint.dataType == m_constants.STRUCTURED_DATA_TYPE ? m_accessPoint
							.retrieveTypeDeclaration(accessPoint, this
									.getModel()).name
							: accessPoint.primitiveDataType)
							: elementType;

					// Assign the element name as type name assuming this is an element
					// with anonymous nested type
					tableRow.typeName = tableRow.typeName ? tableRow.typeName : elementName;

					if (output) {
						// for output mapping
						tableRow.mappingExpression = this.mappingExpressions[path] == null ? ""
								: this.mappingExpressions[path];
						tableRow.advancedMapping = this.advancedMappings[path];
						tableRow.problems = "";

						this.outputTableRows.push(tableRow);
					} else {
						// for input mapping
						this.inputTableRows.push(tableRow);
					}

					// Embedded structure
					if (element == null) {
						return;
					}

					if (element.length > 0) {
						for ( var elmnt in element) {
							if (element[elmnt].classifier === "attribute") {
								this.initializeTableRowsRecursively(output,
										accessPoint, element[elmnt], path,
										scopeModel);
							}
							var childElements = element[elmnt].body;
							if (childElements == null) {
								continue;
							}

							for ( var index in childElements) {
								var childElement = childElements[index];
								if (childElement.attributes || childElement.body) {
									var childElementsArray = [];
									m_utils.insertArrayAt(childElementsArray, childElement.body);
									m_utils.insertArrayAt(childElementsArray, childElement.attributes);
									this.initializeTableRowsRecursively(output,
											accessPoint, childElementsArray,
											path, scopeModel,
											childElement.name,
											childElement.type);

								} else {
									// TODO - review
									var typeDeclaration = m_accessPoint.retrieveTypeDeclaration(accessPoint, this.getModel());
									var schemaType = typeDeclaration.asSchemaType();
									var childSchemaType = schemaType.resolveElementTypeFromElement(childElement);
									if (!childSchemaType && schema) {
										childSchemaType = schema.resolveElementTypeFromElement(childElement);
									}
									if (childSchemaType && childSchemaType.isStructure()) {
										var childElementsArray = [];
										m_utils.insertArrayAt(childElementsArray, childSchemaType.type.body);
										m_utils.insertArrayAt(childElementsArray, childSchemaType.getAttributes());
										var elemName = childElement.name ? childElement.name : (childSchemaType.name ? m_typeDeclaration.parseQName(childSchemaType.name).name : "");
										this.initializeTableRowsRecursively(output, accessPoint,
												childElementsArray, path,
												childSchemaType.model, elemName,
												((childSchemaType.type && childSchemaType.type.type) ? childSchemaType.type.type : childSchemaType.name), childSchemaType);
									} else {
										this.initializeTableRowsRecursively(output,
												accessPoint, childElement, path,
												scopeModel);
									}
								}
							}
						}
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
								.replace(/[:<>\.]/g, "-");

						var content = "<tr id=\""
								+ rowId
								+ "\" "
								+ (tableRows[tableRow].parentPath != null ? ("class=\"child-of-"
										+ tableRows[tableRow].parentPath
												.replace(/[:<>\.]/g, "-") + "\"")
										: "") + ">";

						content += "<td class='elementCell'>";
						content += "<span class=\"data-element\">"
								+ tableRows[tableRow].name + "</span>";
						content += "</td>";
						content += "<td class='typeCell'>" + tableRows[tableRow].typeName;
						+"</td>";

						if (source) {
							content += "</tr>";
							tableBody.append(content);

							var dataElement = m_utils.jQuerySelect("#sourceTable #" + rowId
									+ " .data-element");

							dataElement.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});

							if (this.getModelElement()
									&& !this.getModelElement().isReadonly()) {
								dataElement.draggable({
									helper : "clone",
									opacity : .75,
									refreshPositions : true,
									revert : "invalid",
									revertDuration : 300,
									scroll : true
								});
							}

							var row = m_utils.jQuerySelect("#sourceTable #" + rowId);

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view.clearErrorMessages();
								event.data.view
										.highlightSource(event.data.tableRow);
							});

						} else {
							var clearMappingTooltip = m_i18nUtils.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.targetMessage.clearMapping");
							content += "<td class='mappingCell' />";
							content += "<td class='problemCell' />";
							content += "<td class='outputActionsCell'>";
							content += "<div class='clearMappingAction' title='" + clearMappingTooltip + "'></div>";
							content += "</td>";
							content += "</tr>";

							tableBody.append(content);

							this.populateMappingCell(tableRows[tableRow]);

							// Add click event handler for "clearMapping" action
							var clearMappingIcon = m_utils.jQuerySelect("#targetTable #" + rowId + " .clearMappingAction");
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

							var row = m_utils.jQuerySelect("#targetTable #" + rowId);
							row.data({
								"view" : this,
								"tableRow" : tableRows[tableRow]
							});

							if (this.getModelElement()
									&& !this.getModelElement().isReadonly()) {
								row
										.droppable({
											accept : ".data-element",
											drop : function(e, ui) {
												var view = m_utils.jQuerySelect(this)
														.data("view");
												var outputTableRow = m_utils.jQuerySelect(this).data("tableRow");
												var inputTableRow = ui.draggable
														.data("tableRow");

												// TODO: @Anoop - Refactor
												// invalid operations
												view.clearErrorMessages();
												var valid = true;
												if (view
														.isStructuredType(inputTableRow)
														&& !view
																.isStructuredType(outputTableRow)) {
													valid = false;
												}else if (!view
														.isStructuredType(inputTableRow)
														&& view
																.isStructuredType(outputTableRow)) {
													valid = false;
												} else if (view
														.isStructuredType(inputTableRow)
														&& view
																.isStructuredType(outputTableRow)
														&& (inputTableRow.typeName != outputTableRow.typeName)) {
													valid = false;
												}

												if (valid == false) {
													view.errorMessages
															.push(m_i18nUtils
																	.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.errorMessage.differentMappingTypes"));
													view.showErrorMessages();
													return;
												}

												if (view.isStructuredType(inputTableRow) && view.isStructuredType(outputTableRow) &&
														(inputTableRow.typeName === outputTableRow.typeName)) {
													var prefix = outputTableRow.path + ".";
													for (var n = 0; n < view.outputTableRows.length; ++n) {
														if (view.outputTableRows[n].path.indexOf(prefix) == 0) {
															if (view.isPrimitive(view.outputTableRows[n])) {
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
							}

							row.click({
								"view" : this,
								"tableRow" : tableRow
							}, function(event) {
								event.data.view.clearErrorMessages();
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
					m_utils.jQuerySelect("#sourceTable .data-element").removeClass(
							"highlighted");
					m_utils.jQuerySelect("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.outputTableRows.length; ++n) {
						if (this.outputTableRows[n].mappingExpression
								.indexOf(this.inputTableRows[tableRow].path) != -1) {
							m_utils.jQuerySelect(
									"#targetTable #"
											+ this.outputTableRows[n].path
													.replace(/[:<>\.]/g, "-")
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
					m_utils.jQuerySelect("#sourceTable .data-element").removeClass(
							"highlighted");
					m_utils.jQuerySelect("#targetTable .data-element").removeClass(
							"highlighted");

					for ( var n = 0; n < this.inputTableRows.length; ++n) {
						if (this.outputTableRows[tableRow].mappingExpression
								.indexOf(this.inputTableRows[n].path) != -1) {
							m_utils.jQuerySelect(
									"#sourceTable #"
											+ this.inputTableRows[n].path
													.replace(/[:<>\.]/g, "-")
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
					filter = filter.trim();
					if (filter == null || filter == "") {
						m_utils.jQuerySelect("table#sourceTable tbody tr").removeClass(
								"invisible");
					} else {
						m_utils.jQuerySelect("table#sourceTable tbody tr").addClass(
								"invisible");

						m_utils.jQuerySelect("table#sourceTable tbody tr").each(function() {
							var element = m_utils.jQuerySelect(this).find(".data-element");
							if( element && !(element.html().toLowerCase().indexOf(filter.toLowerCase()) === -1)) {
								m_utils.jQuerySelect(this).removeClass("invisible");
								m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(this))).removeClass("invisible");
						    }
						});
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterTarget = function(
						filter) {
					filter = filter.trim();
					if (filter == null || filter == "") {
						m_utils.jQuerySelect("table#targetTable tbody tr").removeClass(
								"invisible");
					} else {
						m_utils.jQuerySelect("table#targetTable tbody tr").addClass(
								"invisible");

						m_utils.jQuerySelect("table#targetTable tbody tr").each(function() {
							var element = m_utils.jQuerySelect(this).find(".data-element");
							if( element && !(element.html().toLowerCase().indexOf(filter.toLowerCase()) === -1)) {
								m_utils.jQuerySelect(this).removeClass("invisible");
								m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(this))).removeClass("invisible");
						    }
						});

					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithNoMapping = function(enabled) {
					if (enabled) {
						m_utils.jQuerySelect("table#targetTable tbody tr").addClass("invisible");
						m_utils.jQuerySelect("table#targetTable tbody tr .mappingCell:empty").parent().each(function() {
							m_utils.jQuerySelect(this).removeClass("invisible");
							m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(this))).removeClass("invisible");
						});
					}
					else {
						m_utils.jQuerySelect("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMapping = function(enabled) {
					if (enabled) {
						m_utils.jQuerySelect("table#targetTable tbody tr").addClass("invisible");
						m_utils.jQuerySelect("table#targetTable tbody tr .mappingCell:not(:empty)").parent().each(function() {
							m_utils.jQuerySelect(this).removeClass("invisible");
							m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(this))).removeClass("invisible");
						});
					}
					else {
						m_utils.jQuerySelect("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterFieldsWithMappingInvalid = function(enabled) {
					if (enabled) {
						m_utils.jQuerySelect("table#targetTable tbody tr").addClass("invisible");
						m_utils.jQuerySelect("table#targetTable tbody tr .mappingError").parent().each(function() {
							m_utils.jQuerySelect(this).removeClass("invisible");
							m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(this))).removeClass("invisible");
						});
					}
					else {
						m_utils.jQuerySelect("table#targetTable tbody tr").removeClass("invisible");
					}
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedSourceFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeVisible(this.showAllSourceFieldsInput);

					m_utils.jQuerySelect("table#sourceTable tbody tr").addClass("invisible");
					m_utils.jQuerySelect("table#sourceTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.filterHighlightedTargetFields = function() {
					m_dialog
							.makeInvisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeVisible(this.showAllTargetFieldsInput);

					m_utils.jQuerySelect("table#targetTable tbody tr").addClass("invisible");
					m_utils.jQuerySelect("table#targetTable tbody tr .highlighted").parent()
							.parent().removeClass("invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.showAllSourceFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedSourceFieldsInput);
					m_dialog.makeInvisible(this.showAllSourceFieldsInput);
					m_utils.jQuerySelect("table#sourceTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.showAllTargetFields = function() {
					m_dialog
							.makeVisible(this.filterHighlightedTargetFieldsInput);
					m_dialog.makeInvisible(this.showAllTargetFieldsInput);
					m_utils.jQuerySelect("table#targetTable tbody tr").removeClass(
							"invisible");
				};

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.populateMappingCell = function(outputTableRow) {
					var maxLength = 35;

					var rowId = outputTableRow.path.replace(/[:<>\.]/g, "-");
					var mappingCell = m_utils.jQuerySelect("#targetTable tr#" + rowId + " .mappingCell");
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
					var rowId = path.replace(/[:<>\.]/g, "-");
					var problemCell = m_utils.jQuerySelect("#targetTable tr#" + rowId + " .problemCell");
					var mappingCell = m_utils.jQuerySelect("#targetTable tr#" + rowId + " .mappingCell");

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
					m_utils.jQuerySelect("#indexConfigurationDialog").dialog("open");

					// Note: tableScroll must be added after the dialog is visible for correct behavior
					var indexConfigSourceTable = m_utils.jQuerySelect("#idx-sourceTable");
					var indexConfigTargetTable = m_utils.jQuerySelect("#idx-targetTable");

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

					var dragSource = m_utils.jQuerySelect("#idx-sourceTable tbody tr#n-New1-one");
					var dropTarget = m_utils.jQuerySelect("#idx-targetTable tbody tr#n2-New2-three");

					// Make the affected nodes highlighted
					m_utils.jQuerySelect(dragSource).find(".data-element").addClass("highlighted");
					m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dragSource))).find(".data-element").addClass("highlighted");

					// Make the affected nodes highlighted
					m_utils.jQuerySelect(dropTarget).find(".data-element").addClass("highlighted");
					m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dropTarget))).find(".data-element").addClass("highlighted");

					// Make all rows invisible
					m_utils.jQuerySelect("#idx-sourceTable tbody tr").addClass("invisible");
					// Make the affected nodes visible
					m_utils.jQuerySelect(dragSource).removeClass("invisible");
					m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dragSource))).removeClass("invisible");

					// Make all rows invisible
					m_utils.jQuerySelect("#idx-targetTable tbody tr").addClass("invisible");
					// Make the affected nodes visible
					m_utils.jQuerySelect(dropTarget).removeClass("invisible");
					m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dropTarget))).removeClass("invisible");
				}

				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.initializeIndexConfigurationDialog = function() {
					var indexConfigSourceTable = m_utils.jQuerySelect("#idx-sourceTable");
					var indexConfigTargetTable = m_utils.jQuerySelect("#idx-targetTable");

					indexConfigSourceTable.tableScroll("undo");
					indexConfigTargetTable.tableScroll("undo");

					this.populateIndexConfigurationTableRows(m_utils.jQuerySelect("#idx-sourceTable tbody"), this.inputTableRows);
					this.populateIndexConfigurationTableRows(m_utils.jQuerySelect("#idx-targetTable tbody"), this.outputTableRows);

					m_utils.jQuerySelect("#idx-sourceTable tbody tr").mousedown(
							function() {
								m_utils.jQuerySelect("#idx-sourceTable tr.selected").removeClass("selected");
								m_utils.jQuerySelect(this).addClass("selected");
							});

					m_utils.jQuerySelect("#idx-targetTable tbody tr").mousedown(
							function() {
								m_utils.jQuerySelect("#idx-targetTable tr.selected").removeClass("selected");
								m_utils.jQuerySelect(this).addClass("selected");
							});

					m_utils.jQuerySelect("#idx-showAffectedTreePaths").click(
							function() {
								var dragSource = m_utils.jQuerySelect("#idx-sourceTable tbody tr#n-New1-one");
								var dropTarget = m_utils.jQuerySelect("#idx-targetTable tbody tr#n2-New2-three");

								if (m_utils.jQuerySelect(this).is(':checked')) {
									// Make all rows invisible
									m_utils.jQuerySelect("#idx-sourceTable tbody tr").addClass("invisible");
									// Make the affected node visible
									m_utils.jQuerySelect(dragSource).removeClass("invisible");
									m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dragSource))).removeClass("invisible");

									// Make all rows invisible
									m_utils.jQuerySelect("#idx-targetTable tbody tr").addClass("invisible");
									// Make the affected nodes visible
									m_utils.jQuerySelect(dropTarget).removeClass("invisible");
									m_utils.jQuerySelect(ancestorsOf(m_utils.jQuerySelect(dropTarget))).removeClass("invisible");
								}
								else {
									// Make all rows visible
									m_utils.jQuerySelect("#idx-sourceTable tbody tr").removeClass("invisible");
									m_utils.jQuerySelect("#idx-targetTable tbody tr").removeClass("invisible");
								}
							});
				}
				/**
				 *
				 */
				MessageTransformationApplicationView.prototype.populateIndexConfigurationTableRows = function(tableBody, tableRows) {
					tableBody.empty();

					for (var tableRow in tableRows) {
						var rowId = tableRows[tableRow].path.replace(/[:<>\.]/g, "-");

						var content = '<tr id="' + rowId + '" '
								+ (tableRows[tableRow].parentPath != null ?
										('class="child-of-' + tableRows[tableRow].parentPath .replace(/[:<>\.]/g, "-") + '"') : '')
								+ '>';

						content += '<td>';
						content += '<span class="data-element">' + tableRows[tableRow].name + '</span>';
						content += '</td>';
						content += '<td>' + '' + '</td>';
						content += '</tr>';

						tableBody.append(content);

						/*var dataElement = m_utils.jQuerySelect("#sourceTable #" + rowId + " .data-element");

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
						var xmlObject = m_utils.jQuerySelect(xmlDoc);

						var view = this; // required since jQuery.find() below changes context of 'this'

						m_utils.jQuerySelect(xmlObject).find("fieldMappings").each(
								function() {

									var fieldPath = m_utils.jQuerySelect(this).attr("fieldPath")

									fieldPath = fieldPath.replace(/\/$/g, ""); // Remove trailing slash(es)
									fieldPath = fieldPath.replace(/\//g, "."); // Replace slash(es) with "."

									view.mappingExpressions[fieldPath] = m_utils.jQuerySelect(
											this).attr("mappingExpression");
									view.advancedMappings[fieldPath] = m_utils.jQuerySelect(
											this).attr("advancedMapping");
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
							if (outputTableRow.advancedMapping == "true") {
								transformationProperty += '" advancedMapping="true';
							}
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

					this.advancedMappingCheckbox.change({
															"view" : this
														},function(event) {
															if (view.advancedMappingCheckbox.attr("checked")) {
																var advMapping = "true";
															} else {
																var advMapping = null;
															}

															for (var n = 0; n < view.outputTableRows.length; ++n) {
																if (view.selectedOutputTableRow === view.outputTableRows[n]) {
																	view.selectedOutputTableRow.advancedMapping = advMapping;
																	view.outputTableRows[n].advancedMapping = advMapping;
																	if (advMapping) {
																		m_utils.jQuerySelect("#elementIndicatorText").empty();
																	} else {
																		m_utils.jQuerySelect("#elementIndicatorText").empty();
																		m_utils.jQuerySelect("#elementIndicatorText")
																				.append(
																						view.selectedOutputTableRow.path
																								+ " = ");
																	}
																	break;
																}
															}

															view.populateMappingCell(view.selectedOutputTableRow);

															view.submitChanges(view.determineTransformationChanges());
														});

					m_utils.jQuerySelect("#" + this.editorAnchor.id)
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
						var inputV = event.data.view.sourceFilterInput.val();
						if(event.which != 0 && event.which != 8){
							inputV = inputV.concat(String.fromCharCode(event.which));
						}
						event.data.view.filterSource(inputV);
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
						var inputV = event.data.view.targetFilterInput.val();
						if(event.which != 0 && event.which != 8){
							inputV = inputV.concat(String.fromCharCode(event.which));
						}
						event.data.view.filterTarget(inputV);
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

					// Index Configuration Dialog
					m_utils.jQuerySelect("#indexConfigurationDialog").dialog({
						autoOpen : false,
						draggable : true,
						resizable : false,
						title : m_i18nUtils
									.getProperty("modeler.model.propertyView.messageTransformation.configurationProperties.idxConfig.title"),
						width : 'auto'
									});

					m_utils.jQuerySelect("#idx-okButton")
							.click(
									{
										"view" : this
									},
									function(event) {
										if (true) {
											m_utils.jQuerySelect("#indexConfigurationDialog").dialog("close");
										}
									});

					m_utils.jQuerySelect("#idx-cancelButton").click(function() {
						m_utils.jQuerySelect("#indexConfigurationDialog").dialog("close");
					});

					// Test
					m_utils.jQuerySelect("#runButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										var inputDataTextarea = m_utils.jQuerySelect("#inputDataTextarea");
										var outputDataTable = m_utils.jQuerySelect("#outputDataTable");

										outputDataTable.empty();

										for ( var n = 0; n < view.outputTableRows.length; ++n) {
											var tableRow = view.outputTableRows[n];

											var outputRow = m_utils.jQuerySelect("<tr></tr>");

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

					m_utils.jQuerySelect("#resetButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;
										var inputDataTextarea = m_utils.jQuerySelect("#inputDataTextarea");
										var outputDataTable = m_utils.jQuerySelect("#outputDataTable");

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

					if (m_utils.isEmptyString(this.nameInput.val())) {
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

					if (m_utils.isEmptyString(nameInput.val())) {
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

					if(!element.length){
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
							return m_utils.jQuerySelect(node).siblings("#" + classNames[key].substring(childPrefix.length));
						}
					}

					return null;
				};
			}
		});