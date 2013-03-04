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
				'bpm-modeler/js/UiMashupTestPropertiesPage',
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_modelElementCommentsPropertiesPage,
				UiMashupTestPropertiesPage, m_i18nUtils) {
			return {
				propertiesPage : [
						{
							panelId : "uiMashupApplicationView",
							id : "uiMashupTestPropertiesPage",
							pageHtmlUrl : "uiMashupTestPropertiesPage.html",
							provider : UiMashupTestPropertiesPage,
							visibility : "preview",
							pageName : m_i18nUtils
									.getProperty("modeler.model.propertyView.uiMashup.test.title"),
							pageIconUrl : "../../images/icons/test-properties-page.png"
						},
						{
							panelId : "uiMashupApplicationView",
							id : "uiMashupApplicationCommentsPropertiesPage",
							pageHtmlUrl : "modelElementCommentsPropertiesPage.html",
							provider : m_modelElementCommentsPropertiesPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.element.properties.commonProperties.comments"),
							pageIconUrl : "../../images/icons/comments-properties-page.png"
						} ]
			};
		});