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
(function()
{
   'use strict';

   angular.module('bpm-common').directive('sdStatusFilter',
            [ 'sdStatusService', StatusFilter ]);

   /*
    * 
    */
   function StatusFilter()
   {
      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/StatusFilter.html',
         controller : [ '$scope', 'sdStatusService', StatusFilterController ],
         link : function(scope, element, attr, ctrl)
         {
            scope.handlers.applyFilter = function()
            {

               if (scope.filterData.like.length < 1)
               {
                  return false;
               }
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
   function StatusFilterController($scope, sdStatusService)
   {

      var self = this;

      this.intiliaze($scope, sdStatusService);

      $scope.statusFilterCtrl = this;

   }

   StatusFilterController.prototype.intiliaze = function($scope, sdStatusService)
   {
      var self = this;
      this.i18n = $scope.$parent.i18n;
      this.statuses = [];
      sdStatusService.getAllActivityStates().then(function(value)
      {
         self.statuses = value;
      });
   };

   StatusFilterController.prototype.getDisplayText = function(values)
   {
      var filtered = [];
      var self = this;
      angular.forEach(values, function(value)
      {
         for ( var key in self.statuses)
         {
            if (self.statuses[key].value === value)
            {
               filtered.push(self.statuses[key].label);
            }
         }
      });
      return filtered.join(',');
   };

})();
