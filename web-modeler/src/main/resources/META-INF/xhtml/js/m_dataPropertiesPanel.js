/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_command", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_model",
		"bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_propertiesPage" ], function(m_utils,
		m_constants, m_commandsController, m_command, m_extensionManager, m_model, m_propertiesPanel,
		m_propertiesPage) {

	return {
		initialize : function(diagram) {
			var dataPropertiesPanel = new DataPropertiesPanel();

			m_commandsController.registerCommandHandler(dataPropertiesPanel);

			dataPropertiesPanel.initialize(diagram);
			
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

		this.viewLink = m_utils.jQuerySelect("#dataPropertiesPanel #viewLink");

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

			var data = this.data;

			//If external data, then use the external data's identifiers to open view.
			if (this.data.externalReference) {
				data = m_model.findData(this.data.dataFullId);
			}

			if (data && data.model) {
				this.viewManager
				.openView(
						"dataView",
						"dataId="
								+ encodeURIComponent(data.id)
								+ "&modelId="
								+ encodeURIComponent(data.model.id)
								+ "&dataName="
								+ encodeURIComponent(data.name)
								+ "&fullId="
								+ encodeURIComponent(data
										.getFullId())
								+ "&uuid="
								+ data.uuid
								+ "&modelUUID="
								+ data.model.uuid,
								data.uuid);
			}
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
				this.data = m_model.findElementByUuid(this.element.modelElement.uuid);
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
			m_utils.debug("Changes to be submitted for UUID "
					+ this.getElementUuid() + ":");
			m_utils.debug(changes);
			m_commandsController.submitCommand(m_command
					.createUpdateModelElementWithUUIDCommand(this
							.getModel().id, this.getElementUuid(),
							changes));
		};
	}
});