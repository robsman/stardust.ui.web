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

	angular.module('bpm-common.services').provider('sdProcessActivityTableRendererService', function() {
		this.$get = ['$parse', 'sdLoggerService', 'sgI18nService','sdMimeTypeService','$filter', 'sdMarkupCompilerService',
		             function($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter, sdMarkupCompilerService) {
			var service = new ProcessActivityTableRendererService($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter, sdMarkupCompilerService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function ProcessActivityTableRendererService($parse, sdLoggerService, sgI18nService, sdMimeTypeService, $filter, sdMarkupCompilerService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdProcessActivityTableRendererService');

		/*
		 * 
		 */
		ProcessActivityTableRendererService.prototype.create = function() {
			return new ProcessActivityTableRenderer();
		}

		/*
		 * 
		 */
		function ProcessActivityTableRenderer() {
			var templateCache = {};
			var markupCompiler = sdMarkupCompilerService.create();

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.defaultRenderer = function(col, row, contents) {
				return contents;
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.criticalityRenderer = function(col, row, contents) {
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
			ProcessActivityTableRenderer.prototype.priorityRenderer = function(col, row, contents, isWorklist, availablePrios) {
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
			ProcessActivityTableRenderer.prototype.benchmarkRenderer = function(col, row, contents) {
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
			ProcessActivityTableRenderer.prototype.descriptorsRenderer = function(col, row, contents) {
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
			ProcessActivityTableRenderer.prototype.descriptorRenderer = function(col, row, contents) {
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
			ProcessActivityTableRenderer.prototype.trivialDataRenderer = function(col, row, contents) {
				return 'trivialDataRenderer';
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.rowHandler = function(row, rowData, dataIndex, scope) {
				markupCompiler.compile(row, rowData, dataIndex, scope);
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.drawHandler = function(table, scope) {
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
