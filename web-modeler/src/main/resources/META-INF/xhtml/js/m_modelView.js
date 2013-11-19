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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_dialog", "bpm-modeler/js/m_view",
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_modelElementView","bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_jsfViewManager", "bpm-modeler/js/m_elementConfiguration"],
		function(m_utils, m_extensionManager, m_communicationController,
				m_command, m_commandsController, m_dialog, m_view, m_model, m_modelElementView,m_i18nUtils, m_constants,
				m_jsfViewManager, m_elementConfiguration) {
			return {
				initialize : function(modelId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					var model = m_model.findModel(modelId);
					var view = new ModelView();
					i18modelview();
					// TODO Make View singleton

					m_commandsController.registerCommandHandler(view);

					view.initialize(model);
					m_utils.hideWaitCursor();
				}
			};

			function i18modelview() {
				m_utils.jQuerySelect("#accesscontrol")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.accessControl"));
				m_utils.jQuerySelect("#problem")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.problem"));
				m_utils.jQuerySelect("#modeltext1")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.modelText"));
				m_utils.jQuerySelect("#modeltex2")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.modelText2"));
				m_utils.jQuerySelect("#markreadonly")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.markReadonlyLink"));
				m_utils.jQuerySelect("#markwritable")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.markWritable"));
				m_utils.jQuerySelect("#severity")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.severity"));
				m_utils.jQuerySelect("#element")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.element"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("#refreshValidationButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.validationRefresh"));
				m_utils.jQuerySelect("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("label[for='lastModificationDateOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.lastModificationDate"));
				m_utils.jQuerySelect("label[for='creationDateOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.creationDateOutput"));
				m_utils.jQuerySelect("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.name"));
				m_utils.jQuerySelect("label[for='validFromDate']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.validFrom"));				

			}
			/**
			 *
			 */
			function ModelView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(ModelView.prototype,
						modelElementView);
				var viewManager = m_jsfViewManager.create();

				/**
				 *
				 */
				ModelView.prototype.initialize = function(model) {
					this.id = "modelView";
					this.view = m_utils.jQuerySelect("#" + this.id);
					this.versionTable = m_utils.jQuerySelect("#versionTable");
					this.versionTableBody = m_utils.jQuerySelect("table#versionTable tbody");
					this.problemsTable = m_utils.jQuerySelect("#problemsTable");
					this.problemsTableBody = m_utils.jQuerySelect("table#problemsTable tbody");
					this.refreshValidationButton = m_utils.jQuerySelect("#refreshValidationButton");
					this.creationDateOutput = m_utils.jQuerySelect("#creationDateOutput");
					this.lastModificationDateOutput = m_utils.jQuerySelect("#lastModificationDateOutput");
					this.validFromDate = m_utils.jQuerySelect("#validFromDate");
					this.validFromDate.get(0).id = "validFromDate" + Math.floor((Math.random()*10000) + 1);
					this.validFromDate.datepicker();

					m_utils.jQuerySelect("#modelTabs").tabs();

					this.versionTable.tableScroll({
						height : 200
					});

					this.refreshValidationButton.click({
						"view" : this
					}, function(event) {
						event.data.view.refreshValidation();
					});

					this.validFromDate
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;
										var attribute = "carnot:engine:validFrom";
										if (m_utils.jQuerySelect(this).val()
												&& "" != m_utils.jQuerySelect(this).val()) {
											var dt = m_utils.jQuerySelect(this).datepicker(
													"getDate");
											var validFrom = dt.getFullYear()
													+ "/" + (dt.getMonth() + 1)
													+ "/" + dt.getDate()
													+ " 00:00:00:000";
											if (view.getModelElement().attributes[attribute] != validFrom) {
												var modelElement = {
													attributes : {}
												};
												modelElement.attributes[attribute] = validFrom;

												view
														.submitChanges(modelElement);
											}
										} else if (view.getModelElement().attributes
												&& view.getModelElement().attributes[attribute]
												&& "" != view.getModelElement().attributes[attribute]) {
											var modelElement = {
												attributes : {}
											};
											modelElement.attributes[attribute] = "";

											view.submitChanges(modelElement);
										}
									});

					this.initializeModelElementView(model);
					this.view.css("visibility", "visible");
				};

				/**
				 *
				 */
				ModelView.prototype.setModelElement = function(model) {
					this.model = model;

					this.initializeModelElement(model);
					if (this.model[m_constants.DATE_OF_CREATION]) {
						this.creationDateOutput.empty();
						this.creationDateOutput.append(this.model[m_constants.DATE_OF_CREATION]);
					} else {
						this.creationDateOutput.empty();
						this.creationDateOutput.append(m_i18nUtils
								.getProperty("modeler.common.value.unknown"));
					}

					if (this.model[m_constants.DATE_OF_MODIFICATION]) {
						this.lastModificationDateOutput.empty();
						this.lastModificationDateOutput.append(this.model[m_constants.DATE_OF_MODIFICATION]);
					} else {
						this.lastModificationDateOutput.empty();
						this.lastModificationDateOutput.append(m_i18nUtils
								.getProperty("modeler.common.value.unknown"));
					}

					if (this.model.attributes
							&& this.model.attributes["carnot:engine:validFrom"]) {
						this.setValidFromDate(this.model.attributes["carnot:engine:validFrom"]);
					}

					// TODO: Needed?

					if (this.model.attributes == null) {
						this.model.attributes = {};
					}

					this.updateViewIcon();

					// TODO Commented out because it is slow

					//this.refreshValidation();
				};

				/**
				 *
				 */
				ModelView.prototype.setValidFromDate = function(dateString) {
					var parts = dateString.split(" ")[0].split("/");
					var validFromDate = new Date(parseInt(parts[0]),
							(parseInt(parts[1]) - 1), parseInt(parts[2]), 0, 0,
							0, 0);
					this.validFromDate.val((validFromDate.getMonth() < 9 ? "0"
							: "")
							+ (validFromDate.getMonth() + 1)
							+ "/"
							+ (validFromDate.getDate() < 10 ? "0" : "")
							+ validFromDate.getDate()
							+ "/"
							+ validFromDate.getFullYear());
				};

				/**
				 *
				 */
				ModelView.prototype.toString = function() {
					return "Lightdust.ModelView";
				};

				/**
				 *
				 */
				ModelView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages
								.push("Model name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};

				/**
				 * Overridden
				 */
				ModelView.prototype.submitChanges = function(changes) {
					if (!this.validate()) {
						return;
					}
					// Generic attributes
					// TODO Is this really needed?

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(this.getModelElement().id, this.getModelElement().id, changes));
				};

				/**
				 *
				 */
				ModelView.prototype.getModelElement = function() {
					return this.model;
				};

				/**
				 *
				 */
				ModelView.prototype.getModel = function() {
					return this.model;
				};

				/**
				 *
				 */
				ModelView.prototype.refreshValidation = function() {
					this.problemsTableBody.empty();

					m_communicationController
							.syncGetData(
									{
										url : m_communicationController
												.getEndpointUrl()
												+ "/models/"
												+ encodeURIComponent(this.getModel().id)
												+ "/problems"
									},
									{
										"success" : function(json) {
											for ( var n = 0; n < json.length; ++n) {
												var content = "<tr>";

												if (json[n].severity == 0) {
													content += "<td class=\"infoSeverityIssueItem\">";
												} else if (json[n].severity == 1) {
													content += "<td class=\"warningSeverityIssueItem\">";
												} else if (json[n].severity == 2) {
													content += "<td class=\"errorSeverityIssueItem\">";
												}

												content += "</td>";
												content += "<td class=\"modelProblemElement\">";
												content += "<a id=\"issue"
														+ n + "\">";
												content += "</a></td>";
												content += "<td class=\"modelProblemDescription\">";
												content += json[n].message;
												content += "</td>";
												content += "</tr>";

												m_utils.jQuerySelect(
														"table#problemsTable tbody")
														.append(content);

												var viewManagerExtension = m_extensionManager
														.findExtension("viewManager");
												var viewManager = viewManagerExtension.provider.create();
												// TODO This is heuristically
												// obtained need clear model for
												// model pathes or switch to
												// UUIDs
												var segments = json[n].modelElement
														.split("/");
												var model = m_model
														.findModel(segments[0]);

												m_utils.debug("Path: " + json[n].modelElement);

												if (model.applications[segments[1]] != null) {
													var application = model.applications[segments[1]];
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Application "
																			+ model.name
																			+ "/"
																			+ application.name);
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.click(
																	{
																		viewManager : viewManager
																	},
																	function(
																			event) {
																		var applicationTypeExtension = m_extensionManager
																				.findExtensions(
																						"applicationType",
																						"id",
																						application.applicationType)[0];

																		if (applicationTypeExtension == null) {
																			throw "No extension for application type "
																					+ application.applicationType;
																		}

																		event.data.viewManager
																				.openView(
																						applicationTypeExtension.viewId,
																							"modelId="
																									+ encodeURIComponent(model.id)
																									+ "&applicationId="
																									+ encodeURIComponent(application.id)
																									+ "&applicationName="
																									+ encodeURIComponent(application.name)
																									+ "&fullId="
																									+ encodeURIComponent(application
																											.getFullId())
																									+ "&uuid="
																									+ application.uuid
																									+ "&modelUUID="
																									+ model.uuid,
																							application.uuid);
																	});
												} else if (model.processes[segments[1]] != null) {
													var process = model.processes[segments[1]];
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Process "
																			+ model.name
																			+ "/"
																			+ process.name);
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.click(
																	{
																		viewManager : viewManager
																	},
																	function(
																			event) {
																		event.data.viewManager
																				.openView(
																						"processDefinitionView",
																							"processId="
																									+ encodeURIComponent(process.id)
																									+ "&modelId="
																									+ encodeURIComponent(model.id)
																									+ "&processName="
																									+ encodeURIComponent(process.name)
																									+ "&fullId="
																									+ encodeURIComponent(process
																											.getFullId())
																									+ "&uuid="
																									+ process.uuid
																									+ "&modelUUID="
																									+ model.uuid,
																							process.uuid);
																	});
												} else {
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Model "
																			+ model.name);
													m_utils.jQuerySelect(
															"table#problemsTable tbody a#issue"
																	+ n)
															.click(
																	{
																		viewManager : viewManager
																	},
																	function(
																			event) {
																		event.data.viewManager
																				.openView(
																						"modelView",
																						"modelId="
																								+ encodeURIComponent(model.id)
																								+ "&modelName="
																								+ encodeURIComponent(model.name)
																								+ "&uuid="
																								+ model.uuid,
																						model.uuid);
																	});
												}
											}
										},
										"error" : function() {
											m_utils.debug("Error");
										}
									});

					this.problemsTable.tableScroll({
						height : 200
					});

				};

				/**
				 * Updates the view icon as per the read-only status.
				 */
				ModelView.prototype.updateViewIcon = function() {
					if (this.model.isReadonly()) {
						viewManager
								.updateView(
										"modelView",
										m_constants.VIEW_ICON_PARAM_KEY
												+ "="
												+ m_elementConfiguration
														.getIconForElementType("lockedModel"),
										this.model.uuid);
					} else {
						viewManager
								.updateView(
										"modelView",
										m_constants.VIEW_ICON_PARAM_KEY
												+ "="
												+ m_elementConfiguration
														.getIconForElementType("model"),
										this.model.uuid);
					}
				};
			}
		});