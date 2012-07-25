/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_extensionManager", "m_model",
		"m_propertiesPanel", "m_propertiesPage" ], function(m_utils,
		m_constants, m_extensionManager, m_model, m_propertiesPanel,
		m_propertiesPage) {

	var dataPropertiesPanel = null;

	return {
		initialize : function(models) {
			dataPropertiesPanel = new DataPropertiesPanel(models);

			dataPropertiesPanel.initialize();
		},
		getInstance : function(element) {
			return dataPropertiesPanel;
		}
	};

	/**
	 * 
	 */
	function DataPropertiesPanel(models) {
		// Inheritance

		var propertiesPanel = m_propertiesPanel
				.createPropertiesPanel("dataPropertiesPanel");

		m_utils.inheritFields(this, propertiesPanel);
		m_utils.inheritMethods(DataPropertiesPanel.prototype, propertiesPanel);

		this.viewLink = jQuery("#dataPropertiesPanel #viewLink");

		this.models = models;
		this.data = null;

		var viewManagerExtension = m_extensionManager
				.findExtension("viewManager");

		this.viewManager = require(viewManagerExtension.moduleUrl).create();

		this.viewLink.click({
			panel : this
		}, function(event) {
			m_utils.debug("Click");
			event.data.panel.openView();
		});

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.toString = function() {
			return "Lightdust.DataPropertiesPanel";
		};

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.openView = function() {
			m_utils.debug("Open View");
			this.viewManager
					.openView("dataView", "dataId=" + this.data.id
							+ "&modelId=" + "notsetyet" + "&dataName="
							+ this.data.name + "&fullId="
							+ this.data.getFullId(), this.data.getFullId());
		};

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.setElement = function(element) {
			this.clearErrorMessages();

			this.element = element;
			this.data = m_model.findData(this.element.dataFullId);

			if (this.element.properties == null) {
				this.element.properties = {};
			}

			for ( var n in this.propertiesPages) {
				this.propertiesPages[n].setElement();
			}
		};
	}
});