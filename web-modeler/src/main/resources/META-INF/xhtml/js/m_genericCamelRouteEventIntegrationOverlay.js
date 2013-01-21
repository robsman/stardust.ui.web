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

					jQuery("label[for='routeTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.genericCamelRouteEvent.additionalRoutes"));
					jQuery("label[for='beanTextarea']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.genericCamelRouteEvent.additionalBeans"));

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.endpointUriPrefix = this
							.mapInputId("endpointUriPrefix");
					this.endpointUriTextarea = this
							.mapInputId("endpointUriTextarea");
					this.additionalRouteTextarea = this
							.mapInputId("routeTextarea");
					this.additionalBeanTextarea = this
							.mapInputId("beanTextarea");

					this.endpointUriPrefix.change({
						overlay : this
					}, function(event) {
						var overlay = event.data.overlay;

						overlay.submitRouteChanges();
					});

					this.registerForRouteChanges(this.endpointUriTextarea);
					this.registerForRouteChanges(this.additionalRouteTextarea);
					this.registerForRouteChanges(this.additionalBeanTextarea);

					// .append("<option value=\"Message\">(Map)</option>");
					// .append("<option value=\"EventCategory\">EventCategory
					// .append("<option value=\"SessionID\">SessionID
					// .append("<option value=\"MessageType\">MessageType
					// .append("<option value=\"DataDictionary\">DataDictionary
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.mapInputId = function(
						inputId) {
					return jQuery("#" + this.id + " #" + inputId);
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.submitOverlayChanges = function(
						parameterMappings) {
					if (parameterMappings == null) {
						parameterMappings = [];
					}

					this.submitChanges({
						modelElement : {
							parameterMappings : parameterMappings,
							attributes : {
								"carnot:engine:integration::overlay" : this.id
							}
						}
					});
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
					
					route = route.replace(/&/g, "&amp;");

					var xmlDoc = jQuery
							.parseXML(route);
					var xmlObject = jQuery(xmlDoc);

					var fromUri = "";
					var additionalRoutes = "";

					jQuery(xmlObject).find("from").each(function() {
						fromUri = jQuery(this).attr("uri");
					});

					jQuery(xmlObject)
							.find("route")
							.each(
									function() {
										jQuery(this)
												.children()
												.each(
														function() {
															m_utils
																	.debug("XML Element: "
																			+ m_utils
																					.xmlToString(jQuery(this)));
															if (m_utils
																	.xmlToString(
																			jQuery(this))
																	.indexOf(
																			"<from") < 0) {
																additionalRoutes += m_utils
																		.xmlToString(jQuery(this))
																		+ "\n";
															}
														});
									});

					this.endpointUriTextarea.val(fromUri);
					this.additionalRouteTextarea.val(additionalRoutes);
					this.additionalBeanTextarea.val(this.page.getEvent().attributes["carnot:engine:camel::additionalBeans"]);										

					this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.getEndpointUri = function() {
					return this.endpointUriTextarea.val();
				};

				/**
				 * 
				 */
				GenericCamelRouteEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return this.additionalRouteTextarea.val();
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
					return true;
				};
			}
		});