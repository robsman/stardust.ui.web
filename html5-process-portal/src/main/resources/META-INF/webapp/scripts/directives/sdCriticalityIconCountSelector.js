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
 * @author Nikhil.Gahlot
 */
(function() {
   'use strict';

   angular.module('bpm-common').directive('sdCriticalityIconCountSelector', [ CriticalityIconCountSelector ]);

   /*
    * 
    */

   function CriticalityIconCountSelector() {

      return {
         restrict : 'A',
         template :
        	 '<span ng-repeat="count in criticalityIconCountSelectorCtrl.flagCounts">'
				+ '<button ng-click="criticalityIconCountSelectorCtrl.setIconCount(count)"'
					+ ' class="button-link tbl-tool-link" ng-disabled="editMode != true"> '
					+ '<i class="glyphicon glyphicon-flag criticality-flag" '
					+ 'ng-class="\'criticality-flag-\'+ (count <= bindModel ? icon : \'NO-COLOR\')"><\/i>'
				+ '<\/button>'
			+ '</span>',
		 scope : {
			 bindModel:'=ngModel',
			 editMode:'=sdaEditMode',
			 icon:'=sdaIcon'
		 },
         controller : [ '$scope', CriticalityIconCountController ]
      };

   }

   function CriticalityIconCountController($scope) {
      
      this.i18n = $scope.i18n;
      
      this.flagCounts = [1, 2, 3, 4 ,5];

      this.setIconCount = function(count) {
    	  $scope.bindModel = count;
      };
      
      $scope.criticalityIconCountSelectorCtrl = this;
   };
})();
