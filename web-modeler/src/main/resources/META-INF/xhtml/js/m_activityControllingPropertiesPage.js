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
		[ "m_utils", "m_constants", "m_propertiesPage" ],
		function(m_utils, m_constants, m_propertiesPage) {
			return {
				create: function(propertiesPanel) {
					return new ActivityControllingPropertiesPage(propertiesPanel);
				}
			};

			/**
			 * 
			 */
			function ActivityControllingPropertiesPage(propertiesPanel) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, "controllingPropertiesPage", "Controlling",  "../../images/icons/controlling-properties-page.png");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(ActivityControllingPropertiesPage.prototype,
						propertiesPage);

				// Field initialization
				
				this.targetCostPerExecutionInput = jQuery("#"
						+ this.propertiesPanel.id + " #" + this.id
						+ " #targetCostPerExecutionInput");

				/**
				 * 
				 */
				ActivityControllingPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.element.properties.cost == null)
					{
					this.propertiesPanel.element.properties.cost = {};
					}

					this.targetCostPerExecutionInput
							.val(this.propertiesPanel.element.properties.cost.targetCostPerExecution);
				};
			}
		});