define([ 'm_controlFlowBasicPropertiesPage',
         'm_controlFlowTransactionPropertiesPage',
         'm_modelElementAnnotationsPropertiesPage'], function(
        		 m_controlFlowBasicPropertiesPage,
        		 m_controlFlowTransactionPropertiesPage,
        		 m_modelElementAnnotationsPropertiesPage) {
	return {
		propertiesPage : [ {
			panelId: "controlFlowPropertiesPanel",
			pageId: "basicPropertiesPage",
			provider: m_controlFlowBasicPropertiesPage,
			visibility: "always",
		}, {
			panelId: "controlFlowPropertiesPanel",
			pageId: "transactionPropertiesPage",
			pageHtmlUrl: "controlFlowTransactionPropertiesPage.html",
			provider: m_controlFlowTransactionPropertiesPage,
			visibility: "always",
		}, {
			panelId: "controlFlowPropertiesPanel",
			pageId: "controlFlowAnnotationsPropertiesPage",
			pageHtmlUrl: "modelElementAnnotationsPropertiesPage.html",
			provider: m_modelElementAnnotationsPropertiesPage,
			visibility: "always",
		}, ],
	};
});