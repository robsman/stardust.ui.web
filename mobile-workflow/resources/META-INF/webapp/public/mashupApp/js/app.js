define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		app=angular.module('mashupApp',[]),/*create angular application*/
		rootScope;  /*Angular rootScope within a JQuery context*/
	
	app.controller("main",function($scope){
		$scope.model={
				title: "Test Mashup Application"
		};
	});
	
	var jsApp = {};
	
	/* 1. bootstrap our document against our angular application*/
	jsApp.init=function(){
		/*bootstrapping - after this point we have an angular application tied to our html*/
		angular.bootstrap(document,['mashupApp']);
		return app;
	};
	return jsApp;
});