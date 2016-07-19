(function(){
  
  var mod = angular.module("bpm-common.directives");
  
  
  /*Controller section*/
  
  docRepoController.$inject = ["documentRepositoryService", "$timeout", "sdUtilService", 
                               "$scope", "$filter", "sdViewUtilService", "$q",
                               "sdMimeTypeService", "sdI18nService"];

  function docRepoController(documentRepositoryService, $timeout, sdUtilService, $scope, $filter, 
                             sdViewUtilService, $q, sdMimeTypeService, sdI18nService){

    var that = this;
    var virtualRoot;

    this.baseTemplateUrl = sdUtilService.getRootUrl() + '/plugins/html5-common/scripts/directives/sdDocumentRepository/templates/';
    this.name = "Ctrl";
    this.documentService = documentRepositoryService;
    this.sdViewUtilService = sdViewUtilService;
    this.sdUtilService = sdUtilService;
    this.$timeout = $timeout;
    this.$filter = $filter;
    this.$scope = $scope;
    this.$q = $q;
    this.sdMimeTypeService = sdMimeTypeService;
    this.i18n = sdI18nService.getInstance('views-common-messages');
    this.showSearchFilter = $scope.showSearchFilter;
    this.textMap = this.getTextMap(this.i18n);
    this.testDate = new Date("12/24/2005").getTime();
    this.selectedRepo = {};
    this.repositoryProviders =[]; 
    this.selectedMatches = []; //matched from our sdAutocomplete directive
    this.matches = []; //Tree node matches corresponding to our selectedMatches
    this.data=[];
    this.documentRepositoryUrl = documentRepositoryService.documentRoot + "/upload";
    this.documentTypes = [];

    //set up a watch on our selected matches from our autocomplete directive
    $scope.$watchCollection('ctrl.selectedMatches', function(newValue, oldValue) {
      that.matches=[];
      if(newValue.length > 0){
        that.expandNodePath(newValue[0]);
      };
    });


    //Async retrieve document types
    this.documentService.getDocumentTypes()
    .then(function(docTypes){
      docTypes.forEach(function(v){
        that.documentTypes.push(v);
      });
    })
    ["catch"](function(err){
      //TODO:err handling
    });

    //Async retrieve repo providers.
    this.getRepositoryProviders()
    .then(function(providers){
    	
      that.repositoryProviders = providers;
      
      if(providers.length > 0){
        that.boundDialogRepoProvider = providers[0];
      }
      
    });

  }
  
  //Currently this will only work for the full full doc repo view (sdaRootPath="/").
  //It will nto work for any other path.
  docRepoController.prototype.expandNodePath = function(res){

    var expPaths = [],
        pathSegments = [],
        i = 0,
        that = this,
        repoUrn,
        pathAccum = [];

    pathSegments = res.path.split("/").filter(function(v){return !!v});
    for(; i < pathSegments.length  ; i++){
      pathAccum.push("/" + pathSegments.slice(i).join("/"));
    }

    //1:Expand virtual Root, we know it already has children
    this.treeApi.expandNode("VirtualRoot");

    var fx = function(nodeId,childPaths){

      var treeNode = that.treeApi.childNodes[nodeId];

      that.refreshFolder(treeNode.nodeItem)
      .then(function(res){

          that.treeApi.expandNode(treeNode.nodeItem.id);

          //This block handles our Repository Structure
          if(treeNode.nodeItem.nodeType==="Repo"){

            var nextPath = childPaths.splice(0,1)[0];

            that.$timeout(function(){
              var virtualRoot = treeNode.nodeItem.children[0];
              var nextChild;

              //This is a virtualized root folder and already has children initialized
              that.treeApi.expandNode(virtualRoot.id);

              nextChild = virtualRoot.children.filter(function(child){
                var testPath = child.path;
                testPath += (child.nodeType==='folder')?"/":"";
                return nextPath.indexOf(testPath) == 0;
              })[0];
              
              if(nextChild && (nextChild.nodeType==="folder" || nextChild.nodeType==="document")){
                that.$timeout(function(){
                  fx(nextChild.id,childPaths);//recursive call
                },300);
              }

            },0);
          }
          //Else we have a pure folder structure
          else if(treeNode.nodeItem.nodeType==="folder"){
            that.treeApi.expandNode(treeNode.nodeItem.id);
            that.$timeout(function(){

              var nextPath = childPaths.splice(0,1)[0];
              nextPath = treeNode.nodeItem.path + nextPath;

              var nextChild = treeNode.nodeItem.children.filter(function(child){
                var testPath = child.path;
                testPath += (child.nodeType==='folder' && childPaths.length > 0)?"/":"";
                return nextPath.indexOf(testPath ) == 0;
              })[0];

              if(nextChild){
                fx(nextChild.id,childPaths);//set up recursion
              }
              else{
                //there is an assumption here that this block should
                //only be reached if our match is a folder.
                that.matches.push(treeNode.nodeItem);
              }

            },0);
          }
          //Else we have a document
          else if(treeNode.nodeItem.nodeType==='document'){
            //Add to our matched collection
            that.matches.push(treeNode.nodeItem);
          };

      });

    };

    //boot up recursive calls
    fx(res.repositoryId,pathAccum);

    return;
  };
  
  /**
   * Given an object return the keys representing the properties of that object as an array.
   * This is designed to allow ngRepeat to sort hash maps when iterating over a property collection.
   * @param obj
   * @returns
   */
  docRepoController.prototype.getObjectKeys = function(obj){
	  return obj? Object.keys(obj).sort():[];
  }
  
  docRepoController.prototype.getTextMap = function(i18n){

    textMap = {};
    textMap.rename = i18n.translate("views.genericRepositoryView.treeMenuItem.rename");
    textMap["delete"] = i18n.translate("views.genericRepositoryView.treeMenuItem.delete");
    textMap.createSubFolder = i18n.translate("views.genericRepositoryView.treeMenuItem.createSubFolder");
    textMap.createNewTextFile = i18n.translate("views.genericRepositoryView.treeMenuItem.createNewTextFile");
    textMap.uploadFile = i18n.translate("views.genericRepositoryView.treeMenuItem.uploadFile");
    textMap.sendToZip = i18n.translate("views.genericRepositoryView.treeMenuItem.sendToZip");
    textMap.uploadZip = i18n.translate("views.genericRepositoryView.treeMenuItem.uploadZip");
    textMap.refresh = i18n.translate("views.genericRepositoryView.treeMenuItem.refresh");
    textMap.security = i18n.translate("views.genericRepositoryView.treeMenuItem.security");
    textMap.versionHistory = i18n.translate("views.genericRepositoryView.treeMenuItem.versionHistory");
    textMap.download = i18n.translate("views.genericRepositoryView.treeMenuItem.download");
    textMap.newVersion = i18n.translate("views.genericRepositoryView.treeMenuItem.newVersion");
    textMap.refresh = i18n.translate("views.genericRepositoryView.treeMenuItem.refresh");
    textMap.unbindRepo = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.unbindRepo");
    textMap.bindRepo = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.bindRepo");
    textMap.properties = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.properties");
    textMap.makeDefault = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.makeDefault");
    textMap["default"] = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.default");
    textMap.repoRoot = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.root");
    textMap.quickSearch = i18n.translate("views.genericRepositoryView.quickSearch");
    textMap.newFile= i18n.translate("views.genericRepositoryView.newFile.name");
    textMap.newFolder= i18n.translate("views.genericRepositoryView.newFolder.name");
    textMap.bindErrorRepoId = i18n.translate("views.bindRepositoryDialog.repoId.empty");
    textMap.bindErrorProviderId = i18n.translate("views.bindRepositoryDialog.providerId.invalid");
    textMap.bindErrorJndiName = i18n.translate("views.bindRepositoryDialog.jndiName.empty");
    textMap.bindErrorCreate = i18n.translate("views.bindRepositoryDialog.createException");
    textMap.close =  i18n.translate("common.close");
    textMap.confirm =  i18n.translate("common.confirm");
    textMap.confirmDelete = i18n.translate("common.confirmDelete.title");
    textMap.confirmDeleteQuestion = i18n.translate("common.confirmDeleteRes.message.label").split("<br/>")[0];
    textMap.error =  i18n.translate("common.error");
    textMap.fileAlreadyPresent = i18n.translate("views.myDocumentsTreeView.fileUploadDialog.fileAlreadyPresent");
    textMap.bind = {};
    textMap.bind.capabilities = i18n.translate("views.bindRepositoryDialog.provider.capabilities");
    textMap.bind.fullTextSearch = i18n.translate("views.bindRepositoryDialog.provider.capabilities.fullSearch");
    textMap.bind.metaDataSearch = i18n.translate("views.bindRepositoryDialog.provider.capabilities.metaDataSearch");
    textMap.bind.metaDataStorage = i18n.translate("views.bindRepositoryDialog.provider.capabilities.metaDataStorage");
    textMap.bind.versioning = i18n.translate("views.bindRepositoryDialog.provider.capabilities.versioning");
    textMap.bind.transaction = i18n.translate("views.bindRepositoryDialog.provider.capabilities.transaction");
    textMap.bind.aclPolicy = i18n.translate("views.bindRepositoryDialog.provider.capabilities.aclPolicy");
    textMap.bind.writable = i18n.translate("views.bindRepositoryDialog.provider.capabilities.writable");
    textMap.bind.availableProviders = i18n.translate("views.bindRepositoryDialog.repositorySettings.availableProviders");
    textMap.bind.repositorySettings = i18n.translate("views.bindRepositoryDialog.repositorySettings");
    textMap.bind.id = i18n.translate("views.bindRepositoryDialog.repository.id").replace(":","");
    textMap.bind.name = i18n.translate("views.bindRepositoryDialog.provider.name");
    textMap.bind.provider = i18n.translate("views.bindRepositoryDialog.provider");
    textMap.unbind = {};
    textMap.unbind.confirmation = i18n.translate("common.confirmUnbindRepo.message.label");
    textMap.versionDialog = {};
    textMap.versionDialog.versionNo = i18n.translate("views.documentView.documentVersion.versionNo");
    textMap.versionDialog.documentName = i18n.translate("views.documentPanelView.documentPropertiesPanel.DocumentName");
    textMap.versionDialog.author = i18n.translate("views.documentView.properties.author.label");
    textMap.versionDialog.modifiedDate = i18n.translate("views.documentView.documentVersion.modifiedDate");
    textMap.versionDialog.modifiedDate = i18n.translate("views.common.comments.label");

    return textMap;
  };

  /**
   * Callback associated with our sdTree directive which will
   * recieve the trees API upon tree initialization.
   * @param  {api: Tree Api from sdTree directive}
   * @return {void}
   */
  docRepoController.prototype.repoDialogInit = function(api){
    this.uploadDialogAPI = api;
  };


  docRepoController.prototype.getRepositoryProviders = function(){
    return this.documentService.getRepositoryProviders();
  };

  docRepoController.prototype.openDocumentView = function(docId){
    var params = {"documentId" : docId};
    var viewKey = 'documentOID=' + encodeURIComponent(docId);
    viewKey = window.btoa(viewKey);

    this.sdViewUtilService.openView("documentView",viewKey,params,false);
  };

  docRepoController.prototype.treeifyChildren = function(root){

    root.documents.forEach(function(elem){
      elem.nodeType = "document";
      elem.id=elem.uuid;
    });

    root.folders.forEach(function(elem){
      elem.nodeType = "folder";
      elem.id=elem.uuid;
      elem.children=[];
    });
    return root.folders.concat(root.documents);
  };

  docRepoController.prototype.onTreeInit = function(api){
    this.treeApi = api;
  };
  
  docRepoController.prototype.refreshRepositories = function(){

    var that = this;
      this.$timeout(function(){
        that.$scope.rootPath = "OP_REFRESH";
        that.$timeout(function(){
          that.$scope.rootPath = "/";
          that.$timeout(function(){
            that.treeApi.expandNode("VirtualRoot");
          },250);
        },0)
      },0);
      
  };

  docRepoController.prototype.getDocumentClass = function(doc){

    var docType = doc.contentType;

    return this.sdMimeTypeService.getIcon(docType);  
   
  };

  docRepoController.prototype.iconCallback = function(data,e){
    
    var classes=["fa"],
        srcScope;

    if(this.matches.indexOf(data) > -1){
      classes.push("match");
    }

    if(data.nodeType=="folder" || data.nodeType == "repoFolderRoot"){

      srcScope = this.treeApi.childNodes[data.id];
      if(srcScope.isVisible===true){
        classes.push("pi-folder-open")
      }
      else{
        classes.push("pi-folder");
      }
      
    }
    else if(data.nodeType=="document"){
      classes.push(this.getDocumentClass(data));
    }
    else{
       classes.push("pi-database");
    }
    
    return classes.join(" ");
    
  };
  
  docRepoController.prototype.sortByName = function(a,b){

    var name1 = (a.name)?a.name.toUpperCase():a.path,
        name2 = (b.name)?b.name.toUpperCase():b.path;

    if(a.nodeType !== b.nodeType){
      if(a.nodeType==="folder"){return -1;}
      else{return 1;}
    }
    else if(name1===name2){return 0;}
    else if(name1 < name2){return -1;}
    else{return 1;}

  };

  docRepoController.prototype.eventCallback = function(data,e){
    
    var that = this;


    this.$scope.eventHook({"data" : data, "e" : e});

    switch(data.treeEvent){
      case "node-expand":
        if(! data.valueItem.isInitialized && 
        (data.valueItem.nodeType ==='folder' || data.valueItem.nodeType ==='repoFolderRoot')){

          var resourceId;
          resourceId = (data.valueItem.nodeType ==='repoFolderRoot')?data.valueItem.path : data.valueItem.id;

          this.documentService.getChildren(resourceId)
          .then(function(root){
            var children = that.treeifyChildren(root);
            data.valueItem.children = children.sort(that.sortByName);
            data.valueItem.isInitialized=true;
            data.deferred.resolve();
          });

        }
        else if(! data.valueItem.isInitialized && data.valueItem.nodeType ==='Repo'){

          this.documentService.getRepositoryRootFolder(data.valueItem.id)
          .then(function(root){
            var rootFolder = root.data;
            rootFolder.nodeType="folder";
            rootFolder.id = rootFolder.uuid;
            rootFolder.name = "Root";
            rootFolder.isInitialized = true;
            rootFolder.children = that.treeifyChildren(rootFolder).sort(that.sortByName);
            data.valueItem.isInitialized=true;
            data.valueItem.children=[rootFolder];
            data.deferred.resolve();
          });

        }
        else{
          data.deferred.resolve();
        }
        break;
      case "node-delete" :
         if(data.valueItem.nodeType==="document"){
          this.deleteDocument(data);
        }
        else if(data.valueItem.nodeType==="folder"){
           this.deleteFolder(data);
        }
        break;
      case "node-rename-commit":
        if(angular.isString(data.newValue) && data.newValue.length > 0){
          if(data.valueItem.nodeType==="document"){
            this.renameDocument(data.valueItem.id,data.newValue,data.valueItem);
          }
          else if(data.valueItem.nodeType==="folder"){
             this.renameFolder(data.valueItem.id,data.newValue,data.valueItem);
          }
          data.deferred.resolve();
        }
        else{
          data.deferred.reject();
        }
        break;
      case "node-click" :
        if(data.valueItem.nodeType==="document"){
          this.openDocumentView(data.valueItem.id);
        }
        break;
      case "node-rename" :
        data.deferred.resolve();
        break;
      case "node-dragend" :
        console.log("dragend");
        console.log(data);
        break;
      case "node-drop" :
        if(data.dropData && data.dropData.nodeType==='document'){
          this.moveDocument(data);
        }
        break;
      case "node-native-drop" :
        if(e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length >0){
          this.uploadFile(data.valueItem, e.dataTransfer.files);
        }
        break;
      case "menu-bind" :
        this.openBindRepoDialog();
        break;
      case "menu-setDefault" :
        this.setRepositoryAsDefault(data.valueItem);
        break;
      case "menu-unbindRepo":
        this.unbindRepository(data);
        break;
      case "menu-repoProperties" :
        this.selectedRepo = data.valueItem;
        this.openRepoPropertyDialog();
        break;
      case "menu-createSubFolder" :
        this.createSubFolder(data.valueItem);
        break;
      case "menu-createFile" :
        this.createDocument(data.valueItem);
        break;
      case "menu-versionFileHistory": 
        this.openFileVersionHistoryDialog(data.valueItem);
        break;
      case "menu-refreshFile":
        this.refreshDocument(data.valueItem);
        break;
      case "menu-refreshFolder":
        this.refreshFolder(data.valueItem);
        break;
      case "menu-uploadNewFileVersion":
        this.uploadNewFileVersion(data.valueItem);
        break;
      case "menu-downloadFile" :
        this.downloadFile(data.valueItem);
        break;
      case "menu-downloadFolder" :
        this.downloadFolder(data.valueItem);
        break;
      case "menu-uploadZipFile":
        this.uploadAndExplode(data.valueItem);
        break;
      case "menu-uploadFile" :
        this.uploadFile(data.valueItem);
        break;
      case "menu-securityFile" :
        this.openFileSecuritySettingsDialog(data.valueItem);
        break;
      case "menu-securityFolder" :
        this.openFolderSecuritySettingsDialog(data.valueItem);
        break;
      default :
        data.deferred.resolve();
    }
    
    this.lastEvent = data;
    console.log(data);
  };
  
  docRepoController.prototype.menuCallback = function(menuData){

    var menuItems=[];

    //Virtual root at top level when repositories are present
    if(menuData.item.nodeType=="RootRepo"){
      menuItems.push("(bind,"+ this.textMap.bindRepo +")");
    }
    //Repository nodes
    else if(menuData.item.nodeType=="Repo"){

      //TODO:double check this logic
      if(menuData.item.isDefualt===false){
        menuItems.push("(setDefault," + this.textMap.makeDefault + ")");
        //cant undbind default repo
        menuItems.push("(unbindRepo, " + this.textMap.unbindRepo + ")");
      }

      menuItems.push("(repoProperties, " + this.textMap.properties + ")");
      
    }
    //Virtual Root folders directly below the repository level
    else if (menuData.item.path==="/"){
      menuItems.push("(createSubFolder," + this.textMap.createSubFolder + ")");
      menuItems.push("(createFile," + this.textMap.createNewTextFile + ")");
      menuItems.push("(uploadFile," + this.textMap.uploadFile + ")");
      menuItems.push("(uploadZipFile," + this.textMap.uploadZip + ")");
      menuItems.push("(downloadFolder," + this.textMap.sendToZip + ")");
      menuItems.push("(refreshFolder," + this.textMap.refresh + ")");
      menuItems.push("(securityFolder," + this.textMap.security  + ")");
    }
    else if(menuData.item.nodeType=="folder"){

      menuItems.push("(rename," + this.textMap.rename + ")");
      menuItems.push("(delete," + this.textMap.delete + ")");
      menuItems.push("(createSubFolder," + this.textMap.createSubFolder + ")");
      menuItems.push("(createFile," + this.textMap.createNewTextFile + ")");
      menuItems.push("(uploadFile," + this.textMap.uploadFile + ")");
      menuItems.push("(uploadZipFile," + this.textMap.uploadZip + ")");
      menuItems.push("(downloadFolder," + this.textMap.sendToZip + ")");
      menuItems.push("(refreshFolder," + this.textMap.refresh + ")");
      menuItems.push("(securityFolder," + this.textMap.security  + ")");

    }
    else if(menuData.item.nodeType==="repoFolderRoot"){

      menuItems.push("(createSubFolder," + this.textMap.createSubFolder + ")");
      menuItems.push("(createFile," + this.textMap.createNewTextFile + ")");
      menuItems.push("(uploadFile," + this.textMap.uploadFile + ")");
      menuItems.push("(refreshFolder," + this.textMap.refresh + ")");
      menuItems.push("(securityFolder," + this.textMap.security  + ")");

    }
    else if(menuData.item.nodeType=="document"){

      menuItems.push("(rename," + this.textMap.rename + ")");
      menuItems.push("(delete," + this.textMap.delete + ")");
      menuItems.push("(versionFileHistory," + this.textMap.versionHistory  + ")");
      menuItems.push("(downloadFile," + this.textMap.download + ")");
      menuItems.push("(uploadNewFileVersion," + this.textMap.newVersion + ")");
      menuItems.push("(refreshFile," + this.textMap.refresh + ")");
      menuItems.push("(securityFile," + this.textMap.security + ")");

    }

    if(this.$scope.useMenuHook===true){
      menuItems = this.$scope.menuHook({"menuItems" : menuItems,"treeNode" : menuData.item});
    }

    menuData.deferred.resolve(menuItems.toString());
  };
  
  docRepoController.prototype.isLeaf = function(nodeItem){
    return nodeItem.nodeType==='document';
  };

  docRepoController.prototype.moveDocument = function(dropEvt){

    var doc = dropEvt.dropData;
    var newPath = dropEvt.valueItem.path;
    var that = this;
    var srcNode;

    //retrieve original node
    srcNode = this.treeApi.childNodes[dropEvt.valueItem.id];

    //1: Get target folders children from server so we can check
    //   for file name collisions (dont trust stale client collection).
    this.documentService.getChildren(dropEvt.valueItem.id)
    .then(function(children){

      var hasCollision = children.documents.some(function(childDoc){
        return doc.name === childDoc.name;
      });

       //open confirmation dialog
      if(hasCollision===true){
       that.errorMessage=that.textMap.fileAlreadyPresent;
       that.errorDialog.open();
      }
      //safe to move with revision.
      else{
        //TODO: We dont seem to have a move endpoint on the server so
        //for now we will do a PUT to the /copy endpoint.
        return that.documentService.moveDocument(doc.id, newPath, true);
      };

    })
    .then(function(res){

      var destNode = that.treeApi.childNodes[dropEvt.nodeId];
      var srcParentFolder = that.treeApi.getParentItem(srcNode.nodeId);

      srcParentFolder.children.splice(srcParentFolder.children.indexOf(srcParentFolder),1);

      that.refreshFolder(destNode.nodeItem)
      .then(function(){
        that.treeApi.childNodes[destNode.nodeId].isVisible = true;
        that.treeApi.childNodes[srcParentFolder.nodeId].isVisible = true;
      });
      
    })
    ["catch"](function(err){
      that.errorMessage=that.textMap.error;
      that.errorDialog.open();
    })
    ["finally"](function(){
      dropEvt.deferred.resolve();
    });

  };

  docRepoController.prototype.uploadAndExplode = function(folder){
    
    var that = this;
    var treeFolder;
    var repoId;

    //property tied to the upload dialog directive must be updated
    repoId = folder.uuid.split("}{")[0] + "}";
    this.selectedFolderPath = repoId + "/" + folder.path;

    //supplying an ID here (which is tied the dialog via an attribute binding)
    //will singal the dialog that this is a new file version upload rather than
    //a new file upload. (Unless explode and upload is true in which case the new
    //file version operation is ignored)
    this.documentVersionTarget = folder.uuid;

    //Supplying true here signals the docRepo dialog that we are actually uploading
    //a compressed zip which will then be exploded into its internal file/folder structure
    //within the document repository
    this.explodeUpload = true;

    //now open dialog and wait for folder to explode.
    that.uploadDialogAPI.open()
    .then(function(files){
      that.refreshFolder(folder);
    })
    ["catch"](function(err){
      //Todo: handle error
    })
    ["finally"](function(){
      that.documentVersionTarget=null;
      that.explodeUpload = false;
    });

  };

  docRepoController.prototype.downloadFolder = function(folder){
    this.sdUtilService.downloadFolder(folder.uuid);
  };

  docRepoController.prototype.downloadFile = function(doc){
    this.sdUtilService.downloadDocument(doc.uuid,doc.name);
  };

  /*Refresh a folders children by faking a treeNode event*/
  docRepoController.prototype.refreshFolder = function(treeNode){
    var that = this;
    var data;
    var deferred = this.$q.defer();
    treeNode.isInitialized = false;

    data = {
      "treeEvent" : "node-expand",
      "valueItem" : treeNode,
      "deferred" : deferred
    };

    this.eventCallback(data,{});
    return deferred.promise;
  };

  docRepoController.prototype.recursiveTreeNodeFactory = function(nodeScope){
	  var template;

	  template ='<li sd-tree-node ng-repeat="child in child.children" \
		               sda-menu-items="(,)" \
					         sda-node-id="child.id" \
                   sda-droppable-expr="child.nodeType==\'folder\'" \
                   sda-draggable-expr="child.nodeType==\'document\'" \
					         sda-is-leaf="ctrl.isLeaf(child)" \
		   			       sda-lazy-compile="true" \
					         sda-label="child.name"> \
    					 <ul> \
    						 <li sd-tree-curse-fx></li> \
    					 </ul> \
    				 </li>';
	  
	  return template;
  };
  
  docRepoController.prototype.closeFileVersionHistoryDialog = function(){
    this.fileVersionHistoryDialog.close();
  }

  docRepoController.prototype.openFolderSecuritySettingsDialog = function(folderItem){

    var that = this;

    this.documentService.getFolderPolicy(folderItem.id)
    .then(function(res){
      that.activeFolder= folderItem;
      that.activeFolder.policy = res.data;
      return that.folderSecurityDialog.open();
    });


  };
  
  docRepoController.prototype.openFileSecuritySettingsDialog = function(documentItem){

    var that = this;

    this.documentService.getDocumentPolicy(documentItem.id)
    .then(function(res){
      that.activeDocument = documentItem;
      that.activeDocument.policy = res.data;
      return that.fileSecurityDialog.open();
    });

  };
  
  docRepoController.prototype.uploadFile = function(targetFolder,stagedFiles){
    var that = this;
    var treeFolder;
    var repoId;
    var filteredFiles=[];

    treeFolder = that.treeApi.childNodes[targetFolder.id];

    //reset state as this should be null unless we are uploading a new
    //version of an existing document.
    this.documentVersionTarget=null;

    //property tied to the upload dialog directive must be updated
    repoId = targetFolder.uuid.split("}{")[0] + "}";
    this.selectedFolderPath = repoId + "/" + targetFolder.path;

    //If this folder hasnt been expanded/loaded then we will need to do that
    //so that when the upload dialog returns the folder will have its proper contents.
    //We will do this out of band as we dont wont to delay the dialog and we count
    //on the folder loading before the user is finished uploading.
    if(!treeFolder.isInitialized){
      this.loadFolderChildren(targetFolder.id).then(function(){
        treeFolder.isInitialized=true;
      });
    }

    //now open dialog and wait for succesful files to be resolved.
    that.uploadDialogAPI.open(stagedFiles)
    .then(function(files){
      
      files.forEach(function(file){

        var hasDuplicate;

        file.nodeType = "document";
        file.id = file.uuid;

        //Dont push if we have a duplicate already present e.g. an upload where 
        //the user handled file collisions by creating a new version
        hasDuplicate = targetFolder.children.some(function(child){
          return child.id=== file.id;
        });

        if(!hasDuplicate){
          targetFolder.children.push(file);
        }

        //always make sure node is expanded
        //treeFolder.isVisible = true;
        that.treeApi.expandNode(targetFolder.id);

      });
    });

  };
  
  docRepoController.prototype.loadFolderChildren = function(folderId){

    var that = this;
    var targetNode;
    var children;
    var deferred = this.$q.defer();

    targetNode = that.treeApi.childNodes[folderId];
    
    this.documentService.getChildren(folderId)
    .then(function(root){
      children = that.treeifyChildren(root);
      targetNode.nodeItem.children = children.sort(that.sortByName);
      deferred.resolve();
    });

    return deferred.promise;

  };

  docRepoController.prototype.uploadNewFileVersion = function(file){
    
    var that = this;
    var treeFolder;

    //property tied to the upload dialog directive must be updated
    this.selectedFolderPath = file.path;

    //supplying an ID here (which is tied the dialog via an attribute binding)
    //will singal the dialog that this is a new file version upload rather than
    //a new file upload.
    this.documentVersionTarget = file.uuid;

    //now open dialog and wait for succesful files to be resolved.
    that.uploadDialogAPI.open()
    .then(function(files){
      
        //Todo remove old file version and add new file version
    })
    ["catch"](function(err){
      //Todo: handle error
    })
    ["finally"](function(){
      that.documentVersionTarget=null;
    });

  };
  
  docRepoController.prototype.refreshDocument = function(documentNode){
    var that = this;
    this.documentService.getDocument(documentNode.id)
    .then(function(refreshedDoc){

      var parentItem = that.treeApi.getParentItem(documentNode.id);
      var index = parentItem.children.indexOf(documentNode);

      parentItem.children.splice(index,1);

      refreshedDoc.nodeType = "document";
      refreshedDoc.id = refreshedDoc.uuid;

      parentItem.children.push(refreshedDoc);

    });
  }
  
  docRepoController.prototype.openFileVersionHistoryDialog = function(doc){
    this.versionHistoryDocId = doc.id;
    this.showVersionHistoryDialog = true;
  };
  
  docRepoController.prototype.createSubFolder = function(parentFolderNode){

    var that = this;
    var name = "New Folder"
    var dateTime = new Date();
    var name = this.$filter('date')(new Date(), 'yyyy-MM-dd HH-mm-ss');
    var parentFolderId;
    var newFolderId;
    
    name = this.textMap.newFolder + " " + name;
    parentFolderId = (parentFolderNode.nodeType=="folder")?parentFolderNode.id:parentFolderNode.path;

    this.documentService.createFolder(parentFolderId,name)
    .then(function(newFolder){
        newFolderId = newFolder.uuid;
        return that.refreshFolder(parentFolderNode);
    })
    .then(function(){
        that.treeApi.expandNode(parentFolderNode.id);
        that.$timeout(function(){
          that.treeApi.editNode(newFolderId);
        },0);
    });

  };
  
  docRepoController.prototype.createDocument = function(parentFolderNode){

    var that = this;
    var name = this.$filter('date')(new Date(), 'yyyy-MM-dd HH-mm-ss') + ".txt";
    var parentPath;
    var repoId;

    repoId= parentFolderNode.uuid.split("}{")[0] + "}";
    parentPath = repoId + "/" + parentFolderNode.path;
    name = this.textMap.newFile + " " + name;

    this.documentService.createDocument(parentPath/*parentFolderNode.path*/,name)
    .then(function(newDocument){

      newDocument.nodeType = "document";
      newDocument.id = newDocument.uuid;

      parentFolderNode.children.push(newDocument);
      that.treeApi.expandNode(parentFolderNode.id);

      //allow for angular to complete the digest which will consume our
      //new child node and instance it into our tree. Otherwise the api
      //function will be looking for a node which doesnt yet exist.
      that.$timeout(function(){
        that.treeApi.editNode(newDocument.id);
      },0);

    });
  };
  
  docRepoController.prototype.deleteFolder = function(treeNode){
    var that = this;

    //We will create our handler functions on the fly so as to avoid 
    //scoping all our data onto our controller. Ideally the dialog implementation
    //should be promise based with the open returning a promise which is rejected
    //or resolved based on user action. 
    this.onDeleteDocumentDialogConfirm = function(res){
      if(res===true){
        this.documentService.deleteFolder(treeNode.valueItem.id)
        .then(function(name){
          var parentItem = that.treeApi.getParentItem(treeNode.valueItem.id);
          var index = parentItem.children.indexOf(treeNode.valueItem);
          parentItem.children.splice(index,1);
        })
        ["catch"](function(err){
          //TODO: err handling 
        })
        ["finally"](function(){
          treeNode.deferred.resolve();
        });
      }
      else{
        treeNode.deferred.resolve();
      };
    };

    this.onDeleteDocumentDialogClose = function(res){
       treeNode.deferred.reject();
    };

    this.openDeleteDocumentDialog();


  };
  
  docRepoController.prototype.deleteDocument = function(treeNode){

    var that = this;

    //We will create our handler functions on the fly so as to avoid 
    //scoping all our data onto our controller. Ideally the dialog implementation
    //should be promise based with the open returning a promise which is rejected
    //or resolved based on user action. 
    this.onDeleteDocumentDialogConfirm = function(res){
      if(res===true){
        that.documentService.deleteDocument(treeNode.valueItem.id)
        .then(function(name){

          var parentItem = that.treeApi.getParentItem(treeNode.valueItem.id);
          var index = parentItem.children.indexOf(treeNode.valueItem);
          var data = {};

          parentItem.children.splice(index,1);

          //signal any listeners that a file was deleted.
          data.treeEvent = "node-delete-confirmation";
          data.valueItem = treeNode.valueItem;
          that.$scope.eventHook({"data" : data, "e" : {}});

        })
        ["catch"](function(err){
          //TODO: err handling 
        })
        ["finally"](function(){
          treeNode.deferred.resolve();
        });
      }
      else{
        treeNode.deferred.resolve();
      };
    };

    this.onDeleteDocumentDialogClose = function(res){
       treeNode.deferred.reject();
    };

    this.openDeleteDocumentDialog();
  };
  
  docRepoController.prototype.renameFolder = function(folderId,newName,nodeItem){
    var that = this;
    this.documentService.renameFolder(folderId,newName)
    .then(function(name){
      var newPath =  nodeItem.path.replace(nodeItem.path.match("[^/]+$")[0],name);
      nodeItem.path = newPath;
      nodeItem.name = name;
    });
  };
  
  docRepoController.prototype.renameDocument = function(docId,newName,nodeItem){
    var that = this;

    if(newName===nodeItem.name){
      return;
    }

    this.documentService.renameDocument(docId,newName,nodeItem.contentType)
    .then(function(doc){
      nodeItem.name = doc.name;
      nodeItem.contentType = doc.contentType;
    });
  };
  
  docRepoController.prototype.unbindRepository = function(treeEvent){

    var that = this;
    this.dynamicUnbindLabel = this.textMap.unbind.confirmation.replace("{0}",treeEvent.valueItem.id);

    this.onUnbindDialogConfirm = function(res){
      if(res===true){
        that.documentService.unbindRepository(treeEvent.valueItem.id)
        .then(function(){
          var parentItem = that.treeApi.getParentItem(treeEvent.valueItem.id);
          var index = parentItem.children.indexOf(treeEvent.valueItem);
          parentItem.children.splice(index,1);
        });
      }
    }

    this.unbindRepoDialog.open();
  
  };
  
  docRepoController.prototype.setRepositoryAsDefault = function(repo){

    var that = this,
        tempRepo;

    this.documentService.makeRepositoryDefault(repo.id)
    .then(function(){
      repo.isDefualt=true;
      repo.labelModifier = " (" + that.textMap.default + ")";
      that.repositories.forEach(function(id){
        tempRepo = that.treeApi.childNodes[id].nodeItem;
        if(tempRepo.id !== repo.id){
          tempRepo.isDefualt = false;
          tempRepo.labelModifier = "";
        }
      })
    })
    ["catch"](function(err){
      alert("Error occurred setting defualt repository.");
    });

  };
  
  docRepoController.prototype.openRepoPropertyDialog = function(){
    this.repoPropertyDialog.open();
  };
  
  docRepoController.prototype.openDeleteDocumentDialog = function(){
    this.deleteDocumentDialog.open();
  };

  docRepoController.prototype.openBindRepoDialog = function(){
    this.bindRepoDialog.open();
  };
  
  docRepoController.prototype.closeBindRepoDialog = function(){
    this.bindRepoDialog.close();
  };

  docRepoController.prototype.onBindDialogConfirm = function(res){
    var that = this;
    var jndiName = that.boundDialogRepoProvider.jndiName;
    var id = that.boundDialogRepoProvider.beanId;
    var providerId = that.boundDialogRepoProvider.id;
    
    if(res===true){

      if( id && providerId){

        that.documentService.bindRepository(providerId,id,that.boundDialogRepoProvider.attributesMap)
        .then(function(boundRepo){
          that.bindRepoDialog.close();
          //refresh tree as we need the uuid etc.
          that.refreshRepositories();
          
        })
        ["catch"](function(err){
          that.errorMessage=that.textMap.bindErrorCreate;
          that.errorDialog.open();
        });

      }
      else{
        this.errorMessage = "";
        if(!jndiName){this.errorMessage = this.textMap.bindErrorJndiName;}
        else if(!providerId){this.errorMessage = this.textMap.bindErrorProviderId;}
        else if(!id){this.errorMessage = this.textMap.bindErrorRepoId;}
        this.errorDialog.open();
      }

    };
    
  };

  docRepoController.prototype.onBindDialogOpen = function(res){
    //always reset beanId
    this.boundDialogRepoProvider.beanId = "";
  };
  
  docRepoController.prototype.getMatches = function(matchVal){
    var that = this;
    if(matchVal.length > 2){
      this.documentService.searchRepository(matchVal,that.repositories)
      .then(function(res){

        var docs = [],
            folders = [];

        if(res.documents.list){

          docs = res.documents.list.map(function(doc){
            return {
              "uuid" : doc.uuid,
              "name" : doc.name,
              "repositoryId" : doc.repositoryId,
              "path" : doc.path,
              "type" : "document",
              "contentType" : doc.contentType
            }
          });

        };

        if(res.folders.list){

          folders = res.folders.list.map(function(doc){
            return {
              "uuid" : doc.uuid,
              "name" : doc.name,
              "repositoryId" : doc.repositoryId,
              "path" : doc.path,
              "type" : "folder",
              "contentType" : ""
            }
          });

        };

        that.searchMatches = docs.concat(folders);

      });

    }

  };

  docRepoController.prototype.searchItemIcon = function(item,index){

    if(item.type==="folder"){
      return "pi pi-fw pi-lg pi-folder"
    }
    else{
      return this.sdMimeTypeService.getIcon("todo/docType");  
    }

  };

  /*Controller Section Ends*/
  
  
  /*Linking Function*/
  function lnk($scope,$elem,$attrs){
    var ctrl = $scope.ctrl;
    var paths =[];

    //If user has supplied the directive with a menuHook that indicate that on our
    //scope as we only wish to invoke if we have a function that will return something.
    $scope.useMenuHook = ($attrs.sdaMenuHook)?true:false;

    ctrl.repositories = [];

    $scope.$watch("rootPath",function(v1){

      var key;

      //do nothing if null
      if(v1==="OP_REFRESH"){
        return;
      }

      //If user has not assigned a value or simply given us a "/" then load all repositories
      if(v1 == "/"){
        ctrl.documentService.getRepositories(v1)
        .then(function(data){
          data[0].children.forEach(function(elem){
            elem.nodeType = "Repo";
            elem.labelModifier = (elem.isDefualt===true)? " (" + ctrl.textMap.default + ")" : "";
            elem.children=[];
            ctrl.repositories.push(elem.id);
          });
          ctrl.data = data;
        });
      }
      //user has given us a folder path as our root and we are loading an actual root folder and its children.
      else if(angular.isString(v1) || angular.isArray(v1)){

        if(angular.isString(v1)){
          paths = v1.split(",");
        }

        paths.forEach(function(path){

          ctrl.documentService.getChildren(path)
          .then(function(data){

            var children  = ctrl.treeifyChildren(data);
            var parent = {
              "uuid" : data.uuid,
              "name" : data.name, 
              "path" : data.path, 
              "hasChildren" : true,
              "nodeType" : "folder",
              "id" : data.uuid
            }

            parent.children = children.sort(ctrl.sortByName);
            ctrl.data.push(parent);

          });//then end

        });//foreach end

      }
      //We have a hash map whose key will be the path and value will be the name for the node.
      else if(angular.isObject(v1)){
        var v1Keys = Object.keys(v1).sort();
        v1Keys.forEach(function(key){
          ctrl.documentService.getChildren(key)
            .then(function(data){

              var children = ctrl.treeifyChildren(data);
              var parent = {
                "uuid" : data.uuid,
                "name" : v1[key], 
                "path" : data.path, 
                "hasChildren" : true,
                "nodeType" : "folder",
                "id" : data.uuid
              }

              parent.children = children.sort(ctrl.sortByName);
              ctrl.data.push(parent);
              ctrl.data.sort(ctrl.sortByName);
            });
        });
      }
      ctrl.data.sort(ctrl.sortByName);

    });
    
  }
  
  /*Directive Section*/
  docRepo.$inject = ["sdUtilService"];
  
  function docRepo(sdUtilService){
      
      var templateUrl = sdUtilService.getBaseUrl() + 'plugins/html5-common/scripts/directives/sdDocumentRepository/sdDocumentRepository.html';

      return {
        "scope" : {
          "rootPath" : "=sdaRootPath",
          "menuHook" : "&sdaMenuHook",
          "eventHook" : "&sdaEventHook",
          "showSearchFilter" : "=sdaSearchable"
        },
        "controller" : docRepoController,
        "controllerAs" : 'ctrl',
        "templateUrl" : templateUrl,
        "link" : lnk
      };
      
  }
  
  mod.directive("sdDocumentRepository",docRepo);
  
})();