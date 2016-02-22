/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage", "bpm-modeler/js/m_i18nUtils" ],
		function(m_utils, m_constants, m_commandsController,
				m_command, m_propertiesPage, m_i18nUtils) {
			return {
				create : function(propertiesPanel) {
					return new DataViewAuthorizationPage(
							propertiesPanel);
				}
			};
			
			/**
			 * 
			 */
			function DataViewAuthorizationPage(propertiesPanel) {
				var propertiesPage = m_propertiesPage.createPropertiesPage(
						propertiesPanel, 'dataViewAuthorizationPage', m_i18nUtils.getProperty("modeler.common.authorization"));

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						DataViewAuthorizationPage.prototype,
						propertiesPage);
				
				/**
				 * 
				 */
				DataViewAuthorizationPage.prototype.setElement = function() {
					
				};
			}
			
		});