angular.module('bpm-common.services')
.provider('httpInterceptor', function () {
    
    var domains = [];

    this.whitelist = function (domain) {
      domains.push(domain);
    };

    this.$get = ['$q','$rootScope', 'eventBus',function ($q, $rootScope, eventBus) {
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
        
        var checkAndHide = function (config,eventName) {
          if (onWhitelist(config.url) &&
            (--numLoadings) === 0) {
        	if(eventName==='http.error'){
        		eventBus.emitMsg(eventName, config);
        	}
        	else{
        		eventBus.emitMsg(eventName, config.method);
        	}
          }
        };

        return {
          request: function (config) {
            if (onWhitelist(config.url)) {
              numLoadings++;
              eventBus.emitMsg('http.request', config.method);
            }
            return config || $q.when(config);
          },

          response: function (response) {
            checkAndHide(response.config,'http.response');
            return response || $q.when(response);
          },

          responseError: function (response,x) {
            checkAndHide(response.config,'http.error');
            return $q.reject(response);
          }
        };
      }
    ];
});