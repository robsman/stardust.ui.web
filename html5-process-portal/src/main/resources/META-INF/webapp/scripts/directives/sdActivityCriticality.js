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

   angular.module('bpm-common').directive('sdActivityCriticality', [ '$parse', ActivityCriticality ]);

   /*
    * 
    */

   function ActivityCriticality() {

      return {
         restrict : 'A',
         template :
        	 '<span ng-mouseenter="criticalityCtrl.toolTip.show = true" ng-mouseleave="criticalityCtrl.toolTip.show = false">\n' +
	        	 '<span data-ng-repeat="t in criticalityCtrl.getFlagCount(criticalityCtrl.criticality.count) track by $index">\n' +
	    	 		'<i ng-show="criticalityCtrl.criticality.color != \'WHITE\' && criticalityCtrl.criticality.color!=\'WHITE_WARNING\'" ' +
	                    'class="pi pi-flag icon-lg" ng-class="\'criticality-flag-\'+criticalityCtrl.criticality.color"></i>\n' +
	                '<i ng-show="criticalityCtrl.criticality.color == \'WHITE\'" '+
	                    'class="pi pi-flag icon-lg criticality-flag-WHITE"></i>\n'+
	                '<i ng-show="criticalityCtrl.criticality.color == \'WHITE_WARNING\'" '+
	                	'class="glyphicon icon-lg criticality-flag-WHITE-WARNING glyphicon-exclamation-triangle"></i>\n' +
	             '</span>\n' +
	        '</span>\n' +
         	'<div class="popup-dlg worklist-tooltip" style="color: black" ng-show="criticalityCtrl.toolTip.show">\n' +
         		'<span class="worklist-tooltip-label" ng-bind="criticalityCtrl.i18n(\'views-common-messages.processHistory-activityTable-criticalityTooltip-criticality\')"></span>: '+
                '<span ng-bind="criticalityCtrl.criticality.label"></span><br/>\n' + 
         		'<span class="worklist-tooltip-label" ng-bind="criticalityCtrl.i18n(\'views-common-messages.processHistory-activityTable-criticalityTooltip-value\')"></span>: '+
                '<span ng-bind="criticalityCtrl.criticality.value">\n' +
            '</div>',
         controller : [ '$scope', '$attrs', '$parse', CriticalityController ]
      };

   }

   function CriticalityController( $scope, $attrs, $parse) {
      
      this.toolTip = {
         show : false
      };

      var criticalityBinding = $parse($attrs.sdaCriticality);
      this.criticality = criticalityBinding($scope);
      this.i18n = $scope.i18n;
      $scope.criticalityCtrl = this;
   };
   
   /**
    * 
    */
   CriticalityController.prototype.getFlagCount = function( count){
	   return new Array(count);
   };
   
})();
