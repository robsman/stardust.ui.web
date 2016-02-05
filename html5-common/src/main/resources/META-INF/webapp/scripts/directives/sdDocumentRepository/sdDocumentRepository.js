(function(){
  
  var mod = angular.module("bpm-common.directives");
  
  
  /*Controller section*/
  
  docRepoController.$inject = ["documentRepositoryService", "$timeout", "sdUtilService", 
                               "$scope", "$filter", "sdViewUtilService", "$q",
                               "sdMimeTypeService", "sdI18nService"];

  function docRepoController(documentRepositoryService, $timeout, sdUtilService, $scope, $filter, sdViewUtilService, $q, sdMimeTypeService, sdI18nService){

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

    this.textMap = this.getTextMap(this.i18n);

    this.selectedRepo = {};
    this.repositoryProviders =[]; 
    this.selectedMatches = [];
    this.documentRepositoryUrl = documentRepositoryService.documentRoot + "/upload";

    //set up a watch on our selected matches from our autocomplete directive
    $scope.$watchCollection('ctrl.selectedMatches', function(newValue, oldValue) {
      if(newValue.length > 0){
        that.expandNodePath(newValue[0]);
      };
    });

    //Async retrieve repo providers.
    this.getRepositoryProviders()
    .then(function(providers){
      that.repositoryProviders = providers;
    });

  }
  
  docRepoController.prototype.expandNodePath = function(path){

    /*TODO: Implementation...
    leverage the eventCallback method to dupe tree node-expand 
    events for our path. We will pass a deferred with the object
    and once that deferred is resolve we will process the next
    node in the path until we reach the last folder to expand.
    This approach is used for a single folder in our refreshFolder
    method.
    */

  };

  docRepoController.prototype.getTextMap = function(i18n){

    textMap = {};
    textMap.rename = i18n.translate("views.genericRepositoryView.treeMenuItem.rename");
    textMap.delete = i18n.translate("views.genericRepositoryView.treeMenuItem.delete");
    textMap.createSubFolder = i18n.translate("views.genericRepositoryView.treeMenuItem.createSubFolder");
    textMap.createNewDoc = i18n.translate("views.genericRepositoryView.treeMenuItem.createNewDoc");
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
    textMap.default = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.default");
    textMap.repoRoot = i18n.translate("views.genericRepositoryView.treeMenuItem.repo.root");

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
  
  docRepoController.prototype.getDocumentClass = function(doc){

    var docType = doc.contentType;

    return this.sdMimeTypeService.getIcon(docType);  
   
  };

  docRepoController.prototype.iconCallback = function(data,e){
    
    var classes=["fa"];

    if(data.nodeType=="folder" || data.nodeType == "repoFolderRoot"){
      classes.push("pi-folder");
    }
    else if(data.nodeType=="document"){
      classes.push(this.getDocumentClass(data));
    }
    else{
       classes.push("pi-database");
    }
    
    return classes.join(" ");
    
  };
  
  docRepoController.prototype.eventCallback = function(data,e){
    
    var that = this;

    switch(data.treeEvent){
      case "node-expand":
        if(! data.valueItem.isInitialized && 
        (data.valueItem.nodeType ==='folder' || data.valueItem.nodeType ==='repoFolderRoot')){

          var resourceId;
          resourceId = (data.valueItem.nodeType ==='repoFolderRoot')?data.valueItem.path : data.valueItem.id;

          this.documentService.getChildren(resourceId)
          .then(function(root){
            var children = that.treeifyChildren(root);
            data.valueItem.children = children;
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
            rootFolder.children = that.treeifyChildren(rootFolder);
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
          this.deleteDocument(data.nodeId,data.valueItem);
        }
        else if(data.valueItem.nodeType==="folder"){
           this.deleteFolder(data.nodeId,data.valueItem);
        }
        break;
      case "node-rename-commit":
        if(data.valueItem.nodeType==="document"){
          this.renameDocument(data.valueItem.id,data.newValue,data.valueItem);
        }
        else if(data.valueItem.nodeType==="folder"){
           this.renameFolder(data.valueItem.id,data.newValue,data.valueItem);
        }
        data.deferred.resolve();
        break;
      case "node-click" :
        if(data.valueItem.nodeType==="document"){
          this.openDocumentView(data.valueItem.id);
        }
        break;
      case "node-rename" :
        data.deferred.resolve();
        break;
      case "menu-bind" :
        this.openBindRepoDialog();
        break;
      case "menu-setDefault" :
        this.setRepositoryAsDefault(data.valueItem.id);
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
        this.uploadFile(data.valueItem,data);
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

    if(menuData.item.nodeType=="RootRepo"){
      menuItems.push("(bind,"+ this.textMap.bindRepo +")");
    }
    else if(menuData.item.nodeType=="Repo"){

      //TODO:double check this logic
      if(menuData.item.isDefault===false){
        menuItems.push("(setDefault," + this.textMap.makeDefault + ")");
        menuItems.push("(unbindRepo," + this.textMap.unbindRepo + ")");
      }

      menuItems.push("(repoProperties, " + this.textMap.properties + ")");
       menuItems.push("(unbindRepo, " + this.textMap.unbindRepo + ")");
    }
    else if(menuData.item.nodeType=="folder"){

      menuItems.push("(rename," + this.textMap.rename + ")");
      menuItems.push("(delete," + this.textMap.delete + ")");
      menuItems.push("(createSubFolder," + this.textMap.createSubFolder + ")");
      menuItems.push("(createFile," + this.textMap.createNewDoc + ")");
      menuItems.push("(uploadFile," + this.textMap.uploadFile + ")");
      menuItems.push("(uploadZipFile," + this.textMap.uploadZip + ")");
      menuItems.push("(downloadFolder," + this.textMap.sendToZip + ")");
      menuItems.push("(refreshFolder," + this.textMap.refresh + ")");
      menuItems.push("(securityFolder," + this.textMap.security  + ")");

    }
    else if(menuData.item.nodeType==="repoFolderRoot"){

      menuItems.push("(createSubFolder," + this.textMap.createSubFolder + ")");
      menuItems.push("(createFile," + this.textMap.createNewDoc + ")");
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
    menuData.deferred.resolve(menuItems.toString());
  };
  
  docRepoController.prototype.isLeaf = function(nodeItem){
    return nodeItem.nodeType==='document';
  };

  docRepoController.prototype.uploadAndExplode = function(folder){
    
    var that = this;
    var treeFolder;

    //property tied to the upload dialog directive must be updated
    this.selectedFolderPath = folder.path;

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
					         sda-is-leaf="ctrl.isLeaf(child)" \
		   			       sda-lazy-compile="true" \
					         sda-label="child.name"> \
    					 <ul> \
    						 <li sd-tree-curse-fx></li> \
    					 </ul> \
    				 </li>';
	  
	  return template;
  };
  
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
  
  docRepoController.prototype.uploadFile = function(targetFolder){
    var that = this;
    var treeFolder;

    treeFolder = that.treeApi.childNodes[targetFolder.id];

    //reset state as this should be null unless we are uploading a new
    //version of an existing document.
    this.documentVersionTarget=null;

    //property tied to the upload dialog directive must be updated
    this.selectedFolderPath = targetFolder.path;

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
    that.uploadDialogAPI.open()
    .then(function(files){
      
      files.forEach(function(file){
        file.nodeType = "document";
        file.id = file.uuid;
        targetFolder.children.push(file);
        treeFolder.isVisible = true;
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
      targetNode.nodeItem.children = children;
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
    var that = this;
    this.documentService.getFileVersionHistory(doc.id)
    .then(function(fvh){
      that.currentFileVersionHistory = fvh.data;
      that.fileVersionHistoryDialog.open(doc);
    });
  };
  
  docRepoController.prototype.createSubFolder = function(parentFolderNode){

    var that = this;
    var name = "New Folder"
    var dateTime = new Date();
    var name = this.$filter('date')(new Date(), 'yyyy-MM-dd HH-mm-ss');
    var parentFolderId;
    var newFolderId;
    
    name = "New Folder, " + name;
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
    var name = this.$filter('date')(new Date(), 'yyyy-MM-dd HH-mm-ss');

    name = "New Document, " + name;

    this.documentService.createDocument(parentFolderNode.path,name)
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
  
  docRepoController.prototype.deleteFolder = function(nodeId,nodeItem){
    var that = this;
    this.documentService.deleteFolder(nodeItem.id)
    .then(function(name){
      var parentItem = that.treeApi.getParentItem(nodeId);
      var index = parentItem.children.indexOf(nodeItem);
      parentItem.children.splice(index,1);
    });
  };
  
  docRepoController.prototype.deleteDocument = function(nodeId,nodeItem){
    var that = this;
    this.documentService.deleteDocument(nodeItem.id)
    .then(function(name){
      var parentItem = that.treeApi.getParentItem(nodeId);
      var index = parentItem.children.indexOf(nodeItem);
      parentItem.children.splice(index,1);
    });
  };
  
  docRepoController.prototype.renameFolder = function(folderId,newName,nodeItem){
    var that = this;
    this.documentService.renameFolder(folderId,newName)
    .then(function(name){
      nodeItem.name = name;
    });
  };
  
  docRepoController.prototype.renameDocument = function(docId,newName,nodeItem){
    var that = this;
    this.documentService.renameDocument(docId,newName)
    .then(function(doc){
      nodeItem.name = doc.name;
    });
  };
  
  docRepoController.prototype.unbindRepository = function(treeEvent){
    var that = this;
    this.documentService.unbindRepository(treeEvent.valueItem.id)
    .then(function(){
      var parentItem = that.treeApi.getParentItem(treeEvent.valueItem.id);
      var index = parentItem.children.indexOf(treeEvent.valueItem);
      parentItem.children.splice(index,1);
    });
  };
  
  docRepoController.prototype.setRepositoryAsDefault = function(repoId){
    this.documentService.makeRepositoryDefault(repoId)
    .then(function(){
      //successdialog
      alert("repo is now default");
    });
  };
  
  docRepoController.prototype.openRepoPropertyDialog = function(){
    this.repoPropertyDialog.open();
  };
  
  docRepoController.prototype.openBindRepoDialog = function(){
    this.bindRepoDialog.open();
  };
  
   docRepoController.prototype.onBindDialogConfirm = function(res){
    var that = this;
    var jndiName = that.boundDialogRepoProvider.jndiName;
    var id = that.boundDialogRepoProvider.beanId;
    var providerId = that.boundDialogRepoProvider.id;
    
    if(res===true){
      that.documentService.bindRepository(providerId,id,jndiName)
      .then(function(boundRepo){
        //TODO:refresh tree as we need the uuid etc.
        that.data[0].children.push(boundRepo);//this wont work...
      })
      ["catch"](function(err){
        alert("TODO: Error handling!" );
      });
    };
    
  };

  docRepoController.prototype.onBindDialogOpen = function(res){
    var that = this;
    return;    
    res.promise.then(function(){
      that.documentService.bindRepository(that.boundDialogRepoProvider)
      .then(function(boundRepo){
        that.data[0].children.push(boundRepo);
      });
    });
    
  };
  
  docRepoController.prototype.getMatches = function(matchVal){
    var that = this;
    this.documentService.searchRepository(matchVal)
    .then(function(res){
      that.searchMatches = res;
    });

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
    $attrs.$observe("sdaRootPath",function(v1){
      //TODO:update tree nodes

      ctrl.documentService.getRepositories(v1)
      .then(function(data){
        data[0].children.forEach(function(elem){
          elem.nodeType = "Repo";
          elem.children=[];
        });
        ctrl.data = data;
      });
    });
    
  }
  
  /*Directive Section*/
  docRepo.$inject = ["sdUtilService"];
  
  function docRepo(sdUtilService){
      
      var templateUrl = sdUtilService.getBaseUrl() + 'plugins/html5-common/scripts/directives/sdDocumentRepository/sdDocumentRepository.html';

      return {
        "controller" : docRepoController,
        "controllerAs" : 'ctrl',
        "templateUrl" : templateUrl,
        "link" : lnk
      };
      
  }
  
  mod.directive("sdDocumentRepository",docRepo);
  
})();