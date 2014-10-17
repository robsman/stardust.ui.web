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
  .directive('sdJsError', ['eventBus',
    function (eventBus) {
      return {
        scope: {
          title: '@'
        },
        template: '<div  ng-click="dismiss()" class="message warning" ' +
                  'ng-show="showError">'+
                  '<i class="fa fa-warning"></i>' +
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
          
          var initScope = function(e,m){
            console.log(m);
            $scope.errors.push(m);
            $scope.errorModel = $scope.errors[$scope.errors.length-1];
            $scope.showError=true;
          };
          eventBus.onMsg("js.error",initScope,$scope);
        }
      };
    }
  ]);