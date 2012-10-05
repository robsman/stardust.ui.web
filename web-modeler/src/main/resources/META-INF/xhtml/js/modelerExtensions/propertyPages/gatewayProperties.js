define([ 'm_gatewayBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_gatewayBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "gatewayPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_gatewayBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "gatewayPropertiesPanel",
			pageId: "gatewayAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});