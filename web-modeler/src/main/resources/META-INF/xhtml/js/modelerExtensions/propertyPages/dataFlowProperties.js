define([ 'm_dataFlowBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_dataFlowBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataFlowPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_dataFlowBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "dataFlowPropertiesPanel",
			pageId: "dataFlowgatewayAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});