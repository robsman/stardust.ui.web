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
				"m_dialog", "m_modelElementView", "m_model" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					var role = m_model.findParticipant(fullId);

					m_utils.debug("===> role");
					m_utils.debug(role);

					var roleView = new RoleView();

					roleView.initialize(role);
					m_commandsController.registerCommandHandler(roleView);
				}
			};

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

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.chooseAssignmentRadio = jQuery("#chooseAssignmentRadio");
					this.assignAutomaticallyRadio = jQuery("#assignAutomaticallyRadio");
					this.workingWeeksPerYearInput = jQuery("#workingWeeksPerYearInput");
					this.targetWorktimePerDayInput = jQuery("#targetWorktimePerDayInput");
					this.targetWorktimePerWeekInput = jQuery("#targetWorktimePerWeekInput");
					this.targetQueueDepthInput = jQuery("#targetQueueDepthInput");
					this.actualCostPerMinuteInput = jQuery("#actualCostPerMinuteInput");

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

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.errorMessages.push("Role name must not be empty.");
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