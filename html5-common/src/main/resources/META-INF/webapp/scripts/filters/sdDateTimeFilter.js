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
 * @author Subodh.Godbole
 */
angular.module('bpm-common').filter('sdDateTimeFilter', function() {
	return function(input) {
		if (!input) {
	        return "-";
	    } else {
	    	try {
	    		var dateTime = new Date(input);
		        return pad(dateTime.getUTCDate(), 2) + "."
							+ pad(dateTime.getUTCMonth() + 1, 2) + "."
							+ dateTime.getUTCFullYear() + " "
							+ pad(dateTime.getUTCHours(), 2) + ":"
							+ pad(dateTime.getUTCMinutes(), 2);
	    	} catch(e) {
	    		return input; // Cannot process, return as is!
	    	}
	    }
	};

	/**
	 * 
	 */
	function pad(number, characters) {
		// combine with large number & convert to string and cut leading "1"
		return (1e15 + number + "").slice(-characters); 
	}
});