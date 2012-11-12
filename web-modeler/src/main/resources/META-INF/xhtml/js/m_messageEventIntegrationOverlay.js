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
		[ "m_utils", "m_constants", "m_commandsController", "m_command",
				"m_model", "m_accessPoint", "m_parameterDefinitionsPanel",
				"m_eventIntegrationOverlay", "m_i18nUtils" ],
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

					jQuery("label[for='typeSelect']")
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.messageEvent.type"));
					jQuery("label[for='nameInput']")
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.messageEvent.name"));

					jQuery("label[for='preserveQoSInput']")
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.messageEvent.name.preserveQoS"));
					
					this.configurationSpan = this.mapInputId("configuration");
					
					this.configurationSpan
					.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");
					
					this.parametersSpan.text(
							m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));
					
					this.typeSelect = this.mapInputId("typeSelect");
					this.nameInput = this.mapInputId("nameInput");

					this.registerForRouteChanges(this.typeSelect);
					this.registerForRouteChanges(this.nameInput);
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "jms:";

					uri += this.typeSelect.val();
					uri += ":";
					uri += this.nameInput.val();

					return uri;
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.activate = function() {
					this.nameInput.val(m_i18nUtils
							.getProperty("modeler.element.properties.event.toBeDefined"));

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));

					this.submitEventClassChanges(parameterMappings);
				};

				/**
				 * 
				 */
				MessageEventIntegrationOverlay.prototype.update = function() {
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					var xmlDoc = jQuery.parseXML(route);
					var xmlObject = jQuery(xmlDoc);
					var from = jQuery(xmlObject).find("from");
					var uri = from.attr("uri");
					var uri = uri.split("//");

					if (uri[1] != null) {
						// uri = uri[1].split("?");
						// this.fileOrDirectoryNameInput.val(uri[0]);
						//
						// if (uri[1] != null) {
						// var options = uri[1].split("&");
						//
						// for ( var n = 0; n < options.length; ++n) {
						// var option = options[n];
						//
						// option = option.split("=");
						//
						// var name = option[0];
						// var value = option[1];
						//
						// if (name == "") {
						// }
						// }
						// }
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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.page.propertiesPanel.errorMessages
								.push("Topic/queue name must not be empty.");
						this.nameInput.addClass("error");

						this.page.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});