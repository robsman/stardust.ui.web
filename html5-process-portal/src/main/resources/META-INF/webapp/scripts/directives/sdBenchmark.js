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

   angular.module('bpm-common').directive( 'sdBenchmark', [ BenchmarkDirective]);

   /*
    * 
    */
   function BenchmarkDirective() {
	   return {
		   restrict : 'A',
		   scope : {
			   sdaBenchmarkValue : '=',
		   },
		   template : '<i class="glyphicon glyphicon-flag portal-glyph-icon" '+
		   						'ng-style="benchmarkCtrl.flagStyle" '+
		   						'ng-mouseenter=\'benchmarkCtrl.toolTip.show = true\' '+
		   						'ng-mouseleave=\'benchmarkCtrl.toolTip.show = false\'> '+
		   			  '<\/i>'+
		   			  '<div class="popup-dlg worklist-tooltip" style="color: black" ng-show="benchmarkCtrl.toolTip.show">'+
		   			  		'<span class="worklist-tooltip-label" ng-bind="benchmarkCtrl.i18n(\'views-common-messages.views-processTable-benchmark-tooltip-categoryLabel\')"><\/span> '+
		   			  		': <span ng-bind="sdaBenchmarkValue.label"><\/span>' +
		   			  '<\/div>',
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
	   
	   this.flagStyle = {color : $scope.sdaBenchmarkValue.color};
      
      this.i18n = $scope.$parent.i18n;
      $scope.benchmarkCtrl = this;
   }
})();
