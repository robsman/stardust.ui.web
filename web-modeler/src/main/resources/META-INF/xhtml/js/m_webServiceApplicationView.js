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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_urlUtils" , "bpm-modeler/js/m_constants", "bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model" ,"bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_urlUtils, m_constants, m_communicationController,
				m_command, m_commandsController, m_dialog, m_modelElementView,
				m_model, m_i18nUtils) {

			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					m_utils.jQuerySelect("#hideGeneralProperties").hide();
					initViewCollapseClickHandlers();

					var view = new WebServiceApplicationView();
					// TODO Unregister!
					// In Initializer?
					i18webserviceproperties();
					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
					m_utils.hideWaitCursor();

				}
			};

			/**
			 *
			 */
			function initViewCollapseClickHandlers() {
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").hide();
					m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").show();
					m_utils.jQuerySelect("#hideGeneralProperties").hide();
				});
			}

			function i18webserviceproperties() {
				m_utils.jQuerySelect("#hideGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));

				m_utils.jQuerySelect("#showGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));


				m_utils.jQuerySelect("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("#application")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				m_utils.jQuerySelect("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));
				m_utils.jQuerySelect("#wsdlurl")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.wsdlURL"));

				m_utils.jQuerySelect("#browseButton")
						.val(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.load"));
				m_utils.jQuerySelect("#service")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.service"));

				m_utils.jQuerySelect("#port")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.port"));
				m_utils.jQuerySelect("#operation")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.operation"));
				m_utils.jQuerySelect("#style")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.style"));
				m_utils.jQuerySelect("#protocal")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.protocol"));
				m_utils.jQuerySelect("#use")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.use"));
				m_utils.jQuerySelect("#endpointLabel")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.endpoint"));

				m_utils.jQuerySelect("#implementation")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.implementation"));
				m_utils.jQuerySelect("#includedAddressing")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.implementationProperties.includeAddressing"));
				m_utils.jQuerySelect("#implementation1")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.implementation"));

				m_utils.jQuerySelect("#security")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.security"));
				m_utils.jQuerySelect("#authenticationReq")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.securityProperties.authenticationRequired"));
				m_utils.jQuerySelect("#mechanism")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.securityProperties.mechanism"));
				m_utils.jQuerySelect("#variant")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.webService.securityProperties.variant"));

				/* Comments Tab Changes */
				m_utils.jQuerySelect("#comments")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.comments"));
				m_utils.jQuerySelect("#comments")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.webservice.commentsProperties.comments"));
				m_utils.jQuerySelect("#submitButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.submit"));
				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));

			}

			/**
			 *
			 */
			function WebServiceApplicationView() {
				var view = m_modelElementView.create();
				var initializing;

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(WebServiceApplicationView.prototype,
						view);

				/**
				 *
				 */
				WebServiceApplicationView.prototype.initialize = function(
						application) {
					initializing = true;
					this.id = "webServiceApplicationView";
					this.view = m_utils.jQuerySelect("#" + this.id);

					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.wsdlUrlInput = m_utils.jQuerySelect("#wsdlUrlInput");
					this.browseButton = m_utils.jQuerySelect("#browseButton");
					this.serviceSelect = m_utils.jQuerySelect("#serviceSelect");
					this.implementselect = m_utils.jQuerySelect("#implementationSelect");
					this.portSelect = m_utils.jQuerySelect("#portSelect");
					this.operationSelect = m_utils.jQuerySelect("#operationSelect");
					this.styleOutput = m_utils.jQuerySelect("#styleOutput");
					this.protocolOutput = m_utils.jQuerySelect("#protocolOutput");
					this.useOutput = m_utils.jQuerySelect("#useOutput");
					this.endpointLabel = m_utils.jQuerySelect("#endpointLabel");
					this.endpoint = m_utils.jQuerySelect("#endpointInput");
					this.addressingInput = m_utils.jQuerySelect("#addressingInput");
					this.implementationSelect = m_utils.jQuerySelect("#implementationSelect");
					this.authenticationInput = m_utils.jQuerySelect("#authenticationInput");
					this.mechanismSelect = m_utils.jQuerySelect("#mechanismSelect");
					this.variantSelect = m_utils.jQuerySelect("#variantSelect");

					// values for implentationselect
					var selectdata = null;
					selectdata = m_i18nUtils
							.getProperty("modeler.model.propertyView.webService.implementationselect.option.genericRescource");
					this.implementselect.append("<option value=\"generic\">"
							+ selectdata + "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.model.propertyView.webService.implementationSelect.option.infinitySpecific");
					this.implementselect.append("<option value=\"carnot\">"
							+ selectdata + "</option>");

					// values for mechanism select
					selectdata = m_i18nUtils
							.getProperty("modeler.model.propertyView.webService.mechanismSelect.option.httpBasicAuthorization");
					this.mechanismSelect.append("<option value=\"basic\">"
							+ selectdata + "</option>");
					selectdata = m_i18nUtils
							.getProperty("modeler.model.propertyView.webService.mechanismSelect.option.wsSecurity");
					this.mechanismSelect
							.append("<option value=\"ws-security\">"
									+ selectdata + "</option>");

					this.browseButton.click({
						view : this
					}, function(event) {
						event.data.view.loadWebServiceStructure()
					});
					this.serviceSelect
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view.application.attributes["carnot:engine:wsPortName"] = null;
										event.data.view.application.attributes["carnot:engine:wsOperationName"] = null;
										event.data.view
												.setWebService(event.data.view.serviceSelect
														.val());
									});
					this.portSelect.change({
						view : this
					}, function(event) {
						event.data.view.application.attributes["carnot:engine:wsOperationName"] = null;
						event.data.view.setPort(event.data.view.portSelect
								.val());
					});
					this.operationSelect.change({
						view : this
					}, function(event) {
						event.data.view
								.setOperation(event.data.view.operationSelect
										.val());
					});
					this.addressingInput.change({
						view : this
					}, function(event) {
						event.data.view
								.setAddressing(event.data.view.addressingInput
										.is(":checked"));
					});

					this.endpoint.change({
						view : this
					}, function(event) {
						var endpointVal = null;
						if (m_utils.isEmptyString(event.data.view.endpoint.val())) {
							endpointVal = event.data.view.endpoint.val();
						}
						event.data.view.submitChanges({
							attributes : {
								"carnot:engine:wsUddiAccessPoint" : endpointVal
							}
						});
					});

                    this.wsdlUrlInput.change({
                        view : this
                  }, function(event) {
                        event.data.view.submitChanges({
							attributes : {
								"carnot:engine:wsdlUrl" : event.data.view.wsdlUrlInput.val()
							}
                        });
                  });


					this.implementationSelect
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view
												.setImplementation(event.data.view.implementationSelect
														.val());
									});
					this.authenticationInput
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view
												.setAuthentication(event.data.view.authenticationInput
														.is(":checked"));
									});
					this.mechanismSelect
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view.setMechanism(event.data.view.mechanismSelect.val());
									});
					this.publicVisibilityCheckbox
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:engine:visibility"]
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});
					this.variantSelect
							.change(
									{
										view : this
									},
									function(event) {
										event.data.view
												.setVariant(event.data.view.variantSelect
														.val());
									});

					this.initializeModelElementView(application);
					this.view.css("visibility", "visible");
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setModelElement = function(
						application) {
					this.initializeModelElement(application);

					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(application);

					if (!this.application.attributes["carnot:engine:visibility"]
							|| "Public" == this.application.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if (initializing) {
						if (this.application.webServiceStructure) {
							this.updateServiceFromAppWebServiceStructure();
						} else {
							// Build dummy Web Service structure - allows too initialize
							// selects even without full WSDL information
							var structure = {};
							var services = {};
							var ports = {};
							var operations = {};

							if (this.application.attributes["carnot:engine:wsOperationName"] != null) {
								operations[this.application.attributes["carnot:engine:wsOperationName"]] = {
									name : this.application.attributes["carnot:engine:wsOperationName"]
								};
							}

							if (this.application.attributes["carnot:engine:wsPortName"] != null) {
								ports[this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"])] = {
									name : this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"]),
									operations : operations
								};
							}

							if (this.application.attributes["carnot:engine:wsServiceName"] != null) {
								services[this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"])] = {
									name : this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"]),
									ports : ports
								};
							}

							structure = {
								services : services,
								url : this.application.attributes["carnot:engine:wsdlUrl"]
							};

							this.loadWebServiceStructure(structure);
						}
					}

					// Populate inputs from application

					this.wsdlUrlInput
							.val(this.application.attributes["carnot:engine:wsdlUrl"]);
					this.serviceSelect
							.val(this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"]));
					this.portSelect
							.val(this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"]));
					this.operationSelect
							.val(this.application.attributes["carnot:engine:wsOperationName"]);

					if (this.serviceSelect.val()
							&& this.serviceSelect.val().indexOf(
									m_constants.DYNAMICALLY_BOUND_SERVICE) > -1) {
						this.endpointLabel.removeClass("invisible");
						this.endpoint.removeClass("invisible");
					} else {
						this.endpointLabel.addClass("invisible");
						this.endpoint.addClass("invisible");
					}
					if (this.application.attributes["carnot:engine:wsUddiAccessPoint"]) {
						this.endpoint
								.val(this.application.attributes["carnot:engine:wsUddiAccessPoint"]);
					} else {
						this.endpoint.val("");
					}

					if (this.application.attributes["carnot:engine:wsServiceName"]
							&& this.application.attributes["carnot:engine:wsPortName"]
							&& this.application.attributes["carnot:engine:wsOperationName"]) {
						// Update style output for selected service and port
						this.styleOutput
								.append(this.application.attributes["carnot:engine:wsPortName"]);
						var port = this.application.webServiceStructure
											.services[this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"])]
												.ports[this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"])];
						this.styleOutput.empty();
						this.styleOutput.append(port.style);

						// Update use output for selected service and port and operation
						var operation = this.application.webServiceStructure
												.services[this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"])]
													.ports[this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"])]
														.operations[this.application.attributes["carnot:engine:wsOperationName"]];
						this.useOutput.empty();
						this.useOutput.append(operation.use);

						// Update protocol output for selected service and port and operation
						this.protocolOutput.empty();
						this.protocolOutput
								.append(operation["carnot:engine:wsSoapProtocol"]);
					}

					if (this.application.attributes["carnot:engine:wsImplementation"]) {
						this.initializeWSAddressing(true, this.application.attributes["carnot:engine:wsImplementation"]);
					} else {
						this.initializeWSAddressing(false, this.application.attributes["carnot:engine:wsImplementation"]);
					}

					if (this.application.attributes["carnot:engine:wsAuthentication"]) {
						this.authenticationInput.attr("checked", true);
						this.initializeAuthentication(this.application.attributes["carnot:engine:wsAuthentication"],
										this.application.attributes["carnot:engine:wsAuthenticationVariant"]);
					} else {
						this.authenticationInput.attr("checked", false);
						this.mechanismSelect.attr("disabled", true);
						this.variantSelect.attr("disabled", true);
					}
					initializing = false;
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.getDisplayName = function(fullyQualifiedName) {
					if (fullyQualifiedName
							&& fullyQualifiedName.indexOf("{") == 0
							&& fullyQualifiedName.indexOf("}") > -1
							&& fullyQualifiedName.indexOf("}") < (fullyQualifiedName.length - 1)) {
						return fullyQualifiedName.substring((fullyQualifiedName.indexOf("}") + 1));
					};

					return fullyQualifiedName;
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.toString = function() {
					return "Lightdust.WebServiceApplicationView";
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages
								.push("Application name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * <code>structure</code> allows to pass a structure if no
				 * structure cannot be retrieved from the server.
				 */
				WebServiceApplicationView.prototype.loadWebServiceStructure = function(
						structure) {
					var wsdlURL = (structure && structure.url) ? structure.url : this.wsdlUrlInput.val();

					if (wsdlURL.trim() != "") {
						var successCallback = {
								callbackScope : this,
								callbackMethod : "setWebServiceStructure"
							};

							m_utils.jQuerySelect("body").css("cursor", "progress");
							this.clearErrorMessages();
							this.wsdlUrlInput.removeClass("error");

							var view = this;

							m_communicationController
									.syncPostData(
											{
												url : m_communicationController
														.getEndpointUrl()
														+ "/webServices/structure"
											},
											JSON.stringify({
												wsdlUrl : wsdlURL,
												modelID : view.application.model.id
											}),
											{
												"success" : function(serverData) {
													successCallback.callbackScope[successCallback.callbackMethod]
															(serverData);
													m_utils.jQuerySelect("body")
															.css("cursor", "auto");
												},
												"error" : function() {
													m_utils.jQuerySelect("body")
															.css("cursor", "auto");
													if (structure == null) {
														var errormessage = m_i18nUtils.getProperty("modeler.model.propertyView.webService.errorMessage")
														view.errorMessages
																.push(errormessage);
														view.showErrorMessages();
														view.wsdlUrlInput
																.addClass("error");
														view.serviceSelect.empty();
														view.portSelect.empty();
														view.operationSelect.empty();
													} else {
														successCallback.callbackScope[successCallback.callbackMethod]
																(structure);
													}
												}
											});
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setWebServiceStructure = function(
						webServiceStructure) {

					m_utils.debug("===> Web Service Structure");
					m_utils.debug(webServiceStructure);

					this.setApplicationWebServiceStructure(webServiceStructure);

					this.updateServiceFromAppWebServiceStructure();
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setApplicationWebServiceStructure = function(
						webServiceStructure) {
					this.application.webServiceStructure = webServiceStructure;
				}

				/**
				 *
				 */
				WebServiceApplicationView.prototype.updateServiceFromAppWebServiceStructure = function() {
					this.serviceSelect.empty();

					var serviceSet = false;

					for ( var m in this.application.webServiceStructure.services) {
						var service = this.application.webServiceStructure.services[m];

						this.serviceSelect.append("<option value=\""
								+ service.name + "\">" + service.name
								+ "</option>");

						if (!serviceSet
								&& !this.application.attributes["carnot:engine:wsServiceName"]) {
							this.setWebService(service.name);

							serviceSet = true;
						}
					}

					if (!serviceSet) {
						this.setWebService(this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"]));
					}
				}

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setWebService = function(
						service) {
					this.serviceSelect.val(service);

					this.portSelect.empty();

					var portSet = false;

					for ( var m in this.application.webServiceStructure.services[service].ports) {
						var port = this.application.webServiceStructure.services[service].ports[m];

						this.portSelect.append("<option value=\"" + port.name
								+ "\">" + port.name + "</option>");

						if (!portSet
								&& !this.application.attributes["carnot:engine:wsPortName"]) {
							this.setPort(port.name);

							portSet = true;
						}
					}

					if (!portSet) {
						this.setPort(this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"]));
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setPort = function(portName) {
					this.portSelect.val(portName);

					var port = this.application.webServiceStructure.services[this.serviceSelect
							.val()].ports[portName];

					this.styleOutput.empty();
					this.styleOutput.append(port.style);
					this.operationSelect.empty();

					var operationSet = false;

					for ( var m in port.operations) {
						var operation = port.operations[m];

						this.operationSelect.append("<option value=\""
								+ operation.name + "\">" + operation.name
								+ "</option>");

						if (!operationSet
								&& !this.application.attributes["carnot:engine:wsOperationName"]) {
							this.setOperation(operation.name);

							operationSet = true;
						}
					}

					if (!operationSet) {
						this.setOperation(this.application.attributes["carnot:engine:wsOperationName"]);
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setOperation = function(
						operationName) {
					this.operationSelect.val(operationName);

					var operation = this.application.webServiceStructure.services[this.serviceSelect
							.val()].ports[this.portSelect.val()].operations[operationName];
					this.useOutput.empty();
					this.useOutput.append(operation.use);
					this.protocolOutput.empty();
					this.protocolOutput.append(operation["carnot:engine:wsSoapProtocol"]);

					if (!initializing
							&& (this.serviceSelect.val() != this.getDisplayName(this.application.attributes["carnot:engine:wsServiceName"])
								|| this.portSelect.val() != this.getDisplayName(this.application.attributes["carnot:engine:wsPortName"])
								|| this.operationSelect.val() != this.application.attributes["carnot:engine:wsOperationName"])) {
						this
								.submitChanges({
									attributes : {
										"carnot:engine:wsdlUrl" : this.wsdlUrlInput
												.val(),
										"carnot:engine:wsServiceName" : this.serviceSelect
												.val(),
										"carnot:engine:wsPortName" : this.portSelect
												.val(),
										"carnot:engine:wsOperationName" : this.operationSelect
												.val()
									}
								});
					}
				};

				/**
				 *  Initialize WS-addressing
				 */
				WebServiceApplicationView.prototype.initializeWSAddressing = function(wsAdressing,
						implementation) {
					if (wsAdressing) {
						this.addressingInput.attr("checked", true);
						this.implementationSelect.removeAttr("disabled");
						if (implementation == null) {
							implementation = "generic";
						}
						this.implementationSelect.val(implementation);
					} else {
						this.addressingInput.attr("checked", false);
						this.implementationSelect.attr("disabled", true);
					}
				};

				/**
				 * Set WS-addressing
				 */
				WebServiceApplicationView.prototype.setAddressing = function(wsAdressing) {
					if (wsAdressing) {
						this.initializeWSAddressing(wsAdressing, null);
						this.updateWSAddressing(this.implementationSelect.val());
					} else {
						this.initializeWSAddressing(wsAdressing, null);
						this.updateWSAddressing(null);
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setImplementation = function(
						implementation) {
					this.implementationSelect.val(implementation);
					this.updateWSAddressing(implementation);
				};

				/**
				 * update WS-Addressing
				 */
				WebServiceApplicationView.prototype.updateWSAddressing = function(
						implementation) {
					this.submitChanges({attributes :
								{"carnot:engine:wsImplementation" : implementation
							}
					});
				};

				/**
				 * initialize Authentication
				 */
				WebServiceApplicationView.prototype.initializeAuthentication = function(
						mechanism, variant) {

					this.mechanismSelect.removeAttr("disabled");
					this.variantSelect.removeAttr("disabled");

					if (mechanism == null) {
						mechanism = "basic";
					}
					this.mechanismSelect.val(mechanism);
					this.variantSelect.empty();

					if (mechanism == "basic") {
						selectdata = m_i18nUtils
								.getProperty("modeler.model.propertyView.webService.variant.option.userNamePwd");
						this.variantSelect
								.append("<option value=\"passwordText\">"
										+ selectdata + "</option>");

					} else {
						selectdata = m_i18nUtils
								.getProperty("modeler.model.propertyView.webService.variant.option.userNamePwd");
						this.variantSelect
								.append("<option value=\"passwordText\">"
										+ selectdata + "</option>");
						selectdata = m_i18nUtils
								.getProperty("modeler.model.propertyView.webService.variant.option.userNamePwdDigest");
						this.variantSelect
								.append("<option value=\"passwordDigest\">"
										+ selectdata + "</option>");
					}
					if (variant) {
						this.variantSelect.val(variant);
					}
				};

				/**
				 *	set/reset Authentication checkbox and update server
				 */
				WebServiceApplicationView.prototype.setAuthentication = function(authentication) {
					if (authentication) {
						this.authenticationInput.attr("checked", true);
						this.initializeAuthentication(null, null);
						this.updateAuthentication(this.mechanismSelect.val(), this.variantSelect.val());
					} else {
						this.authenticationInput.attr("checked", false);
						this.mechanismSelect.attr("disabled", true);
						this.variantSelect.attr("disabled", true);
						this.updateAuthentication(null, null);
					}
				};

				/**
				 *	sets Mechanism and reset Variant and then update server
				 */
				WebServiceApplicationView.prototype.setMechanism = function(mechanism) {
					this.initializeAuthentication(mechanism, null);
					this.updateAuthentication(this.mechanismSelect.val(), this.variantSelect.val());
				};

				/**
				 *	set Variant and update server
				 */
				WebServiceApplicationView.prototype.setVariant = function(variant) {
					this.variantSelect.val(variant);
					this.updateAuthentication(this.mechanismSelect.val(), this.variantSelect.val());
				};

				/**
				 *	Update server
				 */
				WebServiceApplicationView.prototype.updateAuthentication = function(
						mechanism, variant) {
					this.submitChanges({
						attributes : {
							"carnot:engine:wsAuthentication" : mechanism,
							"carnot:engine:wsAuthenticationVariant" : variant
						}
					});
				};
			}
		});