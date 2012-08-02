/**
 * Helper functions for object inspection and object initialization.
 * 
 * @author Marc.Gille
 */
define(
		[],
		function() {

			return {
				removeFromArray : function(array, from, to) {
					removeFromArray(array, from, to);
				},

				removeItemFromArray : function(array, item) {
					removeItemFromArray(array, item);
				},

				viewObject : function(obj) {
					viewObject(obj);
				},

				inheritFields : function(childObject, parentObject) {
					inheritFields(childObject, parentObject);
				},

				inheritMethods : function(childObject, parentObject) {
					inheritMethods(childObject, parentObject);
				},

				typeObject : function(object, prototype) {
					typeObject(object, prototype);
				},

				debug : function(obj) {
					debug(obj);
				},
				
				getLastIndexOf : function(str, searchStr) {
					return getLastIndexOf(str, searchStr);
				},
				
				generateIDFromName : function(name) {
					return name.replace(/ /g, '_');
				}
			};

			function getLastIndexOf(str, searchStr) {
				var index = -1;
				if (undefined != str && undefined != searchStr) {
					var subStr = str;
					var ind = 0;
					while (-1 != (ind = subStr.indexOf(searchStr, ind))) {
						index = ind += searchStr.length;
					}					
				}
				
				return index;
			}
			/**
			 * 
			 * @param from
			 * @param to
			 * @returns
			 */
			function removeFromArray(array, from, to) {
				var rest = array.slice((to || from) + 1 || array.length);
				array.length = from < 0 ? array.length + from : from;
				return array.push.apply(array, rest);
			}
			;

			/**
			 * 
			 * @param item
			 */
			function removeItemFromArray(array, item) {
				var n = 0;
				while (n < array.length) {
					if (array[n] == item) {
						removeFromArray(array, n, n);
						// incase duplicates are present array size decreases,
						// so again checking with same index position
						continue;
					}
					++n;
				}
			}
			;

			function debug(obj) {
				if (typeof console == "object"){
					console.log(obj);
				}
			}

			/**
			 * Copies all data members of and object into another object
			 * recursively. Members existing in the childObject and not existing
			 * in the parentObject will not be overwritten.
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
							&& childObject[member] != null) {
						// Copy recursively

						inheritFields(childObject[member], parentObject[member]);
					} else {
						childObject[member] = parentObject[member];
					}
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

			function viewObject(obj) {
				var outStr = "";

				for ( var mem in obj) {
					if (obj[mem] instanceof Function) {
						outStr += "\t" + "function " + mem + "()= ...\n";
					} else {
						outStr += "\t" + mem + " = " + obj[mem] + "\n";
					}
				}

				debug("----------------------------------------------------------------\n"
						+ "   JavaScript Type "
						+ obj
						+ "\n"
						+ "----------------------------------------------------------------\n\n"
						+ "*** Public Members ***\n"
						+ outStr
						+ "\n"
						+ "*** Constructor ***\n" + obj.constructor ? "\t"
						+ obj.constructor.prototype + "\n" : "undefined");
			}
		});
