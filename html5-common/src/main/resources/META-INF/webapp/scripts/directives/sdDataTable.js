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
			transclude: true,
			template : '<div ng-transclude></div>',
			scope: {
				// TODO: Accept more inputs...
			},
			compile: function(elem, attr, transclude) {
				return {
					post : function(scope, element, attr, ctrl) {
						var dataTableCompiler = new DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl);
					}
				};
			}
		};	
	}];

	/*
	 * 
	 */
	function DataTableCompiler($parse, $compile, $timeout, scope, element, attr, ctrl) {
		// This is same as scope.$parent
		var elemScope = element.scope();

		var sdData = ctrl[0];
		
		var columns = [];
		var toolbarHtml;

		var dtColumns = [];
		var headerHtml = '';

		var theTable;
		var theDataTable;

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
			// Toolbar
			var toolbar = element.find('sd-toolbar');
			if (toolbar.length != undefined && toolbar.length != 0) {
				toolbarHtml = getTemplateContent(toolbar);
			}

			// Columns
			var cols = element.find('sd-column');
			angular.forEach(cols, function(col) {
				col = angular.element(col);

				var colDef = {
					statik: true,
					field: col.attr('sda-field'),
					title: col.attr('sda-title'),
					dataType: col.attr('sda-data-type'),
					sortable: col.attr('sda-sortable')
				};

				var content = col.find('sd-column-template');
				if (content.length == undefined || content.length == 0) {
					colDef.contents = getDefaultContent(colDef);
				} else {
					colDef.contents = getTemplateContent(content);
				}

				columns.push(colDef);
			});

			// TODO: Provide API to rearrange the columns
		}

		/*
		 * 
		 */
		function getTemplateContent(templateElm) {
			return templateElm.html();
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
				headerHtml += '\n<th><div class="tbl-flt-title" ng-click="alert(\"TODO\")">No filter set</div><span>{{' + col.title + '}}</span></th>';
				dtColumns.push({
					"data": colRenderer(col)
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
			var toolbarTemplate =
				'<a href="#" ng-click="" title="Select Columns" class="tbl-toolbar-item tbl-tool-link">' +
					'<i class="fa fa-table"></i>' +
				'</a>' +
				'<a href="#" ng-click="" title="Export Excel" class="tbl-toolbar-item tbl-tool-link">' +
					'<i class="fa fa-file-excel-o"></i>' +
				'</a>' +
				'<a href="#" ng-click="" title="Export CSV" class="tbl-toolbar-item tbl-tool-link">' +
					'<i class="fa fa-file-text-o"></i>' +
				'</a>';

			toolbarTemplate = '<div>' + toolbarTemplate + toolbarHtml + '</div>';

			// Replace the contents
			// TODO: Check if current element is <table> itself
			// Not possible to edit the current elements tag name
			var tableTemplate = '<table style="width: 100%"><thead><tr>HEADERS</tr></thead></table>';
			tableTemplate = tableTemplate.replace('HEADERS', headerHtml);

			var template = toolbarTemplate + tableTemplate;
			element.html(template);

			//theTable = angular.element(element.children()[0]);
			theTable = element.find("table");
			$compile(theTable)(elemScope);
		}

		/*
		 * 
		 */
		function buildDataTable() {
			var dtOptions = {};

			dtOptions.hasOverrideDom = true;
			dtOptions.sDom = "itp";
			dtOptions.sPaginationType = "full_numbers";
			dtOptions.iDisplayLength = 8;

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
			var params = {skip: data.start};
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