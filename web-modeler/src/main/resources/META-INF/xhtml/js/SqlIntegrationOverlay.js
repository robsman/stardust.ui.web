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
					var overlay = new SqlIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function SqlIntegrationOverlay() {
				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.view
							.insertPropertiesTab(
									"sqlIntegrationOverlay",
									"parameters",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
									"plugins/bpm-modeler/images/icons/database_link.png");

					this.view
							.insertPropertiesTab("sqlIntegrationOverlay",
									"dataSource", "Data Source",
									"plugins/bpm-modeler/images/icons/database_link.png");

					this.sqlQueryHeading = m_utils
							.jQuerySelect("#sqlIntegrationOverlay #sqlQueryHeading");
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

					this.connectionTypeSelect.empty();
					this.connectionTypeSelect
							.append("<option value='direct' selected>Direct</option>");
					this.connectionTypeSelect
							.append("<option value='jndi'>JNDI</option>");

					this.directConfigTab = m_utils
							.jQuerySelect("#dataSourceTab #directConfigTab");
					this.jndiConfigTab = m_utils.jQuerySelect("#jndiConfigTab");

					this.othersDbConfig = m_utils
							.jQuerySelect("#othersDbConfig");
					this.othersDbConfig.hide();// hide by default; show only
					// when others is selected
					this.commonDbConfig = m_utils
							.jQuerySelect("#commonDbConfig");

					this.databaseTypeSelect.empty();
					this.databaseTypeSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "'>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");
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
							.change(function() {
								if (!self.view.validate()) {
									return;
								}
								if (self.databaseTypeSelect.val() == m_constants.TO_BE_DEFINED) {
									self.commonDbConfig.show();
									self.othersDbConfig.hide();
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::databasetype",
													null);
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::url",
													null);
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::driverClassName",
													null);

								} else if (self.databaseTypeSelect.val() == "others") {
									self.commonDbConfig.hide();
									self.othersDbConfig.show();
								} else {
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::databasetype",
													self.databaseTypeSelect
															.val());
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::url",
													null);
									self.view
											.submitModelElementAttributeChange(
													"stardust:sqlScriptingOverlay::driverClassName",
													null);
								}
							});

					this.urlInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.view.submitModelElementAttributeChange(
								"stardust:sqlScriptingOverlay::url",
								this.urlInput.val());

						// self.submitChanges();
					});
					this.driverInput
							.change(function() {
								if (!self.view.validate()) {
									return;
								}
								self.view
										.submitModelElementAttributeChange(
												"stardust:sqlScriptingOverlay::driverClassName",
												this.driverInput.val());
								// self.submitChanges();
							});
					this.hostInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.submitChanges();
					});
					this.portInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.submitChanges();
					});

					this.dataBaseNameInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.submitChanges();
					});
					this.userNameInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.submitChanges();
					});
					this.passwordInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.submitChanges();
					});
					this.useCVforPassowrdInput.change(function() {
						self.submitChanges();
					});

					var self = this;

					this.codeEditor.getEditor().on('blur', function(e) {
						self.submitChanges();
					});
					this.inputBodyAccessPointInput
							.change(function() {
								if (!self.view.validate()) {
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
									// self.inputBodyAccessPointInput.val(
									// m_constants.TO_BE_DEFINED);

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
										// if
										// (!accessPoints[self.outputBodyAccessPointInput.val()])
										// {

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
							.change(function() {
								if (!self.view.validate()) {
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
										// if
										// (!accessPoints[self.outputBodyAccessPointInput.val()])
										// {

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
								supportsOrdering : false,
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
				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",
									"carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay"
								}
							});

					// Predefined access points

					this.view.submitChanges({
						contexts : {
							application : {
								accessPoints : this
										.createIntrinsicAccessPoints()
							}
						}
					});
				};

				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.update = function() {
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
					/*
					 * this.dataSourceNameInput
					 * .val(this.getApplication().attributes["stardust:sqlOverlay::dataSourceId"]);
					 */
					// Initialize the UI to show only primitives IN only
					this.parameterDefinitionDirectionSelect = m_utils
							.jQuerySelect("#parametersTab #parameterDefinitionDirectionSelect");
					this.parameterDefinitionDirectionSelect.empty();
					var direction = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.in");
					this.parameterDefinitionDirectionSelect
							.append("<option value=\"IN\">" + direction
									+ "</option>");

					this.dataTypeSelect = m_utils
							.jQuerySelect("#parametersTab #dataTypeSelect");
					this.dataTypeSelect.empty();
					this.dataTypeSelect
							.append("<option value='primitive' selected>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.commonProperties.primitive")
									+ "</option>");

					this.connectionTypeSelect
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::connectionType"]);
					this.databaseTypeSelect
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::databasetype"]);

					if (this.getApplication().attributes["stardust:sqlScriptingOverlay::databasetype"] == "others") {
						this.urlInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::url"]);
						this.driverInput
								.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::driverClassName"]);
						this.commonDbConfig.hide();
						this.othersDbConfig.show();

					} else {
						this.commonDbConfig.show();
						this.othersDbConfig.hide();

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
							.val(this.getApplication().attributes["stardust:sqlScriptingOverlay::password"]);
					this.useCVforPassowrdInput
							.prop(
									"checked",
									this.getApplication().attributes["stardust:sqlScriptingOverlay::useCVforPassowrd"]);
				};
				SqlIntegrationOverlay.prototype.createIntrinsicAccessPoints = function() {
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
							name : "CamelSqlQuery",
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
							name : "CamelSqlUpdateCount",
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
							name : "CamelSqlRowCount",
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

				SqlIntegrationOverlay.prototype.populateDataStructuresSelectInput = function(
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

				SqlIntegrationOverlay.prototype.populateDataSourceBeanDefinition = function() {
					if (!this.view.validate()) {
						return;
					}

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
				SqlIntegrationOverlay.prototype.getDataSourceName = function() {
					return this.getApplication().id + "Ds";
				}
				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.getRoute = function() {
					var route = "";
					var sqlQuery = this.codeEditor.getEditor().getSession()
							.getValue();
					var dataSourceName = "";
					/* var dataSourceName = this.dataSourceNameInput.val(); */

					if (sqlQuery != null && sqlQuery != "") {

						sqlQuery = sqlQuery.replace(/&/g, "&amp;");
						sqlQuery = sqlQuery.replace(/</g, "&lt;");
						sqlQuery = sqlQuery.replace(/>/g, "&gt;");
					}

					route += "<to uri=\"sql:"
							+ sqlQuery
							+ "?dataSource=#"
							+ this.getDataSourceName()
							+ "&alwaysPopulateStatement=true&prepareStatementStrategy=#sqlPrepareStatementStrategy\" />";

					var outBodyAccessPoint = this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"];
					if (this.getApplication().contexts.application.accessPoints.length > 0) {
						for (i = 0; i < this.getApplication().contexts.application.accessPoints.length; i++) {
							var accessPoint = this.getApplication().contexts.application.accessPoints[i];
							if (accessPoint.direction == "OUT"
									&& outBodyAccessPoint != null
									&& outBodyAccessPoint != ""
									&& outBodyAccessPoint == accessPoint.id) {
								route += "<setHeader headerName=\""
										+ accessPoint.id + "\">";
								route += "<simple>$simple{body}</simple>"
								route += "</setHeader>";
							} else if (accessPoint.direction == "OUT") {
								/*
								 * route += "<setHeader headerName=\"" +
								 * accessPoint.id + "\">"; route += "<simple>$simple{header."+accessPoint.id+"}</simple>"
								 * route += "</setHeader>";
								 */
							}
						}
					}
					route += "<to uri=\"bean:bpmTypeConverter?method=fromList\"/>"

					m_utils.debug(route);
					route = route.replace(/&/g, "&amp;");
					return route;
				};

				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.submitChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",
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
									"stardust:sqlScriptingOverlay::password" : this.passwordInput
											.val(),

									"carnot:engine:camel::additionalSpringBeanDefinitions" : this
											.populateDataSourceBeanDefinition(),
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
								}
							});
				};
				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
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
									"carnot:engine:camel::applicationIntegrationOverlay" : "sqlIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::invocationPattern" : "sendReceive",
									"carnot:engine:camel::invocationType" : "synchronous",

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
									"stardust:sqlScriptingOverlay::password" : this.passwordInput
											.val(),
									"carnot:engine:camel::additionalSpringBeanDefinitions" : this
											.populateDataSourceBeanDefinition(),
									"carnot:engine:camel::routeEntries" : this
											.getRoute()
								}
							});
				};
				/**
				 * 
				 */
				SqlIntegrationOverlay.prototype.validate = function() {
					var valid = true;
					 this.outputBodyAccessPointInput.removeClass("error");
					if(m_utils.isEmptyString(this.outputBodyAccessPointInput.val()) || this.outputBodyAccessPointInput.val()==m_constants.TO_BE_DEFINED){
						 this.view.errorMessages .push("No Out Mapping provided."); 
						 this.outputBodyAccessPointInput.addClass("error"); 
						 valid = false;
					}
					
					
					if(m_utils.isEmptyString(this.codeEditor.getEditor().getSession().getValue())){
						 this.view.errorMessages .push("No SQL Query provided."); 
						 valid = false;
					}

					if(this.connectionTypeSelect.val()=="direct" && (this.databaseTypeSelect.val()!="others" && this.databaseTypeSelect.val()!=m_constants.TO_BE_DEFINED) ){
						//when using direct connection verify host,port,databasename
						this.hostInput.removeClass("error");
						this.portInput.removeClass("error");
						this.dataBaseNameInput.removeClass("error");
						
						if(m_utils.isEmptyString(this.hostInput.val()) ){
							 this.view.errorMessages .push("No Host provided."); 
							 this.hostInput.addClass("error"); 
							 valid = false;
						}
						
						if(m_utils.isEmptyString(this.portInput.val()) ){
							 this.view.errorMessages .push("No port number provided.");
							 this.portInput.addClass("error"); 
							 valid = false;
						}
						if(m_utils.isEmptyString(this.dataBaseNameInput.val()) ){
							 this.view.errorMessages .push("No SID name provided.");
							 this.dataBaseNameInput.addClass("error"); 
							 valid = false;
						}
					}
					
					return valid;
				};
			}
		});