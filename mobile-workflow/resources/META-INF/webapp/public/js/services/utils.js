define([],function(){
	
	var utils;
	
	utils = {
			
			"test" : function(){
				return "Hello From Util Service";
			},
			
			/**
			 * Trigger a navigateRequest event on a target.
			 * @param root : Obj event will be triggered on.
			 * @param target : target URL to navigate to.
			 * @param data : data to send with the event
			 */
			"navigateTo" : function(root,target,data){
				$(root).trigger("navigateRequest",{
					"target": target,
					"payload" : data
				});
			},
			
			/**
			 * Generates a UUID as per RFC-4122 section 4.4
			 * @see http://www.ietf.org/rfc/rfc4122.txt
			 * @returns 
			 */
			"generateUUID" : function(){
				  var uuid='xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
				      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
				      return v.toString(16);
				  });
				  return uuid;
			}
		};

		return function(){
			return utils;
		};

});