(function(){
  'use strict';


  angular.module('workflow-ui.services').provider('sdCriticalityService',function(){
    this.$get = ['$q', '$resource', function ($q, $resource) {
      var service = new CriticalityService($q, $resource);
      return service;
    }];
  });

  /**
  *
  */
  function CriticalityService($q, $resource) {
    var REST_URL = "services/rest/portal/activity-instances/availableCriticalities";

    CriticalityService.prototype.getAllCriticalities = function() {
      return  $resource(REST_URL).query().$promise;
    };

  }

})();
