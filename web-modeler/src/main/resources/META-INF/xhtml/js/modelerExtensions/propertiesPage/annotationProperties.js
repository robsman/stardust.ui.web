define([ 'm_annotationBasicPropertiesPage'], function(
        		 m_annotationBasicPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "annotationPropertiesPanel",
			pageId: "basicPropertiesPage",
			pageHtmlUrl: "annotationBasicPropertiesPage.html",
			provider: m_annotationBasicPropertiesPage,
			visibility: "always",
		}, ],
	};
});