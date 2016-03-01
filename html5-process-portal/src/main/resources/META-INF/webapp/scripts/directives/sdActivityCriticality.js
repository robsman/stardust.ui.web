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
            '<span sd-popover sda-template="\'criticalityTemplate.html\'" sda-trigger="mouseenter" sda-placement="top">\n' +
	       	 	'<span data-ng-repeat="t in criticalityCtrl.getFlagCount(criticalityCtrl.criticality.count) track by $index">\n' +
	   	 		'<i ng-show="criticalityCtrl.criticality.color != \'WHITE\' && criticalityCtrl.criticality.color!=\'WHITE_WARNING\'" ' +
	                   'class="pi pi-flag pi-lg" ng-class="\'criticality-flag-\'+criticalityCtrl.criticality.color"></i>\n' +
	               '<i ng-show="criticalityCtrl.criticality.color == \'WHITE\'" '+
	                   'class="pi pi-flag pi-lg criticality-flag-WHITE"></i>\n'+
	               '<i ng-show="criticalityCtrl.criticality.color == \'WHITE_WARNING\'" '+
	               	'class="pi pi-lg criticality-flag-WHITE-WARNING pi-information"></i>\n' +
	             '</span>\n' +
	        '</span>\n' +
            '<script type="text/ng-template" id="criticalityTemplate.html">'+
	            '<div style="color: black">\n' +
    		     		'<span class="worklist-tooltip-label" ng-bind="criticalityCtrl.i18n(\'views-common-messages.processHistory-activityTable-criticalityTooltip-criticality\')"></span>: '+
    		        '<span ng-bind="criticalityCtrl.criticality.label"></span><br/>\n' +
    		     		'<span class="worklist-tooltip-label" ng-bind="criticalityCtrl.i18n(\'views-common-messages.processHistory-activityTable-criticalityTooltip-value\')"></span>: '+
    		        '<span ng-bind="criticalityCtrl.criticality.value">\n' +
	            '</div> '+
	        '</script>',
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
