/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

define(
		[ "m_utils", "m_constants", "m_model", "m_propertiesPanel", "m_propertiesPage",
				"m_dataBasicPropertiesPage" ],
		function(m_utils, m_constants, m_model, m_propertiesPanel, m_propertiesPage,
				m_dataBasicPropertiesPage) {

			var dataPropertiesPanel = null;

			return {
				initialize : function(models) {
					dataPropertiesPanel = new DataPropertiesPanel(
							models);
					
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
				m_utils.inheritMethods(DataPropertiesPanel.prototype,
						propertiesPanel);

				this.models = models;
				this.data = null;
				this.propertiesPages = [
						m_dataBasicPropertiesPage
								.createPropertiesPage(this)];

				/**
				 * 
				 */
				DataPropertiesPanel.prototype.toString = function() {
					return "Lightdust.DataPropertiesPanel()";
				};

				/**
				 * 
				 */
				DataPropertiesPanel.prototype.setElement = function(
						element) {
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

				/**
				 * 
				 */
				DataPropertiesPanel.prototype.apply = function() {
					this.applyPropertiesPages();
					this.element.refresh();
					this.element.submitUpdate();					
				};
			}
		});