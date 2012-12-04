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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_accessPoint", "bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_eventIntegrationOverlay", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_model, m_accessPoint, m_parameterDefinitionsPanel,
				m_eventIntegrationOverlay, m_i18nUtils) {

			return {
				create : function(page, id) {
					var overlay = new FileEventIntegrationOverlay();

					overlay.initialize(page, id);

					return overlay;
				}
			};

			/**
			 * 
			 */
			function FileEventIntegrationOverlay() {
				var eventIntegrationOverlay = m_eventIntegrationOverlay
						.create();

				m_utils.inheritFields(this, eventIntegrationOverlay);
				m_utils.inheritMethods(FileEventIntegrationOverlay.prototype,
						eventIntegrationOverlay);

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.initialize = function(
						page, id) {
					this.initializeEventIntegrationOverlay(page, id);

					jQuery("configuration")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.configuration"));
					jQuery("parameters")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.parameters"));
					jQuery("label[for='fileOrDirectoryNameInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.fileOrDirectoryName"));
					jQuery("label[for='recursiveInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.recursive"));
					jQuery("label[for='initialIntervalInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.initialInterval"));
					jQuery("label[for='postProcessingSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.postProcessing"));
					jQuery("label[for='alwaysConsumeInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.alwaysConsume"));

					this.fileOrDirectoryNameInput = this
							.mapInputId("fileOrDirectoryNameInput");
					this.recursiveInput = this.mapInputId("recursiveInput");
					this.initialIntervalInput = this
							.mapInputId("initialIntervalInput");
					this.initialIntervalUnitSelect = this
							.mapInputId("initialIntervalUnitSelect");

					this.initialIntervalUnitSelect
							.append("<option value='1'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.milliseconds")
									+ "</option>");
					this.initialIntervalUnitSelect
							.append("<option value='1000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.seconds")
									+ "</option>");
					this.initialIntervalUnitSelect
							.append("<option value='60000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.minutes")
									+ "</option>");
					this.initialIntervalUnitSelect
							.append("<option value='3600000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.hours")
									+ "</option>");
					this.initialIntervalUnitSelect
							.append("<option value='3600000'>"
									+ m_i18nUtils
											.getProperty("modeler.element.properties.event.days")
									+ "</option>");
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

					this.lockBehaviorSelect = this
							.mapInputId("lockBehaviorSelect");
					this.postProcessingSelect = this
							.mapInputId("postProcessingSelect");
					this.alwaysConsumeInput = this
							.mapInputId("alwaysConsumeInput");

					this.registerForRouteChanges(this.fileOrDirectoryNameInput);
					this.registerForRouteChanges(this.recursiveInput);
					this.registerForRouteChanges(this.initialIntervalInput);
					this
							.registerForRouteChanges(this.initialIntervalUnitSelect);
					this.registerForRouteChanges(this.lockBehaviorSelect);
					this.registerForRouteChanges(this.postProcessingSelect);
					this.registerForRouteChanges(this.alwaysConsumeInput);
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "file://";

					uri += this.fileOrDirectoryNameInput.val();

					uri += "?consumer.recursive="
							+ this.recursiveInput.is(":checked");
					uri += "&amp;consumer.initialDelay="
							+ (this.initialIntervalInput.val() == null ? 0
									: this.initialIntervalInput.val())
							* this.initialIntervalUnitSelect.val();
					uri += "&amp;consumer.alwaysConsume="
							+ this.alwaysConsumeInput.is(":checked");

					if (this.postProcessingSelect.val() == "noop") {
						uri += "&amp;consumer.noop=true";
						uri += "&amp;consumer.delete=false";
					} else if (this.postProcessingSelect.val() == "delete") {
						uri += "&amp;consumer.noop=false";
						uri += "&amp;consumer.delete=true";
					}

					return uri;
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.activate = function() {
					this.fileOrDirectoryNameInput
							.val(m_i18nUtils
									.getProperty("modeler.element.properties.event.toBeDefined"));
					this.initialIntervalInput.val(5000);
					this.repeatIntervalInput.val(5000);

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("File Name",
									"CamelFileName", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("File Name Only",
									"CamelFileNameOnly", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping(
									"Absolute File Path",
									"CamelFileAbsolutePath", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("File Path",
									"CamelFileAbsolutePath", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("Relative Path",
									"CamelFileRelativePath", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping("File Parent",
									"CamelFileParent", "String"));
					parameterMappings.push(this
							.createPrimitiveParameterMapping(
									"Last Modified Date",
									"CamelFileLastModified", "String"));

					this.submitOverlayChanges(parameterMappings);
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.update = function() {
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
						uri = uri[1].split("?");
						this.fileOrDirectoryNameInput.val(uri[0]);

						if (uri[1] != null) {
							var options = uri[1].split("&");

							for ( var n = 0; n < options.length; ++n) {
								var option = options[n];

								option = option.split("=");

								var name = option[0];
								var value = option[1];

								if (name == "consumer.recursive") {
									this.recursiveInput.prop("checked", value);
								} else if (name == "consumer.initialDelay") {
									this.initialIntervalInput.val(value);
									// this.initialIntervalUnitSelect.val();
								} else if (name == "consumer.alwaysConsume") {
									this.alwaysConsumeInput.prop("checked",
											value);
								} else if (name == "consumer.noop") {
									if (value == "true") {
										this.postProcessingSelect.val("noop");
									}
								} else if (name == "consumer.delete") {
									if (value == "true") {
										this.postProcessingSelect.val("delete");
									}
								}
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
				FileEventIntegrationOverlay.prototype.validate = function() {
					this.fileOrDirectoryNameInput.removeClass("error");

					if (this.fileOrDirectoryNameInput.val() == null
							|| this.fileOrDirectoryNameInput.val() == "") {
						this.page.propertiesPanel.errorMessages
								.push("File or directory name must not be empty.");
						this.fileOrDirectoryNameInput.addClass("error");

						this.page.propertiesPanel.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});