define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		workflowService=require("js/WorkflowService").instance(),
		angWorkflow = require("js/services/workflowService"),
		baseControllers = require('js/controllers/baseControllers'),
		worklistControllers = require('js/controllers/worklistControllers'),
		baseFilters = require("js/filters/baseFilters"),
		fileDirectives = require("js/directives/fileHandlingDirectives"),
		jqmDirectives = require("js/directives/jqmDirectives"),
		docViewDirectives = require("js/directives/documentViewers"),
		utilService = require("js/services/utils"),
		app=angular.module('phoneApp',[]),/*create angular application*/
		rootScope;  /*Angular rootScope within a JQuery context*/

	/*Perform dependency injection for our application before we bootstrap*/
	app.controller( "loginCtrl",  baseControllers.loginCtrl);
	app.controller( "footerCtrl", baseControllers.footerCtrl);
	app.controller( "headerCtrl", baseControllers.headerCtrl);
	app.controller( "worklistCtrl",  worklistControllers.worklistCtrl);
	app.controller( "detailCtrl",    worklistControllers.detailCtrl);
	app.controller( "panelCtrl",     worklistControllers.panelCtrl);
	app.controller( "startableProcessesCtrl", worklistControllers.startableProcessesCtrl);
	app.controller( "formCtrl",      worklistControllers.formCtrl);
	app.controller( "processCtrl",   worklistControllers.processCtrl);
	app.controller( "mainPageCtrl",  worklistControllers.mainPageCtrl);
	app.controller( "documentViewerCtrl" ,worklistControllers.documentViewerCtrl);
	app.controller( "repositoryRootCtrl" ,worklistControllers.repositoryRootCtrl);
	app.filter( "humaneDate",        baseFilters.humaneDate);
	app.filter( "serializeObject",   baseFilters.serializeObject);
	app.filter( "criticality",       baseFilters.criticality);
	app.filter( "absoluteTime",      baseFilters.absoluteTime);
	app.filter( "priority",          baseFilters.priority);
	app.directive( "jqmTemplate",    jqmDirectives.jqmTemplate);
	app.directive("jqmPopup",        jqmDirectives.jqmPopup);
	app.directive("jqmLoader",       jqmDirectives.jqmLoader);
	app.directive( "testTemplate",   jqmDirectives.testTemplate);
	app.directive( "fileUpload",   	 fileDirectives.fileUpload);
	app.directive( "imageViewer",    docViewDirectives.imageViewer);
	//app.directive( "jqSmartZoom",    	 docViewDirectives.jqSmartZoom);
	app.factory("utilService",utilService);
	app.factory("workflowService",angWorkflow);
	
	/*Initial setup TODO:-*/
	app.run(function($rootScope,workflowService){
		
		$rootScope.appData={
				"isAuthorized" : false,
				"user" :{},
				"worklistItems":[],
				"isActivityHot" : false,
				"hotActivityInstance" : {},
				"activePage" : "login"
		};
		
		$rootScope.signalJQMNavigation = function(data){
			$rootScope.$broadcast("jqm-navigate",data);
		};
		
		/*TODO: we need the completed OID sent to us*/
		$rootScope.$on("activityStatusChange",function(e,data){
			/*filter out events that don't match our hotInstance*/
			console.log("activityStatusChange event on rootScope...");
			if(data.newStatus=="complete"){
				console.log("Resetting rootscope activity Instance state.");
				$rootScope.appData.hotActivityInstance={};
				$rootScope.appData.isActivityHot="false";
			}
		});
		
	});
	
	/* 1. bootstrap our document against our angular application
	 * 2. tie in any handlers we need to for our events */
	app.init=function(options){
		var ui ={};

		/*bootstrapping - after this point we have an angular application tied to our html*/
		angular.bootstrap(document,['phoneApp']);

		/****************************************JQUERY******************************************/
		/* Jquery initialization*/
		
		/*Initialize our external headers and footers, as external headers and footers exist outside 
		 *of a JQM page they must be explicitly initialized.
		 *TODO:investigate why jqm-directive does not handle this case, ideally we should not
		 *	   have to call these explicitly as the directive should take care of it.*/
		$( "[data-role='header'], [data-role='footer']" ).toolbar();
		$( "body>[data-role='panel']" ).panel();
		
		/*Set up rootScope to handle events from Angular within our JQuery universe*/
		rootScope=angular.element($(document)).scope();
		
		/* Handle navigation requests triggered within Angular that need to be performed manually.
		 * The goal is to keep from explicitly referencing JQM as a dependency internal to Angular.
		 * Other than our JQM template directives, our Angular domain should not be aware that it is
		 * co-habitating with JQuery Mobile, well it's a goal...
		 * */
		$(rootScope).on("navigateRequest",function(e,data){
			/*Particulars will be handled within the JQMRouteProveder,*/
			$.mobile.navigate(data.target,data.payload);
		});
		
		$(document).delegate("#mainPage", "pageinit", function(event) {
	        $(".iscroll-wrapper", this).bind( {
		        "iscroll_onpulldown" : function(){console.log("pulldown");},
		        "iscroll_onpullup"   : function(){console.log("pullup");}
	        });
	      });
		$.mobile.loading( "show", {
			  text: "foo",
			  textVisible: true,
			  theme: "z",
			  html: ""
			});
		
	};
	
	return app;
});