var pageInitializer = function() {
	var fullId = jQuery.url.setUrl(window.location.search).param(
			"fullId");
	
	require("m_roleView").initialize(fullId);
};