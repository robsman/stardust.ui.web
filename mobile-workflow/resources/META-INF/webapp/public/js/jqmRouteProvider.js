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
	
	/**
	 * Look up a worklist item within our $rootScope collection.
	 */
	var worklistItemLookup = function(data,worklistItems){
		
		var baseItem={},
			i=0;
		
		itemsLength=worklistItems.length;
		for(i=0;i<itemsLength;i++){
			item=worklistItems[i];
			if(item.processInstanceOid == data.id){
				baseItem = item;
				break;
			}
		}
		return baseItem;
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
	            { "#mainPage":  { events: "bC", handler: "mainPage" } },
	            { "#worklistListViewPage": { events: "bC", handler: "worklistListViewPage" }},
	            { "#activityPage": { events: "bC", handler: "activityPage" }},
	            { "#notesPage": { events: "bC", handler: "notesPage" }},
	            { "#activityPage": { events: "bC", handler: "activityPage" }},
	            { "#startableProcessesPage": {events: "bC", handler: "startableProcessesPage" }},
	            { "#detailPage": {events: "bC", handler: "detailPage" }},
	            { "#formPage": {events: "bC", handler: "formPage" }},
	            { "#activityPanelSubpageNotes": {events: "bC", handler: "activityPanelSubpageNotes"}},
	            { "#processPage": {events: "bC", handler: "processPage"}}
	        ],
	        {
				/*Reference app.js for our AJAX handler for login authorization.
				 *and for updates to our angular rootScope*/
				"login" : function(eventType, matchObj, ui, page, e){
					var scope,
						data;
					
					e.preventDefault();
					console.log("JQM Router: /#login");
					data=router.getParams(e.target.baseURI);
					
					if(!angular.element($("#login").scope)){
						console.log("no scope, race condition?");
					}
					try{
					scope=angular.element($("#login").scope())[0];
	
					if(data.partition){
						scope.$apply(function(){
							scope.partition=data.partition;
						});
					}
					}catch(ex){
						//stubbed
					}
					/*TODO - Any intiialization work should be done here*/
					ui.bCDeferred.resolve();
				},
				
				"processPage" : function(eventType, matchObj, ui, page, e){
					var rootscope, /*rootScope of document*/
						scope,	   
						data,
						baseItem;
				
					e.preventDefault();
					
					console.log("JQM Router: /#processPage");
					
					scope=angular.element($("#processPage")).scope();
					rootscope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					
					/*Lookup base worklist Item on rootscope.*/
					baseItem=worklistItemLookup(data,rootscope.appData.worklistItems);
					

					/*Collect data from multiple Ajax sources, when all data is ready init controllers and
					 *allow the UI to transition to the page.*/
					$.when(workflowService.getNotes(data.id),
						   workflowService.getDocuments(data.id),
						   workflowService.getParticipants(data.id))
						   .done(function(notes,docs,participants){
								scope.$apply(function(){
		                			scope.notesModel.notes=notes;
		                			scope.documentModel.docs=docs;
		                			scope.participantModel.participants=participants;
		                			scope.activityModel.item=baseItem;
		                		});
		                		ui.bCDeferred.resolve();
					});
					
					ui.bCDeferred.resolve();

				},
				
				"mainPage" : function(eventType, matchObj, ui, page, e){
					var scope, /*angular Scope*/
						rootScope; /*angular rootScope*/
					e.preventDefault();
					console.log("JQM Router: /#mainPage");
					
					workflowService.getWorklist()
						.done(function(data){
							e.preventDefault(); /*Prevent page transitions until we authorize the user*/
							scope=angular.element($("#mainPage")).scope();
							rootScope=angular.element(document).scope();
							scope.$apply(function(){
								rootScope.appData.worklistItems=data;
								ui.bCDeferred.resolve();
							});
						}).fail(function(){
							/*TODO:-ZZM handle fails*/
							console.log("getWorklist failed");
							ui.bCDeferred.resolve();
						});
					
				},
				
				"worklistListViewPage" : function(eventType, matchObj, ui, page, e){
					var scopeParent, /*Scope we need to access data from*/
						scope,		 /*our local scope for this JQM dom page*/
						rootScope;   /*angular rootScope*/
					
					e.preventDefault();
					console.log("JQM Router: /#worklistListViewPage");
					

					scope=angular.element($("#worklistListViewPage")).scope();
					rootScope=angular.element($(document)).scope();
					scope.$apply(function(){
						//scope.worklistItems=scopeParent.worklistItems;				
					});
					$("#worklistListViewPage ul").listview("refresh");
					ui.bCDeferred.resolve();
				},
				
				"detailPage" : function(eventType, matchObj, ui, page, e){
					var rootscope,
						scope,
						data,
						baseItem,
						itemsLength,i,item;
					
					e.preventDefault();
					
					console.log("JQM Router: /#detailPage");
					
					scope=angular.element($("#detailPage")).scope();
					rootscope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					
					/*Lookup base worklist Item on rootscope.*/
					baseItem=worklistItemLookup(data,rootscope.appData.worklistItems);
					scope.item=baseItem;
					scope.activityModel.item=baseItem;
					
					console.log(scope);
					/*Collect data from multiple Ajax sources, when all data is ready init controllers and
					 *allow the UI to transition to the page.*/
					$.when(workflowService.getNotes(data.id),
						   workflowService.getDocuments(data.id))
						   .done(function(notes,docs){
								//notescope=angular.element($("#notesTabContent")).scope();
								scope.$apply(function(){
		                			console.log(docs);
		                			scope.notesModel.notes=notes;
		                			scope.activityModel.item=baseItem;
		                			scope.documentModel.docs=docs;
		                		});
		                		ui.bCDeferred.resolve();
					});
					
					scope.$apply(function(){
						scope.item=baseItem;
					});
					ui.bCDeferred.resolve();
				},
				
				"notesPage" : function(eventType, matchObj, ui, page, e){
					var scopeParent, /*Scope we need to access data from*/
						data,		 /*data object extracted from hash params*/
						scope;		 /*our local scope for this JQM DOM page*/
					
					e.preventDefault();
					console.log("JQM Router: /#notesPage");
					
					/*calling router function to parse our hash url params*/
	                data=router.getParams(matchObj.input);
	                workflowService.getNotes(data.id)	                
	                	.done(function(notes){
	                		scope=angular.element($("#notesPage")).scope();
	                		scope.$apply(function(){
	                			scope.processoid=data.id;
	                			scope.notes=notes;
	                		});
	                		$("#notesPage ul").listview("refresh");
	                		ui.bCDeferred.resolve();
	                	})
	                	.fail(function(){
	                		console.log("getNotes failed...");
	                		ui.bCDeferred.resolve();
	                	});
				},
				
				"activityPage" : function(eventType, matchObj, ui, page, e){
					var scopeParent, /*Scope we need to access data from*/
						data,		 /*data object extracted from hash params*/
						scope;
					
					e.preventDefault();
					console.log("JQM Router: /#activityPage");
					
					data=router.getParams(matchObj.input);
					scope=angular.element($("#activityPage")).scope();
					
					workflowService.activateActivity()
						.done(function(){
							
						})
						.fail(function(){
							
						});
					
					scope.$apply(function(){
						scope.activityID=data.id;
					});
					ui.bCDeferred.resolve();
					
				},
				
				"activityPanelSubpageNotes": function(eventType, matchObj, ui, page, e){
					var data,		 	 /*data object extracted from hash params*/
						notesScope,		 /*our local scope for the notes DOM*/
						navbarScope, 	 /*our local scope for the navbar DOM*/
						rootscope,		 /*rootScope of our angular app*/
						baseItem;		 /*base worklistItem active for this scope*/
				
					e.preventDefault();
					console.log("JQM Router: /#activityPanelSubpageNotes");
					
					/*calling router function to parse our hash url params*/
	                data=router.getParams(matchObj.input);
	                rootscope=angular.element($(document)).scope();
	                baseItem=worklistItemLookup(data,rootscope.appData.worklistItems);
	                workflowService.getNotes(data.id)	                
	                	.done(function(notes){
	                		
	                		notesScope=angular.element($("#activityPanelSubpageNotes [ ng-controller='notesListCtrl']")).scope();
	                		notesScope.$apply(function(){
	                			notesScope.processoid=data.id;
	                			notesScope.notes=notes;
	                		});
	                		
	                		navbarScope=angular.element($("#activityPanelSubpageNotes [ ng-controller='activityNavbarCtrl']")).scope();
	                		navbarScope.$apply(function(){
	                			navbarScope.activeSubPage="notes";
	                			navbarScope.item=baseItem;
	                		});
	                		ui.bCDeferred.resolve();
	                	})
	                	.fail(function(){
	                		console.log("getNotes failed...");
	                		ui.bCDeferred.resolve();
	                	});
					
				},
				
				"formPage" : function(eventType, matchObj, ui, page, e){
					var data,
						rootScope,
						scope,
						navbarScope;
					
					e.preventDefault(); /*Prevent default navigation*/
					console.log("JQM Router: /#formPage");
					
					scope=angular.element($("#formPage")).scope();
					rootscope = angular.element($(document)).scope();
					data=router.getParams(matchObj.input);
					baseItem=worklistItemLookup(data,rootscope.appData.worklistItems);
	
            		navbarScope=angular.element($("#formPage [ ng-controller='formCtrl']")).scope();
            		navbarScope.$apply(function(){
            			navbarScope.activeSubPage="form";
            			navbarScope.item=baseItem;
            		});
					
					scope.$apply(function(){
						scope.item=baseItem;
					});
					ui.bCDeferred.resolve();
				},
				
				"startableProcessesPage" : function(eventType, matchObj, ui, page, e){
					var data,  /*processDefinitions from workflowService*/
						scope; /*local angularScope*/
					
					e.preventDefault(); /*Prevent default navigation*/
					console.log("JQM Router: /#startableProcessesPage");
					
					/* Query REST endpoint for data, delaying JQM page transition*/
					workflowService.getStartableProcesses()
						.done(function(data){
							
							/*extract local angular scope*/
							scope=angular.element($("#startableProcessesPage")).scope();
							
							/*Apply changes to our local scope*/
							scope.$apply(function(){
								scope.processes=data;
							});	
							
							/*Let JQM update any dynamically inserted DOM pieces*/
							$("#startableProcessesPage").enhanceWithin();
							
							/*Signal that we can now continue our page transition*/
							ui.bCDeferred.resolve();
						})
						.fail(function(err){
							/*TODO:ZZM-Implement error handlingtied to the UI*/
							ui.bCDeferred.resolve();
						});
				}
				
	        },{ajaxApp:true}
	);
	
	return router;
});