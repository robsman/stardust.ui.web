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
 * @author Johnson.Quadras
 */

(function(){
   'use strict';
   angular.module('bpm-common').directive('sdBooleanFilter', ['sdUtilService', BooleanFilterDirective]);

   /*
    * 
    */
   function BooleanFilterDirective (sdUtilService) {
      return {
         restrict: 'A',
         template: '<form name="filterForm">'+
                        '<table> <tr>'+
                           '<td><label class="label-form"> {{i18n(\'views-common-messages.views-common-column-select\')}}</label></td>'+
                           '<td><input type="radio" style="margin-top: -4px;" ng-model="filterData.equal" ng-value="true"></td>'+
                           '<td><label  class="label-form"> {{i18n(\'portal-common-messages.common-true\')}}</label></td></td>'+
                           '<td><input type="radio" style="margin-top: -4px;" ng-model="filterData.equal" ng-value="false"></td>'+
                           '<td><label  class="label-form"> {{i18n(\'portal-common-messages.common-false\')}}</label></td></td>'+
                        '</tr></table>'+
                     ' </form>',
         link: function(scope, element, attr, ctrl) {
            /*
             * 
             */
            scope.handlers.applyFilter = function() {

               if (scope.filterForm.$valid) {
                  scope.setFilterTitle( scope.filterData.equal);
                  return true;
               }
               return false;
            };

            /*
             * 
             */
            scope.handlers.resetFilter = function() {
               // NOP
            };
         }
      };
   }
})();