  (function(){
    'use strict';

    angular.module('bpm-common').directive('sdProcessPriority',ProcessPriority);

    /*
    *
    */

    function ProcessPriority(){

      return {
        restrict : 'A',
        templateUrl :'plugins/html5-process-portal/scripts/directives/partials/ProcessPriority.html'
      };

    }

  })();
