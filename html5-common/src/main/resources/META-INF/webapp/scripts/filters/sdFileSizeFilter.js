/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
/**
 * @author Abhay.Thappan
 */

angular.module('bpm-common').filter('sdFileSizeFilter', function() {
	var units = [ 'bytes', 'KB', 'MB', 'GB', 'TB', 'PB' ];

	return function(bytes, precision) {
		if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
			return '?';
		}

		var unit = 0;

		while (bytes >= 1024) {
			bytes /= 1024;
			unit++;
		}
		
		if(precision == undefined){
			precision = 2;
		}

		return bytes.toFixed(+precision) + ' ' + units[unit];
	};

});
