var pageInitializer = function() {
	require("m_roleView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};