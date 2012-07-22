var pageInitializer = function() {
	require("m_modelerViewLayoutManager").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};