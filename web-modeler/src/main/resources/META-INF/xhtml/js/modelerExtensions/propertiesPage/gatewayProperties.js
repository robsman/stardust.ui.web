define([ 'bpm-modeler/js/m_gatewayBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_gatewayBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "gatewayPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_gatewayBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "gatewayPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ],
	};
});