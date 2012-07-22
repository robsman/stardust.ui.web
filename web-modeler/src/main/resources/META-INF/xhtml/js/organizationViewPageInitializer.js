var pageInitializer = function() {
	require("m_organizationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};