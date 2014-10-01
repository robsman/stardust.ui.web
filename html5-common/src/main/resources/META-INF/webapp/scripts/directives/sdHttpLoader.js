angular.module('bpm-common.directives')
  .directive('sdHttpLoader', ['$rootScope','$parse','$timeout',
    function ($rootScope, $parse, $timeout) {
      return {
        scope: {
          methods: '@',
          template: '@',
          title: '@',
          ttl: '@'
        },
        template: '<div class="http-loader__wrapper" ' +
                  'ng-include="template" ' +
                  'ng-show="showLoader">'+
                  '</div>',
        link: function ($scope) {
          
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
              showLoader = (event.name === 'loaderShow');
            } else if (methods.length === 0) {
              showLoader = (event.name === 'loaderShow');
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

          $rootScope.$on("loaderShow", toggleLoader);
          $rootScope.$on("loaderHide", toggleLoader);
        }
      };
    }
  ])
  .provider('httpInterceptor', function () {
    var domains = [];

    this.whitelist = function (domain) {
      domains.push(domain);
    };

    this.$get = ['$q','$rootScope',function ($q, $rootScope) {
        var numLoadings = 0;
        
        var onWhitelist = function (url) {
          var re;
          for (var i = domains.length; i--;) {
            re = new RegExp(domains[i]);
            if(re.test(url)){
              return true;
            }
          }
          return false;
        };
        
        var checkAndHide = function (config) {
          if (onWhitelist(config.url) &&
            (--numLoadings) === 0) {
            $rootScope.$emit('loaderHide', config.method);
          }
        };

        return {

          request: function (config) {
            if (onWhitelist(config.url)) {
              numLoadings++;
              $rootScope.$emit('loaderShow', config.method);
            }
            return config || $q.when(config);
          },

          response: function (response) {
            checkAndHide(response.config);

            return response || $q.when(response);
          },

          responseError: function (response) {
            checkAndHide(response.config);

            return $q.reject(response);
          }
        };
      }
    ];
  })
  .config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('httpInterceptor');
  }]);