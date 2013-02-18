/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_dataFlowBasicPropertiesPage',
		'bpm-modeler/js/m_modelElementCommentsPropertiesPage' ], function(
		m_dataFlowBasicPropertiesPage, m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId : "dataFlowPropertiesPanel",
			id : "basicPropertiesPage",
			provider : m_dataFlowBasicPropertiesPage,
			visibility : "always"
		}
//		, {
//			panelId : "dataFlowPropertiesPanel",
//			id : "dataFlowgatewayAnnotationsPropertiesPage",
//			pageHtmlUrl : "modelElementCommentsPropertiesPage.html",
//			provider : m_modelElementCommentsPropertiesPage,
//			visibility : "always"
//		}
		]
	};
});