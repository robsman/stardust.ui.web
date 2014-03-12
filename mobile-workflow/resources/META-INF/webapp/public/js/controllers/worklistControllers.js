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
			
			"worklistCtrl" : function($scope, $rootScope, $filter, workflowService){
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.pageTarget != "worklistListViewPage"){return;}
					$rootScope.appData.activePage = edata.pageTarget;
					success=function(data){
						data.worklist= $filter("orderBy")(data.worklist,"oid",true);
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
					if(edata.pageTarget != "mainPage"){return;}
					$rootScope.appData.activePage = edata.pageTarget;
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
						
						if($rootScope.appData.isActivityHot==true || $rootScope.appData.isActivityHot=="true" ){
							/*We have a hotActivity already active...*/
							$scope.$apply(function(){
								$scope.uiModel.showHotNavBtn=true;
								$scope.uiModel.showProcessDetailsBtn=true;
								$scope.uiModel.showCloseDialogBtn=true;
								$scope.uiModel.popupMessage = data.processId + " (#" +
									data.activatedActivityInstance.processInstanceOid + ") was started " +
									"but we could not activate its first interactive Activity because " +
									"you have another active Activity.";
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
								$scope.uiModel.popupMessage=data.processId + " (#" +
									data.activatedActivityInstance.processInstanceOid + ") was started " +
									"but we could not activate its first interactive Activity because " + 
									"that Activity Instance is not activatable.";
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
					if(edata.pageTarget != "startableProcessesPage"){return;}
					$rootScope.appData.activePage = edata.pageTarget;
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
				$scope.resetGlobalState = function(){
						$rootScope.appData.user={};
						$rootScope.appData.isAuthorized=false;
						$rootScope.appData.isActivityHot = false;
						$rootScope.appData.hotActivityInstance = {};
				}
			},
			
			
			"processCtrl" : function($scope, $rootScope, $q, workflowService, utilService){
				
				/**
				 * 
				 * @param processOid
				 * @returns
				 */
				var init = function(processOid){
					var success,
						fail,
						deferred=$q.defer();
	
					workflowService.getProcessInstance(processOid)
						.then(function(data){
							$scope.$apply(function(){
									$scope.notesModel.notes = data.notes;
									$scope.activityModel.item ={
											"processInstanceOid" : processOid,
											"processName" : data.processName
									};
									$scope.documentModel.docs = data.documents;
									$scope.participantModel.participants = data.participants;
									$scope.processModel = data;
								});
							},function(status){
								console.log("Process Instance retrieval failed.");
								deferred.reject();
							})
						.then(function(){
								workflowService.getProcessHistory(processOid).then(function(data){
									$scope.$apply(function(){
										$scope.processHistoryModel.parentProcessInstances=data.parentProcessInstances;
										$scope.processHistoryModel.selectedProcessInstance=data.selectedProcessInstance;
										$scope.processHistoryModel.activityInstances=data.activityInstances;
									});
								},function(status){
									console.log("process history retrieval failed");
									deferred.reject();
								});
							})
						.then(deferred.resolve);
					
					return deferred.promise;
				};
				
				
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
				$scope.activeSubView = "overview";
				
				/*functions for determining classes we will need.*/
				$scope.getStateClass=utilService.getStateClass;
				$scope.getActivityTypeClass = utilService.getActivityTypeClass;
				
				/*intercept a click event to prevent default navigation on a subprocess*/
				$scope.interceptNav = function(activity,e){
					if(activity.childProcessInstance){
						e.preventDefault(); /*prevent navigation*/
						/*reload page by hand*/
						init(activity.childProcessInstance.oid);
					}

				}
				
				/*Build a url for an activityInstance*/
				$scope.getTargetUrl=function(activity){
					var url="#";
					if(!activity.childProcessInstance){
						url=url + "detailPage?id=" + activity.oid;
					}
					return url;
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
		        			  $scope.alertMessage="Upload Successful";
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
					if(edata.scopeTarget != "#processPage"){return;}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.$apply(function(){
						$scope.activeSubView = "overview";
					});
					
					init(edata.data.id)
						.then(edata.ui.bCDeferred.resolve);
				});
				
				$scope.createNote = function(oid,content){
					
					$scope.isAjaxLoading = true;
					workflowService.createNote(oid,content)
						.then(function(){
							return workflowService.getNotes(oid);
						})
						.then(function(data){
							$scope.$apply(function(){
								$scope.notesModel.notes=data.notes;
							});
						})
						.catch(function(){
							//TODO: Handle error conditions
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading = false;
							});
						});
				};
				
			},
			
			"detailCtrl" : function($scope,$rootScope,$filter,$sce,$q,utilService,workflowService){
				
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
				$scope.previousPage="";
				$scope.activeTab='activityTab';
				
				/*Initialization function, retrieve remote data and initialize UI with that data*/
				$scope.init=function(activityOid, prevPage){
					
					var deferred = $q.defer();
					workflowService.getActivityInstance(activityOid)
					.then(
						function(data){
							var sortedNotes = $filter("orderBy")(data.processInstance.notes,"timestamp",true),
							    sortedDocs = $filter("orderBy")(data.processInstance.documents,
								   "lastModifiedTimestamp",true),
						        url;
							
							/*Navigation to this controllers page from the startableProcessPage is the only case
							 *where we should load the mashup URL outside of an activate button click event.*/
							if(prevPage==="startableProcessesPage"){
								if(data.activatable){
									url=data.contexts.externalWebApp["carnot:engine:ui:externalWebApp:uri"] +
									    "?ippInteractionUri=" + data.contexts.externalWebApp.ippInteractionUri +
									    "&ippPortalBaseUri=" + data.contexts.externalWebApp.ippPortalBaseURi +
									    "&ippServicesBaseUri=" + data.contexts.externalWebApp.ippServicesBaseUri +
									    "&interactionId=" + data.contexts.externalWebApp.interactionId;
								}
								$scope.mashupModel.externalUrl= $sce.trustAsResourceUrl(url);
							}
						
							$scope.notesModel.notes = sortedNotes;
							$scope.activityModel.item = data;
							$scope.documentModel.docs = sortedDocs;
							
							deferred.resolve(data);
						},
						function(status){
							console.log("Process Instance retrieval failed.");
							deferred.reject();
						}
					);				
					return deferred.promise;
				};
				
				/* Always show activate/defaultUI unless our hotActivityInstance (meaning our currently
				 * activated instance matches our local scoped instance). In that case we should show our
				 * Mashup application as it has already been activated.*/
				$scope.isMashupShowable = function(force){
					return ($rootScope.appData.isActivityHot=="true" || $rootScope.appData.isActivityHot==true) && 
					        $scope.activityModel.item.oid == $rootScope.appData.hotActivityInstance.oid;
				};
				
				/*Test for a conflict with any previously activated instance that has not been completed.*/
				$scope.isHotActivityConflict = function(){
					var isHot=false;
					if($rootScope.appData.isActivityHot=="false" || $rootScope.appData.isActivityHot == false ){
						/*No hot activity thus no conflict*/
						isHot = false;
					}
					else if($scope.activityModel.item.oid !== $rootScope.appData.hotActivityInstance.oid){
						/*Hot activity and its oid does not match our scopes.*/
						isHot = true;
					}else{
						/*Hot activity and its oid matches ours*/
						isHot = false;
					}
					return isHot;
				};
				
				$scope.activate = function(activityOid){
					$scope.isAjaxLoading=true;
					workflowService.activate(activityOid)
					.then(
						function(data){
							
							var url=data.contexts.externalWebApp["carnot:engine:ui:externalWebApp:uri"] +
									"?ippInteractionUri=" + data.contexts.externalWebApp.ippInteractionUri +
									"&ippPortalBaseUri=" + data.contexts.externalWebApp.ippPortalBaseURi +
									"&ippServicesBaseUri=" + data.contexts.externalWebApp.ippServicesBaseUri +
									"&interactionId=" + data.contexts.externalWebApp.interactionId;
							
							/*Load new data for iframe, as soon as we modify externalUrl on our scope the 
							  iframe will trigger a load.*/
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
							
						})
						.then(function(){
								return workflowService.getActivityInstance(activityOid);
							}
						)
						.then(function(data){
							$scope.$apply(function(){
								$scope.activityModel.item = data;
								$scope.activeSubView='form';
							});
						})
						.catch(function(){
							/*TODO: handle errors*/
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading=false;
							});
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
		        	console.log("Document Upload Progress...");
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
				
				$scope.$on("activityStatusChange",function(e,data){
					if(data.newStatus=="complete"){
						console.log("DetailCtrl received activityStatusChangeEvent, intiating navigation...");
						$scope.mashupModel.externalUrl= "";
						$scope.mashupModel.interactionId="";
						utilService.navigateTo($rootScope,"#worklistListViewPage");
					}
					console.log(data);
					
				});
				
				/*Listener for JQuery Mobile Navigation events*/
				$scope.$on("jqm-navigate",function(e,edata){
					
					/*Filter messages not for our scope*/
					if(edata.pageTarget != "detailPage"){return;}
					$rootScope.appData.activePage = edata.pageTarget;
					console.log("initializing detailCtrl on jqmNavigate event");
					$scope.init(edata.data.id,edata.ui.options.fromPage[0].id)
						.then(function(){
							edata.ui.bCDeferred.resolve();
							console.log("Updating Previous page............................");
							console.log(edata.ui.options.fromPage[0].id);
							$scope.previousPage=edata.ui.options.fromPage[0].id;
							console.log("Previous page = " + $scope.previousPage);
							if(edata.data.activeTab=="formTab"){
								$scope.activeSubView='form';
							}
							else{
								$scope.activeSubView='activity';
							}

						})
						.catch(edata.ui.bCDeferred.resolve);
				});

				$scope.createNote = function(oid,content){
					
					$scope.isAjaxLoading = true;
					workflowService.createNote(oid,content)
						.then(function(){
							return workflowService.getNotes(oid);
						})
						.then(function(data){
							$scope.$apply(function(){
								$scope.notesModel.notes=data.notes;
							});
						})
						.catch(function(){
							//TODO: Handle error conditions
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading = false;
							});
						});
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
				
				$scope.getFolder = function(folderId,doPush,e){
					
					/*if user passes event object then call preventDefault*/
					if(e && e.preventDefault){
						e.preventDefault();
					}
					
					/*push target folderID onto our navStack so we can simulate a back navigation when required*/
					if(doPush==true){
						$scope.directoryNavStack.push({"id" : folderId});
					}
					
					$scope.isAjaxLoading = true;
					workflowService.getRepositoryFolder(folderId)
					.then(function(data){
						updateModel(data);
					})
					.catch(function(){
						/*TODO: handle errors*/
					})
					.finally(function(){
						$scope.$apply(function(){
							$scope.isAjaxLoading = false;
						});			
					});
				};
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					if(edata.pageTarget != "repositoryRootPage"){return;}
					
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
					$rootScope.appData.activePage = edata.pageTarget;			
					workflowService.getRepositoryFolder()
						.then(success,fail);
					
				});
			},
			
			"documentViewerCtrl" : function($scope, $rootScope, $sce, workflowService){
				$scope.documentViewerModel = new documentViewerModel();
				
				$scope.$on("jqm-navigate",function(e,edata){
					console.log(edata);
					var success,fail;
					if(edata.pageTarget != "documentViewerPage"){return;}
					console.log("documentViewer page controller initializing...");
					console.log("doc id=" + edata.data.id);
					console.log("process id=" + edata.data.processOid);
					$rootScope.appData.activePage = edata.pageTarget;
					success=function(data){
						console.log("workflowService.getDocument success...");
						console.log(data);
							$scope.$apply(function(){
								$scope.documentViewerModel=data;
								$scope.documentViewerModel.downloadUrl = workflowService.getDocumentUrl(data.downloadToken);
							});
							edata.ui.bCDeferred.resolve();
					},
					fail =  function(status){
							console.log("Document viewer data failed");
							console.log(status);
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