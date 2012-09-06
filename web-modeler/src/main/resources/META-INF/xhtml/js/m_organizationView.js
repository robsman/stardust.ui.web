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
		[ "m_utils", "m_constants", "m_command", "m_commandsController",
				"m_dialog", "m_modelElementView", "m_model",
				"m_typeDeclaration" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_typeDeclaration) {
			return {
				initialize : function(fullId) {
					var organization = m_model.findParticipant(fullId);
					var view = new OrganizationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(organization);
				}
			};

			/**
			 * 
			 */
			function OrganizationView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(OrganizationView.prototype,
						modelElementView);

				jQuery("#organizationTabs").tabs();

				/**
				 * 
				 */
				OrganizationView.prototype.initialize = function(organization) {
					this.initializeModelElementView();

					this.organization = organization;

					this.initializeModelElement(organization);

					m_utils.debug("===> Organization");
					m_utils.debug(organization);

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.chooseAssignmentRadio = jQuery("#chooseAssignmentRadio");
					this.assignAutomaticallyRadio = jQuery("#assignAutomaticallyRadio");
					this.supportsDepartmentsCheckbox = jQuery("#supportsDepartmentsCheckbox");
					this.departmentDataSelect = jQuery("#departmentDataSelect");
					this.departmentDataPathInput = jQuery("#departmentDataPathInput");
					this.costCenterInput = jQuery("#costCenterInput");
					this.leaderSelect = jQuery("#leaderSelect");

					this.registerInputForModelElementAttributeChangeSubmission(
							this.departmentDataSelect, "carnot:engine:dataId");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.departmentDataPathInput,
							"carnot:engine:dataPath");
					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.supportsDepartmentsCheckbox,
									"carnot:engine:bound");

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

										if (view.publicVisibilityCheckbox
												.is(":checked")
												&& view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
														}
													});
										} else if (!view.publicVisibilityCheckbox
												.is(":checked")
												&& view.modelElement.attributes["carnot:engine:visibility"] == "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Private"
														}
													});
										}
									});
					this.chooseAssignmentRadio
							.click(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.chooseAssignmentRadio
												.is(":checked")) {
											view.assignAutomaticallyRadio.attr(
													"checked", false);

											if (view.modelElement.attributes["carnot:engine:tasks:assignment:mode"] != "assemblyLine") {
												view
														.submitChanges({
															attributes : {
																"carnot:engine:tasks:assignment:mode" : "assemblyLine"
															}
														});
											}
										}
									});
					this.assignAutomaticallyRadio
							.click(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.assignAutomaticallyRadio
												.is(":checked")) {
											view.chooseAssignmentRadio.attr(
													"checked", false);

											if (view.modelElement.attributes["carnot:engine:tasks:assignment:mode"] != "assemblyLine") {
												view
														.submitChanges({
															attributes : {
																"carnot:engine:tasks:assignment:mode" : "assemblyLine"
															}
														});
											}
										}
									});
					this.costCenterInput
							.change(
									{
										"view" : this
									},
									function(event) {
										var view = event.data.view;

										if (!view.validate()) {
											return;
										}

										if (view.modelElement.attributes["carnot:pwh:costCenter"] != view.costCenterInput
												.val()) {
											view
													.submitChanges({
														attributes : {
															"carnot:pwh:costCenter" : view.costCenterInput
																	.val()
														}
													});
										}
									});
					this.supportsDepartmentsCheckbox
							.change(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view
												.setSupportDepartments(view.supportsDepartmentsCheckbox
														.is(":checked"));

										// Submit changes
									});
					this.departmentDataSelect
							.change(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view.departmentDataPathInput.val(null);
										view
												.submitChanges({
													attributes : {
														"carnot:engine:dataId" : view.departmentDataSelect
																.val() == m_constants.TO_BE_DEFINED ? null
																: view.departmentDataSelect
																		.val(),
														"carnot:engine:dataId" : null
													}
												});
									});
					this.departmentDataPathInput
							.change(
									{
										view : this
									},
									function(event) {
										var view = event.data.view;

										view
												.submitChanges({
													attributes : {
														"carnot:engine:dataId" : view.departmentDataPathInput
																.val()
													}
												});
									});

					this.leaderSelect.change({
						view : this
					}, function(event) {
						var view = event.data.view;

						view.submitChanges({
							teamLeadFullId : view.leaderSelect.val()
						});
					});

					this.populateDepartmentDataSelectInput();
					this.populateLeaderSelectInput();

					// TODO Workaround

					if (this.organization.attributes == null) {
						this.organization.attributes = {};
					}

					if (this.organization.teamLeadFullId != null) {
						this.leaderSelect.val(this.organization.teamLeadFullId);
					} else {
						this.leaderSelect.val(m_constants.TO_BE_DEFINED);
					}

					// Set default

					if (this.organization.attributes["carnot:engine:visibility"] == null) {
						this.organization.attributes["carnot:engine:visibility"] = "Public";
					}

					if ("Public" == this.organization.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if ("assemblyLine" == this.organization.attributes["carnot:engine:tasks:assignment:mode"]) {
						this.assignAutomaticallyRadio.attr("checked", true);
						this.chooseAssignmentRadio.attr("checked", false);
					} else {
						this.assignAutomaticallyRadio.attr("checked", false);
						this.chooseAssignmentRadio.attr("checked", true);
					}

					this
							.setSupportDepartments(
									this.organization.attributes["carnot:engine:bound"],
									this.organization.attributes["carnot:engine:dataId"],
									this.organization.attributes["carnot:engine:dataPath"]);
					this.costCenterInput
							.val(this.organization.attributes["carnot:pwh:costCenter"]);
				};

				/**
				 * 
				 */
				OrganizationView.prototype.getModelElement = function() {
					return this.organization;
				};

				/**
				 * 
				 */
				OrganizationView.prototype.populateDepartmentDataSelectInput = function() {
					this.departmentDataSelect.empty();
					this.departmentDataSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
					this.departmentDataSelect
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.getModelElement().model.dataItems) {
						var dataItem = this.getModelElement().model.dataItems[i];

						this.departmentDataSelect.append("<option value='"
								+ dataItem.getFullId() + "'>" + dataItem.name
								+ "</option>");
					}

					this.departmentDataSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						for ( var m in m_model.getModels()[n].dataItems) {
							var dataItem = m_model.getModels()[n].dataItems[m];

							this.departmentDataSelect.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ m_model.getModels()[n].name + "/"
									+ dataItem.name + "</option>");
						}
					}

					this.departmentDataSelect.append("</optgroup>");
				};

				/**
				 * 
				 */
				OrganizationView.prototype.setSupportDepartments = function(
						supportDepartments, departmentDataId,
						departmentDataPath) {
					if (supportDepartments) {
						this.departmentDataSelect.removeAttr("disabled");
						this.departmentDataPathInput.removeAttr("disabled");
						this.supportsDepartmentsCheckbox.attr("checked", true);

						if (departmentDataId == null) {
							this.departmentDataSelect
									.val(m_constants.TO_BE_DEFINED);
							this.departmentDataPathInput.val(null);
						} else {
							this.departmentDataSelect.val(departmentDataId);
							this.departmentDataPathInput
									.val(departmentDataPath);

						}
					} else {
						this.supportsDepartmentsCheckbox.attr("checked", false);
						this.departmentDataSelect.attr("disabled", true);
						this.departmentDataPathInput.attr("disabled", true);
					}
				};

				/**
				 * 
				 */
				OrganizationView.prototype.populateLeaderSelectInput = function() {
					this.leaderSelect.empty();
					this.leaderSelect
							.append("<option value='" + m_constants.TO_BE_DEFINED + "'>(None)</option>");

					this.leaderSelect.append("<optgroup label=\"This Model\">");

					var participant = null;

					for ( var i in this.getModelElement().model.participants) {
						participant = this.getModelElement().model.participants[i];

						if (participant.type == m_constants.ROLE_PARTICIPANT_TYPE) {
							this.leaderSelect.append("<option value='"
									+ participant.getFullId() + "'>"
									+ participant.name + "</option>");
						}
					}

					this.leaderSelect
							.append("</optgroup><optgroup label=\"Other Models\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModelElement().model) {
							continue;
						}

						for ( var m in m_model.getModels()[n].participants) {
							participant = m_model.getModels()[n].participants[m];

							if (participant.type == m_constants.ROLE_PARTICIPANT_TYPE) {
								this.leaderSelect.append("<option value='"
										+ participant.getFullId() + "'>"
										+ m_model.getModels()[n].name + "/"
										+ participant.name + "</option>");
							}
						}
					}

					this.leaderSelect.append("</optgroup>");

				};

				/**
				 * 
				 */
				OrganizationView.prototype.toString = function() {
					return "Lightdust.OrganizationView";
				};

				/**
				 * 
				 */
				OrganizationView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages
								.push("Application name must not be empty.");
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
				OrganizationView.prototype.processCommand = function(command) {
					if (command.type == m_constants.CHANGE_USER_PROFILE_COMMAND) {
						this.initialize(this.organization);

						return;
					}

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object
							&& null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.organization.oid) {

						m_utils.inheritFields(this.organization,
								object.changes.modified[0]);

						this.initialize(this.organization);
					}
				};
			}
		});