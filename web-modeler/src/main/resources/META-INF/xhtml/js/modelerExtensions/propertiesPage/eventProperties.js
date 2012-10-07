define([ 'm_eventBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage'], function(
        		 m_eventBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "eventPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_eventBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "eventPropertiesPanel",
			pageId: "eventAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
		}, ],
	};
});