define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_typeDeclaration",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_codeEditorAce" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_typeDeclaration,
				m_parameterDefinitionsPanel, m_codeEditorAce) {
			return {
				create : function(view) {
					var overlay = new RulesIntegrationOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function RulesIntegrationOverlay() {
				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.view.insertPropertiesTab("rulesIntegrationOverlay",
							"parameters", "Parameters",
							"../../images/icons/mapping.gif");
					this.view.insertPropertiesTab("rulesIntegrationOverlay",
							"drl", "DRL", "../../images/icons/bricks.png");

					this.typeDeclarationsTextarea = jQuery("#rulesIntegrationOverlay #typeDeclarationsTextarea")
					this.ruleSetEditor = m_codeEditorAce
							.getDrlEditor("ruleSetEditorDiv");

					var self = this;

					this.ruleSetEditor.getEditor().on('blur', function(e) {
						self.submitDrlChanges();
					});

					this.drlTextarea = jQuery("#drlTab #drlTextarea");

					this.typeDeclarationsTextarea.prop("disabled", true);
					this.drlTextarea.prop("disabled", true);

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true
							});
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "rulesIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries":"<to uri=\"isb://service/BRMS/stateless\"/>",
									"stardust:rulesOverlay::drl" : this
											.createDrl(),
									"stardust:rulesOverlay::ruleSetDrl" : this.ruleSetEditor
											.getValue()
								}
							});
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.createDrl = function() {
					var drl = "";

					drl += this.createTypeDeclarationsDrl();
					drl += this.ruleSetEditor.getValue();

					return drl;
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.createTypeDeclarationsDrl = function() {
					var typeDeclarations = {};
					var drl = "";

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.dataType != "struct"
								|| !accessPoint.structuredDataTypeFullId
								|| typeDeclarations[accessPoint.structuredDataTypeFullId]) {
							continue;
						}

						var typeDeclaration = m_model
								.findTypeDeclaration(accessPoint.structuredDataTypeFullId)

						typeDeclarations[accessPoint.structuredDataTypeFullId] = typeDeclaration;

						drl += this.createTypeDeclarationDrl(typeDeclarations,
								typeDeclaration);
					}

					return drl;
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.createTypeDeclarationDrl = function(
						typeDeclarations, typeDeclaration) {
					var drl = "";
					var self = this;

					// Create DRL for dependent structures first

					jQuery
							.each(
									typeDeclaration.getBody().elements,
									function(i, element) {
										var type = element.type;

										// Strip prefix

										if (element.type.indexOf(':') !== -1) {
											type = element.type.split(":")[1];
										}

										var childTypeDeclaration = typeDeclaration.model
												.findTypeDeclarationBySchemaName(type);

										if (childTypeDeclaration != null) {
											if (childTypeDeclaration
													.isSequence()
													&& !typeDeclarations[childTypeDeclaration
															.getFullId()]) {

												drl += self
														.createTypeDeclarationDrl(
																typeDeclarations,
																childTypeDeclaration);
											} else {
												drl += "   "
														+ element.name
														+ ": "
														+ self
																.mapXsdTypeToJava("xsd:string")
														+ ";\n";
											}
										}
									});

					// Create the type declaration itself

					drl += "declare " + typeDeclaration.id + "\n";

					jQuery.each(typeDeclaration.getBody().elements, function(i,
							element) {
						var type = element.type;

						// Strip prefix

						if (element.type.indexOf(':') !== -1) {
							type = element.type.split(":")[1];
						}

						var childTypeDeclaration = typeDeclaration.model
								.findTypeDeclarationBySchemaName(type);

						if (!childTypeDeclaration) {
							drl += "   " + element.name + ": "
									+ self.mapXsdTypeToJava(element.type)
									+ ";\n";
						} else {
							drl += "   " + element.name + ": "
									+ childTypeDeclaration.id + ";\n";
						}
					});

					drl += "end\n\n";

					return drl;
				}

				/**
				 * TODO Move to TypeDeclaration
				 */
				RulesIntegrationOverlay.prototype.mapXsdTypeToJava = function(
						xsdType) {
					if (xsdType == "xsd:decimal") {
						return "java.math.BigDecimal";
					} else if (xsdType == "xsd:byte") {
						return "byte";
					} else if (xsdType == "xsd:short") {
						return "short";
					} else if (xsdType == "xsd:int") {
						return "int";
					} else if (xsdType == "xsd:long") {
						return "long";
					} else if (xsdType == "xsd:float") {
						return "float";
					} else if (xsdType == "xsd:double") {
						return "double";
					} else if (xsdType == "xsd:boolean") {
						return "boolean";
					} else if (xsdType == "xsd:date" || xsdType == "xsd:time"
							|| xsdType == "xsd:dateTime") {
						return "java.util.Date";
					} else {
						return "String";
					}
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.update = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					var drl = this.createTypeDeclarationsDrl();

					this.typeDeclarationsTextarea.val(drl);
					this.ruleSetEditor
							.getEditor()
							.getSession()
							.setValue(
									this.getApplication().attributes["stardust:rulesOverlay::ruleSetDrl"]);
					this.drlTextarea.val(this.createDrl());
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.submitDrlChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "rulesIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries":"<to uri=\"isb://service/BRMS/stateless\"/>",
									"stardust:rulesOverlay::drl" : this
											.createDrl(),
									"stardust:rulesOverlay::ruleSetDrl" : this.ruleSetEditor
											.getValue()
								}
							});
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.createSignatureJson = function(
						parameterDefinitions) {
					var signatureJson = {};
					var typeDeclarations = [];

					signatureJson.input = {};

					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var parameterDefinition = parameterDefinitions[n];

						if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						if (parameterDefinition.dataType == "struct") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

							signatureJson.input[parameterDefinition.id] = typeDeclaration.id;

							typeDeclarations.push(typeDeclaration);
						}
					}

					signatureJson.output = {};

					for ( var n = 0; n < parameterDefinitions.length; ++n) {
						var parameterDefinition = parameterDefinitions[n];

						if (parameterDefinition.direction == m_constants.IN_ACCESS_POINT) {
							continue;
						}

						if (parameterDefinition.dataType == "struct") {
							var typeDeclaration = m_model
									.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

							signatureJson.output[parameterDefinition.id] = typeDeclaration.id;

							typeDeclarations.push(typeDeclaration);
						}
					}

					signatureJson.structures = m_typeDeclaration
							.generateJsonRepresentation(typeDeclarations);

					m_utils.debug(signatureJson);
					m_utils.debug(JSON.stringify(signatureJson, null, 3));

					return signatureJson;
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								contexts : {
									application : {
										accessPoints : parameterDefinitionsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "rulesIntegrationOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries":"<to uri=\"isb://service/BRMS/stateless\"/>",
									"stardust:rulesOverlay::drl" : this
											.createDrl(),
									"stardust:rulesOverlay::ruleSetDrl" : this.ruleSetEditor
											.getValue(),
									"stardust:rulesOverlay::signatureDefinition" : JSON
											.stringify(
													this
															.createSignatureJson(parameterDefinitionsChanges),
													null, 3)
								}
							});
				};

				/**
				 * 
				 */
				RulesIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});