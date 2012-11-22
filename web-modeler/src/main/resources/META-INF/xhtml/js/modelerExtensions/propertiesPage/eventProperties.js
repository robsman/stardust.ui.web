define([ 'bpm-modeler/js/m_eventBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_eventBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "eventPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_eventBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "eventPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ]
	};
});