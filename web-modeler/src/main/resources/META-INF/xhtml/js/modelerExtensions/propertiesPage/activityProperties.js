/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_constants',
		'bpm-modeler/js/m_activityBasicPropertiesPage',
		'bpm-modeler/js/m_activityImplementationPropertiesPage',
		'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
		'bpm-modeler/js/m_activityProcessingPropertiesPage',
		'bpm-modeler/js/m_activityControllingPropertiesPage',
		'bpm-modeler/js/m_activityQualityControlPropertiesPage',
		'bpm-modeler/js/m_activityDisplayPropertiesPage' ],
		function(m_constants, m_activityBasicPropertiesPage,
				m_activityImplementationPropertiesPage,
				m_modelElementCommentsPropertiesPage,
				m_activityProcessingPropertiesPage,
				m_activityControllingPropertiesPage,
				m_activityQualityControlPropertiesPage,
				m_activityDisplayPropertiesPage) {
			return {
				propertiesPage : [ {
					panelId : "activityPropertiesPanel",
					id : "basicPropertiesPage",
					provider : m_activityBasicPropertiesPage,
					visibility : "always"
				}, {
					panelId : "activityPropertiesPanel",
					id : "implementationPropertiesPage",
					provider : m_activityImplementationPropertiesPage,
					profiles : [ m_constants.INTEGRATOR_ROLE ],
					visibility : "always"
				}, {
					panelId : "activityPropertiesPanel",
					id : "commentsPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
					provider : m_modelElementCommentsPropertiesPage,
					visibility : "always"
				}, {
					panelId : "activityPropertiesPanel",
					id : "processingPropertiesPage",
					provider : m_activityProcessingPropertiesPage,
					visibility : "preview"
				}, {
					panelId : "activityPropertiesPanel",
					id : "controllingPropertiesPage",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/activityControllingPropertiesPage.html",
					provider : m_activityControllingPropertiesPage,
					visibility : "always"
				}, {
					panelId : "activityPropertiesPanel",
					id : "qualityControlPropertiesPage",
					provider : m_activityQualityControlPropertiesPage,
					visibility : "preview"
				}, {
					panelId : "activityPropertiesPanel",
					id : "displayPropertiesPage",
					provider : m_activityDisplayPropertiesPage,
					visibility : "always"
				}, ]
			};
		});