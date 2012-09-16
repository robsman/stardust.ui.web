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
 * @author Marc.Gille
 */
define([ "m_utils", "m_constants", "m_commandsController", "m_propertiesPanel",
		"m_propertiesPage" ], function(m_utils, m_constants,
		m_commandsController, m_propertiesPanel, m_propertiesPage) {

	var swimlanePropertiesPanel = null;

	return {
		initialize : function(diagram) {
			swimlanePropertiesPanel = new SwimlanePropertiesPanel();

			m_commandsController
					.registerCommandHandler(swimlanePropertiesPanel);

			swimlanePropertiesPanel.initialize(diagram);
		},
		getInstance : function() {
			return swimlanePropertiesPanel;
		}
	};

	/**
	 * 
	 */
	function SwimlanePropertiesPanel() {
		var propertiesPanel = m_propertiesPanel
				.createPropertiesPanel("swimlanePropertiesPanel");

		m_utils.inheritFields(this, propertiesPanel);
		m_utils.inheritMethods(SwimlanePropertiesPanel.prototype,
				propertiesPanel);

		/**
		 * 
		 */
		SwimlanePropertiesPanel.prototype.toString = function() {
			return "Lightdust.SwimlanePropertiesPanel";
		};

		/**
		 * 
		 */
		SwimlanePropertiesPanel.prototype.getModelElement = function() {
			return this.element;
		};

		/**
		 * 
		 */
		SwimlanePropertiesPanel.prototype.setElement = function(element) {
			this.clearErrorMessages();

			this.element = element;

			if (this.element.properties == null) {
				this.element.properties = {};
			}

			for ( var n in this.propertiesPages) {
				this.propertiesPages[n].setElement();
			}
		};
	}
});