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
					var overlay = new ScriptingIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 *
			 */
			function ScriptingIntegrationOverlay() {
				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.initialize = function(
						view) {
					this.view = view;

					this.view
							.insertPropertiesTab(
									"scriptingIntegrationOverlay",
									"parameters",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.parameters.title"),
									"plugins/bpm-modeler/images/icons/table.png");
					this.view
							.insertPropertiesTab(
									"scriptingIntegrationOverlay",
									"test",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.test.title"),
									"plugins/bpm-modeler/images/icons/table.png");

					this.scriptCodeHeading = m_utils.jQuerySelect("#scriptingIntegrationOverlay #scriptCodeHeading");
					this.languageSelect = m_utils.jQuerySelect("#scriptingIntegrationOverlay #languageSelect");
					this.codeEditor = m_codeEditorAce
							.getJSCodeEditor("codeEditorDiv");
					this.resetButton = m_utils.jQuerySelect("#testTab #resetButton");
					this.runButton = m_utils.jQuerySelect("#testTab #runButton");
					this.inputDataTextarea = m_utils.jQuerySelect("#testTab #inputDataTextarea");
					this.outputDataTextarea = m_utils.jQuerySelect("#testTab #outputDataTextarea");
					this.inputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = m_utils.jQuerySelect("#parametersTab #outputBodyAccessPointInput");

					this.scriptCodeHeading.empty();
					this.scriptCodeHeading
							.append(m_i18nUtils
									.getProperty("modeler.model.applicationOverlay.scripting.code.heading"));

					var self = this;

					m_utils.jQuerySelect("a[href='#configurationTab']").click(function() {
						self.setGlobalVariables();
					});

					this.languageSelect
							.change(function() {
								var code = self.codeEditor.getEditor()
										.getSession().getValue();

								if (self.languageSelect.val() == "JavaScript") {
									self.codeEditor = m_codeEditorAce
											.getJSCodeEditor("codeEditorDiv");
								} else if (self.languageSelect.val() == "Python") {
									self.codeEditor = m_codeEditorAce
											.getJSCodeEditor("codeEditorDiv");
								} else if (self.languageSelect.val() == "Groovy") {
									self.codeEditor = m_codeEditorAce
											.getJSCodeEditor("codeEditorDiv");
								}

								self.codeEditor.getEditor().getSession()
										.setValue(code);
								self.submitChanges();
							});
					this.codeEditor.getEditor().on('blur', function(e) {
						self.submitChanges();
					});
					this.resetButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.test.resetButton.title"));
					this.runButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.test.runButton.title"));
					m_utils.jQuerySelect("label[for='inputDataTextArea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.test.inputDataTextArea.label"));
					m_utils.jQuerySelect("label[for='outputDataTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.applicationOverlay.scripting.test.outputDataTextArea.label"));

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true,
								supportsDocumentTypes : false
							});

					this.runButton
							.click(function() {
								var functionBody = "var input = "
										+ self.inputDataTextarea.val() + ";\n";

								functionBody += "var output = "
										+ self.createParameterObjectString(
												m_constants.OUT_ACCESS_POINT,
												false) + ";\n";

								var code = self.codeEditor.getEditor()
										.getSession().getValue();

								// Convert Input and Output Access Points

								for ( var n = 0; n < self.getApplication().contexts.application.accessPoints.length; ++n) {
									var accessPoint = self.getApplication().contexts.application.accessPoints[n];

									// \b is to demarcate whole words only

									if (accessPoint.direction === m_constants.IN_ACCESS_POINT) {
										code = code.replace(new RegExp("\\b"
												+ accessPoint.id + "\\b", "g"),
												"input." + accessPoint.id);
									}

									if (accessPoint.direction === m_constants.OUT_ACCESS_POINT) {
										code = code.replace(new RegExp("\\b"
												+ accessPoint.id + "\\b", "g"),
												"output." + accessPoint.id);
									}
								}

								functionBody += code + "\n";

								functionBody += "return output;";

								m_utils.debug(functionBody);

								var mappingFunction = new Function(functionBody);

								var result = mappingFunction();

								self.outputDataTextarea.val(JSON
										.stringify(result));
							});
					this.resetButton.click(function() {
						self.inputDataTextarea.empty();
						self.outputDataTextarea.empty();

						self.inputDataTextarea.append(self
								.createParameterObjectString(
										m_constants.IN_ACCESS_POINT, true));
					});
				this.update();
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.createParameterObjectString = function(
						direction, initializePrimitives, singleVariables, identifier) {
					var otherDirection;
					if (direction === m_constants.IN_ACCESS_POINT) {
						otherDirection = m_constants.OUT_ACCESS_POINT;
					} else {
						otherDirection = m_constants.IN_ACCESS_POINT;
					}

					var parameterObjectString = "";

					if (!singleVariables) {
						parameterObjectString += "{";
					}

					var index = 0;

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

						if (parameterDefinition.direction === otherDirection) {
							continue;
						}

						if (index > 0 && !singleVariables) {
							parameterObjectString += ", ";
						}

						++index;

						if (parameterDefinition.dataType == "primitive") {
							if (initializePrimitives || singleVariables) {
								if (singleVariables) {
									parameterObjectString += identifier;
								}

								parameterObjectString += parameterDefinition.id;

								if (singleVariables) {
									parameterObjectString += " = ";
								} else {
									parameterObjectString += ": ";
								}

								if (parameterDefinition.primitiveDataType === "String") {
									parameterObjectString += "\"\"";
								} else if (parameterDefinition.primitiveDataType === "Boolean") {
									parameterObjectString += "false";
								} else {
									parameterObjectString += "0";
								}

								if (singleVariables) {
									parameterObjectString += ";\n";
								}
							}
						} else if (parameterDefinition.dataType == "struct") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
							if (singleVariables) {
								parameterObjectString += identifier;
							}

							parameterObjectString += parameterDefinition.id;
							if (singleVariables) {
								parameterObjectString += " = ";
							} else {
								parameterObjectString += ": ";
							}

							parameterObjectString += JSON
									.stringify(
											typeDeclaration
													.createInstance({
														initializePrimitives : initializePrimitives
													}), null, 3);

							if (singleVariables) {
								parameterObjectString += ";\n";
							}
						} else if (parameterDefinition.dataType == "dmsDocument") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

							if (singleVariables) {
								parameterObjectString += identifier;
							}

							parameterObjectString += parameterDefinition.id;

							if (singleVariables) {
								parameterObjectString += " = ";
							} else {
								parameterObjectString += ": ";
							}

							parameterObjectString += JSON
									.stringify(
											typeDeclaration
													.createInstance({
														initializePrimitives : initializePrimitives
													}), null, 3);

							if (singleVariables) {
								parameterObjectString += ";\n";
							}
						}
					}

					if (!singleVariables) {
						parameterObjectString += "}";
					}

					return parameterObjectString;
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::applicationIntegrationOverlay" : "scriptingIntegrationOverlay"
								}
							});
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.update = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);
					this.languageSelect
							.val(this.getApplication().attributes["stardust:scriptingOverlay::language"]);
					this.codeEditor
							.getEditor()
							.getSession()
							.setValue(
									this.getApplication().attributes["stardust:scriptingOverlay::scriptCode"]);
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.getRoute = function() {
					var route = "";
					if (this.languageSelect.val() === "JavaScript") {
					var code = "function setOutHeader(key, output){\nexchange.out.headers.put(key,output);}\n";

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];
						if (accessPoint.direction === m_constants.IN_ACCESS_POINT) {
							if (accessPoint.dataType == "primitive") {
								code += "var " + accessPoint.id + ";\n";
								code += "if(request.headers.get('"
										+ accessPoint.id + "')!=null){\n"
								code += accessPoint.id
										+ " =  request.headers.get('"
										+ accessPoint.id + "');\n";
								code += "}\n";

							} else if (accessPoint.dataType == "struct") {
								code += "var " + accessPoint.id + ";\n";
								code += "if(request.headers.get('"
										+ accessPoint.id + "')!=null){\n"
								code += accessPoint.id
										+ " =  eval('(' + request.headers.get('"
										+ accessPoint.id + "')+ ')');\n";
								code += "}\n";
							}
						}
					}

					code += this.createParameterObjectString(
							m_constants.OUT_ACCESS_POINT, false, true,"var ");
					code += this.codeEditor.getEditor().getSession()
							.getValue();

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
							code += "\nsetOutHeader('" + accessPoint.id
									+ "'," + accessPoint.id + ");";
						}
					}
					code = code.replace(/&/g, "&amp;");
					code = code.replace(/</g, "&lt;");
					code = code.replace(/>/g, "&gt;");
					// Determine language

					route += "<to uri=\"bean:bpmTypeConverter?method=toNativeObject\" /><setHeader headerName=\"CamelLanguageScript\"><constant>"
							+ code
							+ "</constant></setHeader><to uri=\"language:rhino-nonjdk\" /><to uri=\"bean:bpmTypeConverter?method=fromNativeObject\" />\n";
					} else if (this.languageSelect.val() === "Groovy") {
						route += "<setHeader headerName=\"CamelLanguageScript\"><constant>"
								+ code
								+ "</constant></setHeader><to uri=\"language:groovy\" />\n";
					} else {
						var code = "import json\nimport pprint\n";
						for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
							var accessPoint = this.getApplication().contexts.application.accessPoints[n];
							if (accessPoint.direction === m_constants.IN_ACCESS_POINT) {
								if (accessPoint.dataType == "primitive") {
									code += accessPoint.id + "= exchange.getIn().getHeader('"+ accessPoint.id + "')\n";
								} else if (accessPoint.dataType == "struct") {
									code += accessPoint.id + "JSON= exchange.getIn().getHeader('"+ accessPoint.id + "')\n";
									code += accessPoint.id + "=json.loads("+accessPoint.id + "JSON)\n";
								}
							}
						}

						code += this.createParameterObjectString(
								m_constants.OUT_ACCESS_POINT, false, true,"");
						code += this.codeEditor.getEditor().getSession()
								.getValue();

						for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
							var accessPoint = this.getApplication().contexts.application.accessPoints[n];

							if (accessPoint.direction == m_constants.OUT_ACCESS_POINT) {
								code += "\nexchange.getOut().setHeader('" + accessPoint.id + "'," + accessPoint.id + ");";
							}
						}
						code = code.replace(/&/g, "&amp;");
						code = code.replace(/</g, "&lt;");
						code = code.replace(/>/g, "&gt;");

						
						
						route += "<to uri=\"bean:bpmTypeConverter?method=toJSON\" /><setHeader headerName=\"CamelLanguageScript\"><constant>"
								+ code
								+ "</constant></setHeader><to uri=\"language:python\" />\n";
					}
					m_utils.debug(route);

					return route;
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.submitChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "scriptingIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"stardust:scriptingOverlay::language" : this.languageSelect
											.val(),
									"stardust:scriptingOverlay::scriptCode" : this.codeEditor
											.getEditor().getSession()
											.getValue()
								}
							});
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
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
									"carnot:engine:camel::applicationIntegrationOverlay" : "scriptingIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"stardust:scriptingOverlay::scriptCode" : this.codeEditor
											.getEditor().getSession()
											.getValue()
								}
							});
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.setGlobalVariables = function() {
					// Global variables for Code Editor auto-complete / validation
					var globalVariables = {};

					for (var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var parameterDefinition = this.getApplication().contexts.application.accessPoints[n];

						var typeDeclaration = null;
						if (parameterDefinition.dataType == "struct") {
							typeDeclaration = m_model.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);
						}

						if (typeDeclaration != null) {
							globalVariables[parameterDefinition.id] = typeDeclaration.createInstance();
						}
						else {
							globalVariables[parameterDefinition.id] = "";
						}
					}

					this.codeEditor.setGlobalVariables(globalVariables);
				};

				/**
				 *
				 */
				ScriptingIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});