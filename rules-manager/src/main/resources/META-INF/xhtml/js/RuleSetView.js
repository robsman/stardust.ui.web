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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
			"bpm-modeler/js/m_extensionManager", 
			"bpm-modeler/js/m_model",
			"bpm-modeler/js/m_dialog",
			"rules-manager/js/CommandsDispatcher", 
			"bpm-modeler/js/m_view",
			"bpm-modeler/js/m_modelElementView",
			"bpm-modeler/js/m_i18nUtils",
			"bpm-modeler/js/m_parameterDefinitionsPanel",
			"bpm-modeler/js/m_jsfViewManager",
			"rules-manager/js/RuleSet", 
			"rules-manager/js/DecisionTable",
			"rules-manager/js/m_drlAceEditor",
			"rules-manager/js/hotDecisionTable/m_decisionTable",
			"rules-manager/js/hotDecisionTable/m_tableConfig",
			"rules-manager/js/hotDecisionTable/m_renderEngines",
			"rules-manager/js/hotDecisionTable/m_dataFactory",
			"rules-manager/js/hotDecisionTable/m_chFactory",
			"rules-manager/js/hotDecisionTable/m_images",
			"rules-manager/js/hotDecisionTable/m_treeFactory",
			"Handsontable",
			"rules-manager/js/hotDecisionTable/m_popoverFactory",
			"rules-manager/js/hotDecisionTable/m_typeParser"],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				CommandsDispatcher, m_view, m_modelElementView, m_i18nUtils,
				m_parameterDefinitionsPanel, m_jsfViewManager, RuleSet, DecisionTable,ace2,
				hotDecisionTable,tableConfig,renderEngines,dataFactory,chFactory,
				images,treeFactory,ht2,popoverFactory,typeParser) {
			return {
				initialize : function(uuid) {
					var ruleSet = RuleSet.findRuleSetByUuid(uuid);
					var view = new RuleSetView();
					CommandsDispatcher.registerCommandHandler(view);
					view.initialize(ruleSet);
				}
			};

			/**
			 * 
			 */
			function RuleSetView() {
				/**
				 * 
				 */
				RuleSetView.prototype.initialize = function(ruleSet) {
					var paramDefCount,    /*Number of Parameter Definitions in our ruleset*/
						paramDef,         /*instance of a parameter definition*/
						typeDecl,         /*instance of a typeDeclaration*/
						typeBody,         /*result of a typeDecl.getBody() call*/
						codeEditSelector; /*selector for the Ace Code editor linked to the decision table*/
					
					this.drlEditor;
					this.id = "ruleSetView";
					this.uuidOutput = m_utils.jQuerySelect("#ruleSetView #uuidOutput");
					this.idOutput = m_utils.jQuerySelect("#ruleSetView #idOutput");
					this.nameInput = m_utils.jQuerySelect("#ruleSetView #nameInput");
					this.creationDateOutput = m_utils.jQuerySelect("#ruleSetView #creationDateOutput");
					this.lastModificationDateOutput = m_utils.jQuerySelect("#ruleSetView #lastModificationDateOutput");
					codeEditSelector="ruleSetCodeEditor";

				    //initialize tabs control
					m_utils.jQuerySelect("#ruleSetTabs").tabs();
					
					//initialize ACE editor for expert mode drl scripting
					//this.drlEditor=ace2.getDrlEditor(codeEditSelector);

					this.parameterMappingsPanelAnchor = m_utils.jQuerySelect("#parameterMappingsPanelAnchor");

					var view = this;

					this.parameterMappingsPanelAnchor
							.load(
									"plugins/bpm-modeler/views/modeler/parameterDefinitionsPanel.html",
									function(response, status, xhr) {
										if (status == "error") {
											var msg = "Properties Page Load Error: "
													+ xhr.status
													+ " "
													+ xhr.statusText;

											jQuery(this).append(msg);
										} else {
											view.parameterMappingsPanel = m_parameterDefinitionsPanel
													.create({
														scope : view.id,
														submitHandler : view,
														supportsOrdering : false,
														supportsDataMappings : false,
														supportsDescriptors : false,
														supportsDataTypeSelection : true,
														readOnlyParameterList : false,
														supportsInOutDirection : true
													});
											// TODO Not very elegant, only works
											// because the
											// parameterMappingsPanel is the
											// only panel initialized
											// asynchronously
											view.activate(ruleSet, true);
										}
									});

					this.nameInput
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view.ruleSet.name = event.data.view.nameInput
												.val();
										event.data.view.renameView(event.data.view.ruleSet);
										CommandsDispatcher.submitCommand();
									});
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

										for ( var n = 0; n < view.ruleSet.parameterDefinitions.length; ++n) {
											var parameterDefinition = view.ruleSet.parameterDefinitions[n];

											m_utils
													.debug("Parameter Definition");
											m_utils.debug(parameterDefinition);

											if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
												continue;
											}

											if (parameterDefinition.dataType == "struct") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												m_utils
														.debug("Type Declaration");
												m_utils.debug(typeDeclaration);

												inputData += "var ";
												inputData += parameterDefinition.id;
												inputData += " = ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
												inputData += ";";
											} else {

											}
										}

										inputDataTextarea.append(inputData);
									});
				};

				/**
				 * 
				 */
				RuleSetView.prototype.activate = function(ruleSet,
						decisionTableUpdate) {
					var drlText="";
					this.ruleSet = ruleSet;

					this.uuidOutput.empty();
					this.uuidOutput.append(this.ruleSet.uuid);
					this.idOutput.empty();
					this.idOutput.append(this.ruleSet.id);
					this.nameInput.val(this.ruleSet.name);
					this.creationDateOutput.empty();
					this.creationDateOutput.append("" + this.ruleSet.creationDate);
					this.lastModificationDateOutput.empty();
					this.lastModificationDateOutput.append("" + this.ruleSet.lastModificationDate);
					//this.codeTextarea.val(this.ruleSet.generateDrl());
					//debugger;
					drlText +=this.ruleSet.generateDrl();
					//this.drlEditor.setValue(drlText);
					// Workaround until command handling is improved

					this.parameterMappingsPanel.setScopeModel(null);
					this.parameterMappingsPanel
							.setParameterDefinitions(this.ruleSet.parameterDefinitions);					
				};

				/**
				 * 
				 */
				RuleSetView.prototype.toString = function() {
					return "rules-manager.RuleSetView";
				};

				/**
				 * 
				 */
				RuleSetView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Model name must not be empty.");
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
				RuleSetView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitions) {
					// TODO Bridge command protocol

					this.ruleSet.parameterDefinitions = parameterDefinitions;

					m_utils.debug("Facts:");
					m_utils.debug(parameterDefinitions);

					this.activate(this.ruleSet);
				};

				/**
				 * 
				 */
				RuleSetView.prototype.processCommand = function(command) {
					// TODO Dummy

					this.activate(this.ruleSet);
					if (command.name
							&& command.name === "RuleSet.Rename") {
						this.renameView(command.ruleSet);	
					}
				};
				
				/**
				 * 
				 */
				RuleSetView.prototype.renameView = function(ruleSet) {
					m_jsfViewManager.create().updateView("ruleSetView", "name" + "=" + ruleSet.name, ruleSet.uuid);
				};
			}
		});