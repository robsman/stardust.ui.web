/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command",
		"bpm-modeler/js/m_propertiesPage","bpm-modeler/js/m_i18nUtils"], function(m_utils, m_constants,
		m_commandsController, m_command, m_propertiesPage,m_i18nUtils) {
	return {
		create : function(propertiesPanel) {
			var page = new ActivityDisplayPropertiesPage(propertiesPanel);

			page.initialize();

			return page;
		}
	};

	function ActivityDisplayPropertiesPage(propertiesPanel) {
		var propertiesPage = m_propertiesPage.createPropertiesPage(
				propertiesPanel, "displayPropertiesPage", m_i18nUtils.getProperty("modeler.propertyPages.commonProperties.display"),
				"../../images/icons/display-properties-page.png");

		m_utils.inheritFields(this, propertiesPage);
		m_utils.inheritMethods(ActivityDisplayPropertiesPage.prototype,
				propertiesPage);

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.initialize = function() {
			this.auxiliaryActivityInput = this
					.mapInputId("auxiliaryActivityInput");

			this.registerCheckboxInputForModelElementAttributeChangeSubmission(
					this.auxiliaryActivityInput, "isAuxiliaryActivity");
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.getModelElement = function() {
			return this.propertiesPanel.element.modelElement;
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.assembleChangedObjectFromProperty = function(property, value) {
			var element = {};

			element[property] = value;

			return element;
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(attribute, value) {
			var element = { attributes: {}};

			element.attributes[attribute] = value;

			return element;
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.setElement = function() {
			if (this.getModelElement().attributes
					&& (true == this.getModelElement().attributes["isAuxiliaryActivity"])) {
				this.auxiliaryActivityInput.attr("checked", true);
			} else {
				this.auxiliaryActivityInput.attr("checked", false);
			}
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.validate = function() {
			this.propertiesPanel.clearErrorMessages();

			return true;
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.getElementUuid = function() {
			return this.propertiesPanel.element.modelElement.oid;
		};

		/**
		 *
		 */
		ActivityDisplayPropertiesPage.prototype.submitChanges = function(changes) {
			m_utils.debug("Changes to be submitted for UUID "
					+ this.getElementUuid() + ":");
			m_utils.debug(changes);
			m_commandsController.submitCommand(m_command
					.createUpdateModelElementCommand(
							this.getModel().id, this.getElementUuid(),
							changes));
		};
	}
});