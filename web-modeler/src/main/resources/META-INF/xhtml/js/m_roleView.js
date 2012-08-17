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
		[ "m_utils", "m_command", "m_commandsController", "m_dialog",
				"m_modelElementView", "m_model" ],
		function(m_utils, m_command, m_commandsController, m_dialog,
				m_modelElementView, m_model) {
			return {
				initialize : function(fullId) {
					var role = m_model.findParticipant(fullId);

					var view = new RoleView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(role);
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

				jQuery("#roleTabs").tabs();

				/**
				 * 
				 */
				RoleView.prototype.initialize = function(role) {
					this.initializeModelElementView();
					this.initializeModelElement(role);

					this.role = role;

					m_utils.debug("===> role");
					m_utils.debug(role);

					this.publicVisibilityCheckbox = jQuery("#publicVisibilityCheckbox");
					this.chooseAssignmentRadio = jQuery("#chooseAssignmentRadio");
					this.assignAutomaticallyRadio = jQuery("#assignAutomaticallyRadio");

					this.workingWeeksPerYearInput = jQuery("workingWeeksPerYearInput");
					this.targetWorktimePerDayInput = jQuery("targetWorktimePerDayInput");
					this.targetWorktimePerWeekInput = jQuery("targetWorktimePerWeekInput");
					this.targetQueueDepthInput = jQuery("targetQueueDepthInput");
					this.actualCostPerMinuteInput = jQuery("actualCostPerMinuteInput");

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

										if (view.modelElement.attributes["carnot:engine:visibility"] != "Public") {
											view
													.submitChanges({
														attributes : {
															"carnot:engine:visibility" : "Public"
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

                    this.registerInputForModelElementAttributeChangeSubmission(
                    		this.workingWeeksPerYearInput, "carnot:pwh:workingWeeksPerYear");
                    this.registerInputForModelElementAttributeChangeSubmission(
                    		this.targetWorktimePerDayInput, "carnot:pwh:targetWorkTimePerDay");
                    this.registerInputForModelElementAttributeChangeSubmission(
                    		this.targetWorktimePerWeekInput, "carnot:pwh:targetWorkTimePerWeek");
                    this.registerInputForModelElementAttributeChangeSubmission(
                    		this.targetQueueDepthInput, "carnot:pwh:targetQueueDepth");
                    this.registerInputForModelElementAttributeChangeSubmission(
                    		this.actualCostPerMinuteInput, "carnot:pwh:actualCostPerMinute");

					// Set values

					if ("Public".equals(this.role.attributes["carnot:engine:visibility"])) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if ("assemblyLine".equals(this.role.attributes["carnot:engine:tasks:assignment:mode"])) {
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
				RoleView.prototype.processCommand = function(command) {
					m_utils.debug("===> Role View Process Command");
					m_utils.debug(command);

					// Parse the response JSON from command pattern

					var object = ("string" == typeof (command)) ? jQuery
							.parseJSON(command) : command;

					if (null != object && null != object.changes
							&& null != object.changes.modified
							&& 0 != object.changes.modified.length
							&& object.changes.modified[0].oid == this.role.oid) {

						m_utils.inheritFields(this.role,
								object.changes.modified[0]);

						this.initialize(this.role);
					}
				};
			}
		});