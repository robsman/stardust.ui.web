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

	angular.module('bpm-common.services').provider('sdDataTableRendererService', function() {
		this.$get = ['$parse', 'sdLoggerService', 'sgI18nService', function($parse, sdLoggerService, sgI18nService) {
			var service = new DataTableRendererService($parse, sdLoggerService, sgI18nService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function DataTableRendererService($parse, sdLoggerService, sgI18nService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdDataTableRendererService');

		/*
		 * 
		 */
		DataTableRendererService.prototype.create = function() {
			return new DataTableRenderer();
		}

		/*
		 * 
		 */
		function DataTableRenderer() {
			/*
			 * 
			 */
			DataTableRenderer.prototype.activityNameRenderer = function(col, row, contents) {
				return contents.replace(new RegExp('__activity.name__', 'g'), row.activity.name || row.activity.id);
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.criticalityRenderer = function(col, row, contents) {
				return row.criticality.label + ' (' + row.criticality.color + ')';
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.priorityRenderer = function(col, row, contents) {
				return row.priority.label;
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.benchmarkRenderer = function(col, row, contents) {
				return row.benchmark.value;
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.descriptorsRenderer = function(col, row, contents) {
				var result = '';
				var data = row.descriptorValues;

				if (data) {
					var descKeys = Object.keys(data);

					if (descKeys && descKeys.length > 0) {
						result = '<table>';
						for (var key = 0; key < descKeys.length; key++) {
							if (data[descKeys[key]] && data[descKeys[key]].value && data[descKeys[key]].value !== '') {
								result += '\n<tr><td>' + descKeys[key] + ':</td><td>' + data[descKeys[key]].value + '</td></tr>';
							}
						}
						result += '\n</table>';
					}
				}

				return result;
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.descriptorRenderer = function(col, row, contents) {
				var obj = row.descriptorValues[col.field];
				return obj != undefined ? obj.value : '';
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.trivialDataRenderer = function(col, row, contents) {
				return 'trivialDataRenderer';
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.actionsRenderer = function(col, row, contents) {
				return contents;
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.rowHandler = function(row, rowData, dataIndex, scope) {
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

				// One time processing of sda-if
				if (row.children().length > 0) {
					var sdaIfs = row.find("[sda-if]");
					if (sdaIfs !== undefined && sdaIfs.length > 0) {
						for (var i = 0; i < sdaIfs.length; i++) {
							handleSdaIf(sdaIfs[i], rowData);
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
					if (element === undefined) return;

					element = jQuery(element);
					var onChangeExpr = element.attr('sda-on-change');
					if(onChangeExpr !== undefined) {
						var onChangeParser = null;
						element.on('change', function(){
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
					if (element === undefined || element.attr('sda-if') === undefined) return;

					var value = evaluate(element.attr('sda-if'), rowData);
					if (value == false) {
						element.remove();
					}
				}

				/*
				 * 
				 */
				function evaluate(expr, rowData) {
					var parser = $parse(expr);
					var value = parser(scope, {
						rowData: rowData
					});
					
					return value;
				}
			}
		}
	};
})();
