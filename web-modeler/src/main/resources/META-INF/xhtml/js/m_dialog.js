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
		showAutoCursor: function(element)
		{
			jQuery("body").css("cursor", "auto");		
		},
		registerForIntegerFormatValidation: function(input)
		{
//			input.keypress({input: input}, function(event){
//				if(event.data.input.val() != "") {
//				    var value = event.data.input.val().replace(/^\s\s*/, '').replace(/\s\s*$/, '');
//				    var intRegex = /^\d+$/;
//				    
//				    if (!intRegex.test(value)) {
//				    	event.preventDefault();
//				    }
//				}
//			});
		}
	};
});