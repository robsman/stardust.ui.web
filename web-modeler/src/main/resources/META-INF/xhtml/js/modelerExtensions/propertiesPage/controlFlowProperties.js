define([ 'm_controlFlowBasicPropertiesPage',
         'm_controlFlowTransactionPropertiesPage',
         'm_modelElementCommentsPropertiesPage'], function(
        		 m_controlFlowBasicPropertiesPage,
        		 m_controlFlowTransactionPropertiesPage,
        		 m_modelElementCommentsPropertiesPage) {
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
			pageId: "controlFlowCommentsPropertiesPage",
			pageHtmlUrl: "modelElementCommentsPropertiesPage.html",
			provider: m_modelElementCommentsPropertiesPage,
			visibility: "always"
		}, ],
	};
});