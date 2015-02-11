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
(function()
{
   'use strict';

   angular.module('bpm-common').directive('sdProcessPriority', ProcessPriority);

   /*
    *
    */
   function ProcessPriority()
   {
      return {
         restrict : 'A',
         template : "<i class=\"fa fa-flag priority-flag\" "+
                      "ng-class=\"'priority-flag-'+rowData.priority\" "+
                      "ng-mouseenter=\'processPriorityCtrl.toolTip.show = true\'"+
                      " ng-mouseleave=\'processPriorityCtrl.toolTip.show = false\'><\/i>"+
                      "<div class=\"popup-dlg worklist-tooltip\" style=\"color: black\" ng-show=\"processPriorityCtrl.toolTip.show\">"+
                         "<span class=\"worklist-tooltip-label\" ng-bind=\"processPriorityCtrl.i18n('views-common-messages.views-activityTable-priorityFilter-table-priorityColumn-name')\"><\/span> "+
                         ": <span ng-bind=\"processPriorityCtrl.i18n('views-common-messages.common-priorities-'+rowData.priority)\"><\/span>" +
                       "<\/div>",
         controller : [ '$scope', ProcessPriorityController ]
      };
   }
   /**
    *
    */
   function ProcessPriorityController($scope, element)
   {
      this.toolTip = {
         show : false
      };
      this.i18n = $scope.i18n;
      $scope.processPriorityCtrl = this;
   }
})();
