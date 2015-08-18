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
(function() {
   'use strict';

   angular.module('bpm-common').directive('sdTrivialManualActivityData',['sdUtilService', TrivialManualActivityDataDirective]);

   /*
    *
    */
   function TrivialManualActivityDataDirective( sdUtilService) {

      return {
         restrict : 'A',
         templateUrl : 'plugins/html5-process-portal/scripts/directives/partials/trivialManualActivityData.html',
         controller : [ '$scope', '$parse', '$attrs', 'sdUtilService', DataController ]
      };
   }
   /**
    *
    */
   function DataController( $scope, $parse, $attrs, sdUtilService) {
      
      var mappingHandler  = $parse($attrs.sdaDataMappings);
      this.dataMappings = mappingHandler($scope);
      var dataHandler  = $parse($attrs.sdaOutData);
      this.outData = dataHandler($scope);
      
      var methodHandler  = $parse($attrs.sdaOnChange);
    
      /**
       * 
       */
      this.onChange = function(){
         if($attrs.sdaOnChange){
            methodHandler($scope);
         }
      }
      $scope.dataCtrl = this;
   }
   
   
})();
