/**
 * Utility functions for dialog programming.
 * 
 * @author Marc.Gille
 */
define([ "m_utils", "m_constants" ], function(m_utils, m_constants) {
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