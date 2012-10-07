define([ 'm_modelElementCommentsPropertiesPage'], function(
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "roleView",
			pageId: "roleCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
			pageName: "Comments",
			pageIconUrl: "../../images/icons/comments-properties-page.png"
		} ]
	};
});