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
		[ 'bpm-modeler/js/m_modelElementCommentsPropertiesPage', 'bpm-modeler/js/m_propertiesPageProvider',
				"bpm-modeler/js/m_i18nUtils" ],
		function(m_modelElementCommentsPropertiesPage, m_propertiesPageProvider, m_i18nUtils) {
			return {
				propertiesPage : [ {
					panelId : "camelApplicationView",
					id : "applicationRetriesPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/applicationRetriesPropertiesPage.html",
					provider : m_propertiesPageProvider,
					visibility : "always",
					pageName : m_i18nUtils
							.getProperty("modeler.model.propertyView.webService.retries.header"),
					pageIconUrl : "plugins/bpm-modeler/images/icons/arrow-circle.png",
					html5: true
				}, {
					panelId : "camelApplicationView",
					id : "camelApplicationCommentsPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
					provider : m_modelElementCommentsPropertiesPage,
					visibility : "always",
					pageName : m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.comments"),
					pageIconUrl : "plugins/bpm-modeler/images/icons/comments.png"
				} ]
			};
		});