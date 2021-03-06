/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
   'use strict';
   angular.module('bpm-common').directive('sdBooleanTableFilter', [ BooleanFilterDirective ]);

   /*
    * 
    */
   function BooleanFilterDirective() {
      return {
         restrict : 'A',
         template : '<form name="filterForm">'
                  +  '<table>'
                  +      '<tr>'
                  +          '<td><label class="label-item" style="margin-right : 20px;"> {{i18n(\'views-common-messages.views-common-column-select\')}}</label></td>'
                  +           '<td><input type="radio" style="margin-top: -4px; margin-left: 5px;" ng-model="filterData.equals" ng-value="true"></td>'
                  +           '<td><label  class="label-item"> {{i18n(\'portal-common-messages.common-true\')}}</label></td></td>'
                  +           '<td><input type="radio" style="margin-top: -4px; margin-left: 5px;" ng-model="filterData.equals" ng-value="false"></td>'
                  +           '<td><label  class="label-item"> {{i18n(\'portal-common-messages.common-false\')}}</label></td></td>'
                  +      '</tr>'
                  +   '</table>' 
                  + '</form>',
         link : function(scope, element, attr, ctrl) {
            /*
             * 
             */
            scope.handlers.applyFilter = function() {
               if (scope.filterForm.$valid) {
                  scope.setFilterTitle(scope.filterData.equals);
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

            /*
			 * Return true to show data
			 */
			scope.handlers.filterCheck = function(rowData, value) {
				var filterData = scope.filterData;

				if (value != undefined && value != null) {
					return filterData.equals == value;
				}

				return true;
			};
         }
      };
   }
})();