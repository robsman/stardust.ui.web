/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * 
 */
function debug(obj) {
	console.debug(obj);
}

/**
 * Copies all data members of and object into another object.
 */
function inheritFields(childObject, parentObject) {
	for ( var member in parentObject) {
		if (parentObject[member] instanceof Function) {
			continue;
		}

		childObject[member] = parentObject[member];
	}
}

/**
 * Copies all methods of and object into another object.
 */
function inheritMethods(childObject, parentObject) {
	for ( var member in parentObject) {
		if (parentObject[member] instanceof Function) {
			childObject[member] = parentObject[member];
		}
	}
}

/**
 * 
 */
function typeObject(object, prototype) {
	inheritMethods(object, prototype);
}
