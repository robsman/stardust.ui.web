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
		[   "bpm-modeler/js/m_utils", 
		    "bpm-modeler/js/m_constants",
			"bpm-modeler/js/m_extensionManager", 
			"bpm-modeler/js/m_model",
			"bpm-modeler/js/m_dialog",
			"rules-manager/js/m_commandsDispatcher", 
			"bpm-modeler/js/m_view",
			"bpm-modeler/js/m_modelElementView",
			"rules-manager/js/m_i18nUtils",
			"bpm-modeler/js/m_parameterDefinitionsPanel",
			"bpm-modeler/js/m_jsfViewManager",
			"rules-manager/js/m_ruleSet", 
			"rules-manager/js/m_decisionTable",
			"rules-manager/js/m_drlAceEditor",
			"rules-manager/js/hotDecisionTable/m_decisionTable",
			"rules-manager/js/hotDecisionTable/m_tableConfig",
			"rules-manager/js/hotDecisionTable/m_treeFactory",
			"Handsontable",
			"rules-manager/js/hotDecisionTable/m_typeParser",
			"rules-manager/js/m_i18nMapper",
			"rules-manager/js/m_ruleSetCommandDispatcher",
			"rules-manager/js/m_ruleSetCommand","rules-manager/js/hotDecisionTable/m_utilities"],
		function(m_utils, m_constants, m_extensionManager, m_model, m_dialog,
				CommandsDispatcher, m_view, m_modelElementView, m_i18nUtils,
				m_parameterDefinitionsPanel, m_jsfViewManager, RuleSet, DecisionTable,ace2,
				hotDecisionTable,tableConfig,treeFactory,ht2,
				typeParser,m_i18nMapper,
				m_ruleSetCommandDispatcher,m_ruleSetCommand,m_utilities) {
			return {
				initialize : function(uuid,options) {
					var ruleSet = RuleSet.findRuleSetByUuid(uuid);
					var view = new RuleSetView();
					view.initialize(ruleSet,options);
				}
			};

			/**
			 * 
			 */
			function RuleSetView() {
				/**
				 * 
				 */
				RuleSetView.prototype.initialize = function(ruleSet,options) {
					var paramDefCount,    /*Number of Parameter Definitions in our ruleset*/
						paramDef,         /*instance of a parameter definition*/
						typeDecl,         /*instance of a typeDeclaration*/
						typeBody,         /*result of a typeDecl.getBody() call*/
						$descriptionTextArea,
						cnstCmd,		  /*enum of our commands from the command Factory.*/
						codeEditSelector; /*selector for the Ace Code editor linked to the decision table*/
					
					var uiElements={
							uuidLabel: m_utils.jQuerySelect(options.selectors.uuidLabel),
							idLabel: m_utils.jQuerySelect(options.selectors.idLabel),
							nameLabel: m_utils.jQuerySelect(options.selectors.nameLabel),
							descriptionLabel: m_utils.jQuerySelect(options.selectors.descriptionLabel),
							creationDateLabel: m_utils.jQuerySelect(options.selectors.creationDateLabel),
							lastModificationDateLabel: m_utils.jQuerySelect(options.selectors.lastModificationDateLabel),
							parameterTabLabel: m_utils.jQuerySelect(options.selectors.parameterTabLabel)
					};
					
					m_i18nMapper.map(options,uiElements,true);
					
					this.drlEditor;
					this.id = "ruleSetView";
					this.uuidOutput = m_utils.jQuerySelect("#ruleSetView #uuidOutput");
					this.idOutput = m_utils.jQuerySelect("#ruleSetView #idOutput");
					this.nameInput = m_utils.jQuerySelect("#ruleSetView #nameInput");
					this.creationDateOutput = m_utils.jQuerySelect("#ruleSetView #creationDateOutput");
					this.lastModificationDateOutput = m_utils.jQuerySelect("#ruleSetView #lastModificationDateOutput");
					this.parameterMappingsPanelAnchor = m_utils.jQuerySelect("#parameterMappingsPanelAnchor");
					
					codeEditSelector="ruleSetCodeEditor";
					
					/*For brevity , access command constants using shorthand*/
					cnstCMD=m_ruleSetCommand.commands;
					
				    //initialize tabs control
					m_utils.jQuerySelect("#ruleSetTabs").tabs();
					
					/*Bind ruleSet description to our description textArea*/
					$descriptionTextArea=m_utils.jQuerySelect("#descriptionTextarea");
					$descriptionTextArea.val(ruleSet.description);
					
					/* Bind change events to our ruleSet and our toplevel processor
					 * Outgoing events....
					 * */
					$descriptionTextArea.on("change",function(event){
						ruleSet.description=$descriptionTextArea.val();
						ruleSet.state.isDirty=true;
						var cmd=m_ruleSetCommand.ruleSetDescriptionCmd(
								ruleSet,ruleSet.description,event);
						m_ruleSetCommandDispatcher.trigger(cmd);
						console.log(event);
					});
					
					/*Bind our description textarea to incoming events from our top level processor.*/
					m_ruleSetCommandDispatcher.register($descriptionTextArea,cnstCMD.ruleSetDescriptionCmd);
					$descriptionTextArea.on(cnstCMD.ruleSetDescriptionCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(ruleSet.uuid ===uuid && $descriptionTextArea.val()!=newVal){
							$descriptionTextArea.val(newVal);
						}
					});
					
					/*Bind our nameInput field to incoming events from our top level processor.*/
					m_ruleSetCommandDispatcher.register(this.nameInput,cnstCMD.ruleSetRenameCmd);
					var $nameInput=this.nameInput;
					this.nameInput.on(cnstCMD.ruleSetRenameCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(ruleSet.uuid ===uuid && $nameInput.val()!=newVal){
							$nameInput.val(newVal);
						}
					});
					
					/*Bind our Parameter Definition panel  to incoming events from our top level processor.*/
					var $that=$(this);
					m_ruleSetCommandDispatcher.register($that,cnstCMD.ruleSetFactCmd);
					$that.on(cnstCMD.ruleSetFactCmd,function(event,data){
						var uuid=data.elementID;
						var newVal=data.changes[0].value.after;
						if(ruleSet.uuid ===uuid){
							$that[0].parameterMappingsPanel
							.setParameterDefinitions($that[0].ruleSet.parameterDefinitions);
						}
					});
					
					

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
											view.parameterMappingsPanel =  m_parameterDefinitionsPanel
													.create({
														scope : view.id,
														submitHandler : view,
														supportsOrdering : false,
														supportsDocumentTypes: false,
														supportsDataMappings : false,
														supportsDescriptors : false,
														supportsDataTypeSelection : true,
														readOnlyParameterList : false,
														supportsInOutDirection : true,
														displayParameterId: true
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
										var oldName = event.data.view.ruleSet.name;
										event.data.view.ruleSet.name = event.data.view.nameInput.val();
										ruleSet.state.isDirty=true;
										var cmd=m_ruleSetCommand.ruleSetRenameCmd(
												ruleSet,event.data.view.ruleSet.name,event);
										m_ruleSetCommandDispatcher.trigger(cmd);

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
					this.creationDateOutput.append("" + m_utilities.formatDate(this.ruleSet.creationDate,"MM/dd/yy hh:mm tt"));
					this.lastModificationDateOutput.empty();
					this.lastModificationDateOutput.append("" + m_utilities.formatDate( this.ruleSet.lastModificationDate,"MM/dd/yy hh:mm tt"));
					drlText +=this.ruleSet.generateDrl();
					this.parameterMappingsPanel.setScopeModel(null);
					this.parameterMappingsPanel.setParameterDefinitions(this.ruleSet.parameterDefinitions);					
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
					this.ruleSet.state.isDirty=true;
					var cmd=m_ruleSetCommand.ruleSetFactCmd(
							this.ruleSet,parameterDefinitions,undefined);
					m_ruleSetCommandDispatcher.trigger(cmd);
					
					m_utils.debug("Facts:");
					m_utils.debug(parameterDefinitions);
					this.activate(this.ruleSet);
				};

			}
		});