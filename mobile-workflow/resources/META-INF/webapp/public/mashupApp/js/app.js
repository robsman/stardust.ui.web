define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		angWorkflow = require("js/services/workflowService"),
		app=angular.module('mashupApp',[]),/*create angular application*/
		rootScope;  /*Angular rootScope within a JQuery context*/
	
	app.factory("workflowService",angWorkflow);
	app.controller("main",function($scope,workflowService){
		
		$scope.submitModel = function(){
			console.log($scope.model);
         workflowService.complete({
            name : "Investigate Accident",
            oid : "85",
            context : "external-webapp"
            }, $scope.model);
		},
		
		$scope.model={
				"title": "Accident Information Form v2.0",
				"accident":{
					"date" : "",
					"location" : "",
					"numVehicles" : "",
					"vehicleTowed" : false,
					"damageLocs" : {
						"front" : false,
						"rear" : false,
						"passSide" : false,
						"driverSide" : false,
						"hood"  : false,
						"underCarriage" :  false
					}
				}
		};
	});
	
	var jsApp = {};
	
	/* 1. bootstrap our document against our angular application, on init function invoke*/
	jsApp.init=function(){
		/*bootstrapping - after this point we have an angular application tied to our html*/
		angular.bootstrap(document,['mashupApp']);
		return app;
	};
	
	return jsApp;
});