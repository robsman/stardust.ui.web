define(function(require){
	'use strict';
	
	/*reference our angular dependency, reference our controllers etc, 
	 *and create an Angular module representing our application.*/
	var angular=require('angularjs'),
		angWorkflow = require("js/services/workflowService"),
		baseControllers = require('js/controllers/baseControllers'),
		worklistControllers = require('js/controllers/worklistControllers'),
		baseFilters = require("js/filters/baseFilters"),
		fileDirectives = require("js/directives/fileHandlingDirectives"),
		jqmDirectives = require("js/directives/jqmDirectives"),
		docViewDirectives = require("js/directives/documentViewers"),
		baseDirectives = require("js/directives/baseDirectives"),
		utilService = require("js/services/utils"),
		il18nService = require("js/services/il18nService"), /*Internationalization*/
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
	app.controller( "documentViewerCtrl" , worklistControllers.documentViewerCtrl);
	app.controller( "repositoryRootCtrl" , worklistControllers.repositoryRootCtrl);
	app.controller( "documentSearchCtrl" , worklistControllers.documentSearchCtrl);
	app.controller( "activitySearchCtrl" , worklistControllers.activitySearchCtrl);
	app.controller( "processSearchCtrl"  , worklistControllers.processSearchCtrl);
	app.controller( "reportRootCtrl"     , worklistControllers.reportRootCtrl);
	app.controller( "reportViewerCtrl"   , worklistControllers.reportViewerCtrl);
	app.controller("unauthorizedCtrl"    , worklistControllers.unauthorizedCtrl);
	app.controller("profileCtrl"         , worklistControllers.profileCtrl);
	app.controller("settingsCtrl"        , worklistControllers.settingsCtrl);
	app.filter( "friendlyDate",        baseFilters.friendlyDate);
	app.filter( "serializeObject",   baseFilters.serializeObject);
	app.filter( "criticality",       baseFilters.criticality);
	app.filter( "absoluteTime",      baseFilters.absoluteTime);
	app.filter( "priority",          baseFilters.priority);
	app.directive( "jqmTemplate",    jqmDirectives.jqmTemplate);
	app.directive("jqmPopup",        jqmDirectives.jqmPopup);
	app.directive("jqmLoader",       jqmDirectives.jqmLoader);
	app.directive( "testTemplate",   jqmDirectives.testTemplate);
	app.directive( "fileUpload",   	 fileDirectives.fileUpload);
	app.directive( "setFocus",   	 baseDirectives.setFocus);
	app.directive( "speechToText",   baseDirectives.speechToText);
	app.directive( "imageViewer",    docViewDirectives.imageViewer);
	app.factory("utilService",utilService);
	app.factory("workflowService",angWorkflow);
	app.factory("il18nService",il18nService);
	
	/*Initial setup for our application*/
	app.run(function($rootScope,workflowService,il18nService){
		
		/*Initialize app level data which all scopes may require*/
		$rootScope.appData={
				"barDuration" : 5000,
				"isAuthorized" : false,
				"user" :{},
				"isActivityHot" : false,
				"hotActivityInstance" : {},
				"activePage" : "login",
				"infoText" : {
					"upload" : il18nService.getProperty("mobile.info.upload"),
					"genericSave" : il18nService.getProperty("mobile.info.general.save")
				},
				"errorText"  : {
					"pageload"        : il18nService.getProperty("mobile.error.pageload"),
					"recordretrieval" : il18nService.getProperty("mobile.error.recordretrieval"),
					"startprocess"    : il18nService.getProperty("mobile.error.startprocess"),
					"upload"          : il18nService.getProperty("mobile.error.upload"),
					"notesave"        : il18nService.getProperty("mobile.error.note.save"),
					"activation"      : il18nService.getProperty("mobile.error.activation"),
					"refresh"         : il18nService.getProperty("mobile.error.document.refresh"),
					"folder"          : il18nService.getProperty("mobile.error.folder"),
					"priority"        : il18nService.getProperty("mobile.error.process.priority"),
					"genericSave"     : il18nService.getProperty("mobile.error.general.save"),
					"delegation" 	  : il18nService.getProperty("mobile.error.delegation"),
				}
		};
		
		/*Any time we detect a navigate event in our JQM router provider, we will need
		 *to signal the Angular universe that the event has occurred so that the
		 *appropriate controller can initialize itself.*/
		$rootScope.signalJQMNavigation = function(data){
			$rootScope.$broadcast("jqm-navigate",data);
		};
		
		/*Handler for activityStatus changes from our embedded mashup Apps.*/
		$rootScope.$on("activityStatusChange",function(e,data){
			/*filter out events that don't match our hotInstance*/
			console.log("activityStatusChange event on rootScope...");
			switch(data.newStatus){
				case "complete"       :
				case "suspend"        :
				case "suspendAndSave" :
					console.log("Resetting rootscope activity Instance state.");
					$rootScope.appData.hotActivityInstance={};
					$rootScope.appData.isActivityHot="false";
					break;	
			}
		});
		
	});
	
	/* 1. bootstrap our document against our angular application
	 * 2. tie in any handlers we need to for our events */
	app.init=function(options){
		var ui ={};
		
		/*bootstrapping - after this point we have an angular application tied to our html
		  Initialize our internationalization resource service first so that it is has its
		  data before any attempts are made to access a value from it (and hundreds of attempts
		  will occur during the bootstrap process).*/
		il18nService().init()
			.finally(function(){
				
				angular.bootstrap(document,['phoneApp']);
			
				/****************************************JQUERY******************************************/
				/* Jquery initialization*/
				$.mobile.ignoreContentEnabled=true;
				
				/*Initialize our external headers and footers, as external headers and footers exist outside 
				 *of a JQM page they must be explicitly initialized.
				 *TODO:investigate why jqm-directive does not handle this case, ideally we should not
				 *	   have to call these explicitly as the directive should take care of it.*/
				$( "[data-role='header'], [data-role='footer']" ).toolbar();
				$( "body>[data-role='panel']" ).panel();
				
				//TODO:ZZM- TEMP CODE FOR CRNT-32870
				/*
				$( window ).on( "orientationchange", function( event ) {
					$("#mainPage").trigger("create");
					var winWidth =$(window).width();
                    $(".ui-header").width(winWidth);
                    $(".ui-listview").width(winWidth);
                    $(".ui-footer").width(winWidth);
                    $(".ui-page").width(winWidth);
				});
				
				//TODO:ZZM- TEMP CODE FOR CRNT-32870
				$(window).resize(function() { 
					var winWidth =$(window).width();
                    $(".ui-header").width(winWidth);
                    $(".ui-listview").width(winWidth);
                    $(".ui-footer").width(winWidth);
                    $(".ui-page").width(winWidth);
                });*/
				
				
				/*Acquire reference in Jquery scope to our Angular rootScope*/
				rootScope=angular.element($(document)).scope();
				
				/* Handle navigation requests triggered within Angular that need to be performed manually.
				 * Triggering this event with no data.target defined will result in mobile performing a back navigation
				 * relative to the navigation history.
				 * The goal is to keep from explicitly referencing JQM as a dependency internal to Angular (
				 * Other than our JQM template directives), our Angular domain should not be aware that it is
				 * co-habitating with JQuery Mobile, well it's a goal...
				 * */
				$(rootScope).on("navigateRequest",function(e,data){
					if(data && data.target){
						$.mobile.navigate(data.target,data.payload);
					}
					else{
						$.mobile.back();
					}
				});
				
				
				/*TODO-ZZM: Get this working so we can detect iscroll events*/
				$(document).delegate("#mainPage", "pageinit", function(event) {
			        $(".iscroll-wrapper", this).bind( {
				        "iscroll_onpulldown" : function(){console.log("pulldown");},
				        "iscroll_onpullup"   : function(){console.log("pullup");}
			        });
			      });
		
		});//finally end
	};
	
	return app;
});