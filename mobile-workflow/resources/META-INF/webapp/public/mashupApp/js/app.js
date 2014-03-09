define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		angWorkflow = require("js/services/workflowService"),
		app=angular.module('mashupApp',[]),/*create angular application*/
		rootScope,  /*Angular rootScope within a JQuery context*/
		// SG
    	i = require('bpm.portal.Interaction'),
    	g = controller = require('bpm.portal.GenericController'),
    	jui = require('jquery.url');
	
    	var interaction = new bpm.portal.Interaction();
    	var controller = new bpm.portal.GenericController();
	
	app.factory("workflowService",angWorkflow);
	app.controller("main",function($scope,workflowService){
		
		$scope.submitModel = function(){
			console.log($scope.model);
			// SG
			interaction.transfer.AccidentInformation = $scope.model.accident;
			interaction.post();
			interaction.completeActivity();
		},
		
		$scope.getCurrentPosition = function(){
			workflowService.getCurrentPosition()
				.then(function(pos){
					$scope.$apply(function(){
						$scope.model.geoPosition = pos;
						$scope.model.accident.AccidentLocation = pos.coords.latitude + "," + pos.coords.longitude;
					});
			});
		};
		
		$scope.model={
				"geoPostition" : {},
				"title": "Accident Information Form v2.0",
				"accident":{
					"DateOfAccident" : "",
					"AccidentLocation" : "",
					"CarsInvolvedInAccident" : "",
					"YourVehicleTowed" : false,
					"WhereVehicleDamaged" : {
						"Front" : false,
						"Rear" : false,
						"PassengerSide" : false,
						"DriverSide" : false,
						"Hood"  : false,
						"Undercarriage" :  false
					}
				}
		};
		
		// SG
		var scope = $scope;
    	interaction.bind().done(function(){
    		//controller.bind(angular, interaction);
    		scope.model.accident.AccidentLocation = interaction.transfer.AccidentInformation.AccidentLocation.__text;
    		scope.model.accident.DateOfAccident = interaction.transfer.AccidentInformation.DateOfAccident.__text;
    		scope.model.accident.CarsInvolvedInAccident = parseInt(interaction.transfer.AccidentInformation.CarsInvolvedInAccident.__text);
    		scope.model.accident.YourVehicleTowed = interaction.transfer.AccidentInformation.YourVehicleTowed.__text == "true";
    		scope.model.accident.WhereVehicleDamaged.Front = interaction.transfer.AccidentInformation.WhereVehicleDamaged.Front == "true";
    		scope.model.accident.WhereVehicleDamaged.Rear = interaction.transfer.AccidentInformation.WhereVehicleDamaged.Rear == "true";
    		scope.model.accident.WhereVehicleDamaged.PassengerSide = interaction.transfer.AccidentInformation.WhereVehicleDamaged.PassengerSide == "true";
    		scope.model.accident.WhereVehicleDamaged.DriverSide = interaction.transfer.AccidentInformation.WhereVehicleDamaged.DriverSide == "true";
    		scope.model.accident.WhereVehicleDamaged.Hood = interaction.transfer.AccidentInformation.WhereVehicleDamaged.Hood == "true";
    		scope.model.accident.WhereVehicleDamaged.Undercarriage = interaction.transfer.AccidentInformation.WhereVehicleDamaged.Undercarriage == "true";
    		
    		scope.$apply();
    	});
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