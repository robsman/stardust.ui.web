define([ 'm_activityBasicPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage',
         'm_activityProcessingPropertiesPage',
         'm_activityControllingPropertiesPage',
         'm_activityQualityControlPropertiesPage',
         'm_activityServiceParametersPropertiesPage'], function(
        		 m_activityBasicPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage,
        		 m_activityProcessingPropertiesPage,
        		 m_activityControllingPropertiesPage,
        		 m_activityQualityControlPropertiesPage,
        		 m_activityServiceParametersPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "activityPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_activityBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "activityAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "processingPropertiesPage",
			provider: m_activityProcessingPropertiesPage,
			visibility: "preview",
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "controllingPropertiesPage",
			pageHtmlUrl: "activityControllingPropertiesPage.html",
			provider: m_activityControllingPropertiesPage,
			visibility: "always",
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "qualityControlPropertiesPage",
			provider: m_activityQualityControlPropertiesPage,
			visibility: "preview",
		}, {
			panelId: "activityPropertiesPanel",
			pageId: "serviceParametersPropertiesPage",
			pageHtmlUrl: "activityServiceParametersPropertiesPage.html",
			provider: m_activityServiceParametersPropertiesPage,
			visibility: "always",
		}, ],
	};
});