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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_extensionManager",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog",
				"bpm-modeler/js/m_basicPropertiesPage",
				"bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_modelElementUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_user, m_session, m_model, m_dialog,
				m_basicPropertiesPage, m_activity, m_i18nUtils,
				m_modelElementUtils) {
			return {
				create : function(propertiesPanel) {
					i18nProcessActivityScreen();
					var page = new ActivityBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function i18nProcessActivityScreen() {
				$("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				$("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				jQuery("#name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				jQuery("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				$("label[for='allowAbortByParticipantInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.participantAction"));
				$("label[for='hibernateInitiallyInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.hibernate"));
				$("label[for='supportsRelocationInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.relocation"));
				$("label[for='isRelocationTargetInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.relocationTarget"));
			}

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

					this.taskInput = this.mapInputId("taskInput");
					this.taskTypeList = this.mapInputId("taskTypeList");
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

					// I18N

					jQuery("label[for='taskInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.task"));
					this.taskTypeList
							.append("<option value='none'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.noneTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='manual'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.manualTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='user'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.userTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='service'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.serviceTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='script'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.scriptTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='send'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.sendTask")
									+ "</option>");
					this.taskTypeList
							.append("<option value='receive'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.receiveTask")
									+ "</option>");

					if (m_session.getInstance().technologyPreview) {
						this.taskTypeList
								.append("<option value='rule'>"
										+ m_i18nUtils
												.getProperty("modeler.activity.propertyPages.general.ruleTask")
										+ "</option>");
					}

					jQuery("label[for='subprocessInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.subProcessInput"));

					// Events

					this.registerCheckboxInputForModelElementChangeSubmission(
							this.allowAbortByParticipantInput,
							"isAbortableByPerformer");
					this.registerCheckboxInputForModelElementChangeSubmission(
							this.hibernateInitiallyInput,
							"isHibernatedOnCreation");
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

					this.taskInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.taskInput.is(":checked")) {
							event.data.page.setTaskType();
							event.data.page.submitTaskTypeChanges();
						}
					});
					this.taskTypeList.change({
						"page" : this
					}, function(event) {
						var page = event.data.page;

						if (!page.validate()) {
							return;
						}

						page.submitTaskTypeChanges();
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
				ActivityBasicPropertiesPage.prototype.populateSubprocessSelect = function() {
					this.subprocessList.empty();
					this.subprocessList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					this.subprocessList.append("<optgroup label='"
							+ m_i18nUtils
									.getProperty("modeler.general.thisModel")
							+ "'>");

					for ( var i in this.getModel().processes) {
						this.subprocessList.append("<option value='"
								+ this.getModel().processes[i].getFullId()
								+ "'>" + this.getModel().processes[i].name
								+ "</option>");
					}

					this.subprocessList.append("</optgroup>");

					this.subprocessList.append("<optgroup label='"
							+ m_i18nUtils
									.getProperty("modeler.general.otherModels")
							+ "'>");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].processes) {
							if (!(m_model.getModels()[n].processes[m].processInterfaceType === m_constants.NO_PROCESS_INTERFACE_KEY)) {
								this.subprocessList
										.append("<option value='"
												+ m_model.getModels()[n].processes[m]
														.getFullId()
												+ "'>"
												+ m_model.getModels()[n].name
												+ "/"
												+ m_model.getModels()[n].processes[m].name
												+ "</option>");
							}
						}
					}

					this.subprocessList.append("</optgroup>");
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setTaskType = function() {
					this.taskInput.attr("checked", true);
					this.taskTypeList.removeAttr("disabled");
					this.taskTypeList.val(this.getModelElement().taskType);
					this.subprocessInput.attr("checked", false);
					this.subprocessList.attr("disabled", true);
					this.subprocessModeSelect.attr("disabled", true);
					this.copyDataInput.attr("disabled", true);
					this.subprocessList.val(m_constants.TO_BE_DEFINED);

					this.participantOutput.empty();

					if (this.getModelElement().taskType == m_constants.USER_TASK_TYPE
							|| this.getModelElement().taskType == m_constants.MANUAL_TASK_TYPE) {
						if (this.getModelElement().participantFullId) {
							var participant = m_model.findParticipant(this
									.getModelElement().participantFullId);

							this.participantOutput
									.append(m_i18nUtils
											.getProperty(
													"modeler.activity.propertyPages.general.performedBy")
											.replace("{0}", participant.name));
						} else {
							this.participantOutput
									.append(m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.performerToBeSet"));
						}
					}
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessType = function(
						subprocessFullId, executionType, copyData) {
					this.taskInput.attr("checked", false);
					this.taskTypeList.attr("disabled", true);
					this.subprocessInput.attr("checked", true);
					this.subprocessList.removeAttr("disabled");
					this.subprocessModeSelect.removeAttr("disabled");

					if (subprocessFullId != null) {
						this.subprocessList.val(subprocessFullId);
					}

					this.taskInput.attr("checked", false);
					this.setSubprocessMode(executionType, copyData);

					if (m_session.getInstance().technologyPreview) {
						this.rulesActivityInput.attr("checked", false);
						this.rulesActivityPackageList.attr("disabled", true);
					}

					this.participantOutput.empty();
				};

				/**
				 * 
				 */
				ActivityBasicPropertiesPage.prototype.submitTaskTypeChanges = function() {
					if (this.propertiesPanel.element.modelElement.taskType != this.taskTypeList
							.val()) {
						this.submitChanges({
							modelElement : {
								taskType : this.taskTypeList.val()
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

					this.populateSubprocessSelect();

					if (m_user.getCurrentRole() != m_constants.INTEGRATOR_ROLE) {
						m_dialog.makeInvisible(this.subprocessExecutionRow);

					} else {
						m_dialog.makeVisible(this.subprocessExecutionRow);
					}

					this.allowAbortByParticipantInput.removeAttr("disabled");
					this.allowAbortByParticipantInput.attr("checked", this
							.getModelElement().isAbortableByPerformer == true);
					this.hibernateInitiallyInput.attr("checked", this
							.getModelElement().isHibernatedOnCreation == true);
					this.supportsRelocationInput.removeAttr("disabled");
					this.supportsRelocationInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:source"] == true);
					this.isRelocationTargetInput
							.attr(
									"checked",
									this.getModelElement().attributes["carnot:engine:relocate:target"] == true);

					if (this.getModelElement().activityType != m_constants.SUBPROCESS_ACTIVITY_TYPE) {
						this.setTaskType(this.getModelElement().taskType);
					} else {
						this
								.setSubprocessType(
										this.getModelElement().subprocessFullId,
										this.getModelElement().subprocessMode,
										this.getModelElement().attributes["carnot:engine:subprocess:copyAllData"]);

						this.allowAbortByParticipantInput
								.attr("checked", false);
						this.allowAbortByParticipantInput
								.attr("disabled", true);
						this.supportsRelocationInput.attr("checked", false);
						this.supportsRelocationInput.attr("disabled", true);
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