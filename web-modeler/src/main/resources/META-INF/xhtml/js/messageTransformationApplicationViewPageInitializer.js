var pageInitializer = function() {
	require("m_messageTransformationApplicationView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};