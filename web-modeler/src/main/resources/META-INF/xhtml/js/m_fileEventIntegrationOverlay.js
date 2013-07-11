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

					m_utils.jQuerySelect("configuration")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.configuration"));
					m_utils.jQuerySelect("parameters")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.event.parameters"));
					m_utils.jQuerySelect("label[for='directoryNameInput']")
					        .text(
						           	m_i18nUtils
						           			.getProperty("modeler.element.properties.fileEvent.directoryName"));
			        m_utils.jQuerySelect("label[for='fileNameInput']")
			        		.text(
			        				m_i18nUtils
			        						.getProperty("modeler.element.properties.fileEvent.fileName"));
					m_utils.jQuerySelect("label[for='recursiveInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.recursive"));
					m_utils.jQuerySelect("label[for='initialIntervalInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.initialInterval"));
					m_utils.jQuerySelect("label[for='postProcessingSelect']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.postProcessing"));
					m_utils.jQuerySelect("label[for='alwaysConsumeInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.element.properties.fileEvent.alwaysConsume"));

					this.directoryNameInput = this.mapInputId("directoryNameInput");
					this.fileNameInput = this.mapInputId("fileNameInput");
					this.recursiveInput = this.mapInputId("recursiveInput");
					this.initialIntervalInput = this
							.mapInputId("initialIntervalInput");
					this.initialIntervalUnitSelect = this
							.mapInputId("initialIntervalUnitSelect");
					this.repeatIntervalInput = this
							.mapInputId("repeatIntervalInput");
					this.repeatIntervalUnitSelect = this
							.mapInputId("repeatIntervalUnitSelect");

					this
							.initializeIntervalUnitSelect(this.initialIntervalUnitSelect);
					this
							.initializeIntervalUnitSelect(this.repeatIntervalUnitSelect);

					this.lockBehaviorSelect = this
							.mapInputId("lockBehaviorSelect");
					this.postProcessingSelect = this
							.mapInputId("postProcessingSelect");
					this.alwaysConsumeInput = this
							.mapInputId("alwaysConsumeInput");

					this.registerForRouteChanges(this.directoryNameInput);
					this.registerForRouteChanges(this.fileNameInput);
					this.registerForRouteChanges(this.recursiveInput);
					this.registerForRouteChanges(this.initialIntervalInput);
					this
							.registerForRouteChanges(this.initialIntervalUnitSelect);
					this.registerForRouteChanges(this.repeatIntervalInput);
					this.registerForRouteChanges(this.repeatIntervalUnitSelect);
					this.registerForRouteChanges(this.lockBehaviorSelect);
					this.registerForRouteChanges(this.postProcessingSelect);
					this.registerForRouteChanges(this.alwaysConsumeInput);
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.getEndpointUri = function() {
					var uri = "file://";
					//if(this.fileOrDirectoryNameInput!=null && this.fileOrDirectoryNameInput.val()!="Please specify ..."){
						uri += this.directoryNameInput.val();
					//}
					
					var separator = "?";
					
					if(this.fileNameInput != null && this.fileNameInput.val().length != 0){
						uri += separator + "fileName="+this.fileNameInput.val();
						separator = "&amp;";
						
					}
					
					if (this.recursiveInput.is(":checked") == true) {
						uri += separator + "recursive="
								+ this.recursiveInput.is(":checked");
						separator = "&amp;";

					}

					if (this.getIntervalInMilliseconds(
							this.initialIntervalInput.val(),
							this.initialIntervalUnitSelect.val()) != null) {
						uri += separator
								+ "initialDelay="
								+ this.getIntervalInMilliseconds(
										this.initialIntervalInput.val(),
										this.initialIntervalUnitSelect.val());
						separator = "&amp;";
					}

					if (this.getIntervalInMilliseconds(this.repeatIntervalInput
							.val(), this.repeatIntervalUnitSelect.val()) != null) {
						uri += separator
								+ "delay="
								+ this.getIntervalInMilliseconds(
										this.repeatIntervalInput.val(),
										this.repeatIntervalUnitSelect.val());
						separator = "&amp;";
					}
					if (this.lockBehaviorSelect.val() == "none") {
						// nothing to do
					} else {
						if (this.lockBehaviorSelect.val() == "markerFile") {
							uri += separator + "readLock=markerFile";
							separator = "&amp;";
						} else {
							if (this.lockBehaviorSelect.val() == "changed") {
								uri += separator + "readLock=changed";
								separator = "&amp;";
							}
						}
					}

					/*
					 * uri += "&consumer.alwaysConsume=" +
					 * this.alwaysConsumeInput.prop("checked");
					 */

					if (this.postProcessingSelect.val() == "noop") {
						uri += "&amp;noop=true";
						uri += "&amp;delete=false";
					} else if (this.postProcessingSelect.val() == "delete") {
						uri += "&amp;noop=false";
						uri += "&amp;delete=true";
					}
					return uri;
				};

				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.activate = function() {
					this.directoryNameInput.val(m_i18nUtils
							.getProperty("modeler.general.toBeDefined"));
					this.initialIntervalInput.val(5000);
					this.repeatIntervalInput.val(5000);

					var parameterMappings = [];

					parameterMappings.push(this
							.createPrimitiveParameterMapping("Message",
									"message", "String"));
					/*
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Name",
					 * "CamelFileName", "String")); parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Name Only",
					 * "CamelFileNameOnly", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Absolute File Path",
					 * "CamelFileAbsolutePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Path",
					 * "CamelFileAbsolutePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("Relative Path",
					 * "CamelFileRelativePath", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping("File Parent",
					 * "CamelFileParent", "String"));
					 * parameterMappings.push(this
					 * .createPrimitiveParameterMapping( "Last Modified Date",
					 * "CamelFileLastModified", "String"));
					 */

					this.submitOverlayChanges(parameterMappings);
				};
				FileEventIntegrationOverlay.prototype.getAdditionalRouteDefinitions = function() {
					return "<to uri=\"ipp:direct\"/>";
				};

				FileEventIntegrationOverlay.prototype.getRouteDefinitions = function() {
					return "<from uri=\"" + this.getEndpointUri() + "\"/>"
							+ this.getAdditionalRouteDefinitions();
				};
				/**
				 * 
				 */
				FileEventIntegrationOverlay.prototype.update = function() {
					var route = this.page.propertiesPanel.element.modelElement.attributes["carnot:engine:camel::camelRouteExt"];

					if (route == null) {
						return;
					}

					// TODO Need better URL encoding

					// route = route.replace(/&/g,"&amp;");

					var xmlDoc = jQuery
							.parseXML("<route>" + route + "</route>");
					var xmlObject = m_utils.jQuerySelect(xmlDoc);
					var from = m_utils.jQuerySelect(xmlObject).find("from");
					var uri = from.attr("uri");
					var uri = uri.split("//");

					if (uri[1] != null) {
						uri = uri[1].split("?");
						this.directoryNameInput.val(uri[0]);

						if (uri[1] != null) {
							var options = uri[1].split("&");

							if (options) {
								for ( var n = 0; n < options.length; ++n) {
									var option = options[n];

									option = option.split("=");

									var name = option[0];
									var value = option[1];

									if (name == "fileName") {
										this.fileNameInput.val(value);
									}else if (name == "recursive") {
										this.recursiveInput.prop("checked",
												value);
									} else if (name == "initialDelay") {
										var intervalWithUnit = this
												.getIntervalWithUnit(value);

										this.initialIntervalInput
												.val(intervalWithUnit.value);
										this.initialIntervalUnitSelect
												.val(intervalWithUnit.unit);
									} else if (name == "delay") {
										var intervalWithUnit = this
												.getIntervalWithUnit(value);

										this.repeatIntervalInput
												.val(intervalWithUnit.value);
										this.repeatIntervalUnitSelect
												.val(intervalWithUnit.unit);
										/*
										 * } else if (name ==
										 * "consumer.alwaysConsume") {
										 * this.alwaysConsumeInput.prop("checked",
										 * value == "true");
										 */
									} else if (name == "noop") {
										if (value == "true") {
											this.postProcessingSelect
													.val("noop");
										}
									} else if (name == "delete") {
										if (value == "true") {
											this.postProcessingSelect
													.val("delete");
										}
									} else if (name == "readLock") {
										this.lockBehaviorSelect.val(value)
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
					this.directoryNameInput.removeClass("error");
					this.page.propertiesPanel.errorMessages=[];

					if (this.directoryNameInput.val() == null
							|| this.directoryNameInput.val() == "") {
						this.page.propertiesPanel.errorMessages
								.push("Directory name must not be empty.");
						this.directoryNameInput.addClass("error");

						
					}
					
					if (this.page.propertiesPanel.errorMessages.length != 0){
						this.page.propertiesPanel.showErrorMessages();
						return false;
					}

					return true;
				};
			}
		});