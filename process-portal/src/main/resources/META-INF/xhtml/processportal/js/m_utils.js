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
 * Helper functions for object inspection and object initialization.
 *
 * @author Shrikant.Gangal
 */
define(
		function() {

			return {
				inheritFields : inheritFields,
				
				inheritMethods : inheritMethods
			};


			/**
			 * Copies all data members of and object into another object
			 * recursively. Members existing in the childObject and not existing
			 * in the parentObject will not be overwritten.
			 *
			 * Arrays however will be overwritten.
			 *
			 * TODO - review behaviour for attributes: Attributes also will be
			 * over written, like arrays, as in some cases attributes don't
			 * switch between different values (like true and false), but they
			 * either exist or they don't. In such cases it is necessary to
			 * remove the attributes from child if they don't exist in the
			 * parent.
			 *
			 * The function will not check for cyclic dependencies.
			 *
			 * Functions in parentObject will not be copied.
			 */
			function inheritFields(childObject, parentObject) {
				for ( var member in parentObject) {
					if (parentObject[member] instanceof Function) {
						continue;
					}

					if (typeof parentObject[member] == "object"
							&& childObject[member] != null
							&& !isArray(parentObject[member])
							&& !isAttribute(member)) {
						// Copy recursively

						inheritFields(childObject[member], parentObject[member]);
					} else {
						childObject[member] = parentObject[member];
					}
				}
			};

			/**
			 * Copies all methods of and object into another object.
			 */
			function inheritMethods(childObject, parentObject, superMethodsAccess) {
				var superMethods = {};
				//copy all methods to child object
				for ( var member in parentObject) {
					if (parentObject[member] instanceof Function) {

						//retain all super methods
						if (superMethodsAccess && superMethodsAccess.all) {
							var method = parentObject[member];
							superMethods[member] = function(method) {
								return function() {
									var targetObject = Array.prototype.shift.call(arguments);
									return method.apply(targetObject, arguments);
								};
							}(method);
						}//retain only selected super methods
						else if (superMethodsAccess && superMethodsAccess.selected) {
							if (jQuery.inArray(member, superMethodsAccess.selected) != -1) {
								var method = parentObject[member];
								superMethods[member] = function(method) {
									return function() {
										var targetObject = Array.prototype.shift.call(arguments);
										return method.apply(targetObject, arguments);
									};
								}(method);
							}
						}
						//copy all super methods to child
						childObject[member] = parentObject[member];
					}
				}
				return superMethods;
			};
			

			/**
			 *
			 */
			function isAttribute(member) {
				if (member == "attributes") {
					return true;
				}

				return false;
			}

			/**
			 * See
			 * http://perfectionkills.com/instanceof-considered-harmful-or-how-to-write-a-robust-isarray/
			 */
			function isArray(o) {
				return Object.prototype.toString.call(o) === '[object Array]';
			}
});
