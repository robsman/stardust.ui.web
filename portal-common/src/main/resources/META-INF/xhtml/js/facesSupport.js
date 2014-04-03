/**
 * Any JS initializations that need to be done in all faces views
 * 
 * @author Shrikant.Gangal
 */

/**
 * Added a dummy debug function as IE9/10 doesn't seem to support console.debug function
 * and icefaces code has used it in many places.
 */
if (!console.debug) {
	console.debug = function(a) {
		console.log(a);
	};
}