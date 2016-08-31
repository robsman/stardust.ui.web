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
		this.$get = ['$parse', '$compile','$filter', 'sdLoggerService', 'sgI18nService','sdMimeTypeService',
		             'sdMarkupCompilerService', 'sdUtilService',
		             function($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService,
		            		 sdMarkupCompilerService,  sdUtilService) {
			var service = new ProcessActivityTableRendererService($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService, 
					sdMarkupCompilerService, sdUtilService);
			return service;
		}];
	});

	/*
	 * 
	 */
	function ProcessActivityTableRendererService($parse, $compile, $filter, sdLoggerService, sgI18nService, sdMimeTypeService,
			sdMarkupCompilerService, sdUtilService) {
		var trace = sdLoggerService.getLogger('bpm-common.services.sdProcessActivityTableRendererService');
		var globalTemplateCache = {};

		/*
		 * 
		 */
		ProcessActivityTableRendererService.prototype.create = function(uniqueId) {
			return new ProcessActivityTableRenderer(uniqueId);
		}

		/**
		 * 
		 */
		function getTemplateCache(uniqueId) {
			var templateCache;

			if (uniqueId) {
				if (!globalTemplateCache[uniqueId]) {
					globalTemplateCache[uniqueId] = {};
				}
				templateCache = globalTemplateCache[uniqueId];
			} else {
				templateCache = {};
			}

			return templateCache;
		}

		/*
		 * 
		 */
		function ProcessActivityTableRenderer(uniqueId) {
			var templateCache = getTemplateCache(uniqueId);
			var markupCompiler = sdMarkupCompilerService.create(uniqueId);
			var selectorUUID = "AT-"+(Math.floor(Math.random() * 9000) + 1000);
			var	currentDataMapping;

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
						var html = cellContents.find('.' + childClasses[i]).html() || ''; 
						templateCache[col.field][childClasses[i]] = html.trim();
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
				var obj = row.descriptorValues ? row.descriptorValues[col.field] : undefined;
				var html = '';
				var cellTemplate  = getCellTemplateMarkup(col, contents, ["typeDocument","typeLink","typeText"]);


				if (obj && obj.isDocument && obj.documents) {
					var docTemplate = cellTemplate.typeDocument;

					for (var indx = 0; indx < obj.documents.length; indx++) {
						var document = obj.documents[indx];
						var documentName = document.name;
						var icon = sdMimeTypeService
						.getIcon(document.contentType);
						var documentId = document.uuid;

						html += docTemplate.replace('_documentName_',
								documentName).replace('_documentIcon_', icon)
								.replace('_documentId_', documentId);

					}
				} else if (obj && obj.isLink) {
					var linkTemplate = cellTemplate.typeLink;
					html = linkTemplate.replace('_linkValue_',obj.value).replace('_linkText_',obj.linkText);
				} else if (col.dataType === 'DATE') {
					var docTemplate = cellTemplate.typeText;
					var value = obj != undefined ? $filter('sdDateFilter')(
							obj.value) : undefined;
							if (value) {
								html = docTemplate.replace('_value_', value);
							}

				} else {
					var docTemplate = cellTemplate.typeText;
					if (obj && obj.value) {
						html = docTemplate.replace('_value_', obj.value);
					}
				}
				return html;
			}

			/**
			 * 
			 */
			var createDataMappingContent = function(row) {
				var content = '';

				if(row.dataMappings && row.dataMappings.length > 0) {
					var indicator = '<i class="pi worklist-data-edit-flag pi-edit pi-lg" id="'+selectorUUID+row.activityOID+'-editFlag" style="display:none;"> </i>';
					content += '<div class="right">'+indicator +'</div>\n';
					content += '<table class="worklist-data-read-only"><tbody>';

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
					content += '</tbody></table>';
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
				var dataMappingContent = createDataMappingContent(row);
				var content = '<div class="worklist-data-row" selectorId="AngularCompile" sda-hover="#'+selectorUUID+row.activityOID+'-editFlag">\n';
				content += dataMappingContent + '</div>'
				return content;
			}


			/**
			 * 
			 */
			function dataMappingHandler(row, rowData, dataIndex, scope) {
				var dataMappings = row.find('[selectorId="AngularCompile"]');


				if (dataMappings !== undefined) {
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

				/**
				 * 
				 */
				ProcessActivityTableRenderer.prototype.worklistDataResetHandler = function($event) {

					if (currentDataMapping === undefined) return;
					try {
						if (jQuery($event.target).closest('table').hasClass("worklist-data-edit-mode") === false) {
							swicthToReadOnly();
						}
					} catch (e) { /* ignore */ }

				}

				/**
				 * 
				 */
				function swicthToReadOnly() {
					if (currentDataMapping.rowData.isDataDirty !== true) {
						currentDataMapping.dataMappings.html(createDataMappingContent(currentDataMapping.rowData));
						currentDataMapping = undefined;
					}
				}

				/**
				 */
				function bindDataMappingHandler(element, rowData, scope, $compile) {

					element.on('click', renderOnClick);

					function renderOnClick($event) {

						if (currentDataMapping !== undefined && currentDataMapping.rowData.activityOID === rowData.activityOID) return;

						if (currentDataMapping !== undefined && currentDataMapping.rowData.activityOID !== rowData.activityOID) {
							//Switching the previous to read only mode
							swicthToReadOnly();
						}

						scope.rowData = rowData;
						element.html(
								$compile(templateCache['trivialManual'])(scope)
						);

						$event.stopPropagation();

						// apply scope changes
						sdUtilService.safeApply(scope);

						// set current data mapping
						currentDataMapping = {
								dataMappings: element,
								rowData: rowData
						};

					}
				}
			}


			/**
			 * 
			 */
			ProcessActivityTableRenderer.prototype.showActivityActionsPopover = function ($event, passedScope) {
				var popoverContent = "<div sd-activity-actions-popover-content></div>";
				this.showActionsPopover($event, passedScope, popoverContent);
			}
			
			/**
			 * 
			 */
			ProcessActivityTableRenderer.prototype.showProcessActionsPopover = function ($event, passedScope) {
				var popoverContent = "<div sd-process-actions-popover-content></div>";
				this.showActionsPopover($event, passedScope, popoverContent);
			}
			
			/**
			 * 
			 */
			
			ProcessActivityTableRenderer.prototype.showActionsPopover = function ($event, passedScope, popoverContent) {
				var template = $compile(popoverContent)(passedScope);
				var elem = jQuery($event.target);
				elem.addClass("popover-open");
				var popoverElem = elem.popover({
					"html" : true,
					placement : "left",
					"content" : function () {
						return template;
					}
				})
				sdUtilService.safeApply(passedScope);
				
				//Work around for popup not positioned correctly on the first load.
				setTimeout(function () {
					popoverElem.popover("show");
					
				},100);
			}
			
			
			/**
			 * 
			 */
			
			ProcessActivityTableRenderer.prototype.openProcessDocumentsPopover = function ($event, passedScope, popoverContent) {
				var content = "<div sd-process-documents-popover-content></div>";
				return this.openDocumentsPopover($event, passedScope, content);
			}
			
			/**
			 * 
			 */
			
			ProcessActivityTableRenderer.prototype.openActivityDocumentsPopover = function ($event, passedScope, popoverContent) {
				var content = "<div sd-activity-documents-popover-content></div>";
				return this.openDocumentsPopover($event, passedScope, content);
			}


			/**
			 * 
			 */
			ProcessActivityTableRenderer.prototype.openDocumentsPopover = function ($event, passedScope, popoverContent) {
				var template = $compile(popoverContent)(passedScope);
				var elem = jQuery($event.target);
				elem.addClass("popover-open");
				
				var popoverElem = elem.popover({
					"html" : true,
					trigger : 'focus',
					placement : "left",
					"content" : function () {
						return template;
					}
				}).popover("show").popover("hide");

				function showDocuments () {
					// Delay for positioning
					setTimeout(function () {
						popoverElem.popover("show");
						sdUtilService.safeApply(passedScope);
					});
				}
				return {
					showDocuments : showDocuments
				}
			}

			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.rowHandler = function(params) {
				var row = params.row;
				var rowData = params.rowData;
				var dataIndex = params.dataIndex; 
				var scope = params.scope; 

				dataMappingHandler(row, rowData, dataIndex, scope);
				markupCompiler.compile(row, rowData, dataIndex, scope);
			}



			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.drawHandler = function(params) {
				var table = params.table;
				var self = this;
				var containsDataColumn = templateCache['trivialManual']!== undefined;
				
				// Timeout makes loading of page appear faster
				window.setTimeout(function() {

					jQuery("[data-toggle=table-popover]", table).popover({
						html: true,
						content: function() {
							var content = jQuery(this).attr('data-popover-content');
							return jQuery(content).html();
						}
					});
					
					var currentView = jQuery(table).closest(".sg-selected");

					if(currentView.parent().hasClass("sg-view-panel")) {
						currentView.off('click').on('click', function (e) {
							jQuery('.document-popover.popover-open,.actions-popover.popover-open', table).each(function () {
								if (!jQuery(this).is(e.target) && jQuery(this).has(e.target).length === 0 && jQuery('.popover').has(e.target).length === 0) {
									jQuery(this).removeClass("popover-open")
									jQuery(this).popover('destroy');
								}
							});
							
							if(containsDataColumn) {
								self.worklistDataResetHandler(e);
							}
							
						});
					} else {
						trace.log('Couldnt register click handler on the view.');
					}
				});
			}
			
			/*
			 * 
			 */
			ProcessActivityTableRenderer.prototype.hasClass = function(params) {
				var table = params.table;
				var self = this;
				var containsDataColumn = templateCache['trivialManual']!== undefined;
				
				// Timeout makes loading of page appear faster
				window.setTimeout(function() {

					jQuery("[data-toggle=table-popover]", table).popover({
						html: true,
						content: function() {
							var content = jQuery(this).attr('data-popover-content');
							return jQuery(content).html();
						}
					});
					
					var currentView = jQuery(table).closest(".sg-selected");

					if(currentView.parent().hasClass("sg-view-panel")) {
						currentView.off('click').on('click', function (e) {
							jQuery('.document-popover.popover-open,.actions-popover.popover-open', table).each(function () {
								if (!jQuery(this).is(e.target) && jQuery(this).has(e.target).length === 0 && jQuery('.popover').has(e.target).length === 0) {
									jQuery(this).removeClass("popover-open")
									jQuery(this).popover('destroy');
								}
							});
							
							if(containsDataColumn) {
								self.worklistDataResetHandler(e);
							}
							
						});
					} else {
						trace.log('Couldnt register click handler on the view.');
					}
				});
			}
		}
	};
})();
