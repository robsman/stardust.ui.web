/**
 * Router module for our JQuery mobile application:
 * The router module will listen to the JQUERY mobile navigate events
 * and intercept them providing us with a mechanism to halt page transition until
 * we have the target pages associated angular controller initialized as well
 * as having updates to the UI completed. Communication with our REST end points
 * is via 'WorkflowService.js'.
 * 
 * Angular Controllers:
 * ---------------------------------------------------------------------------
 * As our routing mechanism is external to Angular (routing is solely in the JQuery.mobile world)
 * we must follow the pattern of extracting our angular scope into the JQuery world and operating
 * on the scopes data within the confines of $scope.apply. This can generate dynamic HTML in
 * our JQM DOM pages which will not be refreshed by JQUery.mobile unless we explicitly tell
 * JQuery.mobile that we have a stale page. We do this by calling either enhanceWithin or refresh
 * on the JQM framework.
 */

define(["jquery-mobile", "angularjs"],function(jqm,angular){
	
	/*JQM navigation event object we pass from JQM to Angular*/
	var jqmNavigateData=function(scopeTarget,evtType,ui,page,baseEvent,data,pageTarget){
		this.scopeTarget=scopeTarget; /*Scope the event will apply to, used for filtering events*/
		this.navigationType=evtType;  /*JQMrouter event Maps*/
		this.ui = ui;				  /*JQMrouter ui object, transports our JQM promise into the Angular realm */
		this.page=page;				  /*JQM Dom page that is the target of the navigation event*/
		this.baseEvent=baseEvent;	  /*Original JQM event*/
		this.data=data;				  /*Any data we need to pass from JQM to Angular*/
		this.pageTarget=pageTarget;   /*JQM page we are navigating to*/
	};
	
	/*TODO: ZZM !!!!! REFACTOR events as appropriate to avoid double loads and optimize for initialization,
	 * remember only bC will give us a valid bCDeferred instance. Need to check for this to avoid calling deferred
	 * on undefined objects etc. Possibly consider multiple methods based on event they are handling.*/
	/**
	 * EVENT MAPS
	 * -------------------------------------
	 *  bc  => pagebeforecreate
	    c   => pagecreate
	    i   => pageinit
	    bs  => pagebeforeshow
	    s   => pageshow
	    bh  => pagebeforehide
	    h   => pagehide
	    rm  => pageremove
	    bC  => pagebeforechange
	    bl  => pagebeforeload
	    l   => pageload
	 */
	var router=new $.mobile.Router([
	            { "#login": { events: "bC", handler: "login" } },
	            { "#mainPage":  { events: "bC", handler: "mainPage" , step: "url" } },
	            { "#worklistListViewPage": { events: "bC", handler: "worklistListViewPage" }},
	            { "#startableProcessesPage": {events: "bC", handler: "startableProcessesPage" }},
	            { "#detailPage":  {events: "bC", handler: "detailPage" }},
	            { "#processPage": {events: "bC", handler: "processPage"}},
	            { "#documentViewerPage" :  {events: "bC", handler: "documentViewerPage"}},
	            { "#reportViewerPage"   :  {events: "bC", handler: "reportViewerPage"}},
	            { "#repositoryRootPage" :  {events: "bC", handler: "repositoryRootPage"}},
	            { "#searchLandingPage"  :  {events: "bC", handler: "searchLandingPage"}},
	            { "#documentSearchPage" :  {events: "bC", handler: "documentSearchPage"}},
	            { "#activitySearchPage" :  {events: "bC", handler: "activitySearchPage"}},
	            { "#processSearchPage"  :  {events: "bC", handler: "processSearchPage"}},
	            { "#reportRootPage"     :  {events: "bC", handler: "reportRootPage"}},
	            { "#profilePage"        :  {events: "bC", handler: "profilePage"}},
	            { "#settingsPage"       :  {events: "bC", handler: "settingsPage"}}
	        ],
	        {
				/*Reference controllers/baseControllers.js for login authentication*/
				"login" : function(eventType, matchObj, ui, page, e){
					e.preventDefault();
					console.log("JQM Router: /#login");
					ui.bCDeferred.resolve();
				},
				
				"reportRootPage" : function(eventType, matchObj, ui, page, e){
					var rootScope, /*rootScope of document*/
						scope,	   /*local scope of the JQM processPage*/
						data;      /*Parameter data attached to our hash URL*/
			
					e.preventDefault();
					console.log("JQM Router: /#reportRootPage");
					
					scope=angular.element($("#reportRootPage")).scope();
					rootScope = angular.element($(document)).scope();
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"reportRootPage");	
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"reportViewerPage" : function(eventType, matchObj, ui, page, e){
					var rootScope, /*rootScope of document*/
						scope,	   /*local scope of the JQM processPage*/
						data;      /*Parameter data attached to our hash URL*/
			
					e.preventDefault();
					console.log("JQM Router: /#reportViewerPage");
					
					scope=angular.element($("#reportViewerPage")).scope();
					rootScope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,data,"reportViewerPage");	
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"documentViewerPage" : function(eventType, matchObj, ui, page, e){
					var rootScope, /*rootScope of document*/
						scope,	   /*local scope of the JQM processPage*/
						data;      /*Parameter data attached to our hash URL*/
			
					e.preventDefault();
					console.log("JQM Router: /#documentViewerPage");
					
					scope=angular.element($("#documentViewerPage")).scope();
					rootScope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,data,"documentViewerPage");	
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"repositoryRootPage" : function(eventType, matchObj, ui, page, e){
					var rootScope, /*rootScope of document*/
						scope,	   /*local scope of the JQM processPage*/
						data;      /*Parameter data attached to our hash URL*/
			
					e.preventDefault();
					console.log("JQM Router: /#repositoryRootPage");
					data=router.getParams(matchObj.input);
					scope=angular.element($("#repositoryRootPage")).scope();
					rootScope = angular.element($(document)).scope();
					$.mobile.loading( 'show');
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"repositoryRootPage");	
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"searchLandingPage" : function(eventType, matchObj, ui, page, e){
					var jqmNData,
						rootScope,
						scope;
					
					e.preventDefault();
					console.log("JQM Router: /#searchLandingPage");
					rootScope = angular.element($(document)).scope();
					scope=angular.element($("#searchLandingPage")).scope();
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"searchLandingPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
					ui.bCDeferred.resolve();
				},
				
				"documentSearchPage" : function(eventType, matchObj, ui, page, e){
					var jqmNData,
						rootScope,
						scope;
				
					e.preventDefault();
					console.log("JQM Router: /#documentSearchPage");
					
					rootScope = angular.element($(document)).scope();
					scope=angular.element($("#documentSearchPage")).scope();
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"documentSearchPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"activitySearchPage" : function(eventType, matchObj, ui, page, e){
					var jqmNData,
						rootScope,
						scope;
					
					e.preventDefault();
					console.log("JQM Router: /#activitySearchPage");
					
					rootScope = angular.element($(document)).scope();
					scope=angular.element($("#activitySearchPage")).scope();
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"activitySearchPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				"processSearchPage" : function(eventType, matchObj, ui, page, e){
					var jqmNData,
						rootScope,
						scope;
					
					e.preventDefault();
					console.log("JQM Router: /#processSearchPage");
					
					rootScope = angular.element($(document)).scope();
					scope=angular.element($("#processSearchPage")).scope();
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"processSearchPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},			
				
				/*Navigation event to processPage*/
				"processPage" : function(eventType, matchObj, ui, page, e){
					var rootScope, /*rootScope of document*/
						scope,	   /*local scope of the JQM processPage*/
						data;      /*Parameter data attached to our hash URL*/
				
					e.preventDefault();
					console.log("JQM Router: /#processPage");
					
					scope=angular.element($("#processPage")).scope();
					rootScope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					
					jqmNData = new jqmNavigateData("#processPage",eventType,ui,page,e,data,"processPage");	
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData); /*signal Angular listeners*/
				},
				
				/*Navigation event to mainPage.*/
				"mainPage" : function(eventType, matchObj, ui, page, e){
					var rootScope = angular.element(document).scope(),
						scope = angular.element($("#mainPage")).scope(),
						jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"mainPage");	
					
					e.preventDefault();
					console.log("JQM Router: /#mainPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);	
				},
				
				/*Navigation events to the worklist-listview page will result in a list of 
				 *activity instances being pulled*/
				"worklistListViewPage" : function(eventType, matchObj, ui, page, e){
					var rootScope = angular.element(document).scope(),
						scope = angular.element($("#worklistListViewPage")).scope(),
						jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"worklistListViewPage");	
					
					e.preventDefault();
					console.log("JQM Router: /#worklistListViewPage");
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);
				},
				
				"detailPage" : function(eventType, matchObj, ui, page, e){
					var rootScope,
						scope,
						data,
						jqmNData;
					
					e.preventDefault();
					console.log("JQM Router: /#detailPage");
					rootScope= angular.element(document).scope();
					scope=angular.element($("#detailPage")).scope();
					data=router.getParams(matchObj.input);
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,data,"detailPage");	
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);
				},
				
				"profilePage" : function(eventType, matchObj, ui, page, e){
					var rootScope,
						scope,
						jqmNData;
				
					e.preventDefault();
					console.log("JQM Router: /#profilePage");
					rootScope= angular.element(document).scope();
					scope=angular.element($("#profilePage")).scope();
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"profilePage");	
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);
				},
				
				"settingsPage" : function(eventType, matchObj, ui, page, e){
					var rootScope,
						scope,
						jqmNData;
				
					e.preventDefault();
					console.log("JQM Router: /#settingsPage");
					rootScope= angular.element(document).scope();
					scope=angular.element($("#settingsPage")).scope();
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"settingsPage");	
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);
				},
				
				"startableProcessesPage" : function(eventType, matchObj, ui, page, e){
					var rootScope,
						scope,
						data,
						jqmNData;
					
					e.preventDefault(); /*Prevent default navigation*/
					console.log("JQM Router: /#startableProcessesPage");
					rootScope= angular.element(document).scope();
					scope=angular.element($("#startableProcessesPage")).scope();
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"startableProcessesPage");	
					$.mobile.loading( 'show');
					rootScope.signalJQMNavigation(jqmNData);
				}
				
	        },{ajaxApp:true}
	);
	
	return router;
});