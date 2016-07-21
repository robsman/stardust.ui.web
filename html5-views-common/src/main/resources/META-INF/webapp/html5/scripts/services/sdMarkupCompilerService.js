/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/*
 * @author Subodh.Godbole
 */

(function() {
	'use strict';

	angular.module('bpm-common.services').provider('sdMarkupCompilerService', function() {
		this.$get = ['$parse', 'sdLoggerService', 'sgI18nService', 
		             function($parse, sdLoggerService, sgI18nService) {
			var service = new MarkupCompilerService($parse, sdLoggerService, sgI18nService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function MarkupCompilerService($parse, sdLoggerService, sgI18nService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdMarkupCompilerService');
		var globalParserCache = {};

		/*
		 * 
		 */
		MarkupCompilerService.prototype.create = function(uniqueId) {
			return new MarkupCompiler(uniqueId);
		}

		/*
		 * 
		 */
		function MarkupCompiler(uniqueId) {
			var parserCache;

			if (uniqueId) {
				if (!globalParserCache[uniqueId]) {
					globalParserCache[uniqueId] = {};
				}
				parserCache = globalParserCache[uniqueId];
			} else {
				parserCache = {};
			}

			/*
			 * 
			 */
			MarkupCompiler.prototype.compile = function(row, rowData, dataIndex, scope) {

				handleAttribute('[sda-if]', handleSdaIf); // One time processing of sda-if
				handleAttribute('[sda-repeat]', handleSdaRepeat); // One time processing of sda-repeat
				handleAttribute('[sda-repeat-if]', handleSdaRepeatIf); // One time processing of sda-repeat-if
				handleAttribute('[sda-bind]', handleSdaBind); // One time processing of sda-bind
				handleAttribute('[sda-bind-i18n]', handleSdaI18n); // One time processing of sda-bind-i18n
				handleAttribute('[sda-click]', bindAction); // One time processing of sda-click
				handleAttribute('select[sda-on-change]', bindSelect); // One time processing of sda-on-change
				handleAttribute('[sda-class]', handleSdaAttribute, 'sda-class'); // One time processing of sda-class
				handleAttribute('[sda-style]', handleSdaAttribute, 'sda-style'); // One time processing of sda-style

				/*
				 * 
				 */
				function handleAttribute(selector, handlerFunc, attr) {
					if (row.children().length > 0) {
						var elems = row.find(selector);
						if (elems !== undefined && elems.length > 0) {
							for (var i = 0; i < elems.length; i++) {
								try {
									handlerFunc(elems[i], rowData, attr);
								} catch (e) {
									trace.error('Could not handle :' + selector + ' for row: ' + dataIndex, e);
								}
							}
						}
					}
				}

				/*
				 * 
				 */
				function bindAction(element, rowData) {
					if (element === undefined) return;

					// Handle sda-title - I18N Title
					element = jQuery(element);
					if (element.attr('sda-title') !== undefined) {
						element.attr('title', sgI18nService.translate(element.attr('sda-title')));
					}

					// Handle sda-disabled on current element
					var disabled = handleSdaDisabled(element, rowData);

					// Bind Click
					var clickExpr = element.attr('sda-click');
					if (clickExpr !== undefined && disabled === false) {
						var actionParser = null;
						element.on('click', function ($event) {
							if (!actionParser) {
								actionParser = $parse(clickExpr);
							}

							actionParser(scope, {
								rowData: rowData,
								$event: $event
							});
						});
					}

					// Bind Hover
					var hover = element.attr('sda-hover');
					if (hover !== undefined) {
						element.on('mouseover', function ($event) {
							element.find(hover).show();
						});
						element.on('mouseout', function ($event) {
							element.find(hover).hide();
						});
					}
				}

				/*
				 * 
				 */
				function bindSelect(element, rowData) {
					if (element === undefined)
						return;

					element = jQuery(element);
					var onChangeExpr = element.attr('sda-on-change');
					if(onChangeExpr !== undefined) {
						var onChangeParser = null;
						element.on('change', function($event){
							if (!onChangeParser) {
								onChangeParser = $parse(onChangeExpr);
							}

							onChangeParser(scope, {
								rowData: rowData,
								$event: $event
							});
						});
					}
				}

				/*
				 * 
				 */
				function handleSdaDisabled(element, rowData) {
					var disabled = false;
					if(element.attr('sda-disabled') != undefined) {
						disabled = evaluate(element.attr('sda-disabled'), {rowData: rowData});
					}

					if (disabled == true) {
						element.attr('disabled', 'true');
					}

					return disabled;
				}

				/*
				 * 
				 */
				function handleSdaIf(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-if') === undefined)
						return;

					var value = evaluate(element.attr('sda-if'), {rowData: rowData});
					if (value !== true) {
						element.remove();
					}
				}

				/*
				 * 
				 */
				function handleSdaRepeatIf(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-repeat-if') === undefined)
						return;

					var value = evaluate(element.attr('sda-repeat-if'), {rowData: rowData});
					if ( value !== true ) {
						element.remove();
					}
				}

				/*
				 * At the moment only handling simple expression: "loopVar in collection"
				 */
				function handleSdaRepeat(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-repeat') === undefined)
						return;

					var repeatedHtml = '';

					var repeat = element.attr('sda-repeat');

					var parts = repeat.split(' ');
					var loopVar = parts[0];
					var repeatExpr = parts[2];

					var repeatValue = evaluate(repeatExpr, {rowData: rowData});
					if(repeatValue != undefined && repeatValue != null) {
						element.removeAttr('sda-repeat'); // Remove now and add it later

						var html = element.get(0).outerHTML;
						var searchVal = new RegExp(eval('/' + loopVar + './g'));
						var keys = Object.keys(repeatValue);
						if (keys && keys.length > 0) {
							for (var key = 0; key < keys.length; key++) {
								var newVal = repeatExpr + '[\'' + keys[key] + '\'].';

								var loopHtml = html.replace(searchVal, newVal);

								repeatedHtml += loopHtml + '\n';
							}
						}

						element.attr('sda-repeat', repeat); // Add it back
					}

					element.replaceWith(repeatedHtml);
				}

				/*
				 * 
				 */
				function handleSdaBind(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-bind') === undefined)
						return;

					var value = evaluate(element.attr('sda-bind'), {rowData: rowData});
					value = (value === undefined || value == null) ? '' : value;
					
					element.html(value);
				}

				/*
				 * 
				 */
				function handleSdaI18n(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-bind-i18n') === undefined)
						return;

					var key = element.attr('sda-bind-i18n');
					var value = sgI18nService.translate(key);
					element.html(value);
				}

				/*
				 * 
				 */
				function handleSdaAttribute(element, rowData, attr) {
					element = jQuery(element);
					if (element === undefined || element.attr(attr) === undefined)
						return;

					var value = evaluate(element.attr(attr), {rowData: rowData}) || '';
					var attrKey = attr.substring('sda-'.length);
					element.attr(attrKey, value);
				}

				/*
				 * 
				 */
				function evaluate(expr, locals) {
					if (parserCache[expr] == undefined) {
						try {
							trace.log('Adding to Parser Cache for expression: ' + expr);
							parserCache[expr] = $parse(expr);
						} catch (e) {
							trace.error('Could not parse expression:' + expr, e);
							parserCache[expr] = null;
						}
					}

					var value = '';
					var parser = parserCache[expr];

					if (parser) {
						try {
							value = parser(scope, locals);
						} catch (e) {
							trace.error('Error while running parser function for expression:' + expr, e);
						}
					}

					return value;
				}
			}
		}
	};
})();
