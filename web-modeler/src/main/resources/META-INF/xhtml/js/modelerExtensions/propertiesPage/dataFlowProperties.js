define([ 'bpm-modeler/js/m_dataFlowBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_dataFlowBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataFlowPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_dataFlowBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "dataFlowPropertiesPanel",
			pageId: "dataFlowgatewayAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
		}, ],
	};
});