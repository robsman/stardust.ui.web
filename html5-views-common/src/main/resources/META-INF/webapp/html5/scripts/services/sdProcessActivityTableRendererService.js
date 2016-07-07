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
		this.$get = ['$parse', '$compile','$filter', 'sdLoggerService', 'sgI18nService','sdMimeTypeService', 'sdMarkupCompilerService', 'sdUtilService',
		             function($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService, sdMarkupCompilerService, sdUtilService) {
			var service = new ProcessActivityTableRendererService($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService, sdMarkupCompilerService, sdUtilService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function ProcessActivityTableRendererService($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService, sdMarkupCompilerService, sdUtilService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdProcessActivityTableRendererService');
		var templateCache = {};
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
			var markupCompiler = sdMarkupCompilerService.create();
			var selectorUUID = "AT-"+(Math.floor(Math.random() * 9000) + 1000);
			var currentDataMapping;

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
				var cellTemplate = getCellTemplateMarkup(col, contents, ['flag', 'popover']);

				var flag = cellTemplate.flag;
				var popover = cellTemplate.popover;
				
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
				var cellTemplate = getCellTemplateMarkup(col, contents, ['flag', 'popover', 'editMode']);

				if (isWorklist === true) {
					var flag = cellTemplate.flag;
					var popover = cellTemplate.popover;
					var popoverId = 'prio-' + row.activityOID;

					return buildPopover(popoverId, popover, flag);
				} else {
					var options = '';
					
					for (var i = 0; i < availablePrios.length; i++) {
						if (availablePrios[i].value == row.priority.value) {
							options += '<option selected="selected" value="' + availablePrios[i].value + '">'
									+ availablePrios[i].label + '</option>\n';
						} else {
							options += '<option value="' + availablePrios[i].value + '">' + availablePrios[i].label
									+ '</option>\n';
						}
					}

					return cellTemplate.editMode.replace('__OPTIONS__', options);
				}
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.benchmarkRenderer = function(col, row, contents) {
				var html = '';

				if(row.benchmark != undefined && row.benchmark.color != undefined) {
					var cellTemplate = getCellTemplateMarkup(col, contents, ['flag', 'popover']);

					var flag = cellTemplate.flag;
					var popover = cellTemplate.popover;
					var popoverId = 'bm-' + row.activityOID;

					html = buildPopover(popoverId, popover, flag);
				}
			
				return html;
			}
			
			/*
			 * 
			 */
			function getCellTemplateMarkup(col, contents, childClasses) {
				if (templateCache[col.field] == undefined) {
					templateCache[col.field] = {};

					var cellContents = jQuery('<div>' + contents + '</div>');
					for(var i = 0; i < childClasses.length; i++) {
						var html = cellContents.find('.' + childClasses[i]).html().trim();
						templateCache[col.field][childClasses[i]] = html;
					}
				}

				return templateCache[col.field];
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

			/**
			 * 
			 */
			var createDataMappingContent = function(row) {
				var content = '';
				
				if(row.dataMappings && row.dataMappings.length > 0) {
					content = '<div selectorId="'+selectorUUID+'AngularCompile" sda-hover="#'+selectorUUID+row.activityOID+'-editFlag">\n';
					var indicator = '<i class="pi pi-edit pi-lg" id="'+selectorUUID+row.activityOID+'-editFlag" style="display:none;"> </i>';
					content += '<div style="width:10px;height:15px" class="right">'+indicator +'</div>\n';
					content += '<table><tbody>';
					
					for(var i=0; i < row.dataMappings.length; i++) {
						var item = row.dataMappings[i];
						var value = row.inOutData[item.id] || '';
						switch(item.typeName) {	
						case 'java.util.Date':  
							value = $filter('sdDateFilter')(value); 
							break;
						case 'date':  
							value = $filter('sdTimeFilter')(value);
							break;	
						case 'java.lang.Boolean':  
							value = !!value; 
							break;
						}
						content += '<tr>';
						content += '<td>'+item.name+'</td>';
						content += '<td style="width:5px">:</td>';
						content += '<td style="white-space: nowrap;">'+value+'</td>';
						content += '</tr>';
					}
					content += '</tbody></table></div>';
				}
				return content;
			}
			
			
			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.trivialDataRenderer = function(col, row, contents) {
				if(templateCache['trivialManual'] === undefined) {
					templateCache['trivialManual'] = contents;
				}
				
				if(currentDataMapping) {
					currentDataMapping = undefined;
				}
				return createDataMappingContent(row);
			}


			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.rowHandler = function(row, rowData, dataIndex, scope) {
				
				var dataMappings = row.find('[selectorId="'+selectorUUID+'AngularCompile"]');
				
				
				if(dataMappings !== undefined) {
					bindDataMappingHandler(dataMappings, rowData, scope, $compile);
					// Bind Hover
					var hover = dataMappings.attr('sda-hover');
					
					if (hover !== undefined) {
						dataMappings.on('mouseover', function ($event) {
							dataMappings.find(hover).show();
						});
						dataMappings.on('mouseout', function ($event) {
							dataMappings.find(hover).hide();
						});
					}
				}
			
				function bindDataMappingHandler (element, rowData, scope, $compile) {
					
					element.on('click', renderOnClick);
					
					//Change this element
					$(window).on('click', resetHandler);
					
					function swicthToReadOnly() {
						if(currentDataMapping.rowData.isDataDirty !== true) {
							currentDataMapping.dataMappings.html(createDataMappingContent(currentDataMapping.rowData));
							currentDataMapping = undefined;
						}
					}
					
					function resetHandler($event) {
						
						if(currentDataMapping === undefined) return;
						try {
							if($event.target.offsetParent.localName !== 'table') {
								swicthToReadOnly();
							} 
						} catch (e) { /* ignore */ }
					
					}

					function renderOnClick ($event) {
						
						if(currentDataMapping !== undefined && currentDataMapping.rowData.activityOID === rowData.activityOID) return;
						
						if(currentDataMapping !== undefined && currentDataMapping.rowData.activityOID !== rowData.activityOID) {
							//Switching the previous to read only mode
							swicthToReadOnly();
						}
						
						scope.rowData = rowData;	 			
						element.html(
								$compile(templateCache['trivialManual'])(scope)
						);

						// apply scope changes
						sdUtilService.safeApply(scope);

						// set current data mapping
						currentDataMapping = {
								dataMappings: element,
								rowData: rowData
						};	
						
					}
				}
				
				markupCompiler.compile(row, rowData, dataIndex, scope);
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.drawHandler = function(table, scope) {
				// Timeout makes loading of page appear faster
				window.setTimeout(function() {
					jQuery("[data-toggle=table-popover]", table).popover({
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
