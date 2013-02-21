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
					var overlay = new GenericCamelRouteEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};
 
			/**
			 * 
			 */
			function GenericCamelRouteEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(
						GenericCamelRouteEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);
					
					jQuery("label[for='camelContextInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.genericCamelRouteEvent.camelContext"));
					jQuery("label[for='routeTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.genericCamelRouteEvent.routeDefinition"));
					jQuery("label[for='beanTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.genericCamelRouteEvent.additionalBeans"));

					this.camelContextInput = this
							.mapInputId("camelContextInput");
					
					this.configurationSpan = this.mapInputId("configuration");
					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.routeTextarea = this
							.mapInputId("routeTextarea");
					this.additionalBeanTextarea = this
							.mapInputId("beanTextarea");

					this.routeTextarea.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;
						overlay.submitRouteChanges();
					});
					
					this.additionalBeanTextarea.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;
						overlay.submitRouteChanges();
					});
					
					this.camelContextInput.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;
						overlay.submitRouteChanges();
					});

					this.registerForRouteChanges(this.camelContextInput);
					this.registerForRouteChanges(this.routeTextarea);
					this.registerForRouteChanges(this.additionalBeanTextarea);
					this.camelContextInput.val("defaultCamelContext");
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.getCamelContext = function() {
					return this.camelContextInput.val();
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.activate = function() {
					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));

					this.submitOverlayChanges(parameterMappings);
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.update = function() {
					
					var route = this.page.getEvent().attributes["carnot:engine:camel::camelRouteExt"];
					
					// TODO Need better URL encoding
				//	route = route.replace(/&/g, "&amp;");
					
					this.camelContextInput
							.val(this.page.getEvent().attributes["carnot:engine:camel::camelContextId"]);
					
					this.routeTextarea.val(route);
					
					this.additionalBeanTextarea
							.val(this.page.getEvent().attributes["carnot:engine:camel::additionalSpringBeanDefinitions"]);
					
					this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};	

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return this.routeTextarea.val().replace(/&/g, "&amp;");
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.getAdditionalBeanSpecifications = function() {
					return this.additionalBeanTextarea.val();
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.validate = function() {
					
					if (m_utils.isEmptyString(this.camelContextInput.val()) ||
							this.camelContextInput.val() == m_i18nUtils
							.getProperty("modeler.general.toBeDefined")) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.camelContextInput.addClass("error");
						this.camelContextInput.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					if (m_utils.isEmptyString(this.routeTextarea.val()) ||
							this.routeTextarea.val() == m_i18nUtils
							.getProperty("modeler.general.toBeDefined")) {
						this.getPropertiesPanel().errorMessages
								.push(m_i18nUtils
										.getProperty("modeler.general.fieldMustNotBeEmpty"));
						this.routeTextarea.addClass("error");
						this.routeTextarea.focus();

						this.getPropertiesPanel().showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});