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
					var overlay = new GenericEndpointOverlay();

					overlay.initialize(view);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function GenericEndpointOverlay() {
				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.initialize = function(view) {
					this.view = view;

					this.view.insertPropertiesTab("genericEndpointOverlay",
							"parameters", "Parameters",
							"../../images/icons/table.png");

					this.camelContextInput = jQuery("#genericEndpointOverlay #camelContextInput");
					this.routeTextarea = jQuery("#genericEndpointOverlay #routeTextarea");
					this.additionalBeanSpecificationTextarea = jQuery("#genericEndpointOverlay #additionalBeanSpecificationTextarea");
					this.requestDataInput = jQuery("#genericEndpointOverlay #requestDataInput");
					this.responseDataInput = jQuery("#genericEndpointOverlay #responseDataInput");
					this.directionInput = jQuery("#genericEndpointOverlay #directionInput");
					this.inputBodyAccessPointInput = jQuery("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = jQuery("#parametersTab #outputBodyAccessPointInput");

					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "parametersTab",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true
							});

					this.directionInput
							.append("<option value=\"requestOnly\">"
									+ m_i18nUtils
											.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.direction.requestOnly")
									+ "</option>");
					this.directionInput
							.append("<option value=\"requestResponse\">"
									+ m_i18nUtils
											.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.direction.requestResponse")
									+ "</option>");

					var self = this;

					this.camelContextInput.change(function() {
						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::camelContextId",
								self.camelContextInput.val());
					});
					this.routeTextarea.change(function() {
						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::routeEntries",
								self.routeTextarea.val());
					});
					this.additionalBeanSpecificationTextarea
							.change(function() {
								if (!self.view.validate()) {
									return;
								}

								self.view
										.submitModelElementAttributeChange(
												"carnot:engine:camel::additionalSpringBeanDefinitions",
												self.additionalBeanSpecificationTextarea
														.val());
							});
					this.inputBodyAccessPointInput
							.change(function() {
								if (!self.view.validate()) {
									return;
								}

								if (self.inputBodyAccessPointInput.val() == m_constants.TO_BE_DEFINED) {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::inBodyAccessPoint",
													null);
								} else {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::inBodyAccessPoint",
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
													"carnot:engine:camel::outBodyAccessPoint",
													null);
								} else {
									self.view
											.submitModelElementAttributeChange(
													"carnot:engine:camel::outBodyAccessPoint",
													self.outputBodyAccessPointInput
															.val());
								}
							});
					this.directionInput
							.change(function() {
								if (self.directionInput.val() == "requestResponse") {
									self.view
											.submitChanges({
												attributes : {
													"carnot:engine:camel::producerMethodName" : "sendBodyInOut(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)"
												},
												contexts : {
													application : {
														accessPoints : [
																{
																	id : "oParam1",
																	name : "Input",
																	direction : "IN",
																	dataType : "primitive",
																	primitiveDataType : "string"
																},
																{
																	id : "returnValue",
																	name : "Output",
																	direction : "OUT",
																	dataType : "primitive",
																	primitiveDataType : "string",
																	attributes : {
																		"carnot:engine:flavor" : "RETURN_VALUE"
																	}
																} ]
													}
												}
											});
								} else {
									self.view
											.submitChanges({
												attributes : {
													"carnot:engine:camel::producerMethodName" : ""
												},
												contexts : {
													application : {
														accessPoints : [ {
															id : "oParam1",
															name : "Input",
															direction : "IN",
															dataType : "primitive",
															primitiveDataType : "string"
														} ]
													}
												}
											});
								}
							});
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getModelElement = function() {
					return this.view.getModelElement();
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getApplication = function() {
					return this.view.application;
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.getScopeModel = function() {
					return this.view.getModelElement().model;
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.activate = function() {
					this.view
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext"
								}
							});
				};


				/**
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.update = function() {
					this.parameterDefinitionsPanel.setScopeModel(this
							.getScopeModel());
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

					this.inputBodyAccessPointInput.empty();
					this.inputBodyAccessPointInput
							.append("<option value='" + m_constants.TO_BE_DEFINED + "'>"
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
					this.outputBodyAccessPointInput
							.append("<option value='" + m_constants.TO_BE_DEFINED + "' selected>"
									+ m_i18nUtils.getProperty("None") // TODO I18N
									+ "</option>");

					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n) {
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						this.outputBodyAccessPointInput.append("<option value='"
								+ accessPoint.id + "'>" + accessPoint.name
								+ "</option>");
					}

					this.inputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
					this.outputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);
					this.camelContextInput
							.val(this.getApplication().attributes["carnot:engine:camel::camelContextId"]);
					this.routeTextarea
							.val(this.getApplication().attributes["carnot:engine:camel::routeEntries"]);
					this.additionalBeanSpecificationTextarea
							.val(this.getApplication().attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);

					if (this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
							&& this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
									.indexOf("sendBodyInOut") == 0) {
						this.directionInput.val("requestResponse");
					} else {
						this.directionInput.val("requestOnly");
					}
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.validate = function() {
					this.camelContextInput.removeClass("error");

					if (this.camelContextInput.val() == null
							|| this.camelContextInput.val() == "") {
						this.view.errorMessages
								.push("Camel Context must not be empty."); // TODO
						// I18N
						this.camelContextInput.addClass("error");

						return false;
					}

					return true;
				};

				/**
				 * 
				 */
				GenericEndpointOverlay.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.view
							.submitChanges({
								contexts : {
									application : {
										accessPoints : parameterDefinitionsChanges
									}
								},
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay"
								}
							});
				};

			}
		});