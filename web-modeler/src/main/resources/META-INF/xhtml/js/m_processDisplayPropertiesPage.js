/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_commandsController", "m_command",
		"m_propertiesPage" ], function(m_utils, m_constants,
		m_commandsController, m_command, m_propertiesPage) {
	return {
		create : function(propertiesPanel) {
			var page = new ProcessDisplayPropertiesPage(propertiesPanel);

			page.initialize();

			return page;
		}
	};

	function ProcessDisplayPropertiesPage(propertiesPanel) {
		var propertiesPage = m_propertiesPage.createPropertiesPage(
				propertiesPanel, "displayPropertiesPage", "Display",
				"../../images/icons/display-properties-page.png");

		m_utils.inheritFields(this, propertiesPage);
		m_utils.inheritMethods(ProcessDisplayPropertiesPage.prototype,
				propertiesPage);

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.initialize = function() {
			this.auxiliaryProcessInput = this
					.mapInputId("auxiliaryProcessInput");

			this.registerCheckboxInputForModelElementAttributeChangeSubmission(
					this.auxiliaryProcessInput, "isAuxiliaryProcess");
		};

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.getModelElement = function() {
			return this.propertiesPanel.element;
		};

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.assembleChangedObjectFromProperty = function(property, value) {
			var element = {};
			
			element[property] = value;
			
			return element;
		};

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.assembleChangedObjectFromAttribute = function(attribute, value) {
			var element = { attributes: {}};
			
			element.attributes[attribute] = value;
			
			return element;
		};

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.setElement = function() {
			this.auxiliaryProcessInput.attr("checked",
					this.getModelElement().attributes["isAuxiliaryProcess"]);
		};

		/**
		 * 
		 */
		ProcessDisplayPropertiesPage.prototype.validate = function() {
			this.propertiesPanel.clearErrorMessages();

			return true;
		};
	}
});