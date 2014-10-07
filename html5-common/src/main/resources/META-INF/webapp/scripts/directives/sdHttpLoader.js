angular.module('bpm-common.directives')
  .directive('sdHttpLoader', ['$parse','$timeout','$injector','eventBus',
    function ($parse, $timeout,$injector,eventBus) {
      return {
        scope: {
          methods: '@',
          template: '@',
          title: '@',
          ttl: '@'
        },
        template: '<div class="http-loader__wrapper" ' +
                  'ng-show="showLoader">'+
                  'Loading...' +
                  '</div>',
        link: function ($scope) {
          
          $scope.methods = $scope.methods || 'GET,PUT,POST,DELETE';
          var methods =  $scope.methods.split(',')
                         .map(function(v){
                            return v.toUpperCase();
                          });

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
        	  console.log("directive received event..");
          },$scope);
          eventBus.onMsg("http.response",toggleLoader,$scope);
          eventBus.onMsg("http.error",toggleLoader,$scope);
        }
      };
    }
  ]);