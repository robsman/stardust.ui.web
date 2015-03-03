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

   angular.module('bpm-common').directive('sdProcessPriority',
            [ '$parse', ProcessPriority ]);

   /*
    * 
    */
   function ProcessPriority() {
      return {
         restrict : 'A',
         template : "<i class=\"glyphicon glyphicon-flag priority-flag\" "+
                      "ng-class=\"'priority-flag-'+processPriorityCtrl.priority\" "+
                      "ng-mouseenter=\'processPriorityCtrl.toolTip.show = true\'"+
                      " ng-mouseleave=\'processPriorityCtrl.toolTip.show = false\'><\/i>"+
                      "<div class=\"popup-dlg worklist-tooltip\" style=\"color: black\" ng-show=\"processPriorityCtrl.toolTip.show\">"+
                         "<span class=\"worklist-tooltip-label\" ng-bind=\"processPriorityCtrl.i18n('views-common-messages.views-activityTable-priorityFilter-table-priorityColumn-name')\"><\/span> "+
                         ": <span ng-bind=\"processPriorityCtrl.i18n('views-common-messages.common-priorities-'+processPriorityCtrl.priority)\"><\/span>" +
                       "<\/div>",
         controller : [ '$scope', '$attrs', '$parse', ProcessPriorityController ]
      };
   }
   /**
    *
    */
   function ProcessPriorityController($scope, $attrs, $parse) {
      this.toolTip = {
         show : false
      };

      var priorityBinding = $parse($attrs.sdaPriority);
      this.priority = priorityBinding($scope);
      this.i18n = $scope.i18n;
      $scope.processPriorityCtrl = this;
   }
})();
