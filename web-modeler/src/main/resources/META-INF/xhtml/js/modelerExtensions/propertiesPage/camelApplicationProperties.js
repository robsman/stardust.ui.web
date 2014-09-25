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
		  'bpm-modeler/js/m_camelApplicationAttachmentsPropertiesPage',
		  "bpm-modeler/js/m_i18nUtils" ],
		function(m_modelElementCommentsPropertiesPage, m_camelApplicationAttachmentsPropertiesPage, m_i18nUtils) {
			return {
				propertiesPage : [ {
					panelId : "camelApplicationView",
					id : "camelApplicationCommentsPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
					provider : m_modelElementCommentsPropertiesPage,
					visibility : "always",
					pageName : m_i18nUtils
							.getProperty("modeler.element.properties.commonProperties.comments"),
					pageIconUrl : "plugins/bpm-modeler/images/icons/comments.png"
				}, {
					panelId : "camelApplicationView",
					id : "camelApplicationAttachmentsPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/camelApplicationAttachmentsPropertiesPage.html",
					provider : m_camelApplicationAttachmentsPropertiesPage,
					visibility : "always",
					pageName : m_i18nUtils.getProperty("modeler.model.applicationOverlay.email.attachments.title"),
					pageIconUrl : "plugins/bpm-modeler/images/icons/data-folder.png"
				} ]
			};
		});