define([ 'm_activityBasicPropertiesPage',
         'm_modelElementCommentsPropertiesPage',
         'm_activityProcessingPropertiesPage',
         'm_activityControllingPropertiesPage',
         'm_activityQualityControlPropertiesPage'], function(
        		 m_activityBasicPropertiesPage,
        		 m_modelElementCommentsPropertiesPage,
        		 m_activityProcessingPropertiesPage,
        		 m_activityControllingPropertiesPage,
        		 m_activityQualityControlPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "activityPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_activityBasicPropertiesPage,
			visibility: "always"
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "activityCommentsPropertiesPage",
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
		}, ]
	};
});