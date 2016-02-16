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

angular.module("bpm-common").filter('sdMapOrderBy', function() {
	return function(input, attribute, orderby) {
		if (!angular.isObject(input))
			return input;

		var array = [];
		for ( var objectKey in input) {
			array.push(input[objectKey]);
		}

		array.sort(function(a, b) {
			a = parseInt(a[attribute]);
			b = parseInt(b[attribute]);
			if (orderby === 'desc') {
				return b - a;
			} else {
				return a - b;
			}

		});
		return array;
	}
});