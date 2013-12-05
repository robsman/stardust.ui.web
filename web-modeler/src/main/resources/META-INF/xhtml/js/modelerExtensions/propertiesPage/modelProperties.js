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
		[ 'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
				"bpm-modeler/js/m_i18nUtils",
				'bpm-modeler/js/m_modelConfigurationVariablesPropertiesPage', 'bpm-modeler/js/m_modelReadOnlyPropertiesPage' ],
		function(m_modelElementCommentsPropertiesPage, m_i18nUtils,
				m_modelConfigurationVariablesPropertiesPage, m_modelReadOnlyPropertiesPage) {
			return {
				propertiesPage : [
						{
							panelId : "modelView",
							id : "modelCommentsPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
							provider : m_modelElementCommentsPropertiesPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.element.properties.commonProperties.comments"),
							pageIconUrl : "plugins/bpm-modeler/images/icons/comments.png"
						},
						{
							panelId : "modelView",
							id : "configurationVariablesPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelConfigurationVariablesPropertiesPage.html",
							provider : m_modelConfigurationVariablesPropertiesPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.propertyView.modelView.configurationVariables.title"),
							pageIconUrl : "plugins/bpm-modeler/images/icons/table.png"
						},
						{
							panelId : "modelView",
							id : "modelReadOnlyPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelReadOnlyPropertiesPage.html",
							provider : m_modelReadOnlyPropertiesPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.propertyView.modelView.readOnlyPage.title"),
							pageIconUrl : "plugins/bpm-modeler/images/icons/key--pencil.png"
						}]
			};
		});