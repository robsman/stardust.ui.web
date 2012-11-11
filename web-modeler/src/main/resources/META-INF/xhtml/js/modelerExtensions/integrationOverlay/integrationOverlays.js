define(["m_manualTriggerIntegrationOverlay", "m_timerEventIntegrationOverlay", "m_fileEventIntegrationOverlay", "m_emailEventIntegrationOverlay", "m_messageEventIntegrationOverlay", "m_scanEventIntegrationOverlay", "m_genericCamelRouteEventIntegrationOverlay"], function(
		m_manualTriggerIntegrationOverlay, m_timerEventIntegrationOverlay, m_fileEventIntegrationOverlay, m_emailEventIntegrationOverlay, m_messageEventIntegrationOverlay, m_scanEventIntegrationOverlay, m_genericCamelRouteEventIntegrationOverlay) {
	return {
		applicationIntegrationOverlay: [],
		eventIntegrationOverlay: [{
			id : "manualTrigger",
			name : "Manual Start by User",
			pageHtmlUrl: "manualTriggerIntegrationOverlay.html",
			provider : m_manualTriggerIntegrationOverlay
		},
		{
			id : "timerEvent",
			name : "Timer Event",
			pageHtmlUrl: "timerEventIntegrationOverlay.html",
			provider : m_timerEventIntegrationOverlay
		},
		{
			id : "fileEvent",
			name : "File Event",
			pageHtmlUrl: "fileEventIntegrationOverlay.html",
			provider : m_fileEventIntegrationOverlay
		},
		{
			id : "emailEvent",
			name : "EMail Event",
			pageHtmlUrl: "emailEventIntegrationOverlay.html",
			provider : m_emailEventIntegrationOverlay
		},
		{
			id : "messageEvent",
			name : "Message Event",
			pageHtmlUrl: "messageEventIntegrationOverlay.html",
			provider : m_messageEventIntegrationOverlay
		},
		{
			id : "scanEvent",
			name : "Scan Event",
			pageHtmlUrl: "scanEventIntegrationOverlay.html",
			provider : m_scanEventIntegrationOverlay
		},
		{
			id : "genericCamelRouteEvent",
			name : "Generic Camel Route Event",
			pageHtmlUrl: "genericCamelRouteEventIntegrationOverlay.html",
			provider : m_genericCamelRouteEventIntegrationOverlay
		}]
	};
});