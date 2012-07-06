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
				}
			};

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
			};

			/**
			 * 
			 * @param item
			 */
			function removeItemFromArray(array, item) {
				debug("===> Before removal:");
				debug(array);
				var n=0;
				while (n < array.length) {
					if (array[n] == item) {
						removeFromArray(array, n, n);
						// incase duplicates are present array size decreases,
						// so again checking with same index position
						continue;
					}
					++n;
				}

				debug("===> After removal:");
				debug(array);
			};

			function debug(obj) {
				if (console) {
					console.debug(obj);
				}
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
