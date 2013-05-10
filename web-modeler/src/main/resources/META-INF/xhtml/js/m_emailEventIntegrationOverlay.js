/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_accessPoint",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils) {

			return {
				create : function(page, id) {
					var overlay = new EmailEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function EmailEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(EmailEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("label[for='protocolSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.protocol"));
					jQuery("label[for='mailServerInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.mailServer"));
					jQuery("label[for='portInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.port"));
					jQuery("label[for='accountInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.account"));
					jQuery("label[for='passwordInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.password"));
					jQuery("label[for='connectionTimeoutInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.connectionTimeout"));
					jQuery("label[for='initialDelayInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.initialDelay"));
				/*	jQuery("label[for='pollingDelayInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.pollingDelay")); */
					jQuery("label[for='unseenInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.unseen"));
					jQuery("label[for='deleteInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.delete"));
					/*	jQuery("label[for='copyToInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.emailEvent.copyTo")); */

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.protocolSelect = this.mapInputId("protocolSelect");
					this.mailServerInput = this.mapInputId("mailServerInput");
					this.portInput = this.mapInputId("portInput");
					this.accountInput = this.mapInputId("accountInput");
					this.passwordInput = this.mapInputId("passwordInput");
					this.connectionTimeoutInput = this
							.mapInputId("connectionTimeoutInput");
					this.connectionTimeoutUnitSelect = this
							.mapInputId("connectionTimeoutUnitSelect");
					this.initialDelayInput = this
							.mapInputId("initialDelayInput");
					this.initialDelayUnitSelect = this
							.mapInputId("initialDelayUnitSelect");
				/*	this.pollingDelayInput = this
							.mapInputId("pollingDelayInput");
					this.pollingDelayUnitSelect = this
							.mapInputId("pollingDelayUnitSelect"); */
					this.unseenInput = this.mapInputId("unseenInput");
					this.deleteInput = this.mapInputId("deleteInput");
				/*	this.copyToInput = this.mapInputId("copyToInput");
					this.copyToFolderInput = this
							.mapInputId("copyToFolderInput"); */

					this
							.initializeIntervalUnitSelect(this.connectionTimeoutUnitSelect);
					this
							.initializeIntervalUnitSelect(this.initialDelayUnitSelect);
				/*	this
							.initializeIntervalUnitSelect(this.pollingDelayUnitSelect);*/

					this.registerForRouteChanges(this.protocolSelect);
					this.registerForRouteChanges(this.mailServerInput);
					this.registerForRouteChanges(this.portInput);
					this.registerForRouteChanges(this.accountInput);
					this.registerForRouteChanges(this.passwordInput);
					this.registerForRouteChanges(this.connectionTimeoutInput);
					this
							.registerForRouteChanges(this.connectionTimeoutUnitSelect);
					this.registerForRouteChanges(this.initialDelayInput);
					this.registerForRouteChanges(this.initialDelayUnitSelect);
				/*	this.registerForRouteChanges(this.pollingDelayInput);
					this.registerForRouteChanges(this.pollingDelayUnitSelect); */
					this.registerForRouteChanges(this.unseenInput);
					this.registerForRouteChanges(this.deleteInput);
				/*	this.registerForRouteChanges(this.copyToInput);
					this.registerForRouteChanges(this.copyToFolderInput); */

				};

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "";

					uri += this.protocolSelect.val();
					uri += "://";

					uri += this.mailServerInput.val();

					if (this.portInput.val() != null) {
						uri += ":" + this.portInput.val();
					}

					if(this.accountInput.val()!= null){
					uri += "?username=" + this.accountInput.val();
					}

					if (this.passwordInput.val() != null) {
						uri += "&amp;password=" + this.passwordInput.val();
					}

					
					if(this.getIntervalInMilliseconds(
							this.connectionTimeoutInput.val(),
							this.connectionTimeoutUnitSelect.val()) != null){						
					uri += "&amp;connectionTimeout=";
					uri += this.getIntervalInMilliseconds(
							this.connectionTimeoutInput.val(),
							this.connectionTimeoutUnitSelect.val());
						}
					
					if(this.getIntervalInMilliseconds(
							this.initialDelayInput.val(),
							this.initialDelayUnitSelect.val()) != null){
					uri += "&amp;initialDelay=";
					uri += this.getIntervalInMilliseconds(
							this.initialDelayInput.val(),
							this.initialDelayUnitSelect.val());
					}
					
					/*
					uri += "&pollingDelay=";
					uri += this.getIntervalInMilliseconds(
							this.pollingDelayInput.val(),
							this.pollingDelayUnitSelect.val());
							*/
										
					uri += "&amp;unseen=";
					uri += this.unseenInput.prop("checked");
					uri += "&amp;delete=";
					uri += this.deleteInput.prop("checked");

				/*	if (this.copyToInput.prop("checked")) {
						uri += "&copyTo=";
						uri += this.copyToFolderInput.val();
					} */
					//uri=uri.replace(/&/g, "&amp;");
					return uri;
				};
				
				
				EmailEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};
				EmailEventIntegrationOverlay.prototype.getRouteDefinitions= function() {
					return "<from uri=\""+this.getEndpointUri()+"\"/>"+this.getAdditionalRouteDefinitions();
				}
				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.activate = function() {
					this.mailServerInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));
					this.accountInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));
					this.portInput.val("30");

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));
					/*
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Mail Body", "mailBody",
					 * "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Mail Attachments",
					 * "mailAttachments", "String"));
					 */

					this.submitOverlayChanges(parameterMappings);
				};

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.update = function() {
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					// TODO Need better URL encoding
					
				//	route = route.replace(/&/g, "&amp;");

					var xmlDoc = jQuery.parseXML("<route>"+route+"</route>");
					var xmlObject = jQuery(xmlDoc);
					var from = jQuery(xmlObject).find("from");
					var uri = from.attr("uri");
					var protocolAndRest = uri.split("://");

					this.protocolSelect.val(protocolAndRest[0]);

					var parametersAndOptions = protocolAndRest[1].split("?");
					var parameters = parametersAndOptions[0];
					var options = parametersAndOptions[1];

					var userAndRest = parameters.split("@");
					var hostAndPort = null;
					var user = null;
					var host = null;
					var port = null;

					if (userAndRest[1]) {
						user = userAndRest[0];
						hostAndPort = userAndRest[1].split(":");

						if (hostAndPort[1]) {
							port = hostAndPort[1];
							host = hostAndPort[0];
						} else {
							host = hostAndPort[0];
						}
					} else {
						hostAndPort = userAndRest[0].split(":");

						if (hostAndPort[1]) {
							port = hostAndPort[1];
							host = hostAndPort[0];
						} else {
							host = hostAndPort[0];
						}
					}

					this.mailServerInput.val(host);
					this.portInput.val(port);
					this.accountInput.val(user);

					// Map options

					var nameValues = options.split("&");

					for ( var n = 0; n < nameValues.length; ++n) {
						var nameValue = nameValues[n].split("=");
						var name = nameValue[0];
						var value = nameValue[1];

						m_utils.debug("name: " + name);
						m_utils.debug("value: " + value);

						if (name == "password") {
							this.passwordInput.val(value);
						} else if (name == "username") {
							this.accountInput.val(value);
						} else if (name == "to") {
						} else if (name == "replyTo") {
						} else if (name == "CC") {
						} else if (name == "BCC") {
						} else if (name == "from") {
						} else if (name == "subject") {
						} else if (name == "connectionTimeout") {
							var intervalWithUnit = this
									.getIntervalWithUnit(value);

							this.connectionTimeoutInput
									.val(intervalWithUnit.value);
							this.connectionTimeoutUnitSelect
									.val(intervalWithUnit.unit);
						} else if (name == "initialDelay") {
							var intervalWithUnit = this
									.getIntervalWithUnit(value);

							this.initialDelayInput.val(intervalWithUnit.value);
							this.initialDelayUnitSelect
									.val(intervalWithUnit.unit);
						} 
						
					/*	else if (name == "pollingDelay") {
							var intervalWithUnit = this
									.getIntervalWithUnit(value);

							this.pollingDelayInput.val(intervalWithUnit.value);
							this.pollingDelayUnitSelect
									.val(intervalWithUnit.unit);
						} */
						
						else if (name == "unseen") {
							this.unseenInput.prop("checked", value == "true");
						} else if (name == "delete") {
							this.deleteInput.prop("checked", value == "true");
						}/* else if (name == "copyTo") {
							this.copyToInput.prop("checked", true);
							this.copyToFolderInput.val(value);
						} else if (name == "fetchSize") {
						}*/
					}
					this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 * 
				 */
				EmailEventIntegrationOverlay.prototype.validate = function() {
					this.mailServerInput.removeClass("error");
					this.accountInput.removeClass("error");
					this.portInput.removeClass("error");
					this.connectionTimeoutInput.removeClass("error");
					this.initialDelayInput.removeClass("error");
					this.passwordInput.removeClass("error");
					this.page.propertiesPanel.errorMessages=[];
																

					if (this.mailServerInput.val() == null
							|| this.mailServerInput.val() == "") {
						this.page.propertiesPanel.errorMessages
								.push("Mail server name must not be empty.");
						this.mailServerInput.addClass("error");

										
					}

					if (this.accountInput.val() == null
							|| this.accountInput.val() == "") {
						this.page.propertiesPanel.errorMessages
								.push("Mail account must not be empty.");
						this.accountInput.addClass("error");

					
					}
					

					if (this.passwordInput.val() == null
							|| this.passwordInput.val() == ""){
						this.page.propertiesPanel.errorMessages
								.push("Password must not be empty.");
						this.passwordInput.addClass("error");

					
					}
					
					if (this.portInput.val() == null
							|| this.portInput.val() == "" || isNaN(this.portInput.val())) {
						this.page.propertiesPanel.errorMessages
								.push("Port must be a Number.");
						this.portInput.addClass("error");

					
					}
					
					if (this.connectionTimeoutInput.val() == null
							|| this.connectionTimeoutInput.val() == "" || isNaN(this.connectionTimeoutInput.val())) {
						this.page.propertiesPanel.errorMessages
								.push("ConnectionTimeout must be a Number.");
						this.connectionTimeoutInput.addClass("error");

					
					}
					
					if (this.initialDelayInput.val() == null
							|| this.initialDelayInput.val() == "" || isNaN(this.initialDelayInput.val())) {
						this.page.propertiesPanel.errorMessages
								.push("InitialDelay must be a Number.");
						this.initialDelayInput.addClass("error");

					
					}
					
					if (this.page.propertiesPanel.errorMessages.length != 0){
						this.page.propertiesPanel.showErrorMessages();
						return false;
					}
					
					return true;
				};
			}
		});