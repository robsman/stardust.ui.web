define([ 'm_swimlaneBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_swimlaneBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "swimlanePropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_swimlaneBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "swimlanePropertiesPanel",
			pageId: "swimlaneAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});