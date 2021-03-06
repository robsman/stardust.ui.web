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
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_i18nUtils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants"],
		function(m_i18nUtils, m_globalVariables, m_constants) {

			return {
				removeFromArray : function(array, from, to) {
					removeFromArray(array, from, to);
				},

				removeItemFromArray : function(array, item) {
					removeItemFromArray(array, item);
				},

				pushArray : pushArray,

				insertArrayAt : insertArrayAt,

				convertToSortedArray : convertToSortedArray,
				isItemInArray : function(array, item) {
					return isItemInArray(array, item);
				},

				viewObject : function(obj) {
					viewObject(obj);
				},

				typeObject : typeObject,

				inheritFields : function(childObject, parentObject) {
					inheritFields(childObject, parentObject);
				},

				inheritMethods : function(childObject, parentObject, superMethodsAccess) {
					return inheritMethods(childObject, parentObject, superMethodsAccess);
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

				initializeWaitCursor : function(element) {
//					if (element && window.parent.InfinityBpm.Core) {
//						element.ajaxStart(function() {
//							window.parent.InfinityBpm.Core.changeMouseCursorStyle("progress");
//						});
//						element.ajaxStop(function() {
//							window.parent.InfinityBpm.Core.changeMouseCursorStyle("default");
//						});
//					}
				},

				showWaitCursor : function() {
//					if (window.parent.InfinityBpm.Core) {
//						window.parent.InfinityBpm.Core.changeMouseCursorStyle("progress");
//					}
				},

				hideWaitCursor : function () {
//					if (window.parent.InfinityBpm.Core) {
//						window.parent.InfinityBpm.Core.changeMouseCursorStyle("default");
//					}
				},

				/**
				 * TODO
				 * This function takes a simple approach of changing the cursor property of the document "body" element and all its children.
				 * This would not work if a user hovers over an IFRAME or hovers out of the current IFRAME, but this should be good enough for most
				 * use cases as all we need to do is to indicate to the user that a background process is going on and are not trying to stop them from
				 * clicking elsewhere.
				 * If we want to prevent a user from clicking elsewhere we will have to take an approach of creating a modal div with wait cursor. This
				 * also would be quite straight forward, but with a draw back that, if due to some error the modal div does not get removed by the hideWaitCursor function,
				 * it would block the user completely. May be we can have two function, one to go the modal way and one otherwise.
				 *
				 * Note: For any of the above approaches to work, the showWaitCursor function should only be called in conjunction with "async" ajax calls only,
				 * as this won't work for sync calls.
				 *
				 * TODO
				 * There is a separate ticket filed for getting the wait cursor working and here is a summary of what needs to be done
				 * 1. Get rid of sync calls and replace them with async calls and deferred pattern
				 * 2. Have two functions for wait cursor (modal and non-modal) if needed and call them accordingly
				 * 3. Move the content of this function to the non-modal function, delete these tmp functions and make changes to the calling code.
				 */
				showWaitCursorTmp : function() {
					jQuery("body").addClass("waiting");
				},

				/**
				 * TODO - see comment for function "showWaitCursorTmp"
				 */
				hideWaitCursorTmp : function () {
					jQuery("body").removeClass("waiting");
				},

				isBrowserChrome : function() {
					if (navigator.userAgent.toLowerCase().indexOf("chrome") > -1) {
						return true;
					}
				},

				markControlsReadonly : function(divName, readonly) {
					var lookupScope = divName == undefined ? null : jQuerySelect("#" + divName);
					this.markControlsReadonlyForScope(lookupScope, readonly);
				},

				/**
				 * lookupscope should be a jQuerySelect('#elementId') object
				 */
				markControlsReadonlyForScope : function(lookupScope, readonly) {
					jQuerySelect(['input:not(.noDataChange input)', 'textarea:not(.noDataChange textarea)', 'select:not(.noDataChange select)']).each(function(index, type){
						jQuerySelect(type, lookupScope).each(function(index, control){
							if (readonly == undefined || readonly == true) {
								if (control.disabled !== undefined) {
									// Exclude links marked specifically
									if(control.className.indexOf("noDataChange") == -1) {
									  markControlReadonly(control, true);
									}
								}
							} else {
							  markControlReadonly(control, false);
							}
						});
					});
				},

				isElementReadonly : function(element) {
					if (element) {
						if ((typeof element.isReadonly === "function")) {
							return element.isReadonly();
						}
					}

					return false;
				},

        markControlReadonly : markControlReadonly,

				prettyDateTime : prettyDateTime,

				formatDate : formatDate,

				textWrap : textWrap,

				xmlToString : xmlToString,

				contentWrap : contentWrap,

				isArray : isArray,

				isEmptyString : isEmptyString,

				isNumber : isNumber,

				getOutlineWindowAndDocument : getOutlineWindowAndDocument,

				activeViewElement : activeViewElement,
				
				jQuerySelect : jQuerySelect,
				
				executeTimeoutLoop : executeTimeoutLoop,
				
				isIntermediateEvent : isIntermediateEvent,

				encodeXmlPredfinedCharacters : encodeXmlPredfinedCharacters,

				decodeXmlPredfinedCharacters : decodeXmlPredfinedCharacters,

				getUniqueElementNameId : function(array, name) {
					return getUniqueElementNameId(array, name);
				},
				
				getUpdatedUniqueElementNameId : function(array, name) {
				   return getUpdatedUniqueElementNameId(array, name);
				},
				
				generateID : generateID
			};
			
		/**
		 * copied from Rules Manager - m_utilities - with minor changes
		 */	
		function generateID(baseName, coExistantObjs, prop, self, returnOnDuplicate){
				if(!baseName){
					return "";
				}
				var key,				/*key in a for-in construct*/
					temp,				/*temp obj we pull from our coexisters*/
					tempHash={},		/*Hash map we will check against*/
					tempSuffix,			/*Sufffix extracted from our baseName*/
					patt=/_[0-9]+\b/;	/*suffix matcher*/
				
				/*Remove spaces and special characters*/
				baseName=baseName.replace(/[^a-zA-Z0-9_.]+/g,"");
				
				/*Build a hash of our existing IDs*/
				for(key in coExistantObjs){
					if(coExistantObjs.hasOwnProperty(key)){
						temp=coExistantObjs[key];
						/*Avoid checking self against self*/
						if(temp && temp!=self){
							tempHash[temp[prop || "id"].toLowerCase()]={};
						}
					}
				}
				/*Now check our hash for our baseName, adding an incremental suffix
				 *to our baseName until no hash is found*/
				while(true){
					if(!tempHash.hasOwnProperty(baseName.toLowerCase())){
							break;
						}else if(returnOnDuplicate){
							return "duplicate";
					}
					if(patt.test(baseName)){
						tempSuffix=patt.exec(baseName)[0]; /*extract suffix*/
						baseName=baseName.replace(patt,""); /*remove from baseName*/
						tempSuffix =(1*tempSuffix.replace("_",""))+1; /*generate new suffix*/
					}
					else{
						tempSuffix=1;
					}
					baseName =baseName + "_" + tempSuffix;
					
					
				}
				return baseName;
			}			
			
      function markControlReadonly(control, readonly) {
        if (readonly == undefined || readonly == true) {
          jQuery(control).prop("disabled", true);
          jQuery(control).css("opacity", "0.5");
          jQuery(control).css("cursor", "default");
        } else {
          jQuery(control).prop("disabled", false);
          jQuery(control).css("opacity", "1");
          jQuery(control).css("cursor", "auto");
        }
      };


			/**
			 * A utility function to execute a <fn> function, after a delay of
			 * <delay> milliseconds, for a maximum of <reps> repetitions,
			 * until <conditionFn> functions returns true.
			 */
			function executeTimeoutLoop(fn, reps, delay, conditionFn) {
				if (reps > 0) {
					setTimeout(function() {
						if (conditionFn()) {
							fn();	
						} else {
							executeTimeoutLoop(fn, reps-1, delay, conditionFn);
						}
					}, delay);
				}
			};

			/*
			 * 
			 */
			function activeViewElement() {
				// Find HTML5 Framework div for current View
				var view = jQuery(".sg-view-panel").children(".sg-selected");

				if (view == undefined || view.length == 0) {
				  // Fallback portal-shell
				  view = jQuery(".view-panel-active");
				}
				
				if (view == undefined || view.length == 0) {
					return null;	
				} else {
					return view;
				}
			}

			/*
			 *
			 */
			function jQuerySelect(pattern, context) {
				var ret = null;
				if (!context) {
					if (!(typeof pattern === "string"
						&& (pattern.indexOf("</") != -1
									|| pattern.indexOf("/>") != -1))) {

					  // Find HTML5 Framework div for current View
						var view = activeViewElement();

						if (view) {
							var ret = jQuery(pattern, jQuery(view));
							if (ret.length > 0) {
								return ret;
							}
						}
					}
					
					ret = jQuery(pattern);
				}
				ret = jQuery(pattern, context);
				
//				if (ret.length == 0) {
//					return null;
//				}
					
				return ret;
			}

			/*
			 *
			 */
			function getOutlineWindowAndDocument() {
				var parentWindow = m_globalVariables.findMainWindowBottomUp();
				if (parent && parentWindow["BridgeUtils"]) {
					return {
						win: parentWindow.document.getElementById("portalLaunchPanels"),
						doc: parentWindow.document.getElementById("portalLaunchPanels").contentDocument
					};
				} else { // Compatibility to old portal
					return {
						win: parentWindow.frames['ippPortalMain'],
						doc: parentWindow.frames['ippPortalMain'].document
					};
				}
			};

			/**
			 *
			 */
			function isEmptyString(str) {
				return str == null || jQuery.trim(str).length == 0;
			}

			/**
			 *
			 */
			function isNumber(n) {
				return !isNaN(parseFloat(n)) && isFinite(n);
			}

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

			/**
			 * Adds contents of array "newArray" to "thisArray"
			 * This is better than concat as this doesn't create a new array.
			 *
			 * @param arr
			 * @param arr2
			 * @returns
			 */
			function pushArray(thisArray, newArray) {
				thisArray.push.apply(thisArray, newArray);
			}

			/**
			 *
			 * @param toArray
			 * @param fromArray
			 * @param index
			 * @returns
			 */
			function insertArrayAt(toArray, fromArray, index) {
				if (toArray && fromArray) {
					for (var i = (fromArray.length - 1); i >= 0; i--) {
						toArray.splice(index, 0, fromArray[i]);
					};
				}
			}

			/**
			 * @author Yogesh.Manware
			 * This method accepts Element Array and proposed name for new
			 * element. It assumes that all elements in the Array have id and
			 * name attributes. It searches for all elements in the given
			 * array to see if any of the element already has same id or name.
			 * If yes, it increases the index until it finds unique id and unique
			 * name.
			 */
			function getUniqueElementNameId(array, name) {
				var index = 1;
				var id = name.replace(/\s+/g, '');

				var elementNameId = {};
				var hasElement = true;

				while (true) {
					elementNameId.name = name + " " + index;
					elementNameId.id = id + index;

					hasElement = hasElementWithName(array, elementNameId.name);
					hasElement = hasElement || hasElementWithId(array, elementNameId.id);
					if (!hasElement) {
						break;
					}
					index++;
				}
				return elementNameId;
			}

			function hasElementWithName(array, name) {
				for ( var n in array) {
					if (array[n].name == name) {
						return true;
					}
				}
				return false;
			}

			function hasElementWithId(array, id) {
				for ( var n in array) {
					if (array[n].id == id) {
						return true;
					}
				}
				return false;
			}
			
			/**
			 * @author Aditya.Gaikwad
			 * This method accepts Element Array and proposed name for new
			 * element. This function checks Default Label of a Diagram Element which is 
			 * used only once should not contain "1". 
			 */
			function getUpdatedUniqueElementNameId(array, name) {
			   var id = name.replace(/\s+/g, '');
			   
			   var elementNameId = {};
			   var hasElement = true;
               
			   hasElement = hasElementWithName(array, name);
			   hasElement = hasElement || hasElementWithId(array, id);
			   if (hasElement) {
			      return getUniqueElementNameId(array, name);
			   } else {
			      elementNameId.name = name;
			      elementNameId.id = id;
			      return elementNameId; 
			   }
			}

			/**
			 * Trim the text for TextNode element when symbol size is less than
			 * textNode size
			 *
			 * @param t :
			 *            textNode element for Symbol
			 * @param width :
			 *            actual width of Symbol
			 */
			function textWrap(t, width) {
				var content = t.attr("text");
				var abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
				t.attr({
					"text" : abc
				});
				var letterWidth = t.getBBox().width / abc.length;
				t.attr({
					"text" : content
				});

				var x = 0;
				var str = "";
				for ( var i = 0; i < content.length; i++) {
					// If textNode text width reaches symbol width append ".."
					// and break
					if (x + letterWidth > (width - letterWidth)) {
						str += "..";
						break;
					}
					x += letterWidth;
					str += content.charAt(i);
				}
				t.attr({
					"text" : str,
					"title" : content
				});
			}

			/**
			 *
			 * @param array
			 * @param item
			 */
			function isItemInArray(array, item) {
				var i = array.length;
				while (i--) {
					if (array[i] === item) {
						return true;
					}
				}
				return false;
			}

			/**
			 *
			 */
			function convertToSortedArray(obj, field, ascending) {
				var sortedObjects = [];

				for ( var key in obj) {
					if (obj.hasOwnProperty(key)) {
						sortedObjects.push(obj[key]);
					}
				}

				var ascendingFactor = ascending ? 1 : -1;

				sortedObjects.sort(function(left, right) {
					
					var leftValue,
						rightValue;
					
					if(left[field] && right[field]){
					
						leftValue=left[field].toLowerCase();
						rightValue=right[field].toLowerCase();
	
						if (leftValue < rightValue) {
							return -1 * ascendingFactor;
						}
						if (leftValue > rightValue) {
							return 1 * ascendingFactor;
						}
					}
					return 0;
				});

				return sortedObjects;
			}

			/**
			 *
			 */
			function lexicalSort(left, right) {
				left = left.toLowerCase();
				right = right.toLowerCase();

				if (left < right) {
					return -1;
				}
				if (left > right) {
					return 1;
				}

				return 0;
			}

			function debug(obj) {
				if (console && typeof console == "object") {
					if (obj) {
						console.log(obj);
					} else {
						console.log("null");
					}
				}
			}

			/**
			 *
			 */
			function typeObject(proto, untypedObject) {
				var typedObject = Object.create(proto);
				for (prop in untypedObject) {
					if (untypedObject.hasOwnProperty(prop)) {
						typedObject[prop] = untypedObject[prop];
					}
				}
				return typedObject;
			}

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

					//TODO: this logic needs to be improved, 
					//if server has deleted any element, consequently parentObject does not contain it
					//child object still contain this element.
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
			}

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

			/**
			 *
			 */
			function prettyDateTime(date) {
				if (date == null) {
					return "-";
				}

				var time_formats = [
						[
								60,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.lessThanAMinute") ],
						[
								90,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneMinute") ], // 60*1.5
						[
								3600,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.minutes"),
								60 ], // 60*60, 60
						[
								5400,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneHour") ], // 60*60*1.5
						[
								86400,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.hours"),
								3600 ], // 60*60*24, 60*60
						[
								129600,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneDay") ], // 60*60*24*1.5
						[
								604800,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.days"),
								86400 ], // 60*60*24*7, 60*60*24
						[
								907200,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneWeek") ], // 60*60*24*7*1.5
						[
								2628000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.weeks"),
								604800 ], // 60*60*24*(365/12),
						// 60*60*24*7
						[
								3942000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneMonth") ], // 60*60*24*(365/12)*1.5
						[
								31536000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.months"),
								2628000 ], // 60*60*24*365,
						// 60*60*24*(365/12)
						[
								47304000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneYear") ], // 60*60*24*365*1.5
						[
								3153600000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.years"),
								31536000 ], // 60*60*24*365*100,
						// 60*60*24*365
						[
								4730400000,
								m_i18nUtils
										.getProperty("modeler.dateTimeFormatter.values.oneCentury") ], // 60*60*24*365*100*1.5
				];

				var seconds = (new Date().getTime() - date.getTime()) / 1000;
				var suffix = " "
						+ m_i18nUtils
								.getProperty("modeler.dateTimeFormatter.values.ago");

				if (seconds < 0) {
					seconds = Math.abs(seconds);
					suffix = " "
							+ m_i18nUtils
									.getProperty("modeler.dateTimeFormatter.values.fromNow");
				}

				var n = 0;
				var format;

				while (format = time_formats[n]) {
					if (seconds < format[0]) {
						if (format.length == 2) {
							return format[1] + suffix;
						} else {
							return Math.round(seconds / format[2]) + " "
									+ format[1] + suffix;
						}
					}

					++n;
				}

				if (seconds > 4730400000)
					return Math.round(seconds / 4730400000)
							+ " "
							+ m_i18nUtils
									.getProperty("modeler.dateTimeFormatter.values.centuries")
							+ token;

				return m_i18nUtils
						.getProperty("modeler.dateTimeFormatter.values.unknown");
			}

			// TODO I18N

			var nameOfMonths = [ 'January', 'February', 'March', 'April',
					'May', 'June', 'July', 'August', 'September', 'October',
					'November', 'December' ];
			var nameOfDays = [ 'Monday', 'Tuesday', 'Wednesday', 'Thursday',
					'Friday', 'Saturday', 'Sunday' ];

			/**
			 *
			 */
			function formatDate(date, s, utc) {
				s = s.split('');

				var l = s.length;
				var r = '';
				var n = m = null;

				for ( var i = 0; i < l; i++) {
					switch (s[i]) {

					// Day of the month, 2 digits with leading zeros: 01 to 31
					case 'd':
						n = utc ? date.getUTCDate() : date.getDate();
						if (n * 1 < 10)
							r += '0';
						r += n;
						break;
					// A textual representation of a day, three letters: Mon
					// through Sun
					case 'D':
						r += getNameOfDay(date, utc).substring(0, 3);
						break;
					// Day of the month without leading zeros: 1 to 31
					case 'j':
						r += utc ? date.getUTCDate() : date.getDate();
						break;
					// Lowercase l A full textual representation of the day of
					// the week:
					// Sunday (0) through Saturday (6)
					case 'l':
						r += getNameOfDay(date, utc);
						break;
					// ISO-8601 numeric representation of the day of the week: 1
					// (for
					// Monday) through 7 (for Sunday)
					case 'N':
						r += getISODay(date, utc);
						break;
					// English ordinal suffix for the day of the month, 2
					// characters
					case 'S':
						r += getDaySuffix(date, utc);
						break;
					// Numeric representation of the day of the week: 0 (for
					// Sunday) through
					// 6 (for Saturday)
					case 'w':
						r += utc ? date.getUTCDay() : date.getDay();
						break;
					// The day of the year (starting from 0) 0 through 365
					case 'z':
						n = 0;
						m = utc ? date.getUTCMonth() : date.getMonth();
						for ( var i = 0; i < m; i++)
							n += Date.daysInMonth[i]

						if (isLeapYear(date))
							n++;

						n += utc ? date.getUTCDate() : date.getDate();
						n--;
						r += n;
						break;
					// ISO-8601 week number of year, weeks starting on Monday
					case 'W':
						r += getISOWeek(date, utc);
						break;
					// A full textual representation of a month, such as January
					// or March:
					// January through December
					case 'F':
						r += getNameOfMonth(date, utc);
						break;
					// Numeric representation of a month, with leading zeros 01
					// through 12
					case 'm':
						n = utc ? date.getUTCMonth() : date.getMonth();
						n++;
						if (n < 10)
							r += '0';
						r += n;
						break;
					// A short textual representation of a month, three letters:
					// Jan through
					// Dec
					case 'M':
						r += getNameOfMonth(date, utc).substring(0, 3);
						break;
					// Numeric representation of a month, without leading zeros:
					// 1 through
					// 12
					case 'n':
						n = utc ? date.getUTCMonth() : date.getMonth();
						r += ++n;
						break;
					// Number of days in the given month: 28 through 31
					case 't':
						r += date.getDaysInMonth(utc);
						break;
					// Whether it's a leap year: 1 if it is a leap year, 0
					// otherwise.
					case 'L':
						if (isLeapYear(date, utc))
							r += '1';
						else
							r += '0';
						break;
					// ISO-8601 year number. This has the same value as Y,
					// except that if
					// the ISO week number (W) belongs to the previous or next
					// year, that
					// year is used instead
					/*
					 * case 'o': break;
					 */
					// A full numeric representation of a year, 4 digits
					case 'Y':
						r += utc ? date.getUTCFullYear() : date.getFullYear();
						break;
					// A two digit representation of a year
					case 'y':
						n = utc ? date.getUTCFullYear() : date.getFullYear();
						r += (n + '').substring(2);
						break;
					// Lowercase Ante meridiem and Post meridiem am or pm
					case 'a':
						n = utc ? date.getUTCHours() : date.getHours();
						r += n < 12 ? 'am' : 'pm';
						break;
					// AM/PM
					case 'A':
						n = utc ? date.getUTCHours() : date.getHours();
						r += n < 12 ? 'AM' : 'PM';
						break;
					// Swatch Internet time 000 through 999
					// case 'B':
					// break;
					// 12-hour format of an hour without leading zeros
					case 'g':
						n = utc ? date.getUTCHours() : date.getHours();
						if (n > 12)
							n -= 12;
						r += n;
						break;
					// 24-hour format of an hour without leading zeros 0 through
					// 23
					case 'G':
						r += date.getHours();
						break;
					// 12-hour format of an hour with leading zeros 01 through
					// 12
					case 'h':
						n = utc ? date.getUTCHours() : date.getHours();
						if (n > 12)
							n -= 12;
						if (n < 10)
							r += '0';
						r += n;
						break;
					// 24-hour format of an hour with leading zeros 00 through
					// 23
					case 'H':
						n = utc ? date.getUTCHours() : date.getHours();
						if (n < 10)
							r += '0';
						r += n;
						break;
					// i Minutes with leading zeros 00 to 59
					case 'i':
						n = utc ? date.getUTCMinutes() : date.getMinutes();
						if (n < 10)
							r += '0';
						r += n;
						break;
					// s Seconds, with leading zeros 00 through 59
					case 's':
						n = utc ? date.getUTCSeconds() : date.getSeconds();
						if (n < 10)
							r += '0';
						r += n;
						break;
					// Milliseconds
					case 'u':
						r += utc ? date.getUTCMilliseconds() : date
								.getMilliseconds();
						break;
					// Timezone identifier
					// case 'e':
					// break;
					// Whether or not the date is in daylight saving time 1 if
					// Daylight
					// Saving Time, 0 otherwise.
					case 'I':
						if (date.getMinutes() != date.getUTCMinutes)
							r += '1';
						else
							r += '0';
						break;
					// Difference to Greenwich time (GMT) in hours
					case 'O':
						n = date.getTimezoneOffset() / 60;
						if (n >= 0)
							r += '+';
						else
							r += '-';
						n = Math.abs(n);
						if (Math.abs(n) < 10)
							r += '0';
						r += n + '00';
						break;
					// Difference to Greenwich time (GMT) with colon between
					// hours and
					// minutes: Example: +02:00
					case 'P':
						n = date.getTimezoneOffset() / 60;
						if (n >= 0)
							r += '+';
						else
							r += '-';
						n = Math.abs(n);
						if (Math.abs(n) < 10)
							r += '0';
						r += n + ':00';
						break;
					// T Timezone abbreviation EST, MDT etc.
					// case 'T':
					// break;
					// Z Timezone offset in seconds. The offset for timezones
					// west of UTC is
					// always negative, and for those east of UTC is always
					// positive.
					case 'Z':
						r += date.getTimezoneOffset() * 60;
						break;
					// ISO 8601 date: 2004-02-12T15:19:21+00:00
					case 'c':
						r += formatDate(date, 'Y-m-d', utc) + 'T'
								+ formatDate(date, 'H:i:sP', utc);
						break;
					// RFC 2822 formatted date Example: Thu, 21 Dec 2000
					// 16:01:07 +0200
					case 'r':
						r += formatDate(date, 'D, j M Y H:i:s P', utc);
						break;
					// UNIX system time epoch
					case 'U':
						r += date.getTime();
						break;
					default:
						r += s[i];
					}
				}

				return r;
			}

			/**
			 *
			 */
			function getDaySuffix(date, utc) {
				var n = utc ? date.getUTCDate() : date.getDate();
				// If not the 11th and date ends at 1
				if (n != 11 && (n + '').match(/1$/))
					return 'st';
				// If not the 12th and date ends at 2
				else if (n != 12 && (n + '').match(/2$/))
					return 'nd';
				// If not the 13th and date ends at 3
				else if (n != 13 && (n + '').match(/3$/))
					return 'rd';
				else
					return 'th';
			}

			/**
			 * Return the ISO day number for a date
			 */
			function getISODay(date, utc) {
				// Native JS method - Sunday is 0, monday is 1 etc.

				var d = utc ? date.getUTCDay() : date.getDay();

				// Return d if not sunday; otherwise return 7

				return d ? d : 7;
			}

			/**
			 *
			 */
			function getISOWeek(date, utc) {
				var y = utc ? date.getUTCFullYear() : date.getFullYear();
				var m = utc ? date.getUTCMonth() + 1 : date.getMonth() + 1;
				var d = utc ? date.getUTCDate() : date.getDate();

				// If month jan. or feb.

				if (m < 3) {
					var a = y - 1;
					var b = (a / 4 | 0) - (a / 100 | 0) + (a / 400 | 0);
					var c = ((a - 1) / 4 | 0) - ((a - 1) / 100 | 0)
							+ ((a - 1) / 400 | 0);
					var s = b - c;
					var e = 0;
					var f = d - 1 + 31 * (m - 1);
				}

				// If month mar. through dec.

				else {
					var a = y;
					var b = (a / 4 | 0) - (a / 100 | 0) + (a / 400 | 0);
					var c = ((a - 1) / 4 | 0) - ((a - 1) / 100 | 0)
							+ ((a - 1) / 400 | 0);
					var s = b - c;
					var e = s + 1;
					var f = d + ((153 * (m - 3) + 2) / 5 | 0) + 58 + s;
				}

				var g = (a + b) % 7;

				// ISO Weekday (0 is monday, 1 is tuesday etc.)

				var d = (f + g - e) % 7;
				var n = f + 3 - d;

				if (n < 0)
					var w = 53 - ((g - s) / 5 | 0);
				else if (n > 364 + s)
					var w = 1;
				else
					var w = (n / 7 | 0) + 1;
				return w;
			}

			/**
			 *
			 * @param date
			 * @param utc
			 * @returns
			 */
			function getNameOfDay(date, utc) {
				var d = date.getISODay(utc) - 1;

				return nameOfDays[d];
			}

			/**
			 *
			 * @param date
			 * @param utc
			 * @returns
			 */
			function getNameOfMonth(date, utc) {
				var m = utc ? date.getUTCMonth() : date.getMonth();

				return nameOfMonths[m];
			}

			/**
			 *
			 */
			function getTimezoneOffset(date) {
				return date.getTimezoneOffset() * -1;
			}

			/**
			 * Retuns true if year is a leap year; otherwise false
			 */
			function isLeapYear(date, utc) {
				var y = utc ? date.getUTCFullYear() : date.getFullYear();

				return !(y % 4) && (y % 100) || !(y % 400) ? true : false;
			}

			/**
			 * Stringifies the content of an entire XML tag.
			 */
			function xmlToString(xmlData) {
				var xmlString;

				// IE
				if (window.ActiveXObject) {
					xmlString = xmlData.xml;
				}
				// Code for Mozilla, Firefox, Opera, etc.
				else {
					xmlString = (new XMLSerializer())
							.serializeToString(xmlData[0]);
				}

				return xmlString;
			}

			/**
			 * wraps String
			 *
			 * @param content :
			 *            string to be wrapped
			 * @param maxLength :
			 *            max number of characters in one line
			 * @param brk :
			 *            The character(s) to be inserted at every break
			 *
			 */
			function contentWrap(content, maxLength, brk) {
				if (!content) {
					return content;
				}
				var regex = ".{1," + maxLength + "}(\\s|$)";
				return content.match(RegExp(regex, "g")).join(brk);
			}
			
			/**
			 * checks if the symbol is of type Intermediate Event
			 */
			function isIntermediateEvent(symbol) {
				if (symbol.modelElement
						&& (m_constants.INTERMEDIATE_EVENT_TYPE == symbol.modelElement.eventType)) {
					return true;
				}
				return false;
			}


			function encodeXmlPredfinedCharacters(content) {

				content = content.replace(new RegExp("&", 'g'), "&amp;");
				content = content.replace(new RegExp(">", 'g'), "&gt;");
				content = content.replace(new RegExp("<", 'g'), "&lt;");
				content = content.replace(new RegExp("\"", 'g'), "&quot;");
				content = content.replace(new RegExp("'", 'g'), "&apos;");

				return content;
			}

			function decodeXmlPredfinedCharacters(content) {

				content = content.replace(new RegExp("&amp;", 'g'), "&");
				content = content.replace(new RegExp("&gt;", 'g'), ">");
				content = content.replace(new RegExp("&lt;", 'g'), "<");
				content = content.replace(new RegExp("&quot;", 'g'), "\"");
				content = content.replace(new RegExp("&apos;", 'g'), "'");

				return content;
			}

		});
