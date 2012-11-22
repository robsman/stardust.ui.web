define([ 'bpm-modeler/js/m_dataBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_dataBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_dataBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "dataPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ],
	};
});