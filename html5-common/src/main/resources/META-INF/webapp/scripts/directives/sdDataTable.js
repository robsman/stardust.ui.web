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

	var dataTablesDirective = ['$parse', '$compile', '$timeout', function($parse, $compile, $timeout) {
		return {
			restrict : 'AE', // TODO: Remove support for E and only support as A
			require: ['sdData'],
			scope: {
				// TODO: Accept more inputs...
			},
			compile: function(elem, attr, transclude) {				
				processRawMarkup(elem, attr);

				return {
					post : function(scope, element, attr, ctrl) {
						var dataTableCompiler = new DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl);
					}
				};
			}
		};	
	}];

	var TOOLBAR_TEMPLATE =
		'<div class="tbl-toolbar-section">\n' +
			'<a href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-filterPopup-selectColumnsLabel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
				'<i class="fa fa-table"></i>\n' +
			'</a>\n' +
			'<a href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asExcel\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
				'<i class="fa fa-file-excel-o"></i>\n' +
			'</a>\n' +
			'<a href="#" ng-click="" title="{{i18n(\'portal-common-messages.common-genericDataTable-asCSV\')}}" class="tbl-toolbar-item tbl-tool-link">\n' +
				'<i class="fa fa-file-text-o"></i>\n' +
			'</a>\n' +
		'</div>\n';
	/*
	 * 
	 */
	function processRawMarkup(elem, attr) {
		// Add ng-non-bindable, so that the markup is not compiled
		var bodyCols = elem.find('> tbody > tr > td');
		bodyCols.attr('ng-non-bindable', '');
	}

	/*
	 * 
	 */
	function DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl) {
		// This is same as scope.$parent
		var elemScope = element.scope();
		var sdData = ctrl[0];

		var columns = [], dtColumns = [], theTable, theDataTable;

		// Setup component instance
		setup();

		/*
		 * 
		 */
		function setup() {
			if (attr.sdaPageSize == undefined || attr.sdaPageSize == "") {
				attr.sdaPageSize = 8;
			}

			// TODO: Revisit 'ready' behaviour
			if (attr.sdaReady) {
				elemScope.$watch(attr.sdaReady, function(newVal, oldVal) {
					if(newVal === true) {
						// Initialize after current digest cycle
						$timeout(initialize);

						// TODO: Can unregister here, to reduce the watchers!
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
			processMarkup();

			cleanupAsDirective();
			
			buildDataTableInformation();

			createDataTable();

			// We just changed the markup, so can proceed further only after current digest cycle
			$timeout(function() {
				buildDataTable();
			});
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
		function processMarkup() {
			// Process Columns
			var head = element.find('> thead');
			var i18nScope = "";
			if (head.attr('sda-i18n-scope') != undefined) {
				i18nScope = head.attr('sda-i18n-scope');
			}

			var headCols = element.find('> thead > tr > th');
			var bodyCols = element.find('> tbody > tr > td');

			if (headCols.length != bodyCols.length) {
				throw "Table Header and body columns are not matching";
			}
			
			headCols.addClass('tbl-hdr-col');
			
			for(var i = 0; i < headCols.length; i++) {
				var hCol = angular.element(headCols[i]);
				var bCol = angular.element(bodyCols[i]);
				
				var colDef = {
						field: bCol.attr('sda-field'),
						dataType: bCol.attr('sda-data-type'),
						sortable: hCol.attr('sda-sortable')
				};

				if (hCol.attr('sda-label') != undefined) {
					if (i18nScope != "") {
						colDef.label = i18nScope + '-' + hCol.attr('sda-label');
					} else {
						colDef.label = hCol.attr('sda-label');
					}
				}
				
				colDef.contents = bCol.html();
				colDef.contents = colDef.contents.trim();
				if (colDef.contents == "") {
					colDef.contents = getDefaultContent(colDef);
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
				};
			}
		}

		/*
		 * 
		 */
		function createDataTable() {
			// Toolbar
			var toolbar = element.prev();
			if (toolbar.attr('sda-toolbar') != undefined) {
				toolbar.prepend(TOOLBAR_TEMPLATE);
			} else {
				var toolbarTemplate = '<div>' + TOOLBAR_TEMPLATE + '</div>';
				jQuery(toolbarTemplate).insertBefore(element);
				toolbar = element.prev();
			}

			toolbar = angular.element(toolbar);
			toolbar.addClass('tbl-toolbar');

			// Compile the default toolbar, which was inserted
			var defaultToolbar = toolbar.children().first();
			$compile(defaultToolbar)(defaultToolbar.scope());

			// Add Header Labels
			var headCols = element.find('> thead > tr > th');
			angular.forEach(columns, function(col, i) {
				if (col.label != undefined) {
					var columnHeader = '<span class="tbl-hdr-col-label">{{i18n("' + col.label + '")}}</span>';

					var hCol = angular.element(headCols[i]);
					hCol.prepend(columnHeader);
					
					var header = hCol.children().first();
					$compile(header)(header.scope());
				}
			});
			
			theTable = element;
		}

		/*
		 * 
		 */
		function buildDataTable() {
			var dtOptions = {};

			dtOptions.hasOverrideDom = true;
			dtOptions.sDom = "itp";
			dtOptions.sPaginationType = "full_numbers";
			dtOptions.iDisplayLength = attr.sdaPageSize;

			dtOptions.columns = dtColumns;

			dtOptions.processing = true;
			dtOptions.serverSide = true;
			dtOptions.ajax = ajaxHandler;

			dtOptions.fnDrawCallback = drawCallbackHandler;
			dtOptions.fnCreatedRow = createRowHandler;

			theDataTable = theTable.DataTable(dtOptions);
			exposeAPIs();
		}

		/*
		 * 
		 */
		function ajaxHandler(data, callback, settings) {
			var params = {skip: data.start, pageSize: data.length};

			var dataResult = sdData.retrieveData(params);
			dataResult.then(function(result) {
				var ret = {
						"draw" : data.draw,
						"recordsTotal": result.totalCount,
						"recordsFiltered": result.totalCount,
						"data": result.list
					};

					callback(ret);
			}, function(error) {
				// TODO: Notify Datatables
		    });
		}

		/*
		 * 
		 */
		function createRowHandler(row, data, dataIndex) {
			var row = angular.element(row);
			row.find('> td').addClass('tbl-col');
			
			var rowScope = row.scope();
			if (rowScope == undefined) {
				rowScope = elemScope.$new();
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
		}

		/*
		 * 
		 */
		function exposeAPIs() {
			if (attr.sdDataTable != undefined && attr.sdDataTable != "") {
				var userTable = $parse(attr.sdDataTable).assign;
				if (userTable) {
					userTable(elemScope, theDataTable);
				}
			}

			theDataTable.refresh = refresh;
			theDataTable.reInitialize = reInitialize;
		}

		/*
		 * 
		 */
		function refresh(retainPageIndex) {
			var oSettings = theDataTable.settings()[0];
			jQuery(oSettings.oInstance).trigger('page', oSettings);
		    oSettings.oApi._fnCalculateEnd(oSettings);
		    oSettings.oApi._fnDraw(oSettings);
		}

		/*
		 * 
		 */
		function reInitialize() {
			var oSettings = theDataTable.settings()[0];
			oSettings._iDisplayStart = 0;

			jQuery(oSettings.oInstance).trigger('page', oSettings);
		    oSettings.oApi._fnCalculateEnd(oSettings);
		    oSettings.oApi._fnDraw(oSettings);

		}

		/*
		 * 
		 */
		function safeApply() {
			if (elemScope.$root.$$phase !== '$apply' || elemScope.$root.$$phase !== '$digest') {
				elemScope.$apply();
			}
		}
	};

	angular.module('bpm-common').directive('sdDataTable', dataTablesDirective);
})();