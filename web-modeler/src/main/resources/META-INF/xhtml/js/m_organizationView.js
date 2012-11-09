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
				"m_dialog", "m_modelElementView", "m_model","m_i18nUtils"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model,m_i18nUtils) {
			return {
				initialize : function(fullId) {
					var organization = m_model.findParticipant(fullId);
					i18nOrganizationview();
					m_utils.debug("===> Organization");
					m_utils.debug(organization);

					var view = new OrganizationView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(organization);
					
				}
			};

						
			function i18nOrganizationview() {
				
				$("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));
								
				$("label[for='idOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.id"));

				
				$("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.organizationName"));
				$("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				$("label[for='supportsDepartmentsCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.supportDepartment"));
				$("label[for='departmentDataSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.data"));
				$("label[for='departmentDataPathInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dataPath"));
				$("label[for='leaderSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.leader"));
				$("label[for='chooseAssignmentRadio']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.assignment"));
				$("label[for='assignAutomaticallyRadio']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.assignAutomatically"));
				$("label[for='costCenterInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.costCenter"));
				jQuery("#deptartment")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.department"));
				jQuery("#teamlead")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.organization.teamLead"));
				jQuery("#activityassignment")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.activityAssignment"));
				jQuery("#controlling")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling"));
				jQuery("#basicPropertiesPage div.heading")
						.text(
								m_i18nUtils
										.getProperty("modeler.processDefinition.propertyPages.general.heading"));
			}

			/**
			 *
			 */
			function OrganizationView() {
				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(OrganizationView.prototype,
						modelElementView);

				/**
				 *
				 */
				OrganizationView.prototype.initialize = function(organization) {
					this.id = "organizationView";

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

					//TODO - check if needed, else delete
//					this.supportsDepartmentsCheckbox
//							.change(
//									{
//										view : this
//									},
//									function(event) {
//										var view = event.data.view;
//
//										view
//												.setSupportDepartments(view.supportsDepartmentsCheckbox
//														.is(":checked"));
//
//										// Submit changes
//									});
//					this.departmentDataSelect
//							.change(
//									{
//										view : this
//									},
//									function(event) {
//										var view = event.data.view;
//
//										view.departmentDataPathInput.val(null);
//										view
//												.submitChanges({
//													attributes : {
//														"carnot:engine:dataId" : view.departmentDataSelect
//																.val() == m_constants.TO_BE_DEFINED ? null
//																: view.departmentDataSelect
//																		.val(),
//														"carnot:engine:dataId" : null
//													}
//												});
//									});
//					this.departmentDataPathInput
//							.change(
//									{
//										view : this
//									},
//									function(event) {
//										var view = event.data.view;
//
//										view
//												.submitChanges({
//													attributes : {
//														"carnot:engine:dataId" : view.departmentDataPathInput
//																.val()
//													}
//												});
//									});

					this.leaderSelect.change({
						view : this
					}, function(event) {
						var view = event.data.view;

						view.submitChanges({
							teamLeadFullId : view.leaderSelect.val()
						});
					});

					this.initializeModelElementView(organization);
				};

				/**
				 *
				 */
				OrganizationView.prototype.setModelElement = function(organization) {
					this.organization = organization;

					this.initializeModelElement(organization);
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
				OrganizationView.prototype.populateDepartmentDataSelectInput = function() {
					this.departmentDataSelect.empty();
					this.departmentDataSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
				var	 modellabel =  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.thisModel");
					this.departmentDataSelect
							.append("<optgroup label=\""+modellabel+"\">");

					for ( var i in this.getModelElement().model.dataItems) {
						var dataItem = this.getModelElement().model.dataItems[i];

						this.departmentDataSelect.append("<option value='"
								+ dataItem.id + "'>" + dataItem.name
								+ "</option>");
					}

					 modellabel =  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel");
					this.departmentDataSelect
							.append("</optgroup><optgroup label=\""+modellabel+"\">");

					for ( var n in m_model.getModels()) {
						if (this.getModelElement().model == m_model.getModels()[n]) {
							continue;
						}

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
					if ("true" == supportDepartments) {
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
				var dataNone =	m_i18nUtils
					.getProperty("modeler.element.properties.commonProperties.none")
					 ;
					this.leaderSelect.append("<option value='"
							+ m_constants.TO_BE_DEFINED + "'>("+dataNone+")</option>");

					for ( var i in this.getModelElement().model.participants) {
						var participant = this.getModelElement().model.participants[i];

						if (participant.parentUUID == this.getModelElement().uuid
								&& (participant.type == m_constants.ROLE_PARTICIPANT_TYPE
								|| participant.type == m_constants.TEAM_LEADER_TYPE)) {
							this.leaderSelect.append("<option value='"
									+ participant.getFullId() + "'>"
									+ participant.name + "</option>");
						}
					}
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
								.push("Organization name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});