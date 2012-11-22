define([ 'bpm-modeler/js/m_modelElementCommentsPropertiesPage'], function(
        		 m_modelElementCommentsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "conditionalPerformerView",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
			pageName: "Comments",
			pageIconUrl: "../../images/icons/comments-properties-page.png"
		} ]
	};
});