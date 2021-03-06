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
				"bpm-modeler/js/m_ruleSetsHelper",
				"bpm-modeler/js/m_command",
				"bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_user",
				"bpm-modeler/js/m_session", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_activity", "bpm-modeler/js/m_i18nUtils",
				"bpm-modeler/js/m_modelElementUtils", "bpm-modeler/js/m_modelerUtils"],
		function(m_utils, m_constants, m_ruleSetsHelper, m_command,
				m_commandsController, m_user, m_session, m_model, m_dialog,
				m_propertiesPage, m_activity, m_i18nUtils, m_modelElementUtils, m_modelerUtils) {
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
								m_i18nUtils.getProperty("modeler.propertiesPage.toolbar.implementation.title"),
								"plugins/bpm-modeler/images/icons/wrench.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityImplementationPropertiesPage.prototype,
						propertiesPage);

				/**
				 *
				 */
				ActivityImplementationPropertiesPage.prototype.initialize = function() {
					this.heading = this.mapInputId("implementationHeading");
					this.noImplementationRow = this
							.mapInputId("noImplementationRow");
					this.noImplementationLabel = this
							.mapInputId("noImplementationLabel");
					this.applicationRow = this.mapInputId("applicationRow");
					this.applicationList = this.mapInputId("applicationList");
					this.ruleSetRow = this.mapInputId("ruleSetRow");
					this.ruleSetList = this.mapInputId("ruleSetList");
					this.appViewLink = m_utils.jQuerySelect("#appViewLink");
					this.heading.empty();
					this.heading
							.append(m_i18nUtils
									.getProperty("modeler.propertiesPage.activity.implementation.heading"));

					m_utils.jQuerySelect("label[for='applicationList']")
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

					this.appViewLink.click({
						panel : this
					}, function(event) {
						event.data.panel.openApplicationView();
					});

					this.registerInputForModelElementAttributeChangeSubmission(this.ruleSetList, "ruleSetId");
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

					var appsSorted = m_utils.convertToSortedArray(this.getModel().applications, "name", true);
					for ( var i in appsSorted) {
						if (!this
								.checkCompatibility(appsSorted[i])) {
							continue;
						}

						this.applicationList.append("<option value='"
								+ appsSorted[i].getFullId()
								+ "'>" + appsSorted[i].name
								+ "</option>");
					}

					this.applicationList.append("</optgroup>");
					this.applicationList.append("<optgroup label='"
							+ m_i18nUtils
									.getProperty("modeler.general.otherModels")
							+ "'>");

					var modelsSorted = m_utils.convertToSortedArray(m_model.getModels(), "name", true);
					for ( var n in modelsSorted) {
						if (modelsSorted[n] == this.getModel()) {
							continue;
						}

						var appsSorted = m_utils.convertToSortedArray(modelsSorted[n].applications, "name", true);
						for ( var m in appsSorted) {
							if (!m_modelElementUtils
									.hasPublicVisibility(appsSorted[m])) {
								continue;
							}

							if (!this
									.checkCompatibility(appsSorted[m])) {
								continue;
							}

							this.applicationList
									.append("<option value='"
											+ appsSorted[m]
													.getFullId()
											+ "'>"
											+ modelsSorted[n].name
											+ "/"
											+ appsSorted[m].name
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
					if (this.getModelElement().taskType === application.getCompatibleActivityTaskType()) {
						return true
					}

					return false;
				}

				/**
				 *
				 */
				ActivityImplementationPropertiesPage.prototype.populateRuleSetSelect = function() {
					this.ruleSetList.empty();
					this.ruleSetList
							.append("<option value='"
									+ m_constants.TO_BE_DEFINED
									+ "'>"
									+ m_i18nUtils
											.getProperty("modeler.general.toBeDefined")
									+ "</option>");

					var ruleSets = m_ruleSetsHelper.getRuleSets();

					if (ruleSets) {
						ruleSets = m_utils.convertToSortedArray(ruleSets, "name", true);
						for ( var i in ruleSets) {
							if (ruleSets[i].state.isDeleted != true) {
								this.ruleSetList.append("<option value='"
										+ ruleSets[i].id + "'>"
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
					this.appViewLink.addClass("imgLinkDisabled");

					if (this.getModelElement().applicationFullId != null) {
						this.applicationList
								.val(this.getModelElement().applicationFullId);
						this.appViewLink.removeClass("imgLinkDisabled");
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

					this.ruleSetList.val(this.getModelElement().attributes["ruleSetId"]);

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
				ActivityImplementationPropertiesPage.prototype.submitRuleSetChanges = function() {
					if (this.propertiesPanel.element.modelElement.ruleSetUuid != this.ruleSetList.val()) {
						this.submitChanges({
									modelElement : {
										ruleSetUuid : this.ruleSetList.val() == m_constants.TO_BE_DEFINED ? null : this.ruleSetList.val()
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
					this.appViewLink.hide();
					if (this.getModelElement().taskType == m_constants.NONE_TASK_TYPE
							|| this.getModelElement().taskType == m_constants.MANUAL_TASK_TYPE) {
						this.setNoImplementationType();
					} else if (this.getModelElement().taskType == m_constants.RULE_TASK_TYPE) {
						this.populateRuleSetSelect();
						this.setRuleType();
					} else {
						this.appViewLink.show();
						this.populateApplicationSelect();
						this.setApplicationType();
					}
				};

				/**
				 *
				 */
				ActivityImplementationPropertiesPage.prototype.validate = function() {
					if (this.getModelElement().taskType === "rule") {
						return true;
					}

					return this.validateCircularModelReference(this.applicationList);
				};

				/**
				 *
				 */
				ActivityImplementationPropertiesPage.prototype.openApplicationView = function() {
					var application = m_model.findApplication(this.getModelElement().applicationFullId);
					m_modelerUtils.openApplicationView(application);
				};
			}
		});