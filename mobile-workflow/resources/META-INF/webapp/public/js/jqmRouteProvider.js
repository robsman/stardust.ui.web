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

define(["jquery-mobile", "angularjs","js/WorkflowService"],function(jqm,angular,WorkflowService){

	var workflowService = WorkflowService.instance();
	
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
	            { "#detailPage": {events: "bC", handler: "detailPage" }},
	            { "#processPage": {events: "bC", handler: "processPage"}},
	            { "#documentViewerPage": {events: "bC", handler: "documentViewerPage"}},
	            { "#repositoryRootPage": {events: "bC", handler: "repositoryRootPage"}}
	        ],
	        {
				/*Reference controllers/baseControllers.js for login authentication*/
				"login" : function(eventType, matchObj, ui, page, e){
					var scope, /*Angular scope for our login JQM page*/
						data,  /*Parameter data passed to our login page*/
						rootScope;
					
					e.preventDefault();
					console.log("JQM Router: /#login");
					data=router.getParams(e.target.baseURI);
					
					try{
						scope=angular.element($("#login").scope())[0];
		
						if(data.partition){
							scope.$apply(function(){
								scope.partition=data.partition;
							});
						}
					}catch(ex){
						console.log("Error on login:");
						console.log(scope);
					}
					ui.bCDeferred.resolve();
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
					
					jqmNData = new jqmNavigateData(scope.$id,eventType,ui,page,e,{},"repositoryRootPage");	
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
					console.log("Broadcasting to #processPage : " + scope.$id);
					console.log("##############################################################");
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