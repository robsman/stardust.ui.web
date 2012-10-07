define([ 'm_dataBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage'], function(
        		 m_dataBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "dataView",
			pageId: "dataCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ]
	};
});