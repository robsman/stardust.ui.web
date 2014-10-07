angular.module('bpm-common.directives')
  .directive('sdJsError', ['eventBus',
    function (eventBus) {
      return {
        scope: {
          title: '@'
        },
        template: '<div ng-click="showError=false" class="http-error__wrapper" ' +
                  'ng-show="showError">'+
                  'ERROR! {{errorModel | json}}' + 
                  '</div>',
        link: function ($scope) {
          var showError;
          
          $scope.showError = false;
          $scope.errorModel = {};
          
          var initScope = function(e,m){
            console.log(m);
            $scope.errorModel = m;
            $scope.showError=true;
          };
          eventBus.onMsg("js.error",initScope,$scope);
        }
      };
    }
  ]);