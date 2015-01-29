(function(){
  'use strict';


  angular.module('workflow-ui.services').provider('sdPriorityService',function(){
    this.$get = ['$q', 'sgI18nService', function ($q, sgI18nService) {
      var service = new PriorityService($q, sgI18nService);
      return service;
    }];
  });

  /**
  *
  */
  function PriorityService($q, sgI18nService) {

    this.priorities = [
    {"id": "0", "name" : sgI18nService.translate('views-common-messages.common-priorities-low'), "category" : "low"},
    {"id": "1", "name" : sgI18nService.translate('views-common-messages.common-priorities-normal'), "category" : "normal"},
    {"id": "2", "name" : sgI18nService.translate('views-common-messages.common-priorities-high'), "category" : "high"}
    ];


    PriorityService.prototype.getAllPriorities = function() {
      var deferred = $q.defer();
      deferred.resolve(this.priorities);
      return deferred.promise;
    };

  }

})();
