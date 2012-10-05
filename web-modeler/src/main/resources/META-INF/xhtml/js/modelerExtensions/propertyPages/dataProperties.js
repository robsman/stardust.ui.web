define([ 'm_dataBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_dataBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_dataBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "dataPropertiesPanel",
			pageId: "dataAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});