/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ 'bpm-modeler/js/m_dataBasicPropertiesPage',
		'bpm-modeler/js/m_modelElementCommentsPropertiesPage' ], function(
		m_dataBasicPropertiesPage, m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId : "dataPropertiesPanel",
			id : "basicPropertiesPage",
			provider : m_dataBasicPropertiesPage,
			visibility : "always"
		}, {
			panelId : "dataPropertiesPanel",
			id : "commentsPropertiesPage",
			pageHtmlUrl : "plugins/bpm-modeler/views/modeler/modelElementCommentsPropertiesPage.html",
			provider : m_modelElementCommentsPropertiesPage,
			visibility : "always"
		}, ]
	};
});