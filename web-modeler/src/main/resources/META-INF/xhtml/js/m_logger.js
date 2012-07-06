/**
 * @author Omkar.Patil
 */

define(function(){
	return {
		log: function(){ //deal upto 3 parameters, ignore rest
			try {
				switch (arguments.length) {
					case 1:
						console.log(arguments[0]);
						break;
					case 2:
						console.log(arguments[0], arguments[1]);
						break;
					case 3:
						console.log(arguments[0], arguments[1], arguments[2]);
						break;
				}
			}catch (e) {/* do nothing */}
		}		
	}
});