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
		}
	};
});