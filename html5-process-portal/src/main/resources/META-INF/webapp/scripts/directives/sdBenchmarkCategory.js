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

   angular.module('bpm-common').directive( 'sdBenchmarkCategory', [ BenchmarkDirective]);

   /*
    *
    */
   function BenchmarkDirective() {
	   return {
		   restrict : 'A',
		   scope : {
			   sdaValue : '=',
		   },
		   template : '<i ng-if="sdaValue.color" class="pi pi-flag pi-lg" ng-style="benchmarkCtrl.flagStyle" '+
		   						 'sd-popover sda-template="\'benchmarkPopoverTemplate.html\'" sda-trigger="mouseenter" sda-placement="top auto" '+
		   			      '<\/i>'+
                  '<script id="benchmarkPopoverTemplate.html" type="text/ng-template">'+
      		   			  '<div>'+
      		   			  		'<span class="worklist-tooltip-label" ng-bind="benchmarkCtrl.i18n(\'views-common-messages.views-processTable-benchmark-tooltip-categoryLabel\')"><\/span> '+
      		   			  		': <span ng-bind="sdaValue.label"><\/span>' +
      		   			  '</div>'+
                  '</script>',
		   controller : [ '$scope', BenchmarkController ]
	   };
   };
   /**
    *
    */
   function BenchmarkController( $scope ) {
	   this.toolTip = {
         show : false
      };

	   this.flagStyle = {color : $scope.sdaValue.color};

      this.i18n = $scope.$parent.i18n;
      $scope.benchmarkCtrl = this;
   }
})();
