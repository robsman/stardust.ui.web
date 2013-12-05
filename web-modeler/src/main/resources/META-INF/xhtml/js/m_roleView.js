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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_model", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model, m_i18nUtils) {
			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					var role = m_model.findParticipant(fullId);
					i18nRoleScreen();
					m_utils.debug("===> role");
					m_utils.debug(role);
					var roleView = new RoleView();
					roleView.initialize(role);
					m_commandsController.registerCommandHandler(roleView);
					//view.initialize(m_model.findApplication(fullId));
					m_utils.hideWaitCursor();
				}
			};


		function i18nRoleScreen() {

			   m_utils.jQuerySelect("label[for='guidOutput']")
		        	.text(
					m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.uuid"));

		   	  m_utils.jQuerySelect("label[for='idOutput']")
			        .text(
					m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.nameInput"));
				m_utils.jQuerySelect("label[for='cardinalityInput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.cardinality"));
				m_utils.jQuerySelect("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				m_utils.jQuerySelect("label[for='chooseAssignmentRadio']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.activityAssignment.assignment"));
				m_utils.jQuerySelect("label[for='assignAutomaticallyRadio']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.assignAutomatically"));
				m_utils.jQuerySelect("label[for='workingWeeksPerYearInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.workingWeeksPerYear"));
				m_utils.jQuerySelect("label[for='targetWorktimePerDayInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.targetWorkTime"));
				m_utils.jQuerySelect("label[for='targetWorktimePerWeekInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.targetWorkTimePerWeek"));
				m_utils.jQuerySelect("label[for='actualCostPerMinuteInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.actualCostPerMin"));
				m_utils.jQuerySelect("label[for='targetQueueDepthInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.targetQueueDepth"));
				m_utils.jQuerySelect("#activityAssignment")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.activityAssignment"));
				m_utils.jQuerySelect("#controlling")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling"));
				m_utils.jQuerySelect("#weekstext")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.role.controlling.week.name"));
				m_utils.jQuerySelect("#hours")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				m_utils.jQuerySelect("#hours2")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.hours"));
				m_utils.jQuerySelect("#dollar")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.dollar"));

			}

			/**
			 *
			 */
			function RoleView() {
				// Inheritance

				var modelElementView = m_modelElementView.create();

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(RoleView.prototype, modelElementView);

				/**
				 *
				 */
				RoleView.prototype.initialize = function(role) {
					this.id = "roleView";
					this.view = m_utils.jQuerySelect("#" + this.id);

					this.cardinalityInput = m_utils.jQuerySelect("#cardinalityInput");
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.chooseAssignmentRadio = m_utils.jQuerySelect("#chooseAssignmentRadio");
					this.assignAutomaticallyRadio = m_utils.jQuerySelect("#assignAutomaticallyRadio");
					this.workingWeeksPerYearInput = m_utils.jQuerySelect("#workingWeeksPerYearInput");
					this.targetWorktimePerDayInput = m_utils.jQuerySelect("#targetWorktimePerDayInput");
					this.targetWorktimePerWeekInput = m_utils.jQuerySelect("#targetWorktimePerWeekInput");
					this.targetQueueDepthInput = m_utils.jQuerySelect("#targetQueueDepthInput");
					this.actualCostPerMinuteInput = m_utils.jQuerySelect("#actualCostPerMinuteInput");

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

											if (view.modelElement.attributes["carnot:engine:tasks:assignment:mode"] == "assemblyLine") {
												view
														.submitChanges({
															attributes : {
																"carnot:engine:tasks:assignment:mode" : ""
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

					this.registerInputForModelElementChangeSubmission(
							this.cardinalityInput, m_constants.CARDINALITY);
					this.registerInputForModelElementAttributeChangeSubmission(
							this.workingWeeksPerYearInput,
							"carnot:pwh:workingWeeksPerYear");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.targetWorktimePerDayInput,
							"carnot:pwh:targetWorkTimePerDay");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.targetWorktimePerWeekInput,
							"carnot:pwh:targetWorkTimePerWeek");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.targetQueueDepthInput,
							"carnot:pwh:targetQueueDepth");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.actualCostPerMinuteInput,
							"carnot:pwh:actualCostPerMinute");

					this.initializeModelElementView(role);
					this.view.css("visibility", "visible");
				};

				/**
				 *
				 */
				RoleView.prototype.setModelElement = function(role) {
					this.role = role;

					this.initializeModelElement(role);

					// Set values

					if (!this.role.attributes["carnot:engine:visibility"] ||
							this.role.attributes["carnot:engine:visibility"] == "Public") {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if (this.role.attributes["carnot:engine:tasks:assignment:mode"] == "assemblyLine") {
						this.assignAutomaticallyRadio.attr("checked", true);
						this.chooseAssignmentRadio.attr("checked", false);
					} else {
						this.assignAutomaticallyRadio.attr("checked", false);
						this.chooseAssignmentRadio.attr("checked", true);
					}

					if (this.role[m_constants.CARDINALITY]) {
						this.cardinalityInput.val(this.role[m_constants.CARDINALITY]);
					}
					this.workingWeeksPerYearInput
							.val(this.role.attributes["carnot:pwh:workingWeeksPerYear"]);
					this.targetWorktimePerDayInput
							.val(this.role.attributes["carnot:pwh:targetWorkTimePerDay"]);
					this.targetWorktimePerWeekInput
							.val(this.role.attributes["carnot:pwh:targetWorkTimePerWeek"]);
					this.targetQueueDepthInput
							.val(this.role.attributes["carnot:pwh:targetQueueDepth"]);
					this.actualCostPerMinuteInput
							.val(this.role.attributes["carnot:pwh:actualCostPerMinute"]);
				};

				/**
				 *
				 */
				RoleView.prototype.toString = function() {
					return "Lightdust.RoleView";
				};

				/**
				 *
				 */
				RoleView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");
					this.cardinalityInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages.push("Role name must not be empty.");
						this.nameInput.addClass("error");
					}

					if (this.cardinalityInput.val()
							&& this.cardinalityInput.val() != ""
							&& (isNaN(this.cardinalityInput.val()) || parseInt(this.cardinalityInput
									.val()) <= 0)) {
						this.errorMessages
								.push("Cardinality should be a number greater than 0.");
						this.cardinalityInput.addClass("error");
					}

					if (this.errorMessages.length > 0) {
						this.showErrorMessages();

						return false;
					}

					return true;
				};
			}
		});