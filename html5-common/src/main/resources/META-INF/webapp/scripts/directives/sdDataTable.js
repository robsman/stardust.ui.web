/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Subodh.Godbole
 */

(function(){
	'use strict';

	angular.module('bpm-common').directive('sdDataTable', 
			['$parse', '$q', '$compile', '$timeout', 'sgI18nService', 'sdUtilService', 'sdLoggerService', 'sdPreferenceService',
			 DataTableDirective]);

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
	function DataTableDirective($parse, $q, $compile, $timeout, sgI18nService, sdUtilService, sdLoggerService, sdPreferenceService) {
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
								$parse, $q, $compile, $timeout, sgI18nService, sdUtilService, sdPreferenceService, 
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

		var headCols = elem.find('> thead > tr > th');
		angular.forEach(headCols, function(hCol) {
			hCol = angular.element(hCol);
			var filterTemplate = hCol.find('> [sda-filter-template]');
			if (filterTemplate && filterTemplate.length > 0) {
				filterTemplate.attr('ng-non-bindable', '');
			}
		});
		
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
	function DataTableCompiler($parse, $q, $compile, $timeout, sgI18nService, sdUtilService, sdPreferenceService, 
			scope, element, attr, ctrl) {
		var TOOLBAR_TEMPLATE =
			'<div class="tbl-toolbar-section">\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableSelectColumns" ng-click="$dtApi.toggleColumnSelector()"' + 
					' title="{{i18n(\'portal-common-messages.common-filterPopup-selectColumnsLabel\')}}">\n' +
					'<i class="glyphicon glyphicon-th"></i>\n' +
				'</button>\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableExportExcel" ng-click=""' +
					' title="{{i18n(\'portal-common-messages.common-genericDataTable-asExcel\')}}">\n' +
					'<i class="glyphicon glyphicon-export"></i>\n' +
				'</button>\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableExportCSV" ng-click="$dtApi.exportCSV({allRows: false, allCols: false})"' +
					' title="{{i18n(\'portal-common-messages.common-genericDataTable-asCSV\')}}" style="margin-right: 0px;">\n' +
					'<i class="glyphicon glyphicon-export"></i>\n' +
				'</button>\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableExportCSV" ng-click="$dtApi.toggleExportOptions()">\n' +
					'<i class="glyphicon glyphicon-chevron-right glyphicon-rotate-90"></i>\n' +
				'</button>\n' +
				'<div ng-if="$dtApi.showSelectColumns" class="popup-dlg">\n' +
					'<div class="popup-dlg-hdr">\n' +
						'<span class="popup-dlg-hdr-txt">{{i18n("portal-common-messages.common-filterPopup-selectColumnsLabel")}}</span>\n' + 
						'<span class="popup-dlg-cls glyphicon glyphicon-remove" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()"></span>\n' +
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
								'<span class="glyphicon glyphicon-ban-circle" ng-show="$dtApi.lock"></span>\n' + 
								'<span class="glyphicon glyphicon-ok-circle" ng-show="!$dtApi.lock"></span>\n' +
							'</button>\n' +
							'<button class="glyphicon glyphicon-repeat button-link" ng-click="$dtApi.resetColumnSelector()" style="cursor: pointer;"></button>\n' +
						'</div>\n' +
						'<div class="tbl-col-sel-list">\n' +
							'<div ng-repeat="col in $dtApi.columns" class="tbl-col-sel-row" ng-model="$index" sd-data-drag sd-data-drop on-drop="$dtApi.moveColumns($data, $index, $event)">\n' +
								'<input type="checkbox" class="tbl-col-sel-input" ng-model="col.visible"></span>\n' +
								'<span class="tbl-col-sel-label">{{col.title}}</span>\n' +
							'</div>\n' +
						'</div>\n' +
					'</div>\n' +
					'<div class="popup-dlg-footer">\n' +
						'<input type="submit" class="button primary" value="{{i18n(\'portal-common-messages.common-apply\')}}" ng-click="$dtApi.applyColumnSelector()" />' +
						'<input type="submit" class="button secondary" value="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()" />' +
					'</div>\n' +
				'</div>\n' +
				'<div ng-show="$dtApi.showExportOptions" class="popup-dlg" style="margin-left: 20px;">\n' +
					'<div class="popup-dlg-cnt">\n' +
						'<div><a href="" ng-hide="!$dtApi.enableSelectColumns" ng-click="$dtApi.exportCSV({allRows: false, allCols: false})">' + 
							'{{i18n(\'html5-common.export-options-current-page-current-fields\')}}\n' +
						'</a></div>\n' +
						'<div><a href="" ng-click="$dtApi.exportCSV({allRows: false, allCols: true})">' + 
							'{{i18n(\'html5-common.export-options-current-page-all-fields\')}}\n' +
						'</a></div>\n' +
						'<div><a href="" ng-hide="!$dtApi.enableSelectColumns" ng-click="$dtApi.exportCSV({allRows: true, allCols: false})">' + 
							'{{i18n(\'html5-common.export-options-all-pages-current-fields\')}}\n' +
						'</a></div>\n' +
						'<div><a href="" ng-click="$dtApi.exportCSV({allRows: true, allCols: true})">' + 
							'{{i18n(\'html5-common.export-options-all-pages-all-fields\')}}\n' +
						'</a></div>\n' +
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
		var onSelect = {}, onPagination = {}, onColumnReorder = {}, onSorting = {};
		var enableColumnSelector, columnSelectorAdmin, columnsByDisplayOrder, columnsInfoByDisplayOrder, devColumnOrderPref;
		var columnSelectorPreference, localPrefStore = {};
		var pageSize = 8;
		var sortingMode, sortByGetter;
		var columnFilters;
		var exportConfig = {}, exportAnchor = document.createElement("a"), remoteModeLastParams;

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
		function showErrorOnUI(e) {
			trace.error(theTableId + ':', e);
			trace.printStackTrace();

			showElement(theTable.parent(), false);
			showElement(theToolbar, false);

			var errorToShow = e;
			if (e.status != undefined && e.statusText != undefined) {
				errorToShow = e.status + ' - ' + e.statusText;
			}

			// TODO: i18n
			var msg = 'sd-data-table is unable to process table. Pl. refer browser console for details. Reason: ' + errorToShow;
			jQuery('<pre class="tbl-error">' + msg + '</pre>').insertAfter(theTable.parent());
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
						showErrorOnUI(e);
					}
				});
			} catch (e) {
				showErrorOnUI(e);
			}
		}

		/*
		 * 
		 */
		function processAttributes() {
			trace.log(theTableId + ': Processing table attributes...');

			if (attr.sdaPageSize != undefined && attr.sdaPageSize != '') {
				pageSize = parseInt(attr.sdaPageSize);
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

			if (attr.sdaSortable == 'true' || attr.sdaSortable == 'single' || attr.sdaSortable == 'multiple') {
				sortingMode = attr.sdaSortable == 'multiple' ? 'multiple' : 'single';

				if (attr.sdaSortBy != undefined && attr.sdaSortBy != '') {
					sortByGetter = $parse(attr.sdaSortBy);
				}

				if (attr.sdaOnSorting) {
					onSorting.handler = $parse(attr.sdaOnSorting);

					var onSortingfuncInfo = sdUtilService.parseFunction(attr.sdaOnSorting);
					if (onSortingfuncInfo && onSortingfuncInfo.params && onSortingfuncInfo.params.length > 0) {
						onSorting.param = onSortingfuncInfo.params[0];
					} else {
						trace.error(theTableId + ': sda-on-sorting does not seems to be correcly used, it does not appear to be a function accepting parameter.');
					}
				}
			}

			if (attr.sdaExports != undefined && attr.sdaExports != '') {
				var exports = attr.sdaExports.split(',');
				for (var i in exports) {
					if (exports[i].toLowerCase() == 'excel') {
						exportConfig.EXCEL = true;
					} else if (exports[i].toLowerCase() == 'csv') {
						exportConfig.CSV = true;
					}
				}

				if (attr.sdaExportsFileName != undefined && attr.sdaExportsFileName != '') {
					exportConfig.fileName = attr.sdaExportsFileName;
				} else {
					exportConfig.fileName = 'table_data';
				}

				if (attr.sdaExportsBatchSize != undefined && attr.sdaExportsBatchSize != '') {
					exportConfig.batchSize = parseInt(attr.sdaExportsBatchSize);
				} else {
					exportConfig.batchSize = 250; // Default Batch Size
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
					filterable: hCol.attr('sda-filterable') == 'true' ? true : false,
					exportable: hCol.attr('sda-exportable') == undefined || hCol.attr('sda-exportable') == 'true' ? true : false,
					exportParser: bCol.attr('sda-exporter') ? $parse(bCol.attr('sda-exporter')) : null,
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
				
				if (colDef.labelKey) {
					var titleParser = $parse('i18n("' + colDef.labelKey + '")');
					colDef.title = titleParser(elemScope);
				} else {
					colDef.title = colDef.label;
				}
				
				colDef.contents = bCol.html();
				colDef.contents = colDef.contents.trim();
				if (colDef.contents == "") {
					var contents = getDefaultContent(colDef);
					colDef.contents = '{{' + contents + '}}';
					colDef.defaultContentsParser = $parse(contents);
				}

				if (bCol.attr('style')) {
					colDef.cellStyle = bCol.attr('style');
				}

				if (bCol.attr('class')) {
					colDef.cellClass = bCol.attr('class');
				}

				colDef.filterMarkup = '';
				var filterTemplate = hCol.find('> [sda-filter-template]');
				if (filterTemplate && filterTemplate.length > 0) {
					filterTemplate.remove();
					colDef.filterable = true;
				
					var url = filterTemplate.attr('sda-filter-template');
					if (url != undefined && url != null && url != '') {
						colDef.filterMarkup = '<div ng-include="\'' + url + '\'"></div>';
					} else {
						colDef.filterMarkup = filterTemplate.html();
					}
				}

				if (colDef.filterMarkup == '') {
					switch (bCol.attr('sda-data-type')) {
						case 'int': 
						case 'integer':
							colDef.filterMarkup = '<div sd-number-filter></div>';
							break;
						case 'date': 
						case 'dateTime':
						case 'time':
							colDef.filterMarkup = '<div sd-date-filter></div>';
							break;
					}
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
		function getVisibleColumnsByDisplayOrder() {
			var visibleCols = [];
			angular.forEach(columnsByDisplayOrder, function(col, i) {
				if (col.visible) {
					visibleCols.push(col);
				}
			});
			return visibleCols;
		}
		
		/*
		 * 
		 */
		function getDefaultContent(colDef) {
			var contents = 'rowData.' + colDef.field;

			if (colDef.dataType === 'int') {
				contents = 'rowData.' + colDef.field + ' | number';
			} else if (colDef.dataType === 'dateTime') {
				contents = 'rowData.' + colDef.field + ' | date: "short"';
			} else if (colDef.dataType === 'date') {
				contents = 'rowData.' + colDef.field + ' | date: "shortDate"';
			} else if (colDef.dataType === 'time') {
				contents = 'rowData.' + colDef.field + ' | date: "shortTime"';
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
					sName: col.name,
					mData: colRenderer(col),
					bSortable: col.sortable,
					sType: attr.sdaMode == 'local' ? 'string' : undefined
				});
			});

			/*
			 * Need to have covering function to maintain outer scope for appropriate 'col' 
			 */
			function colRenderer(col) {
				return function(row, type, set) {
					var ret;

					if (type === 'display') {
						ret = col.contents;
					} else {
						// For 'local' mode return the actual value for types other than display
						if (attr.sdaMode == 'local') {
							ret = row[col.field];
						}
					}

					if (ret == undefined || ret == null) {
						ret = "";
					}

					return ret;
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

			// Create space for column filters
			columnFilters = {};
			var theFilters = angular.element('<div></div>');
			element.append(theFilters);

			// Add Header Labels
			var filterable = sdUtilService.isEmpty(element.attr('sda-filterable')) || element.attr('sda-filterable') == 'true' ? true : false;
			var headCols = element.find('> thead > tr > th');
			angular.forEach(columns, function(col, i) {
				var filterMarkup = '', filterDialogMarkup = ''
				if (filterable && col.filterable) {
					var toggleFilter = '$dtApi.toggleColumnFilter(\'' + col.name + '\')';
					var resetFilter = '$dtApi.resetColumnFilter(\'' + col.name + '\')';
					var resetFilterAndClose = '$dtApi.resetColumnFilter(\'' + col.name + '\', true)';
					var filterVisible = '$dtApi.isColumnFilterVisible(\'' + col.name + '\')';
					var applyFilter = '$dtApi.applyColumnFilter(\'' + col.name + '\')';
					var filterSet = '$dtApi.isColumnFilterSet(\'' + col.name + '\')';
					var filterTitle = '$dtApi.getColumnFilterTitle(\'' + col.name + '\')';
					var stopEvent = 'bpmCommon.stopEvent($event);';
					
					filterMarkup =
						'<span flt-anchor="' + col.name + '"></span>' +
						'<button class="button-link tbl-col-flt" ng-show="!' + filterSet + '" ng-click="' + stopEvent + toggleFilter + '" title="{{i18n(\'portal-common-messages.common-filterPopup-showFilter-tooltip\')}}">\n' +
							'<i class="glyphicon glyphicon-filter"></i>\n' +
						'</button>\n' +
						'<button class="button-link tbl-col-flt-set" ng-show="' + filterSet + '" ng-click="' + stopEvent + resetFilter + '" title="{{i18n(\'portal-common-messages.common-filterPopup-resetFilter-tooltip\')}}">\n' +
							'<i class="glyphicon glyphicon-filter"></i>\n' +
						'</button>\n' +
						'<button class="button-link" ng-click="' + stopEvent + toggleFilter + '" title="{{i18n(\'portal-common-messages.common-filterPopup-showFilter-tooltip\')}}">\n' +
							'<span class="tbl-col-flt-title" ng-if="!' + filterSet + '">{{i18n("portal-common-messages.common-filterPopup-filterNotSet")}}</span>' + 
							'<span class="tbl-col-flt-title" ng-if="' + filterSet + '">{{' + filterTitle + '}}</span>' +
						'</button>';

					filterDialogMarkup = 
						'<div ng-show="' + filterVisible + '" class="popup-dlg tbl-col-flt-dlg" col="' + col.name + '">\n' +
							'<div class="popup-dlg-hdr">\n' +
								'<span class="popup-dlg-hdr-txt">' +
									'{{i18n("portal-common-messages.common-filterPopup-dataFilterByLabel")}} ' + col.title + '</span>\n' + 
								'<span class="popup-dlg-cls glyphicon glyphicon-remove" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="' + toggleFilter + '"></span>\n' +
							'</div>\n' +
							'<div class="popup-dlg-cnt tbl-col-flt-dlg-cnt">\n' +
								'<div ng-if="' + filterVisible + '">\n' + 
									col.filterMarkup +
								'</div>\n' + 
							'\n</div>\n' +
							'<div class="popup-dlg-footer">\n' +
								'<input type="submit" class="button primary" value="{{i18n(\'portal-common-messages.common-filterPopup-applyFilter\')}}" ng-click="' + applyFilter + '" />' +
								'<input type="submit" class="button secondary" value="{{i18n(\'portal-common-messages.common-filterPopup-resetFilter\')}}" ng-click="' + resetFilterAndClose + '" />' +
							'</div>\n' +
						'</div>\n';
				}

				var columnHeader = 
					'<div>\n' + 
						'<div class="tbl-col-flt-wrapper">' + filterMarkup + '</div>\n' +
						'<div class="tbl-hdr-col-label">' + col.title + '</div>\n' +
					'</div>';

				var hCol = angular.element(headCols[i]);
				hCol.prepend(columnHeader);

				var header = hCol.children().first();
				$compile(header)(header.scope());

				if (filterDialogMarkup.length > 0) {
					// Setup Filter Dialog Element
					var filterDialog = angular.element(filterDialogMarkup);
					var filterScope = elemScope.$new();
					$compile(filterDialog)(filterScope);
					theFilters.append(filterDialog);

					// Setup Filter Data
					filterScope.$$filterData = {};

					filterScope.colData = {
						name: col.name,
						field: col.field,
						dataType: col.dataType,
						sortable: col.sortable,
						fixed: col.fixed,
						title: col.title
					};

					// Wrapper for applyFilter and resetFilter callback functions
					filterScope.handlers = {};

					/*
					 * 
					 */
					filterScope.setFilterTitle = function(title) {
						filterScope.$$filterTitle = title;
					}

					columnFilters[col.name] = {
						filter : filterDialog,
						anchor : header.find('[flt-anchor="' + col.name + '"]')
					};
				}
			});

			theTable = element;
			theTable.addClass('tbl');
		}

		/*
		 * 
		 */
		function buildDataTable() {
			var dtOptions = {};

			dtOptions.sDom = 'irtp';
			dtOptions.sPaginationType = "full_numbers";
			dtOptions.iDisplayLength = pageSize;
			dtOptions.oLanguage = {
				 "oPaginate": {
					 "sFirst": "<<",
					 "sPrevious": "<",
					 "sNext": ">",
					 "sLast": ">>"
				 }
			};
			
			dtOptions.aoColumns = dtColumns;
			dtOptions.bAutoWidth = false;

			dtOptions.bProcessing = false;

			// TODO: Datatables does not support single column sorting yet. What it has is only multi column sort
			dtOptions.bSort = sortingMode != undefined;
			if (dtOptions.bSort) {
				var dtOrder = [];

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

				if (dtOrder.length == 0) {
					dtOrder.push([0, 'asc']);
				}

				dtOptions.aaSorting = dtOrder;
			}

			dtOptions.fnDrawCallback = drawCallbackHandler;
			dtOptions.fnPreDrawCallback = function() {
				destroyRows();
			}
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
			dtOptions.bServerSide = true;
			dtOptions.sAjaxSource = "dummy.html";
			dtOptions.fnServerData = ajaxHandler;

			trace.log(theTableId + ': Building table for remote mode... with options: ', dtOptions);

			try {
				theDataTable = theTable.DataTable(dtOptions);
				buildDataTableCompleted();
			} catch (e) {
				trace.error(theTableId + ': Error occurred while using Datatables library', e);
				showErrorOnUI(e);
			}
		}

		/*
		 * 
		 */
		function buildDataTableLocalMode(dtOptions) {
			trace.log(theTableId + ': Building table for local mode... with options: ', dtOptions);

			fetchData(undefined).then(function(result) {
				try {
					dtOptions.aaData = result.list ? result.list : result;
	
					sdUtilService.assert(dtOptions.aaData && angular.isArray(dtOptions.aaData),
							'sd-data did not return acceptable result: Missing "list" or its not an array');
	
					if (attr.sdaNoPagination == '' || attr.sdaNoPagination == 'true') {
						dtOptions.iDisplayLength = dtOptions.aaData.length;
						dtOptions.sDom = 't';
					} else {
						// TODO: Undefine this for now! It causes wired issue with pagination
						dtOptions.iDisplayLength = undefined;
					}
					
					theDataTable = theTable.DataTable(dtOptions);
					buildDataTableCompleted();
				} catch (e) {
					showErrorOnUI(e);
				}
			}, function(error) {
				alert('Error while fetching data'); // TODO
			});
		}

		/*
		 * 
		 */
		function ajaxHandler(source, data, callback, settings) {
			trace.log(theTableId + ': Callback received for fetching server data, with data:', data);
			var dataMap = {};
			for (var i = 0; i < data.length; i++) {
				dataMap[data[i].name] = data[i].value;
			}

			var ret = {
				"sEcho" : dataMap['sEcho']
			};

			var params = {skip : dataMap['iDisplayStart'], pageSize : dataMap['iDisplayLength']};

			// Sorting / Ordering Info
			if (dataMap['iSortingCols'] > 0) {
				params.order = [];
				for (var i = 0; i < dataMap['iSortingCols']; i++) {
					var colIndex = dataMap['iSortCol_' + i];
					var colDir = dataMap['sSortDir_' + i];
					
					var column = columnsByDisplayOrder[colIndex];
					params.order.push({name: column.name, field: column.field, dir: colDir});
				}
			}

			// Filtering Info
			params.filters = {};
			for (var colName in columnFilters) {
				var filterScope = columnFilters[colName].filter.scope();
				if (filterScope.$$filterData != undefined && !jQuery.isEmptyObject(filterScope.$$filterData)) {
					params.filters[colName] = angular.copy(filterScope.$$filterData);
				}
			}
			if (jQuery.isEmptyObject(params.filters)) {
				delete params.filters;	
			}

			// Visible Columns
			var visibleOrderedCols = getVisibleColumnsByDisplayOrder();
			var colNames = [];
			angular.forEach(visibleOrderedCols, function(col, i) {
				colNames.push({
					name: col.name,
					field: col.field,
					dataType: col.dataType,
					sortable: col.sortable,
					fixed: col.fixed
				});
			});
			params.columns = colNames;

			remoteModeLastParams = params;
			fetchData(params).then(function(result) {
				try {
					sdUtilService.assert(jQuery.isPlainObject(result),
							'sd-data did not return acceptable result: Return is not a plain object');
					sdUtilService.assert(result.totalCount != undefined,
							'sd-data did not return acceptable result: Missing "totalCount"');
					sdUtilService.assert(result.list && angular.isArray(result.list),
							'sd-data did not return acceptable result: Missing "list" or its not an array');
					sdUtilService.assert(result.list.length <= data.length,
							'sd-data did not return acceptable result: Returned more records than expected (' + data.length + ')');
	
					ret.iTotalRecords = result.totalCount;
					ret.iTotalDisplayRecords = result.totalCount;
					ret.aaData = result.list;
	
					callback(ret);
				} catch (e) {
					showErrorOnUI(e);
				}
			}, function(error) {
				showErrorOnUI(error);
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

			// Register for sorting events
			theDataTable.on('sort.dt', processSortEvent);

			if (enableColumnSelector) {
				try {
					theColReorder = new jQuery.fn.dataTable.ColReorder(theDataTable, {bNoDragDrop: true});
				} catch (e) {
					trace.error(theTableId + ': Error occurred while enabling ColReorder, so disabling column selector', e);
					enableColumnSelector = false;
				}
			}

			// If column selector is not there, then mark all columns as visible
			if (!enableColumnSelector) {
				for (var i = 0; i < columnsByDisplayOrder.length; i++) {
					columnsByDisplayOrder[i].visible = true;
				}
			}

			exposeAPIs();
		}

		/*
		 * 
		 */
		function firePaginationEvent() {
			// Invoke the event handler when processing is complete and data is displayed
			$timeout(function() {
				var settings = theDataTable.fnSettings();
				var paginationInfo = {
					currentPage: (settings._iDisplayStart / settings._iDisplayLength) + 1,
					totalPages: Math.ceil(settings._iRecordsTotal / settings._iDisplayLength)
				};
				
				fireDataTableEvent(onPagination, paginationInfo, 'onPagination');
			}, 0, true);
		}

		/*
		 * 
		 */
		function processSortEvent() {
			// Invoke the event handler when processing is complete and data is displayed
			$timeout(function() {
				var sortingInfo = [];

				var order = theDataTable.fnSettings().aaSorting;
				for (var i in order) {
					var columnInfo = columnsByDisplayOrder[order[i][0]];
					if (columnInfo) {
						sortingInfo.push({
							name : columnInfo.name,
							dir : order[i][1] == 'asc' ? 'asc' : 'desc'
						});
					}
				}

				if (sortingMode == 'single' && sortingInfo.length > 0) {
					sortingInfo = sortingInfo[sortingInfo.length - 1];
				}
				
				if (sortByGetter && sortByGetter.assign) {
					sortByGetter.assign(elemScope, sortingInfo);
				}

				fireDataTableEvent(onSorting, sortingInfo, 'onSorting');
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
		function fetchData(params) {
			trace.log(theTableId + ': Calling sd-data with params:', params);

			var deferred = $q.defer();

			var dataResult = sdData.retrieveData(params);
			dataResult.then(function(result) {
				trace.log(theTableId + ': sd-data returned with:', result);
				deferred.resolve(result);
			}, function(error) {
				trace.log(theTableId + ': sd-data failed with:', error);
				deferred.reject(error);
		    });

			return deferred.promise;
		}

		/*
		 * 
		 */
		function createRowHandler(row, data, dataIndex) {
			row = angular.element(row);
			row.addClass(CLASSES.BODY_TR);

			var cells = row.find('> td');
			cells.addClass(CLASSES.TD);

			var visibleOrderedCols = getVisibleColumnsByDisplayOrder();

			angular.forEach(cells, function(cell, i) {
				// Safety check
				if (i >= visibleOrderedCols.length) {
					return;
				}

				cell = angular.element(cell);
				if (visibleOrderedCols[i].cellClass && visibleOrderedCols[i].cellClass != '') {
					var value = cell.attr('class');
					value = value ? (value.trim() + ' ') : '';
					value += visibleOrderedCols[i].cellClass;
					cell.attr('class', value);
				}

				if (visibleOrderedCols[i].cellStyle && visibleOrderedCols[i].cellStyle != '') {
					var value = cell.attr('style');
					value = value ? (value.trim()) : '';
					if (value.length > 1 && value.substr(value.length - 1) != ';') {
						value += '; ';
					}
					value += visibleOrderedCols[i].cellStyle;
					cell.attr('style', value);
				}
			});

			var rowScope = row.scope();
			if (rowScope == undefined) {
				rowScope = myScope.$new();
				rowScope.$on('$destroy', function() {
					//trace.log(theTableId + ': Row Scope ' + rowScope.$id + ' destroyed for parent ' + rowScope.$parent.$id);
				});
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
		function refresh(retainPageIndex) {
			theDataTable.fnDraw();
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
					theDataTable.fnSetColumnVis(i, col.visible, false);
					
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
				theDataTable.fnDraw(false);

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
			var tableData = theDataTable.fnGetData();

			if (index == undefined || index == null) {
				var start = 0;
				var end = tableData.length;

				if (attr.sdaMode == 'local') {
					var settings = theDataTable.fnSettings();
					start = settings._iDisplayStart;
					end = settings._iDisplayEnd;
				}

				var data = [];
				for (var i = start; i < end; i++) {
					data.push(tableData[i]);
				}
				return data;
			} else {
				if (attr.sdaMode == 'local') {
					// TODO: Get the current UI ordered Data
					var settings = theDataTable.fnSettings();
					return tableData[settings._iDisplayStart + index];
				} else {
					return tableData[index];
				}
			}
		}

		/*
		 * 
		 */
		function getPageDataCount() {
			if (attr.sdaMode == 'local') {
				var settings = theDataTable.fnSettings();
				return settings._iDisplayEnd - settings._iDisplayStart;
			} else {
				var data = theDataTable.fnGetData();
				return data.length;
			}
		}

		/*
		 * 
		 */
		function getTotalCount() {
			var settings = theDataTable.fnSettings();
			return settings._iRecordsTotal;
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
		 * 
		 */
		function destroyRows() {
			try {
				var rows = theTable.find('> tbody > tr');
				for (var i = 0; i < rows.length; i++) {
					var row = angular.element(rows[i]);
					row.scope().$destroy();
				}
			} catch (e) {
				trace.error(theTableId + ': Error while destroying rows scopes', e);
			}
		}

		/*
		 * options.allRows = boolean, true for All Rows
		 * options.allCols = boolean, true for All Columns
		 */
		function exportAsCSV(options) {
			var promise = getDataForExport(options, 'csv', encoderForCSV);
			promise.then(function(result) {
				var exportedRows;
				if (angular.isArray(result)) {
					exportedRows = result;
				} else {
					exportedRows = result.data;
				}

				// First entry will be headers, so exportData.length == 1 means no data to export
				if (exportedRows.length > 1) {
					var exportData = [];
					for (var i = 0; i < exportedRows.length; i++) {
						exportData.push(exportedRows[i].join(','));
					}
	
					var exportConents = exportData.join('\n');
					
					// Download File
					var downloadMetaData = 'data:application/csv;charset=utf-8,';
					var downloadUrl = downloadMetaData + encodeURIComponent(exportConents);
					downloadDataAsFile(exportConfig.fileName + '.csv', downloadUrl);
				}

				var message = {};
				if (angular.isArray(result)) {
					if (exportedRows.length == 1) {
						message.key = 'export-error-no-data';
						message.val = 'No data to export.';
					}
				} else {
					trace.error(theTableId + ': Error occurred while exporting data.', result.error);
					if (exportedRows.length > 1) {
						message.key = 'export-error-incomplete-data';
						message.val = 'Error occurred, however exported the data fetched till now.'; 
					} else {
						message.key = 'export-error';
						message.val = 'Error occurred while exporting data.';
					}
				}

				if (message.key != undefined) {
					alert(sgI18nService.translate('html5-common.' + message.key, message.val));
				}
			}, function(error) {
				trace.error(theTableId + ': Error occurred while exporting data', error);
				alert(sgI18nService.translate('html5-common.export-error', 'Error occurred while exporting data.'));
			});
		}

		/*
		 * 
		 */
		function encoderForCSV(text) {
			if (text != undefined && text != null) {
				text = '' + text; // Convert to String
				text = text.replace('"', '""'); // Escape double quotes
				text = '\"' + text + '\"'; // Wrap value in double quotes
			}
			return text;
		}

		/*
		 * options.allRows = boolean, true for All Rows
		 * options.allCols = boolean, true for All Columns
		 */
		function getDataForExport(options, exportType, encoder) {
			var deferred = $q.defer();

			if (options == undefined || options == null) {
				options = {};
			}

			var expotCols = [];
			var colDefs = options.allCols ? columnsByDisplayOrder : getVisibleColumnsByDisplayOrder();
			for (var j = 0; j < colDefs.length; j++) {
				if (colDefs[j].exportable) {
					expotCols.push(colDefs[j]);
				}
			}

			var promise = getTableDataForExport(options.allRows, expotCols);
			promise.then(function(result) {
				try {
					var data;
					if (angular.isArray(result)) {
						data = result;
					} else {
						data = result.data;
					}

					var exportedRows = [];
	
					// Export Header / Titles
					exportedRows.push(getTableTitlesForExport(expotCols, encoder));
	
					// Validate
					for (var j = 0; j < expotCols.length; j++) {
						if (!expotCols[j].exportParser && !expotCols[j].defaultContentsParser) {
							trace.warn(theTableId + ': Cannot export column ' + expotCols[j].name + ', as sda-exporter is not defined.');
						}
					}

					// Export Data
					for (var i = 0; i < data.length; i++) {
						var rowExportData = [];
						for (var j = 0; j < expotCols.length; j++) {
							var locals = {
								rowData : data[i],
								exportType : exportType
							}

							var exportVal = '';
							if (expotCols[j].exportParser) {
								exportVal = expotCols[j].exportParser(elemScope, locals);
							} else if (expotCols[j].defaultContentsParser) {
								exportVal = expotCols[j].defaultContentsParser(elemScope, locals);
							}
							
							rowExportData.push(encoder(exportVal));
						}
						exportedRows.push(rowExportData);
					}

					if (angular.isArray(result)) {
						deferred.resolve(exportedRows);
					} else {
						result.data = exportedRows;
						deferred.resolve(result);
					}
					
				} catch(e) {
					deferred.reject(e);
				}
			}, function(error) {
				deferred.reject(error);
			});
			
			return deferred.promise;
		}

		/*
		 * 
		 */
		function getTableDataForExport(exportAllRows, expotCols) {
			if (attr.sdaMode == 'local') {
				return getLocalModeTableDataForExport(exportAllRows, expotCols);
			} else {
				return getRemoteModeTableDataForExport(exportAllRows, expotCols);
			}
		}

		/*
		 * 
		 */
		function getTableTitlesForExport(colDefs, encoder) {
			var titles = [];

			angular.forEach(colDefs, function(col, i) {
				titles.push(encoder(col.title));
			});

			return titles;
		}

		/*
		 * 
		 */
		function getLocalModeTableDataForExport(exportAllRows, expotCols) {
			var deferred = $q.defer();

			var data;
			if (exportAllRows) {
				data = theDataTable.fnGetData();
			} else {
				data = getPageData();
			}

			deferred.resolve(data);

			return deferred.promise;
		}

		/*
		 * 
		 */
		function getRemoteModeTableDataForExport(exportAllRows, expotCols) {
			var deferred = $q.defer();

			// Get Table Data
			if (exportAllRows) {
				var params = angular.copy(remoteModeLastParams);
				params.fetchType = 'export';

				params.skip = 0;
				params.pageSize = exportConfig.batchSize;

				params.columns = [];
				angular.forEach(expotCols, function(col, i) {
					params.columns.push({
						name: col.name,
						field: col.field,
						dataType: col.dataType,
						sortable: col.sortable,
						fixed: col.fixed
					});
				});

				fetchAllInBatches(deferred, params, []);
			} else {
				var data = getPageData();
				deferred.resolve(data);
			}

			return deferred.promise;
		}

		/*
		 * 
		 */
		function fetchAllInBatches(deferred, params, data) {
			var batchNo = (params.skip / params.pageSize) + 1;
			trace.log(theTableId + ': Fetching data for export, batch ' + batchNo);

			fetchData(params).then(function(result) {
				// TODO: Validate result!
				data = data.concat(result.list);

				params.skip += params.pageSize;
				if (params.skip < result.totalCount) {
					fetchAllInBatches(deferred, params, data);
				} else {
					deferred.resolve(data);
				}
			}, function(error) {
				deferred.resolve({data: data, error: error});
			});
		}

		/*
		 * 
		 */
		function downloadDataAsFile(fileName, dataUrl) {
			exportAnchor.download = fileName;
			exportAnchor.href = dataUrl;
			exportAnchor.target = '_blank';

			var mouseEvent = document.createEvent("MouseEvents");
			mouseEvent.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
			exportAnchor.dispatchEvent(mouseEvent);
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
				refresh(retainPageIndex);
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
				self.enableExportExcel = false; // exportConfig.EXCEL; // TODO: Support Excel download
				self.enableExportCSV = exportConfig.CSV;
				self.showExportOptions = false;

				self.showColumnFilters = {};
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
			this.toggleColumnFilter = function(colName, copyDataBack) {
				for (var name in self.showColumnFilters) {
					if (name != colName && self.showColumnFilters[name]) {
						self.showColumnFilters[name] = false;

						var filterScope = columnFilters[name].filter.scope();
						delete filterScope.filterData;
					}
				}

				self.showColumnFilters[colName] = !self.showColumnFilters[colName];

				var filterScope = columnFilters[colName].filter.scope();
				if (self.showColumnFilters[colName]) {
					columnFilters[colName].filter.css('top', columnFilters[colName].anchor.position().top + 20);
					columnFilters[colName].filter.css('left', columnFilters[colName].anchor.position().left + 15);

					filterScope.filterData = angular.copy(filterScope.$$filterData);
				} else {
					if (copyDataBack) {
						filterScope.$$filterData = filterScope.filterData;
					}

					delete filterScope.filterData;
				}
			}

			/*
			 * 
			 */
			this.isColumnFilterVisible = function(colName) {
				return self.showColumnFilters[colName];
			}

			/*
			 * 
			 */
			this.applyColumnFilter = function(colName) {
				var filterScope = columnFilters[colName].filter.scope();
				if (filterScope.handlers.applyFilter) {
					var ret = filterScope.handlers.applyFilter();
					if (!ret) {
						return;
					}
				}
				self.toggleColumnFilter(colName, true);
				refresh();
			}

			/*
			 * 
			 */
			this.resetColumnFilter = function(colName, closeDlg) {
				var filterScope = columnFilters[colName].filter.scope();
				if (filterScope.handlers.resetFilter) {
					filterScope.handlers.resetFilter();
				}

				if (closeDlg) {
					self.toggleColumnFilter(colName);
				}

				filterScope.$$filterData = {};
				refresh();
			}

			/*
			 * 
			 */
			this.isColumnFilterSet = function(colName) {
				if (columnFilters[colName]) {
					var filterScope = columnFilters[colName].filter.scope();
					return !jQuery.isEmptyObject(filterScope.$$filterData);
				}
			}

			/*
			 * 
			 */
			this.getColumnFilterTitle = function(colName) {
				var filterScope = columnFilters[colName].filter.scope();
				return filterScope.$$filterTitle;
			}

			/*
			 * 
			 */
			this.toggleExportOptions = function(forceHide) {
				if (forceHide) {
					self.showExportOptions = false;
				} else {
					self.showExportOptions = !self.showExportOptions;
				}
			}

			/*
			 * 
			 */
			this.exportCSV = function(options) {
				exportAsCSV(options);
				self.toggleExportOptions(true);
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
							title: col.title
						});
					}
				});

				return selectableCols;
			}
		}
	};
})();
