  (function(){
    'use strict';

    angular.module('bpm-common').directive('sdActivityCriticality',ActivityCriticality);

    /*
    *
    */

    function ActivityCriticality(){

      return {
        restrict : 'A',
        templateUrl :'plugins/html5-process-portal/scripts/directives/partials/ActivityCriticality.html',
        controller :CriticalityController
      };


    }

    function CriticalityController($scope){
      this.getTimes = function(count){
        return new Array(count);
      }

      $scope.criticalityCtrl = this;
    }


  })();
