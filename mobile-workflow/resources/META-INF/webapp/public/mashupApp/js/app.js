define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		angWorkflow = require("js/services/workflowService"),
		$ = require("jquery"),
		jqm=require("jquery-mobile"),
		app=angular.module('mashupApp',[]),/*create angular application*/
		rootScope,  /*Angular rootScope within a JQuery context*/
    	i = require('bpm.portal.Interaction'),
    	jui = require('jquery.url');
	
    	var interaction = new bpm.portal.Interaction();
    	
	app.factory("workflowService",angWorkflow);
	
	$( "#mashupMain" ).on( "pageinit", function( event ) {
		console.log("Page init, MashupMain");
	});
	
	
	app.controller("main",function($scope,workflowService){
		

		$scope.complete = function(){
			/*TODO: handle this better*/
			delete interaction.transfer.PersonIn;
			delete interaction.transfer.AccidentInformationIn;
			
			interaction.transfer.AccidentInformationOut = $scope.model.accident;
			interaction.transfer.PersonOut= $scope.model.person;
			interaction.post(); // This should actually be called from within interaction.completeActivity() 
			interaction.completeActivity();
		},
		
		$scope.suspend = function(){
			/*TODO: handle this better*/
			delete interaction.transfer.PersonIn;
			delete interaction.transfer.AccidentInformationIn;
			interaction.suspendActivity();
		};
		
		$scope.suspendAndSave = function(){
			/*TODO: handle this better*/
			delete interaction.transfer.PersonIn;
			delete interaction.transfer.AccidentInformationIn;
			
			interaction.transfer.AccidentInformationOut = $scope.model.accident;
			interaction.transfer.PersonOut= $scope.model.person;
			interaction.suspendActivity(true);
		};
		
		$scope.getCurrentPosition = function(){
			workflowService.getCurrentPosition()
				.then(function(pos){
					workflowService.getAddress( pos.coords.longitude,pos.coords.latitude)
					.then(
						function(data){
							console.log("Reverse geo returned...");
							console.log(data);
							$scope.$apply(function(){
								$scope.model.geoPosition = pos;
								$scope.model.accident.AccidentLocation = data.display_name;
							});
						}
					).catch(
						function(e){
							console.log("Promise returned error...");
							console.log(e);
					});
			});
		};
		
		$scope.model={
				"geoPostition" : {},
				"title": "Accident Information Form v2.0",
				"person" : {"FirstName" : "John", "LastName" : "Doe", "PolicyNumber" : "12345"},
				"accident":{
					"DateOfAccident" : "2014-01-01",
					"AccidentLocation" : "",
					"CarsInvolvedInAccident" : "2",
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
    		var accInfo,perInfo;
    		
    		if(interaction.transfer.PersonIn){
    			
    			perInfo=interaction.transfer.PersonIn;
    			
	    		scope.model.person.FirstName = perInfo.FirstName.__text || "";
	    		scope.model.person.LastName =  perInfo.LastName.__text || "";
	    		scope.model.person.PolicyNumber = perInfo.PolicyNumber.__text || "";
    		}
    		
    		if(interaction.transfer.AccidentInformationIn){
    			
    			accInfo = interaction.transfer.AccidentInformationIn;
    			scope.model.accident.AccidentLocation = accInfo.AccidentLocation.__text || "";
	    		scope.model.accident.DateOfAccident = accInfo.DateOfAccident.__text || "2014-01-01";
	    		scope.model.accident.CarsInvolvedInAccident = parseInt(accInfo.CarsInvolvedInAccident.__text || "1");
	    		scope.model.accident.YourVehicleTowed = accInfo.YourVehicleTowed.__text == "true";
	    		scope.model.accident.WhereVehicleDamaged.Front = accInfo.WhereVehicleDamaged.Front == "true";
	    		scope.model.accident.WhereVehicleDamaged.Rear = accInfo.WhereVehicleDamaged.Rear == "true";
	    		scope.model.accident.WhereVehicleDamaged.PassengerSide = accInfo.WhereVehicleDamaged.PassengerSide == "true";
	    		scope.model.accident.WhereVehicleDamaged.DriverSide = accInfo.WhereVehicleDamaged.DriverSide == "true";
	    		scope.model.accident.WhereVehicleDamaged.Hood = accInfo.WhereVehicleDamaged.Hood == "true";
	    		scope.model.accident.WhereVehicleDamaged.Undercarriage = accInfo.WhereVehicleDamaged.Undercarriage == "true";
    		}
    		
    		scope.$apply();
    		
    		$( "#mashupMain input[type='checkbox']").each(function(){
    			try{
    				$(this).checkboxradio("refresh");
    			}catch(ex){
    				console.log("refresh not available...");
    			}
    		});
 
    		
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