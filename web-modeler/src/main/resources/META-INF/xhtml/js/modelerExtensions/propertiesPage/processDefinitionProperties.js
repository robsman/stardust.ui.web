/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_processBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
         'bpm-modeler/js/m_processProcessInterfacePropertiesPage',
         'bpm-modeler/js/m_processDataPathPropertiesPage',
         'bpm-modeler/js/m_processDisplayPropertiesPage',
         'bpm-modeler/js/m_processProcessAttachmentsPropertiesPage'], function(
        		 m_processBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage,
        		 m_processProcessInterfacePropertiesPage,
        		 m_processDataPathPropertiesPage,
        		 m_processDisplayPropertiesPage,
        		 m_processProcessAttachmentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "processPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_processBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processInterfacePropertiesPage",
			provider: m_processProcessInterfacePropertiesPage,
			visibility: "always"
		}, {
			panelId: "processPropertiesPanel",
			pageId: "dataPathPropertiesPage",
			provider: m_processDataPathPropertiesPage,
			visibility: "always"
		}, {
			panelId: "processPropertiesPanel",
			pageId: "displayPropertiesPage",
			provider: m_processDisplayPropertiesPage,
			visibility: "always"
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processAttachmentsPropertiesPage",
			pageHtmlUrl: "processDefinitionProcessAttachmentsPropertiesPage.html",
			provider: m_processProcessAttachmentsPropertiesPage,
			visibility: "always"
	}, ]
	};
});