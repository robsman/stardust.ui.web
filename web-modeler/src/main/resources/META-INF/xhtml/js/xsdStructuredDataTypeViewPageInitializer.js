var pageInitializer = function() {
	require("m_xsdStructuredDataTypeView").initialize(
			jQuery.url.setUrl(window.location.search).param("fullId"));
};