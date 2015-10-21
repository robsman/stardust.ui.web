/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/**
 * Provides an implementation of a tree structure wired the process attachments
 * and specific documents of a given process.
 * Process Oid can be set using the @sdaProcessOid attribute on the directive.
 * @sdaProcessOid is required.
 * 
 * ATTRIBUTES:
 * -----------------------------------------------------------------------------------
 * @sdaProcessOid - Process Oid to request process documents for.
 * @sdaMultiselect - string 'True' || 'False', whether the tree supports multiple node selection.
 * &sdaOnInit - function to passback the tree api to the user (api only supports .getSelectedNodes())
 * &sdaEventCallback - user callback to listen for events on.
 * 
 * Tree-API:
 * -----------------------------------------------------------------------------------
 * Tree Api is passed back to the function the user has provided to the sdaOnInit attribute.
 * API Methods:
 * 		getSelectedNodes : returns the array of nodes currently selected in the tree.
 * 
 * Events:
 * -----------------------------------------------------------------------------------
 * Document related events
 * document-selected-single 
 * document-selected-multi
 * document-deselected
 */

(function(){
  
  var mod = angular.module("bpm-common.directives");
  
  /**
   * Service to interact with rest-common in order to retrieve information
   * regarding a processes documents.
   */
  function processDocumentService($http,$q,sdUtilService){
    this.$http = $http;
    this.$q = $q;
    this.rootUrl = sdUtilService.getBaseUrl();
  }
 
  /**
   * Given a processOid, retrieve the process documents for that process.
   * @param oid
   * @returns
   */
  processDocumentService.prototype.getProcessDocuments = function(oid){
    var deferred = this.$q.defer(),
    	url;
    
    url = this.rootUrl + "services/rest/portal/process-instances/" + oid + "/documents";
    
    this.$http.get(url)
    .then(function(data){
    	deferred.resolve(data.data);
    })
    ["catch"](function(err){
    	deferred.reject(err);
    });

    return deferred.promise;
  };
 
  //Dependencies required by the service (see contructor)
  processDocumentService.$inject = ["$http","$q","sdUtilService"];
  
  //register service with the angular module
  mod.service("processDocumentService",processDocumentService);
 
  /**
   * Controller function we will use within our directive.
   */
  function processDocumentController(processDocumentService,$scope){
    var that = this;
    
    this.processDocService = processDocumentService;
    this.loadProcessDocuments($scope.oid);
    this.$scope = $scope;
    this.selectedNodes = [];
    
     //Setup multiselect property based on scope attribute
    $scope.allowMultiselect = $scope.allowMultiselect || "";
    $scope.allowMultiselect = $scope.allowMultiselect.toUpperCase();
    this.allowMultiselect = $scope.allowMultiselect==="TRUE";
    
    //Set up api to return to any listeners.
    this.api = {
      getSelectedNodes : function(){
        return that.selectedNodes;
      }
    };
    that.$scope.onInit({api:that.api});
  };
  
  /**
   * Icon callback to handle folders,documents, and selected folders.
   * @param item
   * @returns {String}
   */
  processDocumentController.prototype.iconCallback = function(item){
   
    var classes = "pi";
    
    if(item.itemType==="document"){
    	classes += " pi-other";
  		if(this.selectedNodes.some(function(v){return v.valueItem.uuid === item.uuid;})){
  		    classes +=" selected";
  		}
    }
    
    //can't select root folders, only documents
    if(item.itemType.indexOf('Root')>-1){
      classes +=" pi-folder";
    }
  
    return classes;
  
  };
  
  /**
   * Handle our tree events here. We only expose a dumbed-down selection of events
   * to the user (as opposed to the normal firehose of sd-tree events).
   * These include...
   * -------------
   * document-selected-single
   * document-selected-multi
   * document-deselected
   * --------------
   *...all events are published via the function the user assigned to the sdaEventCallback attribute.
   * @param data
   * @param e
   */
  processDocumentController.prototype.eventCallback = function(data,e){

    data.deferred.resolve();
    
    var i, 
    	tempNode,
    	docIndex,
        that = this;
        
    this.selectedItem  = data.valueItem;
    this.lastEvent = data.treeEvent;
    docIndex = -1;
    
    //On expansion of a node we need to build out its children which means
    //if the node has not been previously expanded we need to leverage our 
    //service to instantiate its child folders and documents.
    if(data.treeEvent==="node-expand"){
      
      //If item has already been loaded or is a document then return
      if(data.valueItem.isLoaded===true || data.valueItem.itemType==="document"){
        data.deferred.resolve();
        return;
      }
      else{
        that.folderService.getFolders(data.valueItem.path)
        .then(function(children){
          data.valueItem.items =  that.normalizeFolderData(children).items;
          data.valueItem.isLoaded = true;
        });
      }
    }
    
    //Nothing to do on node collapse so just resolve
    else if(data.treeEvent==="node-collapse"){
       data.deferred.resolve();
    }
    
    //For nodeclicks on a document we need to add the node to our selectedNodes collection,
    //unless the node is already present in which case we remove it. Single select
    //unless the ctrl key is depressed in which case we use multi-select logic
    else if (data.treeEvent ==="node-click" && data.valueItem.itemType==="document"){
        
    	//Find existing index (if any) of item clicked
		for(i=0;i<that.selectedNodes.length;i++){
		  tempNode = that.selectedNodes[i];
		  if(tempNode.valueItem.uuid === data.valueItem.uuid){
		    docIndex = i;
		    break;
		  }
		}
		
		//if found on selectedNodes then remove
		if(docIndex > -1){
		  that.selectedNodes.splice(docIndex,1);
		  this.$scope.eventCallback({item:{
			  eventType: "document-deselected",
			  document: data.valueItem
		  }});
		}
		//not on selected nodes but as ctrlKey is pressed we push onto
		//the array without emptying it first (multi-select)
		else if(e.ctrlKey && this.allowMultiselect){
		 that.selectedNodes.push(data);
		 this.$scope.eventCallback({item:{
			  eventType: "document-selected-multi",
			  document: data.valueItem
		  }});
		}
		//regular click, no ctrl key, dump the old array and create new one
		//with only our clicked item of interest.
		else{
		  that.selectedNodes=[];
		  that.selectedNodes.push(data);
		  this.$scope.eventCallback({item:{
			  eventType: "document-selected-single",
			  document: data.valueItem
		  }});
		}
	    
    }
    else{
      this.$scope.eventCallback({item:data});
    }

    data.deferred.resolve();

  };
  
  /**
   * Wrapper function for our service call to retrieve process documents.
   * On success we then massaage the data into a friendly form for our 
   * tree structure.
   * @param oid
   */
  processDocumentController.prototype.loadProcessDocuments = function(oid){
    var that = this;
    this.processDocService.getProcessDocuments(oid)
    .then(function(data){
      that.processDocuments = that.normalizeData(data);
    });
  };
  
  processDocumentController.prototype.normalizeData = function(data){
	  
	  var res =[],
      specRoot, //root item for specific documents
      tempDataPath,     //temp object
      tempDoc,
      procRoot, //root item for process attachments
      i,j;      //iterators
      
	  procRoot = {itemType:'attachmentsRoot',name:'Process Attachments',items:[]};
	  specRoot = {itemType:'specificRoot',name:'Specific Documents',items:[]};  
	  
	  //PROCESS ATTACHMENT PROCESSING
	  for(i=0;i<data.length;i++){
	    tempDataPath = data[i];
	    if(tempDataPath.dataPath.id === "PROCESS_ATTACHMENTS"){
	      for(j=0;j<tempDataPath.documents.length;j++){
	        tempDoc = tempDataPath.documents[j];
	        tempDoc.dataPathId = tempDataPath.dataPath.id;
	        tempDoc.dataPathName = tempDataPath.dataPath.name;
	        tempDoc.itemType = "document";
	        procRoot.items.push(tempDoc);
	      }
	      data.splice(i,1); //Remove Process Attachments from main array
	      break;
	    }
	  }
	  
	  res.push(procRoot);
	  
	  //SPECIFIC DOCUMENT PROCESSING
	  for(i=0;i<data.length;i++){
	    tempDataPath = data[i];
	    for(j=0;j<tempDataPath.documents.length;j++){
	      tempDoc = tempDataPath.documents[j];
	      tempDoc.dataPathId = tempDataPath.dataPath.id;
	      tempDoc.dataPathName = tempDataPath.dataPath.name;
	      tempDoc.itemType = "document";
	      specRoot.items.push(tempDoc);
	    }
	  }
	  
	  res.push(specRoot);
	  
	  return res;
	  
  }
  
  /**
   * RETIRED-----  TODO:Remove once developer has a warm happy feeling.
   * Convert our data as retrieved from the REST end point, into a structure
   * which can be more easily used by our tree directive.
   * @param data
   * @returns
   */
  processDocumentController.prototype.normalizeDataOld=function(data){
    
    var res =[],
        specRoot, //root item for specific documents
        temp,     //temp object
        specItem, //specific document item we will push
        procRoot, //root item for process attachments
        key,      //key for our hashmap iterator
        i;        //index for our numeric iterator
        
    procRoot = {itemType:'attachmentsRoot',name:'Process Attachments',items:[]};
    specRoot = {itemType:'specificRoot',name:'Specific Documents',items:[]};
    
    for(i=0;i< data.PROCESS_ATTACHMENTS.length;i++){
      temp = data.PROCESS_ATTACHMENTS[i];
      data.PROCESS_ATTACHMENTS[i].itemType = "document";
      procRoot.items.push(data.PROCESS_ATTACHMENTS[i]);
    }
    
    res.push(procRoot);
    
    delete data.PROCESS_ATTACHMENTS;
    
    for(key in data){
      temp = data[key];
      if(temp.length >0){
        specItem = {};
        specItem = angular.extend({},temp[0]);
        specItem.itemType = "document";
        specItem.name = key;
        specRoot.items.push(specItem);
      }
    }
    
    res.push(specRoot);
    
    return res;
    
  };
  processDocumentController.$inject=["processDocumentService","$scope"];
  
  mod.controller("processDocumentController",processDocumentController);
  
  mod.directive("sdProcessDocumentTree",["sdUtilService",function(sdUtilService){
    return {
      scope: {
        oid : "@sdaProcessOid",
        allowMultiselect : "@sdaMultiselect",
        onInit : "&sdaOnInit",
        eventCallback: "&sdaEventCallback"
      },
      controller: "processDocumentController",
      controllerAs: "treeCtrl",
      templateUrl : sdUtilService.getBaseUrl()+"plugins/html5-common/scripts/directives/sdProcessDocumentsTree/sdProcessDocumentsTree.html"
    };
  }]);
    
})();