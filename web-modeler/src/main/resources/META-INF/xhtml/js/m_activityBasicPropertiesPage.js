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
		[ "m_utils", "m_constants", "m_command", "m_commandsController", "m_model",
				"m_basicPropertiesPage", "m_activity" ],
		function(m_utils, m_constants, m_command, m_commandsController, m_model,
				m_basicPropertiesPage, m_activity) {
			return {
				create : function(propertiesPanel) {
					var page = new ActivityBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ActivityBasicPropertiesPage(propertiesPanel) {
				// Inheritance

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

					this.applicationInput = this.mapInputId("applicationInput");
					this.applicationList = this.mapInputId("applicationList");
					this.subprocessInput = this.mapInputId("subprocessInput");
					this.subprocessList = this.mapInputId("subprocessList");
					this.shareDataInput = this.mapInputId("shareDataInput");
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

					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.allowAbortByParticipantInput,
									"@TOADD@");
					this
							.registerCheckboxInputForModelElementAttributeChangeSubmission(
									this.hibernateInitiallyInput, "@TOADD@");
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
									this.shareDataInput, "@synchshared");
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
							event.data.pae.submitSubprocessChanges();
						}
					});
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.populateApplicationSelect = function() {
					this.applicationList.empty();
					this.applicationList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>(To be defined)</option>");
					this.applicationList.append("<option value='"
							+ m_constants.AUTO_GENERATED_UI
							+ "'>(Auto-generated Screen)</option>");

					this.applicationList
							.append("<optgroup label=\"This Model\">");

					for ( var i in this.getModel().applications) {
						this.applicationList
								.append("<option value='"
										+ this.getModel().applications[i]
												.getFullId()
										+ "'>"
										+ this.getModel().applications[i].name
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
				ActivityBasicPropertiesPage.prototype.setApplicationType = function(
						applicationFullId) {
					this.propertiesPanel.showHelpPanel();
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.shareDataInput.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);
					this.applicationInput.attr("checked", true);
					this.applicationList.removeAttr("disabled");

					if (applicationFullId != null) {
						this.applicationList.val(applicationFullId);
					}

					if (this.propertiesPanel.element.modelElement.applicationFullId != this.applicationList
							.val()) {
						this
								.submitChanges({
									modelElement : {
										activityType : this.applicationList
												.val() == m_constants.AUTO_GENERATED_UI ? m_constants.MANUAL_ACTIVITY_TYPE
												: m_constants.APPLICATION_ACTIVITY_TYPE,
										applicationFullId : (this.applicationList
												.val() == m_constants.TO_BE_DEFINED || this.applicationList
												.val() == m_constants.AUTO_GENERATED_UI) ? null
												: this.applicationList.val()
									}
								});
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setApplicationType = function(
						applicationFullId) {
					this.propertiesPanel.showHelpPanel();
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.shareDataInput.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);
					this.applicationInput.attr("checked", true);
					this.applicationList.removeAttr("disabled");

					if (applicationFullId != null) {
						this.applicationList.val(applicationFullId);
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
										activityType : this.applicationList
												.val() == m_constants.AUTO_GENERATED_UI ? m_constants.MANUAL_ACTIVITY_TYPE
												: m_constants.APPLICATION_ACTIVITY_TYPE,
										applicationFullId : (this.applicationList
												.val() == m_constants.TO_BE_DEFINED || this.applicationList
												.val() == m_constants.AUTO_GENERATED_UI) ? null
												: this.applicationList.val()
									}
								});
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessType = function(
						subprocessFullId) {
					this.propertiesPanel.showHelpPanel();
					this.subprocessInput.attr("checked", true);
					this.subprocessList.removeAttr("disabled");
					this.shareDataInput.removeAttr("disabled");

					if (subprocessFullId != null) {
						this.subprocessList.val(subprocessFullId);
					}

					this.applicationInput.attr("checked", false);
					this.applicationList.attr("disabled", true);
					this.applicationList.val(m_constants.TO_BE_DEFINED);
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.submitSubprocessChanges = function(
						subprocessFullId) {
					if (this.propertiesPanel.element.modelElement.subprocessFullId != this.subprocessList
							.val()) {
						this
								.submitChanges({
									modelElement : {
										activityType : m_constants.SUBPROCESS_ACTIVITY_TYPE,
										subprocessFullId : this.subprocessList
												.val() == m_constants.TO_BE_DEFINED ? null
												: this.subprocessList.val()
									}
								});
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					m_utils.debug("===> Activity");
					m_utils.debug(this.getModelElement());

					this.populateApplicationSelect();
					this.populateSubprocessSelect();

					this.allowAbortByParticipantInput.attr("checked", this
							.getModelElement().attributes["@TOADD@"] == true);
					this.hibernateInitiallyInput.attr("checked", this
							.getModelElement().attributes["@TOADD@"] == true);
					this.supportsRelocationInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:source"] == true);
					this.isRelocationTargetInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:target"] == true);

					if (this.getModelElement().activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
						this.setApplicationType(m_constants.AUTO_GENERATED_UI);
						this.participantOutput.empty();

						if (this.propertiesPanel.participant != null) {
							this.participantOutput.append("executed by <b>"
									+ this.propertiesPanel.participant.name
									+ ".</b>");
						} else {
							this.participantOutput
									.append("executed by a participant to be defined.</b>");
						}
					} else if (this.getModelElement().activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this
								.setSubprocessType(this.getModelElement().subprocessFullId);
					} else if (this.getModelElement().activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						this
								.setApplicationType(this.getModelElement().applicationFullId);
						this.participantOutput.empty();

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
					if (this.validateModelElement()) {
						return true;
					}

					return false;
				};
			}
		});