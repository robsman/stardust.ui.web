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

   angular.module('bpm-common').directive('sdActivityCriticality', ActivityCriticality);

   /*
    * 
    */

   function ActivityCriticality()
   {

      return {
         restrict : 'A',
         template : "<span data-ng-repeat=\"t in criticalityCtrl.getTimes(rowData.criticality.count) track by $index\" "+
                        "ng-mouseenter=\"criticalityCtrl.toolTip.show = true\" "+
                         "ng-mouseleave=\"criticalityCtrl.toolTip.show = false\"> "+
                         "<i ng-show=\"rowData.criticality.color != 'WHITE' && rowData.criticality.color!='WHITE_WARNING'\" "+
                         "class=\"fa fa-flag criticality-flag\" "+
                         "ng-class=\"'criticality-flag-'+rowData.criticality.color\"><\/i>  "+
                         "<i ng-show=\"rowData.criticality.color == 'WHITE'\" "+
                         "class=\"fa fa-flag-o criticality-flag criticality-flag-WHITE\"><\/i>  "+
                         "<i ng-show=\"rowData.criticality.color == 'WHITE_WARNING'\" "+
                         "class=\"fa criticality-flag criticality-flag-WHITE-WARNING  fa-exclamation-triangle\"><\/i>" +
                   "<\/span> "+
                   "<div class=\"popup-dlg worklist-tooltip\" style=\"color: black\" "+
                      "ng-show=\"criticalityCtrl.toolTip.show\"> "+
                      "<span class=\"worklist-tooltip-label\" ng-bind=\"criticalityCtrl.i18n('views-common-messages.processHistory-activityTable-criticalityTooltip-criticality')\"><\/span> "+
                      ": <span ng-bind=\"rowData.criticality.label\"><\/span><br> <span class=\"worklist-tooltip-label\""+
                      "ng-bind=\"criticalityCtrl.i18n('views-common-messages.processHistory-activityTable-criticalityTooltip-value')\"><\/span> "+
                      ": <span ng-bind=\"rowData.criticality.value\">" +
                   "<\/div> ",
         controller : CriticalityController
      };

   }

   function CriticalityController($scope)
   {
      this.getTimes = function(count)
      {
         return new Array(count);
      }
      this.toolTip = {
         show : false
      };
      this.i18n = $scope.i18n;
      $scope.criticalityCtrl = this;
   }

})();
