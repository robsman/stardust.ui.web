/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * Utility functions for dialog programming.
 * 
 * @author 
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce" ],
		function(m_utils, m_i18nUtils, m_constants, m_commandsController,
				m_command, m_model, m_accessPoint, m_typeDeclaration,
				m_parameterDefinitionsPanel, m_codeEditorAce) {
			return {
				create : function(view) {
					var overlay = new StoredProcedureIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function StoredProcedureIntegrationOverlay() {
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.view
							.insertPropertiesTab(
									"storedProcedureIntegrationOverlay",
									"parameters",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
									"plugins/bpm-modeler/images/icons/database_link.png");

					this.view
							.insertPropertiesTab("storedProcedureIntegrationOverlay",
									"dataSource", "Data Source",
									"plugins/bpm-modeler/images/icons/database_link.png");
					
					this.simpleQueryDiv = m_utils
					.jQuerySelect("#storedProcedureIntegrationOverlay #simpleQueryDiv");
					this.customSqlQueryDiv = m_utils
					.jQuerySelect("#storedProcedureIntegrationOverlay #customSqlQueryDiv");
					this.customSqlQueryDiv.hide();
					this.customSqlQueryCbx= m_utils
					.jQuerySelect("#storedProcedureIntegrationOverlay #customSqlQueryCbx");
					
					this.sqlQueryHeading = m_utils
							.jQuerySelect("#storedProcedureIntegrationOverlay #sqlQueryHeading");
					this.inputBodyAccessPointInput = m_utils
							.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = m_utils
							.jQuerySelect("#parametersTab #outputBodyAccessPointInput");
					this.editorAnchor = m_utils.jQuerySelect("#codeEditorDiv")
							.get(0);
					this.editorAnchor.id = "codeEditorDiv"
							+ Math.floor((Math.random() * 100000) + 1);

					this.codeEditor = m_codeEditorAce
							.getSQLCodeEditor(this.editorAnchor.id);
					this.codeEditor.loadLanguageTools();
					
					this.connectionTypeSelect = m_utils
							.jQuerySelect("#dataSourceTab #connectionTypeSelect");
					this.databaseTypeSelect = m_utils
							.jQuerySelect("#dataSourceTab #databaseTypeSelect");

					this.urlInput = m_utils
							.jQuerySelect("#dataSourceTab #urlInput");
					this.driverInput = m_utils
							.jQuerySelect("#dataSourceTab #driverInput");

					this.hostInput = m_utils
							.jQuerySelect("#dataSourceTab #hostInput");
					this.portInput = m_utils
							.jQuerySelect("#dataSourceTab #portInput");
					this.dataBaseNameInput = m_utils
							.jQuerySelect("#dataSourceTab #dataBaseNameInput");
					this.userNameInput = m_utils
							.jQuerySelect("#dataSourceTab #userNameInput");
					this.passwordInput = m_utils
							.jQuerySelect("#dataSourceTab #passwordInput");
					this.useCVforPassowrdInput = m_utils
							.jQuerySelect("#dataSourceTab #useCVforPassowrdInput");
					
					this.dbCatalogInput = m_utils
					.jQuerySelect("#configurationTab #simpleQueryDiv #dbCatalogInput");
					this.dbSchemaInput = m_utils
					.jQuerySelect("#configurationTab #simpleQueryDiv #dbSchemaInput");
					this.spNameInput = m_utils
					.jQuerySelect("#configurationTab #simpleQueryDiv #spNameInput");
					

					this.connectionTypeSelect.empty();
					this.connectionTypeSelect
							.append("<option value='direct' selected>Direct</option>");
					/*this.connectionTypeSelect
							.append("<option value='jndi'>JNDI</option>");*/

					this.directConfigTab = m_utils
							.jQuerySelect("#dataSourceTab #directConfigTab");
					this.jndiConfigTab = m_utils.jQuerySelect("#jndiConfigTab");

					this.dbUrlConfig = m_utils.jQuerySelect("#dbUrlConfig");
					this.dbDriverConfig = m_utils.jQuerySelect("#dbDriverConfig");
					this.showHideOthersDbConfig(true);// hide by default; show only when others is selected
					this.hostDbConfig = m_utils.jQuerySelect("#hostDbConfig");
					this.portConfig = m_utils.jQuerySelect("#portConfig");
					this.dbNameConfig = m_utils.jQuerySelect("#dbNameConfig");

					this.databaseTypeSelect.empty();
					this.databaseTypeSelect
							.append("<option value='oracle'>Oracle</option>");
					this.databaseTypeSelect
							.append("<option value='mysql'>Mysql</option>");
					this.databaseTypeSelect
							.append("<option value='postgres'>PostgreSQL</option>");
					this.databaseTypeSelect
							.append("<option value='others'>Others...</option>");

					this.connectionTypeSelect.change(function() {
						if (self.connectionTypeSelect.val() == "direct") {
							self.jndiConfigTab.hide();
							self.directConfigTab.show();
						} else if (self.connectionTypeSelect.val() == "jndi") {
							self.directConfigTab.hide();
							self.jndiConfigTab.show();
						}
						// self.submitChanges();
						self.view.submitModelElementAttributeChange(
								"stardust:sqlScriptingOverlay::connectionType",
								self.connectionTypeSelect.val());
					});

               this.databaseTypeSelect
                     .change({
                        panel : this
                     },
                     function(event) {
                        if (!event.data.panel.view.validate()) {
                           return;
                        }
								if (self.databaseTypeSelect.val() == "others") {
									self.showHideCommonDbConfig(true);
									self.showHideOthersDbConfig();
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::hostname",null);
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::port" ,null);
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::dbname",null);
									
								} else {
									self.showHideCommonDbConfig();
									self.showHideOthersDbConfig(true);
									
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::databasetype",self.databaseTypeSelect.val());
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::url",null);
									self.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::driverClassName",null);
								}
							});

					this.urlInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::hostname",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::port",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::dbname",null);
					
					});
					
					this.driverInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}

						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::hostname",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::port",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::dbname",null);
					});

					this.hostInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}

						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::url",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::driverClassName",null);
					});
					
					this.portInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}

						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::url",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::driverClassName",null);
					
					});

					this.dataBaseNameInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}

						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::url",null);
                        event.data.panel.view.submitModelElementAttributeChange("stardust:sqlScriptingOverlay::driverClassName",null);
					});
					
					this.userNameInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}
					
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
						
					
					});
					
					
					
					
					this.passwordInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}					
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
					
					});
					
					
					
					this.useCVforPassowrdInput.change({
						panel : this
					},
					function(event) {
					//	self.submitChanges();
						event.data.panel.view.submitChanges({
							attributes : {
									"stardust:sqlScriptingOverlay::useCVforPassowrd":
									event.data.panel.useCVforPassowrdInput.prop("checked") ? event.data.panel.useCVforPassowrdInput.prop("checked"): null
								}
						});
					});
					
					this.dbCatalogInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}
						event.data.panel.view.submitChanges({
							attributes : {
									"stardust:sqlScriptingOverlay::catalogName":
									event.data.panel.dbCatalogInput.val()
								}
						});
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
					});
					this.dbSchemaInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}
						event.data.panel.view.submitChanges({
							attributes : {
									"stardust:sqlScriptingOverlay::schemaName":
									event.data.panel.dbSchemaInput.val()
								}
						});
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
					});
					this.spNameInput.change({
						panel : this
					},
					function(event) {
						if (!event.data.panel.validate()) {
							return;
						}
						event.data.panel.view.submitChanges({
							attributes : {
									"stardust:sqlScriptingOverlay::storedProcedureName":
									event.data.panel.spNameInput.val()
								}
						});
						event.data.panel.submitParameterDefinitionsChanges(event.data.panel.getApplication().contexts.application.accessPoints);
					});
					this.customSqlQueryCbx.change({
						panel : this
					},
					function(event) {
					//	self.submitChanges();
						if(event.data.panel.customSqlQueryCbx.prop("checked")){
							/*
							event.data.panel.dbCatalogInput.val(null);
							event.data.panel.dbSchemaInput.val(null);
							event.data.panel.spNameInput.val(null);
							
							*/
							event.data.panel.dbCatalogInput.val(null);
							event.data.panel.dbSchemaInput.val(null);
							event.data.panel.spNameInput.val(null);
							
							event.data.panel.simpleQueryDiv.hide();
							event.data.panel.view.submitChanges({
									attributes : {
										"stardust:sqlScriptingOverlay::catalogName":null,
										"stardust:sqlScriptingOverlay::schemaName":null,
										"stardust:sqlScriptingOverlay::storedProcedureName":null,
										"stardust:sqlScriptingOverlay::useCustomSqlQuery":event.data.panel.customSqlQueryCbx.prop("checked")
									}
							});
							
							
							
							
							event.data.panel.customSqlQueryDiv.show();
						}else{
							event.data.panel.customSqlQueryDiv.hide();
							event.data.panel.simpleQueryDiv.show();
							event.data.panel.view.submitChanges({
									attributes : {
										"stardust:sqlScriptingOverlay::sqlQuery":null,
										"stardust:sqlScriptingOverlay::useCustomSqlQuery": null
									}
							});
							event.data.panel.codeEditor
							.getEditor()
							.getSession()
							.setValue(null);
						}
						
					});
					
					
					var self = this;

					this.codeEditor.getEditor().on('blur', function(e) {
						self.submitChanges();
					});
               this.inputBodyAccessPointInput
                     .change(
                           {
                               panel : this
                           },
                           function(event) {
                        if (!event.data.panel.view.validate()) {
                           return;
                        }

								if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {

									var filteredAccessPoints = [];
									var index = 0;
									for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; n++) {
										var parameterDefinition = self
												.getApplication().contexts.application.accessPoints[n];

										if ((parameterDefinition.direction == m_constants.OUT_ACCESS_POINT)
												|| (parameterDefinition.direction == m_constants.IN_ACCESS_POINT && parameterDefinition.id == "CamelSqlQuery")) {
											filteredAccessPoints[index] = parameterDefinition;
											index++;
										}
									}

									self.view
											.submitChanges({
												contexts : {
													application : {
														accessPoints : filteredAccessPoints
													}

												}
											});
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::inBodyAccessPoint",
													null);

								} else {
									var accessPoints = {};
									var defaultAccessPoints = [];
									for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; ++n) {
										var parameterDefinition = self
												.getApplication().contexts.application.accessPoints[n];

										if (parameterDefinition.direction == m_constants.IN_ACCESS_POINT) {
											accessPoints[parameterDefinition.id] = parameterDefinition;
											defaultAccessPoints
													.push(parameterDefinition);
										}
									}
									var inAccessPoint = self.getApplication().contexts.application.accessPoints;
									var structuredData;// =self.getScopeModel().findData(self.outputBodyAccessPointInput.val());
									for ( var i in self.getScopeModel().typeDeclarations) {
										if (self.getScopeModel().typeDeclarations[i]
												.isSequence()) {
											if (self.getScopeModel().typeDeclarations[i].id
													.toLowerCase() == self.inputBodyAccessPointInput
													.val()) {
												structuredData = self
														.getScopeModel().typeDeclarations[i];
												break;
											}
										}
									}
									if (structuredData) {

										var alreadyExists = false;
										for ( var n = 0; n < inAccessPoint.length; ++n) {
											var param = inAccessPoint[n];
											if (param.direction == "IN"
													&& param.id == structuredData.id
															.toLowerCase()) {
												alreadyExists = true;
												continue;
											}
										}
										if (!inAccessPoint[structuredData.id
												.toLowerCase()]
												&& !alreadyExists) {

											inAccessPoint
													.push({
														id : structuredData.id
																.toLowerCase(),
														name : structuredData.name
																.toLowerCase(),
														dataType : "struct",
														direction : "IN",
														structuredDataTypeFullId : structuredData
																.getFullId(),
														attributes : {
															"stardust:predefined" : true,
															"carnot:engine:dataType" : structuredData.id
														}
													});
										}
										self.view
												.submitChanges({
													contexts : {
														application : {
															accessPoints : inAccessPoint
														}
													}
												});
										self.view
												.submitModelElementAttributeChange(
														"carnot:engine:camel::inBodyAccessPoint",
														structuredData.id
																.toLowerCase());
									}
								}
							});
					   this.outputBodyAccessPointInput
							 .change(
								   {
									  panel : this
								   },
								   function(event) {
								if (!event.data.panel.view.validate()) {
								   return;
								}

								if (self.outputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::outBodyAccessPoint",
													null);
									var accessPoints = [];
									var index = 0;
									for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; ++n) {
										var parameterDefinition = self
												.getApplication().contexts.application.accessPoints[n];

										if ((parameterDefinition.direction == m_constants.IN_ACCESS_POINT)
												|| (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT && (parameterDefinition.id == "CamelSqlUpdateCount" || parameterDefinition.id == "CamelSqlRowCount"))) {
											accessPoints[index] = parameterDefinition;
											index++;

										}
									}

									self.view.submitChanges({
										contexts : {
											application : {
												accessPoints : accessPoints
											}
										}
									});

								} else {

									var accessPoints = {};
									var defaultAccessPoints = [];
									for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; ++n) {
										var parameterDefinition = self
												.getApplication().contexts.application.accessPoints[n];

										if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
											accessPoints[parameterDefinition.id] = parameterDefinition;
											defaultAccessPoints
													.push(parameterDefinition);
										}
									}
									var outAccessPoint = self.getApplication().contexts.application.accessPoints;
									var structuredData;// =self.getScopeModel().findData(self.outputBodyAccessPointInput.val());
									for ( var i in self.getScopeModel().typeDeclarations) {
										if (self.getScopeModel().typeDeclarations[i]
												.isSequence()) {
											if (self.getScopeModel().typeDeclarations[i].id
													.toLowerCase() == self.outputBodyAccessPointInput
													.val()) {
												structuredData = self
														.getScopeModel().typeDeclarations[i];
												break;
											}
										}
									}
									if (structuredData) {

										var alreadyExists = false;
										for ( var n = 0; n < outAccessPoint.length; ++n) {
											var param = outAccessPoint[n];
											if (param.id == structuredData.id
													.toLowerCase()) {
												alreadyExists = true;
												continue;
											}
										}
										if (!outAccessPoint[structuredData.id
												.toLowerCase()]
												&& !alreadyExists) {

											outAccessPoint
													.push({
														id : structuredData.id
																.toLowerCase(),
														name : structuredData.name
																.toLowerCase(),
														dataType : "struct",
														direction : "OUT",
														structuredDataTypeFullId : structuredData
																.getFullId(),
														attributes : {
															"stardust:predefined" : true,
															"carnot:engine:dataType" : structuredData.id
														}
													});
										}

										self.view
												.submitChanges({
													contexts : {
														application : {
															accessPoints : outAccessPoint
														}
													}
												});
										// }

										self.view
												.submitModelElementAttributeChange(
														"carnot:engine:camel::outBodyAccessPoint",
														structuredData.id
																.toLowerCase());
									}
								}
							});

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : true,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								supportsDocumentTypes : false,
								supportsOtherData : false
							});

					this.update();
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.showHideOthersDbConfig= function(hide) {
					if(hide){
						this.dbUrlConfig.hide();
						this.dbDriverConfig.hide();
					}else{
						this.dbUrlConfig.show();
						this.dbDriverConfig.show();
					}
				}
				StoredProcedureIntegrationOverlay.prototype.showHideCommonDbConfig= function(hide) {
					if(hide){
						this.hostDbConfig.hide();
						this.portConfig.hide();
						this.dbNameConfig.hide();
						
					}else{
						this.hostDbConfig.show();
						this.portConfig.show();
						this.dbNameConfig.show();
					}
				}

				
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",
									"carnot:engine:camel::applicationIntegrationOverlay" : "storedProcedureIntegrationOverlay"
								}
							});

					
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.update = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					this.inputBodyAccessPointInput.empty();
					this.inputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "'>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");

					this.outputBodyAccessPointInput.empty();
					this.outputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "' selected>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");

					this.inputBodyAccessPointInput = this
							.populateDataStructuresSelectInput(
									this.inputBodyAccessPointInput, this
											.getScopeModel(), true);
					this.outputBodyAccessPointInput = this
							.populateDataStructuresSelectInput(
									this.outputBodyAccessPointInput, this
											.getScopeModel(), true);

					this.inputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);

					this.outputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
					
					this.codeEditor
							.getEditor()
							.getSession()
							.setValue(
									this.getApplication().attributes["stardust:sqlScriptingOverlay::sqlQuery"]);
					
					this.connectionTypeSelect
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::connectionType"]);
					this.databaseTypeSelect
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::databasetype"]);

					if (this.getApplication().attributes["stardust:sqlScriptingOverlay::databasetype"] == "others") {
						this.urlInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::url"]);
						this.driverInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::driverClassName"]);
						this.hostDbConfig.hide();
						this.portConfig.hide();
						this.dbUrlConfig.show();
						this.dbDriverConfig.show();
						

					} else {
						this.hostDbConfig.show();
						this.portConfig.show();
						this.dbUrlConfig.hide();
						this.dbDriverConfig.hide();
						

						this.hostInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::hostname"]);
						this.portInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::port"]);
						this.dataBaseNameInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::dbname"]);
					}

					this.hostInput
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::hostname"]);
					this.portInput
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::port"]);
					this.dataBaseNameInput
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::dbname"]);
					this.userNameInput
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::username"]);

					this.passwordInput
							.val(this.convertConfigVariableToPassword(
									this.getApplication().attributes["stardust:sqlScriptingOverlay::password"]));
					this.useCVforPassowrdInput
							.prop(
									"checked",
									this.getApplication().attributes["stardust:sqlScriptingOverlay::useCVforPassowrd"]);
					
					this.customSqlQueryCbx
					.prop(
							"checked",
							this.getApplication().attributes["stardust:sqlScriptingOverlay::useCustomSqlQuery"]);
					
					
					if(this.customSqlQueryCbx.prop("checked")){
						this.simpleQueryDiv.hide();
						this.customSqlQueryDiv.show();
					}else{
						this.customSqlQueryDiv.hide();
						this.simpleQueryDiv.show();
					}
					
					this.dbCatalogInput
					.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::catalogName"]);
					this.dbSchemaInput
					.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::schemaName"]);
					this.spNameInput
					.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::storedProcedureName"]);
					
					
					this.parameterDefinitionsTableBody=this.parameterDefinitionsPanel.parameterDefinitionsTableBody;
					this.initializeParameterDefinitionsTable();
					this.parameterDefinitionsPanel.selectCurrentParameterDefinition();
					//this.parameterDefinitionsPanel.populateParameterDefinitionFields();
				};
				
				StoredProcedureIntegrationOverlay.prototype.initializeParameterDefinitionsTable = function() {
					this.parameterDefinitionsTableBody.empty();

					for ( var m = 0; m < this.parameterDefinitionsPanel.parameterDefinitions.length; ++m) {
						var parameterDefinition = this.parameterDefinitionsPanel.parameterDefinitions[m];
						if(parameterDefinition.id===this.inputBodyAccessPointInput.val() || parameterDefinition.id===this.outputBodyAccessPointInput.val()){
							
						}else{
						var content = "<tr id=\"" + m + "\">";

						content += "<td class=\"";

						if (parameterDefinition.direction == "IN") {
							if (this.parameterDefinitionsPanel.options.supportsDescriptors) {
								if (parameterDefinition.descriptor) {
									content += "descriptorDataPathListItem";
								} else if (parameterDefinition.keyDescriptor) {
									content += "keyDescriptorDataPathListItem";
								} else {
									content += "inDataPathListItem";
								}
							} else {
								content += "inDataPathListItem";
							}
						} else if (parameterDefinition.direction == "INOUT") {
							content += "inoutDataPathListItem";
						} else {
							content += "outDataPathListItem";
						}

						content += "\" style=\"width: "
								+ this.parameterDefinitionsPanel.options.directionColumnWidth
								+ "\"></td>";

						content += "<td style=\"width: "
								+ this.parameterDefinitionsPanel.options.nameColumnWidth + "\">"
								+ parameterDefinition.name;
						content += "</td>";

						if (this.parameterDefinitionsPanel.options.supportsDataTypeSelection) {
							content += "<td style=\"width: "
									+ this.parameterDefinitionsPanel.options.typeColumnWidth + "\">";
							if (parameterDefinition.dataType == m_constants.PRIMITIVE_DATA_TYPE) {
								content += m_typeDeclaration
										.getPrimitiveTypeLabel(parameterDefinition.primitiveDataType); // TODO
								// Convert
							} else {
								if (parameterDefinition.structuredDataTypeFullId) {
									content += m_model
											.stripElementId(parameterDefinition.structuredDataTypeFullId); // TODO
								}
								// Format
							}

							content += "</td>";
						}

						if (this.parameterDefinitionsPanel.options.supportsDataMappings) {
							content += "<td style=\"width: "
									+ this.parameterDefinitionsPanel.options.mappingColumnWidth + "\">";

							if (parameterDefinition.dataFullId != null
									&& m_model
											.findData(parameterDefinition.dataFullId)) {
								var data = m_model
										.findData(parameterDefinition.dataFullId);

								content += data.name;

								if (this.options.supportsDataPathes) {
									if (parameterDefinition.dataPath != null) {
										content += ".";
										content += parameterDefinition.dataPath;
									}
								}
							}

							content += "</td>";
						}

						var newValue = m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.inputText.new");
						content = content.replace(">New", ">" + newValue);
						newValue = m_i18nUtils
								.getProperty("modeler.model.propertyView.structuredTypes.configurationProperties.element.selectType.string");
						content = content.replace("String", newValue);

						this.parameterDefinitionsTableBody.append(content);

						m_utils.jQuerySelect(
								this.parameterDefinitionsPanel.options.scope
										+ "table#parameterDefinitionsTable tr")
								.mousedown(
										{
											panel : this
										},
										function(event) {
											//event.data.panel.deselectParameterDefinitions();
											event.data.panel.parameterDefinitionsPanel.deselectParameterDefinitions()
											m_utils.jQuerySelect(this).addClass("selected");

											var index = m_utils.jQuerySelect(this).attr("id");

											//event.data.panel.currentParameterDefinition = event.data.panel.parameterDefinitions[index];
											event.data.panel.parameterDefinitionsPanel.currentParameterDefinition = event.data.panel.parameterDefinitionsPanel.parameterDefinitions[index];
											event.data.panel.parameterDefinitionsPanel.selectedRowIndex = index;

											event.data.panel.parameterDefinitionsPanel
													.populateParameterDefinitionFields();
										});
					}}

					
					return parameterDefinitionsTable;
				}
				
				StoredProcedureIntegrationOverlay.prototype.createIntrinsicAccessPoints = function() {
					var accessPoints = this.getApplication().contexts.application.accessPoints;
					var defaultAccessPoints = this.getApplication().contexts.application.accessPoints;
					var addCamelSqlQueryVar = true, addCamelSqlUpdateCountVar = true, addCamelSqlRowCountVar = true;

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];
						if (parameterDefinition.id == "CamelSqlQuery") {
							addCamelSqlQueryVar = false;
							continue;
						}
						if (parameterDefinition.id == "CamelSqlUpdateCount") {
							addCamelSqlUpdateCountVar = false;
							continue;
						}
						if (parameterDefinition.id == "CamelSqlRowCount") {
							addCamelSqlRowCountVar = false;
							continue;
						}
						/*
						 * if (parameterDefinition.direction ==
						 * m_constants.IN_ACCESS_POINT) {
						 * accessPoints[parameterDefinition.id] =
						 * parameterDefinition;
						 * defaultAccessPoints.push(parameterDefinition); }
						 */
					}

					if (!accessPoints["CamelSqlQuery"] && addCamelSqlQueryVar) {
						defaultAccessPoints.push({
							id : "CamelSqlQuery",
							name : "SqlQuery",
							dataType : "primitive",
							primitiveDataType : "String",
							direction : "IN",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}
					if (!accessPoints["CamelSqlUpdateCount"]
							&& addCamelSqlQueryVar) {
						defaultAccessPoints.push({
							id : "CamelSqlUpdateCount",
							name : "SqlUpdateCount",
							dataType : "primitive",
							primitiveDataType : "int",
							direction : "OUT",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}
					if (!accessPoints["CamelSqlRowCount"]
							&& addCamelSqlQueryVar) {
						defaultAccessPoints.push({
							id : "CamelSqlRowCount",
							name : "SqlRowCount",
							dataType : "primitive",
							primitiveDataType : "int",
							direction : "OUT",
							attributes : {
								"stardust:predefined" : true
							}
						});
					}

					return defaultAccessPoints;
				}

				StoredProcedureIntegrationOverlay.prototype.populateDataStructuresSelectInput = function(
						structuredDataTypeSelect, scopeModel,
						restrictToCurrentModel) {
					// var structuredDataTypeSelect;
					structuredDataTypeSelect.empty();
					structuredDataTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					if (scopeModel) {
						structuredDataTypeSelect
								.append("<optgroup label='"
										+ m_i18nUtils
												.getProperty("modeler.general.thisModel")
										+ "'>");

						for ( var i in scopeModel.typeDeclarations) {
							if (!scopeModel.typeDeclarations[i].isSequence())
								continue;
							structuredDataTypeSelect.append("<option value='"
									+ scopeModel.typeDeclarations[i].id
											.toLowerCase() + "'>"
									+ scopeModel.typeDeclarations[i].name
									+ "</option>");
						}
					}
					return structuredDataTypeSelect;
				};

				StoredProcedureIntegrationOverlay.prototype.populateDataSourceBeanDefinition = function() {
/*
				if (!this.view.validate()) {
						return;
					}
*/
					var beanDefinition = "";
					var driverClassName = "";
					var url = "";
					if (this.databaseTypeSelect.val() == "oracle") {
						driverClassName = "oracle.jdbc.driver.OracleDriver";
						url = "jdbc:oracle:thin:@" + this.hostInput.val() + ":"
								+ this.portInput.val() + ":"
								+ this.dataBaseNameInput.val();
					} else if (this.databaseTypeSelect.val() == "mysql") {
						driverClassName = "com.mysql.jdbc.Driver";
						url = "jdbc:mysql://" + this.hostInput.val() + ":"
								+ this.portInput.val() + "/"
								+ this.dataBaseNameInput.val();
					} else if (this.databaseTypeSelect.val() == "postgres") {
						driverClassName = "org.postgresql.Driver";
						url = "jdbc:postgresql://" + this.hostInput.val() + ":"
								+ this.portInput.val() + "/"
								+ this.dataBaseNameInput.val();
					} else if (this.databaseTypeSelect.val() == "others") {
						driverClassName = this.getApplication().attributes["stardust:sqlScriptingOverlay::driverClassName"];
						url = this.getApplication().attributes["stardust:sqlScriptingOverlay::url"];
					}

					beanDefinition += "<bean id=\""
							+ this.getDataSourceName()
							+ "\" class=\"org.apache.tomcat.dbcp.dbcp.BasicDataSource\" destroy-method=\"close\">";
					beanDefinition += "<property name=\"url\" value=\"" + url
							+ "\" />";
					beanDefinition += "<property name=\"driverClassName\" value=\""
							+ driverClassName + "\" />";
					if(this.userNameInput.val()!=""){
						beanDefinition += "<property name=\"username\" value=\""+ this.userNameInput.val() + "\" />";
					}
					if (this.useCVforPassowrdInput.prop("checked") && this.passwordInput.val() !="") {
						beanDefinition += "<property name=\"password\" value=\"${"
								+ this.passwordInput.val() + ":Password}\" />";
					} else {
						if(this.passwordInput.val()!=""){
						beanDefinition += "<property name=\"password\" value=\""
								+ this.passwordInput.val() + "\" />";
						}
					}
					beanDefinition += "</bean>";
					return beanDefinition;
				}
				StoredProcedureIntegrationOverlay.prototype.getDataSourceName = function() {
					return this.getApplication().id + "Ds";
				}
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.getRoute = function() {
					var route = "";
					var sqlQuery = this.codeEditor.getEditor().getSession()
							.getValue();
					var dataSourceName = "";
					if (sqlQuery != null && sqlQuery != "") {

						sqlQuery = sqlQuery.replace(/&/g, "&amp;");
						sqlQuery = sqlQuery.replace(/</g, "&lt;");
						sqlQuery = sqlQuery.replace(/>/g, "&gt;");
					}
					route += "<to uri=\"spComponent://service/spsystem/storedProcedureExecutor\" />";
					m_utils.debug(route);
					route = route.replace(/&/g, "&amp;");
					return route;
				};

				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.convertPasswordToConfigVariable = function(password) {	
					if(!m_utils.isEmptyString(password) && this.useCVforPassowrdInput.prop("checked")){
						password = "${"+password+ ":Password}";	
					}
					return password;
				};
				
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.convertConfigVariableToPassword = function(password) {	
					
					if(!m_utils.isEmptyString(password))
					{
						if(password.indexOf("${") > -1){
							var firstIdex = password.indexOf("${");
							var lastIdex = password.lastIndexOf(":");
							password = password.substring(firstIdex+2,lastIdex);
						}
						return password;
					}
					return password;
				};
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.submitChanges = function(
						changes) {

					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "storedProcedureIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",
									"carnot:engine:camel::includeAttributesAsHeaders" : "true",
									"carnot:engine:camel::processContextHeaders" : "true",
									"carnot:engine:camel::inBodyAccessPoint":(this.inputBodyAccessPointInput.val()!=null && this.inputBodyAccessPointInput.val()!=m_constants.TO_BE_DEFINED)?this.inputBodyAccessPointInput.val():null,
									"carnot:engine:camel::outBodyAccessPoint":(this.outputBodyAccessPointInput.val()!=null && this.outputBodyAccessPointInput.val()!=m_constants.TO_BE_DEFINED)?this.outputBodyAccessPointInput.val():null,
									"stardust:sqlScriptingOverlay::sqlQuery" : this.codeEditor
											.getEditor().getSession()
											.getValue(),
									"stardust:sqlScriptingOverlay::connectionType" : this.connectionTypeSelect
											.val(),
									"stardust:sqlScriptingOverlay::databasetype" : this.databaseTypeSelect
											.val(),
									"stardust:sqlScriptingOverlay::hostname" : this.hostInput
											.val(),
									"stardust:sqlScriptingOverlay::port" : this.portInput
											.val(),
									"stardust:sqlScriptingOverlay::dbname" : this.dataBaseNameInput
											.val(),
									"stardust:sqlScriptingOverlay::username" : this.userNameInput
											.val(),
									"stardust:sqlScriptingOverlay::useCVforPassowrd" : this.useCVforPassowrdInput
											.prop("checked") ? this.useCVforPassowrdInput
											.prop("checked")
											: null,
									"stardust:sqlScriptingOverlay::password" : this.convertPasswordToConfigVariable(this.passwordInput
											.val()),
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"stardust:sqlScriptingOverlay::catalogName" : this.dbCatalogInput.val(),
									"stardust:sqlScriptingOverlay::schemaName" : this.dbSchemaInput.val(),
									"stardust:sqlScriptingOverlay::storedProcedureName" : this.spNameInput.val(),
									"stardust:sqlScriptingOverlay::useCustomSqlQuery" : this.customSqlQueryCbx
											.prop("checked") ? this.customSqlQueryCbx
											.prop("checked")
											: null
								}
							});
				};
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.getApplication().contexts.application.accessPoints = parameterDefinitionsChanges;
					this.view
							.submitChanges({
								contexts : {
									application : {
										accessPoints : parameterDefinitionsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "storedProcedureIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",
									"carnot:engine:camel::includeAttributesAsHeaders" : "true",
									"carnot:engine:camel::processContextHeaders" : "true",
									"stardust:sqlScriptingOverlay::sqlQuery" : this.codeEditor
											.getEditor().getSession()
											.getValue(),
									"stardust:sqlScriptingOverlay::connectionType" : this.connectionTypeSelect
											.val(),
									"stardust:sqlScriptingOverlay::databasetype" : this.databaseTypeSelect
											.val(),
									"stardust:sqlScriptingOverlay::hostname" : this.hostInput
											.val(),
									"stardust:sqlScriptingOverlay::port" : this.portInput
											.val(),
									"stardust:sqlScriptingOverlay::dbname" : this.dataBaseNameInput
											.val(),
									"stardust:sqlScriptingOverlay::username" : this.userNameInput
											.val(),
									"stardust:sqlScriptingOverlay::useCVforPassowrd" : this.useCVforPassowrdInput
											.prop("checked") ? this.useCVforPassowrdInput
											.prop("checked")
											: null,
									"stardust:sqlScriptingOverlay::password" : this.convertPasswordToConfigVariable(this.passwordInput
											.val()),
								/*	"carnot:engine:camel::additionalSpringBeanDefinitions" : this
											.populateDataSourceBeanDefinition(),*/
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									 "stardust:sqlScriptingOverlay::url" : (this.urlInput.val()!=null)?this.urlInput.val():null,
									 "stardust:sqlScriptingOverlay::driverClassName" : (this.driverInput.val()!=null)?this.driverInput.val():null,
									 
									 
									"stardust:sqlScriptingOverlay::catalogName" : this.dbCatalogInput.val(),
									"stardust:sqlScriptingOverlay::schemaName" : this.dbSchemaInput.val(),
									"stardust:sqlScriptingOverlay::storedProcedureName" : this.spNameInput.val(),
									"stardust:sqlScriptingOverlay::useCustomSqlQuery" : this.customSqlQueryCbx
											.prop("checked") ? this.customSqlQueryCbx
											.prop("checked")
											: null		
								}
							});
				};
				/**
				 * 
				 */
				StoredProcedureIntegrationOverlay.prototype.validate = function() {

					var valid = true;
					   if(this.connectionTypeSelect.val()=="direct" && (this.databaseTypeSelect.val()!="others" && this.databaseTypeSelect.val()!=m_constants.TO_BE_DEFINED) ){
						  this.showHideCommonDbConfig();
						  this.showHideOthersDbConfig(true);
						  this.hostInput.removeClass("error");
						  this.portInput.removeClass("error");
						  this.dataBaseNameInput.removeClass("error");
						  if(m_utils.isEmptyString(this.hostInput.val()) ){
							  this.view.errorMessages .push("No Data Source Host provided."); 
							  this.hostInput.addClass("error"); 
							  valid = false;
						  }
						 var numRegexp=new RegExp("[^0-9]");
						 if( numRegexp.test(this.portInput.val()) ||(m_utils.isEmptyString(this.portInput.val())) ||(Number(this.portInput.val() ) < 1  ||  Number(this.portInput.val()) > 65535)) {
							  this.view.errorMessages .push("Port number should be from 1-65535.");
							  this.portInput.addClass("error"); 
							  valid = false;
						  }
						  if(m_utils.isEmptyString(this.dataBaseNameInput.val()) ){
							  this.view.errorMessages .push("No Data Source Name provided.");
							  this.dataBaseNameInput.addClass("error"); 
							  valid = false;
						  }
					   }
					if(this.connectionTypeSelect.val()=="direct" && (this.databaseTypeSelect.val() =="others") ){
							 // when using others connection verify url/driver
							 this.showHideCommonDbConfig(true);
							 this.showHideOthersDbConfig();
							 this.urlInput.removeClass("error");
							 this.driverInput.removeClass("error");
					   if(m_utils.isEmptyString(this.urlInput.val()) ){
							  this.view.errorMessages .push("No URL provided.");
							  this.urlInput.addClass("error"); 
							  valid = false;
						  }
						if(m_utils.isEmptyString(this.driverInput.val()) ){
							  this.view.errorMessages .push("No Driver provided.");
							  this.driverInput.addClass("error"); 
							  valid = false;
						  }
					   }

						// validation for configuration tab
						if(this.customSqlQueryCbx.is(":checked")){
						 if(m_utils.isEmptyString(this.codeEditor.getEditor().getSession().getValue())){
							   this.view.errorMessages .push("No SQL Query provided."); 
							   valid = false;
						   }

						}else{
							this.dbCatalogInput.removeClass("error");
							this.dbSchemaInput.removeClass("error");
							this.spNameInput.removeClass("error");
							if(m_utils.isEmptyString(this.dbCatalogInput.val())){
								 this.view.errorMessages.push("No Catalog provided."); 
								 this.dbCatalogInput.addClass("error"); 
								 valid = false;
							}
							if(m_utils.isEmptyString(this.dbSchemaInput.val())){
								 this.view.errorMessages.push("No Schema provided."); 
								 this.dbSchemaInput.addClass("error"); 
								 valid = false;
							}
							if(m_utils.isEmptyString(this.spNameInput.val())){
								 this.view.errorMessages.push("No Name provided."); 
								 this.spNameInput.addClass("error"); 
								 valid = false;
							}
						}
						this.userNameInput.removeClass("error");
						if(m_utils.isEmptyString(this.userNameInput.val())){
								 this.view.errorMessages.push("No User provided."); 
								 this.userNameInput.addClass("error"); 
								 valid = false;
							}

					
					return true;
				};
				
			}
		});