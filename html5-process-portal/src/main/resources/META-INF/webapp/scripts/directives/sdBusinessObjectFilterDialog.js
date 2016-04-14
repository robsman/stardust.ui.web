/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/*
 * @author Shrikant.Gangal
 */
(function() {
	'use strict';

	angular.module('bpm-common').directive(
			'sdBusinessObjectFilterDialog',
			[ 'sdUtilService', 'sdBusinessObjectManagementService',
					BusinessObjectFilterDialog ]);

	function BusinessObjectFilterDialog(sdUtilService,
			sdBusinessObjectManagementService) {
		sdBusinessObjectManagementService.getBusinessObjects().then(
				function(json) {
					console.log(json);
				});
		return {
			restrict : 'A',
			templateUrl : sdUtilService.getBaseUrl()
					+ 'plugins/html5-process-portal/scripts/directives/partials/businessObjectFilterDialog.html',
			controller : [ '$scope', 'sdBusinessObjectManagementService',
					BusinessObjectFilterDialogCtrl ]
		};
	}

	function BusinessObjectFilterDialogCtrl($scope,
			sdBusinessObjectManagementService) {
		this.parentScope = $scope;
		this.bomService = sdBusinessObjectManagementService;
		this.open = false;
		$scope.boDialog = this;

		this.initializeBOs();
	}

	BusinessObjectFilterDialogCtrl.prototype.initializeBOs = function() {
		var self = this;
		this.bos = [];
		this.selectedBOInstances = [];
		this.bomService.getBusinessObjects().then(
				function(bos) {
					if (bos && bos.models) {
						jQuery.each(bos.models, function(_, model) {
							jQuery.each(model.businessObjects, function(_, bo) {
								self.bos.push(bo);
							});
						});
					}

					self.boInstances = {};
					jQuery.each(self.bos, function(_, bo) {
						self.bomService.getBusinessObjectInstances(bo).then(
								function(instances) {
									self.boInstances[bo.id] = instances;
								});
					});

				});
	};

	BusinessObjectFilterDialogCtrl.prototype.boSelectionChanged = function() {
		var self = this;
		this.selectedBOInstances = [];
		if (this.selectedBO) {
			jQuery.each(this.selectedBO.fields, function(_, field) {
				if (field.primaryKey) {
					self.primaryKeyForSelectedBO = field.id;
				}
			});
		}
	};

	BusinessObjectFilterDialogCtrl.prototype.filterBySelectedBOs = function() {
		var self = this;
		var boFilter = {
			dataId : self.selectedBO.id,
			primaryKey : self.primaryKeyForSelectedBO,
			identifiers : []
		};
		if (this.selectedBOInstances && this.selectedBOInstances.length > 0) {
			jQuery.each(this.selectedBOInstances, function(_, bo) {
				boFilter.identifiers.push(bo[self.primaryKeyForSelectedBO]);
			});
		} else {
			// TODO
		}

		this.parentScope.activityTableCtrl.boFilter = boFilter;
		this.parentScope.activityTableCtrl.refresh();

		this.closeDialog();
	};

	BusinessObjectFilterDialogCtrl.prototype.resetFilter = function() {
		this.selectedBO = undefined;
		this.selectedBOInstances = [];

		this.parentScope.activityTableCtrl.boFilter = undefined;
		this.parentScope.activityTableCtrl.refresh();

		this.closeDialog();
	};

	BusinessObjectFilterDialogCtrl.prototype.getBOInstanceMatches = function(
			matchStr) {
		var self = this;
		var results = [];
		jQuery.each(this.boInstances[this.selectedBO.id], function(_, v) {
			if (v[self.primaryKeyForSelectedBO].indexOf(matchStr) > -1) {
				results.push(v);
			}
		});

		this.businessObjectInstancesData = results;
	};

	BusinessObjectFilterDialogCtrl.prototype.openDialog = function() {
		this.open = true;
	};

	BusinessObjectFilterDialogCtrl.prototype.closeDialog = function() {
		this.open = false;
	};

	BusinessObjectFilterDialogCtrl.prototype.toggleDialogOpenState = function() {
		if (this.open) {
			this.closeDialog();
		} else {
			this.openDialog();
		}
	};
})();