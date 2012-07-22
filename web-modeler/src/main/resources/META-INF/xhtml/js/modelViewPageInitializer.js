var pageInitializer = function() {
	require("m_modelView").initialize(
			jQuery.url.setUrl(window.location.search).param("modelId"));
};