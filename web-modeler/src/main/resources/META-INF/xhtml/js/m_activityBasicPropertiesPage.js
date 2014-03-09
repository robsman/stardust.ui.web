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
				"bpm-modeler/js/m_modelElementUtils", "bpm-modeler/js/m_activityProcessingPropertiesCommon" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_user, m_session, m_model, m_dialog,
				m_basicPropertiesPage, m_activity, m_i18nUtils,
				m_modelElementUtils, m_activityProcessingPropertiesCommon) {
			return {
				create : function(propertiesPanel) {
					i18nProcessActivityScreen();
					var page = new ActivityBasicPropertiesPage(propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function i18nProcessActivityScreen() {
				m_utils.jQuerySelect("label[for='subprocessModeSelect']")
					.text(
						m_i18nUtils
								.getProperty("modeler.activity.propertyPages.controlling.executionMode.label"));

				m_utils.jQuerySelect("label[for='copyDataInput']")
					.text(
						m_i18nUtils
								.getProperty("modeler.activity.propertyPages.controlling.copyalldata.label"));

				m_utils.jQuerySelect("option[value='synchShared']")
					.text(
						m_i18nUtils
							.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.synchShared"));

				m_utils.jQuerySelect("option[value='synchSeparate']")
				.text(
					m_i18nUtils
						.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.synchSeparate"));

				m_utils.jQuerySelect("option[value='asynchSeparate']")
				.text(
					m_i18nUtils
						.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.asynchSeparate"));


				m_utils.jQuerySelect("label[for='guidOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("#name")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.name"));
				m_utils.jQuerySelect("#description")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("label[for='allowAbortByParticipantInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.participantAction"));
				m_utils.jQuerySelect("label[for='hibernateInitiallyInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.hibernate"));
				m_utils.jQuerySelect("label[for='supportsRelocationInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.relocation"));
				m_utils.jQuerySelect("label[for='isRelocationTargetInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.relocationTarget"));
				m_utils.jQuerySelect("label[for='processingTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.general.processingType.label"));
				m_utils.jQuerySelect("option[value='" + m_constants.SINGLE_PROCESSING_TYPE + "']")
						.text(
							m_i18nUtils
								.getProperty("modeler.activity.propertyPages.general.processingType.options.singleInstance"));
				m_utils.jQuerySelect("option[value='" + m_constants.PARALLEL_MULTI_PROCESSING_TYPE + "']")
						.text(
							m_i18nUtils
								.getProperty("modeler.activity.propertyPages.general.processingType.options.multiInstanceParallel"));
				m_utils.jQuerySelect("option[value='" + m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE + "']")
						.text(
							m_i18nUtils
								.getProperty("modeler.activity.propertyPages.general.processingType.options.multiInstanceSequential"));
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
					this.processingTypeSelect = this.mapInputId("processingTypeSelect");

					// I18N

					m_utils.jQuerySelect("label[for='taskInput']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.task"));

					this.taskTypeList.empty();
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
					this.taskTypeList
							.append("<option value='rule'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.general.ruleTask")
									+ "</option>");

					m_utils.jQuerySelect("label[for='subprocessInput']")
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
					this.processingTypeSelect.change({
						"callbackScope" : this
					}, function(event) {
						var me = event.data.callbackScope.propertiesPanel.getModelElement();
						var val = event.data.callbackScope.processingTypeSelect.val();
						if (val == m_constants.SINGLE_PROCESSING_TYPE) {
							me.setProcessingTypeSingleInstance();
						} else {
							me.setProcessingTypeMultiInstance(val === m_constants.SEQUENTIAL_MULTI_PROCESSING_TYPE);
						}

						event.data.callbackScope.submitChanges({modelElement : {loop : me.loop}});
					});

					this.taskInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.taskInput.is(":checked")) {
							event.data.page.setTaskType();
							event.data.page.submitTaskTypeChanges(true);
						}
					});
					this.taskTypeList
							.change(
									{
										"page" : this
									},
									function(event) {
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

						page.submitSubprocessChanges(true);
					});
					this.subprocessInput.click({
						"page" : this
					}, function(event) {
						if (event.data.page.subprocessInput.is(":checked")) {
							event.data.page.setSubprocessType();
							event.data.page.submitSubprocessChanges(true);
						}
					});
					this.subprocessModeSelect
							.change(
									{
										"page" : this
									},
									function(event) {
										event.data.page.validate();
										event.data.page
												.setSubprocessMode(event.data.page.subprocessModeSelect
														.val());
										event.data.page
												.submitSubprocessChanges(false);
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
						if (this.getModelElement().participantFullId
								&& m_model.findParticipant(this
										.getModelElement().participantFullId)) {
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

					this.participantOutput.empty();
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.submitTaskTypeChanges = function(force) {
					if (force || this.propertiesPanel.element.modelElement.taskType != this.taskTypeList
							.val()) {

						var submitObj = {
											modelElement : {
												taskType : this.taskTypeList.val(),
												participantFullId : (this.taskTypeList.val() == m_constants.MANUAL_TASK_TYPE
														|| this.taskTypeList.val() == m_constants.USER_TASK_TYPE) ? this
																.getElement().parentSymbol.participantFullId : null
											}
										};

						// Reset loop (processing type) object if it exists
						if (this.getModelElement().loop) {
							this.getModelElement().loop = null;
							submitObj.modelElement.loop = null;
						}
						this.submitChanges(submitObj);
					}
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.setSubprocessMode = function(
						executionType, copyData) {
					this.initSubprocessModeSelect();
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
				ActivityBasicPropertiesPage.prototype.initSubprocessModeSelect = function() {
					this.subprocessModeSelect.empty();
					if (!(this.getModelElement().loop
							&& this.getModelElement().loop.type === m_constants.MULTI_INSTANCE_LOOP_TYPE && !this
							.getModelElement().loop.sequential)) {
						this.subprocessModeSelect
								.append("<option value='synchShared'>"
										+ m_i18nUtils
												.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.synchShared")
										+ "</option>");
					}
					this.subprocessModeSelect
							.append("<option value='synchSeparate'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.synchSeparate")
									+ "</option>");
					this.subprocessModeSelect
							.append("<option value='asynchSeparate'>"
									+ m_i18nUtils
											.getProperty("modeler.activity.propertyPages.controlling.executionMode.options.asynchSeparate")
									+ "</option>");
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.submitSubprocessChanges = function(
						resetProcessingType) {
					var attributes = {};

					attributes["carnot:engine:subprocess:copyAllData"] = this.copyDataInput
							.is(":checked");

					var submitObj = {
										modelElement : {
											activityType : m_constants.SUBPROCESS_ACTIVITY_TYPE,
											subprocessFullId : this.subprocessList
													.val() == m_constants.TO_BE_DEFINED ? null
													: this.subprocessList.val(),
											subprocessMode : this.subprocessModeSelect
													.val(),
											attributes : attributes
										}
									};

					// Reset loop (processing type) object if it exists and resetProcessingType
					// flag is set
					if (resetProcessingType && this.getModelElement().loop) {
						this.getModelElement().loop = null;
						submitObj.modelElement.loop = null;
					}

					this.submitChanges(submitObj);
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

					if (m_activityProcessingPropertiesCommon
							.getProcessingType(this) === m_constants.SINGLE_PROCESSING_TYPE) {
						this.supportsRelocationInput.removeAttr("disabled");
						this.supportsRelocationInput
								.attr(
										"checked",
										this.getModelElement().attributes["carnot:engine:relocate:source"] == true);
					} else {
						this.supportsRelocationInput.attr("disabled", true);
						this.supportsRelocationInput.attr("checked", false);
					}

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

					m_activityProcessingPropertiesCommon.initProcessingType(this);
				};

				/**
				 *
				 */
				ActivityBasicPropertiesPage.prototype.validate = function() {
					if (this.validateModelElement()
							&& this.validateCircularModelReference(this.subprocessList)) {
						return true;
					}

					return false;
				};
			}
		});