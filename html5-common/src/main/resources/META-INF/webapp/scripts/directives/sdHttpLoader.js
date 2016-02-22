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
  .directive('sdHttpLoader', ['$parse','$timeout','$injector','eventBus','sdLoggerService',
    function ($parse, $timeout,$injector,eventBus,sdLoggerService) {
      return {
        scope: {
          methods: '@',
          template: '@',
          title: '@',
          ttl: '@'
        },
        template: '<div class="loading" ' +
                  'ng-show="showLoader">'+
                  '<div class="loading-inner"></div>' +
                  '</div>',
        link: function ($scope,elem,attrs) {
          
          $scope.methods = $scope.methods || 'GET,PUT,POST,DELETE';
          var methods =  $scope.methods.split(',')
                         .map(function(v){
                            return v.toUpperCase();
                          });
          var trace = sdLoggerService.getLogger('bpm-common.directives.sdHttpLoader');
          var ttl = $parse($scope.ttl)() || $scope.ttl;
          ttl = angular.isUndefined(ttl) ? 0 : ttl;
          ttl = Number(ttl) * 1000;
          ttl = angular.isNumber(ttl) ? ttl : 0;

          $scope.showLoader = false;

          var timeoutId,
              showLoader = $scope.showLoader;

          var toggleLoader = function (event, method) {
            if (methods.indexOf(method.toUpperCase()) !== -1) {
              showLoader = (event.name === 'http.request');
            } else if (methods.length === 0) {
              showLoader = (event.name === 'http.request');
            }
            
            if (ttl <= 0 || (!timeoutId && !showLoader)) {
              $scope.showLoader = showLoader;
              return;
            } else if (timeoutId) {
              return;
            }

            $scope.showLoader = showLoader;
            timeoutId = $timeout(function () {
              if (!showLoader) {
                $scope.showLoader = showLoader;
              }
              timeoutId = undefined;
            }, ttl);
          };
          
          eventBus.onMsg("http.request",function(e,m){
        	  toggleLoader(e,m);
        	  trace.log("directive received event..");
          },$scope);
          
          eventBus.onMsg("http.response",toggleLoader,$scope);
          eventBus.onMsg("http.error",toggleLoader,$scope);
        }
      };
    }
  ]);