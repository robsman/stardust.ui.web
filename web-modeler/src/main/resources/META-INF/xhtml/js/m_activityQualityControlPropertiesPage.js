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
		  "bpm-modeler/js/m_propertiesPage",
		  "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, 
				m_constants, 
				m_propertiesPage,
				m_i18nUtils) {
			
			return {
				create : function(propertiesPanel) {
					return new ActivityQualityControlPropertiesPage(
							propertiesPanel);
				}
			};

			function ActivityQualityControlPropertiesPage(newPropertiesPanel,
					newId, newTitle) {

				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel, "qualityControlPropertiesPage",
						m_i18nUtils.getProperty("modeler.propertiesPage.activity.qualityassurance.heading"));
				
				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						ActivityQualityControlPropertiesPage.prototype,
						propertiesPage);

				/*Internationalization*/
				m_utils.jQuerySelect("#qualityControlPropertiesPage > .heading")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.qualityassurance.heading"));
				m_utils.jQuerySelect("label[for='qualityControlProbabilityInput']")
					.text(m_i18nUtils.getProperty("modeler.propertiesPage.activity.qualityassurance.probability"));
				
				// Field initialization
				this.qualityControlProbabilityInput = this.mapInputId("qualityControlProbabilityInput");

				/**
				 *
				 */
				ActivityQualityControlPropertiesPage.prototype.setElement = function() {
					if (this.propertiesPanel.element.properties.qualityControl == null) {
						this.propertiesPanel.element.properties.qualityControl = {};
					}

					this.qualityControlProbabilityInput
							.val(this.propertiesPanel.element.properties.qualityControl.qualityControlProbability);
				};

				/**
				 *
				 */
				ActivityQualityControlPropertiesPage.prototype.apply = function() {
					this.propertiesPanel.properties.qualityControl.qualityControlProbability = this.qualityControlProbabilityInput
							.val();
				};
			}
		});