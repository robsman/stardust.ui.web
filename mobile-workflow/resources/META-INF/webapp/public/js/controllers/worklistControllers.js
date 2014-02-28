/*Controllers Related to worklists...*/
define([],function(){
	
	
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
	},
	
	 processModel = function(){
		this.descriptors={};
		this.documents=[];
		this.events={};
		this.notes=[];
		this.participants={};
		this.priority =0;
		this.processId ="";
		this.processName="";
		this.startTimestamp=0;
		this.state="";
	},
	
	startableProcessModel=function(){
		this.processes =[];
	},
	
	worklistItem =function(){
		this.item={};
	},
	
	documentModel=function(){
		this.docs=[];
	},
	
	documentViewerModel = function(){
		this.document={};
	},
	
	mainPageModel = function(){
		this.worklistCount=0;
	},
	
	notesModel=function(){
		this.notes=[];
		this.newNote={
				"content" : "", 
				"processoid": ""
		};
	},
	
	participantModel = function(){
		this.participants =[];
	},
	
	mashupModel=function(){
		this.externalUrl = "#";
		this.interactionId = "";
	},
	
	/*Our return object with all controllers defined*/
	worklistCtrl = {
			
			"worklistCtrl" : function($scope,$rootScope,workflowService){
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.scopeTarget != $scope.$id){return;}

					success=function(data){
							$scope.$apply(function(){
								//$rootScope.appData.worklistItems=data.worklist;
								$scope.worklistModel.worklistItems = data.worklist;
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Worklist retrieval failed.");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getWorklist().then(success,fail);
					
				});
				
				$scope.worklistModel=new worklistModel();
				$scope.test="Hello From Worklist CTRL";
			},
			
			"mainPageCtrl" : function($scope,$rootScope,workflowService){
				
				$scope.mainPageModel = new mainPageModel();
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.scopeTarget != $scope.$id){return;}

					success=function(data){
							$scope.$apply(function(){
								console.log("Count total=");
								console.log(data);
								$scope.mainPageModel.worklistCount=data.total;
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Activity Instance Count retrieval failed");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getWorklistCount().then(success,fail);
					
				});
			},

			
			/*startableProcessesControl*/
			"startableProcessesCtrl" : function($scope,$rootScope,workflowService){
				$scope.test = "Hello From Startable Process(s) Control";
				$scope.startableProcessModel = new startableProcessModel();
				
				$scope.startProcess = function(processDefinitionId){
					var success,fail;
					
					success=function(data){
						console.log("startProcess Returned...");
						console.log(data);
						/*check data and then do some crazy stuff...*/
					},
					fail =  function(status){
							console.log("Startable Process retrieval failed");
					};
					
					workflowService.startProcess(processDefinitionId)
						.then(success,fail);
					
				};
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.scopeTarget != $scope.$id){return;}

					success=function(data){
							$scope.$apply(function(){
								$scope.startableProcessModel.processes=data.processDefinitions;
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Startable Process retrieval failed");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getStartableProcesses().then(success,fail);
					
				});
			},
			
			/*panelControl*/
			"panelCtrl" : function($scope,$rootScope){
				$scope.test = "Hello From Panel Ctrl";
			},
			
			
			"processCtrl" : function($scope,$rootScope,workflowService){
				/*declare our model(s)*/
				$scope.notesModel = new notesModel();
				$scope.activityModel = new worklistItem();
				$scope.documentModel = new documentModel();
				$scope.participantModel = new participantModel();
				$scope.processModel = new processModel();
				$scope.baseHref = workflowService.baseHref;
				$scope.showMsg = false;
				$scope.alertMessage = "";
				$scope.uploadSuccesful = false;
				$scope.isUploading =false;
				
				/*Helper function to refresh our scoped document collection*/
				$scope.getDocuments = function(processOid){
					var success = function(data){
							$scope.$apply(function(){
								$scope.documentModel.docs = data.documents;
							});
						},
						fail = function(){
							console.log("document refresh failed.");
						};
						
					workflowService.getDocuments(processOid).then(success,fail);
				};
				
				/*Reporter object we will tie to our file-upload directive*/
				$scope.uploadReporter ={
		          onProgress: function(e){
		        	console.log("Progress");
		            console.log(e);
		          },
		          onLoad: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.isUploading=false;
		        		  if(e.currentTarget.status==200){
		        			  $scope.uploadSuccesful=true;
		        			  $scope.alertMessage="Upload Succesful";
		        			  $scope.getDocuments($scope.activityModel.item.processInstanceOid);
		        		  }else{
		        			  $scope.uploadSuccesful=false;
		        			  $scope.alertMessage="Upload Failed " + e.currentTarget.statusText ;
		        		  }
		        		  $scope.showMsg=true;
		        	  });
		          },
		          onError: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.uploadSuccesful=false;
		        		  $scope.alertMessage="Upload Failed "+ e.currentTarget.statusText ;
		        	  });
		          }
		        };
				
				/*Listener for JQuery Mobile navigation events*/
				$scope.$on("jqm-navigate",function(e,edata){
					var success,
						fail;
					
					/*Ignore Navigate events on scopes other than our own*/
					if(edata.scopeTarget != $scope.$id){return;}
				
					success=function(data){
							$scope.$apply(function(){
								$scope.notesModel.notes = data.notes;
								console.log(data.participants);
								/*For coherence with detailPage template structure,
								 *TODO: abstract title and ids as appropriate*/
								$scope.activityModel.item ={
										"processInstanceOid" : edata.data.id,
										"processName" : data.processName
								};
								$scope.documentModel.docs = data.documents;
								$scope.participantModel.participants = data.participants;
								$scope.processModel = data;
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Process Instance retrieval failed.");
							edata.ui.bCDeferred.resolve();
					};		
					workflowService.getProcessInstance(edata.data.id).then(success,fail);
				});
				
				/**/
				$scope.createNote = function(oid,content){
					var success,fail;
					success=function(){
						workflowService.getNotes(oid).then(function(data){
							$scope.$apply(function(){
								$scope.notesModel.notes=data.notes;
							});
						});
					};
					fail = function(){
						console.log("failed");
					};
					workflowService.createNote(oid,content).then(success,fail);
				}; 
			},
			
			"detailCtrl" : function($scope,$rootScope,$filter,$sce,utilService,workflowService){
				
				$scope.notesModel = new notesModel();
				$scope.activityModel = new worklistItem();
				$scope.formModel = new mashupModel();
				$scope.documentModel = new documentModel();
				$scope.mashupModel = new mashupModel();
				$scope.baseHref = workflowService.baseHref;
				$scope.showMsg = false;
				$scope.alertMessage = "";
				$scope.uploadSuccesful = false;
				$scope.isUploading =false;
				$scope.formTabTarget = "#formTab";
				$scope.showMashupIframe=true;
				$scope.showActivateButton =false;
				$scope.hotActivityConflict = false;
				$scope.previousPage="";
				
				$scope.isMashupShowable = function(){
					if(  $rootScope.appData.isActivityHot &&  
							(  $scope.activityModel.item.oid == $rootScope.appData.hotActivityInstance.oid )
						){
						return true;
					}else{
						return false;
					}
				};
				
				$scope.isHotActivityConflict = function(){
					if($rootScope.appData.isActivityHot && 
							(  $scope.activityModel.item.oid != $rootScope.appData.hotActivityInstance.oid )
							){
						return true;
					}else{
						return false;
					}
				};
				
				$scope.activate = function(activityOid){
					workflowService.activate(activityOid).then(
						function(data){
							
							var url=data.contexts.externalWebApp["carnot:engine:ui:externalWebApp:uri"] +
									"?interactionId=" + data.contexts.externalWebApp.interactionId;
							
							//Load new data for iframe
							$scope.$apply(function(){
								$scope.mashupModel.externalUrl= $sce.trustAsResourceUrl(url);
								$scope.mashupModel.interactionId=data.contexts.externalWebApp.interactionId;
							});	
							
							$rootScope.$apply(function(){
								$rootScope.appData.isActivityHot = true;
								$rootScope.appData.hotActivityInstance = {
										"oid" : $scope.activityModel.item.oid,
										"name" : $scope.activityModel.item.activityName
								};
							});
							//Get updates state etc from server...
							workflowService.getActivityInstance(activityOid).then(
									function(data){
										$scope.$apply(function(){
											$scope.activityModel.item = data;
											$scope.isMashupShowable();
											$scope.isHotActivityConflict();
										});
									}
							);
							//AutoNav to form tab
							$("[href='#formTab']").addClass("ui-btn-active");
							$("[href='#formTab']").trigger("click");
						},
						
						function(status){
							console.log("activation failed");
						});
				};
				
				/*Helper function to refresh our scoped document collection*/
				$scope.getDocuments = function(processOid){
					var success = function(data){
							$scope.$apply(function(){
								$scope.documentModel.docs = data.documents;
							});
						},
						fail = function(){
							console.log("document refresh failed.");
						};
						
					workflowService.getDocuments(processOid).then(success,fail);
				};
				
				/*Reporter object we will tie to our file-upload directive*/
				$scope.uploadReporter ={
		          onProgress: function(e){
		        	console.log("Progress");
		            console.log(e);
		          },
		          onLoad: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.isUploading=false;
		        		  if(e.currentTarget.status==200){
		        			  $scope.uploadSuccesful=true;
		        			  $scope.alertMessage="Upload Succesful";
		        			  $scope.getDocuments($scope.activityModel.item.processInstanceOid);
		        		  }else{
		        			  $scope.uploadSuccesful=false;
		        			  $scope.alertMessage="Upload Failed " + e.currentTarget.statusText ;
		        		  }
		        		  $scope.showMsg=true;
		        	  });
		          },
		          onError: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.uploadSuccesful=false;
		        		  $scope.alertMessage="Upload Failed "+ e.currentTarget.statusText ;
		        	  });
		          }
		        };
				
				/*Listener for JQuery Mobile Navigation events*/
				$scope.$on("jqm-navigate",function(e,edata){
					
					var success,fail;
					console.log("jqmn data");
					console.log(edata);
					
					/*filter messages that don't match our scopeID*/
					if(edata.scopeTarget != $scope.$id){return;}
					success=function(data){
							var sortedNotes = $filter("orderBy")(data.processInstance.notes,
													"timestamp",true),
								sortedDocs = $filter("orderBy")(data.processInstance.documents,
													 "lastModifiedTimestamp",true);
							$scope.$apply(function(){
								
								$scope.notesModel.notes = sortedNotes;
								$scope.activityModel.item = data;
								$scope.documentModel.docs = sortedDocs;
								$scope.showMashupIframe=false;
								$scope.hotActivityConflict=false;
								$scope.previousPage=edata.ui.options.fromPage[0].id;
								$scope.isMashupShowable();
								$scope.isHotActivityConflict();
								
						
								
							});
							if(edata.data.activeTab){
								$("[href='#" + edata.data.activeTab + "']").addClass("ui-btn-active");
								$("[href='#" + edata.data.activeTab + "']").trigger("click");
							}	
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Process Instance retrieval failed.");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getActivityInstance(edata.data.id).then(success,fail);
				});

				/*Initialization*/
				$scope.activeTab='activityTab';
				
				$scope.createNote = function(oid,content){
					var success,fail;
					success=function(){
						workflowService.getNotes(oid).then(function(data){
							$scope.$apply(function(){
								$scope.notesModel.notes=data.notes;
							});
						});
					};
					fail = function(){
						console.log("failed");
					};
					workflowService.createNote(oid,content).then(success,fail);
				}; 
				
				
				
				/*Signal JQM to perform a manual navigation to a target page*/
				$scope.navigateTo = function(target){
					utilService.navigateTo($rootScope,target);
				};
				
				
			},
			
			"formCtrl" : function($scope,$rootScope){
				$scope.test= "Hello From Form Ctrl";
				$scope.formModel = new mashupModel();
			},
			
			"documentViewerCtrl" : function($scope, $rootScope, $sce, workflowService){
				$scope.documentViewerModel = new documentViewerModel();
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.scopeTarget != $scope.$id){return;}

					success=function(data){
							$scope.$apply(function(){
								$scope.documentViewerModel=data;
								$scope.documentViewerModel.downloadUrl = workflowService.getDocumentUrl(data.downloadToken);
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Document viewer data failed");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getDocument(edata.data.id,edata.data.processOid)
						.then(success,fail);
					
				});
			}
			
			
	};
	
	return worklistCtrl;
});