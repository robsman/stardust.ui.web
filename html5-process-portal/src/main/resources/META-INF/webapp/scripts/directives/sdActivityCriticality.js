  (function(){
    'use strict';

    angular.module('bpm-common').directive('sdActivityCriticality',ActivityCriticality);

    /*
    *
    */

    function ActivityCriticality(){

      return {
        restrict : 'A',
        templateUrl :'plugins/html5-process-portal/scripts/directives/partials/ActivityCriticality.html'
      };

    }

  })();
