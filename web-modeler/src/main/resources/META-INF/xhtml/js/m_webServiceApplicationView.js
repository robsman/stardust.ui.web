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
		[ "m_utils", "m_constants", "m_communicationController", "m_command",
				"m_commandsController", "m_dialog", "m_modelElementView",
				"m_model" ],
		function(m_utils, m_constants, m_communicationController, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					var view = new WebServiceApplicationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));
				}
			};

			/**
			 *
			 */
			function WebServiceApplicationView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(WebServiceApplicationView.prototype,
						view);

				/**
				 *
				 */
				WebServiceApplicationView.prototype.initialize = function(
						application) {
					this.initializeModelElementView();
					this.initializeModelElement(application);

					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(application);

					this.webServiceStructure = {};
					this.wsdlUrlInput = jQuery("#wsdlUrlInput");
					this.browseButton = jQuery("#browseButton");
					this.serviceSelect = jQuery("#serviceSelect");
					this.portSelect = jQuery("#portSelect");
					this.operationSelect = jQuery("#operationSelect");
					this.styleOutput = jQuery("#styleOutput");
					this.protocolOutput = jQuery("#protocolOutput");
					this.useOutput = jQuery("#useOutput");
					this.addressingInput = jQuery("#addressingInput");
					this.implementationSelect = jQuery("#implementationSelect");
					this.authenticationInput = jQuery("#authenticationInput");
					this.mechanismSelect = jQuery("#mechanismSelect");
					this.variantSelect = jQuery("#variantSelect");

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
										event.data.view
												.setService(event.data.view.serviceSelect
														.val());
									});
					this.portSelect.change({
						view : this
					}, function(event) {
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
										event.data.view.variantSelect.empty();

										if (event.data.view.mechanismSelect
												.val() == "basic") {
											event.data.view.variantSelect
													.append("<option value=\"passwordText\">User Name/Password</option>");
										} else {
											event.data.view.variantSelect
													.append("<option value=\"passwordText\">User Name/Password</option><option value=\"passwordDigest\">User Name/Password Digest</option><option value=\"xwssConfiguration\">XWSS Configuration</option>");
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
						ports[this.application.attributes["carnot:engine:wsPortName"]] = {
							name : this.application.attributes["carnot:engine:wsPortName"],
							operations : operations
						};
					}

					if (this.application.attributes["carnot:engine:wsServiceName"] != null) {
						services[this.application.attributes["carnot:engine:wsServiceName"]] = {
							name : this.application.attributes["carnot:engine:wsServiceName"],
							ports : ports
						};
					}

					structure = {
						services : services
					};

					this.loadWebServiceStructure(structure);

					// Populate inputs from application

					this.wsdlUrlInput
							.val(this.application.attributes["carnot:engine:wsdlUrl"]);
					this.serviceSelect
							.val(this.application.attributes["carnot:engine:wsServiceName"]);
					this.portSelect
							.val(this.application.attributes["carnot:engine:wsPortName"]);
					this.operationSelect
							.val(this.application.attributes["carnot:engine:wsOperationName"]);
					if (this.application.attributes["carnot:engine:wsImplementation"]) {
						this
								.setAddressing(
										true,
										this.application.attributes["carnot:engine:wsImplementation"]);
					} else {
						this.setAddressing(false);
					}
					if (this.application.attributes["carnot:engine:wsAuthentication"]) {
						this
								.setAuthentication(
										true,
										this.application.attributes["carnot:engine:wsAuthentication"],
										this.application.attributes["carnot:engine:wsAuthenticationVariant"]);
					} else {
						this.setAuthentication(false);
					}
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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
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
					var successCallback = {
						callbackScope : this,
						callbackMethod : "setWebServiceStructure"
					};

					jQuery("body").css("cursor", "progress");
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
										wsdlUrl : this.wsdlUrlInput.val()
									}),
									{
										"success" : function(serverData) {
											successCallback.callbackScope[successCallback.callbackMethod]
													(serverData);
											jQuery("body")
													.css("cursor", "auto");
										},
										"error" : function() {
											jQuery("body")
													.css("cursor", "auto");
											if (structure == null) {
												view.errorMessages
														.push("Could not load WSDL from URL.");
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
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setWebServiceStructure = function(
						webServiceStructure) {
					m_utils.debug("===> Web Service Structure");
					m_utils.debug(webServiceStructure);

					this.webServiceStructure = webServiceStructure;

					this.serviceSelect.empty();

					var start = true;

					for ( var m in webServiceStructure.services) {
						var service = webServiceStructure.services[m];

						this.serviceSelect.append("<option value=\""
								+ service.name + "\">" + service.name
								+ "</option>");

						if (start) {
							this.setWebService(service.name);

							start = false;
						}
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setWebService = function(
						service) {
					this.serviceSelect.val(service);

					this.portSelect.empty();

					var start = true;

					for ( var m in this.webServiceStructure.services[service].ports) {
						var port = this.webServiceStructure.services[service].ports[m];

						this.portSelect.append("<option value=\"" + port.name
								+ "\">" + port.name + "</option>");

						if (start) {
							this.setPort(port.name);

							start = false;
						}
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setPort = function(portName) {
					this.portSelect.val(portName);

					var port = this.webServiceStructure.services[this.serviceSelect
							.val()].ports[portName];

					this.styleOutput.empty();
					this.styleOutput.append(port.style);
					this.operationSelect.empty();

					var start = true;

					for ( var m in port.operations) {
						var operation = port.operations[m];

						this.operationSelect.append("<option value=\""
								+ operation.name + "\">" + operation.name
								+ "</option>");

						if (start) {
							this.setOperation(operation.name);

							start = false;
						}
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setOperation = function(
						operationName) {
					this.operationSelect.val(operationName);

					var operation = this.webServiceStructure.services[this.serviceSelect
							.val()].ports[this.portSelect.val()].operations[operationName];
					this.useOutput.empty();
					this.useOutput.append(operation.use);

					if (this.serviceSelect.val() != this.application.attributes["carnot:engine:wsServiceName"]
							&& this.portSelect.val() != this.application.attributes["carnot:engine:wsPortName"]
							&& this.operationSelect.val() != this.application.attributes["carnot:engine:wsOperationName"]) {
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
				 *
				 */
				WebServiceApplicationView.prototype.setAddressing = function(
						wsAdressing, implementation) {

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
						this.implementationSelect.val(null);
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setImplementation = function(
						implementation) {
					this.implementationSelect.val(implementation);

					if (this.implementationSelect.val() != this.application.attributes["carnot:engine:wsImplementation"]) {
						this
								.submitChanges({
									attributes : {
										"carnot:engine:wsImplementation" : this.implementationSelect
												.val()
									}
								});
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setAuthentication = function(
						authentication, mechanism, variant) {
					if (authentication) {
						this.authenticationInput.attr("checked", true);

						if (mechanism == null) {
							mechanism = "basic";
						}

						this.setMechanism(mechanism, variant);
					} else {
						this.authenticationInput.attr("checked", false);
						this.mechanismSelect.attr("disabled", true);
						this.mechanismSelect.val(null);
						this.variantSelect.attr("disabled", true);
						this.variantSelect.val(null);
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setMechanism = function(
						mechanism, variant) {
					this.mechanismSelect.removeAttr("disabled");
					this.mechanismSelect.val(mechanism);
					this.variantSelect.empty();

					if (variant == null) {
						variant = "passwordText";
					}

					if (mechanism == "basic") {
						this.variantSelect
								.append("<option value=\"passwordText\">User Name/Password</option>");
						this.setVariant(variant);
					} else {
						this.variantSelect
								.append("<option value=\"passwordText\">User Name/Password</option><option value=\"passwordDigest\">User Name/Password Digest</option><option value=\"xwssConfiguration\">XWSS Configuration</option>");
						this.setVariant(variant);
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.setVariant = function(
						variant) {
					this.variantSelect.removeAttr("disabled");
					this.variantSelect.val(variant);

					if (this.mechanismSelect.val() != this.application.attributes["carnot:engine:wsAuthentication"]
							&& this.variantSelect.val() != this.application.attributes["carnot:engine:wsAuthenticationVariant"]) {
						this
								.submitChanges({
									attributes : {
										"carnot:engine:wsAuthentication" : this.mechanismSelect
												.val(),
										"carnot:engine:wsAuthenticationVariant" : this.variantSelect
												.val()
									}
								});
					}
				};

				/**
				 *
				 */
				WebServiceApplicationView.prototype.processCommand = function(
						command) {
					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.application);

						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].uuid == this.application.uuid) {
						this.initialize(this.application);
					}
				};
			}
		});