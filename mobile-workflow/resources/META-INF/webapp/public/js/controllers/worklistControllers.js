/*Controllers Related to worklists...*/
define([],function(){
	
	/*TODO: ZZM- Move utils to a service/factory and inject as a dependency*/
	var utils={
			
			/* Indicate a navigation request outside of the JQM
			 * hash handlers.*/
			"navigateTo" : function(root,target,data){
				$(root).trigger("navigateRequest",{
					"target": target,
					"payload" : data
				});
			},
			
			/*Generic event trigger function*/
			"trigger" : function(root,eventName,data){
				$(root).trigger(eventName,data);
			}
	};
	
	var worklistModel =function(){
		var that = this;
		this.test="Hello From Worklist CTRL";
		this.worklistItems=[];
		this.sort=function(items,key,asc){
			items.sort(function(a,b){
				return a[key] > b[key];
			});
			if(asc){
				items.reverse();
			}
		};
	};
	
	var worklistItem =function(){
		this.item={};
	};
	
	var documentModel=function(){
		this.docs=[];
		this.uploadDoc={
				path:""
		};
	};
	
	var notesModel=function(){
		this.notes=[];
		this.newNote={
				"content" : "", 
				"processoid": ""
		};
		this.addNote=function(scope,rootScope,newNote){
			console.log("triggering addWorklistNote event...");
			console.log(newNote);
			$(rootScope).trigger("addWorklistNote",newNote);
		};
	};
	
	
	var participantModel = function(){
		this.participants =[];
	};
	
	var mashupModel=function(){
		this.url="http://localhost:8080/pepper-test/plugins/mobile-workflow/public/mashupapp/mashup.html";
	};
	
	/*Our return object with all controllers defined*/
	var worklistCtrl = {
			
			/**Overall Note on using $rootScope
			 * ------------------------------------------------------------------
			 * In order to more easily provide the same data to our sibling scopes 
			 * (observe that distinct JQM DOM pages in a single page app may not nest)
			 * we leverage rootScope for certain bits of data that are truly global
			 * to our application (@see worklistItems, isAuthorized). We can then simply inject $rootScope
			 * into our controllers and have access to the same data across scopes.
			 * TODO:ZZM-Consider moving this to a cached data service...
			 * Ref: http://stackoverflow.com/questions/16739084/angularjs-using-rootscope-as-a-data-store
			 * Ref: http://docs.angularjs.org/misc/faq "$rootScope exists, but it can be used for evil"
			 * */
			
			/*Control intended to operate under rootScopes appData.worklist Items.
			 *Provides a few helper functions etc ...
			 *TODO:ZZM-(possibly consider a service once app is more fully fleshed out).*/
			"worklistCtrl" : function($scope,$rootScope){
				$scope.worklistModel=new worklistModel();
				$scope.test="Hello From Worklist CTRL";
			},
			
			/*Deprecated: Popup is now intgrated as a navbar directly in the page
			 *TODO: - Remove upon final refactor*/
			"activityPopupControl" : function($scope,$rootScope){
				$scope.test = "Hello From activity Popup Ctrl";
				$scope.worklistItem=undefined;
			},
			
			/*Deprecated for now, meged with detail control*/
			"notesListCtrl" : function($scope,$rootScope){
				$scope.notesModel = new notesModel();
			},
			
			/*startableProcessesControl*/
			"startableProcessesCtrl" : function($scope,$rootScope){
				$scope.test = "Hello From Startable Process(s) Control";
				$scope.processes = [];
			},
			
			/*Activity Panel navbar Control, repeats across 'subpages'*/
			"activityNavbarCtrl" : function($scope,$rootScope){
				$scope.test = "Hello From Activity NavBar Ctrl";
				$scope.activeSubPage="activity";
			},
			
			/*panelControl*/
			"panelCtrl" : function($scope,$rootScope){
				$scope.test = "Hello From Panel Ctrl";
			},
			
			/*TODO:ZZM-stubbed*/
			"activityCtrl" : function($scope,$rootScope){
				$scope.test = "Hello From Activity Ctrl";
				$scope.activityID = -1;
				$scope.activity={};
			},
			
			"processCtrl" : function($scope,$rootScope){
				/*declare our model(s)*/
				$scope.notesModel = new notesModel();
				$scope.activityModel = new worklistItem();
				$scope.documentModel = new documentModel();
				$scope.participantModel = new participantModel();
				
				/*Signal JQuery universe that we need to add a note via REST*/
				$scope.addNote = function(id,newNote){
					newNote.processoid=id;
					$scope.notesModel.addNote($scope,$rootScope,newNote);
				};
			},
			
			"detailCtrl" : function($scope,$rootScope,utilService,workflowService){
				
				/*Initialization*/
				$scope.activeTab='activityTab';
				
				$scope.createNote = function(oid,content){
					workflowService.createNote(oid,content);
				}; 
				
				$scope.getNotes = function(oid){
					var success = function(data){
							console.log("success");
							console.log(data);
							$scope.notesModel.notes=data;
						},
						fail = function(status){
							console.log("failed");
						};
						
					workflowService.getNotes(oid)
						.then(success,fail);
					
				};
				
				$scope.addNote2 = workflowService.addNote;
				$scope.notesModel = new notesModel();
				$scope.activityModel = new worklistItem();
				$scope.formModel = new mashupModel();
				$scope.documentModel = new documentModel();

				/*Signal JQuery universe that we need to add a note via REST*/
				$scope.addNote = function(id,newNote){
					newNote.processoid=id;
					$scope.notesModel.addNote($scope,$rootScope,newNote);
				};
				
				/*Generic trigger handler for anyone who wishes to trigger an event
				 * to any listeners on $rootScope. Designed for communication with 
				 * the JQuery universe
				 * */
				$scope.trigger= function(eventName,data){
					console.log("trigger request");
					console.log(eventName + " , " + data);
					utils.trigger($rootScope,eventName,data);
				};
				
				/*Signal JQM to perform a manual navigation to a target page,
				 *passing in deep copies of our local data.*/
				$scope.navigateTo = function(target){
					utils.navigateTo($rootScope,target,{});
				};
				
				/**
				 * SECTION : EVENT LISTNENERS 
				 * ----------------------------------------------------------------------
				 * add handlers to listen to events emitted from rootScope. Events can be from
				 * both within and outside the angular universe. In the case of 'worklistAdded'
				 * we are listening for an event triggered externally in the JQUERY universe to
				 * indicate an AJAX post was succesful.
				 */
				$scope.$on("worklistNoteAdded",function(e,data){
					console.log("worklistNoteAdded event received by Controller...");
					if(data.processoid==$scope.item.processInstanceOid){
						console.log("adding data within scope...");
						/*TODO: this only reflects in our data boundUI if we wrap with $apply which seems
						 * extremely odd in that we are making our change within Angular. Investigate...*/
						$scope.$apply(function(){
							$scope.notesModel.notes=data.notes;
						});
					}
				});
				
			},
			
			"formCtrl" : function($scope,$rootScope){
				$scope.test= "Hello From Form Ctrl";
				$scope.formModel = new mashupModel();
			}
			
			
	};
	return worklistCtrl;
});