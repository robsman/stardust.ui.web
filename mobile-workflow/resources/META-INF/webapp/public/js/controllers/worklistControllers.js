/*Controllers Related to worklists...*/
define([],function(){
	
	
	var worklistModel =function(){
			var that = this;
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
	
	errorModel=function(){
		this.errorMessage="";
		this.hasError =false;
		this.showExtended =false;
	},
	
	infoModel=function(){
		this.infoMessage="";
		this.hasInfo=false;
		this.showExtended=false;
	},
	
	settingsModel = function(){
		this.maxItems=0;
		this.dateFormats=[];
		this.saveOptions=[];
	},
	
	warningModel=function(){
		this.warningMessage="";
		this.hasWarning=false;
		this.showExtended=false;
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
	
	activitySearchModel = function(){
		this.states=[],
		this.startableProcesses=[];
		this.activities=[];
		this.results=[];
	},
	
	documentSearchModel = function(){
		this.results=[];
		this.documentTypes=[];
	},
	
	processSearchModel = function(){
		this.states=[],
		this.processes=[];
		this.descriptors=[];
		this.results=[];
	},
	
	participantSearchModel =function(){
		this.matchStr="";
		this.results=[];
		this.selectedParticipant={"name" : ""};
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
	
	reportViewerModel = function(){
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
			
			"worklistCtrl" : function($scope, $rootScope, $q, $filter, workflowService,utilService,il18nService){
				
				$scope.errorModel = new errorModel();
				$scope.worklistModel=new worklistModel();
				
				$scope.uiText={
						"worklist" : il18nService.getProperty("mobile.worklist.header.text"),
						"newest"   : il18nService.getProperty("mobile.worklist.filter.item.newest"),
						"oldest"   : il18nService.getProperty("mobile.worklist.filter.item.oldest"),
						"critical" : il18nService.getProperty("mobile.worklist.filter.item.criticality"),
						"modified" : il18nService.getProperty("mobile.worklist.filter.item.modified"),
						"empty"    : il18nService.getProperty("mobile.worklist.message.empty")
				};
				
				$scope.init=function(){
					var deferred = $q.defer();
					
					workflowService.getWorklist()
					.then(function(data){
						data.worklist= $filter("orderBy")(data.worklist,"oid",true);
						$scope.$apply(function(){
							$scope.worklistModel.worklistItems = data.worklist;
						});
						deferred.resolve();
					})
					.catch(deferred.reject);
					
					return deferred.promise;
				};
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					/*Filter for events destined for our page*/
					if(edata.pageTarget != "worklistListViewPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					/*update global data with current page*/
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError = true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
						});
					})
					.finally(edata.ui.bCDeferred.resolve);

					
				});
			},
			
			"mainPageCtrl" : function($scope,$rootScope,$q,$timeout,workflowService,utilService,il18nService){
				
				$scope.uiText={
					"welcome"   : il18nService.getProperty("mobile.mainpage.header.text"),
					"worklist"  : il18nService.getProperty("mobile.mainpage.listview.worklist"),
					"startwork" : il18nService.getProperty("mobile.mainpage.listview.startwork"),
					"search"    : il18nService.getProperty("mobile.mainpage.listview.search"),
					"reports"   : il18nService.getProperty("mobile.mainpage.listview.reports"),
					"documents" : il18nService.getProperty("mobile.mainpage.listview.documents")
				}
				
				$scope.mainPageModel = new mainPageModel();
				$scope.errorModel = new errorModel();
				$scope.getProperty = il18nService.getProperty;
				
				$scope.init = function(){
					var deferred = $q.defer();
					
					workflowService.getWorklistCount()
					.then(function(data){
						$scope.$apply(function(){
							$scope.mainPageModel.worklistCount=data.total;
						});
						deferred.resolve();
					})
					.catch(deferred.reject);
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					if(edata.pageTarget != "mainPage"){return;}
					
					$rootScope.appData.activePage = edata.pageTarget;

					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage= $rootScope.appData.errorText.pageload;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			"documentSearchCtrl" : function($scope,$rootScope,$q,$timeout,workflowService,utilService,il18nService){
				var now = new Date(),
				    then = new Date(),
				    startDPO,
				    endDPO;
				
				/*set initial values for our dates.*/
				then.setDate(then.getDate() - 7); 
				startDPO = utilService.buildDatePartObject(then.toString());
				endDPO = utilService.buildDatePartObject(now.toString());
				
				$scope.isAjaxLoading=false;
				$scope.documentSearchModel = new documentSearchModel();
				$scope.errorModel = new errorModel();
				$scope.results=[];
				$scope.filter = {
						name : "",
						startDate : startDPO.yyyy + "-" + startDPO.MM + "-" + startDPO.dd,
						startTime : "00:00:01",
						endDate : endDPO.yyyy + "-" + endDPO.MM + "-" + endDPO.dd,
						endTime : endDPO.hh + ":" + endDPO.mm + ":" + endDPO.ss,
						documentTypes : []	
				};
				$scope.uiText={
						"searchDocs" : il18nService.getProperty("mobile.documentsearch.header.text"),
						"createFrom" : il18nService.getProperty("mobile.documentsearch.filters.from"),
						"createTo" : il18nService.getProperty("mobile.documentsearch.filters.to"),
						"search" : il18nService.getProperty("mobile.documentsearch.filters.name.placeholder"),
						"docTypes" : il18nService.getProperty("mobile.documentsearch.filters.doctypes"),
						"all" : il18nService.getProperty("mobile.documentsearch.filters.all"),
						"submit" : il18nService.getProperty("mobile.documentsearch.submit")
				};
				
				
				$scope.isImageType = utilService.isImageType;
				
				$scope.init = function(){
					var deferred = $q.defer();
					
					workflowService.getDocumentTypes()
					.then(function(data){
						$scope.$apply(function(){
							$scope.documentSearchModel.documentTypes=data.documentTypes;
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject)
					.finally(function(){
						$scope.$apply(function(){
							$scope.showResults=false;
							$scope.docTypeToggleState = true;
							$scope.toggleAll($scope.documentSearchModel.documentTypes,true,false);
						});
					});
					
					return deferred.promise;
				}
				
				/*Set all options as either true or false*/
				$scope.toggleAll = function(data,isChecked,e){
					if(e && e.preventDefault){
						e.preventDefault();
						e.stopImmediatePropagation();
					}
					data.forEach(function(v){
						v.isChecked=isChecked;
					});
				}
				
				$scope.getResults = function(){
					
					var docIDs=[],
						startDT,
						endDT;
					
					startDT = new Date($scope.filter.startDate + " " + $scope.filter.startTime).getTime();
					endDT = new Date($scope.filter.endDate + " " + $scope.filter.endTime).getTime();
					
					$scope.documentSearchModel.documentTypes.forEach(function(v){
						if(v.isChecked){docIDs.push(v.id)}
					}); 
					
					$scope.isAjaxLoading=true;
					workflowService.getFilteredDocuments(
							$scope.filter.name,
							startDT,
							endDT,
							docIDs.toString())
						.then(function(data){
							console.log(data);
							$scope.$apply(function(){
								$scope.documentSearchModel.results=data.documents;
							});
						})
						.catch(function(err){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage= $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading=false;
								$scope.showResults=true;
							});
						});
					
				};
				
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "documentSearchPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage= $rootScope.appData.errorText.pageload;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
				
			},
			
			"activitySearchCtrl" : function($scope,$rootScope,$q, $timeout, workflowService,utilService,il18nService){
				var now = new Date(),
				    then = new Date(),
				    startDPO,
				    endDPO,
				    tmrPromise;
				
				then.setDate(then.getDate() - 7); 
				startDPO = utilService.buildDatePartObject(then.toString());
				endDPO = utilService.buildDatePartObject(now.toString());
				
				$scope.isAjaxLoading = false;

				$scope.filter = {
						startDate : startDPO.yyyy + "-" + startDPO.MM + "-" + startDPO.dd,
						startTime : "00:00:01",
						endDate : endDPO.yyyy + "-" + endDPO.MM + "-" + endDPO.dd,
						endTime : endDPO.hh + ":" + endDPO.mm + ":" + endDPO.ss,
						processes : [],
						activities : [],
						states : []
						
				};
				
				$scope.uiText = {
						"searchAct" : il18nService.getProperty("mobile-workflow-client-messages_en.properties"),
						"startFrom" : il18nService.getProperty("mobile.activitysearch.filters.from"),
						"startTo" : il18nService.getProperty("mobile.activitysearch.filters.to"),
						"process" : il18nService.getProperty("mobile.activitysearch.filters.process"),
						"all" : il18nService.getProperty("mobile.activitysearch.filters.all"),
						"activity" : il18nService.getProperty("mobile.activitysearch.filters.Activity"),
						"state" : il18nService.getProperty("mobile.activitysearch.filters.State"),
						"submit" : il18nService.getProperty("mobile.activitysearch.submit")
				};
					
				/*Set all options as either true or false*/
				$scope.toggleAll = function(data,isChecked,e){
					if(e && e.preventDefault){
						e.preventDefault();
						e.stopImmediatePropagation();
					}
					data.forEach(function(v){
						v.isChecked=isChecked;
					});
				}
				
				$scope.activitySearchModel =new activitySearchModel();
				$scope.errorModel = new errorModel();
				
				/* Retrieves activities from the server based on a collection of processIDs. 
				 * A delay is inherent in the function in order to compensate for a user 
				 * selecting multiple processes from the UI before we retrieve the data from the 
				 * server.*/
				$scope.getActivities = function(){
					
					/*Cancel previous timeout*/
		            if(tmrPromise){
		            	$timeout.cancel(tmrPromise);
		            }
		            
		            /*Create a new timeout promise*/
		            tmrPromise=$timeout(function(){
		            	var procIDs=[];
		            	$scope.activitySearchModel.startableProcesses.forEach(function(v){
							if(v.isChecked){procIDs.push(v.id)}
						});  
			            return procIDs.toString();
		            },1000,true);
		            
		            /*If we aren't canceled then do something expensive*/
		            tmrPromise.then(function(data){
		            		$scope.isAjaxLoading=true;
			            	return workflowService.getActivitesByProcess(data);
		            	})
		            	.then(function(data){
	            			$scope.activitySearchModel.activities = data.activities;
		            	})
		            	.catch(function(err){
		            		$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
		            	})
			            .finally(function(){
			            	$scope.isAjaxLoading=false;
			            });
				};
				
				$scope.getResults=function(){
					
					var startDT,
					    endDT,
					    activityIDs=[],
					    stateIDs=[];
				    	
					startDT = new Date($scope.filter.startDate + " " + $scope.filter.startTime).getTime();
					endDT = new Date($scope.filter.endDate + " " + $scope.filter.endTime).getTime();
					
					$scope.activitySearchModel.activities.forEach(function(v){
						if(v.isChecked){activityIDs.push(v.id)}
					});
					
					$scope.activitySearchModel.states.forEach(function(v){
						if(v.isChecked){stateIDs.push(v.value)}
					});
					
					$scope.isAjaxLoading=true;
					workflowService.getFilteredActivities(
							startDT,
							endDT,
							activityIDs.toString(),
							stateIDs.toString())
						.then(function(data){
							console.log(data);
							$scope.$apply(function(data){
								$scope.activitySearchModel.results=data.activities;
							});
						})
						.catch(function(err){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading=false;
								$scope.showResults=true;
							});
						});
					
				};
				
				$scope.init = function(){
					var deferred = $q.defer();
					
					workflowService.getActivityStates()
					.then(function(data){
						$scope.$apply(function(){
							$scope.activitySearchModel.states=data.activityInstanceStates;
						});
					})
					.then(workflowService.getStartableProcesses)
					.then(function(data){
						$scope.$apply(function(){
							$scope.activitySearchModel
								  .startableProcesses=data.processDefinitions;
						});						
					})
					.then(deferred.resolve)
					.catch(deferred.reject)
					.finally(function(){
						$scope.$apply(function(){
							$scope.showResults=false;
							$scope.processesToggleState = true;
							$scope.activityToggleState = true;
							$scope.stateToggleState = false;
							$scope.toggleAll($scope.activitySearchModel.startableProcesses,true,false);
							$scope.toggleAll($scope.activitySearchModel.activities,true,false);
						});	
					});
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					if(edata.pageTarget != "activitySearchPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);

				});
			},
			
			"processSearchCtrl" : function($scope,$rootScope,$q,$timeout, workflowService, utilService, il18nService){
				var now = new Date(),
				    then = new Date(),
				    startDPO,
				    endDPO;

				$scope.isAjaxLoading=false;
				$scope.processSearchModel=new processSearchModel();
				$scope.errorModel = new errorModel();
				
				$scope.uiText = {
						"searchProcs" : il18nService.getProperty("mobile.processsearch.header.text"),
						"start" : il18nService.getProperty("mobile.processsearch.filters.start"),
						"to" : il18nService.getProperty("mobile.processsearch.filters.to"),
						"process" : il18nService.getProperty("mobile.processsearch.filters.process"),
						"state" : il18nService.getProperty("mobile.processsearch.filters.state"),
						"all" : il18nService.getProperty("mobile.processsearch.filters.all"),
						"submit" : il18nService.getProperty("mobile.processsearch.filters.submit")
				};
				
				/*init filter values for our dates*/
				then.setDate(then.getDate() - 7); 
				startDPO = utilService.buildDatePartObject(then.toString());
				endDPO = utilService.buildDatePartObject(now.toString());
				
				
				$scope.filter = {
						startDate : startDPO.yyyy + "-" + startDPO.MM + "-" + startDPO.dd,
						startTime : "00:00:01",
						endDate : endDPO.yyyy + "-" + endDPO.MM + "-" + endDPO.dd,
						endTime : endDPO.hh + ":" + endDPO.mm + ":" + endDPO.ss,
						processes : [],
						states : []
				};
				
				/*Set all options as either true or false*/
				$scope.toggleAll = function(data,isChecked,e){
					e.preventDefault();
					e.stopImmediatePropagation();
					data.forEach(function(v){
						v.isChecked=isChecked;
					});
				}
				
				/*Gather up data from our user interface and call the workflow service for
				 *our search resutls.*/
				$scope.getResults=function(){
					
					var startDT,
					    endDT,
					    processIDs=[],
					    stateIDs=[];
					    	
					startDT = new Date($scope.filter.startDate + " " + $scope.filter.startTime).getTime();
					endDT = new Date($scope.filter.endDate + " " + $scope.filter.endTime).getTime();
					
					
					$scope.processSearchModel.processes.forEach(function(v){
						if(v.isChecked){processIDs.push(v.id)}
					});
					
					$scope.processSearchModel.states.forEach(function(v){
						if(v.isChecked){stateIDs.push(v.value)}
					});
					
					$scope.isAjaxLoading=true;
					workflowService.getFilteredProcesses(
							startDT,
							endDT,
							processIDs.toString(),
							stateIDs.toString())
						.then(function(data){
							$scope.$apply(function(){
								$scope.processSearchModel.results=data.processInstances;
							});
						})
						.catch(function(err){
							$scope.$apply(function(){
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$scope.hasError = true;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading=false;
								$scope.showResults=true;
							});
						});
					
				};
				
				$scope.init =function(){
					var deferred = $q.defer();
					
					workflowService.getProcessDefinitions()
					.then(function(data){
						$scope.$apply(function(){
							$scope.processSearchModel.processes=data.processDefinitions;
						});
					})
					.then(workflowService.getProcessStates)
					.then(function(data){
						$scope.$apply(function(){
							$scope.processSearchModel.states=data.processInstanceStates;
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject)
					.finally(function(){
						$scope.$apply(function(){
							$scope.showResults=false;
							$scope.processToggleState = false;
							$scope.stateToggleState = false;
						});	
					});
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					if(edata.pageTarget != "processSearchPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
					}
					
					$scope.init()
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
								$scope.hasError = true;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(edata.ui.bCDeferred.resolve);
					
				});
			},								
			
			/*startableProcessesControl*/
			"startableProcessesCtrl" : function($scope,$rootScope,$q,$timeout,workflowService,utilService,il18nService){
				
				$scope.startableProcessModel = new startableProcessModel();
				$scope.startableProcessModel.showPopup=false;
				$scope.errorModel = new errorModel();
				
				$scope.uiText={
						"startWork" : il18nService.getProperty("mobile.startableprocess.header.text"),
						"popupHdr" : il18nService.getProperty("mobile.startableprocess.popup.header"),
						"popupSubhdr" : il18nService.getProperty("mobile.startableprocess.popup.subheader"),
						"conflict" : il18nService.getProperty("mobile.startableprocess.popup.message.conflict"),
						"nonactivatable" : il18nService.getProperty("mobile.startableprocess.popup.message.nonactivatable"),
						"return" : il18nService.getProperty("mobile.startableprocess.popup.button.return"),
						"view" : il18nService.getProperty("mobile.startableprocess.popup.button.view"),
						"close" : il18nService.getProperty("mobile.startableprocess.popup.button.close")
				}
				
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
						
						var activityIDtext =data.processId + " (#" + data.activatedActivityInstance.processInstanceOid + ") ";
						
						$scope.$apply(function(){
							$scope.uiModel.currentSelectedProcessId=data.activatedActivityInstance.processInstanceOid;
						});
						
						if($rootScope.appData.isActivityHot==true || $rootScope.appData.isActivityHot=="true" ){
							/*We have a hotActivity already active...*/
							$scope.$apply(function(){
								
								$scope.uiModel.showHotNavBtn=true;
								$scope.uiModel.showProcessDetailsBtn=true;
								$scope.uiModel.showCloseDialogBtn=true;
								$scope.uiModel.popupMessage = $scope.uiText.conflict.replace('{0}',activityIDtext);
								$scope.uiModel.showPopup=true;
							});
						}
						else if(data.activatedActivityInstance.activatable==true){
							
							/*set our rootscope data but*/
							$rootScope.appData.isActivityHot = true;
							$rootScope.appData.hotActivityInstance = {
									"oid" : data.activatedActivityInstance.oid,
									"name" : data.activatedActivityInstance.activityName
							};
							
							/*Set item as our hotActivityInstance and nav to detailPage->formTab*/
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
								$scope.uiModel.popupMessage=$scope.uiText.nonactivatable.replace('{0}',activityIDtext);
								$scope.uiModel.showPopup=true;
							});
							
						}
					},
					fail =  function(status){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage=$rootScope.appData.errorText.startprocess;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
					};
					
					workflowService.startProcess(processDefinitionId)
						.then(success,fail);
					
				};
				
				$scope.init=function(){
					var deferred = $q.defer();
					
					workflowService.getStartableProcesses()
					.then(function(data){
						$scope.$apply(function(){					
							$scope.startableProcessModel.processes=data.processDefinitions;
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject)
					.finally(function(){
						$scope.$apply(function(){
							$scope.uiModel.showPopup=false;
						});
					});
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "startableProcessesPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.init()
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			/*panelControl*/
			"panelCtrl" : function($scope,$rootScope,il18nService){
				
				$scope.uiText={
						"home"     : il18nService.getProperty("mobile.extpanelright.listview.home"),
						"profile"     : il18nService.getProperty("mobile.extpanelright.listview.profile"),
						"settings" : il18nService.getProperty("mobile.extpanelright.listview.settings"),
						"logout"   : il18nService.getProperty("mobile.extpanelright.listview.logout")
				}
				
				$scope.resetGlobalState = function(){
						$rootScope.appData.user={};
						$rootScope.appData.isAuthorized=false;
						$rootScope.appData.isActivityHot = false;
						$rootScope.appData.hotActivityInstance = {};
				}
			},
			
			
			"processCtrl" : function($scope,$rootScope,$filter,$q,$timeout,workflowService,utilService,il18nService){
				
				$scope.getProperty=il18nService.getProperty;
				
				/*including text for all subpages declared in JQM template directives*/
				$scope.uiText={
						"overview"     : il18nService.getProperty("mobile.process.tabs.overview"),
						"documents"    : il18nService.getProperty("mobile.process.tabs.documents"),
						"notes"        : il18nService.getProperty("mobile.process.tabs.notes"),
						"participants" : il18nService.getProperty("mobile.process.tabs.participants"),
						"history"      : il18nService.getProperty("mobile.process.tabs.history"),
						
						//History Template
						"completed"    : il18nService.getProperty("mobile.processhistory.listview.completed"),
						"completedby"    : il18nService.getProperty("mobile.processhistory.listview.completedby"),
						"suspended"    : il18nService.getProperty("mobile.processhistory.listview.suspended"),
						"suspendedto"    : il18nService.getProperty("mobile.processhistory.listview.suspendedto"),
						
						//Details Template
						"OID"          : il18nService.getProperty("mobile.processdetails.listview.oid"),
						"state"        : il18nService.getProperty("mobile.processdetails.listview.state"),
						"starttime"    : il18nService.getProperty("mobile.processdetails.listview.starttime"),
						"startedby"    : il18nService.getProperty("mobile.processdetails.listview.startby"),
						"priority"     : il18nService.getProperty("mobile.processdetails.listview.priority"),
						"low"		   : il18nService.getProperty("mobile.processdetails.priority.low"),
						"normal"	   : il18nService.getProperty("mobile.processdetails.priority.normal"),
						"high"		   : il18nService.getProperty("mobile.processdetails.priority.high"),
						
						//Notes Template
						"addNote"      : il18nService.getProperty("mobile.notes.addnote.header"),
						"newest"       : il18nService.getProperty("mobile.notes.popup.newest"),
						"oldest"       : il18nService.getProperty("mobile.notes.popup.oldest"),
						
						//Document Template
						"addafile"     : il18nService.getProperty("mobile.fileupload.header")
				}
				
				$scope.init = function(processOid){
					var deferred = $q.defer();
					
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
					})
					.then(function(){
						return workflowService.getProcessHistory(processOid);
					})
					.then(function(data){
						$scope.$apply(function(){
							$scope.processHistoryModel.parentProcessInstances=data.parentProcessInstances;
							$scope.processHistoryModel.selectedProcessInstance=data.selectedProcessInstance;
							$scope.processHistoryModel.activityInstances=data.activityInstances;
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject);
					
					return deferred.promise;					
				}

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
									deferred.reject();
								});
							})
						.then(deferred.resolve)
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage= $rootScope.appData.errorText.pageload;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						});
					
					return deferred.promise;
				};
				
				
				/*declare our model(s)*/
				$scope.notesModel = new notesModel();
				$scope.errorModel = new infoModel();
				$scope.infoModel = new infoModel();
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
				
				/*test a filename for an image type extension*/
				$scope.isImageType = utilService.isImageType;
				
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
				
				/*Update process Priority, valid values -1,0,1*/
				$scope.setPriority = function(oid,priority){
					
					$scope.isAjaxLoading = true;
					
					workflowService.setProcessPriority(oid,priority)
					.then(function(data){
						$scope.processModel.priority = priority;
					})
					.catch(function(err){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.priority;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(function(){
						$scope.$apply(function(){
							$scope.isAjaxLoading = false;
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
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						};
						
					workflowService.getDocuments(processOid).then(success,fail);
				};
				
				/*Reporter object we will tie to our file-upload directive*/
				$scope.uploadReporter ={
		          onProgress: function(e){
		        	//Not used...
		          },
		          onLoad: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.isUploading=false;
		        		  if(e.currentTarget.status==200){
		        			  $scope.getDocuments($scope.activityModel.item.processInstanceOid);
		        			  $scope.infoModel.hasInfo=true;
		        			  $scope.infoModel.infoMessage=$rootScope.appData.infoText.upload;
		        			  $timeout(function(){
								  $scope.infoModel.hasInfo=false;
							  },$rootScope.appData.barDuration);
		        		  }else{
		        			  $scope.errorModel.hasError=true;
							  $scope.errorModel.errorMessage=$rootScope.appData.errorText.upload;
							  $timeout(function(){
								  $scope.errorModel.hasError=false;
							  },$rootScope.appData.barDuration);
		        		  }
		        	  });
		          },
		          onError: function(e){
		        	  $scope.$apply(function(){
						  $scope.errorModel.hasError=true;
						  $scope.errorModel.errorMessage=$rootScope.appData.errorText.upload;
						  $timeout(function(){
							  $scope.errorModel.hasError=false;
						  },$rootScope.appData.barDuration);
		        	  });
		          }
		        };
				
				/*Listener for JQuery Mobile navigation events*/
				$scope.$on("jqm-navigate",function(e,edata){
					if(edata.scopeTarget != "#processPage"){return;}
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.$apply(function(){
						$scope.activeSubView = "overview";
					});
					
					$scope.init(edata.data.id)
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
								$scope.errorModel.hasError = true;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(edata.ui.bCDeferred.resolve);

				});
				
				$scope.createNote = function(oid,content){
					
					$scope.isAjaxLoading = true;
					workflowService.createNote(oid,content)
						.then(function(){
							return workflowService.getNotes(oid);
						})
						.then(function(data){
							data.notes=$filter("orderBy")(data.notes,"timestamp",true);
							$scope.$apply(function(){
								$scope.notesModel.notes=data.notes;
							});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage=$rootScope.appData.errorText.notesave;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading = false;
							});
						});
				};
				
			},
			
			"detailCtrl" : function($scope,$rootScope,$filter,$sce,$q,$timeout,utilService,workflowService,il18nService){
				
				$scope.uiText={
						"activity"     : il18nService.getProperty("mobile.detail.tabs.activity"),
						"form"     : il18nService.getProperty("mobile.detail.tabs.form"),
						"documents"     : il18nService.getProperty("mobile.detail.tabs.documents"),
						"notes"     : il18nService.getProperty("mobile.detail.tabs.notes"),
						"process"     : il18nService.getProperty("mobile.detail.tabs.process"),
						"activate"     : il18nService.getProperty("mobile.detail.activity.button.activate"),
						"notActivatableHdr"     : il18nService.getProperty("mobile.detail.activity.popup.notactivatable.header"),
						"notActivatableSub"     : il18nService.getProperty("mobile.detail.activity.popup.notactivatable.subheader"),
						"notActivatableText"     : il18nService.getProperty("mobile.detail.activity.popup.notactivatable.text"),
						"cancel"     : il18nService.getProperty("mobile.detail.activity.popup.notactivatable.button.cancel"),
						"view"     : il18nService.getProperty("mobile.detail.activity.popup.notactivatable.button.view"),
						"delegate"     : il18nService.getProperty("mobile.detail.activity.button.delegate"),
						"delegateActivity"     : il18nService.getProperty("mobile.detail.activity.popup.delegate.header"),
						"whichParticipant"     : il18nService.getProperty("mobile.detail.activity.popup.delegate.text"),
						
						//Details Template
						"oid"         : il18nService.getProperty("mobile.activitydetails.listview.oid"),
						"status"      : il18nService.getProperty("mobile.activitydetails.listview.status"),
						"criticality" : il18nService.getProperty("mobile.activitydetails.listview.criticality"),
						"lastperformer" : il18nService.getProperty("mobile.activitydetails.listview.lastperformer"),
						"assignedto"  : il18nService.getProperty("mobile.activitydetails.listview.assignedto"),
						"starttime"   : il18nService.getProperty("mobile.activitydetails.listview.starttime"),
						"duration"    : il18nService.getProperty("mobile.activitydetails.listview.duration"),
						
						//Notes Template
						"addNote"      : il18nService.getProperty("mobile.notes.addnote.header"),
						"newest"       : il18nService.getProperty("mobile.notes.popup.newest"),
						"oldest"       : il18nService.getProperty("mobile.notes.popup.oldest"),
						
						//Document Template
						"addafile"     : il18nService.getProperty("mobile.fileupload.header")
				}
				
				$scope.notesModel = new notesModel();
				$scope.errorModel=new errorModel();
				$scope.infoModel = new infoModel();
				$scope.activityModel = new worklistItem();
				$scope.formModel = new mashupModel();
				$scope.documentModel = new documentModel();
				$scope.mashupModel = new mashupModel();
				$scope.participantSearchModel = new participantSearchModel();
				$scope.delegatePopupUI ={
						setInputFocus : false
				};
				$scope.baseHref = workflowService.baseHref;
				$scope.showMsg = false;
				$scope.alertMessage = "";
				$scope.uploadSuccesful = false;
				$scope.isUploading =false;
				$scope.formTabTarget = "#formTab";
				$scope.previousPage="";
				$scope.activeTab='activityTab';
				
				$scope.isImageType = utilService.isImageType;
				
				/*Initialization function, retrieve remote data and initialize UI with that data*/
				$scope.init=function(activityOid, prevPage){
					
					var deferred = $q.defer();
					
					$scope.notesModel.newNote.content="";
					
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
							$scope.activeSubView="form";
							$scope.notesModel.notes = sortedNotes;
							$scope.activityModel.item = data;
							$scope.documentModel.docs = sortedDocs;
							
							deferred.resolve(data);
						}
					).catch(deferred.reject);
					
					return deferred.promise;
				};
				

				$scope.getParticipantMatches = function(val){
					workflowService.getParticipantMatches(val)
						.then(function(data){
							$scope.$apply(function(){
								$scope.participantSearchModel.results=data;
							});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally();
				}
				
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
							});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage=$rootScope.appData.errorText.activation;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						})
						.finally(function(){
							$scope.$apply(function(){
								$scope.isAjaxLoading=false;
								$scope.activeSubView=='form'
							});
						});
				};
				
				/*Helper function to refresh our scoped document collection*/
				$scope.getDocuments = function(processOid){
						
					workflowService.getDocuments(processOid)
						.then(function(data){
							$scope.$apply(function(){
								$scope.documentModel.docs = data.documents;
							});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
								$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
								$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
							});
						});
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
		        			  $scope.infoModel.hasInfo=true;
		        			  $scope.infoModel.infoMessage=$rootScope.appData.infoText.upload;
		        			  $timeout(function(){
								  $scope.infoModel.hasInfo=false;
							  },$rootScope.appData.barDuration);
		        			  $scope.getDocuments($scope.activityModel.item.processInstanceOid);
		        		  }
		        		  else{
							  $scope.errorModel.hasError=true;
							  $scope.errorModel.errorMessage=$rootScope.appData.errorText.upload;
							  $timeout(function(){
								  $scope.errorModel.hasError=false;
						      },$rootScope.appData.barDuration);
		        		  }
		        	  });
		          },
		          onError: function(e){
		        	  $scope.$apply(function(){
		        		  $scope.errorModel.uploadSuccesful=false;
		        		  $scope.errorModel.errorMessage=$rootScope.appData.errorText.refresh;
		        		  $timeout(function(){
							  $scope.errorModel.hasError=false;
						  },$rootScope.appData.barDuration);
		        	  });
		          }
		        };
				
				$scope.$on("login",function(e,d){
					if($scope.mashupModel){
						$scope.mashupModel.externalUrl="blank.html";
					}
				});
				
				$scope.$on("activityStatusChange",function(e,data){
					switch(data.newStatus){
						case "complete"       :
						case "suspend"        :
						case "suspendAndSave" :
							console.log("DetailCtrl received activityStatusChangeEvent, intiating navigation...");
							$scope.mashupModel.externalUrl= "blank.html";
							$scope.mashupModel.interactionId="";
							utilService.navigateTo($rootScope,"#worklistListViewPage");
							break;	
					}

				});
				
				/*Listener for JQuery Mobile Navigation events*/
				$scope.$on("jqm-navigate",function(e,edata){
					
					/*Filter messages not for our scope*/
					if(edata.pageTarget != "detailPage"){return;}
					
					/*Force unauthorized users to appropriate page...*/
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					console.log("initializing detailCtrl on jqmNavigate event");
					$scope.init(edata.data.id,edata.ui.options.fromPage[0].id)
						.then(function(){
							var prevPage = edata.ui.options.fromPage[0].id;
							edata.ui.bCDeferred.resolve();
	
							if(prevPage=="worklistListViewPage" || prevPage=="startableProcessesPage"){
								$scope.previousPage=edata.ui.options.fromPage[0].id;
							}

							if(edata.data.activeTab=="formTab"){
								$scope.activeSubView='form';
							}
							else{
								$scope.activeSubView='activity';
							}

						})
						.catch(function(){
							$scope.$apply(function(){
				        		  $scope.errorModel.hasError=true;
				        		  $scope.errorModel.errorMessage= $rootScope.appData.errorText.pageload;
				        		  $timeout(function(){
									  $scope.errorModel.hasError=false;
								  },$rootScope.appData.barDuration);
				        	});
							edata.ui.bCDeferred.resolve();
						});
				});

				$scope.createNote = function(oid,content){
					
					$scope.isAjaxLoading = true;
					workflowService.createNote(oid,content)
						.then(function(){
							return workflowService.getNotes(oid);
						})
						.then(function(data){
							data.notes=$filter("orderBy")(data.notes,"timestamp",true);
							$scope.$apply(function(){		
								$scope.notesModel.notes=data.notes;
							});
						})
						.catch(function(){
							$scope.$apply(function(){
								$scope.errorModel.hasError=true;
				        		$scope.errorModel.errorMessage=$rootScope.errorText.notesave;
				        		$timeout(function(){
									$scope.errorModel.hasError=false;
								},$rootScope.appData.barDuration);
				        	});
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
			
			"reportRootCtrl" : function($scope, $rootScope, $timeout, $q, workflowService, utilService){
				
				var updateModel = function(data){
					$scope.$apply(function(){
						$scope.repositoryModel.name = data.name;
						$scope.repositoryModel.id = data.id;
						$scope.repositoryModel.path = data.path;
						$scope.repositoryModel.children.folders = data.children.folders;
						$scope.repositoryModel.children.documents = data.children.documents;
					});
				},
				
				/*Push an item onto our navigation stack so we can simulate back navigation through our
				 *folder structure. This requires that navigation to the docViewer page insert the doc id onto
				 *the stack as well as nav to a new folder doing likewise. In otherwords we have to push when
				 *we load a new folder and stay on the repository page, as well as navigating to the document
				 *viewer page to view an image or resource.
				 */
				pushNavStack=function(id){
					$scope.directoryNavStack.push(id);
				}
				
				popNavStack = function(popOnDuplicateState){
					var targetFolder;	
					
					/*Pop top of the stack as it represents where we are now*/
					$scope.directoryNavStack.pop();
					
					/*get top of stack as it now represents where we wish to go*/
					targetFolder=$scope.directoryNavStack[$scope.directoryNavStack.length-1];
					
					/*if top of the stack is falsey, nav back to #mainPage*/
					if(!targetFolder){
						utilService.navigateTo($rootScope,"#mainPage");
						return;
					}
					
					/*we now have a folderID so load it.*/
					$scope.getFolder(targetFolder.id,false);
				};
				$scope.errorModel=new errorModel();
				$scope.directoryNavStack=[];
				$scope.pushNavStack=pushNavStack;
				$scope.popNavStack=popNavStack;
				$scope.repositoryModel= new repositoryModel();	
				$scope.isReportType = utilService.isReportType;
				
				$scope.getViewerUrl=function(docName){
					if(utilService.isReportType(docName)){
						return "#reportViewerPage";
					}else{
						return "#documentViewerPage";
					}
				}
				
				$scope.getDocumentClass = function(docName){
					var docClass="fa-file-o";
					if(utilService.isReportType(docName)==true){
						docClass="fa-bar-chart-o";
					}
					else if(utilService.isImageType(docName)){
						docClass="fa-picture-o";
					}
					return docClass;
				}
				
				$scope.getFolder = function(folderId,doPush,e){
					
					/*if user passes event object then call preventDefault*/
					if(e && e.preventDefault){
						e.preventDefault();
					}
					
					/*push target folderID onto our navStack so we can simulate a back navigation when required*/
					if(doPush==true){
						pushNavStack({"id" : folderId});
					}
					
					$scope.isAjaxLoading = true;
					workflowService.getReportFolder(folderId)
					.then(function(data){
						updateModel(data);
					})
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage=$rootScope.errorText.folder;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(function(){
						$scope.$apply(function(){
							$scope.isAjaxLoading = false;
						});			
					});
				};
				
				$scope.init = function(){
					var deferred = $q.defer();
					
					workflowService.getReportFolder()
					.then(function(data){
						updateModel(data);
						$scope.directoryNavStack.push({"id" : ""});	
					})
					.then(deferred.resolve)
					.catch(deferred.reject);
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					
					if(edata.pageTarget != "reportRootPage"){return;}
	
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					if($scope.directoryNavStack.length > 0){
						$scope.popNavStack(false);
						edata.ui.bCDeferred.resolve();
						return;
					}
					
					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage=$rootScope.errorText.folder;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			"reportViewerCtrl" : function($scope, $rootScope,$q,$timeout, $sce, workflowService, utilService, il18nService){
				
				$scope.uiText = {
						"content" : il18nService.getProperty("mobile.reportviewer.tabs.content"),
						"parameters" : il18nService.getProperty("mobile.reportviewer.tabs.parameters"),
						"detail" : il18nService.getProperty("mobile.reportviewer.tabs.detail"),
						"runtime" : il18nService.getProperty("mobile.reportviewer.view.content.runtime"),
						"email" : il18nService.getProperty("mobile.reportviewer.view.content.button.email"),
						"export" : il18nService.getProperty("mobile.reportviewer.view.content.button.export"),
						"from" : il18nService.getProperty("mobile.reportviewer.view.content.parameters.label.from"),
						"to" : il18nService.getProperty("mobile.reportviewer.view.content.parameters.label.to"),
						"submit" : il18nService.getProperty("mobile.reportviewer.view.content.parameters.submit.text"),
						"process" : il18nService.getProperty("mobile.reportviewer.view.content.parameters.process"),
						"filetype" : il18nService.getProperty("mobile.reportviewer.view.content.details.listitem.filetype"),
						"filesize" : il18nService.getProperty("mobile.reportviewer.view.content.details.listitem.filesize"),
						"author" : il18nService.getProperty("mobile.reportviewer.view.content.details.listitem.author"),
						"created" : il18nService.getProperty("mobile.reportviewer.view.content.details.listitem.created"),
						"lastmodified" : il18nService.getProperty("mobile.reportviewer.view.content.details.listitem.lastmodified")
				}
				
				$scope.documentViewerModel = new documentViewerModel();
				$scope.errorModel=new errorModel();
				
				$scope.init = function(docId){
					var deferred=$q.defer();
					
					workflowService.getDocument(docId)
					.then(function(data){
						$scope.$apply(function(){
							$scope.documentViewerModel=data;
							$scope.documentViewerModel.downloadUrl = workflowService.getDocumentUrl(data.downloadToken);
							$scope.activeSubView="content";
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject);
					
					return deferred.promise;
				}
				
				$scope.isImageType = utilService.isImageType;
				$scope.isReportType = utilService.isReportType;
				$scope.isReportDefinition = utilService.isReportDefinition;
				$scope.isReportInstance = utilService.isReportInstance;
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "reportViewerPage"){return;}
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.init(edata.data.id)
					.catch(function(err){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			"repositoryRootCtrl" : function($scope, $rootScope,$timeout, $q, workflowService, utilService){
				
				var updateModel = function(data){
						$scope.$apply(function(){
							$scope.repositoryModel.name = data.name;
							$scope.repositoryModel.id = data.id;
							$scope.repositoryModel.path = data.path;
							$scope.repositoryModel.children.folders = data.children.folders;
							$scope.repositoryModel.children.documents = data.children.documents;
						});
					},
					
					/*Push an item onto our navigation stack so we can simulate back navigation through our
					 *folder structure. This requires that navigation to the docViewer page insert the doc id onto
					 *the stack as well as nav to a new folder doing likewise. In otherwords we have to push when
					 *we load a new folder and stay on the repository page, as well as navigating to the document
					 *viewer page to view an image or resource.
					 */
					pushNavStack=function(id){
						$scope.directoryNavStack.push(id);
					}
					
					popNavStack = function(popOnDuplicateState){
						var targetFolder;	
						
						/*Pop top of the stack as it represents where we are now*/
						$scope.directoryNavStack.pop();
						
						/*get top of stack as it now represents where we wish to go*/
						targetFolder=$scope.directoryNavStack[$scope.directoryNavStack.length-1];
						
						/*if top of the stack is falsey, nav back to #mainPage*/
						if(!targetFolder){
							utilService.navigateTo($rootScope,"#mainPage");
							return;
						}
						
						/*we now have a folderID so load it.*/
						$scope.getFolder(targetFolder.id,false);
					};
				$scope.errorModel=new errorModel();
				$scope.directoryNavStack=[];
				$scope.pushNavStack=pushNavStack;
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
						pushNavStack({"id" : folderId});
					}
					
					$scope.isAjaxLoading = true;
					workflowService.getRepositoryFolder(folderId)
					.then(function(data){
						updateModel(data);
					})
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage=$rootScope.errorText.folder;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(function(){
						$scope.$apply(function(){
							$scope.isAjaxLoading = false;
						});			
					});
				};
				
				$scope.init = function(){
					var deferred = $q.defer();
					
					workflowService.getRepositoryFolder()
					.then(function(data){
						updateModel(data);
						$scope.directoryNavStack.push({"id" : ""});	
					})
					.then(deferred.resolve)
					.catch(deferred.reject);
					
					return deferred.promise;
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					var success,fail;
					
					if(edata.pageTarget != "repositoryRootPage"){return;}

					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					if($scope.directoryNavStack.length > 0){
						$scope.popNavStack(false);
						edata.ui.bCDeferred.resolve();
						return;
					}
					
					$scope.init()
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage=$rootScope.errorText.folder;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			"documentViewerCtrl" : function($scope, $rootScope,$timeout,$q, $sce, workflowService, utilService, il18nService){
				
				$scope.uiText = {
					"content"      : il18nService.getProperty("mobile.documentviewer.tabs.content"),
					"details"      : il18nService.getProperty("mobile.documentviewer.tabs.details"),
					"unsupported"  : il18nService.getProperty("mobile.documentviewer.content.messge.unsupported"),
					"download"     : il18nService.getProperty("mobile.documentviewer.content.button.download"),
					"filetype"     : il18nService.getProperty("mobile.documentviewer.details.listview.filetype"),
					"filesize"     : il18nService.getProperty("mobile.documentviewer.details.listview.filesize"),
					"author"       : il18nService.getProperty("mobile.documentviewer.details.listview.author"),
					"created"      : il18nService.getProperty("mobile.documentviewer.details.listview.created"),
					"lastmodified" : il18nService.getProperty("mobile.documentviewer.details.listview.lastmodified")
				};
				
				$scope.documentViewerModel = new documentViewerModel();
				$scope.errorModel=new errorModel();
				
				$scope.init = function(docId){
					var deferred=$q.defer();
					
					workflowService.getDocument(docId)
					.then(function(data){
						$scope.$apply(function(){
							$scope.documentViewerModel=data;
							$scope.documentViewerModel.downloadUrl = workflowService.getDocumentUrl(data.downloadToken);
							$scope.activeSubView="content";
						});
					})
					.then(deferred.resolve)
					.catch(deferred.reject);
					
					return deferred.promise;
				}
				
				$scope.isImageType = utilService.isImageType;
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "documentViewerPage"){return;}
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.init(edata.data.id)
					.catch(function(err){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.recordretrieval;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
					
				});
			},
			
			"unauthorizedCtrl" : function($scope,il18nService){
				$scope.uiText = {
						"header"  : il18nService.getProperty("mobile.unauthorized.header.text"),
						"content" : il18nService.getProperty("mobile.unauthorized.message.text")
				}
			},
			
			"profileCtrl" : function($scope,$rootScope,$q,$timeout,utilService,il18nService,workflowService){
				
				$scope.uiText = {
					"header" : il18nService.getProperty('mobile.profile.header.text'), 	
					"cancel" : il18nService.getProperty('mobile.profile.btn.cancel'), 
					"save" : il18nService.getProperty('mobile.profile.btn.save'), 
					"changepword" : il18nService.getProperty('mobile.profile.label.changepassword')
				}
				
				$scope.errorModel = new errorModel();
				
				$scope.init = function(){
					var deferred = $q.defer();
					deferred.resolve(); 
					return deferred.promise;
				};
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "profilePage"){return;}
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					
					$rootScope.appData.activePage = edata.pageTarget;
					
					$scope.init()
					.catch(function(err){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(edata.ui.bCDeferred.resolve);
				});	
				
			},
			
			"settingsCtrl" : function($scope,$rootScope,$q,$timeout,il18nService,utilService,workflowService){
				
				$scope.test = "Hello From Settings Ctrl"; 
				
				$scope.uiText = {
					"header"      : 	il18nService.getProperty('mobile.settings.header.text'),
					"action"      : 	il18nService.getProperty('mobile.settings.action.label'),
					"whencancel"  : 	il18nService.getProperty('mobile.settings.action.whencancel'),
					"worklist"    : 	il18nService.getProperty('mobile.settings.action.whencancel.option.worklist'),
					"participant" : 	il18nService.getProperty('mobile.settings.action.whencancel.option.participant'),
					"lists"       : 	il18nService.getProperty('mobile.settings.lists.label'),
					"maxitems"    : 	il18nService.getProperty('mobile.settings.lists.maxitems'),
					"timestampformat" : 	il18nService.getProperty('mobile.settings.lists.timestampformat'),
					"cancel"      : 	il18nService.getProperty('mobile.settings.btn.cancel'),
					"save"        : 	il18nService.getProperty('mobile.settings.btn.save')
				};
				
				$scope.errorModel = new errorModel();
				$scope.infoModel = new infoModel();
				
				/*Set up our settings model*/
				$scope.settingsModel = new settingsModel();
				
				/*Date formats*/
				$scope.settingsModel.dateFormats.push({
					"id" : 1, 
					"value": il18nService.getProperty('mobile.settings.timestampformat.short')}
				);
				$scope.settingsModel.dateFormats.push({
					"id" : 1, 
					"value": il18nService.getProperty('mobile.settings.timestampformat.long')}
				);
				
				/*Suspend and save otpions*/
				$scope.settingsModel.saveOptions.push({"id" : 1, "value" : $scope.uiText.worklist});
				$scope.settingsModel.saveOptions.push({"id" : 1, "value" : $scope.uiText.participant});
				
				/*ng model we will bind to our selected items in our UI*/
				$scope.selectedSettings ={
						"maxItems"   :0,
						"dateFormat" : {},
						"saveAction" :{}
				}
				
				$scope.init = function(){
					var deferred = $q.defer();
					deferred.resolve(); 
					return deferred.promise;
				};
				
				$scope.saveSettings = function(){
					workflowService.saveMobileSettings($rootScope.appData.user,$scope.selectedSettings)
					.then(function(){
						$scope.$apply(function(){
							$scope.infoModel.hasInfo=true;
							$scope.infoModel.infoMessage = $rootScope.appData.infoText.genericSave;
							$timeout(function(){
								$scope.infoModel.hasInfo=false;
							},$rootScope.appData.barDuration);
						});			
					})
					.catch(function(){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.genericSave;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(function(){
						utilService.navigateTo($rootScope);
					});
				}
				
				$scope.$on("jqm-navigate",function(e,edata){
					
					if(edata.pageTarget != "settingsPage"){return;}
					
					if($rootScope.appData.isAuthorized==false){
						utilService.navigateTo($rootScope,"#unauthorizedPage",{});
						return;
					}
					$rootScope.$apply(function(){
						$rootScope.appData.activePage = edata.pageTarget;
					});
					
					
					$scope.isAjaxLoading=true;
					$scope.init()
					.catch(function(err){
						$scope.$apply(function(){
							$scope.errorModel.hasError=true;
							$scope.errorModel.errorMessage = $rootScope.appData.errorText.pageload;
							$timeout(function(){
								$scope.errorModel.hasError=false;
							},$rootScope.appData.barDuration);
						});
					})
					.finally(function(){
						edata.ui.bCDeferred.resolve();
						$scope.isAjaxLoading=false;
					});
					
				});	
				
			}
			
			
	};
	
	return worklistCtrl;
});