var pageInitializer = function() {
	require("m_camelApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};