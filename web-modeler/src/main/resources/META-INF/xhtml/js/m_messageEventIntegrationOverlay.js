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
					var overlay = new MessageEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 *
			 */
			function MessageEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(
						MessageEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("label[for='nameInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.name"));
					jQuery("label[for='typeSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.type"));


					jQuery("label[for='preserveQoSInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.preserveQoS"));
					jQuery("label[for='selector']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.selector"));
					jQuery("label[for='transacted']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.messageEvent.transacted"));


					this.configurationSpan = this.mapInputId("configuration");

				this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.typeSelect = this.mapInputId("typeSelect");
					this.nameInput = this.mapInputId("nameInput");


					this.clientIdInput = this.mapInputId("clientIdInput");
					this.selectorInput = this.mapInputId("selectorInput");
					this.transactedInput = this.mapInputId("transactedInput");
					this.preserveQoSInput = this.mapInputId("preserveQoSInput");

					this.jmsComponentIdInput= this.mapInputId("jmsComponentIdInput")

					this.registerForRouteChanges(this.typeSelect);
					this.registerForRouteChanges(this.nameInput);
					this.registerForRouteChanges(this.clientIdInput);
					this.registerForRouteChanges(this.selectorInput);
					this.registerForRouteChanges(this.transactedInput);
					this.registerForRouteChanges(this.preserveQoSInput);
					this.registerForRouteChanges(this.jmsComponentIdInput);

				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var defaultJmsComponentId="jms";
					if(this.jmsComponentIdInput.val() != null && this.jmsComponentIdInput.val().length != 0)
					{
						defaultJmsComponentId=this.jmsComponentIdInput.val();
					}
					var uri = defaultJmsComponentId+":";

					uri += this.typeSelect.val();
					uri += ":";
					uri += this.nameInput.val();

					var separator = "?";

					if (this.clientIdInput.val() != null && this.clientIdInput.val().length != 0) {
						uri += separator + "clientId=" + encodeURIComponent(this.clientIdInput.val());
						separator = "&";
					}

					if (this.selectorInput.val() != null && this.selectorInput.val().length != 0) {
						uri += separator + "selector=" + encodeURIComponent(this.selectorInput.val());
						separator = "&";
					}

					if(this.transactedInput.prop("checked")== true){
						uri += separator + "transacted=";
						separator = "&";
						uri += this.transactedInput.prop("checked");
					}
					if(this.preserveQoSInput.prop("checked")==true){
						uri += separator + "preserveMessageQos=";
						uri += this.preserveQoSInput.prop("checked");
					}
					uri=uri.replace(/&/g, "&amp;");
					return uri;
				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.activate = function() {
					this.nameInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));

					this.submitOverlayChanges(parameterMappings);
				};
				MessageEventIntegrationOverlay.prototype.getRouteContent = function()
				{
					var route = "<from uri=\"";

					route += this.getEndpointUri();
					route += "\"/>";
					route += this.getAdditionalRouteDefinitions();

					return route;
				};

				MessageEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};

				MessageEventIntegrationOverlay.prototype.getRouteDefinitions= function() {
					return "<from uri=\""+this.getEndpointUri()+"\"/>"+this.getAdditionalRouteDefinitions();
				}
				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.update = function() {
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

					if (uri) {
						var sourceAndProperties = uri.split("?");
						var source = sourceAndProperties[0];

						var sourceParts = source.split(":");
						this.jmsComponentIdInput.val(sourceParts[0])
						this.typeSelect.val(sourceParts[1]);

						var clientName = "";


						for ( var i = 2; i < sourceParts.length; ++i) {
							if (i > 2) {
								clientName += ":";
							}
							clientName += sourceParts[i];
						}

						this.nameInput.val(clientName);

							/* parsing the last part of the URI*/


						if(sourceAndProperties[1] != null){
							var nameValues = sourceAndProperties[1].split('&');

						for ( var n = 0; n < nameValues.length; ++n) {
							var nameValue = nameValues[n].split("=");
							var name = nameValue[0];
							var value = nameValue[1];

							m_utils.debug("name: " + name);
							m_utils.debug("value: " + value);

							if (name == "clientId") {
								this.clientIdInput.val(decodeURIComponent(value));
							} else if (name == "selector") {
								this.selectorInput.val(decodeURIComponent(value));
							} else if (name == "transacted") {
								this.transactedInput.prop("checked",
										value == "true");
							} else if (name == "preserveMessageQos") {
								this.preserveQoSInput.prop("checked",
										value == "true");
							}
						}
						}/* end URI parsing*/
					}

					this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 *
				 */
				MessageEventIntegrationOverlay.prototype.validate = function() {
					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.jmsComponentIdInput.val()) ||
							this.jmsComponentIdInput.val() == m_i18nUtils
							.getProperty("modeler.general.toBeDefined")) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.jmsComponentIdInput.addClass("error");
						this.jmsComponentIdInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}


					if (m_utils.isEmptyString(this.nameInput.val()) ||
							this.nameInput.val() == m_i18nUtils
							.getProperty("modeler.general.toBeDefined")) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.nameInput.addClass("error");
						this.nameInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});
