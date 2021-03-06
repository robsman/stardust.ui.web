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
		[ 'bpm-modeler/js/m_modelElementCommentsPropertiesPage', 'bpm-modeler/js/DataViewAuthorizationPage',
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_modelElementCommentsPropertiesPage, DataViewAuthorizationPage, m_i18nUtils) {
			return {
				propertiesPage : [
						{
							panelId : "dataView",
							id : "dataCommentsPropertiesPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
							provider : m_modelElementCommentsPropertiesPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.element.properties.commonProperties.comments"),
							pageIconUrl : "plugins/bpm-modeler/images/icons/comments.png"
						}, {
							panelId : "dataView",
							id : "dataViewAuthorizationPage",
							pageHtmlUrl : "plugins/bpm-modeler/views/modeler/dataViewAuthorizationPage.html",
							provider : DataViewAuthorizationPage,
							visibility : "always",
							pageName : m_i18nUtils
									.getProperty("modeler.common.authorization"),
							pageIconUrl : "plugins/views-common/images/icons/key.png"
						}, ]
			};
		});