/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdDataTable', 
			['$parse', '$compile', '$timeout', 'sdUtilService', 'sdLoggerService', 'sdPreferenceService', DataTableDirective]);

	var trace;

	var CLASSES = {
		TABLE : 'tbl',
		TH : 'tbl-hdr-col',
		BODY_TR : 'tbl-row',
		TD : 'tbl-col',
		TOOLBAR	 : 'tbl-toolbar',
		ROW_SELECTED : 'tbl-row-selected' 
	};

	/*
	 * 
	 */
	function DataTableDirective($parse, $compile, $timeout, sdUtilService, sdLoggerService, sdPreferenceService) {
		trace = sdLoggerService.getLogger('bpm-common.sdDataTable');

		return {
			restrict : 'A',
			require: ['sdData'],
			scope: true,
			compile: function(elem, attr, transclude) {				
				processRawMarkup(elem, attr);

				return {
					post : function(scope, element, attr, ctrl) {
						var dataTableCompiler = new DataTableCompiler(
								$parse, $compile, $timeout, sdUtilService, sdPreferenceService, 
								scope, element, attr, ctrl);
					}
				};
			}
		};	
	}

	/*
	 * 
	 */
	function processRawMarkup(elem, attr) {
		// Add ng-non-bindable, so that the markup is not compiled
		var bodyCols = elem.find('> tbody > tr > td');
		bodyCols.attr('ng-non-bindable', '');
		
		elem.addClass('tbl');
		
		// Hide the element, till it's ready to be visible
		showElement(elem, false);
		var toolbar = elem.prev();
		if (toolbar.attr('sda-toolbar') != undefined) {
			showElement(toolbar, false);
		}
	}

	/*
	 * 
	 */
	function showElement(element, show) {
		if (show) {
			element.show();
		} else {
			element.hide();
		}
	}
	
	/*
	 * 
	 */
	function DataTableCompiler($parse, $compile, $timeout, sdUtilService, sdPreferenceService, 
			scope, element, attr, ctrl) {
		var TOOLBAR_TEMPLATE =
			'<div class="tbl-toolbar-section">\n' +
				'<button class="button-link" href="#" ng-if="$dtApi.enableSelectColumns" ng-click="$dtApi.toggleColumnSelector()"' + 
					' title="{{i18n(\'portal-common-messages.common-filterPopup-selectColumnsLabel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-table"></i>\n' +
				'</button>\n' +
				'<button class="button-link" href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asExcel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-file-excel-o"></i>\n' +
				'</button>\n' +
				'<button class="button-link" href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asCSV\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-file-text-o"></i>\n' +
				'</button>\n' +
				'<div ng-if="$dtApi.showSelectColumns" class="popup-dlg">\n' +
					'<div class="popup-dlg-hdr">\n' +
						'<span class="popup-dlg-hdr-txt">{{i18n("portal-common-messages.common-filterPopup-selectColumnsLabel")}}</span>\n' + 
						'<span class="popup-dlg-cls fa fa-lg fa-remove" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()"></span>\n' +
					'</div>\n' +
					'<div class="popup-dlg-cnt tbl-col-selector">\n' +
						'<div>\n' +
							'<span class="ui-section" ng-if="$dtApi.columnSelectorAdmin">\n' +
								'<span class="label-form">{{i18n(\'portal-common-messages.common-preferenceScope-label\')}}</span>\n' +
								'<select class="inp-sel-one" ng-model="$dtApi.applyTo" ng-change="$dtApi.applyToChanged()">\n' +
									'<option value="USER">{{i18n(\'portal-common-messages.common-preferenceScope-options-user\')}}</option>\n' +
									'<option value="PARTITION">{{i18n(\'portal-common-messages.common-preferenceScope-options-partition\')}}</option>\n' +
								'</select>\n' +
							'</span>\n' +
							'<button class="button-link" ng-if="$dtApi.columnSelectorAdmin" ng-click="$dtApi.toggleColumnSelectorLock()" ng-disabled="$dtApi.isColumnSelectorLockDisabled()">\n' +
								'<span class="fa fa-lg fa-lock" ng-show="$dtApi.lock"></span>\n' + 
								'<span class="fa fa-lg fa-unlock" ng-show="!$dtApi.lock"></span>\n' +
							'</button>\n' +
							'<button class="fa fa-lg fa-rotate-right button-link" ng-click="$dtApi.resetColumnSelector()" style="cursor: pointer;"></button>\n' +
						'</div>\n' +
						'<div class="tbl-col-sel-list">\n' +
							'<div ng-repeat="col in $dtApi.columns" class="tbl-col-sel-row" ng-model="$index" sd-data-drag sd-data-drop on-drop="$dtApi.moveColumns($data, $index, $event)">\n' +
								'<input type="checkbox" class="tbl-col-sel-input" ng-model="col.visible"></span>\n' +
								'<span class="tbl-col-sel-label" ng-if="col.labelKey">{{i18n(col.labelKey)}}</span>\n' +
								'<span class="tbl-col-sel-label" ng-if="!col.labelKey">{{col.label}}</span>\n' +
							'</div>\n' +
						'</div>\n' +
					'</div>\n' +
					'<div class="popup-dlg-footer">\n' +
						'<input type="submit" class="button primary" value="{{i18n(\'portal-common-messages.common-apply\')}}" ng-click="$dtApi.applyColumnSelector()" />' +
						'<input type="submit" class="button secondary" value="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()" />' +
					'</div>\n' +
				'</div>\n' +
			'</div>\n';

		var elemScope = scope.$parent;
		var myScope = scope;
		var sdData = ctrl[0];

		var initialized;
		var columns = [], dtColumns = [];
		var theTable, theTableId, theDataTable, theToolbar, theColReorder;
		var selectedRowIndexes = {}, rowSelectionMode = false, selectionBinding;
		var onSelect = {}, onPagination = {}, onColumnReorder = {};
		var enableColumnSelector, columnSelectorAdmin, columnsByDisplayOrder, columnsInfoByDisplayOrder, devColumnOrderPref;
		var columnSelectorPreference, localPrefStore = {};
		var pageSize = 8;
		
		// Setup component instance
		setup();

		/*
		 * 
		 */
		function setup() {
			// Assign Id
			if (!element.attr('id')) {
				element.attr('id', 'DT' + (Math.floor(Math.random() * 9000) + 1000));
			}
			theTableId = element.attr('id');

			if (attr.sdaReady) {
				trace.log(theTableId + ': Table defines sda-ready attribute, so deferring initialization...');
				var unregister = elemScope.$watch(attr.sdaReady, function(newVal, oldVal) {
					if(newVal === true) {
						trace.log(theTableId + ': sda-ready flag is triggered...');

						// Initialize after current digest cycle
						$timeout(initialize);
						unregister();
					}
				});
			} else {
				initialize();
			}
		}

		/*
		 * 
		 */
		function initialize() {
			trace.log(theTableId + ': Initializing Data Table...');

			try {
				processAttributes();

				validateMarkup();
	
				processMarkup();
	
				cleanupAsDirective();
				
				buildDataTableInformation();
	
				createDataTable();
				
				// We just changed the markup, so can proceed further only after current digest cycle
				$timeout(function() {
					try {
						buildDataTable();
					} catch (e) {
						trace.error(theTableId + ': Unexpected error occured while building table', e);
					}
				});
			} catch (e) {
				var msg = 'sd-data-table is unable to process table. Reason: ' + e;
				jQuery('<pre class="tbl-error">' + msg + '</pre>').insertAfter(element);
			}
		}

		/*
		 * 
		 */
		function processAttributes() {
			trace.log(theTableId + ': Processing table attributes...');

			if (attr.sdaPageSize != '') {
				pageSize = attr.sdaPageSize;
			}

			if (attr.sdaSelectable == undefined || attr.sdaSelectable == '') {
				rowSelectionMode = false;
			} else {
				if (attr.sdaSelectable === 'row') {
					rowSelectionMode = 'row';
				} else {
					rowSelectionMode = 'multiple';
				}

				if (attr.sdaSelection) {
					selectionBinding = $parse(attr.sdaSelection);

					elemScope.$watch(attr.sdaSelection, function(newVal, oldVal) {
						if (initialized) {
							var sel = getRowSelection();
							if(!angular.equals(newVal, sel)) {
								setRowSelection(newVal);
							}
						}
					});
				}

				if (attr.sdaOnSelect) {
					onSelect.handler = $parse(attr.sdaOnSelect);

					var onSelectFuncInfo = sdUtilService.parseFunction(attr.sdaOnSelect);
					if (onSelectFuncInfo && onSelectFuncInfo.params && onSelectFuncInfo.params.length > 0) {
						onSelect.param = onSelectFuncInfo.params[0];
					} else {
						trace.error(theTableId + ': sda-on-select does not seems to be correcly used, it does not appear to be a function accepting parameter.');
					}
				}
			}

			if (attr.sdaOnPagination) {
				onPagination.handler = $parse(attr.sdaOnPagination);

				var onPaginationfuncInfo = sdUtilService.parseFunction(attr.sdaOnPagination);
				if (onPaginationfuncInfo && onPaginationfuncInfo.params && onPaginationfuncInfo.params.length > 0) {
					onPagination.param = onPaginationfuncInfo.params[0];
				} else {
					trace.error(theTableId + ': sda-on-pagination does not seems to be correcly used, it does not appear to be a function accepting parameter.');
				}
			}

			if (attr.sdaColumnSelector && (attr.sdaColumnSelector == 'true' || attr.sdaColumnSelector == 'admin')) {
				enableColumnSelector = true;
				columnSelectorAdmin = attr.sdaColumnSelector == 'admin' ? true : false;

				if (attr.sdaOnColumnReorder) {
					onColumnReorder.handler = $parse(attr.sdaOnColumnReorder);

					var onColumnReorderfuncInfo = sdUtilService.parseFunction(attr.sdaOnColumnReorder);
					if (onColumnReorderfuncInfo && onColumnReorderfuncInfo.params && onColumnReorderfuncInfo.params.length > 0) {
						onColumnReorder.param = onColumnReorderfuncInfo.params[0];
					} else {
						trace.error(theTableId + ': sda-on-columns-reorder does not seems to be correcly used, it does not appear to be a function accepting parameter.');
					}
				}
			}
		}

		/*
		 *
		 */
		function cleanupAsDirective() {
			var currentDir = "sd-data-table: " + element.attr("sd-data-table");
			jQuery("<!-- " + currentDir + " -->").insertBefore(element);
			element.removeAttr("sd-data-table", "");
		}

		/*
		 * 
		 */
		function validateMarkup() {
			trace.log(theTableId + ': Validating table markup...');

			// Check Tag Name <table>
			var tagName = element.prop('tagName');
			sdUtilService.assert(tagName == 'TABLE', 'Directive must be defined on &lt;table&gt; element, but its defined on ' + tagName + '.');

			// Check <thead>
			var head = element.find('> thead');
			sdUtilService.assert(head.length == 1, '&lt;table&gt; must have at least and at most one &lt;thead&gt; defined.');

			// Check header row
			var headerRow = head.find('> tr');
			sdUtilService.assert(headerRow.length == 1, '&lt;thead&gt; must have at leat and at most one &lt;tr&gt; defined.');

			// Check <tbody>
			var body = element.find('> tbody');
			sdUtilService.assert(body.length == 1, '&lt;table&gt; must have at leat and at most one &lt;tbody&gt; defined.');

			var bodyRow = body.find('> tr');
			sdUtilService.assert(bodyRow.length == 1, '&lt;tbody&gt; must have at leat and at most one &lt;tr&gt; defined.');
		}

		/*
		 * 
		 */
		function processMarkup() {
			trace.log(theTableId + ': Processing table markup...');

			// Process Columns
			var head = element.find('> thead');
			var i18nScope = "";
			if (head.attr('sda-i18n-scope') != undefined) {
				i18nScope = head.attr('sda-i18n-scope');
			}

			var headCols = element.find('> thead > tr > th');
			var bodyCols = element.find('> tbody > tr > td');

			sdUtilService.assert(headCols.length == bodyCols.length, 'Number of columns in &lt;thead&gt; and &lt;tbody&gt; are not matching.');
			
			headCols.addClass(CLASSES.TH);

			columns = [];
			devColumnOrderPref = [];
			var columnsInfo = {};
			for(var i = 0; i < headCols.length; i++) {
				var hCol = angular.element(headCols[i]);
				var bCol = angular.element(bodyCols[i]);
				
				var colDef = {
					name: hCol.attr('sda-name'),
					field: bCol.attr('sda-field'),
					dataType: bCol.attr('sda-data-type'),
					visible: hCol.attr('sda-visible') == undefined || hCol.attr('sda-visible') == 'true' ? true : false,
					sortable: hCol.attr('sda-sortable') == 'true' ? true : false,
					fixed: hCol.attr('sda-fixed') != undefined && hCol.attr('sda-fixed') == 'true' ? true : false
				};

				if (!colDef.name) {
					trace.warn(theTableId + ': Column ' + (i + 1) + ' is missing attribute sda-name');
					colDef.name = 'column' + (i + 1); // Default
				}

				if (!colDef.field) {
					colDef.field = colDef.name;
				}
				
				if (hCol.attr('sda-label')) {
					colDef.label = hCol.attr('sda-label');
				} else {
					if (hCol.attr('sda-label-key')) {
						colDef.labelKey = hCol.attr('sda-label-key');
					} else {
						colDef.labelKey = colDef.name; // Default to name
					}

					if (i18nScope != '') {
						colDef.labelKey = i18nScope + '-' + colDef.labelKey;
					}
				}

				colDef.contents = bCol.html();
				colDef.contents = colDef.contents.trim();
				if (colDef.contents == "") {
					colDef.contents = getDefaultContent(colDef);
				}

				if (bCol.attr('style')) {
					colDef.cellStyle = bCol.attr('style');
				}

				if (bCol.attr('class')) {
					colDef.cellClass = bCol.attr('class');
				}

				columns.push(colDef);
				if (colDef.visible) {
					devColumnOrderPref.push(colDef.name);
				}

				columnsInfo[colDef.name] = {
					column : colDef,
					index : columns.length - 1
				};
			}

			columnsByDisplayOrder = columns;
			columnsInfoByDisplayOrder = columnsInfo;
			

			element.children('tbody').remove();
		}

		/*
		 * 
		 */
		function getDefaultContent(colDef) {
			var contents = '{{rowData.' + colDef.field + '}}';

			if (colDef.dataType === 'int') {
				contents = '{{rowData.' + colDef.field + ' | number}}';
			} else if (colDef.dataType === 'dateTime') {
				contents = '{{rowData.' + colDef.field + ' | date: "short"}}';
			} else if (colDef.dataType === 'date') {
				contents = '{{rowData.' + colDef.field + ' | date: "shortDate"}}';
			} else if (colDef.dataType === 'time') {
				contents = '{{rowData.' + colDef.field + ' | date: "shortTime"}}';
			}

			return contents;
		}

		/*
		 * 
		 */
		function buildDataTableInformation() {
			trace.log(theTableId + ': Building table information...');

			angular.forEach(columns, function(col, i) {
				dtColumns.push({
					data: colRenderer(col),
					sortable: col.sortable
				});
			});

			/*
			 * Need to have covering function to maintain outer scope for appropriate 'col' 
			 */
			function colRenderer(col) {
				return function(row, type, set) {
					if (type === 'display') {
						return col.contents;
					}
					
					return "";
				};
			}
		}

		/*
		 * 
		 */
		function createDataTable() {
			trace.log(theTableId + ': Creating table...');

			// Toolbar
			theToolbar = element.prev();
			if (theToolbar.attr('sda-toolbar') != undefined) {
				theToolbar.prepend(TOOLBAR_TEMPLATE);
			} else {
				var toolbarTemplate = '<div>' + TOOLBAR_TEMPLATE + '</div>';
				jQuery(toolbarTemplate).insertBefore(element);
				theToolbar = element.prev();
			}

			theToolbar = angular.element(theToolbar);
			theToolbar.addClass(CLASSES.TOOLBAR);

			// Compile the default toolbar, which was inserted
			var defaultToolbar = theToolbar.children().first();
			$compile(defaultToolbar)(defaultToolbar.scope());

			// Add Header Labels
			var headCols = element.find('> thead > tr > th');
			angular.forEach(columns, function(col, i) {
				var headerLabel = col.labelKey ? '{{i18n("' + col.labelKey + '")}}' : col.label;
				var columnHeader = '<span class="tbl-hdr-col-label">' + headerLabel + '</span>';

				var hCol = angular.element(headCols[i]);
				hCol.prepend(columnHeader);
				
				var header = hCol.children().first();
				$compile(header)(header.scope());
			});
			
			theTable = element;
			theTable.addClass('tbl');	
		}

		/*
		 * 
		 */
		function buildDataTable() {
			var dtOptions = {};

			dtOptions.hasOverrideDom = true;
			dtOptions.sDom = "itp";
			dtOptions.sPaginationType = "full_numbers";
			dtOptions.iDisplayLength = pageSize;
			dtOptions.language = {
				 "paginate": {
					 "first": "<<",
					 "previous": "<",
					 "next": ">",
					 "last": ">>"
				 }
			};
			
			dtOptions.columns = dtColumns;
			dtOptions.autoWidth = false;

			dtOptions.processing = true;

			// TODO: Datatables does not support single column sorting yet. What it has is only multi column sort 
			dtOptions.sort = attr.sdaSortable == 'true' || attr.sdaSortable == 'single' || attr.sdaSortable == 'multiple';
			if (dtOptions.sort) {
				var dtOrder = [];

				if (attr.sdaSortBy != undefined && attr.sdaSortBy != '') {
					var sortByGetter = $parse(attr.sdaSortBy);
					if (sortByGetter) {
						var orderBy = sortByGetter(elemScope);
						if (orderBy) {
							if (!angular.isArray(orderBy)) {
								orderBy = [orderBy];
							}
	
							for (var i in orderBy) {
								var columnInfo = columnsInfoByDisplayOrder[orderBy[i].name];
								if (columnInfo) {
									dtOrder.push([columnInfo.index, orderBy[i].dir == 'asc' ? 'asc' : 'desc']);
								}
							}
							
						}
					}
				}

				if (dtOrder.length == 0) {
					dtOrder.push([0, 'asc']);
				}

				dtOptions.order = dtOrder;
			}

			dtOptions.fnDrawCallback = drawCallbackHandler;
			dtOptions.fnCreatedRow = createRowHandler;

			if (attr.sdaMode == 'local') {
				buildDataTableLocalMode(dtOptions);
			} else {
				buildDataTableRemoteMode(dtOptions);
			}
		}

		/*
		 * 
		 */
		function buildDataTableRemoteMode(dtOptions) {
			trace.log(theTableId + ': Building table for remote mode...');

			dtOptions.serverSide = true;
			dtOptions.ajax = ajaxHandler;

			theDataTable = theTable.DataTable(dtOptions);
			buildDataTableCompleted();
		}

		/*
		 * 
		 */
		function buildDataTableLocalMode(dtOptions) {
			trace.log(theTableId + ': Building table for local mode...');

			fetchData(undefined, function(result) {
				try {
					dtOptions.data = result.list ? result.list : result;
	
					sdUtilService.assert(dtOptions.data && angular.isArray(dtOptions.data),
							'sd-data did not return acceptable result: Missing "list" or its not an array');
	
					if (attr.sdaNoPagination == '' || attr.sdaNoPagination == 'true') {
						dtOptions.iDisplayLength = dtOptions.data.length;
						dtOptions.sDom = 't';
					} else {
						// TODO: Undefine this for now! It causes wired issue with pagination
						dtOptions.iDisplayLength = undefined;
					}
					
					theDataTable = theTable.DataTable(dtOptions);
					buildDataTableCompleted();
				} catch (e) {
					trace.error(theTableId + ':', e);
				}
			});
		}

		/*
		 * 
		 */
		function ajaxHandler(data, callback, settings) {
			var ret = {
				"draw" : data.draw,
				"recordsTotal": 0,
				"recordsFiltered": 0,
				"data": null
			};

			var params = {skip : data.start, pageSize : data.length};
			if (data.order && data.order.length > 0) {
				params.order = [];
				for (var i in data.order) {
					var column = columnsByDisplayOrder[data.order[i].column];
					params.order.push({name: column.name, field: column.field, dir: data.order[i].dir});
				}
			}

			fetchData(params, function(result) {
				try {
					sdUtilService.assert(result.totalCount,
							'sd-data did not return acceptable result: Missing "totalCount"');
					sdUtilService.assert(result.list && angular.isArray(result.list),
							'sd-data did not return acceptable result: Missing "list" or its not an array');
					sdUtilService.assert(result.list.length <= data.length,
							'sd-data did not return acceptable result: Returned more records than expected (' + data.length + ')');
	
					ret.recordsTotal = result.totalCount;
					ret.recordsFiltered = result.totalCount;
					ret.data = result.list;
	
					callback(ret);
				} catch (e) {
					trace.error(theTableId + ':', e);
				}
			});
		}

		/*
		 * 
		 */
		function buildDataTableCompleted() {
			// Initialization handler
			if (attr.sdaMode == 'local') {
				firePaginationEvent();
			} else {
				theDataTable.on('init.dt', function() {
					firePaginationEvent();
				});
			}
			
			// Register for pagination events
			theDataTable.on('page.dt', firePaginationEvent);

			if (enableColumnSelector) {
				theColReorder = new jQuery.fn.dataTable.ColReorder(theDataTable);
			}

			exposeAPIs();
		}

		/*
		 * 
		 */
		function firePaginationEvent() {
			// Invoke the event handler when processing is complete and data is displayed
			$timeout(function() {
				var info = theDataTable.page.info();
				var paginationInfo = {
					currentPage: info.page + 1,
					totalPages: info.pages
				};
				
				fireDataTableEvent(onPagination, paginationInfo, 'onPagination');
			}, 0, true);
		}

		/*
		 * 
		 */
		function fireDataTableEvent(handleInfo, data, eventType, invokeAfterDigest) {
			if (handleInfo.handler) {
				try {
					var transObj = {};
					if (handleInfo.param) {
						transObj[handleInfo.param] = data;
					} else {
						transObj.info = data;
						trace.warn(theTableId + ': ' + eventType + ' event handler is not properly configured, may not receive event info.');
					}

					if (!invokeAfterDigest) {
						return handleInfo.handler(elemScope, transObj);
					} else {
						$timeout(function() {
							handleInfo.handler(elemScope, transObj);
						}, 0, true);
					}
				} catch(e) {
					trace.error(theTableId + ': Error while firing ' + eventType + ' event on data table', e);
				}
			}
		}
		
		/*
		 * 
		 */
		function fetchData(params, successCallback, errorCallback) {
			var dataResult = sdData.retrieveData(params);
			dataResult.then(function(result) {
				successCallback(result);
			}, function(error) {
				if (errorCallback == undefined) {
					// TODO
				} else {
					errorCallback(error);
				}
		    });
		}

		/*
		 * 
		 */
		function createRowHandler(row, data, dataIndex) {
			var row = angular.element(row);
			row.addClass(CLASSES.BODY_TR);
			
			var cells = row.find('> td');
			cells.addClass(CLASSES.TD);
			angular.forEach(cells, function(cell, i) {
				cell = angular.element(cell);

				if (columns[i].cellClass && columns[i].cellClass != '') {
					var value = cell.attr('class');
					value = value ? (value.trim() + ' ') : '';
					value += columns[i].cellClass;
					cell.attr('class', value);
				}

				if (columns[i].cellStyle && columns[i].cellStyle != '') {
					var value = cell.attr('style');
					value = value ? (value.trim()) : '';
					if (value.length > 1 && value.substr(value.length - 1) != ';') {
						value += '; ';
					}
					value += columns[i].cellStyle;
					cell.attr('style', value);
				}
			});

			var rowScope = row.scope();
			if (rowScope == undefined) {
				rowScope = myScope.$new();
			}

			rowScope.rowData = data;
			rowScope.$index = dataIndex;
			rowScope.$first = (dataIndex === 0);
			//rowScope.$last = (dataIndex === (value.length - 1));
			//rowScope.$middle = !(rowScope.$first || rowScope.$last);
			rowScope.$odd = !(rowScope.$even = (dataIndex & 1) === 0);
			
			$compile(row)(rowScope);
		}

		/*
		 * 
		 */
		function drawCallbackHandler (oSettings) {
			// Table is not yet created, wait for it.
			if (!theDataTable) {
				$timeout(function() {
					drawCallbackHandler (oSettings);
				}, 0, false);
				return;
			}

			if(!initialized) {
				// Show the element, as it's ready to be visible
				showElement(element, true);
				if (theToolbar) {
					showElement(theToolbar, true);
				}

				enableRowSelection();

				$timeout(function() {
					reorderColumns(null, null, false);
					doInitialSelection();
					
					exposeScopeInfo();
				}, 0, false);

				initialized = true;
			} else {
				clearState();
				showElement(theTable, true);
			}
		
			sdUtilService.safeApply(elemScope);
		}

		/*
		 * 
		 */
		function getPreferenceDelegate(pScope) {
			var preferenceDelegate = {};

			if (attr.sdaPreferenceDelegate) {
				var funcInfo = sdUtilService.parseFunction(attr.sdaPreferenceDelegate);
				if (funcInfo && funcInfo.params && funcInfo.params.length > 0) {
					var preferenceDelagate = {
						handler : $parse(attr.sdaPreferenceDelegate),
						param : funcInfo.params[0]
					}
					var data = {
						scope : pScope
					}

					preferenceDelegate.store = fireDataTableEvent(preferenceDelagate, data, 'PreferenceDelegate');					
				} else {
					sdUtilService.assert(false, 'sda-preference-delegate does not seems to be correcly used, it does not appear to be a function accepting parameter.');
				}
			} else if(attr.sdaPreferenceModule && attr.sdaPreferenceModule != '' && 
					attr.sdaPreferenceId && attr.sdaPreferenceId != '' &&
					attr.sdaPreferenceName && attr.sdaPreferenceName != '') {
				preferenceDelegate.store = sdPreferenceService.getStore(pScope, attr.sdaPreferenceModule, attr.sdaPreferenceId);
				preferenceDelegate.name = attr.sdaPreferenceName;
			}

			return preferenceDelegate;
		}

		/*
		 * 
		 */
		function getColumnSelectionFromPreference(pScope) {
			var prefValue = undefined;

			pScope = !pScope ? 'USER' : pScope;
			var preferenceDelegate = getPreferenceDelegate(pScope);
			if (preferenceDelegate.store) {
				if (pScope == 'USER' && !columnSelectorAdmin) {
					var parentPrefValue = preferenceDelegate.store.getValue(preferenceDelegate.name, true);
					parentPrefValue = marshalPreferenceValue(parentPrefValue);
					if (parentPrefValue && parentPrefValue.lock) {
						prefValue = parentPrefValue;
					}
				}
				if (prefValue == undefined) {
					prefValue = preferenceDelegate.store.getValue(preferenceDelegate.name);
					prefValue = marshalPreferenceValue(prefValue);
				}
			} else {
				prefValue = localPrefStore[pScope];
			}

			if (prefValue == undefined || prefValue == null) {
				prefValue = {
					selectedColumns : devColumnOrderPref,
					lock : false
				};
			}

			prefValue.scope = pScope;

			return prefValue;
		}

		/*
		 * 
		 */
		function marshalPreferenceValue(prefValue) {
			if(prefValue) {
				try {
					prefValue = JSON.parse(prefValue);
				} catch(e) {
					// Backward compatibility
					var prefColumns = prefValue.split('$#$');
					prefValue = {
						selectedColumns : prefColumns,
						lock : false
					};
				}
			}
			return prefValue;
		}
		
		/*
		 * 
		 */
		function setColumnSelectionFromPreference(pScope, value) {
			pScope = !pScope ? 'USER' : pScope;
			var preferenceDelegate = getPreferenceDelegate(pScope);
			if (preferenceDelegate.store) {
				preferenceDelegate.store.setValue(preferenceDelegate.name, value);
				preferenceDelegate.store.save();
			} else {
				localPrefStore[pScope] = value;
			}
		}

		/*
		 * 
		 */
		function reorderColumns(pScope, preview, skipVisibility) {
			if (!enableColumnSelector) {
				return;
			}

			var columnDisplayOrderIndexes = [], columnDisplayOrderNames = [], columnDisplayOrderObjects = [];

			var currentOrder = columnsByDisplayOrder;
			
			var currentColumnOrder = [];
			angular.forEach(currentOrder, function(col, i) {
				if (!col.fixed && col.visible) {
					currentColumnOrder.push(col.name);
				}
			});

			// Add fixed columns
			var fixedAfter = [], flag = true;
			angular.forEach(currentOrder, function(col, i) {
				if (col.fixed) {
					 if (flag) {
						 columnDisplayOrderIndexes.push(i);
						 columnDisplayOrderNames.push(col.name);
						 columnDisplayOrderObjects.push(currentOrder[i]);
						 col.visible = true;
					 } else {
						 fixedAfter.push(col.name);
					 }
				} else {
					flag = false;
				}
			});
			
			// Add preference columns
			var columnSelectorPref = getColumnSelectionFromPreference(pScope);
			
			var prefCols = columnSelectorPref.selectedColumns;
			if (prefCols) {
				angular.forEach(prefCols, function(colName, i) {
                    var colInfo = columnsInfoByDisplayOrder[colName];
                    if (!colInfo) {
                    	// Find column using case insensitive nature
    					var colNameToFind = colName.toLowerCase();
    					for (var name in columnsInfoByDisplayOrder) {
    						if (name.toLowerCase() == colNameToFind) {
    							colInfo = columnsInfoByDisplayOrder[name];
    							colName = name;
    							break;
    						}
    					}                    	
                    }

					if (colInfo != undefined && 
							columnDisplayOrderNames.indexOf(colName) == -1 && fixedAfter.indexOf(colName) == -1) {
						columnDisplayOrderIndexes.push(colInfo.index);
						columnDisplayOrderNames.push(colName);
						if (!preview) {					
							columnDisplayOrderObjects.push(colInfo.column);
							colInfo.column.visible = true;
						} else {
							columnDisplayOrderObjects.push({
								name: colInfo.column.name,
								visible: true,
								labelKey: colInfo.column.labelKey,
								label: colInfo.column.label
							});
						}
					}
				});
			}

			// Add remaining columns
			angular.forEach(currentOrder, function(col, i) {
				if (columnDisplayOrderNames.indexOf(col.name) == -1 && fixedAfter.indexOf(col.name) == -1) {
					columnDisplayOrderIndexes.push(i);
					columnDisplayOrderNames.push(col.name);

					if (!preview) {
						columnDisplayOrderObjects.push(col);
						if (prefCols) {
							col.visible = false;
						}
					} else {
						columnDisplayOrderObjects.push({
							name: col.name,
							visible: prefCols ? false : col.visible,
							labelKey: col.labelKey,
							label: col.label
						});
					}
				}
			});

			// Add fixed columns to display order
			angular.forEach(fixedAfter, function(colName, i) {
				var colInfo = columnsInfoByDisplayOrder[colName];
				columnDisplayOrderIndexes.push(colInfo.index);
				columnDisplayOrderNames.push(colName);
				columnDisplayOrderObjects.push(colInfo.column);
				colInfo.column.visible = true;
			});

			if (preview) {
				return {
					lock: columnSelectorPref.lock,
					columns: columnDisplayOrderObjects
				}
			} else {
				columnSelectorPreference = columnSelectorPref;

				// ReOrder columns
				theColReorder.fnOrder(columnDisplayOrderIndexes);

				// Build & preserve info for further use
				columnsByDisplayOrder = columnDisplayOrderObjects;
				columnsInfoByDisplayOrder = {};

				var newColumnOrder = [];
				angular.forEach(columnsByDisplayOrder, function(col, i) {
					// Show / Hide columns based on new reorder
					var tableCol = theDataTable.column(i);
					if (tableCol.visible() != col.visible) {
						tableCol.visible(col.visible);
					}
					
					columnsInfoByDisplayOrder[col.name] = {
						index: i,
						column: col
					}

					if (!col.fixed && col.visible) {
						newColumnOrder.push(col.name);
					}
				});

				if (!skipVisibility) {
					showElement(theTable, false);
				}
				theDataTable.draw(false);

				// Fire Event
				fireColumnReorderEvent(currentColumnOrder, newColumnOrder);
			}
		}

		/*
		 * 
		 */
		function fireColumnReorderEvent(previousOrder, newOrder) {
			var columnReorderInfo = {
				previous : previousOrder,
				current : newOrder
			};
			
			fireDataTableEvent(onColumnReorder, columnReorderInfo, 'onColumnReorder', true);
		}

		/*
		 * 
		 */
		function clearState() {
			unselectRows();
			processSelectionBinding();
		}

		/*
		 * 
		 */
		function getPageData(index) {
			var tableData = theDataTable.data();

			if (index == undefined || index == null) {
				var data = [];
				for (var i = 0; i < tableData.length; i++) {
					data.push(tableData[i]);
				}
				return data;
			} else {
				return tableData[index];
			}
		}

		/*
		 * 
		 */
		function getPageDataCount(index) {
			var info = theDataTable.page.info();
			return info.end - info.start;
		}

		/*
		 * 
		 */
		function doInitialSelection() {
			if (attr.sdaInitialSelection != undefined && attr.sdaInitialSelection != '') {
				var initSelectionGetter = $parse(attr.sdaInitialSelection);
				var initSelection = initSelectionGetter(elemScope);

				setRowSelection(initSelection);
			}

			if (selectionBinding) {
				var sel = selectionBinding(elemScope);
				setRowSelection(sel);	
			}
		}

		/*
		 * 
		 */
		function enableRowSelection() {
			if (rowSelectionMode) {
				theTable.find('> tbody').on('click', '> tr', function() {
					processRowSelection(this);
					processSelectionBinding();
				});
			}
		}

		/*
		 * 
		 */
		function processSelectionBinding() {
			if (selectionBinding && selectionBinding.assign) {
				// Process once current processing is done
				$timeout(function() {
					var sel = getRowSelection();
					selectionBinding.assign(elemScope, sel);

					sdUtilService.safeApply(elemScope.$parent);
				}, 0, false);
			} else {
				sdUtilService.safeApply(elemScope.$parent);
			}
		}

		/*
		 * 
		 */
		function setRowSelection(data) {
			unselectRows();

			if (!angular.isArray(data)) {
				data = data ? [data] : [];
			}

			for(var i = 0; i < data.length; i++) {
				var row = findRowByData(data[i]);
				if (row) {
					processRowSelection(row);
				}
			}

			processSelectionBinding();
		}

		/*
		 * 
		 */
		function getRowSelection() {
			if (rowSelectionMode) {
				var selection = [];
				angular.forEach(selectedRowIndexes, function(value, key) {
					selection.push(getPageData(value));
				});
	
				if (rowSelectionMode == 'row') {
					if (selection.length == 0) {
						selection = null;
					} else {
						selection = selection[0];
					}
				}
				
				return selection;
			} else {
				return null;
			}
		}

		/*
		 * 
		 */
		function findRowByData(rowData) {
			var count = getPageDataCount();
			if (count > 0) {
				var rows = theTable.find('> tbody > tr');
				for(var i = 0; i < count; i++) {
					if (isObjectLike(getPageData(i), rowData)) {
						return rows[i];
					}
				}
			}

			return null;
		}

		/*
		 * 
		 */
		function isObjectLike(objBase, obj) {
			for (var member in obj) {
				if (member.indexOf('$') != 0) {
					if (obj[member] != objBase[member]) {
						return false;
					}
				}
			}

			return true;
		}

		/*
		 * 
		 */
		function processRowSelection(row) {
			row = angular.element(row);
			var rowScope = row.scope();

			var selectionInfo = {};
			selectionInfo.current = getPageData(rowScope.$index);

			if (rowSelectionMode == 'row') {
				if (isRowSelected(row)) {
					unselectRow(row, rowScope.$index);
					selectionInfo.action = 'deselect';
				} else {
					var prevSelRow = getSelectedRow();
					if (prevSelRow) {
						prevSelRow = angular.element(prevSelRow);
						unselectRow(prevSelRow, prevSelRow.scope().$index);
					}

					selectRow(row, rowScope.$index);
					selectionInfo.action = 'select';
				}
			} else {
				if (isRowSelected(row)) {
					unselectRow(row, rowScope.$index);
					selectionInfo.action = 'deselect';
				} else {
					selectRow(row, rowScope.$index);
					selectionInfo.action = 'select';
				}
			}

			selectionInfo.all = getRowSelection();

			fireDataTableEvent(onSelect, selectionInfo, 'onSelect');
		}

		/*
		 * 
		 */
		function selectRow(row, index) {
			row.addClass(CLASSES.ROW_SELECTED);
			selectedRowIndexes['Row' + index] = index;
		}

		/*
		 * 
		 */
		function isRowSelected(row) {
			return row.hasClass(CLASSES.ROW_SELECTED);
		}

		/*
		 * 
		 */
		function getSelectedRow() {
			var selRow = theTable.find('> tbody > tr.' + CLASSES.ROW_SELECTED);
			return (selRow.length != 0) ? selRow : null;
		}

		/*
		 * 
		 */
		function unselectRow(row, index) {
			row.removeClass(CLASSES.ROW_SELECTED);
			delete selectedRowIndexes['Row' + index];
		}

		/*
		 * 
		 */
		function unselectRows() {
			var rows = theTable.find('> tbody > tr');
			rows.removeClass(CLASSES.ROW_SELECTED);
			selectedRowIndexes = {};
		}

		/*
		 * 
		 */
		function exposeAPIs() {
			if (attr.sdDataTable != undefined && attr.sdDataTable != "") {
				var dataTableAssignable = $parse(attr.sdDataTable).assign;
				if (dataTableAssignable) {
					trace.info(theTableId + ': Exposing API for: ' + attr.sdDataTable + ', on scope Id: ' + elemScope.$id + ', Scope:', elemScope);
					dataTableAssignable(elemScope, new DataTable());
				} else {
					trace.error(theTableId + ': Could not expose API for: ' + attr.sdDataTable + ', expression is not an assignable.');
				}
			}
		}

		/*
		 * 
		 */
		function exposeScopeInfo() {
			// Need to add into parent scope as Toolbar belongs to parent scope 
			elemScope.$dtApi= new ScopeAPI();
		}

		/*
		 * Public API
		 */
		function DataTable() {
			this.instance = theDataTable;
			this.ready = true;

			/*
			 * 
			 */
			this.refresh = function (retainPageIndex) {
				var oSettings = theDataTable.settings()[0];
				jQuery(oSettings.oInstance).trigger('page', oSettings);
			    oSettings.oApi._fnCalculateEnd(oSettings);
			    oSettings.oApi._fnDraw(oSettings);
			};

			/*
			 * For single select - rowData or null if none selected
			 * For multiple select - rowData array or empty array if none selected 
			 */
			this.getSelection = function() {
				return getRowSelection();
			};

			/*
			 * 
			 */
			this.setSelection = function(data) {
				setRowSelection(data);
			};
		}

		/*
		 * Scope API
		 */
		function ScopeAPI() {
			var self = this;

			init();

			function init() {
				self.columns = [];
				self.enableSelectColumns = enableColumnSelector && (columnSelectorAdmin || !columnSelectorPreference.lock);
				self.columnSelectorAdmin = columnSelectorAdmin;
				self.showSelectColumns = false;
				self.applyTo = 'USER';
			}
			
			/*
			 * 
			 */
			this.toggleColumnSelector = function() {
				self.showSelectColumns = !self.showSelectColumns;

				self.lock = columnSelectorPreference.lock;
				self.applyTo = columnSelectorPreference.scope;
				self.columns = getSelectableColumns(columnsByDisplayOrder);
			}

			/*
			 * 
			 */
			this.applyColumnSelector = function() {
				var selectedCols = [];
				angular.forEach(self.columns, function(col, i) {
					if (col.visible) {
						selectedCols.push(col.name);
					}
				});
				
				var prefValue = {
					selectedColumns : selectedCols,
					lock : self.lock
				};

				setColumnSelectionFromPreference(self.applyTo, prefValue);
				reorderColumns(self.applyTo);
				self.toggleColumnSelector();
			}

			/*
			 * 
			 */
			this.applyToChanged = function() {
				var reorderInfo = reorderColumns(self.applyTo, true);
				self.columns = getSelectableColumns(reorderInfo.columns);
				self.lock = reorderInfo.lock;
			}

			/*
			 * 
			 */
			this.moveColumns = function(fromIndex, toIndex, event) {
				fromIndex = parseInt(fromIndex);
				toIndex = parseInt(toIndex);

				if (fromIndex != toIndex) {
					var dragItems = self.columns.splice(fromIndex, 1);
					self.columns.splice(toIndex, 0, dragItems[0]);	
					sdUtilService.safeApply(elemScope);
				}
			}

			/*
			 * 
			 */
			this.toggleColumnSelectorLock = function() {
				self.lock = !self.lock;				
			}

			/*
			 * 
			 */
			this.isColumnSelectorLockDisabled = function() {
				return self.applyTo == 'USER';
			}

			/*
			 * 
			 */
			this.resetColumnSelector = function() {
				setColumnSelectionFromPreference(self.applyTo, null);
				reorderColumns(self.applyTo);
				self.toggleColumnSelector();
			}

			/*
			 * 
			 */
			function getSelectableColumns(cols) {
				var selectableCols = [];
				angular.forEach(cols, function(col, i) {
					if (!col.fixed) {
						selectableCols.push({
							name: col.name,
							visible: col.visible,
							labelKey: col.labelKey,
							label: col.label
						});
					}
				});

				return selectableCols;
			}
		}
	};
})();