define([ 'm_processBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage',
         'm_processProcessInterfacePropertiesPage',
         'm_processDataPathPropertiesPage',
         'm_processDisplayPropertiesPage',
         'm_processProcessAttachmentsPropertiesPage'], function(
        		 m_processBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage,
        		 m_processProcessInterfacePropertiesPage,
        		 m_processDataPathPropertiesPage,
        		 m_processDisplayPropertiesPage,
        		 m_processProcessAttachmentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "processPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_processBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processInterfacePropertiesPage",
			provider: m_processProcessInterfacePropertiesPage,
			visibility: "always",
		}, {
			panelId: "processPropertiesPanel",
			pageId: "dataPathPropertiesPage",
			provider: m_processDataPathPropertiesPage,
			visibility: "always",
		}, {
			panelId: "processPropertiesPanel",
			pageId: "displayPropertiesPage",
			provider: m_processDisplayPropertiesPage,
			visibility: "always",
		}, {
			panelId: "processPropertiesPanel",
			pageId: "processAttachmentsPropertiesPage",
			pageHtmlUrl: "processDefinitionProcessAttachmentsPropertiesPage.html",
			provider: m_processProcessAttachmentsPropertiesPage,
			visibility: "always",
	}, ],
	};
});