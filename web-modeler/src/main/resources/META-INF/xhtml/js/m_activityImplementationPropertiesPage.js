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
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_modelElementUtils" ],
		function(m_utils, m_constants, m_extensionManager, m_command,
				m_commandsController, m_user, m_session, m_model, m_dialog,
				m_propertiesPage, m_activity, m_i18nUtils, m_modelElementUtils) {
			return {
				create : function(propertiesPanel) {
					var page = new ActivityImplementationPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ActivityImplementationPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage
						.createPropertiesPage(propertiesPanel,
								"implementationPropertiesPage",
								"Implementation", // TODO I18N
								"../../images/icons/activity-implementation-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityImplementationPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.initialize = function() {
					this.heading = this.mapInputId("heading");
					this.noImplementationRow = this
							.mapInputId("noImplementationRow");
					this.noImplementationLabel = this
							.mapInputId("noImplementationLabel");
					this.applicationRow = this.mapInputId("applicationRow");
					this.applicationList = this.mapInputId("applicationList");
					this.ruleSetRow = this.mapInputId("ruleSetRow");
					this.ruleSetList = this.mapInputId("ruleSetList");

					this.heading.empty();
					this.heading
							.append(m_i18nUtils
									.getProperty("modeler.propertiesPage.activity.implementation.heading"));
					jQuery("label[for='applicationList']")
							.text(
									m_i18nUtils
											.getProperty("modeler.propertiesPage.activity.implementation.application"));

					this.applicationList.change({
						"page" : this
					}, function(event) {
						var page = event.data.page;

						if (!page.validate()) {
							return;
						}

						page.submitApplicationChanges();
					});
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.populateApplicationSelect = function() {
					this.applicationList.empty();
					this.applicationList.append("<option value='"
							+ m_constants.TO_BE_DEFINED
							+ "'>"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					this.applicationList.append("<optgroup label='"
							+ m_i18nUtils
									.getProperty("modeler.general.thisModel")
							+ "'>");

					for ( var i in this.getModel().applications) {
						if (!this
								.checkCompatibility(this.getModel().applications[i])) {
							continue;
						}

						this.applicationList.append("<option value='"
								+ this.getModel().applications[i].getFullId()
								+ "'>" + this.getModel().applications[i].name
								+ "</option>");
					}

					this.applicationList.append("</optgroup>");
					this.applicationList.append("<optgroup label='"
							+ m_i18nUtils
									.getProperty("modeler.general.otherModels")
							+ "'>");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModel()) {
							continue;
						}

						for ( var m in m_model.getModels()[n].applications) {
							if (!m_modelElementUtils
									.hasPublicVisibility(m_model.getModels()[n].applications[m])) {
								continue;
							}

							if (!this
									.checkCompatibility(m_model.getModels()[n].applications[m])) {
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
				ActivityImplementationPropertiesPage.prototype.checkCompatibility = function(
						application) {
					if (this.getModelElement().taskType == m_constants.USER_TASK_TYPE
							&& !application.interactive) {
						return false;
					}

					if (this.getModelElement().taskType != m_constants.USER_TASK_TYPE
							&& application.interactive) {
						return false;
					}

					if (this.getModelElement().taskType == m_constants.SCRIPT_TASK_TYPE
							&& application.applicationType != "messageTransformationBean") {
						return false;
					}

					if (this.getModelElement().taskType == m_constants.SEND_TASK_TYPE
							&& !(application.applicationType == "jms" || application.applicationType == "mailBean")) {
						return false;
					}

					if (this.getModelElement().taskType == m_constants.RECEIVE_TASK_TYPE
							&& application.applicationType != "jms") {
						return false;
					}

					return true;
				}

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.populateRuleSetSelect = function() {
					if (m_session.getInstance().technologyPreview) {
						var ruleSetProviders = m_extensionManager
								.findExtensions("ruleSetProvider");

						this.ruleSetList
								.append("<option value='"
										+ m_constants.TO_BE_DEFINED
										+ "'>"
										+ m_i18nUtils
												.getProperty("modeler.general.toBeDefined")
										+ "</option>");

						for ( var n = 0; n < ruleSetProviders.length; n++) {
							var ruleSetProvider = ruleSetProviders[n].provider
									.create();
							var ruleSets = ruleSetProvider.getRuleSets();

							this.ruleSetList.empty();

							for ( var i in ruleSets) {
								this.ruleSetList.append("<option value='"
										+ ruleSets[i].uuid + "'>"
										+ ruleSets[i].name + "</option>");
							}
						}
					}
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.setNoImplementationType = function() {
					m_dialog.makeVisible(this.noImplementationRow);
					m_dialog.makeInvisible(this.applicationRow);
					m_dialog.makeInvisible(this.ruleSetRow);

					this.noImplementationLabel.empty();

					if (this.getModelElement().taskType == m_constants.MANUAL_TASK_TYPE) {
						this.noImplementationLabel
								.append(m_i18nUtils
										.getProperty("modeler.propertiesPage.activity.implementation.autoGeneratedScreen"));

					} else {
						this.noImplementationLabel
								.append(m_i18nUtils
										.getProperty("modeler.propertiesPage.activity.implementation.notRequiredAvailable"));
					}
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.setApplicationType = function() {
					m_dialog.makeInvisible(this.noImplementationRow);
					m_dialog.makeVisible(this.applicationRow);
					m_dialog.makeInvisible(this.ruleSetRow);

					if (this.getModelElement().applicationFullId != null) {
						this.applicationList
								.val(this.getModelElement().applicationFullId);
					} else {
						this.applicationList.val(m_constants.TO_BE_DEFINED);
					}
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.setRuleType = function() {
					m_dialog.makeInvisible(this.noImplementationRow);
					m_dialog.makeInvisible(this.applicationRow);
					m_dialog.makeVisible(this.ruleSetRow);

					// if (ruleSetUuid != null) {
					// this.ruleSetList.val(ruleSetUuid);
					// } else {
					// this.ruleSetList.val(m_constants.TO_BE_DEFINED);
					// }
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.submitApplicationChanges = function() {
					if (this.propertiesPanel.element.modelElement.applicationFullId != this.applicationList
							.val()) {
						this
								.submitChanges({
									modelElement : {
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
				ActivityImplementationPropertiesPage.prototype.setElement = function() {
					m_utils.debug("===> Activity");
					m_utils.debug(this.getModelElement());

					if (this.getModelElement().taskType == m_constants.NONE_TASK_TYPE
							|| this.getModelElement().taskType == m_constants.MANUAL_TASK_TYPE) {
						this.setNoImplementationType();
					} else if (this.getModelElement().taskType == m_constants.RULE_TASK_TYPE) {
						this.populateRuleSetSelect();
						this.setRuleType();
					} else {
						this.populateApplicationSelect();
						this.setApplicationType();
					}
				};

				/**
				 * 
				 */
				ActivityImplementationPropertiesPage.prototype.validate = function() {
					return true;
				};
			}
		});