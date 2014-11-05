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

	angular.module('bpm-common').directive('sdDataTable', ['$parse', '$compile', '$timeout', DataTableDirective]);

	/*
	 * 
	 */
	function DataTableDirective($parse, $compile, $timeout) {
		return {
			restrict : 'A',
			require: ['sdData'],
			compile: function(elem, attr, transclude) {				
				processRawMarkup(elem, attr);

				return {
					post : function(scope, element, attr, ctrl) {
						var dataTableCompiler = new DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl);
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
		
		// Hide the element, till it's ready to be visible
		showElement(elem, false);
		var toolbar = elem.prev();
		if (toolbar.attr('sda-toolbar') == undefined) {
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
	function DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl) {
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

		var elemScope = scope;
		var sdData = ctrl[0];

		var columns = [], dtColumns = [], theTable, theDataTable, theToolbar;

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
			throwError(tagName != 'TABLE', 'Directive must be defined on &lt;table&gt; element, but its defined on ' + tagName + '.');

			// Check <thead>
			var head = element.find('> thead');
			throwError(head.length != 1, '&lt;table&gt; must have at least and at most one &lt;thead&gt; defined.');

			// Check header row
			var headerRow = head.find('> tr');
			throwError(headerRow.length != 1, '&lt;thead&gt; must have at leat and at most one &lt;tr&gt; defined.');

			// Check <tbody>
			var body = element.find('> tbody');
			throwError(body.length != 1, '&lt;table&gt; must have at leat and at most one &lt;tbody&gt; defined.');

			var bodyRow = body.find('> tr');
			throwError(bodyRow.length != 1, '&lt;tbody&gt; must have at leat and at most one &lt;tr&gt; defined.');
		}

		/*
		 * 
		 */
		function throwError(condition, msg) {
			if (condition) {
				throw msg;
			}
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

			throwError(headCols.length != bodyCols.length, 'Number of columns in head and body are not matching.');
			
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
			// Show the element, as it's ready to be visible
			showElement(element, true);
			if (theToolbar) {
				showElement(theToolbar, true);
			}

			// Datatables is adding pixel width to table.
			// TODO: Find better API
			$timeout(function(){
				element.width("100%");
			}, 0, false);
		}

		/*
		 * 
		 */
		function exposeAPIs() {
			if (attr.sdDataTable != undefined && attr.sdDataTable != "") {
				var dataTableAssignable = $parse(attr.sdDataTable).assign;
				if (dataTableAssignable) {
					dataTableAssignable(elemScope, new DataTable());
				}
			}

			/*
			 * Public API
			 */
			function DataTable() {
				this.instance = theDataTable;
				this.refresh = refresh;
				this.reInitialize = reInitialize;
			}
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
	};
})();