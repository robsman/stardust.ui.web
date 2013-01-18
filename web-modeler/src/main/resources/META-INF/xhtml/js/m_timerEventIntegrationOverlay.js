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
					var overlay = new TimerEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function TimerEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(TimerEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("label[for='repeatIntervalInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent.repeatInterval"));
					jQuery("label[for='repeatCountInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.timerEvent.repeatCount"));

					this.configurationSpan = this.mapInputId("configuration");

					this.configurationSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.configuration"));
					this.parametersSpan = this.mapInputId("parameters");

					this.parametersSpan
							.text(m_i18nUtils
									.getProperty("modeler.element.properties.event.parameters"));

					this.repeatIntervalInput = this
							.mapInputId("repeatIntervalInput");
					this.repeatIntervalUnitSelect = this
							.mapInputId("repeatIntervalUnitSelect");

					this.repeatIntervalUnitSelect
							.append("<option value='1'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.milliseconds")
									+ "</option>");
					this.repeatIntervalUnitSelect
							.append("<option value='1000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.seconds")
									+ "</option>");
					this.repeatIntervalUnitSelect
							.append("<option value='60000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.minutes")
									+ "</option>");
					this.repeatIntervalUnitSelect
							.append("<option value='3600000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.hours")
									+ "</option>");
					this.repeatIntervalUnitSelect
							.append("<option value='3600000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.days")
									+ "</option>");

					this.repeatCountInput = this.mapInputId("repeatCountInput");
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "timer://";

					return uri;
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.activate = function() {
					this.repeatIntervalInput.val(5000);
					this.repeatCountInput.val(1);

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Calendar",
									"calendar", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Fire Time",
									"fireTime", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Job Detail",
									"jobDetail", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Job Instance",
									"jobInstance", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Job Runtime",
									"jobRuntTime", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping(
									"Merged Job Data Map", "mergedJobDataMap",
									"String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Next Fire Time",
									"nextFireTime", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping(
									"Previous Fire Time", "previousFireTime",
									"String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping(
									"Scheduled Fire Time", "scheduledFireTime",
									"String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Refire Count",
									"refireCount", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Trigger Name",
									"triggerName", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Trigger Group",
									"triggerGroup", "String"));

					this.submitOverlayChanges(parameterMappings);
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.update = function() {
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					var xmlDoc = jQuery.parseXML(route);
					var xmlObject = jQuery(xmlDoc);
					var from = jQuery(xmlObject).find("from");
					var uri = from.attr("uri");
					var uri = from.attr("uri");
					var protocolAndRest = uri.split("://");

					var parametersAndOptions = protocolAndRest[1].split("?");
					var options = parametersAndOptions[1];

					// Map options

					if (options) {
						var nameValues = options.split("&");

						for ( var n = 0; n < nameValues.length; ++n) {
							var nameValue = nameValues[n].split("=");
							var name = nameValue[0];
							var value = nameValue[1];

							m_utils.debug("name: " + name);
							m_utils.debug("value: " + value);

							if (name == "repeatCount") {
								this.repeatCountInput.val(value);
							}
						}
					}

					this.parameterMappingsPanel.setScopeModel(this.page
							.getModel());
					this.parameterMappingsPanel
							.setParameterDefinitions(this.page.getEvent().parameterMappings);
				};

				/**
				 * 
				 */
				TimerEventIntegrationOverlay.prototype.validate = function() {
					return true;
				};
			}
		});