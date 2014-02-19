define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		workflowService=require("js/WorkflowService").instance(),
		baseControllers = require('js/controllers/baseControllers'),
		worklistControllers = require('js/controllers/worklistControllers'),
		baseFilters = require("js/filters/baseFilters"),
		jqmDirectives = require("js/directives/jqmDirectives"),
		app=angular.module('phoneApp',[]),/*create angular application*/
		rootScope;  /*Angular rootScope within a JQuery context*/

	/*Perform dependency injection for our application before we bootstrap*/
	app.controller( "loginCtrl",  baseControllers.loginCtrl);
	app.controller( "footerCtrl", baseControllers.footerCtrl);
	app.controller( "headerCtrl", baseControllers.headerCtrl);
	app.controller( "worklistCtrl",  worklistControllers.worklistCtrl);
	app.controller( "activityCtrl",  worklistControllers.activityCtrl);
	app.controller( "activityPopupCtrl", worklistControllers.activityPopupControl);
	app.controller( "notesListCtrl", worklistControllers.notesListCtrl);
	app.controller( "detailCtrl",    worklistControllers.detailCtrl);
	app.controller( "panelCtrl",     worklistControllers.panelCtrl);
	app.controller( "startableProcessesCtrl", worklistControllers.startableProcessesCtrl);
	app.controller( "formCtrl", worklistControllers.formCtrl);
	app.controller( "activityNavbarCtrl", worklistControllers.activityNavbarCtrl);
	app.controller( "processCtrl", worklistControllers.processCtrl);
	app.filter( "humaneDate",        baseFilters.humaneDate);
	app.filter( "serializeObject",   baseFilters.serializeObject);
	app.directive( "jqmTemplate",    jqmDirectives.jqmTemplate);
	app.directive( "testTemplate",   jqmDirectives.testTemplate);
	
	/* 1. bootstrap our document against our angular application
	 * 2. tie in any handlers we need to for our events */
	app.init=function(options){
		var ui ={};

		/*bootstrapping - after this point we have an angular application tied to our html*/
		angular.bootstrap(document,['phoneApp']);

		/****************************************JQUERY******************************************/
		/* Jquery initialization, specifically, we need to handle click or submit events
		 * that are outside the purview of JQMs hash based navigation. Common cases would be a
		 * request to a rest API for data that is NOT concurrent with a page navigation/transition.
		 * The login page is another case as we need to submit login data to the server and get a 
		 * response back before we attempt to navigate. So, in that case we combine a non navigation event
		 * result (login verification) with a control flow based navigation related to that result.*/
		
		/*Create $ objects corresponding to our UI elements we need to interact with directly via jQuery*/
		ui.inptLogin=$(options.selectors.inptLogin);
		ui.loginPage = $(options.selectors.loginPage);
		ui.mainPage = $(options.selectors.mainPage);
		ui.worklistListViewPage = $(options.selectors.worklistListViewPage);
		ui.btnAddNote = $(options.selectors.btnAddNote);
		ui.notesPage = $(options.selectors.notesPage);
		
		/*Initialize our external headers and footers, as external headers and footers exist outside 
		 *of a JQM page they must be explicitly initialized.
		 *TODO:investigate why jqm-directive does not handle this case, ideally we should not
		 *	   have to call these explicitly as the directive should take care of it.*/
		$( "[data-role='header'], [data-role='footer']" ).toolbar();
		$( "body>[data-role='panel']" ).panel();
		
		/*Set up rootscope to handle events from Angular within our JQuery universe*/
		rootScope=angular.element($(document)).scope();
		
		/* Handle navigation requests that need to be performed manually.*/
		$(rootScope).on("navigateRequest",function(e,data){
			console.log("navigation request triggered on rootscope.");
			console.log(data);
			$.mobile.navigate(data.target,data.payload);
		});
		
		
		/* Handle addWorklistNote events generated within Angular and triggered on rootScope.
		 * TODO: move to an internal Angular service, will require 
		 * injecting workflowService as a dependency. IF this is done then to avoid keeping a duplicate
		 * external workflow service for the JQM router provider, we should look at moving its ajax
		 * initialization calls to that service as well. At that point the routing mechanism will be
		 * used only to signal the angular app that a page navigation has occured and the angular app
		 * decides what to do next.*/
		$(rootScope).on("addWorklistNote",function(e,data){
			workflowService.createNote(data.processoid, data.content)
				.done(function(res){
					workflowService.getNotes(data.processoid)
					.done(function(notes){
						rootScope.$broadcast("worklistNoteAdded",{
							"notes" :notes, 
							"processoid" : data.processoid}
						);
					})
					.fail();
				})
				.fail(function(err){
					//TODO: do something to indicate failure
				});
		});
		

		/*Handle login submission attempts*/
		ui.loginPage.on("click","#inptLogin",function(e){
			var scope, 	   /*angular Scope*/
				rootScope; /*angular rootScope*/
			
			e.preventDefault(); /*Prevent page transitions until we authorize the user*/
			scope=angular.element($(options.selectors.loginPage)).scope();
			rootScope = angular.element(document).scope();
			
			/*As we can't nest controllers due to the structure of our JQM pages in the DOM (completely flat)
			 *we will utilize rootScope to hold data common to the entire app, controllers will have rootscope
			 *injected as needed.*/
			rootScope.appData={
					"isAuthorized" : false,
					"user" :{},
					"worklistItems":[]
			};
			
			/*leverage the login function from our workflow service to transition
			 * to the mainPage on success or to show an inline error message on fail.*/
			workflowService.login(scope.username,scope.password,scope.partition)
				.done(function(user){
					rootScope.appData.user=user;
					rootScope.appData.isAuthorized=true;
					$.mobile.navigate(options.selectors.mainPage);
				})
				.fail(function(err){
					var myAlert = $("div:jqmData(role='inlineAlert')",ui.loginPage);
					myAlert.inlineAlert("show","User Authentication Failed...",5000);
			});
			
		});
		
	};
	
	return app;
});