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
			['$parse', '$q', '$compile', '$timeout', 'sgI18nService', 'sdUtilService', 'sdLoggerService', 'sdPreferenceService','sdPortalConfigurationService','sdDialogService',
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

	var EXPORT_LIMIT = 10000;
	var EXPORT_BATCH_SIZE = 250;
	
	/*
	 * 
	 */
	function DataTableDirective($parse, $q, $compile, $timeout, sgI18nService, sdUtilService, sdLoggerService, sdPreferenceService, sdPortalConfigurationService, sdDialogService) {
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
								scope, element, attr, ctrl, sdPortalConfigurationService, sdDialogService);
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
		angular.forEach(bodyCols, function(bCol, i) {
			bCol = angular.element(bCol);
			var contents = bCol.html();
			bCol.html('<div ng-non-bindable>' + contents + '</div>');
		});
		
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
		if (show && element.css('visibility') == 'hidden') {
			element.css('visibility', 'visible');
		} else if (!show && element.css('visibility') != 'hidden') {
			element.css('visibility', 'hidden');
		}
	}
	
	/*
	 * 
	 */
	function DataTableCompiler($parse, $q, $compile, $timeout, sgI18nService, sdUtilService, sdPreferenceService, 
			scope, element, attr, ctrl, sdPortalConfigurationService, sdDialogService) {
		var TOOLBAR_TEMPLATE =
			'<div class="tbl-toolbar-section">\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableSelectColumns" ng-click="$dtApi.toggleColumnSelector()"' + 
					' title="{{i18n(\'portal-common-messages.common-filterPopup-selectColumnsLabel\')}}">\n' +
					'<i class="pi pi-column-selector pi-lg"></i>\n' +
				'</button>\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableExportExcel" ng-click=""' +
					' title="{{i18n(\'portal-common-messages.common-genericDataTable-asExcel\')}}">\n' +
					'<i class="pi pi-export pi-lg"></i>\n' +
				'</button>\n' +
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if=" $dtApi.enableSaveState && $dtApi.saveColumnAttributes" ng-click="$dtApi.toggleSavedState();"' +
						' title="{{i18n(\'portal-common-messages.common-filterPopup-saveState\')}}">\n' +
						'<i class="pi pi-favorite pi-lg"></i>\n' +
				'</button>\n'+
				'<button class="button-link tbl-toolbar-item tbl-tool-link" ng-if="$dtApi.enableSaveState && !$dtApi.saveColumnAttributes" ng-click="$dtApi.toggleSavedState();"' +
						' title="{{i18n(\'portal-common-messages.common-filterPopup-saveState-not\')}}">\n' +
						'<i class="pi pi-favorite-not pi-lg"></i>\n' +
				'</button>\n'+
				'<button class="button-link tbl-toolbar-item  tbl-tool-joined-link tbl-tool-link" ng-if="$dtApi.enableExportCSV" ng-click="$dtApi.exportCSV({allRows: false, allCols: false})"' +
					' title="{{i18n(\'portal-common-messages.common-genericDataTable-asCSV\')}}">\n' +
					'<i class="pi pi-export pi-lg"></i>\n' +
				'</button>\n' +
				'<div ng-if="$dtApi.showSelectColumns" class="popup-dlg">\n' +
				'<div class="popup-dlg-hdr">\n' +
					'<span class="popup-dlg-hdr-txt">{{i18n("portal-common-messages.common-filterPopup-selectColumnsLabel")}}</span>\n' + 
					'<button class="button-link popup-dlg-cls" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="$dtApi.toggleColumnSelector()">\n' +
						'<i class="pi pi-close pi-lg" />\n' +
					'</button>\n' +
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
								'<button class="button-link tbl-col-sel-link" ng-if="$dtApi.columnSelectorAdmin" ng-click="$dtApi.toggleColumnSelectorLock()" ng-disabled="$dtApi.isColumnSelectorLockDisabled()">\n' +
									'<span class="pi pi-lg pi-lock" ng-show="$dtApi.lock" title="{{i18n(\'portal-common-messages.common-filterPopup-unlock\')}}"></span>\n' + 
									'<span class="pi pi-lg pi-unlock" ng-show="!$dtApi.lock" title="{{i18n(\'portal-common-messages.common-filterPopup-lock\')}}"></span>\n' +
								'</button>\n' +
								'<button class="button-link tbl-col-sel-link pi pi-reset pi-lg" ng-click="$dtApi.resetColumnSelector()" title ="{{i18n(\'portal-common-messages.common-reset\')}}" style="cursor: pointer;"></button>\n' +
							'</div>\n' +
							'<div class="tbl-col-sel-list">\n' +
								'<div ng-repeat="col in $dtApi.columns" class="tbl-col-sel-row" ng-model="$index" sd-data-drag sd-data-drop sda-drop="$dtApi.moveColumns($data, $index, $event)">\n' +
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
				'<span ng-if="$dtApi.enableExportCSV" sd-popover="$dtApi.exportPopoverHandle" sda-class="tbl-tool-link">' +
					'<i class="pi pi-menu-dropdown"></i>\n' +
					'<div class="popover-body">\n' +
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
					'</div>\n';
				'</span>'+
			'</div>\n';

		var elemScope = scope.$parent;
		var myScope = scope;
		var sdData = ctrl[0];

		var treeTable = false, treeTableData, treeData;
		var tableInLocalMode, initialized, firstLoad, firstInitiaizingDraw = true;
		var columns = [], dtColumns = [];
		var theTable, theTableId, theDataTable, theToolbar, theColReorder;
		var selectedRowIndexes = {}, rowSelectionMode = false, selectionBinding;
		var onSelect = {}, onPagination = {}, onColumnReorder = {}, onSorting = {}, onTreeNodeAction = {};
		var enableColumnSelector, columnSelectorAdmin, columnsByDisplayOrder, columnsInfoByDisplayOrder, devColumnOrderPref;
		var columnSelectorPreference, localPrefStore = {};
		var pageSize = 8, disablePagination;
		var sortingMode, sortByGetter, enableFiltering;
		var columnFilters;
		var exportConfig = {}, remoteModeLastParams;
		var localModeData, localModeMasterData, localModeRefreshInitiated, localModeRebuildData;
		var localModeGlobalFilter = {}, localModeSortingCols = [];
		var compileTime = 0;
		var tableStateValue = false;
		var enableStateSave  = false;
		var stateSuffixForPreference = ".columnFilterAndSortOrder"

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
				trace.log('Table defines sda-ready attribute, so deferring initialization...');
				var unregister = elemScope.$watch(attr.sdaReady, function(newVal, oldVal) {
					if(newVal === true) {
						trace.log('sda-ready flag is triggered...');

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
			trace.error('Error', e);
			trace.printStackTrace();

			showElement(theTable.parent(), false);
			showElement(theToolbar, false);

			var errorToShow = 'Unknown Error';
			if (angular.isString(e)) {
				errorToShow = e;
			} else if (e.status != undefined && e.statusText != undefined) {
				errorToShow = e.status + ' - ' + e.statusText;
			}

			// No need to i18n this message as this error state is expected to be reached only during development
			var msg = 'sd-data-table is unable to process table. Pl. refer browser console for details. Reason: ' + errorToShow;
			jQuery('<pre class="tbl-error">' + msg + '</pre>').insertAfter(theTable.parent());
		}

		/*
		 * 
		 */
		function initialize() {
			trace.log('Initializing Data Table...');

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
			trace.log('Processing table attributes...');

			// Tree Table
			if (attr.sdaTreeTable != undefined) {
				trace.log('Rendering as Tree Table, Forcing local mode, no pagination, no sorting, no exports...');

				treeTable = true;

				attr.sdaMode = 'local';
				attr.sdaNoPagination = 'true';
				attr.sdaSortable = 'false';

				if (attr.sdaOnTreeNodeAction) {
					onTreeNodeAction = parseFunction(attr.sdaOnTreeNodeAction, 'sda-on-tree-node-action');
				}
			}
			
			tableInLocalMode = attr.sdaMode == 'local';
				
			if (attr.sdaPageSize != undefined && attr.sdaPageSize != '') {
				pageSize = parseInt(attr.sdaPageSize);
			} else {
			    pageSize =  parseInt(sdPortalConfigurationService.getPageSize());
			}

			if (attr.sdaNoPagination == '' || attr.sdaNoPagination == 'true') {
				disablePagination = true;
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
					onSelect = parseFunction(attr.sdaOnSelect, 'sda-on-select');
				}
			}

			if (attr.sdaOnPagination) {
				onPagination = parseFunction(attr.sdaOnPagination, 'sda-on-pagination');
			}

			if (attr.sdaColumnSelector && (attr.sdaColumnSelector == 'true' || attr.sdaColumnSelector == 'admin')) {
				enableColumnSelector = true;
				columnSelectorAdmin = attr.sdaColumnSelector == 'admin' ? true : false;

				if (attr.sdaOnColumnReorder) {
					onColumnReorder = parseFunction(attr.sdaOnColumnReorder, 'sda-on-column-reorder');
				}
			}

			enableFiltering = sdUtilService.isEmpty(attr.sdaFilterable) || attr.sdaFilterable == 'true';

			if (attr.sdaSortable == 'true' || attr.sdaSortable == 'single' || attr.sdaSortable == 'multiple') {
				sortingMode = attr.sdaSortable == 'multiple' ? 'multiple' : 'single';

				if (attr.sdaSortBy != undefined && attr.sdaSortBy != '') {
					sortByGetter = $parse(attr.sdaSortBy);
				}

				if (attr.sdaOnSorting) {
					onSorting = parseFunction(attr.sdaOnSorting, 'sda-on-sorting');
				}
			}

			var exports = [];
			if (attr.sdaExports == 'true') {
				exports = ['excel', 'csv'];
			} else if (attr.sdaExports != undefined) {
				exports = attr.sdaExports.split(',');
			}
			
			if (exports != undefined && exports != '') {
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
					exportConfig.batchSize = EXPORT_BATCH_SIZE;
				}

				if (attr.sdaExportsLimit != undefined && attr.sdaExportsLimit != '') {
					exportConfig.limit = parseInt(attr.sdaExportsLimit);
				} else {
					exportConfig.limit = EXPORT_LIMIT;
				}
			}

			if (tableInLocalMode && enableFiltering && attr.sdaFilterHandler) {
				localModeGlobalFilter = parseFunction(attr.sdaFilterHandler, 'sda-filter-handler');
			}
			
			if (attr.sdaSaveState) {
				enableStateSave = sdUtilService.toBoolean(attr.sdaSaveState);
			} 
		}

		/*
		 *
		 */
		function cleanupAsDirective() {
			var currentDir = 'sd-data-table: ' + element.attr('sd-data-table');
			jQuery('<!-- ' + currentDir + ' -->').insertBefore(element);
			element.removeAttr('sd-data-table', '');
		}

		/*
		 * 
		 */
		function validateMarkup() {
			trace.log('Validating table markup...');

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
			trace.log('Processing table markup...');

			// Process Columns
			var head = element.find('> thead');
			var i18nScope = '';
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
					filterable: hCol.attr('sda-filterable') == undefined || hCol.attr('sda-filterable') == '' ? undefined : (hCol.attr('sda-filterable') == 'true'? true : false),	
					exportable: hCol.attr('sda-exportable') == undefined || hCol.attr('sda-exportable') == 'true' ? true : false,
					exportParser: bCol.attr('sda-exporter') ? $parse(bCol.attr('sda-exporter')) : null,
					fixed: hCol.attr('sda-fixed') != undefined && hCol.attr('sda-fixed') == 'true' ? true : false,
					treeColumn: treeTable && bCol.attr('sda-tree-column') != undefined ? true : undefined
				};

				if (!colDef.name) {
					trace.warn('Column ' + (i + 1) + ' is missing attribute sda-name');
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
				
				colDef.contents = bCol.children().html();
				colDef.contents = colDef.contents.trim();
				if (colDef.contents == '') {
					var contents = getDefaultContent(colDef);
					colDef.contents = '{{' + contents + '}}';
					colDef.defaultContentsParser = $parse(contents);
				} else {
					// Adding dummy ng-if for creating separate subscope for cell, this is to receive separate colData 
					colDef.contents = 
						'<div ng-if="true" ng-init="colData = $dtApi.getColumnData(\'' + colDef.name + '\')">' +
							colDef.contents + 
						'</div>';
				}

				if (treeTable && colDef.treeColumn) {
					colDef.fixed = true;
					var treeContents = 
						'<span class="tbl-tree-controls">' +
							'<span ng-repeat="treeLevel in rowData.$$treeInfo.levels" class="tbl-tree-indent"></span>' +
							'<button class="button-link tbl-tree-action" ng-click="$dtApi.toggleTreeNode($index)" ng-disabled="rowData.$leaf" ng-style="{\'visibility\': rowData.$leaf ? \'hidden\' : \'visible\'}">' +
								'<span ng-show="rowData.$expanded" class="pi pi-tree-collapse"></span>' +
								'<span ng-show="!rowData.$expanded" class="pi pi-tree-expand"></span>' +
							'</button>' +
						'</span>';
					colDef.contents = treeContents + colDef.contents;
				}

				if (bCol.attr('style')) {
					colDef.cellStyle = bCol.attr('style');
				}

				if (bCol.attr('class')) {
					colDef.cellClass = bCol.attr('class');
				}

				colDef.filterMarkup = '';
				if(colDef.filterable == undefined || colDef.filterable) {
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
				}

				if (colDef.filterMarkup == '') {
					switch (colDef.dataType) {
						case 'string':
							colDef.filterMarkup = '<div sd-text-search-table-filter></div>';
							break;
						case 'int': 
						case 'integer':
							colDef.filterMarkup = '<div sd-number-table-filter></div>';
							break;
						case 'date':
						case 'dateTime':
						case 'time':
							colDef.filterMarkup = '<div sd-date-table-filter></div>';
							break;
						case 'boolean':
							colDef.filterMarkup = '<div sd-boolean-table-filter></div>';
							break;
					}
				}

				if (hCol.attr('sda-sort-handler')) {
					colDef.sorter = parseFunction(hCol.attr('sda-sort-handler'), 'sda-sort-handler');
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
		function parseFunction(funcAsStr, attribute) {
			var retInfo = {};
			retInfo.attribute = attribute;
			retInfo.handler = $parse(funcAsStr);
			
			var funcInfo = sdUtilService.parseFunction(funcAsStr);
			if (funcInfo && funcInfo.params && funcInfo.params.length > 0) {
				retInfo.param = funcInfo.params[0];
			} else {
				trace.error(attribute + ' is not correcly used, it does not appear to be a function accepting parameter.');
			}

			return retInfo;
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
				contents = 'rowData.' + colDef.field;
			} else if (colDef.dataType === 'dateTime') {
				contents = 'rowData.' + colDef.field + ' | sdDateTimeFilter';
			} else if (colDef.dataType === 'date') {
				contents = 'rowData.' + colDef.field + ' | sdDateFilter';
			} else if (colDef.dataType === 'time') {
				contents = 'rowData.' + colDef.field + ' | sdTimeFilter';
			}

			return contents;
		}

		/*
		 * 
		 */
		function getCellAlignmentClass(colDef) {
			var clazz = 'tbl-col-align-left';

			if (colDef.dataType && colDef.dataType != '') {
				var dataType = colDef.dataType.toLowerCase();
				if (dataType === 'int') {
					clazz = 'tbl-col-align-center tbl-col-no-wrap';
				} else if (dataType === 'datetime' || dataType === 'date' || dataType === 'time') {
					clazz = 'tbl-col-align-center tbl-col-no-wrap';
				} else if (dataType === 'boolean') {
					clazz = 'tbl-col-align-center';
				}
			}
			
			return clazz;
		}

		/*
		 * 
		 */
		function buildDataTableInformation() {
			trace.log('Building table information...');

			angular.forEach(columns, function(col, i) {
				dtColumns.push({
					sName: col.name,
					mData: colRenderer(col),
					bSortable: col.sortable
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
					}

					if (ret == undefined || ret == null) {
						ret = '';
					}

					return ret;
				};
			}
		}

		/*
		 * 
		 */
		function createDataTable() {
			trace.log('Creating table...');

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
			compileMarkup(defaultToolbar, defaultToolbar.scope(), 'Toolbar');

			// Create space for column filters
			columnFilters = {};
			var theFilters = angular.element('<div></div>');
			element.append(theFilters);

			// Add Header Labels
			var headCols = element.find('> thead > tr > th');
			angular.forEach(columns, function(col, i) {
				var filterMarkup = '', filterDialogMarkup = '';
				if (enableFiltering && col.filterable) {
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
							'<i class="pi pi-filter"></i>\n' +
						'</button>\n' +
						'<button class="button-link tbl-col-flt-set" ng-show="' + filterSet + '" ng-click="' + stopEvent + resetFilter + '" title="{{i18n(\'portal-common-messages.common-filterPopup-resetFilter-tooltip\')}}">\n' +
							'<i class="pi pi-filter"></i>\n' +
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
								'<button class="button-link popup-dlg-cls" title="{{i18n(\'portal-common-messages.common-filterPopup-close\')}}" ng-click="' + toggleFilter + '">\n' +
									'<i class="pi pi-close pi-lg" />\n' +
								'</button>\n' +
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
						'<div>\n' + 
							'<span class="tbl-hdr-col-label">' + col.title + '</span>' + 
							'<span class="pi pi-tbl-sort pi-lg" />' +
						'</div>\n' +
					'</div>';

				var hCol = angular.element(headCols[i]);
				hCol.prepend(columnHeader);

				var header = hCol.children().first();
				compileMarkup(header, header.scope(), 'Header for ' + col.name);

				if (filterDialogMarkup.length > 0) {
					// Setup Filter Dialog Element
					var filterDialog = angular.element(filterDialogMarkup);
					var filterScope = elemScope.$new();
					compileMarkup(filterDialog, filterScope, 'Filter for ' + col.name);
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
			dtOptions.sPaginationType = 'full_numbers';
			dtOptions.iDisplayLength = pageSize;
			dtOptions.oLanguage = {
					oPaginate: {
						sFirst: '<i class="pi pi-fast-rewind dataTables_paginate_icon"></i>',
						sPrevious: '<i class="pi pi-prev-page dataTables_paginate_icon"></i>',
						sNext: '<i class="pi pi-next-page dataTables_paginate_icon"></i>',
						sLast: '<i class="pi pi-fast-forward dataTables_paginate_icon"></i>'
					},
 					sEmptyTable: sgI18nService.translate('portal-common-messages.common-genericDataTable-noRecordsFoundLabel')
			};
			
			dtOptions.aoColumns = dtColumns;
			dtOptions.bAutoWidth = false;

			dtOptions.bProcessing = false;

			dtOptions.bSingleColumnSort = true;
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
				destroyRowScopes();
			}
			dtOptions.fnCreatedRow = createRowHandler;

			dtOptions.bServerSide = true;
			dtOptions.sAjaxSource = 'dummy.html';
			dtOptions.fnServerData = tableInLocalMode ? ajaxHandlerLocalMode : ajaxHandler;

			if (tableInLocalMode) {
				if (disablePagination) {
					dtOptions.sDom = 't';
				}
			}

			trace.log('Building table for ' + (tableInLocalMode ? 'local' : 'remote') + ' mode... with options: ', dtOptions);

			try {
				theDataTable = theTable.DataTable(dtOptions);

				// Table will be refreshed again after initialization is complete, so hide some information

				// Replace no Records Found row with loading indicator
				theTable.find('> tbody > tr').html('<span class="pi pi-lg pi-spin pi-spinner"></span>');

				// Hide Pagination Info
				var dataTablesInfo = angular.element(theTable.parent()).find('.dataTables_info');
				showElement(dataTablesInfo, false);

				// Show table and toolbar, this was hidden while processing raw markup in processRawMarkup()
				showElement(theTable, true);
				showElement(theToolbar, true);

				buildDataTableCompleted();
			} catch (e) {
				trace.error('Error occurred while using Datatables library', e);
				showErrorOnUI(e);
			}
		}

		/*
		 * 
		 */
		function ajaxHandlerLocalMode(source, data, callback, settings) {
			var dataMap = {};
			for (var i = 0; i < data.length; i++) {
				dataMap[data[i].name] = data[i].value;
			}

			var ret = {
				sEcho : dataMap['sEcho']
			};

			if (firstInitiaizingDraw) {
				// Return empty data, table will be refreshed during initialization for Column Reordering.
				// At that time return actual data, this way it avoids duplicate requests to load 1st page.

				ret.iTotalRecords = 0;
				ret.iTotalDisplayRecords = 0;
				ret.aaData = [];
				callback(ret);
				return;
			}

			var params = {skip : dataMap['iDisplayStart'], pageSize : dataMap['iDisplayLength']};

			if(localModeRefreshInitiated) {
				localModeMasterData = undefined;
				localModeData = undefined;
				localModeRefreshInitiated = false;
			}
				
			if (!localModeData) {
				fetchData(undefined).then(function(result) {
					try {
						localModeData = result.list ? result.list : result;

						validateData(localModeData);

						if (treeTable) {
							treeData = localModeData;
							treeTableData = sdUtilService.marshalDataForTree(localModeData);
							localModeData = sdUtilService.rebuildTreeTable(treeTableData, function(rowData) {
								return isRowVisibleForLocalMode(rowData);
							});
						} else {
							localModeSortingCols = getSortingInfo();
							localModeMasterData = localModeData;
							localModeData = applyFilteringAndSortingForLocalMode(localModeMasterData);
						}

						ret.iTotalRecords = localModeData.length;
						ret.iTotalDisplayRecords = localModeData.length;

						if (disablePagination) {
							ret.aaData = localModeData;
						} else {
							ret.aaData = getLocalModePageData(params);
						}

						callback(ret);
					} catch (e) {
						showErrorOnUI(e);
					}
				}, function(error) {
					trace.error('Error while fetching data', error);
					showErrorOnUI(error);
				});
			} else {
				if (treeTable) {
					localModeData = sdUtilService.rebuildTreeTable(treeTableData, function(rowData) {
						return isRowVisibleForLocalMode(rowData);
					});
				} else {
					if (localModeRebuildData) {
						localModeRebuildData = false;
						localModeData = applyFilteringAndSortingForLocalMode(localModeMasterData);
					}
				}

				ret.iTotalRecords = localModeData.length;
				ret.iTotalDisplayRecords = localModeData.length;

				if (disablePagination) {
					ret.aaData = localModeData;
				} else {
					ret.aaData = getLocalModePageData(params);
				}

				callback(ret);
			}
			
			/*
			 * 
			 */
			function getLocalModePageData(params) {
				var pageData = [];

				if (localModeData) {
					var end = params.skip + params.pageSize;
					if (end > localModeData.length) {
						end = localModeData.length;
					}
		
					for (var i = params.skip; i < end; i++) {
						pageData.push(localModeData[i]);
					}
				}

				return pageData;
			}
		}

		/*
		 * 
		 */
		function applyFilteringAndSortingForLocalMode(master) {
			var data = [];

			// Filtering
			for(var i = 0; i < master.length; i++) {
				if (isRowVisibleForLocalMode(master[i])) {
					data.push(master[i]);
				}
			}

			// Sorting
			for (var i = 0; i < localModeSortingCols.length; i++) {
				var sortBy = localModeSortingCols[i];
				var colDef = columnsInfoByDisplayOrder[sortBy.name].column;

				data.sort(function(row1, row2) {
					var params = {
						sortBy: sortBy,
						row1: row1,
						row2: row2
					};

					if (colDef.sorter && colDef.sorter.handler) {
						return invokeScopeFunction(colDef.sorter, params);
					}
					else {
						return builtInComparatorForLocalMode(params);
					}
				});
			}

			return data;
		}

		/*
		 * 
		 */
		function isRowVisibleForLocalMode(rowData) {
			var visible = true;
			if (localModeGlobalFilter.handler) {
				visible = invokeScopeFunction(localModeGlobalFilter, rowData);
			}

			if (visible) {
				for (var colName in columnFilters) {
					var filterScope = columnFilters[colName].filter.scope();
					if (filterScope.$$filterData != undefined && !jQuery.isEmptyObject(filterScope.$$filterData)) {
						try {
							if (!filterScope.handlers.filterCheck) {
								trace.error('Filter does not declare method called filterCheck() for column: ' + colName);
								continue;
							}

							filterScope.filterData = angular.copy(filterScope.$$filterData);
							visible = filterScope.handlers.filterCheck(rowData, sdUtilService.resolveProperty(rowData, filterScope.colData.field));
							if (!visible) {
								break;
							}
						} catch(e) {
							trace.error('Error occurred filtering', e);
						} finally {
							delete filterScope.filterData;
						}
					}
				}
			}

			return visible;
		}

		/*
		 * 
		 */
		function builtInComparatorForLocalMode(params) {
			var ret = 0;

			try {
				var value1 = params.row1[params.sortBy.field];
				var value2 = params.row2[params.sortBy.field];

				if (value1 != undefined && value1 != null && value2 != undefined && value2 != null) {
					switch (params.sortBy.dataType) {
						case 'string':
							value1 = value1.toLowerCase();
							value2 = value2.toLowerCase();
							if (params.sortBy.dir == 'asc') {
								ret = (value1 == value2 ? 0 : (value1 > value2 ? 1 : -1));
							} else {
								ret = (value2 == value1 ? 0 : (value2 > value1 ? 1 : -1));
							}
							break;
						case 'int': 
						case 'integer':
							if (params.sortBy.dir == 'asc') {
								ret = value1 - value2;
							} else {
								ret = value2 - value1;
							}
							break;
						case 'date': 
						case 'dateTime':
						case 'time':
							if (value1.getTime) {
								value1 = value1.getTime();
							}
							if (value2.getTime) {
								value2 = value2.getTime();
							}

							if (params.sortBy.dir == 'asc') {
								ret = value1 - value2;
							} else {
								ret = value2 - value1;
							}
							break;
						case 'boolean':
							value1 = value1 == true ? 1 : 2;
							value2 = value2 == true ? 1 : 2;
							if (params.sortBy.dir == 'asc') {
								ret = value1 - value2;
							} else {
								ret = value2 - value1;
							}
							break;
						default:
							trace.error('Built-in sorting not supported for data type: ' + 
									params.sortBy.dataType + ', column: ' + params.sortBy.name);
					}
				} else {
					// Either value is null or both null
					if ((value1 == undefined || value1 == null) && (value2 == undefined || value2 == null)) {
						ret = 0;
					} else if (value1 != undefined && value1 != null) {
						ret = params.sortBy.dir == 'asc' ? -1 : 1;
					} else if (value2 != undefined && value2 != null) {
						ret = params.sortBy.dir == 'asc' ? 1 : -1;
					}
				}
			} catch (e) {
				trace.error('Error while using built in sort for Local Mode:', params, e);
			}

			return ret;
		}

		/*
		 * 
		 */
		function ajaxHandler(source, data, callback, settings) {
			var dataMap = {};
			for (var i = 0; i < data.length; i++) {
				dataMap[data[i].name] = data[i].value;
			}

			var ret = {
				sEcho : dataMap['sEcho']
			};
			
			if (firstInitiaizingDraw) {
				// Return empty data, table will be refreshed during initialization for Column Reordering.
				// At that time return actual data, this way it avoids duplicate requests to load 1st page.

				ret.iTotalRecords = 0;
				ret.iTotalDisplayRecords = 0;
				ret.aaData = [];

				callback(ret);
				firstLoad = true;
				return;
			}

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
			
			if(firstLoad) { 
				loadSavedState(params);
				firstLoad = false;
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
					validateData(result, pageSize);
	
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
		
		/**
		 * 
		 */
		function getSavedTableState () {
			if(tableStateValue === false) {
				tableStateValue = undefined;
				var pScope = 'USER' ;	
				var preferenceDelegate = getPreferenceDelegate(pScope);				
				
				if(preferenceDelegate.store ) {
					var preferenceName = preferenceDelegate.store.marshalName(pScope, preferenceDelegate.name) + stateSuffixForPreference;
					var filterAndSortOrder = preferenceDelegate.store.getValue(preferenceName)
					if(filterAndSortOrder) { 
						tableStateValue = JSON.parse(filterAndSortOrder);
					}
				}
			}
			return tableStateValue;
		}
		
		/**
		 * 
		 */
		function isColumnAttributesSaved(){
			// !! ensures a falsy value for undefined
			return  !!getSavedTableState();
		}
		
		/**
		 * 
		 */
		function loadSavedState(params){ 
			var filterAndSortOrder = getSavedTableState();
			if(filterAndSortOrder) {
				loadSavedColumnFilters( params, filterAndSortOrder.filters);
				loadSavedSortOrder(params, filterAndSortOrder.order);
			}
		}

		/**
		 * 
		 */
		function loadSavedSortOrder( params , savedOrder) {
			params.order = angular.copy(savedOrder);
			
			var dtOrder = [];

			if (savedOrder) {
				var orderBy = savedOrder;
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

			theDataTable.fnSettings().aaSorting = dtOrder;
		}
		
		/**
		 * 
		 */

		function loadSavedColumnFilters(params, savedFilters){
			for (var colName in savedFilters) { 
				var filter = angular.copy(savedFilters[colName]);
				columnFilters[colName].filter.scope().$$filterTitle = angular.copy(filter.title);
				
				delete filter.title;
				columnFilters[colName].filter.scope().$$filterData = filter ;
				params.filters[colName] = filter;
			}
		}

		/*
		 * 
		 */
		function buildDataTableCompleted() {
			// Initialization handler
			theDataTable.on('init.dt', function() {
				firePaginationEvent();
			});

			// Register for pagination events
			theDataTable.on('page.dt', firePaginationEvent);

			// Register for sorting events
			theDataTable.on('sort.dt', processSortEvent);

			if (enableColumnSelector) {
				try {
					// Sometimes it's observed that jQuery.fn.dataTable is undefined, so added the check and fallback
					var jQueryDataTableFunc = jQuery.fn.dataTable || theTable.DataTable;
					theColReorder = new jQueryDataTableFunc.ColReorder(theDataTable, {bNoDragDrop: true});
				} catch (e) {
					trace.error('Error occurred while enabling ColReorder, so disabling column selector', e);
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
			if (onPagination.handler) {
				// Invoke the event handler when processing is complete and data is displayed
				$timeout(function() {
					var settings = theDataTable.fnSettings();
					var paginationInfo = {
						currentPage: (settings._iDisplayStart / settings._iDisplayLength) + 1,
						totalPages: Math.ceil(settings._iRecordsTotal / settings._iDisplayLength)
					};
					
					invokeScopeFunction(onPagination, paginationInfo);
				}, 0, true);
			}
		}

		/*
		 * 
		 */
		function processSortEvent() {
			if (tableInLocalMode) {
				localModeRebuildData = true;
				localModeSortingCols = getSortingInfo();
			}

			// Invoke the event handler when processing is complete and data is displayed
			if (onSorting.handler) {
				$timeout(function() {
					var sortingInfo = getSortingInfo();
	
					if (sortingMode == 'single' && sortingInfo.length > 0) {
						sortingInfo = sortingInfo[sortingInfo.length - 1];
					}
					
					if (sortByGetter && sortByGetter.assign) {
						sortByGetter.assign(elemScope, sortingInfo);
					}
	
					invokeScopeFunction(onSorting, sortingInfo);
				}, 0, true);
			}
			
			if(self.saveColumnAttributes && self.applyTo == "USER" ) {
				storeTableState();
			}
		}

		/*
		 * 
		 */
		function getSortingInfo() {
			var sortingInfo = [];

			var order = theDataTable.fnSettings().aaSorting;
			for (var i in order) {
				var columnInfo = columnsByDisplayOrder[order[i][0]];
				if (columnInfo) {
					sortingInfo.push({
						name : columnInfo.name,
						field: columnInfo.field,
						dataType: columnInfo.dataType,
						dir : order[i][1] == 'asc' ? 'asc' : 'desc'
					});
				}
			}

			return sortingInfo;
		}

		/*
		 * 
		 */
		function invokeScopeFunction(handleInfo, data, invokeAfterDigest) {
			if (handleInfo.handler) {
				try {
					var transObj = {};
					if (handleInfo.param) {
						transObj[handleInfo.param] = data;
					} else {
						transObj.info = data;
						trace.warn(handleInfo.attribute + ' is not properly implemented, may not receive params');
					}

					if (!invokeAfterDigest) {
						return handleInfo.handler(elemScope, transObj);
					} else {
						$timeout(function() {
							handleInfo.handler(elemScope, transObj);
						}, 0, true);
					}
				} catch(e) {
					trace.error('Error while invoking function for ' + handleInfo.attribute, e);
				}
			}
		}
		
		/*
		 * 
		 */
		function fetchData(params) {
			trace.log('Calling sd-data with params:', params);

			var deferred = $q.defer();

			var dataResult = sdData.retrieveData(params);
			dataResult.then(function(result) {
				trace.log('sd-data returned successfully');
				deferred.resolve(result);
			}, function(error) {
				trace.log('sd-data failed with:', error);
				deferred.reject(error);
		    });

			return deferred.promise;
		}

		/*
		 * 
		 */
		function validateData(result, maxPageSize) {
			try {
				if (tableInLocalMode) {
					sdUtilService.assert(result && angular.isArray(result),
						'sd-data did not return acceptable result: Missing "list" or its not an array');
				} else {
					sdUtilService.assert(jQuery.isPlainObject(result),
						'sd-data did not return acceptable result: Return is not a plain object');
					sdUtilService.assert(result.totalCount != undefined,
						'sd-data did not return acceptable result: Missing "totalCount"');
					sdUtilService.assert(result.list && angular.isArray(result.list),
						'sd-data did not return acceptable result: Missing "list" or its not an array');
					sdUtilService.assert(result.list.length <= maxPageSize,
						'sd-data did not return acceptable result: Returned more records than expected (' + maxPageSize + ')');
				}
			} catch (e) {
				trace.error('sd-data returned with incorrect data:', result);
				throw e;
			}
		}

		/*
		 * 
		 */
		function createRowHandler(row, data, dataIndex) {
			row = angular.element(row);
			row.addClass(CLASSES.BODY_TR);

			// Hide row so that uncompiled markup is not visible 
			row.addClass('ng-hide');
			row.attr('ng-show', 'true');

			var cells = row.find('> td');
			cells.addClass(CLASSES.TD);

			var visibleOrderedCols = getVisibleColumnsByDisplayOrder();

			angular.forEach(cells, function(cell, i) {
				// Safety check
				if (i >= visibleOrderedCols.length) {
					return;
				}

				cell = angular.element(cell);

				// Class Attribute
				addClass(cell, getCellAlignmentClass(visibleOrderedCols[i]));
				addClass(cell, visibleOrderedCols[i].cellClass);

				// Style Attribute
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

			row.attr('sd-data-table-row', dataIndex); // Another directive, which will handle setting of rowData
		}

		/*
		 * 
		 */
		function addClass(elem, clazz) {
			if (clazz && clazz != '') {
				var value = elem.attr('class');
				value = value ? (value.trim() + ' ') : '';
				value += clazz;
				elem.attr('class', value);
			}
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

			// Handle empty table case
			var count = getPageDataCount();
			if (count == 0) {
				trace.log('Handling empty table case...');

				var rows = theTable.find('> tbody > tr');
				var row = angular.element(rows[0]); // There will be only one row 
				var rowScope = myScope.$new();
				compileMarkup(row, rowScope, 'Empty row');
			} else {
				var body = angular.element(theTable.find('> tbody'));
				var bodyScope = body.scope();
				bodyScope.$$pageData = getPageData(); // Used by sd-data-table-row
				compileMarkup(body, bodyScope, 'Table body');
			}

			if(firstInitiaizingDraw) {
				firstInitiaizingDraw = false;
				enableRowSelection();

				$timeout(function() {
					reorderColumns(null, null); // This will cause another draw, at that time further initialization is to be done
					exposeScopeInfo();
				}, 0, false);
			} else {
				if (!initialized) {
					// First Draw is done, this one is second caused by Column Reorder - Complete initialization
					doInitialSelection();
					initialized = true;

					// Show Pagination Info
					var dataTablesInfo = angular.element(theTable.parent()).find('.dataTables_info');
					showElement(dataTablesInfo, true);

					logCompilationTime();
				} else {
					clearState(); // Subsequent draws will pass from here
					logCompilationTime();
				}

				sdUtilService.safeApply(elemScope);
			}
		}

		/*
		 * 
		 */
		function refresh(retainPageIndex) {
			trace.log('Refreshing table with retainPageIndex = ' , retainPageIndex);

			if (tableInLocalMode) {
				localModeRefreshInitiated = true;
			}
			theDataTable.fnDraw(!retainPageIndex);
		}

		/*
		 * rebuild = Not valid for Tree Table
		 */
		function refreshUi(rebuild) {
			trace.log('Refreshing table Ui');

			localModeRebuildData = rebuild;

			theDataTable.fnDraw(true);
		}		

		/*
		 * 
		 */
		function getPreferenceDelegate(pScope) {
			var preferenceDelegate = {};

			if (attr.sdaPreferenceDelegate) {
				var preferenceDelagate = parseFunction(attr.sdaPreferenceDelegate, 'sda-preference-delegate');
				var data = {
					scope : pScope
				}
				preferenceDelegate.store = invokeScopeFunction(preferenceDelagate, data);					
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
					if (parentPrefValue && sdUtilService.toBoolean(parentPrefValue.lock)) {
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
		
		
		/**
		*
		*/
		function setStateInPreferences( value ) {
			var pScope = 'USER' ;
			
			tableStateValue = value;
			var preferenceDelegate = getPreferenceDelegate(pScope);
			if (preferenceDelegate.store) {
				var preferenceName = preferenceDelegate.store.marshalName(pScope, preferenceDelegate.name) + stateSuffixForPreference;
				preferenceDelegate.store.setValue(preferenceName, tableStateValue);
				preferenceDelegate.store.save();
			} 
		}
		
		/*
		 * 
		 */
		function storeTableState( ) {
			var value = {
					filters : getAppliedColumnFilters(),
					order : getSortingInfo()
			}
			setStateInPreferences(value);
		}
		
		/**
		 * 
		 */
		function removeStateFromPreferences() {
			setStateInPreferences(null);
		}
		
		/**
		 * 
		 */
		function getAppliedColumnFilters() {
			var filters = {};
			
			for (var colName in columnFilters) {
				var filterScope = columnFilters[colName].filter.scope();
				if (filterScope.$$filterData != undefined && !jQuery.isEmptyObject(filterScope.$$filterData)) {
					filters[colName] = angular.copy(filterScope.$$filterData);
					filters[colName].title = filterScope.$$filterTitle;
				}
			}
			return filters;
		}
		
		/**
		 * 
		 */
		function getAppliedColumnFilters() {
			var filters = {};
			
			for (var colName in columnFilters) {
				var filterScope = columnFilters[colName].filter.scope();
				if (filterScope.$$filterData != undefined && !jQuery.isEmptyObject(filterScope.$$filterData)) {
					filters[colName] = angular.copy(filterScope.$$filterData);
					filters[colName].title = filterScope.$$filterTitle;
				}
			}
			
			return filters;
		}

		/*
		 * 
		 */
		function reorderColumns(pScope, preview) {
			if (!enableColumnSelector) {
				// Draw is required here for angular markup work properly!
				// If this is not done then colData is not available for render templates on first pass (1st page)
				theDataTable.fnDraw(false);
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
								title: colInfo.column.title
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
							title: col.title
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
			
			invokeScopeFunction(onColumnReorder, columnReorderInfo, true);
		}

		/*
		 * 
		 */
		function clearState() {
			if (getRowSelectionCount() > 0) {
				unselectRows();
				processSelectionBinding();
			}
		}

		/*
		 * 
		 */
		function logCompilationTime() {
			trace.log('Angular Compilation Total Time: ' , compileTime);
			compileTime = 0;
		}

		/*
		 * 
		 */
		function getPageData(index) {
			var tableData = treeTable ? localModeData : theDataTable.fnGetData();

			if (index == undefined || index == null) {
				var start = 0;
				var end = tableData.length;

				var data = [];
				for (var i = start; i < end; i++) {
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
		function getPageDataCount() {
			var data = theDataTable.fnGetData();
			return data.length;
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
					var count = getPageDataCount();
					if (count > 0) {
						processRowSelection(this);
						processSelectionBinding();
					}
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
		function getRowSelectionCount() {
			var selection = [];

			if (rowSelectionMode) {
				angular.forEach(selectedRowIndexes, function(value, key) {
					selection.push(value);
				});
			}

			return selection.length;
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

			invokeScopeFunction(onSelect, selectionInfo);
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
			if (attr.sdDataTable != undefined && attr.sdDataTable != '') {
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
		 * 
		 */
		function destroyRowScopes() {
			try {
				var rows = theTable.find('> tbody > tr');
				for (var i = 0; i < rows.length; i++) {
					var row = angular.element(rows[i]);
					row.scope().$destroy();
				}
			} catch (e) {
				trace.error('Error while destroying rows scopes', e);
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
					var downloadMetaData = 'application/octet-stream;charset=utf-8';
					// Download File
					sdUtilService.downloadAsFile(exportConents, exportConfig.fileName+ '.csv', downloadMetaData);
				}

				var message = {};
				if (angular.isArray(result)) {
					if (exportedRows.length == 1) {
						message.key = 'export-error-no-data';
						message.val = 'No data to export.';
					}
				} else {
					trace.error('Error occurred while exporting data.', result.error);
					if (exportedRows.length > 1) {
						message.key = 'export-error-incomplete-data';
						message.val = 'Error occurred, however exported the data fetched till now.'; 
					} else {
						message.key = 'export-error';
						message.val = 'Error occurred while exporting data.';
					}
				}

				if (message.key != undefined) {
					var options = {
							title : sgI18nService.translate('portal-common-messages.common-info')
							};
					sdDialogService.alert(scope, sgI18nService.translate('html5-common.' + message.key, message.val), options);
				}
			}, function(error) {
				alert(error);
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
							trace.warn('Cannot export column ' + expotCols[j].name + ', as sda-exporter is not defined.');
						}
					}

					// Export Data
					for (var i = 0; i < data.length; i++) {
						var rowExportData = [];
						for (var j = 0; j < expotCols.length; j++) {
							var locals = {
								rowData : data[i],
								colData : {
									name: expotCols[j].name,
									field: expotCols[j].field,
									dataType: expotCols[j].dataType,
									sortable: expotCols[j].sortable,
									fixed: expotCols[j].fixed,
									title: expotCols[j].title
								},
								exportType : exportType
							}

							var exportVal = '';
							if (expotCols[j].exportParser) {
								exportVal = expotCols[j].exportParser(elemScope, locals);
							} else if (expotCols[j].defaultContentsParser) {
								exportVal = expotCols[j].defaultContentsParser(elemScope, locals);
							}

							if (treeTable && expotCols[j].treeColumn) {
								var indent = '';
								for (var level = 0; level < data[i].$$treeInfo.level; level++) {
									indent += '    ';
								}
								exportVal = indent + exportVal;
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
			if (tableInLocalMode) {
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
				data = treeTable ? treeTableData : localModeData; // Tree Table: All Rows including collapsed rows 
			} else {
				data = treeTable ? localModeData : getPageData(); // Tree Table: As seen on UI
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
				var totalCount = getTotalCount();
				if (totalCount > exportConfig.limit) {
					deferred.reject(
						sgI18nService.translate('html5-common.export-error-limit-exceds',
								'Cannot export, total count exceeds the set limit of') + ' ' + exportConfig.limit);
				}
				
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
			trace.log('Fetching data for export, batch ' , batchNo);

			fetchData(params).then(function(result) {
				try {
					validateData(result, params.pageSize);
					
					data = data.concat(result.list);
	
					params.skip += params.pageSize;
					if (params.skip < result.totalCount) {
						fetchAllInBatches(deferred, params, data);
					} else {
						deferred.resolve(data);
					}
				} catch (e) {
					deferred.resolve({data: data, error: e});
				}
			}, function(error) {
				deferred.resolve({data: data, error: error});
			});
		}

		/*
		 * 
		 */
		function compileMarkup(elem, scope, id) {
			var start = new Date().getTime();
			$compile(elem)(scope);
			var end = new Date().getTime();

			compileTime += (end - start);

			trace.log('Angular Compilation For: ' , id , ', Time: ' + (end - start));
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
				$timeout(function() {
					refresh(retainPageIndex);
				}, 0, false);
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

			/*
			 * 
			 */
			this.getData = function(index) {
				return getPageData(index);
			}

			// This API is available only for Tree Table
			if (treeTable) {
				/*
				 * 
				 */
				this.refreshUi = function () {
					$timeout(function() {
						refreshUi();
					}, 0, false);
				};

				/*
				 * 
				 */
				this.expandAll = function () {
					sdUtilService.expandTreeTable(treeTableData);
					refreshUi();
				};
	
				/*
				 * 
				 */
				this.collapseAll = function () {
					sdUtilService.collapseTreeTable(treeTableData);
					refreshUi();
				};

				/*
				 * 
				 */
				this.getTreeData = function () {
					return treeData;
				};
			}
		}

		/*
		 * Scope API
		 */
		function ScopeAPI() {
			var self = this;

			init();

			function init() {
				self.columns = [];
				self.enableSelectColumns = enableColumnSelector && (columnSelectorAdmin || ! sdUtilService.toBoolean(columnSelectorPreference.lock) );
				self.columnSelectorAdmin = columnSelectorAdmin;
				self.showSelectColumns = false;
				self.applyTo = 'USER';
				self.enableExportExcel = false; // exportConfig.EXCEL; // TODO: Support Excel download
				self.enableExportCSV = exportConfig.CSV;
				self.exportPopoverHandle = null;
				self.saveColumnAttributes= isColumnAttributesSaved();
				self.enableSaveState = enableStateSave;

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
			this.toggleSavedState = function() {
				this.saveColumnAttributes = !this.saveColumnAttributes;
				var param = {
						filters : {},
						order : []
				}
				
				if(self.saveColumnAttributes) {
					storeTableState();
				}else {
					removeStateFromPreferences( );
				}
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
				
				var title = sgI18nService.translate('views-common-messages.common-confirm', 'Confirm');
		    	var html = '<span>'
		    		+ sgI18nService.translate('portal-common-messages.common-preferenceScope-resetConfimation',
		    		'Are you sure you want to reset the Preferences?') + '</span>';
		    	
		    	var options = {
		    			title : title,
						dialogActionType : 'YES_NO'
					};
				
		    	var defer = sdDialogService.confirm
							(scope, sgI18nService.translate('portal-common-messages.common-preferenceScope-resetConfimation'), 
							options);
		    	
		    	defer.then(function() {
		    		setColumnSelectionFromPreference(self.applyTo, null);
    				reorderColumns(self.applyTo);
    				self.toggleColumnSelector();
				});
			};

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
				if (tableInLocalMode) {
					refreshUi(true);
				} else {
					refresh();
				}
				
				if(self.saveColumnAttributes) {
					storeTableState();
				}
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
				if (tableInLocalMode) {
					refreshUi(true);
				} else {
					refresh();
				}
				
				if(self.saveColumnAttributes) {
					storeTableState();
				}
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
			this.exportCSV = function(options) {
				self.exportPopoverHandle.hide();
				exportAsCSV(options);
			}

			/*
			 * 
			 */
			this.getColumnData = function(colName) {
				var colInfo = columnsInfoByDisplayOrder[colName];
				if (colInfo && colInfo.column) {
					var colData = {
						name: colInfo.column.name,
						field: colInfo.column.field,
						dataType: colInfo.column.dataType,
						sortable: colInfo.column.sortable,
						fixed: colInfo.column.fixed,
						title: colInfo.column.title
					};

					return colData;
				}
			}

			/*
			 * 
			 */
			this.toggleTreeNode = function(index) {
				var rowData = localModeData[index];

				var treeActionInfo = {
					current : rowData
				};
						
				if (rowData.$expanded) {
					rowData.$expanded = false;

					treeActionInfo.action = 'collapse';
					invokeScopeFunction(onTreeNodeAction, treeActionInfo, true);
					
					refreshUi();
				} else {
					rowData.$expanded = true;
					if (!rowData.$$treeInfo.loaded) {
						// Load Children
						var treeParams = {parent: rowData};
						fetchData(treeParams).then(function(result) {
							try {
								var children = result.list ? result.list : result;
								validateData(children);

								if (children.length > 0) {
									rowData.children = children;
									children = sdUtilService.marshalDataForTree(children, rowData);
									sdUtilService.insertChildrenIntoTreeTable(treeTableData, rowData, children);
								} else {
									rowData.$leaf = true;
									delete rowData.$expanded;
								}

								rowData.$$treeInfo.loaded = true;
							} catch (e) {
								// TODO: Show Error
								trace.error('Error', e);
								rowData.$expanded = false;
							}

							treeActionInfo.action = 'expand';
							invokeScopeFunction(onTreeNodeAction, treeActionInfo, true);

							refreshUi();
						}, function(error) {
							// TODO: Show Error
							trace.error('Error', error);
							rowData.$expanded = false;
							refreshUi();
						});
					} else {
						treeActionInfo.action = 'expand';
						invokeScopeFunction(onTreeNodeAction, treeActionInfo, true);

						refreshUi();
					}
				}
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

	angular.module('bpm-common').directive('sdDataTableRow', [DataTableRowDirective]);

	/*
	 * 
	 */
	function DataTableRowDirective() {
		return {
			restrict: 'A',
			scope: true,
			link: function(scope, element, attr, ctrl) {
				var index = parseInt(attr.sdDataTableRow);
				
				// Using isolate scope and passing data as attributes doesnot work here
				// So retrieve data from parent via $parent
				var pageData = scope.$parent.$$pageData;

				scope.rowData = pageData[index];
				scope.$index = index;
				scope.$first = (index === 0);
				scope.$last = (index === (pageData.length - 1));
				scope.$even = !(scope.$odd = (index & 1) === 0);

				scope.$on('$destroy', function() {
				});
			}
		};
	};
})();
