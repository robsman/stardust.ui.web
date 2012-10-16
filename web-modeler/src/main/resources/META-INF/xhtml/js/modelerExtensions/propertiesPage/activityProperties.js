define([ 'm_activityBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage',
         'm_activityProcessingPropertiesPage',
         'm_activityControllingPropertiesPage',
         'm_activityQualityControlPropertiesPage',
         'm_activityDisplayPropertiesPage'], function(
        		 m_activityBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage,
        		 m_activityProcessingPropertiesPage,
        		 m_activityControllingPropertiesPage,
        		 m_activityQualityControlPropertiesPage,
        		 m_activityDisplayPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "activityPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_activityBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "commentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "processingPropertiesPage",
			provider: m_activityProcessingPropertiesPage,
			visibility: "preview"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "controllingPropertiesPage",
			pageHtmlUrl: "activityControllingPropertiesPage.html",
			provider: m_activityControllingPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "qualityControlPropertiesPage",
			provider: m_activityQualityControlPropertiesPage,
			visibility: "preview"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "displayPropertiesPage",
			provider: m_activityDisplayPropertiesPage,
			visibility: "always"
		}, ]
	};
});