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
							"plugins/bpm-modeler/images/icons/table.png");

					this.view.insertPropertiesTab("genericEndpointOverlay",
							"producerRoute", "Producer Route",
							"../../images/icons/table.png");

					this.view.insertPropertiesTab("genericEndpointOverlay",
							"consumerRoute", "Consumer Route",
							"../../images/icons/table.png");



					// configuration tab
					this.camelContextInput = jQuery("#genericEndpointOverlay #camelContextInput");
					this.additionalBeanSpecificationTextarea = jQuery("#genericEndpointOverlay #additionalBeanSpecificationTextarea");

					this.invocationPatternInput = jQuery("#genericEndpointOverlay #invocationPatternInput");
					this.invocationPatternInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils
						.getProperty("None") + "</option>");
					this.invocationPatternInput.append("<option value=\"send\">" + m_i18nUtils
						.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.send") + "</option>");
					this.invocationPatternInput.append("<option value=\"sendReceive\">" + m_i18nUtils
						.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.sendReceive") + "</option>");
					this.invocationPatternInput.append("<option value=\"receive\">" + m_i18nUtils
						.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationPattern.receive") + "</option>");


					this.invocationTypeInput = jQuery("#genericEndpointOverlay #invocationTypeInput");
					this.invocationTypeInput.append("<option value=\"" + m_constants.TO_BE_DEFINED + "\">" + m_i18nUtils
						.getProperty("None") + "</option>");
					this.invocationTypeInput.append("<option value=\"synchronous\">" + m_i18nUtils
						.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationType.synchronous") + "</option>");
					this.invocationTypeInput.append("<option value=\"asynchronous\">" + m_i18nUtils
						.getProperty("modeler.model.propertyView.camelRoute.camelConfigurationProperties.invocationType.asynchronous") + "</option>");

					// producer route tab
					this.processContextHeadersInput = jQuery("#producerRouteTab #processContextHeadersInput");
					this.producerRouteTextarea = jQuery("#producerRouteTab #producerRouteTextarea");

					// consumer route tab
					this.consumerRouteTextarea = jQuery("#consumerRouteTab #consumerRouteTextarea");

					// parameters tab
					this.requestDataInput = jQuery("#genericEndpointOverlay #requestDataInput");
					this.responseDataInput = jQuery("#genericEndpointOverlay #responseDataInput");
					this.inputBodyAccessPointInput = jQuery("#parametersTab #inputBodyAccessPointInput");
					this.outputBodyAccessPointInput = jQuery("#parametersTab #outputBodyAccessPointInput");

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

					var self = this;

					this.camelContextInput.change(function() {

						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::camelContextId",
									self.camelContextInput.val());
					});

					this.invocationPatternInput.change(function()
					{
						if (!self.view.validate()) {
							return;
						}

						self.manageInvocationSettings();
					});

					this.invocationTypeInput.change(function()
					{
						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::invocationType",
									self.invocationTypeInput.val());
					});

					this.producerRouteTextarea.change(function() {
						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::routeEntries",
								self.producerRouteTextarea.val());
					});

					this.processContextHeadersInput.change(function() {
						if (!self.view.validate()) {
							return;
						}
						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::processContextHeaders",
								self.processContextHeadersInput.prop("checked"));
					});

					this.consumerRouteTextarea.change(function() {
						if (!self.view.validate()) {
							return;
						}

						self.view.submitModelElementAttributeChange(
								"carnot:engine:camel::consumerRoute",
								self.consumerRouteTextarea.val());
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

					/**
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
							**/
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
									"carnot:engine:camel::applicationIntegrationOverlay" : "genericEndpointOverlay"//,
//									"carnot:engine:camel::camelContextId" : "defaultCamelContext"
								}
							});
				};

				/**
				 * Overlay protocol
				 */
				GenericEndpointOverlay.prototype.update = function() {

					this.parameterDefinitionsPanel.setScopeModel(this.getScopeModel());
					this.parameterDefinitionsPanel.setParameterDefinitions(this.getApplication().contexts.application.accessPoints);

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
							+ m_i18nUtils.getProperty("None") // TODO I18N
							+ "</option>");



					for ( var n = 0; n < this.getApplication().contexts.application.accessPoints.length; ++n)
					{
						var accessPoint = this.getApplication().contexts.application.accessPoints[n];

						if (accessPoint.direction != m_constants.OUT_ACCESS_POINT) {
							continue;
						}

						this.outputBodyAccessPointInput
								.append("<option value='" + accessPoint.id
										+ "'>" + accessPoint.name + "</option>");
					}

					this.inputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::inBodyAccessPoint"]);
					this.outputBodyAccessPointInput
							.val(this.getApplication().attributes["carnot:engine:camel::outBodyAccessPoint"]);

					// configuration tab
					this.camelContextInput
							.val(this.getApplication().attributes["carnot:engine:camel::camelContextId"]);

					this.additionalBeanSpecificationTextarea
						.val(this.getApplication().attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);

					this.invocationPatternInput
							.val(this.getApplication().attributes["carnot:engine:camel::invocationPattern"]);

					this.invocationTypeInput
							.val(this.getApplication().attributes["carnot:engine:camel::invocationType"]);

					this.manageInvocationSettings();

					// camel producer tab
					this.processContextHeadersInput.prop("checked",
							this.getApplication().attributes["carnot:engine:camel::processContextHeaders"]);
					this.producerRouteTextarea
						.val(this.getApplication().attributes["carnot:engine:camel::routeEntries"]);

					// camel consumer tab
					this.consumerRouteTextarea
						.val(this.getApplication().attributes["carnot:engine:camel::consumerRoute"]);

					// legacy
					if (this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
							&& this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
									.indexOf("sendBodyInOut") > -1) {

						this.sendReceiveSynchronous();
						this.view.submitModelElementAttributeChange(
								"carnot:engine:camel::producerMethodName", null);

					} if (this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
							&& this.getApplication().attributes["carnot:engine:camel::producerMethodName"]
								.indexOf("executeMessage") > -1) {

						this.sendSynchronous();
						this.view.submitModelElementAttributeChange(
								"carnot:engine:camel::producerMethodName", null);
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

				GenericEndpointOverlay.prototype.submitApplicationTypeChanges = function(
						applicationTypeChanges, invocationPatternChanges,
						invocationTypeChanges) {
					this.view
							.submitChanges({
								type : applicationTypeChanges,
								attributes : {
									"carnot:engine:camel::invocationPattern" : invocationPatternChanges,
									"carnot:engine:camel::invocationType" : invocationTypeChanges
								}
							});
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

				GenericEndpointOverlay.prototype.resetInvocationSettings = function()
				{

					this.view.invocationTypeInput = m_constants.TO_BE_DEFINED;
					this.submitApplicationTypeChanges(
						"camelSpringProducerApplication",
						null,
						null);
					this.invocationTypeInput.prop('disabled', true);
					this.producerRouteTextarea.prop('disabled', true);
					this.consumerRouteTextarea.prop('disabled', true);
				};

				GenericEndpointOverlay.prototype.sendSynchronous = function()
				{
					this.view.invocationTypeInput = "synchronous";
					this.submitApplicationTypeChanges(
						"camelSpringProducerApplication",
						"send",
						"synchronous");
					this.invocationTypeInput.prop('disabled', true);
					this.producerRouteTextarea.prop('disabled', false);
					this.consumerRouteTextarea.prop('disabled', true);
				};

				GenericEndpointOverlay.prototype.sendReceiveSynchronous = function()
				{
					this.view.invocationTypeInput = "synchronous";
					this.submitApplicationTypeChanges(
						"camelSpringProducerApplication",
						"sendReceive",
						"synchronous");
					this.invocationTypeInput.prop('disabled', false);
					this.producerRouteTextarea.prop('disabled', false);
					this.consumerRouteTextarea.prop('disabled', true);
				};

				GenericEndpointOverlay.prototype.sendReceiveAsynchronous = function()
				{
					this.view.invocationTypeInput = "asynchronous";
					this.submitApplicationTypeChanges(
						"camelConsumerApplication",
						"sendReceive",
						"asynchronous");
					this.invocationTypeInput.prop('disabled', false);
					this.producerRouteTextarea.prop('disabled', false);
					this.consumerRouteTextarea.prop('disabled', false);
				};

				GenericEndpointOverlay.prototype.receiveAsynchronous = function()
				{
					this.view.invocationTypeInput = "asynchronous";
					this.submitApplicationTypeChanges(
						"camelConsumerApplication",
						"receive",
						"asynchronous");
					this.invocationTypeInput.prop('disabled', true);
					this.producerRouteTextarea.prop('disabled', true);
					this.consumerRouteTextarea.prop('disabled', false);
				};

				GenericEndpointOverlay.prototype.manageInvocationSettings = function()
				{
					if (this.invocationPatternInput.val() == m_constants.TO_BE_DEFINED)
					{
						this.resetInvocationSettings();
					}
					else if (this.invocationPatternInput.val() == 'send')
					{
						this.sendSynchronous();
					}
					else if (this.invocationPatternInput.val() == 'sendReceive')
					{
						if (this.invocationTypeInput.val() == 'asynchronous')
						{
							this.sendReceiveAsynchronous();
						}
						else if (this.invocationTypeInput.val() == 'synchronous')
						{
							this.sendReceiveSynchronous();
						}
						else
						{
							this.sendReceiveSynchronous();
						}
					}
					else if (this.invocationPatternInput.val() == 'receive')
					{
						this.receiveAsynchronous();
					}
				};

				GenericEndpointOverlay.prototype.disableProducerTab = function() {
					this.producerRouteTextarea.prop('disabled', true);
				};

				GenericEndpointOverlay.prototype.enableProducerTab = function() {
					this.producerRouteTextarea.prop('disabled', false);
				};

				GenericEndpointOverlay.prototype.disableConsumerTab = function() {
					this.consumerRouteTextarea.prop('disabled', true);
				};

				GenericEndpointOverlay.prototype.enableConsumerTab = function() {
					this.consumerRouteTextarea.prop('disabled', false);
				};

			}
		});