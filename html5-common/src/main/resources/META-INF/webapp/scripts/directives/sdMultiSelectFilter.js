/*****************************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or
 * initial documentation
 ****************************************************************************************/
(function() {
   'use strict';

   angular.module('bpm-common').directive('sdMultiSelectFilter',
            [ '$parse', 'sdUtilService', MultiSelectFilter ]);

   /*
    * 
    */
   function MultiSelectFilter($parse, sdUtilService) {
      return {
         restrict : 'A',
         template : '<select style="min-width: 200px; max-width: 350px;" name="options" ng-model="filterData.like" '
                  + 'ng-options="option[value] as option[label] for option in filterCtrl.options | orderBy:\'{{label}}\'"><\/select>'
                  + '<label ng-show="multiple" ng-bind="i18n(\'portal-common-messages.common-filterPopup-pickListFilter-pickMany-list-message\')"><\/label>',
         controller : [ '$scope','$attrs','$parse', FilterController ],
         compile: function (elem, attr) {
			if (!attr.sdaMultiple || attr.sdaMultiple == 'true') {
				var selectElem = elem.find('select');
				selectElem.attr('multiple', '');
				selectElem.attr('height', '75px');
			}

			return function(scope, element, attr, ctrl) { // Link
																		// Function
				if (attr.sdaMultiple == 'false') {
					scope.multiple = false;
				} else {
					scope.multiple = true;
				}

				scope.value = 'value';
				scope.label = 'label';

				if (attr.sdaValue) {
					scope.value = attr.sdaValue;
				}

				if (attr.sdaLabel) {
					scope.label = attr.sdaLabel;
				}

				/**
				 * 
				 */
				scope.handlers.applyFilter = function() {
					var values = scope.filterData.like;
					if (scope.multiple === false) {
						values = [scope.filterData.like];
					}
					scope.setFilterTitle(sdUtilService.truncateTitle(ctrl.getDisplayText(values, scope.label)));
					return true;
				};

				/*
				 * 
				 */
				scope.handlers.resetFilter = function() {
					// NOP
				};

				/*
				 * Return true to show data
				 */
				scope.handlers.filterCheck = function(rowData) {
					var filterData = scope.filterData;
					var value = rowData[scope.colData.field];

					var values = filterData.like;
					if (!values || values.length == 0) {
						return true;
					}

					if (scope.multiple === false) {
						values = [values];
					}

					if (value) {
						for (var i in values) {
							if (values[i] != undefined && values[i] != null) {
								if (values[i] == value) {
									return true;
								}
							}
						}
					}

					return false;
				};
			}
         }
      };
   }

   /*
	 * 
	 */
   function FilterController($scope, $attrs, $parse) {
      var optionsBinding = $parse($attrs.sdaOptions);
      this.options = optionsBinding($scope);
      $scope.filterCtrl = this;
   }

   /**
	 * 
	 */
   FilterController.prototype.getDisplayText = function(values, label) {
      var filtered = [];
      var self = this;
      angular.forEach(values, function(value) {
         for ( var key in self.options) {
            if (self.options[key].value === value) {
               filtered.push(self.options[key][label]);
            }
         }
      });
      return filtered.join(',');
   };

})();
