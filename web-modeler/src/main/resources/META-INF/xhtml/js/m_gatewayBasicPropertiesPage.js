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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_command", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_basicPropertiesPage" ],
		function(m_utils, m_constants, m_command, m_commandsController,
				m_basicPropertiesPage) {
			return {
				create : function(propertiesPanel) {
					var page = new GatewayBasicPropertiesPage(propertiesPanel);
					
					page.initialize();
					
					return page;
				}
			};

			function GatewayBasicPropertiesPage(newPropertiesPanel, newId,
					newTitle) {
				var propertiesPage = m_basicPropertiesPage.create(
						newPropertiesPanel);

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(GatewayBasicPropertiesPage.prototype,
						propertiesPage);

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.initialize = function() {
					this.initializeBasicPropertiesPage();										

					this.gatewayTypeInput = this.mapInputId("gatewayTypeInput");

					this.registerInputForModelElementChangeSubmission(
							this.gatewayTypeInput, "gatewayType");
				};
				
				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.setElement = function() {
					this.setModelElement();
					
					m_utils.debug("===> Gateway");
					m_utils.debug(this.getModelElement());
					
					this.gatewayTypeInput
							.val(this.getModelElement().gatewayType);
				};

				/**
				 * 
				 */
				GatewayBasicPropertiesPage.prototype.validate = function() {
					// We allow empty names
					
					return true;
				};
			}
		});