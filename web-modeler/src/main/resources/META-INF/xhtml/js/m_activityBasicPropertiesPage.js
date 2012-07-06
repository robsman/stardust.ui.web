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
		[ "m_utils", "m_constants", "m_command",
				"m_commandsController", "m_propertiesPage", "m_activity" ],
		function(m_utils, m_constants, m_command,
				m_commandsController, m_propertiesPage, m_activity) {
			return {
				createPropertiesPage : function(propertiesPanel) {
					return new ActivityBasicPropertiesPage(propertiesPanel);
				}
			};

			function ActivityBasicPropertiesPage(propertiesPanel) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "basicPropertiesPage", "Basic");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ActivityBasicPropertiesPage.prototype,
						propertiesPage);

				// Field initialization

				this.nameInput = this.mapInputId("nameInput");
				this.descriptionInput = this.mapInputId("descriptionInput");
				this.applicationInput = this.mapInputId("applicationInput");
				this.applicationList = this.mapInputId("applicationList");
				this.subprocessInput = this.mapInputId("subprocessInput");
				this.subprocessList = this.mapInputId("subprocessList");
				this.participantOutput = this.mapInputId("participantOutput");

				this.initializeDocumentationHandling();

				// Initialize callbacks

				this.nameInput
						.change(
								{
									"page" : this
								},
								function(event) {
									m_commandsController
											.submitCommand(m_command
													.createRenameCommand(
															event.data.page.propertiesPanel.element.getPath(true),
															{
																"id" : event.data.page.propertiesPanel.element.modelElement.id,
																"name" : event.data.page.propertiesPanel.element.modelElement.name
															},
															{
																"name" : event.data.page.nameInput
																.val()
															}));
								});

				this.applicationInput.click({
					"callbackScope" : this
				}, function(event) {
					if (event.data.callbackScope.applicationInput
							.is(":checked")) {
						event.data.callbackScope.setApplicationType();
					}
				});

				this.subprocessInput.click({
					"callbackScope" : this
				},
						function(event) {
							if (event.data.callbackScope.subprocessInput
									.is(":checked")) {
								event.data.callbackScope.setSubprocessType();
							}
						});

				// Populate application from model

				this.applicationList.empty();
				this.applicationList.append("<option value='"
						+ m_constants.TO_BE_DEFINED
						+ "'>(To be defined)</option>");
				this.applicationList.append("<option value='"
						+ m_constants.AUTO_GENERATED_UI
						+ "'>(Auto-generated Screen)</option>");

				for ( var n in this.propertiesPanel.models) {
					for ( var m in this.propertiesPanel.models[n].applications) {
						this.applicationList
								.append("<option value='"
										+ this.propertiesPanel.models[n].applications[m]
												.getFullId()
										+ "'>"
										+ this.propertiesPanel.models[n].name
										+ "/"
										+ this.propertiesPanel.models[n].applications[m].name
										+ "</option>");
					}
				}

				// Populate subprocesses from model

				this.subprocessList.empty();
				this.subprocessList.append("<option value='"
						+ m_constants.TO_BE_DEFINED
						+ "'>(To be defined)</option>");

				for ( var n in this.propertiesPanel.models) {
					for ( var m in this.propertiesPanel.models[n].processes) {
						this.subprocessList
								.append("<option value='"
										+ this.propertiesPanel.models[n].processes[m]
												.getFullId()
										+ "'>"
										+ this.propertiesPanel.models[n].name
										+ "/"
										+ this.propertiesPanel.models[n].processes[m].name
										+ "</option>");
					}
				}

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setApplicationType = function() {
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);
					this.applicationInput.attr("checked", true);
					this.applicationList.removeAttr("disabled");
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessType = function() {
					this.subprocessInput.attr("checked", true);
					this.subprocessList.removeAttr("disabled");
					this.applicationInput.attr("checked", false);
					this.applicationList.attr("disabled", true);
					this.applicationList.val(m_constants.TO_BE_DEFINED);
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setElement = function() {
					this.nameInput.removeClass("error");

					this.nameInput
							.val(this.propertiesPanel.element.modelElement.name);
					this.descriptionInput
							.val(this.propertiesPanel.element.modelElement.description);
					this.participantOutput.empty();
					this.loadDocumentUrl();

					if (this.propertiesPanel.element.modelElement.activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
						this.setApplicationType();
						this.applicationList.val(m_constants.AUTO_GENERATED_UI);
						if (this.propertiesPanel.participant != null) {
							this.participantOutput.append("executed by <b>"
									+ this.propertiesPanel.participant.name
									+ ".</b>");
						} else {
							this.participantOutput
									.append("executed by a participant to be defined.</b>");
						}
					} else if (this.propertiesPanel.element.modelElement.activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this.setSubprocessType();
						this.subprocessList
								.val(this.propertiesPanel.element.modelElement.subprocessFullId);
					} else if (this.propertiesPanel.element.modelElement.activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						this.setApplicationType();
						this.applicationList
								.val(this.propertiesPanel.element.modelElement.applicationFullId);
						if (this.propertiesPanel.participant != null) {
							this.participantOutput.append("executed by <b>"
									+ this.propertiesPanel.participant.name
									+ "</b>");
						}
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.validate = function() {
					this.nameInput.removeClass("error");

					if (this.nameInput.val() == null
							|| this.nameInput.val() == "") {
						this.propertiesPanel.errorMessages
								.push("Activity name must not be empty.");
						this.nameInput.addClass("error");
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.hideHelpPanel();

					this.propertiesPanel.element.modelElement.name = this.nameInput
							.val();
					this.propertiesPanel.element.modelElement.description = this.descriptionInput
							.val();
					this.saveDocumentUrl();

					if (this.applicationInput.is(":checked")) {
						if (this.applicationList.val() == m_constants.AUTO_GENERATED_UI) {
							this.propertiesPanel.element.modelElement.activityType = m_constants.MANUAL_ACTIVITY_TYPE;
							this.propertiesPanel.element.modelElement.applicationFullId = null;
							this.propertiesPanel.element.modelElement.subprocessFullId = null;
						} else {
							this.propertiesPanel.element.modelElement.activityType = m_constants.APPLICATION_ACTIVITY_TYPE;

							if (this.applicationList.val() == m_constants.TO_BE_DEFINED) {
								this.propertiesPanel.element.modelElement.applicationFullId = null;

								this.propertiesPanel.showHelpPanel();
							} else {
								this.propertiesPanel.element.modelElement.applicationFullId = this.applicationList
										.val();
							}

							this.propertiesPanel.element.modelElement.subprocessFullId = null;
						}
					} else if (this.subprocessInput.is(":checked")) {
						this.propertiesPanel.element.modelElement.activityType = m_constants.SUBPROCESS_ACTIVITY_TYPE;

						if (this.subprocessList.val() == m_constants.TO_BE_DEFINED) {
							this.propertiesPanel.element.modelElement.subprocessFullId = null;

							this.propertiesPanel.showHelpPanel();
						} else {
							this.propertiesPanel.element.modelElement.subprocessFullId = this.subprocessList
									.val();
						}

						this.propertiesPanel.element.modelElement.applicationFullId = null;
					}
				};
			}
		});