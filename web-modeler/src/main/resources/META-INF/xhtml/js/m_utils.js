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
		[],
		function() {

			return {
				removeFromArray : function(array, from, to) {
					removeFromArray(array, from, to);
				},

				removeItemFromArray : function(array, item) {
					removeItemFromArray(array, item);
				},
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
					// Added toUpperCase for compatibility with server side Id
					// generation logic
					return name.replace(/ /g, '_').toUpperCase();
				},

				prettyDateTime : prettyDateTime,

				formatDate : formatDate,

				textWrap : textWrap,

				xmlToString : xmlToString,

				contentWrap : contentWrap
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

				var ascendingFactor = ascending ? -1 : 1;
				
				sortedObjects.sort(function(left, right) {
					var leftValue = left[field].toLowerCase();
					var rightValue = right[field].toLowerCase();

					if (leftValue < rightValue) {
						return -1 * ascendingFactor;
					}
					if (leftValue > rightValue) {
						return 1 * ascendingFactor;
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
				if (typeof console == "object") {
					console.log(obj);
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

			/**
			 * 
			 */
			function prettyDateTime(date) {
				if (date == null) {
					return "-";
				}

				var time_formats = [ [ 60, 'Less than a Minute' ],
						[ 90, '1 Minute' ], // 60*1.5
						[ 3600, 'Minutes', 60 ], // 60*60, 60
						[ 5400, '1 Hour' ], // 60*60*1.5
						[ 86400, 'Hours', 3600 ], // 60*60*24, 60*60
						[ 129600, '1 Day' ], // 60*60*24*1.5
						[ 604800, 'Days', 86400 ], // 60*60*24*7, 60*60*24
						[ 907200, '1 Week' ], // 60*60*24*7*1.5
						[ 2628000, 'Weeks', 604800 ], // 60*60*24*(365/12),
						// 60*60*24*7
						[ 3942000, '1 Month' ], // 60*60*24*(365/12)*1.5
						[ 31536000, 'Months', 2628000 ], // 60*60*24*365,
						// 60*60*24*(365/12)
						[ 47304000, '1 Year' ], // 60*60*24*365*1.5
						[ 3153600000, 'Years', 31536000 ], // 60*60*24*365*100,
						// 60*60*24*365
						[ 4730400000, '1 Century' ], // 60*60*24*365*100*1.5
				];

				var seconds = (new Date().getTime() - date.getTime()) / 1000;
				var suffix = " ago";

				if (seconds < 0) {
					seconds = Math.abs(seconds);
					suffix = " from now";
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
					return Math.round(seconds / 4730400000) + " Centuries"
							+ token;

				return "(Unknown)";
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
		});
