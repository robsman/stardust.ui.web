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
define([ "i18n" ], function(InfinityBPMI18N) {
	var modelerI18N;
	return {
		getProperty : function(key, defaultValue) {
			var value = defaultValue;
			if(!(typeof message_bundle === 'undefined')){
				value = message_bundle[key];
			}else{
				if (!modelerI18N) {
					try {
						initI18N();
					} catch (e) {
						if (defaultValue) {
							return defaultValue;
						} else {
							return key;
						}
					}
				}
					value = modelerI18N.getProperty(key, defaultValue);	
			}
		
			if (value) {
				return value;
			} else {
				return key;
			}
		}
	};

	/**
	 * 
	 */
	function getContextName() {
		return location.href.substring(0, location.href.indexOf("/plugins"));

	}
	;

	function initI18N() {
		var lang = "en";

		jQuery.ajax({
			url : getContextName() + "/services/rest/bpm-reporting/language",
			async : false,
			success : function(l) {
				lang = l;
			}
		});

		InfinityBPMI18N.initPluginProps({
			pluginName : "modeler",
			singleEndPoint : getContextName()
					+ "/services/rest/bpm-reporting/bpm-reporting-client-messages/"
					+ lang
		});

		modelerI18N = InfinityBPMI18N.modeler;
	}
});