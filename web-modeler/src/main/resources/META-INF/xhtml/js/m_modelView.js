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
				"bpm-modeler/js/m_model", "bpm-modeler/js/m_modelElementView","bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_constants" ],
		function(m_utils, m_extensionManager, m_communicationController,
				m_command, m_commandsController, m_dialog, m_view, m_model, m_modelElementView,m_i18nUtils, m_constants) {
			return {
				initialize : function(modelId) {
					m_utils.initializeWaitCursor($("html"));
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
				jQuery("#accesscontrol")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.accessControl"));
				jQuery("#problem")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.problem"));
				jQuery("#modeltext1")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.modelText"));
				jQuery("#modeltex2")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.modelText2"));
				jQuery("#markreadonly")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.markReadonlyLink"));
				jQuery("#markwritable")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.markWritable"));
				jQuery("#severity")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.severity"));
				jQuery("#element")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.element"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				jQuery("#refreshValidationButton")
						.attr(
								"title",
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.validationRefresh"));
				$("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("label[for='lastModificationDateOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.lastModificationDate"));
				$("label[for='creationDateOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.creationDateOutput"));
				$("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.name"));
				$("label[for='validFromDate']")
						.text(
								m_i18nUtils
										.getProperty("modeler.propertyView.modelView.validFrom"));
				$("#validFromDate").datepicker();

			}
			/**
			 *
			 */
			function ModelView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(ModelView.prototype,
						modelElementView);

				/**
				 *
				 */
				ModelView.prototype.initialize = function(model) {
					this.id = "modelView";
					this.view = jQuery("#" + this.id);
					this.versionTable = jQuery("#versionTable");
					this.versionTableBody = jQuery("table#versionTable tbody");
					this.problemsTable = jQuery("#problemsTable");
					this.problemsTableBody = jQuery("table#problemsTable tbody");
					this.refreshValidationButton = jQuery("#refreshValidationButton");
					this.creationDateOutput = jQuery("#creationDateOutput");
					this.lastModificationDateOutput = jQuery("#lastModificationDateOutput");
					this.validFromDate = $("#validFromDate");

					jQuery("#modelTabs").tabs();

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
										if ($(this).val()
												&& "" != $(this).val()) {
											var dt = $(this).datepicker(
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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
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
					// Generic attributes
					// TODO Is this really needed?

					if (changes.attributes == null) {
						changes.attributes = {};
					}

					m_commandsController.submitCommand(m_command
							.createUpdateModelCommand(this.getModelElement().uuid, changes));
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

												jQuery(
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
													jQuery(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Application "
																			+ model.name
																			+ "/"
																			+ application.name);
													jQuery(
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
																								+ model.id
																								+ "&applicationId="
																								+ application.id
																								+ "&fullId="
																								+ application
																										.getFullId(),
																						application
																								.getFullId());
																	});
												} else if (model.processes[segments[1]] != null) {
													var process = model.processes[segments[1]];
													jQuery(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Process "
																			+ model.name
																			+ "/"
																			+ process.name);
													jQuery(
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
																								+ process.id
																								+ "&modelId="
																								+ model.id
																								+ "&processName="
																								+ process.name
																								+ "&fullId="
																								+ process
																										.getFullId(),
																						process
																								.getFullId());
																	});
												} else {
													jQuery(
															"table#problemsTable tbody a#issue"
																	+ n)
															.append(
																	"Model "
																			+ model.name);
													jQuery(
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
																								+ model.id
																								+ "&modelName="
																								+ model.name,
																						model.id);
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
			}
		});