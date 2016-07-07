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

		/*
		 * 
		 */
		MarkupCompilerService.prototype.create = function() {
			return new MarkupCompiler();
		}

		/*
		 * 
		 */
		function MarkupCompiler() {
			var parserCache = {};

			/*
			 * 
			 */
			MarkupCompiler.prototype.compile = function(row, rowData, dataIndex, scope) {
				// Binding actions - <a> and <buttons>
				var actions = row.find("[sda-click]");
				for (var i = 0; i < actions.length; i++) {
					bindAction(actions[i], rowData);
				}

				// Binding on change events for <select>
				var selects = row.find("select[sda-on-change]");
				for (var i = 0; i < selects.length; i++) {
					bindSelect(selects[i], rowData);
				}

				handleAttribute('[sda-if]', handleSdaIf); // One time processing of sda-if
				handleAttribute('[sda-bind]', handleSdaBind); // One time processing of sda-bind
				handleAttribute('[sda-bind-i18n]', handleSdaI18n); // One time processing of sda-bind-i18n
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
								handlerFunc(elems[i], rowData, attr);
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
						disabled = evaluate(element.attr('sda-disabled'), rowData);
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

					var value = evaluate(element.attr('sda-if'), rowData);
					if ( value !== true ) {
						element.remove();
					}
				}

				/*
				 * 
				 */
				function handleSdaBind(element, rowData) {
					element = jQuery(element);
					if (element === undefined || element.attr('sda-bind') === undefined)
						return;

					var value = evaluate(element.attr('sda-bind'), rowData) || '';
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

					var value = evaluate(element.attr(attr), rowData) || '';
					var attrKey = attr.substring('sda-'.length);
					element.attr(attrKey, value);
				}

				/*
				 * 
				 */
				function evaluate(expr, rowData) {
					if (parserCache[expr] == undefined) {
						try {
							trace.log('Adding to Parser Cache for expression: ' + expr);
							parserCache[expr] = $parse(expr);
						} catch(e) {
							trace.error('Could not parse expression:' + expr, e);
							parserCache[expr] = null;
						}
					}

					var value = '';
					var parser = parserCache[expr];

					if (parser) {
						try {
							value = parser(scope, {
								rowData: rowData
							});
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
