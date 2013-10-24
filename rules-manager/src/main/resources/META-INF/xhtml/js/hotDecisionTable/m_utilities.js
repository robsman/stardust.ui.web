/* uuidv4-UUID generator following version 4.4 of the spec.
 *  	http://www.ietf.org/rfc/rfc4122.txt @see section 4.4
		http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
 * hashString-Javascript implementation of Javas string.hashCode function
 */
define([],function(){
	return {
		uuidV4:function(){
			  var uuid='xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
			      return v.toString(16);
			  });
			  return uuid;
			},
		hashString: function(str){
		    var hash = 0;
		    if (str.length == 0) return hash;
		    for (i = 0; i < str.length; i++) {
		        char = str.charCodeAt(i);
		        hash = ((hash<<5)-hash)+char;
		        hash = hash & hash; // Convert to 32bit integer
		    }
		    return hash;
		}
	};
});