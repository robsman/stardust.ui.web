define([ 'bpm-modeler/js/m_swimlaneBasicPropertiesPage',
         'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
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
			pageId: "swimlaneCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ],
	};
});