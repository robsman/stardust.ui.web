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
 * Utility functions for dialog programming.
 *
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants" ], function(m_utils, m_constants) {
	return {
		makeInvisible : function(element) {
			element.addClass("invisible");
		},
		makeVisible : function(element) {
			element.removeClass("invisible");
		},
		showWaitCursor : function(element) {
			jQuery("body").css("cursor", "wait");
		},
		showAutoCursor : function(element) {
			jQuery("body").css("cursor", "auto");
		},
		registerForNumericFormatValidation : function(input) {
//			input = jQuery(input);
//
//			input.keyup(function() {
//				var val = jQuery(this).val();
//				if (isNaN(val)) {
//					val = val.replace(/[^0-9\.]/g, '');
//					if (val.split('.').length > 2)
//						val = val.replace(/\.+$/, "");
//				}
//				jQuery(this).val(val);
//			});
		}
	};
});