define(["m_manualTriggerIntegrationOverlay", "m_timerEventIntegrationOverlay", "m_fileEventIntegrationOverlay", "m_emailEventIntegrationOverlay", "m_messageEventIntegrationOverlay", "m_scanEventIntegrationOverlay", "m_genericCamelRouteIntegrationOverlay"], function(
		m_manualTriggerIntegrationOverlay, m_timerEventIntegrationOverlay, m_fileEventIntegrationOverlay, m_emailEventIntegrationOverlay, m_messageEventIntegrationOverlay, m_scanEventIntegrationOverlay, m_genericCamelRouteIntegrationOverlay) {
	return {
		applicationIntegrationOverlay: [],
		eventIntegrationOverlay: [{
			id : "manualTriggerIntegrationOverlay",
			name : "Manual Start by User",
			pageHtmlUrl: "manualTriggerIntegrationOverlay.html",
			provider : m_manualTriggerIntegrationOverlay
		},
		{
			id : "timerEventIntegrationOverlay",
			name : "Timer Event",
			pageHtmlUrl: "timerEventIntegrationOverlay.html",
			provider : m_timerEventIntegrationOverlay
		},
		{
			id : "fileEventIntegrationOverlay",
			name : "File Event",
			pageHtmlUrl: "fileEventIntegrationOverlay.html",
			provider : m_fileEventIntegrationOverlay
		},
		{
			id : "emailEventIntegrationOverlay",
			name : "EMail Event",
			pageHtmlUrl: "emailEventIntegrationOverlay.html",
			provider : m_emailEventIntegrationOverlay
		},
		{
			id : "messageEventIntegrationOverlay",
			name : "Message Event",
			pageHtmlUrl: "messageEventIntegrationOverlay.html",
			provider : m_messageEventIntegrationOverlay
		},
		{
			id : "scanEventIntegrationOverlay",
			name : "Scan Event",
			pageHtmlUrl: "scanEventIntegrationOverlay.html",
			provider : m_scanEventIntegrationOverlay
		},
		{
			id : "genericCamelRouteIntegrationOverlay",
			name : "Generic Camel Route Event",
			pageHtmlUrl: "genericCamelRouteIntegrationOverlay.html",
			provider : m_genericCamelRouteIntegrationOverlay
		}]
	};
});