/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

define(
        ['bpm-modeler/js/m_constants',
            'bpm-modeler/js/m_activityBasicPropertiesPage',
            'bpm-modeler/js/m_activityImplementationPropertiesPage',
            'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
            'bpm-modeler/js/m_activityProcessingPropertiesPage',
            'bpm-modeler/js/m_activityControllingPropertiesPage',
            'bpm-modeler/js/QualityControlActivityPropertiesPage',
            'bpm-modeler/js/m_activityDisplayPropertiesPage',
            'bpm-modeler/js/m_onAssignmentPropertiesPage',
            'bpm-modeler/js/m_propertiesPageProvider',
            'bpm-modeler/js/m_i18nUtils'],
        function(m_constants, m_activityBasicPropertiesPage,
                m_activityImplementationPropertiesPage,
                m_modelElementCommentsPropertiesPage,
                m_activityProcessingPropertiesPage,
                m_activityControllingPropertiesPage,
                QualityControlActivityPropertiesPage,
                m_activityDisplayPropertiesPage, 
                m_onAssignmentPropertiesPage,
                m_propertiesPageProvider,
                m_i18nUtils) {
          return {
            propertiesPage: [
                {
                  panelId: "activityPropertiesPanel",
                  id: "basicPropertiesPage",
                  provider: m_activityBasicPropertiesPage,
                  visibility: "always"
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "implementationPropertiesPage",
                  provider: m_propertiesPageProvider,
                  profiles: [m_constants.INTEGRATOR_ROLE],
                  visibility: "always",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/activityImplementationPropertiesPage.html",
                  imageUrl: "plugins/bpm-modeler/images/icons/wrench.png",
                  title: m_i18nUtils.getProperty("modeler.propertiesPage.toolbar.implementation.title"),
                  html5: true
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "excludedUsersPage",
                  provider: m_propertiesPageProvider,
                  visibility: "always",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/excludedUsersPage.html",
                  imageUrl: "plugins/bpm-modeler/images/icons/user-invalidated.png",
                  title: m_i18nUtils.getProperty("modeler.propertiesPage.activity.excludedUsers.title"),
                  html5: true
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "qualityAssuranceCodesPage",
                  provider: m_propertiesPageProvider,
                  visibility: "always",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/activityQualityAssuranceCodesPage.html",
                  imageUrl: "plugins/bpm-modeler/images/icons/quality-assurance-code.png",
                  title: m_i18nUtils.getProperty("modeler.propertyView.modelView.qualityAssuranceCodes.title"),
                  html5: true
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "resubmissionPage",
                  provider: m_propertiesPageProvider,
                  visibility: "always",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/activityResubmissionPage.html",
                  imageUrl: "plugins/views-common/images/icons/activity_postponed.png",
                  title: m_i18nUtils.getProperty("modeler.activity.propertyPages.resubmission.title"),
                  html5: true
                },
				{
					panelId : "activityPropertiesPanel",
					id : "activityAuthorizationsPropertiesPage",
					provider : m_propertiesPageProvider,
					visibility : "always",
					pageHtmlUrl : "plugins/bpm-modeler/views/modeler/authorizationsPropertiesPage.html",
					imageUrl : "plugins/views-common/images/icons/key.png",
					title : m_i18nUtils
							.getProperty("modeler.common.authorization"),
					html5 : true
				},
                {
                  panelId: "activityPropertiesPanel",
                  id: "commentsPropertiesPage",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
                  provider: m_modelElementCommentsPropertiesPage,
                  visibility: "always"
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "processingPropertiesPage",
                  provider: m_activityProcessingPropertiesPage,
                  visibility: "always"
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "controllingPropertiesPage",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/activityControllingPropertiesPage.html",
                  provider: m_activityControllingPropertiesPage,
                  visibility: "always"
                },
                {
                  panelId: "activityPropertiesPanel",
                  id: "qualityControlActivityPropertiesPage",
                  pageHtmlUrl: "plugins/bpm-modeler/views/modeler/qualityControlActivityPropertiesPage.html",
                  provider: QualityControlActivityPropertiesPage,
                  visibility: "always"
                }, {
                  panelId: "activityPropertiesPanel",
                  id: "displayPropertiesPage",
                  provider: m_activityDisplayPropertiesPage,
                  visibility: "always"
                }, {
                  panelId: "activityPropertiesPanel",
                  id: "onAssignmentPropertiesPage",
                  provider: m_onAssignmentPropertiesPage,
                  visibility: "preview"
                }]
          };
        });