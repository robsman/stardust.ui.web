define([ 'bpm-modeler/js/m_modelElementCommentsPropertiesPage', "bpm-modeler/js/m_i18nUtils"], function(
        		 m_modelElementCommentsPropertiesPage, m_i18nUtils) {
	return {
		propertiesPage : [ {
			panelId: "camelApplicationView",
			pageId: "camelApplicationCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always",
			pageName: m_i18nUtils.getProperty("modeler.element.properties.commonProperties.comments"),
			pageIconUrl: "../../images/icons/comments-properties-page.png"
		} ]
	};
});