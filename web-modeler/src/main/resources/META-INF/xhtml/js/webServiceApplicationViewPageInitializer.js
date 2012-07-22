var pageInitializer = function() {
	require("m_webServiceApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};