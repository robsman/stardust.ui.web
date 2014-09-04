/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Yogesh.Manware
 */

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_commandsController, m_command, m_propertiesPage,
				m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					var page = new ActivityOnAssignmentPropertiesPage(
							propertiesPanel);

					page.initialize();

					return page;
				}
			};

			function ActivityOnAssignmentPropertiesPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage
						.createPropertiesPage(
								propertiesPanel,
								"onAssignmentPropertiesPage",
								m_i18nUtils
										.getProperty("modeler.activity.propertyPages.onAssignment.title"),
								"plugins/bpm-modeler/images/icons/assignment.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityOnAssignmentPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.initialize = function() {
					m_utils
							.jQuerySelect(
									"#onAssignmentPropertiesPage div.heading")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.onAssignment.title"));
					m_utils
							.jQuerySelect(
									"#onAssignmentPropertiesPage label[for='excludeUser']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.onAssignment.excludeUser"));
					m_utils
							.jQuerySelect(
									"#onAssignmentPropertiesPage label[for='exclUserData']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.onAssignment.data"));
					m_utils
							.jQuerySelect(
									"#onAssignmentPropertiesPage label[for='exclUserDataPath']")
							.text(
									m_i18nUtils
											.getProperty("modeler.activity.propertyPages.onAssignment.path"));

					this.exclUserData = this.mapInputId("exclUserData");
					this.exclUserDataPath = this.mapInputId("exclUserDataPath");

					this.exclUserData.append("<option value=\"TO_BE_DEFINED\">"
							+ m_i18nUtils
									.getProperty("modeler.general.toBeDefined")
							+ "</option>");

					var modelname = m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.thisModel");

					this.exclUserData.append("<optgroup label=\"" + modelname
							+ "\">");

					for ( var i in this.getModel().dataItems) {
						var dataItem = this.getModel().dataItems[i];
						// Show only data items from this model and not
						// external references.
						if (!dataItem.externalReference) {
							this.exclUserData.append("<option value='"
									+ dataItem.getFullId() + "'>"
									+ dataItem.name + "</option>");
						}
					}

					this.registerInputForElementChangeSubmission(
							this.exclUserData, "exclUserData");
					this.registerInputForElementChangeSubmission(
							this.exclUserDataPath, "exclUserDataPath");
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.getModelElement = function() {
					return this.propertiesPanel.element.modelElement;
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(
						attribute, value) {
					var element = {
						attributes : {}
					};

					element.attributes[attribute] = value;

					return element;
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.setElement = function() {
					if (this.getModelElement()["excludeUser"]
							&& this.getModelElement()["excludeUser"]["excludedPerformerData"]) {
						this.exclUserData
								.val(this.getModelElement()["excludeUser"]["excludedPerformerData"]);
						this.exclUserDataPath
								.val(this.getModelElement()["excludeUser"]["excludedPerformerDataPath"]);
					} else {
						this.exclUserData.val("TO_BE_DEFINED");
						this.exclUserDataPath.val("");
					}
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.validate = function() {
					this.propertiesPanel.clearErrorMessages();

					return true;
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.getElementUuid = function() {
					return this.propertiesPanel.element.modelElement.oid;
				};

				/**
				 * 
				 */
				ActivityOnAssignmentPropertiesPage.prototype.submitChanges = function(
						changes) {
					m_utils.debug("Changes to be submitted for UUID "
							+ this.getElementUuid() + ":");

					if (this.exclUserData.val() == "TO_BE_DEFINED") {
						changes = {
							excludeUser : null
						};
					} else {
						changes = {
							excludeUser : {
								excludedPerformerData : this.exclUserData.val(),
								excludedPerformerDataPath : this.exclUserDataPath
										.val()
							}
						};
					}

					m_utils.debug(changes);
					m_commandsController.submitCommand(m_command
							.createUpdateModelElementCommand(
									this.getModel().id, this.getElementUuid(),
									changes));
				};
			}
		});