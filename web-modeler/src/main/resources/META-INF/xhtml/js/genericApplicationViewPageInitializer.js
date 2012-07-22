var pageInitializer = function() {
	require("m_genericApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};