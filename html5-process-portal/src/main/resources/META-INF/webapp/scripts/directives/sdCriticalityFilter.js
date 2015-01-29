(function () {
  'use strict';

  angular.module('bpm-common').directive('sdCriticalityFilter', ['sdCriticalityService', PriorityFilter]);

    /*
    */
    function PriorityFilter(){

      return {
        restrict : 'A',
        templateUrl :'plugins/html5-process-portal/scripts/directives/partials/CriticalityFilter.html',
        scope :{
          sdaSelectedCriticalities : '='
        },
        controller :['$scope','sdCriticalityService',CriticalityFilterController]
      };
    }


    /*
    *
    */
    var CriticalityFilterController = function($scope,sdCriticalityService){
      var self = this;
      this.i18n = $scope.$parent.i18n;
      this.intialize($scope);

      this.loadAvailableCriticalities = function(){
        sdCriticalityService.getAllCriticalities().then(function(criticalities) {
         self.availableCriticalities = criticalities;
      });
      }

        this.loadAvailableCriticalities();
        $scope.criticalityCtrl = this;
      };

    /*
    *
    */
    CriticalityFilterController.prototype.intialize = function ($scope){

      $scope.sdaSelectedCriticalities=[];
      this.data = [];
      this.availableCriticalities = [];
      this.matchVal="";
      $scope.criticalityCtrl = this;
    };
    /*
    *
    */
    CriticalityFilterController.prototype.tagPreMapper=function (item,index){
      var tagClass="fa fa-flag criticality-flag-"+item.color;
      return tagClass;
    };


      /**
    *
    */
    CriticalityFilterController.prototype.getCriticalities = function(value) {

      var results=[];

      this.availableCriticalities.forEach(function(v){
        if(v.label.indexOf(value) > -1){
          results.push(v);
        }
      });

      this.data=results;
    };

  })();
