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
	var sdBusinessObjectManagementService = null;
	var trace = null;
	
	angular.module('workflow-ui').directive(
			'sdBusinessObjectFilterDialog',
			[ 'sdUtilService','$injector','sdLoggerService',
					BusinessObjectFilterDialog ]);

	function BusinessObjectFilterDialog(sdUtilService, $injector, sdLoggerService) {
		
		trace = sdLoggerService.getLogger('bpm-common.sdBusinessObjectFilterDialog');
		
		//Check the presence of sdBusinessObjectManagementService
		if($injector.has('sdBusinessObjectManagementService')) {
			sdBusinessObjectManagementService = $injector.get('sdBusinessObjectManagementService');
			trace.log('sdBusinessObjectManagementService found!');
		} else {
			trace.log('sdBusinessObjectManagementService not found.Operating in non BO mode.');
		}
		
		return {
			restrict : 'A',
			scope: {
				selectedBoMaxDisplayLength: '@sdaSelectedBoMaxDisplayLength'
			},
			templateUrl : sdUtilService.getBaseUrl()
					+ 'plugins/html5-process-portal/scripts/directives/partials/businessObjectFilterDialog.html',
			controller : [ '$scope',
					BusinessObjectFilterDialogCtrl ]
		};
	}

	function BusinessObjectFilterDialogCtrl($scope) {
		this.bomService = sdBusinessObjectManagementService;
		this.i18n = $scope.$parent.i18n;
		this.open = false;
		this.initialized = false;
		this.parentScope = $scope.$parent;
		setBOMaxDisplayLenght.call(this, $scope);
		this.attributes = $scope.attributes;


		$scope.boDialog = this;
	}

	function setBOMaxDisplayLenght(scope) {
		if (scope.selectedBoMaxDisplayLength) {
			try {
				this.selectedBoMaxDisplayLength = parseInt(scope.selectedBoMaxDisplayLength);
			} catch (e) {
				this.selectedBoMaxDisplayLength = 35;
			}
		} else {
			this.selectedBoMaxDisplayLength = 35;
		}
	}

	BusinessObjectFilterDialogCtrl.prototype.initializeBOs = function() {
    if (this.initialized) { return; }
	  
		var self = this;
		this.bos = [];
		this.selectedInstances = [];
		this.boInstances = {};
		
		this.bomService.getBusinessObjects().then(
				function(bos) {
					if (bos && bos.models) {
						jQuery.each(bos.models, function(_, model) {
							jQuery.each(model.businessObjects, function(_, bo) {
								self.bos.push(bo);
							});
						});
					}
					self.initialized = true;
				});
	};

	/**
	 *   fetch each bo instance on demand
	 */
	BusinessObjectFilterDialogCtrl.prototype.populateInstances = function(bo) {
	  var self = this;
	  
	  
  }
	
	BusinessObjectFilterDialogCtrl.prototype.setSelectedBOInstancesString = function() {
		var self = this;
		var str;
		if (this.selectedBOInstances && this.selectedBOInstances.length > 0) {
			str = this.selectedBO.name + ": ";
			jQuery.each(this.selectedBOInstances, function(_, bo) {
				str = str + bo[self.primaryKeyForSelectedBO] + ","
			});
		}

		this.selectedBOInstancesString = truncateStringToLength(str, this.selectedBoMaxDisplayLength);
	};

	function truncateStringToLength(str, length) {
		if (str !== undefined && str !== null) {
			if (str.length <= (length + 1)) { // "+ 1" is to compensate for the trailing comma
				return truncateTralingComma(str)
			} else {
				if (length > 3) {
					return str.substring(0, (length - 3)) + "..."
				} else {
					return "...";
				}
			}
		}

		return str;
	}

	function truncateTralingComma(str) {
		return str.substring(0, (str.length - 1));
	}

	BusinessObjectFilterDialogCtrl.prototype.boSelectionChanged = function() {
		var self = this;
		this.selectedInstances = [];
		
		if (this.selectedType) {
  		  if(self.boInstances[this.selectedType.id]) {
          self.refreshInstances();
    		} else {
    		  jQuery("body").css("cursor", "progress");
    		  this.bomService.getBusinessObjectInstances(this.selectedType).then(
                function(instances) {
          self.boInstances[self.selectedType.id] = instances;
          jQuery("body").css("cursor", "default");
          self.refreshInstances();
        });
      }
    }
	};

	BusinessObjectFilterDialogCtrl.prototype.refreshInstances = function() {
	  var self = this;
	  
	  jQuery.each(self.selectedType.fields, function(_, field) {
      if (field.primaryKey) {
        self.primaryKeyForSelectedBO = field.id;
      }
    });	    
	}
	
	BusinessObjectFilterDialogCtrl.prototype.applyFilter = function() {
		var self = this;
		var boFilter = {
			dataId : self.selectedType.id,
			primaryKey : self.primaryKeyForSelectedBO,
			identifiers : []
		};
		if (this.selectedInstances && this.selectedInstances.length > 0) {
			jQuery.each(this.selectedInstances, function(_, bo) {
				boFilter.identifiers.push(bo[self.primaryKeyForSelectedBO]);
			});
		}

		this.parentScope.activityTableCtrl.boFilter = boFilter;
		this.parentScope.activityTableCtrl.refresh();

		this.selectedBO = this.selectedType;
		this.selectedBOInstances = this.selectedInstances;
		this.setSelectedBOInstancesString();

		this.closeDialog();
	};

	BusinessObjectFilterDialogCtrl.prototype.resetFilter = function() {
		this.selectedBO = undefined;
		this.selectedBOInstances = [];

		this.parentScope.activityTableCtrl.boFilter = undefined;
		this.parentScope.activityTableCtrl.refresh();
		this.setSelectedBOInstancesString();

		this.closeDialog();
	};

	BusinessObjectFilterDialogCtrl.prototype.getBOInstanceMatches = function(
			matchStr) {
		var self = this;
		var results = [];
		jQuery.each(this.boInstances[this.selectedType.id], function(_, v) {
			if (v[self.primaryKeyForSelectedBO].indexOf(matchStr) > -1) {
				results.push(v);
			}
		});

		this.businessObjectInstancesData = results;
	};

	BusinessObjectFilterDialogCtrl.prototype.openDialog = function() {
	  if(this.bomService) {
      this.initializeBOs();
      this.selectedType = this.selectedBO;
      this.selectedInstances = this.selectedBOInstances;
      if (this.selectedType) {
          this.refreshInstances();
      }
      this.open = true;
    }
	};

	BusinessObjectFilterDialogCtrl.prototype.closeDialog = function() {
		this.selectedType = undefined;
		this.selectedInstances = [];
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
