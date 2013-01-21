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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_dataTypeSelector",
				"bpm-modeler/js/m_parameterDefinitionsPanel",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_urlUtils, m_command,
				m_commandsController, m_dialog, m_modelElementView, m_model,
				m_dataTypeSelector, m_parameterDefinitionsPanel, m_i18nUtils) {
			return {
				initialize : function(fullId) {
					var view = new UiMashupApplicationView();
					i18uimashupproperties();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(m_model.findApplication(fullId));

				}
			};

			function i18uimashupproperties() {
				$("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				$("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				jQuery("#applicationName")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.applicationName"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));

				jQuery("#configuration")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));
				jQuery("#url")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.url"));
				jQuery("#browseButton")
						.attr(
								"value",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.browse"));
				jQuery("#paramDef")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.parameterDefinitions"));
				jQuery("#name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				jQuery("#direction")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.direction"));
				jQuery("#dataType")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.datatType"));
				jQuery("#primitiveType")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));
				jQuery("#deleteParameterDefinitionButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.delete"));
				jQuery("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));

				var primitiveDataTypeSelect = jQuery("#primitiveDataTypeSelect");
				var selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.string");
				primitiveDataTypeSelect.append("<option value=\"String\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.boolean");
				primitiveDataTypeSelect.append("<option value=\"boolean\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.int");
				primitiveDataTypeSelect.append("<option value=\"int\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.long");
				primitiveDataTypeSelect.append("<option value=\"long\">"
						+ selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.double");
				primitiveDataTypeSelect.append("<option value=\"double\">"
						+ selectdata + "</option>");

				// Commented as we don't support Money values yet.
				// selectdata = m_i18nUtils
				// .getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.decimal");
				// primitiveDataTypeSelect.append("<option value=\"Decimal\">"
				// + selectdata + "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.propertyView.dataTypeProperties.dataTypeSelect.calender");
				primitiveDataTypeSelect.append("<option value=\"Calendar\">"
						+ selectdata + "</option>");

				$("label[for='primitiveDataTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.primitiveType"));

				var parameterDefinitionDirectionSelect = jQuery("#parameterDefinitionDirectionSelect");

				selectdata = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.in");
				parameterDefinitionDirectionSelect
						.append("<option value=\"IN\">" + selectdata
								+ "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.element.properties.commonProperties.out");
				parameterDefinitionDirectionSelect
						.append("<option value=\"OUT\">" + selectdata
								+ "</option>");

				selectdata = m_i18nUtils
						.getProperty("modeler.model.propertyView.uiMashup.configuration.configurationProperties.direction.inOut");
				parameterDefinitionDirectionSelect
						.append("<option value=\"INOUT\">" + selectdata
								+ "</option>");
			}
			/**
			 * 
			 */
			function UiMashupApplicationView() {
				var view = m_modelElementView.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(UiMashupApplicationView.prototype, view);

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.initialize = function(
						application) {
					this.id = "uiMashupApplicationView";
					this.currentAccessPoint = null;
					this.urlInput = jQuery("#urlInput");
					this.applicationFrame = jQuery("#applicationFrame");
					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.parameterDefinitionsPanel = m_parameterDefinitionsPanel
							.create({
								scope : "uiMashupApplicationView",
								submitHandler : this,
								supportsOrdering : false,
								supportsDataMappings : false,
								supportsDescriptors : false,
								supportsDataTypeSelection : true
							});

					this.registerInputForModelElementAttributeChangeSubmission(
							this.urlInput,
							"carnot:engine:ui:externalWebApp:uri");
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

					jQuery("#runButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTable = jQuery("#outputDataTable");

										outputDataTable.empty();

										// Send input data

										m_utils.debug("Location:");
										m_utils.debug(location);

										jQuery
												.ajax(
														{
															type : "POST",
															url : m_urlUtils
																	.getModelerEndpointUrl()
																	+ "/interaction",
															contentType : "application/json",
															data : "{input: "
																	+ inputDataTextarea
																			.val()
																	+ "}"
														})
												.done(
														function() {
															// Refresh external
															// UI

															view.applicationFrame
																	.attr(
																			"src",
																			view.urlInput
																					.val()
																					+ "?ippPortalBaseUri="
																					+ m_urlUtils
																							.getModelerEndpointUrl());
														})
												.fail(
														function() {
															view.applicationFrame
																	.attr(
																			"src",
																			"");
														});
									});
					jQuery("#resetButton")
							.click(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;
										var inputDataTextarea = jQuery("#inputDataTextarea");
										var outputDataTextarea = jQuery("#outputDataTextarea");

										inputDataTextarea.empty();
										outputDataTextarea.empty();

										var inputData = "{";

										for ( var n = 0; n < view
												.getApplication().contexts["externalWebApp"].accessPoints.length; ++n) {
											var parameterDefinition = view
													.getApplication().contexts["externalWebApp"].accessPoints[n];

											m_utils
													.debug("Parameter Definition");
											m_utils.debug(parameterDefinition);

											if (parameterDefinition.direction == m_constants.OUT_ACCESS_POINT) {
												continue;
											}

											if (n > 0) {
												inputData += ","
											}

											if (parameterDefinition.dataType == "struct") {
												var typeDeclaration = m_model
														.findTypeDeclaration(parameterDefinition.structuredDataTypeFullId);

												m_utils
														.debug("Type Declaration");
												m_utils.debug(typeDeclaration);

												inputData += parameterDefinition.id;
												inputData += ": ";
												inputData += JSON
														.stringify(
																typeDeclaration
																		.createInstance(),
																null, 3);
											} else {
												// Deal with primitives and
												// other types
											}
										}

										inputData += "}";

										inputDataTextarea.append(inputData);
									});
					jQuery("#retrieveButton")
					.click(
							{
								view : this
							},
							function(event) {
								var view = event.data.view;

								var outputDataTextarea = jQuery("#outputDataTextarea");

								jQuery
										.ajax(
												{
													type : "GET",
													url : m_urlUtils
															.getModelerEndpointUrl()
															+ "/interaction",
													contentType : "application/json"
												})
										.done(
												function(data) {
													outputDataTextarea.val(JSON.stringify(data.output));
												})
										.fail(
												function() {
												});
							});

					this.initializeModelElementView(application);
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.getApplication = function() {
					return this.application;
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.setModelElement = function(
						application) {
					this.application = application;

					m_utils.debug("===> Application");
					m_utils.debug(this.application);

					if (!this.application.attributes["carnot:engine:visibility"]
							|| "Public" == this.application.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					// TODO Guard needed?

					if (this.application.contexts["externalWebApp"] == null) {
						this.application.contexts["externalWebApp"] = {
							accessPoints : []
						};
					}

					this.initializeModelElement(application);

					this.urlInput
							.val(this.application.attributes["carnot:engine:ui:externalWebApp:uri"]);
					this.parameterDefinitionsPanel
							.setScopeModel(this.application.model);
					this.parameterDefinitionsPanel
							.setParameterDefinitions(this.application.contexts["externalWebApp"].accessPoints);
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.toString = function() {
					return "Lightdust.UiMashupApplicationView";
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages.push("Data name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * 
				 */
				UiMashupApplicationView.prototype.submitParameterDefinitionsChanges = function(
						parameterDefinitionsChanges) {
					this.submitChanges({
						contexts : {
							"externalWebApp" : {
								accessPoints : parameterDefinitionsChanges
							}
						}
					});
				};
			}
		});