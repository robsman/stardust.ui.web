/**
 * @author Shrikant.Gangal
 */
define([ "m_urlUtils" ], function(m_urlUtils) {
	var modelerI18N;
	return {
		getProperty : function(key, defaultVal) {
			if (!modelerI18N) {
				try {
					initModelerI18N();
				} catch (e) {
					return defaultVal;
				}
			}

			return modelerI18N.getProperty(key, defaultVal);
		}
	};

	function initModelerI18N() {
		var lang = "en";

		//TODO - Currently using service written for TIFF viewer in
		//graphics-common. Will need to move to some generic service.
		jQuery.ajax({
			url : require('m_urlUtils').getContextName()
					+ "/services/rest/documents/DUMMU_DOC_ID/pages/0/"
					+ new Date().getTime() + "/language",
			async : false,
			success : function(l) {
				lang = l;
			}
		});
		InfinityBPMI18N.initPluginProps({
			pluginName : "modeler",
			singleEndPoint : require('m_urlUtils').getContextName()
					+ "/services/rest/properties/bpm-modeler-client-messages/"
					+ lang
		});

		modelerI18N = InfinityBPMI18N.modeler;
	}
});