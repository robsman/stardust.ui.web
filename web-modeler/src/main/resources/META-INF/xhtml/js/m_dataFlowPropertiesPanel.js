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
define(
		[ "bpm-modeler/js/m_utils", 
		  "bpm-modeler/js/m_constants", 
		  "bpm-modeler/js/m_commandsController", 
		  "bpm-modeler/js/m_propertiesPanel", 
		  "bpm-modeler/js/m_propertiesPage",
		  "bpm-modeler/js/m_i18nUtils"
				],
		function(m_utils, m_constants, m_commandsController, 
				m_propertiesPanel, m_propertiesPage, m_i18nUtils) {

			return {
				initialize : function(diagram) {
					var dataFlowPropertiesPanel = new DataFlowPropertiesPanel();

					m_commandsController.registerCommandHandler(dataFlowPropertiesPanel);					
					
					dataFlowPropertiesPanel.initialize(diagram);
					return dataFlowPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function DataFlowPropertiesPanel() {
				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("dataFlowPropertiesPanel", true);
				
				m_utils.inheritFields(this, propertiesPanel);
				
				m_utils.inheritMethods(DataFlowPropertiesPanel.prototype,
						propertiesPanel);
				
				/*Internationalization*/
				m_utils.jQuerySelect(".propertiesPanelTitle",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.title'));
				
				m_utils.jQuerySelect("label[for='nameInput']",propertiesPanel.panel[0])
				.text(m_i18nUtils.getProperty('modeler.element.properties.commonProperties.name'));
				
				m_utils.jQuerySelect("label[for='descriptionInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.element.properties.commonProperties.description'));
				
				m_utils.jQuerySelect("label[for='inputInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.input'));
				
				m_utils.jQuerySelect("label[for='outputInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.output'));
				
				m_utils.jQuerySelect("label[for='inputDataPathInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.inputDataPath'));
				
				m_utils.jQuerySelect("label[for='inputAccessPointSelectInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.inputAccessPoint'));
				
				m_utils.jQuerySelect("label[for='outputDataPathInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.outputDataPath'));
				
				m_utils.jQuerySelect("label[for='outputAccessPointSelectInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.outputAccessPoint'));
				
				m_utils.jQuerySelect("label[for='outputAccessPointPathInput']",propertiesPanel.panel[0])
					.text(m_i18nUtils.getProperty('modeler.dataFlow.propertiesPanel.label.outputAccessPointPathInput'));


				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.toString = function() {
					return "Lightdust.DataFlowPropertiesPanel";
				};

				/**
				 * 
				 */
				DataFlowPropertiesPanel.prototype.setElement = function(
						element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};
			}
		});