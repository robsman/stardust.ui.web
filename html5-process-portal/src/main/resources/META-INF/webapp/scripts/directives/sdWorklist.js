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

	angular.module('bpm-common').directive('sdWorklist', ['$parse', '$q', WorklistDirective]);

	/*
	 * 
	 */
	function WorklistDirective($parse, $q) {
		var directiveDefObject = {
			restrict : 'AE',
			scope: true, // Creates a new sub scope
			templateUrl: 'plugins/html5-process-portal/scripts/directives/partials/worklist.html',
			compile: function(elem, attr, transclude) {
				processRawMarkup(elem, attr);
			},
			controller: ['$attrs', '$scope', 'sdUtilService', 'sdViewUtilService', 'sdWorklistService', 
			             'sdActivityInstanceService', 'sdProcessDefinitionService', WorklistCtrl]
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
		}
		
		var _sdViewUtilService, _sdWorklistService, _sdActivityInstanceService, _sdProcessDefinitionService;

		/*
		 * 
		 */
		function WorklistCtrl($attrs, $scope, sdUtilService, sdViewUtilService, sdWorklistService, 
				sdActivityInstanceService, sdProcessDefinitionService) {
			var self = this;
			var scopeToUse = $scope.$parent;

			// Preserve to use later in life-cycle
			_sdViewUtilService = sdViewUtilService;
			_sdWorklistService = sdWorklistService;
			_sdActivityInstanceService = sdActivityInstanceService;
			_sdProcessDefinitionService = sdProcessDefinitionService;

			this.initialize($attrs, scopeToUse);

			/*
			 * This needs to be defined here as it requires access to $scope
			 */
			WorklistCtrl.prototype.safeApply = function() {
				sdUtilService.safeApply($scope);
			};

			// Expose controller as a whole on to scope
			$scope.worklistCtrl = this;
		}

		/*
		 * 
		 */
		WorklistCtrl.prototype.initialize = function($attrs, scopeToUse) {
			// Define data
			this.worklist = {};
			this.worklist.selectedWorkItems = [];
			this.dataTable = null; // Handle to data table instance, to be set later

			// Process Query
			if (!$attrs.sdaQuery) {
				throw 'Query attribute is not specified for worklist.';
			}
			var queryGetter = $parse($attrs.sdaQuery);
			var query = queryGetter(scopeToUse);
			if (query == undefined) {
				throw 'Query evaluated to "nothing" for worklist.';
			}
			this.query = query;

			// Process Title
			var titleExpr = "";
			if ($attrs.sdaTitle) {
				titleExpr = $attrs.sdaTitle;
			}
			var titleGetter = $parse(titleExpr);
			this.title = titleGetter(scopeToUse);

			// Process TableHandle and then set data table instance
			this.tableHandleExpr = $attrs.sdWorklist;
			if (!this.tableHandleExpr) {
				this.tableHandleExpr = "worklistCtrl.dataTable";
			} else {
				var unregister = scopeToUse.$watch(this.tableHandleExpr, function(newVal, oldVal) {
					if (newVal != undefined && newVal != null && newVal != oldVal) {
						self.dataTable = newVal;
						unregister();
					}
				});
			}

			this.fetchDescriptorCols();
		};

		/*
		 * 
		 */
		WorklistCtrl.prototype.refresh = function() {
			this.dataTable.refresh(true);
		};
		
		/*
		 * 
		 */
		WorklistCtrl.prototype.fetchPage = function(options) {
			var self = this;
			var deferred = jQuery.Deferred();

			var query = angular.extend({}, this.query);
			query.options = options;

			this.worklist.selectedWorkItems = [];

			_sdWorklistService.getWorklist(query).then(function(data) {
				self.worklist.list = data.list;
				self.worklist.totalCount = data.totalCount;
				
				var oids = [];
				angular.forEach(self.worklist.list, function(workItem, index){
					if (workItem.trivial == undefined || workItem.trivial) {
						oids.push(workItem.oid);
					}
				});

				_sdActivityInstanceService.getTrivialManualActivitiesDetails(oids).then(function(data) {
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
		WorklistCtrl.prototype.fetchDescriptorCols = function() {
			var self = this;

			_sdProcessDefinitionService.getDescriptorColumns().then(function(descriptors) {
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
		WorklistCtrl.prototype.activateWorkItem = function(workItem) {
			_sdViewUtilService.openView("activityPanel", "OID=" + workItem.oid, {"oid" : "" + workItem.oid});
		};

		/*
		 * 
		 */
		WorklistCtrl.prototype.openNotes = function(workItem) {
			_sdViewUtilService.openView("notesPanel", "oid=" + workItem.processInstance.oid, 
					{"oid": "" + workItem.processInstance.oid}, true);
		};

		/*
		 * 
		 */
		WorklistCtrl.prototype.openProcessHistory = function(workItem) {
			_sdViewUtilService.openView("processInstanceDetailsView", 
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
		WorklistCtrl.prototype.complete = function(workItem) {
			var self = this;

			var outData = self.worklist.trivialManualActivities[workItem.oid].inOutData;
			var activityData = {oid: workItem.oid, outData: outData};
			_sdActivityInstanceService.completeAll([activityData]).then(function(data) {
				self.refresh();
			});
		};

		/*
		 * 
		 */
		WorklistCtrl.prototype.completeAll = function() {
			var self = this;

			if (this.worklist.selectedWorkItems.length > 0) {
				var activitiesData = [];
				angular.forEach(this.worklist.selectedWorkItems, function(workItem, index){
					var outData = self.worklist.trivialManualActivities[workItem.oid].inOutData;
					activitiesData.push({oid: workItem.oid, outData: outData});
				});
				
				_sdActivityInstanceService.completeAll(activitiesData).then(function(data) {
					self.refresh();
				});
			}
		};

		/*
		 * 
		 */
		WorklistCtrl.prototype.openDelegateDialog = function(workItem) {
			
		};

		return directiveDefObject;
	}
})();