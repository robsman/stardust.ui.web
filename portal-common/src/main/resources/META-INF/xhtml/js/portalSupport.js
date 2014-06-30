/**
 * For common routines
 * 
 * @author Subodh.Godbole
 */

if(window.btoa == undefined) {
	// Define functions using jQuery
	window.atob = function(s) {
		return jQuery.base64.decode(s);
	}

	window.btoa = function(s) {
		return jQuery.base64.encode(s);
	}
}