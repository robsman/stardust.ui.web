/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_activityBasicPropertiesPage',
         'bpm-modeler/js/m_activityImplementationPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
         'bpm-modeler/js/m_activityProcessingPropertiesPage',
         'bpm-modeler/js/m_activityControllingPropertiesPage',
         'bpm-modeler/js/m_activityQualityControlPropertiesPage',
         'bpm-modeler/js/m_activityDisplayPropertiesPage'], function(
        		 m_activityBasicPropertiesPage,
        		 m_activityImplementationPropertiesPage,
        		 m_modelElementCommentsPropertiesPage,
        		 m_activityProcessingPropertiesPage,
        		 m_activityControllingPropertiesPage,
        		 m_activityQualityControlPropertiesPage,
        		 m_activityDisplayPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "activityPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_activityBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "implementationPropertiesPage",
			provider: m_activityImplementationPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "processingPropertiesPage",
			provider: m_activityProcessingPropertiesPage,
			visibility: "preview"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "controllingPropertiesPage",
			pageHtmlUrl: "activityControllingPropertiesPage.html",
			provider: m_activityControllingPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "qualityControlPropertiesPage",
			provider: m_activityQualityControlPropertiesPage,
			visibility: "preview"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "displayPropertiesPage",
			provider: m_activityDisplayPropertiesPage,
			visibility: "always"
		}, ]
	};
});