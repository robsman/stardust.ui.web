/*****************************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or
 * initial documentation
 ****************************************************************************************/
(function()
{
   'use strict';

   angular.module('bpm-common').directive('sdMultiSelectFilter',
            [ '$parse', MultiSelectFilter ]);

   /*
    * 
    */
   function MultiSelectFilter()
   {
      return {
         restrict : 'A',
         template : '<select style="height: 75px; min-width: 200px; max-width: 350px;" name="options" ng-model="filterData.like" multiple '
                  + 'ng-options="option.value as option.label for option in filterCtrl.options | orderBy:\'label\'"><\/select>'
                  + '<label ng-bind="i18n(\'portal-common-messages.common-filterPopup-pickListFilter-pickMany-list-message\')"><\/label>',
         controller : [ '$scope','$attrs','$parse', FilterController ],
         link : function(scope, element, attr, ctrl)
         {
            scope.handlers.applyFilter = function()
            {
               scope.setFilterTitle(ctrl.getDisplayText(scope.filterData.like));
               return true;
            };

            /*
             * 
             */
            scope.handlers.resetFilter = function()
            {
               // NOP
            };
         }
      };
   }

   /*
    * 
    */
   function FilterController($scope,$attrs,$parse)
   {
      var optionsBinding = $parse($attrs.sdaOptions);
      this.options = optionsBinding($scope);
      $scope.filterCtrl = this;
   }

   FilterController.prototype.getDisplayText = function(values)
   {
      var filtered = [];
      var self = this;
      angular.forEach(values, function(value)
      {
         for ( var key in self.options)
         {
            if (self.options[key].value === value)
            {
               filtered.push(self.options[key].label);
            }
         }
      });
      return filtered.join(',');
   };

})();
