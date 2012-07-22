var pageInitializer = function() {
	require("m_dataView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};