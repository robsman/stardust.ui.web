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
 * Provides an implementation of a folder browser wired to a rest common end point
 * representing our IPP document repository.
 * Initial path can be set using the @sdaRootPath attribute on the directive.
 * If no path is given the rootpath with be the base folder of the document repository.
 * 
 * ATTRIBUTES:
 * -----------------------------------------------------------------------------------
 * @sdaRootPath - Root path to base the folder tree on
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
  
  /************************************************************************
   * REST Folder service for directive
   ************************************************************************/
  function sdFolderService($http,$q,sdUtilService){
    this.$http = $http;
    this.$q = $q;
    this.rootUrl = sdUtilService.getRootUrl();
  };
  
  //Retrieve folders and their immediate substructure based
  //on the relative path passed to the function. All paths
  //are relative to the top level 'folders' directory
  //representing the root level of the document repository.
  sdFolderService.prototype.getFolders = function(path){
    
    var that = this,
    	url,
        deferred = this.$q.defer();

    path = path || "";
    
    url = this.rootUrl + "/services/rest/portal/folders/" + path;
    this.$http.get(url)
    .then(function(res){
    	deferred.resolve(res.data);
    })
    ["catch"](function(err){
    	deferred.reject(err);
    });

    return deferred.promise;
    
  };
  
  //injectable dependencies for our service.
  sdFolderService.$inject=["$http", "$q", "sdUtilService"];
  
  //register service on our module
  mod.service("sdFolderService",sdFolderService);
  
  /************************************************************************
   * Controller function for Directive
   ************************************************************************/
  function sdFolderTreeController(folderService,$q,$scope){
	var that = this;
	
    this.$q = $q;
    this.$scope = $scope;
    this.folderService = folderService;
    this.loadRootFolder($scope.rootPath || "");//loading top level tree structure
    this.folderData = [];
    this.selectedNodes=[];
    
    //Setup multi-select property based on scope attribute
    $scope.allowMultiselect = $scope.allowMultiselect || "";
    $scope.allowMultiselect = $scope.allowMultiselect.toUpperCase();
    this.allowMultiselect = $scope.allowMultiselect==="TRUE";
    
    //Set up api to return to any listeners.
    this.api = {
      getSelectedNodes : function(){
        return that.selectedNodes;
      }
    };
    //invoke our callback function with our api
    that.$scope.onInit({api:that.api});
    
  };
  
  /**
   * Icon callback to handle folders,documents, and selected folders.
   * @param item
   * @returns {String}
   */
  sdFolderTreeController.prototype.iconCallback = function(item){
    var classes = "fa";
    
    if(item.itemType==="document"){
    	classes += " fa-file";
		if(this.selectedNodes.some(function(v){return v.valueItem.uuid === item.uuid;})){
		    classes +=" selected";
		}
    }
    
    if(item.itemType==="folder"){
      classes +=" fa-folder"
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
  sdFolderTreeController.prototype.eventCallback = function(data,e){

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
   * Inital call to load the first level of the tree based on the @sdaRootPath
   * attribute. All levels below this are lazy loaded on node expansion by the
   * user.
   * @param rootPath
   */
  sdFolderTreeController.prototype.loadRootFolder = function(rootPath){
	  
    var that = this,
        root = {};
        
    root.items=[];
    
    this.folderService.getFolders(that.$scope.rootPath)
    .then(function(data){
    	root.name= data.name;
    	root.uuid = data.uuid;
    	root.path = data.path;
    	root.itemType = "folder";
    	root.isLoaded = true;
    	root.items =  (that.normalizeFolderData(data).items);
    	that.folderData.push(root);
    });
  };
  
  /**
   * Takes JSON as returned from our rest-common endpoint and normalizes
   * it to a common structure which our tree driective can recurse over.
   * Adds an itemType property to explicitly identify the node type.
   * Adds a isLoaded property to keep track over whether the node has 
   * loaded its children (on a previous expansion).
   * @param folder
   * @returns
   */
  sdFolderTreeController.prototype.normalizeFolderData = function(folder){
    var normFolder = {items: []},
        tempItem,
        i=0;
    
    for(i=0;i<folder.documents.length;i++){
      tempItem = folder.documents[i];
      tempItem.itemType = "document";
      normFolder.items.push(tempItem);
    }
    
    for(i=0;i<folder.folders.length;i++){
      tempItem = folder.folders[i];
      tempItem.itemType = "folder";
      tempItem.isLoaded = false;
      normFolder.items.push(tempItem);
    }
    
    return normFolder;
    
  };
  
  //Dependencies we will inject into the controller.
  sdFolderTreeController.$inject = ["sdFolderService","$q","$scope"];
  
  mod.controller("sdFolderTreeController",sdFolderTreeController);
  
  mod.directive("sdFolderTree",function(){
    return {
      scope: {
        rootPath : "@sdaRootPath",
        allowMultiselect : "@sdaMultiselect",
        onInit : "&sdaOnInit",
        eventCallback: "&sdaEventCallback"
      },
      controller: "sdFolderTreeController",
      controllerAs: "treeCtrl",
      templateUrl : "./plugins/html5-common/scripts/directives/sdFolderTree/sdFolderTree.html"
    };
  });
  
})();