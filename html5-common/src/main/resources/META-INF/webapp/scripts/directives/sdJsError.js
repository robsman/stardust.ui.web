/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

angular.module('bpm-common.directives')
  .directive('sdJsError', ['eventBus', 'sdLoggerService',
    function (eventBus,sdLoggerService) {
      return {
        scope: {
          title: '@'
        },
        template: '<div  ng-click="dismiss()" class="message warning" ' +
                  'ng-show="showError">'+
                  '<i class="glyphicon glyphicon-exclamation-sign"></i>' +
                  'ERROR: {{errorModel | json}}' + 
                  '<div class="right">' +
                  '<span class="bubble dark">{{errors.length}}</span>' +
                  '<span class="bubble transparent">&#xd7;</span>' +
                  '</div>' +
                  '</div>',
        link: function ($scope) {
          var showError;
          
          $scope.errors =[];
          $scope.showError = false;
          $scope.errorModel = {};
          
          $scope.dismiss =function(){
        	  $scope.errors.pop();
        	  if($scope.errors.length===0){
        		  $scope.showError=false;
        	  }
          }
          
          var trace = sdLoggerService.getLogger('bpm-common.directives.sdJsError');
          
          var initScope = function(e,m){
        	trace.log(m);
            $scope.errors.push(m);
            $scope.errorModel = $scope.errors[$scope.errors.length-1];
            $scope.showError=true;
          };
          
          var resetScope = function(e,m){
        	trace.log(m);
            $scope.errors.pop();
      	  	if($scope.errors.length===0){
      		  $scope.showError=false;
      	  	}
          };
          
          eventBus.onMsg("js.error",initScope,$scope);
          
          eventBus.onMsg("js.error.reset",resetScope,$scope);
        }
      };
    }
  ]);