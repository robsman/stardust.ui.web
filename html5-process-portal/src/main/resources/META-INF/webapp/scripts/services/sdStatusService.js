(function(){
  'use strict';


  angular.module('workflow-ui.services').provider('sdStatusService',function(){
    this.$get = ['$q', 'sgI18nService', function ($q, sgI18nService) {
      var service = new StatusService($q, sgI18nService);
      return service;
    }];
  });

  /**
  *
  */
  function StatusService($q, sgI18nService) {

    this.statuses = [
      {
        "id" : "Aborted",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-aborted')
      },
      {
        "id" : "Aborting",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-aborting')
      },
      {
        "id" : "Application",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-application')
      },
      {
        "id" : "Completed",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-completed')
      },
      {
        "id" : "Created",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-created')
      },
      {
        "id" : "Hibernated",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-hibernated')
      },
      {
        "id" : "Interupted",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-interrupted')
      },
      {
        "id" : "Suspended",
        "name":sgI18nService.translate('views-common-messages.views-activityTable-statusFilter-suspended')
      }

      ];


    StatusService.prototype.getAllStatuses = function() {
      var deferred = $q.defer();
      deferred.resolve(this.statuses);
      return deferred.promise;
    };

  }

})();

