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
	
	
	processHistoryModel = function(){
		this.parentProcessInstances =[];
		this.selectedProcessInstance={};
		this.activityInstances=[];
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
	
	repositoryModel = function(){
		this.id="";
		this.name="";
		this.path="";
		this.children={};
		this.children.documents=[];
		this.children.folders=[];
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
			"startableProcessesCtrl" : function($scope,$rootScope,workflowService,utilService){
				
				$scope.test = "Hello From Startable Process(s) Control";
				$scope.startableProcessModel = new startableProcessModel();
				$scope.startableProcessModel.showPopup=false;
				$scope.uiModel={
						showPopup: false,
						showHotNavBtn: false,
						showProcessDetailsBtn: false,
						showCloseDialogBtn: true,
						popupMessage: "",
						currentSelectedProcessId: 0
				};
				/*function to handle the user clicking on a process in our process list*/
				$scope.startProcess = function(processDefinitionId){
					var success,fail;
					
					success=function(data){
						console.log("startProcess Returned...");
						console.log(data);
						$scope.$apply(function(){
							$scope.uiModel.currentSelectedProcessId=data.activatedActivityInstance.processInstanceOid;
						});
						
						if($rootScope.appData.isActivityHot){
							/*We have a hotActivity already active...*/
							$scope.$apply(function(){
								$scope.uiModel.showHotNavBtn=true;
								$scope.uiModel.showProcessDetailsBtn=true;
								$scope.uiModel.showCloseDialogBtn=true;
								$scope.uiModel.popupMessage = data.processId + "(#" +
									data.activatedActivityInstance.processInstanceOid + ") was started " +
									"but we could not activate its first interactive activity beacuase " +
									"you have another active activity.";
								$scope.uiModel.showPopup=true;
							});
						}
						else if(data.activatedActivityInstance.activatable==true){
							/*Set item as our hotActivityInstance and nav to detailPage->formTab*/
							$rootScope.$apply(function(){
								$rootScope.appData.isActivityHot = true;
								$rootScope.appData.hotActivityInstance = {
										"oid" : data.activatedActivityInstance.oid,
										"name" : data.activatedActivityInstance.activityName
								};
							});
							
							utilService.navigateTo($rootScope,
									   "#detailPage?id=" + 
									    data.activatedActivityInstance.oid +  
									   "&activeTab=formTab",{});
						}
						else{
							/*This item has been started but it is not activatable*/
							$scope.$apply(function(){
								$scope.uiModel.showHotNavBtn=false;
								$scope.uiModel.showProcessDetailsBtn=true;
								$scope.uiModel.showCloseDialogBtn=true;
								$scope.uiModel.popupMessage=data.processId + "(#" +
									data.activatedActivityInstance.processInstanceOid + ") was started " +
									"but we could not activate its first interactive activity because " + 
									"that activity instance is not activatable";
								$scope.uiModel.showPopup=true;
							});
							
						}
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
								$scope.uiModel.showPopup=false;
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
				$scope.processHistoryModel = new processHistoryModel();
				
				/*Set up a few UI specific props*/
				$scope.baseHref = workflowService.baseHref;
				$scope.showMsg = false;
				$scope.alertMessage = "";
				$scope.uploadSuccesful = false;
				$scope.isUploading =false;
				
				//TODO-move to util service
				$scope.getStateClass=function(state){
					var cssClass;
					switch(state){
					case "Application":
						cssClass="fa-spinner fa-spin";
						break;
					case "Completed":
						cssClass="fa-check-square-o";
						break;
					case "Aborted":
						cssClass="fa-times";
						break;
					case "Suspended":
						cssClass="fa-coffee";
						break;
					case "Hibernated":
						cssClass="fa-clock-o";
						break;
					case "Interrupted":
						cssClass="fa-exclamation-triangle";
						break;
					case "Created":
						cssClass="fa-magic";
						break;
					default:
						cssClass="fa-question-circle";
					}
					
					return cssClass;
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
					workflowService.getProcessHistory().then(function(data){
						console.log("process History data");
						console.log(data);
						$scope.processHistoryModel.parentProcessInstances =data.parentProcessInstances;
						$scope.processHistoryModel.selectedProcessInstance=data.selectedProcessInstance;
						$scope.processHistoryModel.activityInstances=data.activityInstances;
					},function(status){
						console.log("process history retrieval failed");
						console.log(status);
					});
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
					
					/*filter messages that don't match our scopeID*/
					if(edata.scopeTarget != $scope.$id){return;}
					success=function(data){
							var sortedNotes = $filter("orderBy")(data.processInstance.notes,
													"timestamp",true),
								sortedDocs = $filter("orderBy")(data.processInstance.documents,
													 "lastModifiedTimestamp",true),
								url;
							
							if(data.activatable){
								url=data.contexts.externalWebApp["carnot:engine:ui:externalWebApp:uri"] +
								"?interactionId=" + data.contexts.externalWebApp.interactionId;
							}
							
							$scope.$apply(function(){
								$scope.mashupModel.externalUrl= $sce.trustAsResourceUrl(url);
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
			
			"repositoryRootCtrl" : function($scope, $rootScope, workflowService, utilService){
				
				var updateModel = function(data){
						$scope.$apply(function(){
							$scope.repositoryModel.name = data.name;
							$scope.repositoryModel.id = data.id;
							$scope.repositoryModel.path = data.path;
							$scope.repositoryModel.children.folders = data.children.folders;
							$scope.repositoryModel.children.documents = data.children.documents;
						});
					},
					
					popNavStack = function(popOnDuplicateState){
						var targetFolder;						
						targetFolder=$scope.directoryNavStack.pop();
						if(!targetFolder){
							utilService.navigateTo($rootScope,"#mainPage");
						}
						/*Initial Back requries a double pop so we dont renavigate to the current page,
						 *we also have to check that we arent at the home/0 position in our stack as
						 *i nthat case we need to nav back to the main page.*/
						if(targetFolder.id==$scope.repositoryModel.id && popOnDuplicateState==true){
							targetFolder = $scope.directoryNavStack.pop();
						}
						$scope.getFolder(targetFolder.id,false);
					};
				
				$scope.directoryNavStack=[];
				$scope.popNavStack=popNavStack;
				$scope.test= "Hello From Repository-Root Ctrl";
				$scope.repositoryModel= new repositoryModel();	
				
				$scope.getFolder = function(folderId,doPush){
					if(doPush==true){
						$scope.directoryNavStack.push({"id" : folderId});
					}
					workflowService.getRepositoryFolder(folderId).then(function(data){
						console.log(data);
						updateModel(data);
					});
				};
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					console.log("repos navigation " + edata);
					
					if(edata.scopeTarget != $scope.$id){return;}
					
					if($scope.directoryNavStack.length > 0){
						$scope.popNavStack(false);
						edata.ui.bCDeferred.resolve();
						return;
					}
					
					success=function(data){
							updateModel(data);
							$scope.directoryNavStack.push({"id" : ""});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Repository Root Retrieval Failed.");
							edata.ui.bCDeferred.resolve();
					};
								
					workflowService.getRepositoryFolder()
						.then(success,fail);
					
				});
			},
			
			"documentViewerCtrl" : function($scope, $rootScope, $sce, workflowService){
				$scope.documentViewerModel = new documentViewerModel();
				
				$scope.$on("jqm-navigate",function(e,edata){
					console.log(edata);
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
					
					if(edata.data.processOid){			
						workflowService.getDocument(edata.data.id,edata.data.processOid)
							.then(success,fail);
					}
					else{
						workflowService.getRepositoryDocument(edata.data.folderid, edata.data.id)
							.then(success,fail);
					}
					
				});
			}
			
			
	};
	
	return worklistCtrl;
});