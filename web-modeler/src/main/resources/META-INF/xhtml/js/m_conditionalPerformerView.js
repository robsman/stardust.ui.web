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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_dialog", "bpm-modeler/js/m_modelElementView", "bpm-modeler/js/m_model","bpm-modeler/js/m_i18nUtils"],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_dialog, m_modelElementView, m_model,m_i18nUtils) {
			return {
				initialize : function(fullId) {
					m_utils.initializeWaitCursor(m_utils.jQuerySelect("html"));
					m_utils.showWaitCursor();

					m_utils.jQuerySelect("#hideGeneralProperties").hide();
					initViewCollapseClickHandlers();
					
					var conditionalPerformer = m_model.findParticipant(fullId);
					i18nconditionalScreen();
					var view = new ConditionalPerformerView();
					// TODO Unregister!
					// In Initializer?

					m_commandsController.registerCommandHandler(view);

					view.initialize(conditionalPerformer);
					m_utils.hideWaitCursor();
				}
			};


			/**
			 * 
			 */
			function initViewCollapseClickHandlers() {
				m_utils.jQuerySelect("#showGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").hide();
					m_utils.jQuerySelect("#hideGeneralProperties").show();
				});
				m_utils.jQuerySelect("#hideGeneralProperties").click(function() {
					m_utils.jQuerySelect("#showAllProperties").show();
					m_utils.jQuerySelect("#hideGeneralProperties").hide();
				});
			}
			
			function i18nconditionalScreen() {
				
				m_utils.jQuerySelect("#hideGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));
			
				m_utils.jQuerySelect("#showGeneralProperties label")
					.text(m_i18nUtils.getProperty("modeler.element.properties.commonProperties.generalProperties"));
				
				m_utils.jQuerySelect("label[for='guidOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.uuid"));

				m_utils.jQuerySelect("label[for='idOutput']")
				.text(
						m_i18nUtils
								.getProperty("modeler.element.properties.commonProperties.id"));

				m_utils.jQuerySelect("label[for='bindingDataPathInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.name.bindingDataPath"));
				m_utils.jQuerySelect("label[for='bindingDataSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.name.bindingDataSelect"));
				m_utils.jQuerySelect("label#userRealmSecionTitle")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.userRealmSectionTitle"));
				m_utils.jQuerySelect("label[for='userRealmBindingDataPathInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.name.bindingDataPath"));
				m_utils.jQuerySelect("label[for='userRealmBindingDataSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.name.bindingDataSelect"));

				m_utils.jQuerySelect("label[for='performerTypeSelect']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.name.performerTypeSelect"));

				m_utils.jQuerySelect("label[for='publicVisibilityCheckbox']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.publicVisibility"));
				m_utils.jQuerySelect("label[for='descriptionTextarea']")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.description"));
				m_utils.jQuerySelect("label[for='nameInput']")
						.text(
								m_i18nUtils
										.getProperty("modeler.model.propertyView.participants.conditionalPerformer.performerName"));

				m_utils.jQuerySelect("#propertiesTabs span.tabLabel")
						.text(
								m_i18nUtils
										.getProperty("modeler.element.properties.commonProperties.configuration"));

				var performerTypeSelect1 = m_utils.jQuerySelect("#performerTypeSelect");

				var dropDownData = m_i18nUtils
						.getProperty("modeler.model.propertyView.participants.conditionalPerformer.performerTypeSelect.user");
				performerTypeSelect1.append("<option value=\"user\">"
						+ dropDownData + "</option>");

				dropDownData = m_i18nUtils
						.getProperty("modeler.model.propertyView.participants.conditionalPerformer.performerTypeSelect.userGroup");
				performerTypeSelect1.append("<option value=\"userGroup\">"
						+ dropDownData + "</option>");

				dropDownData = m_i18nUtils
						.getProperty("modeler.model.propertyView.participants.conditionalPerformer.performerTypeSelect.orgRole");
				performerTypeSelect1
						.append("<option value=\"modelParticipant\">"
								+ dropDownData + "</option>");

				dropDownData = m_i18nUtils
						.getProperty("modeler.model.propertyView.participants.conditionalPerformer.performerTypeSelect.orgRoleUser");
				performerTypeSelect1
						.append("<option value=\"modelParticipantOrUserGroup\">"
								+ dropDownData + "</option>");


			}

			/**
			 *
			 */
			function ConditionalPerformerView() {
				var modelElementView = m_modelElementView.create(true);

				m_utils.inheritFields(this, modelElementView);
				m_utils.inheritMethods(ConditionalPerformerView.prototype,
						modelElementView);

				/**
				 *
				 */
				ConditionalPerformerView.prototype.initialize = function(
						conditionalPerformer) {
					this.id = "conditionalPerformerView";
					this.view = m_utils.jQuerySelect("#" + this.id);
					this.publicVisibilityCheckbox = m_utils.jQuerySelect("#publicVisibilityCheckbox");
					this.performerTypeSelect = m_utils.jQuerySelect("#performerTypeSelect");
					this.bindingDataSelect = m_utils.jQuerySelect("#bindingDataSelect");
					this.bindingDataPathInput = m_utils.jQuerySelect("#bindingDataPathInput");
					this.userRealmBindingDataSelect = m_utils.jQuerySelect("#userRealmBindingDataSelect");
					this.userRealmBindingDataPathInput = m_utils.jQuerySelect("#userRealmBindingDataPathInput");

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

					this.registerInputForModelElementAttributeChangeSubmission(
							this.performerTypeSelect,
							"carnot:engine:conditionalPerformer:kind");
					this.registerInputForModelElementChangeSubmission(this.bindingDataSelect,
							"dataFullId");
					this.registerInputForModelElementChangeSubmission(this.bindingDataPathInput,
							"dataPath");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.userRealmBindingDataSelect,
							"carnot:engine:conditionalPerformer:realmData");
					this.registerInputForModelElementAttributeChangeSubmission(
							this.userRealmBindingDataPathInput,
							"carnot:engine:conditionalPerformer:realmDataPath");
					this.initializeModelElementView(conditionalPerformer);
					this.view.css("visibility", "visible");
				};

				/**
				 *
				 */
				ConditionalPerformerView.prototype.setModelElement = function(
						conditionalPerformer) {
					this.initializeModelElement(conditionalPerformer);

					this.conditionalPerformer = conditionalPerformer;

					m_utils.debug("===> Conditional Performer");
					m_utils.debug(conditionalPerformer);

					if (this.conditionalPerformer.dataFullId) {
						this.populateBindingDataSelect();
					} else {
						this.populateBindingDataSelect(true);
					}

					if (this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:realmData"]) {
						this.populateUserRealmBindingDataSelect();
					} else {
						this.populateUserRealmBindingDataSelect(true);
					}

					if (!this.conditionalPerformer.attributes["carnot:engine:visibility"]
							|| "Public" == this.conditionalPerformer.attributes["carnot:engine:visibility"]) {
						this.publicVisibilityCheckbox.attr("checked", true);
					} else {
						this.publicVisibilityCheckbox.attr("checked", false);
					}

					if (this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:kind"]) {
						this.performerTypeSelect
								.val(this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:kind"]);
					} else {
						this.performerTypeSelect.val("modelParticipant");
					}

					this.bindingDataSelect
							.val(this.conditionalPerformer.dataFullId);
					this.bindingDataPathInput
							.val(this.conditionalPerformer.dataPath);
					if (this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:realmData"]) {
						this.userRealmBindingDataSelect
								.val(this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:realmData"]);
					}
					if (this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:realmDataPath"]) {
						this.userRealmBindingDataPathInput
								.val(this.conditionalPerformer.attributes["carnot:engine:conditionalPerformer:realmDataPath"]);
					}

					if ("user" === this.performerTypeSelect.val()) {
						m_utils.jQuerySelect("tr.userRealmOnly").removeClass("invisible");
					} else {
						m_utils.jQuerySelect("tr.userRealmOnly").addClass("invisible");
					}
				};

				/**
				 *
				 */
				ConditionalPerformerView.prototype.populateBindingDataSelect = function(includeToBeDefined) {
					this.bindingDataSelect.empty();
					var modellabel = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.thisModel");
					if (includeToBeDefined) {
						this.bindingDataSelect
								.append("<option value='"
										+ m_constants.TO_BE_DEFINED
										+ "'>"
										+ m_i18nUtils
												.getProperty("modeler.general.toBeDefined")
										+ "</option>");
					}
					this.bindingDataSelect
							.append("<optgroup label=\""+modellabel+"\">");

					for ( var i in this.getModelElement().model.dataItems) {
						var dataItem = this.getModelElement().model.dataItems[i];
						if (!this.getModelElement().model.dataItems[i].externalReference) {
							this.bindingDataSelect.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ dataItem.name + "</option>");
						}
					}
					modellabel =  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel");
					this.bindingDataSelect
							.append("</optgroup><optgroup label=\""+modellabel+"\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModelElement().model) {
							continue;
						}

						for ( var m in m_model.getModels()[n].dataItems) {
							var dataItem = m_model.getModels()[n].dataItems[m];

							this.bindingDataSelect.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ m_model.getModels()[n].name + "/"
									+ dataItem.name + "</option>");
						}
					}

					this.bindingDataSelect.append("</optgroup>");
				};

				/**
				 *
				 */
				ConditionalPerformerView.prototype.populateUserRealmBindingDataSelect = function(includeToBeDefined) {
					this.userRealmBindingDataSelect.empty();
					var modellabel = m_i18nUtils.getProperty("modeler.element.properties.commonProperties.thisModel");
					if (includeToBeDefined) {
						this.userRealmBindingDataSelect
								.append("<option value='"
										+ m_constants.TO_BE_DEFINED
										+ "'>"
										+ m_i18nUtils
												.getProperty("modeler.general.toBeDefined")
										+ "</option>");
					}
					this.userRealmBindingDataSelect
							.append("<optgroup label=\""+modellabel+"\">");

					for ( var i in this.getModelElement().model.dataItems) {
						var dataItem = this.getModelElement().model.dataItems[i];

						this.userRealmBindingDataSelect.append("<option value='"
								+ dataItem.getFullId() + "'>" + dataItem.name
								+ "</option>");
					}
					modellabel =  m_i18nUtils.getProperty("modeler.element.properties.commonProperties.otherModel");
					this.userRealmBindingDataSelect
							.append("</optgroup><optgroup label=\""+modellabel+"\">");

					for ( var n in m_model.getModels()) {
						if (m_model.getModels()[n] == this.getModelElement().model) {
							continue;
						}

						for ( var m in m_model.getModels()[n].dataItems) {
							var dataItem = m_model.getModels()[n].dataItems[m];

							this.userRealmBindingDataSelect.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ m_model.getModels()[n].name + "/"
									+ dataItem.name + "</option>");
						}
					}

					this.userRealmBindingDataSelect.append("</optgroup>");
				};

				/**
				 *
				 */
				ConditionalPerformerView.prototype.toString = function() {
					return "Lightdust.ConditionalPerformerView";
				};

				/**
				 *
				 */
				ConditionalPerformerView.prototype.validate = function() {
					this.clearErrorMessages();

					this.nameInput.removeClass("error");

					if (m_utils.isEmptyString(this.nameInput.val())) {
						this.errorMessages
								.push("Conditional performer name must not be empty.");
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