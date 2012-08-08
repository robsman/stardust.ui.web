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
				"m_basicPropertiesPage", "m_activity" ],
		function(m_utils, m_constants, m_command, m_commandsController,
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
					this.participantOutput = this.mapInputId("participantOutput");
					this.hibernateInitiallyInput = this.mapInputId("hibernateInitiallyInput");
					this.supportsRelocationInput = this.mapInputId("supportsRelocationInput");
					this.isRelocationTargetInput = this.mapInputId("isRelocationTargetInput");

					// Initialize callbacks

					this.registerCheckboxInputForModelElementAttributeChangeSubmission(
							this.hibernateInitiallyInput, "@TOADD@");
					this.registerCheckboxInputForModelElementAttributeChangeSubmission(
							this.supportsRelocationInput, "carnot:engine:relocate:source");
					this.registerCheckboxInputForModelElementAttributeChangeSubmission(
							this.isRelocationTargetInput, "carnot:engine:relocate:target");
					
					this.applicationList
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = event.data.page;

										if (!page.validate()) {
											return;
										}

										var changes = {
											modelElement : {}
										};

										if (page.applicationList.val() == m_constants.AUTO_GENERATED_UI) {
											changes.modelElement.activityType = m_constants.MANUAL_ACTIVITY_TYPE;
											changes.modelElement.applicationFullId = null;
											changes.modelElement.subprocessFullId = null;
										} else {
											changes.modelElement.activityType = m_constants.APPLICATION_ACTIVITY_TYPE;

											if (page.applicationList.val() == m_constants.TO_BE_DEFINED) {
												changes.modelElement.applicationFullId = null;

												page.propertiesPanel
														.showHelpPanel();
											} else {
												changes.modelElement.applicationFullId = page.applicationList
														.val();
											}

											changes.modelElement.subprocessFullId = null;
										}

										page.submitChanges(changes);
									});
					this.subprocessList
							.change(
									{
										"page" : this
									},
									function(event) {
										var page = event.data.page;

										if (!page.validate()) {
											return;
										}

										var changes = {
											modelElement : {}
										};

										changes.modelElement.activityType = m_constants.SUBPROCESS_ACTIVITY_TYPE;

										if (page.subprocessList.val() == m_constants.TO_BE_DEFINED) {
											changes.modelElement.subprocessFullId = null;

											page.propertiesPanel.showHelpPanel();
										} else {
											changes.modelElement.subprocessFullId = page.subprocessList
													.val();
										}

										changes.modelElement.applicationFullId = null;

										page.submitChanges(changes);
									});
					this.applicationInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.applicationInput.is(":checked")) {
							event.data.page.setApplicationType();
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
				};
				
				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setApplicationType = function(
						applicationFullId) {
					this.propertiesPanel.showHelpPanel();
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
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
										applicationFullId : this.applicationList
												.val() == m_constants.AUTO_GENERATED_UI ? null
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

					if (subprocessFullId != null) {
						this.subprocessList.val(subprocessFullId);
					}

					this.applicationInput.attr("checked", false);
					this.applicationList.attr("disabled", true);
					this.applicationList.val(m_constants.TO_BE_DEFINED);

					if (this.propertiesPanel.element.modelElement.subprocessFullId != this.subprocessList
							.val()) {
						this
								.submitChanges({
									modelElement : {
										activityType : m_constants.SUBPROCESS_ACTIVITY_TYPE,
										subprocessFullId : this.subprocessList
												.val()
									}
								});
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();

					this.hibernateInitiallyInput.attr("checked", this.propertiesPanel.element.modelElement.attributes["@TOADD@"]);
					this.supportsRelocationInput.attr("checked", this.propertiesPanel.element.modelElement.attributes["carnot:engine:relocate:source"]);
					this.isRelocationTargetInput.attr("checked", this.propertiesPanel.element.modelElement.attributes["carnot:engine:relocate:target"]);

					if (this.propertiesPanel.element.modelElement.activityType == m_constants.MANUAL_ACTIVITY_TYPE) {
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
					} else if (this.propertiesPanel.element.modelElement.activityType == m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this
								.setSubprocessType(this.propertiesPanel.element.modelElement.subprocessFullId);
					} else if (this.propertiesPanel.element.modelElement.activityType == m_constants.APPLICATION_ACTIVITY_TYPE) {
						this
								.setApplicationType(this.propertiesPanel.element.modelElement.applicationFullId);
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