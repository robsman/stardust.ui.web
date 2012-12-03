/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_eventBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage', 'bpm-modeler/js/m_eventImplementationPropertiesPage'], function(
        		 m_eventBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage, m_eventImplementationPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "eventPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_eventBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "eventPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, {
			panelId: "eventPropertiesPanel",
			pageId: "implementationPropertiesPage",
			pageHtmlUrl: "eventImplementationPropertiesPage.html",
			provider: m_eventImplementationPropertiesPage,
			visibility: "always"
		} ]
	};
});