/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_commandsController", "m_command", "m_extensionManager", "m_model",
		"m_propertiesPanel", "m_propertiesPage" ], function(m_utils,
		m_constants, m_commandsController, m_command, m_extensionManager, m_model, m_propertiesPanel,
		m_propertiesPage) {

	var dataPropertiesPanel = null;

	return {
		initialize : function(diagram) {
			dataPropertiesPanel = new DataPropertiesPanel();

			m_commandsController.registerCommandHandler(dataPropertiesPanel);

			dataPropertiesPanel.initialize(diagram);
		},
		getInstance : function(element) {
			return dataPropertiesPanel;
		}
	};

	/**
	 *
	 */
	function DataPropertiesPanel() {
		// Inheritance

		var propertiesPanel = m_propertiesPanel
				.createPropertiesPanel("dataPropertiesPanel");

		m_utils.inheritFields(this, propertiesPanel);
		m_utils.inheritMethods(DataPropertiesPanel.prototype, propertiesPanel);

		this.viewLink = jQuery("#dataPropertiesPanel #viewLink");

		this.data = null;

		var viewManagerExtension = m_extensionManager
				.findExtension("viewManager");

		this.viewManager = viewManagerExtension.provider.create();

		this.viewLink.click({
			panel : this
		}, function(event) {
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
		DataPropertiesPanel.prototype.getElementUuid = function() {
			return this.data.uuid;
		};

		/**
		 *
		 */
		DataPropertiesPanel.prototype.setElement = function(element) {
			this.clearErrorMessages();

			this.element = element;
			// dataFullId doesn't get updated on rename, using
			// modelElement.getFullId() to find data
			if (this.element.modelElement != null) {
				this.data = m_model.findData(this.element.modelElement
						.getFullId());
			} else {
				this.data = m_model.findData(this.element.dataFullId);
			}

			if (this.element.properties == null) {
				this.element.properties = {};
			}

			for ( var n in this.propertiesPages) {
				this.propertiesPages[n].setElement();
			}
		};

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.getModelElement = function() {
			return this.data;
		};

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.assembleChangedObjectFromProperty = function(
				property, value) {
			var element = {};

			element[property] = value;

			return element;
		};

		/**
		 * 
		 */
		DataPropertiesPanel.prototype.assembleChangedObjectFromAttribute = function(
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
		DataPropertiesPanel.prototype.submitChanges = function(changes) {
			m_utils.debug("Changes to be submitted for UUID " + this.getElementUuid() + ":");
			m_utils.debug(changes);
			m_commandsController.submitCommand(m_command
					.createUpdateModelElementWithUUIDCommand(
							this.getDiagram().modelId,
							this.getElementUuid(),
							changes));
		};

	}
});