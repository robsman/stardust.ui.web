define([ 'm_eventBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_eventBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "eventPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_eventBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "eventPropertiesPanel",
			pageId: "eventAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});