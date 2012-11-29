/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_controlFlowBasicPropertiesPage',
         'bpm-modeler/js/m_controlFlowTransactionPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_controlFlowBasicPropertiesPage,
        		 m_controlFlowTransactionPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "controlFlowPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_controlFlowBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "controlFlowPropertiesPanel",
			pageId: "transactionPropertiesPage",
			pageHtmlUrl: "controlFlowTransactionPropertiesPage.html",
			provider: m_controlFlowTransactionPropertiesPage,
			visibility: "always",
		}, {
			panelId: "controlFlowPropertiesPanel",
			pageId: "controlFlowCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ],
	};
});