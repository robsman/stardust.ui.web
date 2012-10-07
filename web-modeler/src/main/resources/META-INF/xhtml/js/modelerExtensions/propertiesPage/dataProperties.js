define([ 'm_dataBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage'], function(
        		 m_dataBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_dataBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "dataPropertiesPanel",
			pageId: "dataAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
		}, ],
	};
});