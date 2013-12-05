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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController", "bpm-modeler/js/m_model",
				"bpm-modeler/js/m_propertiesPanel", "bpm-modeler/js/m_propertiesPage" ],
		function(m_utils, m_constants, m_commandsController, m_model,
				m_propertiesPanel, m_propertiesPage) {

			return {
				initialize : function(diagram, process) {
					var processPropertiesPanel = new ProcessPropertiesPanel();
					m_commandsController
							.registerCommandHandler(processPropertiesPanel);
					processPropertiesPanel.initialize(diagram);
					return processPropertiesPanel;
				}
			};

			/**
			 * 
			 */
			function ProcessPropertiesPanel() {
				// Inheritance

				var propertiesPanel = m_propertiesPanel
						.createPropertiesPanel("processPropertiesPanel");

				m_utils.inheritFields(this, propertiesPanel);
				m_utils.inheritMethods(ProcessPropertiesPanel.prototype,
						propertiesPanel);

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.toString = function() {
					return "Lightdust.ProcessPropertiesPanel";
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.setElement = function(element) {
					this.clearErrorMessages();

					this.element = element;

					for ( var n in this.propertiesPages) {
						this.propertiesPages[n].setElement();
					}
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.getModelElement = function() {
					// TODO More elegantly?
					return this.diagram.process;
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.wrapModelElementProperties = function(
						modelElementProperties) {
					return modelElementProperties;
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.assembleChangedObjectFromProperty = function(
						property, value) {
					var element = {};

					element[property] = value;

					return element;
				};

				/**
				 * 
				 */
				ProcessPropertiesPanel.prototype.assembleChangedObjectFromAttribute = function(
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
				ProcessPropertiesPanel.prototype.getElementUuid = function() {
					return this.element.oid;
				};
			}
		});