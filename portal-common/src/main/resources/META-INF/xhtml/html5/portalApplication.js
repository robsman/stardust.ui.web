/**
 * @author Subodh.Godbole
 */

'use strict';

var portalApplication = new PortalApplication();

/*
 * 
 */
function PortalApplication() {
	var angularModules = ['bpm-ui'];

	/*
	 * 
	 */
	function addModule(module) {
		log("Adding Angular Module - " + module, "i");
		angularModules.push(module);
	};

	/*
	 * 
	 */
	function getModules() {
		return angularModules;
	}

	/*
	 * 
	 */
	function log(str, type) {
		if (console) {
			if (type != undefined && type != 'd') {
				if (type == 'i') {
					console.info(str);
				}
				else if (type == 'w') {
					console.warn(str);
				}
				else if (type == 'e') {
					console.error(str);
				}
			} else {
				console.log(str);
			}
		}
	}

	return {
		addModule : addModule,
		getModules : getModules
	}
}