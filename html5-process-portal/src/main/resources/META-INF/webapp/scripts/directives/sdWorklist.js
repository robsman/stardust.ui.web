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

	angular.module('bpm-common').directive('sdWorklist', 
			['$parse', 'sdUtilService', 'sdViewUtilService', 'sdWorklistService', 
			 'sdActivityInstanceService', 'sdProcessDefinitionService', WorklistDirective]);

	/*
	 * 
	 */
	function WorklistDirective($parse, sdUtilService, sdViewUtilService, sdWorklistService, 
			sdActivityInstanceService, sdProcessDefinitionService) {

		var directiveDefObject = {
			restrict : 'AE',
			scope: true, // Creates a new sub scope
			templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/worklist.html',
			compile: function(elem, attr, transclude) {
				processRawMarkup(elem, attr);

				return {
					post: function(scope, element, attr, ctrl) {
						var worklistCompiler = new WorklistCompiler(scope, element, attr, ctrl);
					}
				};
			}
		};

		/*
		 * 
		 */
		function processRawMarkup(elem, attr) {
			// Process Trivial Data Column
			var showTrivialDataColumn = true; // Default
			if (attr.sdaTrivialDataColumn && attr.sdaTrivialDataColumn === 'false') {
				showTrivialDataColumn = false;
			}
			// If not required remove the column
			if (!showTrivialDataColumn) {
				var cols = elem.find('[sda-column="TRIVIAL_DATA"]');
				cols.remove();
			}
			
			// Toolbar
			var toolbar = elem.prev();
			var items = toolbar.find('[sda-column="TRIVIAL_DATA"]');
			items.remove();
		}

		/*
		 * 
		 */
		function WorklistCompiler(scope, element, attr, ctrl) {
			var self = this;

			this.initialize(attr, scope);

			/*
			 * This needs to be defined here as it requires access to scope
			 */
			WorklistCompiler.prototype.safeApply = function() {
				sdUtilService.safeApply(scope);
			};

			// Expose controller as a whole on to scope
			scope.worklistCtrl = this;
			sdUtilService.addFunctionProxies(scope.worklistCtrl);
		}

		/*
		 * 
		 */
		WorklistCompiler.prototype.initialize = function(attr, scope) {
			var scopeToUse = scope.$parent;
			var self = this;

			// Define data
			this.worklist = {};
			this.dataTable = null; // Handle to data table instance, to be set later

			// Process Query
			if (!attr.sdaQuery) {
				throw 'Query attribute is not specified for worklist.';
			}
			var queryGetter = $parse(attr.sdaQuery);
			var query = queryGetter(scopeToUse);
			if (query == undefined) {
				throw 'Query evaluated to "nothing" for worklist.';
			}
			this.query = query;

			// Process Title
			var titleExpr = "";
			if (attr.sdaTitle) {
				titleExpr = attr.sdaTitle;
			}
			var titleGetter = $parse(titleExpr);
			this.title = titleGetter(scopeToUse);

			// Process TableHandle and then set data table instance
			this.tableHandleExpr = 'worklistCtrl.dataTable';

			var unregister = scope.$watch(this.tableHandleExpr, function(newVal, oldVal) {
				if (newVal != undefined && newVal != null && newVal != oldVal) {				
					if (attr.sdWorklist) {
						var assignable = $parse(attr.sdWorklist).assign;
						if (assignable) {
							assignable(scopeToUse, self.dataTable);
						}						
					}
					unregister();
				}
			});

			if (attr.sdaInitialSelection) {
				this.initialSelection = attr.sdaInitialSelection;
			}

			if (attr.sdaSelection) {
				var assignable = $parse(attr.sdaSelection).assign;
				if (assignable) {
					this.selection = null;
					this.selectionExpr = 'worklistCtrl.selection';

					// Update parent for change in sdDataTable
					scope.$watch(this.selectionExpr, function(newVal, oldVal) {
						if (newVal != undefined && newVal != null && newVal != oldVal) {
							assignable(scopeToUse, self.selection);
						}
					});

					// Update for sdDataTable for change in parent 
					scopeToUse.$watch(attr.sdaSelection, function(newVal, oldVal) {
						if (newVal != undefined && newVal != null && newVal != self.selection) {
							self.selection = newVal;
						}
					});
				}
			}

			if (attr.sdaPageSize) {
				this.sdaPageSize = attr.sdaPageSize;
			}

			this.fetchDescriptorCols();
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};
		
		/*
		 * 
		 */
		WorklistCompiler.prototype.fetchPage = function(options) {
			var self = this;
			var deferred = jQuery.Deferred();

			var query = angular.extend({}, this.query);
			query.options = options;

			sdWorklistService.getWorklist(query).then(function(data) {
				self.worklist.list = data.list;
				self.worklist.totalCount = data.totalCount;
				
				var oids = [];
				angular.forEach(self.worklist.list, function(workItem, index){
					if (workItem.trivial == undefined || workItem.trivial) {
						oids.push(workItem.oid);
					}
				});

				sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).then(function(data) {
					self.worklist.trivialManualActivities = data;

					deferred.resolve(self.worklist);

					self.safeApply();
				});
			});

			return deferred.promise();
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.onSelect = function(info) {
			// NOP
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.onPagination = function(info) {
			// NOP
		};
		
		/*
		 * 
		 */
		WorklistCompiler.prototype.fetchDescriptorCols = function() {
			var self = this;

			sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
				self.descritorCols = [];
				angular.forEach(descriptors, function(descriptor){
					self.descritorCols.push({
						field: "descriptors['" + descriptor.id + "'].value",
						title: descriptor.title,
						dataType: descriptor.type,
						sortable: descriptor.sortable
					});
				});
				
				self.ready = true;
				self.safeApply();
			});
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.stopEvent = function(event) {
			sdUtilService.stopEvent(event);
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.activateWorkItem = function(workItem) {
			sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.openNotes = function(workItem) {
			sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid, 
					{"oid": "" + workItem.processInstance.oid}, true);
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.openProcessHistory = function(workItem) {
			sdViewUtilService.openView("processInstanceDetailsView", 
					"processInstanceOID=" + workItem.processInstance.oid, 
					{
						"oid": "" + workItem.oid,
						"processInstanceOID": "" + workItem.processInstance.oid
					}, true
			);
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.complete = function(workItem) {
			var self = this;

			var outData = self.worklist.trivialManualActivities[workItem.oid].inOutData;
			var activityData = {oid: workItem.oid, outData: outData};
			sdActivityInstanceService.completeAll([activityData]).then(function(data) {
				self.refresh();
			});
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.completeAll = function() {
			var self = this;

			// This will always be array as selection mode is 'multiple'
			var selectedWorkItems = self.dataTable.getSelection();
			if (selectedWorkItems.length > 0) {
				var activitiesData = [];
				angular.forEach(selectedWorkItems, function(workItem, index){
					var trivialActivityInfo = self.worklist.trivialManualActivities[workItem.oid];
					if(trivialActivityInfo) {
						var outData = trivialActivityInfo.inOutData;
						activitiesData.push({oid: workItem.oid, outData: outData});
					}
				});

				if (activitiesData.length > 0) {
					sdActivityInstanceService.completeAll(activitiesData).then(function(data) {
						self.refresh();
					});
				} else {
					self.refresh();
				}
			}
		};

		/*
		 * 
		 */
		WorklistCompiler.prototype.openDelegateDialog = function(workItem) {
			
		};

		return directiveDefObject;
	}
})();