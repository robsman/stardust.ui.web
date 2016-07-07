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
		this.$get = ['$parse', 'sdLoggerService', 'sgI18nService','sdMimeTypeService','$filter', 
		             function($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter) {
			var service = new DataTableRendererService($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter);
			return service;
		}];
	});

	/*
	 * 
	 */
	function DataTableRendererService($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter) {
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
			var parserCache = {};
			var templateCache = {};

			/*
			 * 
			 */
			DataTableRenderer.prototype.defaultRenderer = function(col, row, contents) {
				return contents;
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.criticalityRenderer = function(col, row, contents) {
				var flag, popover;

				if (templateCache[col.field] == undefined) {
					var cellContents = jQuery('<div>' + contents + '</div>');
					flag = cellContents.find(".flag").html().trim();
					popover = cellContents.find(".popover").html().trim();
					
					templateCache[col.field] = {
						flag : flag,
						popover : popover
					}
				} else {
					flag = templateCache[col.field].flag;
					popover = templateCache[col.field].popover;
				}

				var markup = '';
				for (var i = 0; i < row.criticality.count; i++) {
					markup += flag;
				}

				var popoverId = 'cric-' + row.activityOID;

				return buildPopover(popoverId, popover, markup);
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.priorityRenderer = function(col, row, contents, isWorklist, availablePrios) {
				var data = row.priority;
			
				if(isWorklist === true) {
					
					var styleClass = "pi pi-flag pi-lg priority-flag-"+data.name;
					var flagMarkUp = '<i class="'+ styleClass +'"></i>'
					var label = sgI18nService.translate('views-common-messages.views-activityTable-priorityFilter-table-priorityColumn-name');
					var value = data.label;
					
					var popoverContent = '<div>'+
											'<span ><b>'+ label +'</b></span> : '+
											'<span>'+value+'</span>' +
										 '</div>';
					return  getPopover( popoverContent, flagMarkUp);	
					
				} else {
					var options ='';
					for(var indx = 0 ; indx < availablePrios.length; indx++) {
						options += '<option value="'+availablePrios[indx].value+'">'+availablePrios[indx].label+'</option><br/>';
					}
					
					var html = '<div class="change-higlight-container">\n'+
				   	   				'<select class="activity-table-priority-combo" sda-on-change="activityTableCtrl.registerNewPriority(rowData.activityOID)">'+
				   	   						options +	
				   	   				 '</select>\n'+
			   	   				'</div>';		
			   	   				
			   	    return html;    	
				}
			}

			/*
			 * 
			 */
			DataTableRenderer.prototype.benchmarkRenderer = function(col, row, contents) {
				var html = '';
				var data = row.benchmark;
				
				if(data.color !== undefined) {
					var style = "color:"+data.color;
					var flagMarkUp = '<i class="pi pi-flag pi-lg" style="'+ style +'"></i>'
					var label = sgI18nService.translate('views-common-messages.views-processTable-benchmark-tooltip-categoryLabel');
					var value = data.label;
					var popover =  '<div>'+
										'<span ><b>'+ label +'</b></span> : '+
										'<span>'+value+'</span>' +
								  '<\/div>',
					html =	getPopover(popover ,flagMarkUp );
				}
			
				return html;
			}
			
			
			/**
			 * 
			 */
			function getPopover(popoverContent, markup) {
				var html = '<div href="#" data-placement="top" data-html="true" data-trigger="hover" data-toggle="worklist-popover" '+
							' data-content="'+ popoverContent + '"' + '>' + markup + '</div>';
				return html;
			}

			/**
			 * 
			 */
			function buildPopover(popoverId, popoverContent, markup) {
				var popoverSelector = '.' + popoverId;
				var html = '<div href="#" data-placement="top" data-trigger="hover" data-toggle="table-popover" '+
							' data-popover-content="'+ popoverSelector + '"' + '>' + markup + '</div>';
				html += '<div class="' + popoverId + '" style="display: none;">' + popoverContent + '</div>';
				return html;
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
							if (data[descKeys[key]] && !data[descKeys[key]].isDocument) {
								if (data[descKeys[key]].value && data[descKeys[key]].value !== '') {
									result += 
										'\n<tr>' +
											'<td style="white-space: nowrap;">' + descKeys[key] + ':</td>' + 
											'<td style="white-space: normal;">' + data[descKeys[key]].value + '</td>' +
										'</tr>';
								}
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
				var html = '';
				if (obj && obj.isDocument && obj.documents) {
					for (var indx = 0; indx < obj.documents.length; indx++) {
						var document = obj.documents[indx];
						var icon = sdMimeTypeService.getIcon(document.contentType);
						var documentId = document.uuid;
						
						html += '<span class="noWrap">'+
									'<a href="#" sda-click="activityTableCtrl.openDocumentsView(\''+documentId+'\')">'+
									 '<i class="pi-lg spacing-right '+icon+'"> </i> '+
									 document.name+ '</a>'+
								'</span><br/>';
					}
				} else if (obj && obj.isLink) {
					html = '<a href="' + obj.value + '" title="' + obj.value+ '" target="_blank">' + obj.linkText + 
							'</a>'
				} else if (col.dataType === 'DATE') {
					html =  obj != undefined ? $filter('sdDateFilter')(obj.value) : '';
				} else {
					html = obj != undefined ? '<div class="align-center">'+ obj.value+ '</div>'  : ''
				}
				
				return html;
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

			/*
			 * 
			 */
			DataTableRenderer.prototype.drawHandler = function(table, scope) {
				window.setTimeout(function() {
					jQuery("[data-toggle=worklist-popover]").popover();
					
					jQuery("[data-toggle=table-popover]").popover({
						html: true,
						content: function() {
							var content = jQuery(this).attr('data-popover-content');
							return jQuery(content).html();
						}
					});
				});
			}
		}
	};
})();
