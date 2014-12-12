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
			['$parse', '$compile', '$timeout', 'sdUtilService', 'sdLoggerService', DataTableDirective]);

	var trace;

	var CLASS_ROW_SELECTED = "tbl-row-selected";

	/*
	 * 
	 */
	function DataTableDirective($parse, $compile, $timeout, sdUtilService, sdLoggerService) {
		trace = sdLoggerService.getLogger('bpm-common.sdDataTable');

		return {
			restrict : 'A',
			require: ['sdData'],
			scope: true,
			compile: function(elem, attr, transclude) {				
				processRawMarkup(elem, attr);

				return {
					post : function(scope, element, attr, ctrl) {
						var dataTableCompiler = new DataTableCompiler($parse, $compile, $timeout, sdUtilService, scope, element, attr, ctrl);
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
	function DataTableCompiler($parse, $compile, $timeout, sdUtilService, scope, element, attr, ctrl) {
		var TOOLBAR_TEMPLATE =
			'<div class="tbl-toolbar-section">\n' +
				'<a href="#" ng-click="$dtApi.toggleColumnSelector()"' + 
					' title="{{i18n(\'portal-common-messages.common-filterPopup-selectColumnsLabel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-table"></i>\n' +
				'</a>\n' +
				'<a href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asExcel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-file-excel-o"></i>\n' +
				'</a>\n' +
				'<a href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asCSV\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
					'<i class="fa fa-file-text-o"></i>\n' +
				'</a>\n' +
				'<div ng-show="$dtApi.showSelectColumns" class="popup-dlg">\n' +
					'<div class="popup-dlg-hdr">\n' +
						'<span class="popup-dlg-hdr-txt">{{i18n("portal-common-messages.common-filterPopup-selectColumnsLabel")}}</span>\n' + 
						'<span class="popup-dlg-cls fa fa-lg fa-remove" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()"></span>\n' +
					'</div>\n' +
					'<div class="popup-dlg-cnt">\n' +
						'<div class="tbl-col-sel-list">\n' +
							'<div ng-repeat="col in $dtApi.columns" class="tbl-col-sel-row">\n' +
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

		var columns = [], dtColumns = [], theTable, theDataTable, theToolbar;

		var selectedRowIndexes = {}, rowSelectionMode = false, selectionBinding;
		
		var onSelect = {}, onPagination = {};
		
		var forceLocalRefresh = false;

		var pageSize = 8;
		
		// Setup component instance
		setup();

		/*
		 * 
		 */
		function setup() {
			if (attr.sdaReady) {
				var unregister = elemScope.$watch(attr.sdaReady, function(newVal, oldVal) {
					if(newVal === true) {
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
			try {
				processAttributes();

				validateMarkup();
	
				processMarkup();
	
				cleanupAsDirective();
				
				buildDataTableInformation();
	
				createDataTable();
				
				// We just changed the markup, so can proceed further only after current digest cycle
				$timeout(function() {
					buildDataTable();
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
						trace.error('sda-on-select does not seems to be correcly used, it does not appear to be a function accepting parameter.');
					}
				}
			}

			if (attr.sdaOnPagination) {
				onPagination.handler = $parse(attr.sdaOnPagination);

				var onPaginationfuncInfo = sdUtilService.parseFunction(attr.sdaOnPagination);
				if (onPaginationfuncInfo && onPaginationfuncInfo.params && onPaginationfuncInfo.params.length > 0) {
					onPagination.param = onPaginationfuncInfo.params[0];
				} else {
					trace.error('sda-on-pagination does not seems to be correcly used, it does not appear to be a function accepting parameter.');
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
			// Process Columns
			var head = element.find('> thead');
			var i18nScope = "";
			if (head.attr('sda-i18n-scope') != undefined) {
				i18nScope = head.attr('sda-i18n-scope');
			}

			var headCols = element.find('> thead > tr > th');
			var bodyCols = element.find('> tbody > tr > td');

			sdUtilService.assert(headCols.length == bodyCols.length, 'Number of columns in &lt;thead&gt; and &lt;tbody&gt; are not matching.');
			
			headCols.addClass('tbl-hdr-col');
			
			for(var i = 0; i < headCols.length; i++) {
				var hCol = angular.element(headCols[i]);
				var bCol = angular.element(bodyCols[i]);
				
				var colDef = {
					field: bCol.attr('sda-field'),
					dataType: bCol.attr('sda-data-type'),
					visible: hCol.attr('sda-visible') == undefined || hCol.attr('sda-visible') == 'true' ? true : false,
					sortable: hCol.attr('sda-sortable'),
					fixed: hCol.attr('sda-fixed') != undefined && hCol.attr('sda-fixed') == 'true' ? true : false
				};

				if (hCol.attr('sda-label-key') != undefined) {
					if (i18nScope != "") {
						colDef.labelKey = i18nScope + '-' + hCol.attr('sda-label-key');
					} else {
						colDef.labelKey = hCol.attr('sda-label-key');
					}
				} else {
					if (hCol.attr('sda-label') != undefined) {
						colDef.label = hCol.attr('sda-label');
					} else {
						// Some default value, label is now mandatory due to column selector.
						colDef.label = 'Column ' + i;
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
			}

			element.children('tbody').remove();
			
			// TODO: Provide API to rearrange the columns
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
			angular.forEach(columns, function(col, i) {
				dtColumns.push({
					data: colRenderer(col)
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
			theToolbar.addClass('tbl-toolbar');

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
			dtOptions.serverSide = true;
			dtOptions.ajax = ajaxHandler;

			theDataTable = theTable.DataTable(dtOptions);
			buildDataTableCompleted();
		}

		/*
		 * 
		 */
		function buildDataTableLocalMode(dtOptions) {
			fetchData(undefined, function(result) {
				dtOptions.data = result.list ? result.list : result;

				if (attr.sdaNoPagination == '' || attr.sdaNoPagination == 'true') {
					dtOptions.iDisplayLength = dtOptions.data.length;
					dtOptions.sDom = 't';
				} else {
					// TODO: Undefine this for now! It causes wired issue with pagination
					dtOptions.iDisplayLength = undefined;
				}
				
				theDataTable = theTable.DataTable(dtOptions);
				buildDataTableCompleted();
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

			if (forceLocalRefresh) {
				forceLocalRefresh = false;

				var info = theDataTable.page.info();

				ret.recordsTotal = info.recordsTotal;
				ret.recordsFiltered = info.recordsTotal;
				ret.data = getPageData();

				callback(ret);
			} else {
				var params = {skip: data.start, pageSize: data.length};

				fetchData(params, function(result) {
					ret.recordsTotal = result.totalCount;
					ret.recordsFiltered = result.totalCount;
					ret.data = result.list;

					callback(ret);
				});
			}
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

			exposeAPIs();
			exposeScopeInfo();
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
		function fireDataTableEvent(handleInfo, data, eventType) {
			if (handleInfo.handler) {
				try {
					var transObj = {};
					if (handleInfo.param) {
						transObj[handleInfo.param] = data;
					} else {
						transObj.info = data;
						trace.warning(eventType + ' event handler is not properly configured, may not receive event info.');
					}

					handleInfo.handler(elemScope, transObj);
				} catch(e) {
					trace.error('Error while firing ' + eventType + ' event on data table', e);
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
			row.addClass('tbl-row');
			
			var cells = row.find('> td');
			cells.addClass('tbl-col');
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
					doInitialSelection();
				}, 0, false);

				initialized = true;
			} else {
				clearState();
			}
		
			showHideColumns(false);

			sdUtilService.safeApply(elemScope);
		}

		/*
		 * 
		 */
		function showHideColumns(updateUI) {
			angular.forEach(columns, function(col, i) {
				var tableCol = theDataTable.column(i);
				if (tableCol.visible() != col.visible) {
					tableCol.visible(col.visible);
				}
			});

			if (updateUI) {
				forceLocalRefresh = true;
				theDataTable.draw(false);
			}
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
			row.addClass(CLASS_ROW_SELECTED);
			selectedRowIndexes['Row' + index] = index;
		}

		/*
		 * 
		 */
		function isRowSelected(row) {
			return row.hasClass(CLASS_ROW_SELECTED);
		}

		/*
		 * 
		 */
		function getSelectedRow() {
			var selRow = theTable.find('> tbody > tr.' + CLASS_ROW_SELECTED);
			return (selRow.length != 0) ? selRow : null;
		}

		/*
		 * 
		 */
		function unselectRow(row, index) {
			row.removeClass(CLASS_ROW_SELECTED);
			delete selectedRowIndexes['Row' + index];
		}

		/*
		 * 
		 */
		function unselectRows() {
			var rows = theTable.find('> tbody > tr');
			rows.removeClass(CLASS_ROW_SELECTED);
			selectedRowIndexes = {};
		}

		/*
		 * 
		 */
		function exposeAPIs() {
			if (attr.sdDataTable != undefined && attr.sdDataTable != "") {
				var dataTableAssignable = $parse(attr.sdDataTable).assign;
				if (dataTableAssignable) {
					trace.info('Exposing API for: ' + attr.sdDataTable + ', on scope Id: ' + elemScope.$id + ', Scope:', elemScope);
					dataTableAssignable(elemScope, new DataTable());
				} else {
					trace.error('Could not expose API for: ' + attr.sdDataTable + ', expression is not an assignable.');
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
				var selectableCols = [];
				angular.forEach(columns, function(col, i) {
					if (!col.fixed) {
						selectableCols.push(col);
					}
				});

				self.columns = selectableCols;
				self.showSelectColumns = false;
			}
			
			/*
			 * 
			 */
			this.toggleColumnSelector = function() {
				self.showSelectColumns = !self.showSelectColumns;
			}

			/*
			 * 
			 */
			this.applyColumnSelector = function() {
				showHideColumns(true);
				self.toggleColumnSelector();
			}
		}
	};
})();