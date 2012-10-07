define([ 'm_swimlaneBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage'], function(
        		 m_swimlaneBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "swimlanePropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_swimlaneBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "swimlanePropertiesPanel",
			pageId: "swimlaneAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
		}, ],
	};
});