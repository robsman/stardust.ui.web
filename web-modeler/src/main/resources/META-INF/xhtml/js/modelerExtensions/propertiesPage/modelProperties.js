define([ 'm_modelElementCommentsPropertiesPage',"m_i18nUtils"], function(
        		 m_modelElementCommentsPropertiesPage,m_i18nUtils) {
	return {
		propertiesPage : [ {
			panelId: "modelView",
			pageId: "modelCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
			pageName: m_i18nUtils.getProperty("modeler.element.properties.commonProperties.comments") ,
			pageIconUrl: "../../images/icons/comments-properties-page.png"
		} ]
	};
});