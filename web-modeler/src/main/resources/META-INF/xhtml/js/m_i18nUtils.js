/**
 * @author Shrikant.Gangal
 */
define([ "bpm-modeler/js/m_urlUtils", "common/InfinityBPMI18N" ], function(m_urlUtils, InfinityBPMI18N) {
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
					url : m_urlUtils.getContextName()
							+ "/services/rest/bpm-modeler/modeler/"
							+ new Date().getTime() + "/language",
			async : false,
			success : function(l) {
				lang = l;
			}
		});
		InfinityBPMI18N.initPluginProps({
			pluginName : "modeler",
			singleEndPoint : m_urlUtils.getContextName()
					+ "/services/rest/bpm-modeler/modeler/" + new Date().getTime() + "/bpm-modeler-client-messages/"
					+ lang
		});

		modelerI18N = InfinityBPMI18N.modeler;
	}
});