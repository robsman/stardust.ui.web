/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([], function() {
	return {
		inherit : inherit,
		formatDateTime : formatDateTime
	};

	/**
	 * 
	 */
	function inherit(target, source) {
		jQuery.extend(target, source);
		inheritMethods(target, source);

		return target;
	}

	/**
	 * Auxiliary method to copy all methods from the parentObject to the
	 * childObject.
	 */
	function inheritMethods(target, source) {
		for ( var member in source) {
			if (source[member] instanceof Function) {
				target[member] = source[member];
			}
		}
	}

	/**
	 * 
	 */
	function formatDateTime(dateTime) {
		return pad(dateTime.getUTCDate(), 2) + "."
				+ pad(dateTime.getUTCMonth() + 1, 2) + "."
				+ dateTime.getUTCFullYear() + " "
				+ pad(dateTime.getUTCHours(), 2) + ":"
				+ pad(dateTime.getUTCMinutes(), 2);
	}

	function pad(number, characters) {
		return (1e15 + number + // combine with large number
		"" // convert to string
		).slice(-characters); // cut leading "1"
	}
});
