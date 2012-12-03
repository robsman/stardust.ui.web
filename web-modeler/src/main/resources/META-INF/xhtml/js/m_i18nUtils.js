/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Shrikant.Gangal
 */
define([ "bpm-modeler/js/m_urlUtils", "i18n" ], function(m_urlUtils,
		InfinityBPMI18N) {
	var modelerI18N;
	return {
		getProperty : function(key, defaultValue) {
			if (!modelerI18N) {
				try {
					initModelerI18N();
				} catch (e) {
					if (defaultValue) {
						return defaultValue;
					} else {
						return key;
					}
				}
			}

			var value = modelerI18N.getProperty(key, defaultValue);
			
			if (value) {
				return value;
			} else {
				return key;
			}
		}
	};

	function initModelerI18N() {
		var lang = "en";

		// TODO - Currently using service written for TIFF viewer in
		// graphics-common. Will need to move to some generic service.
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
					+ "/services/rest/bpm-modeler/modeler/"
					+ new Date().getTime() + "/bpm-modeler-client-messages/"
					+ lang
		});

		modelerI18N = InfinityBPMI18N.modeler;
	}
});