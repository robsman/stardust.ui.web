var pageInitializer = function() {
	require("m_uiMashupApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};