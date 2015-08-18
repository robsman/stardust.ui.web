/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: SunGard
 * CSA LLC - initial API and implementation and/or initial documentation
 ******************************************************************************/

define(
        ['bpm-modeler/js/m_propertiesPageProvider',
            'bpm-modeler/js/m_modelElementCommentsPropertiesPage',
            'bpm-modeler/js/m_i18nUtils'],
        function(m_propertiesPageProvider,
                m_modelElementCommentsPropertiesPage, m_i18nUtils) {
          return {
            propertiesPage: [{
              panelId: "dataFlowPropertiesPanel",
              id: "dataFlowPropertiesPage",
              provider: m_propertiesPageProvider,
              visibility: "always",

              pageHtmlUrl: "plugins/bpm-modeler/views/modeler/dataFlowPropertiesPage.html",
              imageUrl: "plugins/bpm-modeler/images/icons/table.png",
              title: m_i18nUtils
                      .getProperty("modeler.dataFlow.propertiesPanel.title"),
              html5: true
            }
            // , {
            // panelId : "dataFlowPropertiesPanel",
            // id : "dataFlowgatewayAnnotationsPropertiesPage",
            // pageHtmlUrl : "modelElementCommentsPropertiesPage.html",
            // provider : m_modelElementCommentsPropertiesPage,
            // visibility : "always"
            // }
            ]
          };
        });