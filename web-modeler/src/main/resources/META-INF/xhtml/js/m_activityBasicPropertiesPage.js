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
				"m_user", "m_model", "m_dialog", "m_basicPropertiesPage",
				"m_activity" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_user,
				m_model, m_dialog, m_basicPropertiesPage, m_activity) {
			return {
				create : function(propertiesPanel) {
					var page = new ActivityBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ActivityBasicPropertiesPage(propertiesPanel) {
				var basicPropertiesPage = m_basicPropertiesPage
						.create(propertiesPanel);

				m_utils.inheritFields(this, basicPropertiesPage);
				m_utils.inheritMethods(ActivityBasicPropertiesPage.prototype,
						basicPropertiesPage);

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();

					this.userTaskInput = this.mapInputId("userTaskInput");
					this.userApplicationList = this
							.mapInputId("userApplicationList");
					this.applicationInput = this.mapInputId("applicationInput");
					this.applicationList = this.mapInputId("applicationList");
					this.subprocessInput = this.mapInputId("subprocessInput");
					this.subprocessList = this.mapInputId("subprocessList");
					this.subprocessExecutionRow = this
							.mapInputId("subprocessExecutionRow");
					this.subprocessModeSelect = this
							.mapInputId("subprocessModeSelect");
					this.copyDataInput = this.mapInputId("copyDataInput");
					this.allowAbortByParticipantInput = this
							.mapInputId("allowAbortByParticipantInput");
					this.participantOutput = this
							.mapInputId("participantOutput");
					this.hibernateInitiallyInput = this
							.mapInputId("hibernateInitiallyInput");
					this.supportsRelocationInput = this
							.mapInputId("supportsRelocationInput");
					this.isRelocationTargetInput = this
							.mapInputId("isRelocationTargetInput");

					this.registerCheckboxInputForModelElementChangeSubmission(
							this.allowAbortByParticipantInput,
							"isAbortableByPerformer");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.hibernateInitiallyInput, "isHibernatedOnCreation");
					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.supportsRelocationInput,
									"carnot:engine:relocate:source");
					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.isRelocationTargetInput,
									"carnot:engine:relocate:target");
					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.copyDataInput,
									"carnot:engine:subprocess:copyAllData");

					this.userApplicationList.change({
						"page" : this
					}, function(event) {
						var page = event.data.page;

						if (!page.validate()) {
							return;
						}

						page.submitUserTaskChanges();
					});
					this.applicationList.change({
						"page" : this
					}, function(event) {
						var page = event.data.page;

						if (!page.validate()) {
							return;
						}

						page.submitApplicationChanges();
					});
					this.subprocessList.change({
						"page" : this
					}, function(event) {
						var page = event.data.page;

						if (!page.validate()) {
							return;
						}

						page.submitSubprocessChanges();
					});
					this.userTaskInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.userTaskInput.is(":checked")) {
							event.data.page.setUserTaskType();
							event.data.page.submitUserTaskChanges();
						}
					});
					this.applicationInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.applicationInput.is(":checked")) {
							event.data.page.setApplicationType();
							event.data.page.submitApplicationChanges();
						}
					});
					this.subprocessInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.subprocessInput.is(":checked")) {
							event.data.page.setSubprocessType();
							event.data.page.submitSubprocessChanges();
						}
					});
					this.subprocessModeSelect
							.change(
									{
										"page" : this
									},
									function(event) {
										event.data.page
												.setSubprocessMode(event.data.page.subprocessModeSelect
														.val());
										event.data.page
												.submitSubprocessChanges();
									});
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.populateUserApplicationSelect = function() {
					this.userApplicationList.empty();
					this.userApplicationList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
					this.userApplicationList.append("<option value='"
							+ m_constants.AUTO_GENERATED_UI
							+ "'>(Auto-generated Screen)</option>");

					this.userApplicationList
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.getModel().applications) {
						if (!this.getModel().applications[i].interactive) {
							continue;
						}

						this.userApplicationList.append("<option value='"
								+ this.getModel().applications[i].getFullId()
								+ "'>" + this.getModel().applications[i].name
								+ "</option>");
					}

					this.userApplicationList.append("</optgroup>");
					this.userApplicationList
							.append("</optgroup><optgroup label=\"Others Model\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].applications) {
							if (!m_model.getModels()[n].applications[m].interactive) {
								continue;
							}

							this.userApplicationList
									.append("<option value='"
											+ m_model.getModels()[n].applications[m]
													.getFullId()
											+ "'>"
											+ m_model.getModels()[n].name
											+ "/"
											+ m_model.getModels()[n].applications[m].name
											+ "</option>");
						}
					}

					this.userApplicationList.append("</optgroup>");
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.populateApplicationSelect = function() {
					this.applicationList.empty();
					this.applicationList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					this.applicationList
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.getModel().applications) {
						if (this.getModel().applications[i].interactive) {
							continue;
						}

						this.applicationList.append("<option value='"
								+ this.getModel().applications[i].getFullId()
								+ "'>" + this.getModel().applications[i].name
								+ "</option>");
					}

					this.applicationList.append("</optgroup>");
					this.applicationList
							.append("</optgroup><optgroup label=\"Others Model\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].applications) {
							if (m_model.getModels()[n].applications[m].interactive) {
								continue;
							}

							this.applicationList
									.append("<option value='"
											+ m_model.getModels()[n].applications[m]
													.getFullId()
											+ "'>"
											+ m_model.getModels()[n].name
											+ "/"
											+ m_model.getModels()[n].applications[m].name
											+ "</option>");
						}
					}

					this.applicationList.append("</optgroup>");
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.populateSubprocessSelect = function() {
					this.subprocessList.empty();
					this.subprocessList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");

					this.subprocessList
							.append("<optgroup label=\"This Model\"></optgroup>");

					for ( var i in this.getModel().processes) {
						this.subprocessList.append("<option value='"
								+ this.getModel().processes[i].getFullId()
								+ "'>" + this.getModel().processes[i].name
								+ "</option>");
					}

					this.subprocessList.append("</optgroup>");

					this.subprocessList
							.append("<optgroup label=\"Others Model\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].processes) {
							this.subprocessList.append("<option value='"
									+ m_model.getModels()[n].processes[m]
											.getFullId() + "'>"
									+ m_model.getModels()[n].name + "/"
									+ m_model.getModels()[n].processes[m].name
									+ "</option>");
						}
					}

					this.subprocessList.append("</optgroup>");
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setUserTaskType = function(
						applicationFullId) {
					this.userTaskInput.attr("checked", true);
					this.userApplicationList.removeAttr("disabled");
					m_dialog.makeVisible(this.participantOutput);

					if (applicationFullId != null) {
						this.userApplicationList.val(applicationFullId);
					} else {
						this.userApplicationList
								.val(m_constants.AUTO_GENERATED_UI);
					}

					this.participantOutput.empty();

					if (this.propertiesPanel.participant != null) {
						this.participantOutput.append("executed by <b>"
								+ this.propertiesPanel.participant.name
								+ ".</b>");
					} else {
						this.participantOutput
								.append("executed by a participant to be defined.</b>");
					}

					this.applicationInput.attr("checked", false);
					this.applicationList.attr("disabled", true);
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.subprocessModeSelect.attr("disabled", true);
					this.copyDataInput.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setApplicationType = function(
						applicationFullId) {
					this.applicationInput.attr("checked", true);
					this.applicationList.removeAttr("disabled");

					if (applicationFullId != null) {
						this.applicationList.val(applicationFullId);
					} else {
						this.applicationList.val(m_constants.TO_BE_DEFINED);
					}

					this.userTaskInput.attr("checked", false);
					this.userApplicationList.attr("disabled", true);
					this.userApplicationList.val(m_constants.TO_BE_DEFINED);
					m_dialog.makeInvisible(this.participantOutput);
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.subprocessModeSelect.attr("disabled", true);
					this.copyDataInput.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessType = function(
						subprocessFullId, executionType, copyData) {
					this.subprocessInput.attr("checked", true);
					this.subprocessList.removeAttr("disabled");
					this.subprocessModeSelect.removeAttr("disabled");

					if (subprocessFullId != null) {
						this.subprocessList.val(subprocessFullId);
					}

					this.userTaskInput.attr("checked", false);
					this.userApplicationList.attr("disabled", true);
					this.userApplicationList.val(m_constants.TO_BE_DEFINED);
					m_dialog.makeInvisible(this.participantOutput);
					this.applicationInput.attr("checked", false);
					this.applicationList.attr("disabled", true);
					this.applicationList.val(m_constants.TO_BE_DEFINED);
					this.setSubprocessMode(executionType, copyData);
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.submitUserTaskChanges = function() {
					if (this.propertiesPanel.element.modelElement.applicationFullId != this.userApplicationList
							.val()) {
						this
								.submitChanges({
									modelElement : {
										activityType : this.userApplicationList
												.val() == m_constants.AUTO_GENERATED_UI ? m_constants.MANUAL_ACTIVITY_TYPE
												: m_constants.APPLICATION_ACTIVITY_TYPE,
										applicationFullId : (this.userApplicationList
												.val() == m_constants.TO_BE_DEFINED || this.userApplicationList
												.val() == m_constants.AUTO_GENERATED_UI) ? null
												: this.userApplicationList
														.val()
									}
								});
					}
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.submitApplicationChanges = function() {
					if (this.propertiesPanel.element.modelElement.applicationFullId != this.applicationList
							.val()) {
						this
								.submitChanges({
									modelElement : {
										activityType : m_constants.APPLICATION_ACTIVITY_TYPE,
										applicationFullId : this.applicationList
												.val() == m_constants.TO_BE_DEFINED ? null
												: this.applicationList.val()
									}
								});
					}
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessMode = function(
						executionType, copyData) {
					this.subprocessModeSelect.val(executionType);

					m_utils.debug("===> subprocessMode " + executionType + " "
							+ copyData);

					if (executionType == "synchShared") {
						this.copyDataInput.attr("disabled", true);
						this.copyDataInput.removeAttr("checked");
					} else {
						this.copyDataInput.removeAttr("disabled");
						this.copyDataInput.attr("checked", copyData);
					}

				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.submitSubprocessChanges = function(
						subprocessFullId) {
					var attributes = {};

					attributes["carnot:engine:subprocess:copyAllData"] = this.copyDataInput
							.is(":checked");

					this
							.submitChanges({
								modelElement : {
									activityType : m_constants.SUBPROCESS_ACTIVITY_TYPE,
									subprocessFullId : this.subprocessList
											.val() == m_constants.TO_BE_DEFINED ? null
											: this.subprocessList.val(),
									subprocessMode : this.subprocessModeSelect
											.val(),
									attributes : attributes
								}
							});
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("===> Activity");
					m_utils.debug(this.getModelElement());

					this.populateUserApplicationSelect();
					this.populateApplicationSelect();
					this.populateSubprocessSelect();

					if (m_user.getCurrentRole() != m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeInvisible(this.subprocessExecutionRow);

					} else {
						m_dialog.makeVisible(this.subprocessExecutionRow);
					}

					this.allowAbortByParticipantInput.attr("checked", this
							.getModelElement().isAbortableByPerformer == true);
					this.hibernateInitiallyInput.attr("checked", this
							.getModelElement().isHibernatedOnCreation == true);
					this.supportsRelocationInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:source"] == "true");
					this.isRelocationTargetInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:target"] == "true");

					if (this.getModelElement().activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
						this.setUserTaskType(m_constants.AUTO_GENERATED_UI);
					} else if (this.getModelElement().activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						if (this.getModelElement().applicationFullId == null
								|| m_model.findApplication(this
										.getModelElement().applicationFullId).interactive) {
							this
									.setUserTaskType(this.getModelElement().applicationFullId);
						} else {
							this
									.setApplicationType(this.getModelElement().applicationFullId);
						}
					} else if (this.getModelElement().activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this
								.setSubprocessType(
										this.getModelElement().subprocessFullId,
										this.getModelElement().subprocessMode,
										this.getModelElement().attributes["carnot:engine:subprocess:copyAllData"]);
					}
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()) {
						return true;
					}

					return false;
				};
			}
		});