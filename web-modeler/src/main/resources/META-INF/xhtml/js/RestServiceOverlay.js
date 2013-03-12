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
					var overlay = new RestServiceOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function RestServiceOverlay() {
				/**
				 * 
				 */
				RestServiceOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.view.insertPropertiesTab("restServiceOverlay",
							"parameters", "Parameters",
							"../../images/icons/table.png");
					this.view.insertPropertiesTab("restServiceOverlay", "test",
							"Test", "../../images/icons/table.png");

					this.uriInput = jQuery("#restServiceOverlay #uriInput");
					this.queryStringLabel = jQuery("#restServiceOverlay #queryStringLabel");
					this.commandSelect = jQuery("#restServiceOverlay #commandSelect");
					this.requestTypeSelect = jQuery("#restServiceOverlay #requestTypeSelect");
					this.responseTypeSelect = jQuery("#restServiceOverlay #responseTypeSelect");
					this.resetButton = jQuery("#testTab #resetButton");
					this.runButton = jQuery("#testTab #runButton");
					this.inputDataTextarea = jQuery("#testTab #inputDataTextarea");
					this.outputDataTextarea = jQuery("#testTab #outputDataTextarea");
					this.inputBodyAccessPointInput = jQuery("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = jQuery("#parametersTab #outputBodyAccessPointInput");

					this.resetButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.resetButton.title"));
					this.runButton
							.prop(
									"title",
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.runButton.title"));
					jQuery("label[for='inputDataTextArea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.inputDataTextArea.label"));
					jQuery("label[for='outputDataTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.model.propertyView.uiMashup.test.outputDataTextArea.label"));

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true
							});

					var self = this;

					this.uriInput.change(function() {
						self.submitChanges();
					});
					this.commandSelect.change(function() {
						self.submitChanges();
					});
					this.requestTypeSelect.change(function() {
						self.submitChanges();
					});
					this.responseTypeSelect.change(function() {
						self.submitChanges();
					});
					this.inputBodyAccessPointInput
							.change(function() {
								if (!self.view.validate()) {
									return;
								}

								if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::inputBodyAccessPoint",
													null);
								} else {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::inputBodyAccessPoint",
													self.inputBodyAccessPointInput
															.val());
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
													"carnot:engine:camel::outputBodyAccessPoint",
													null);
								} else {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::outputBodyAccessPoint",
													self.outputBodyAccessPointInput
															.val());
								}
							});

					this.runButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										jQuery.support.cors = true;

										jQuery
												.ajax(
														{
															type : view.commandSelect
																	.val(),
															url : view
																	.getInvocationUri(),
															contentType : view.requestTypeSelect
																	.val(),
															dataType : view.responseTypeSelect
																	.val(),
															data : view
																	.getRequestData(),
															crossDomain : true
														})
												.done(
														function(data) {
															view.outputDataTextarea
																	.val(JSON
																			.stringify(data));
														})
												.fail(
														function() {
															view.outputDataTextarea
																	.val("Could not retrieve data from "
																			+ view
																					.getInvocationUri()
																			+ " via "
																			+ view.commandSelect
																					.val()
																			+ " command.");
														});
									});
					this.resetButton
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view.inputDataTextarea.empty();
										view.outputDataTextarea.empty();

										var inputData = "{";

										for ( var n = 0; n < view
												.getApplication().contexts.application.accessPoints.length; ++n) {
											var parameterDefinition = view
													.getApplication().contexts.application.accessPoints[n];

											if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
												continue;
											}

											if (n > 0) {
												inputData += ", ";
											}

											if (parameterDefinition.dataType == "primitive") {
												inputData += parameterDefinition.id;
												inputData += ": \"\"";
											} else if (parameterDefinition.dataType == "struct") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} else if (parameterDefinition.dataType == "dmsDocument") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} else {
												// Deal with primitives
											}
										}

										inputData += "}";

										view.inputDataTextarea
												.append(inputData);
									});
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getQueryString = function() {
					var queryString = "";

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
								||
								// TODO May collide with Configuration Variables
								this.uriInput.val().indexOf(
										"{" + accessPoint.id + "}") >= 0
								|| accessPoint.id == this.inputBodyAccessPointInput
										.val()) {
							continue;
						}

						if (n > 0) {
							queryString += "&";
						}

						queryString += accessPoint.id;
						queryString += "=<i>";
						queryString += accessPoint.id;
						queryString += "</i>";
					}

					return queryString;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getInvocationUri = function() {
					var uri = this.uriInput.val();

					if (this.inputDataTextarea.val()) {
						var input = eval("(" + this.inputDataTextarea.val()
								+ ")");
						var start = true;

						for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
							var accessPoint = this.getApplication().contexts.application.accessPoints[n];

							if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
									|| accessPoint.id == this.inputBodyAccessPointInput
											.val()) {
								continue;
							}

							if (this.uriInput.val().indexOf(
									"{" + accessPoint.id + "}") >= 0) {
								uri = uri.replace("{" + accessPoint.id + "}",
										input[accessPoint.id]);
							} else {
								if (start) {
									uri += "?";
									start = false;
								} else {
									uri += "&";
								}

								uri += accessPoint.id;
								uri += "=";
								uri += input[accessPoint.id];
							}
						}
					}

					return uri;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getRequestData = function() {
					if (this.inputBodyAccessPointInput.val() != m_constants.TO_BE_DEFINED
							&& this.inputDataTextarea.val()) {
						var input = eval("(" + this.inputDataTextarea.val()
								+ ")");

						return input[this.inputBodyAccessPointInput.val()];
					}

					return null;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.getRoute = function() {
					var uri = this.uriInput.val();
					var start = true;

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction == m_constants.OUT_ACCESS_POINT
								|| accessPoint.id == this.inputBodyAccessPointInput
										.val()) {
							continue;
						}

						if (this.uriInput.val().indexOf(
								"{" + accessPoint.id + "}") >= 0) {
							uri = uri.replace("{" + accessPoint.id + "}",
									"{header." + accessPoint.id + "}");
						} else {
							if (start) {
								uri += "?";
								start = false;
							} else {
								uri += "&";
							}

							uri += accessPoint.id;
							uri += "=";
							uri += "{header." + accessPoint.id + "}";
						}
					}

					var route = "";

					route += "<setHeader headerName='CamelHttpMethod'>\n";
					route += "\t<constant>" + this.commandSelect.val()
							+ "</constant>\n";
					route += "</setHeader>\n";
					route += "<to uri='" + uri + "'/>\n"

					return route;
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.update = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					this.inputBodyAccessPointInput.empty();
					this.inputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "'>"
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.IN_ACCESS_POINT) {
							continue;
						}

						this.inputBodyAccessPointInput.append("<option value='"
								+ accessPoint.id + "'>" + accessPoint.name
								+ "</option>");
					}

					this.outputBodyAccessPointInput.empty();
					this.outputBodyAccessPointInput.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "' selected>"
							+ m_i18nUtils.getProperty("None") // I18N
							+ "</option>");

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						this.outputBodyAccessPointInput
								.append("<option value='" + accessPoint.id
										+ "'>" + accessPoint.name + "</option>");
					}

					this.inputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::inputBodyAccessPoint"]);
					this.outputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::outputBodyAccessPoint"]);
					this.uriInput
							.val(this.getApplication().attributes["stardust:restServiceOverlay::uri"]);
					this.queryStringLabel.empty();
					this.queryStringLabel.append(this.getQueryString());
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.submitChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "restServiceOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"stardust:restServiceOverlay::uri" : this.uriInput
											.val(),
									"stardust:restServiceOverlay::command" : this.commandSelect
											.val(),
									"stardust:restServiceOverlay::requestType" : this.requestTypeSelect
											.val(),
									"stardust:restServiceOverlay::responseType" : this.responseTypeSelect
											.val()
								}
							});
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								contexts : {
									application : {
										accessPoints : parameterDefinitionsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "restServiceOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
									"carnot:engine:camel::routeEntries" : this
											.getRoute(),
									"stardust:restServiceOverlay::uri" : this.uriInput
											.val(),
									"stardust:restServiceOverlay::command" : this.commandSelect
											.val(),
									"stardust:restServiceOverlay::requestType" : this.requestTypeSelect
											.val(),
									"stardust:restServiceOverlay::responseType" : this.responseTypeSelect
											.val()
								}
							});
				};

				/**
				 * 
				 */
				RestServiceOverlay.prototype.validate = function() {
					return true;
				};
			}
		});