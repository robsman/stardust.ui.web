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

					this.camelContextInput = jQuery("#genericEndpointOverlay #camelContextInput");
					this.routeTextarea = jQuery("#genericEndpointOverlay #routeTextarea");
					this.additionalBeanSpecificationTextarea = jQuery("#genericEndpointOverlay #additionalBeanSpecificationTextarea");
					this.requestDataInput = jQuery("#genericEndpointOverlay #requestDataInput");
					this.responseDataInput = jQuery("#genericEndpointOverlay #responseDataInput");
					this.directionInput = jQuery("#genericEndpointOverlay #directionInput");

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
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.activate = function() {
					this
							.submitChanges({
								attributes : {
									"carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpoint",
									"carnot:engine:camel::camelContextId" : "defaultCamelContext",
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
				};

				/**
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.update = function() {
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
								.push("Camel Context must not be empty."); // TODO I18N
						this.camelContextInput.addClass("error");

						return false;
					}

					return true;
				};
			}
		});