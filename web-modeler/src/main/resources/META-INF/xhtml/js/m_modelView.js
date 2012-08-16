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
		[ "m_utils", "m_extensionManager", "m_communicationController",
				"m_command", "m_commandsController", "m_dialog", "m_view",
				"m_model" ],
		function(m_utils, m_extensionManager, m_communicationController,
				m_command, m_commandsController, m_dialog, m_view, m_model) {
			return {
				initialize : function(modelId) {
					var model = m_model.findModel(modelId);
					var view = new ModelView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(model);
					view.refreshValidation();
				}
			};

			/**
			 * 
			 */
			function ModelView() {
				// Inheritance

				var view = m_view.create();

				m_utils.inheritFields(this, view);
				m_utils.inheritMethods(ModelView.prototype, view);

				this.idOutput = jQuery("#idOutput");
				this.nameInput = jQuery("#nameInput");
				this.versionTable = jQuery("#versionTable");
				this.versionTableBody = jQuery("table#versionTable tbody");
				this.problemsTable = jQuery("#problemsTable");
				this.problemsTableBody = jQuery("table#problemsTable tbody");

				jQuery("#modelTabs").tabs();

				this.versionTable.tableScroll({
					height : 200
				});

				this.nameInput.change({
					"view" : this
				}, function(event) {
					var view = event.data.view;

					if (!view.validate()) {
						return;
					}

					if (view.model.name != view.nameInput.val()) {
						view.submitChanges({
							name : view.nameInput.val()
						});
					}
				});

				/**
				 * 
				 */
				ModelView.prototype.initialize = function(model) {
					this.model = model;

					this.idOutput.empty();
					this.idOutput.append(this.model.id);
					this.nameInput.val(this.model.name);

					if (this.model.attributes == null) {
						this.model.attributes = {};
					}
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
				 * 
				 */
				ModelView.prototype.submitChanges = function(changes) {
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.model.model.id, this.model.oid,
									changes));
				};

				/**
				 * 
				 */
				ModelView.prototype.processCommand = function(command) {
					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.model.oid) {

						m_utils.inheritFields(this.model,
								object.changes.modified[0]);

						this.initialize(this.model);
					}
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
												+ this.model.id
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
												content += "<td><a id=\"issue"
														+ n + "\">";
												content += "</a></td>";
												content += "<td>";
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