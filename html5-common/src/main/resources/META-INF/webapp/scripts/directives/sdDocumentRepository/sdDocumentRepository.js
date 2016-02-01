(function(){
  
  var mod = angular.module("bpm-common.directives");
  
  
  /*Controller section*/
  
  docRepoController.$inject = ["documentRepositoryService", "$timeout", "sdUtilService", 
                               "$scope", "$filter", "sdViewUtilService", "$q","sdMimeTypeService"];
  
  //constructor
  function docRepoController(documentRepositoryService, $timeout, sdUtilService, $scope, $filter, sdViewUtilService, $q,sdMimeTypeService){

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
    this.selectedRepo = {};
    this.repositoryProviders =[]; 
    this.documentRepositoryUrl = documentRepositoryService.documentRoot + "/upload";

    //Async retrieve repo providers.
    this.getRepositoryProviders()
    .then(function(providers){
      that.repositoryProviders = providers;
    });

  }
  

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
      menuItems.push("(bind,Bind Repository)");
    }
    else if(menuData.item.nodeType=="Repo"){

      //TODO:double check this logic
      if(menuData.item.isDefault===false){
        menuItems.push("(setDefault,Make Defualt)");
        menuItems.push("(unbindRepo,Unbind Repository)");
      }

      menuItems.push("(repoProperties,Properties)");
       menuItems.push("(unbindRepo,Unbind Repository)");
    }
    else if(menuData.item.nodeType=="folder"){

      menuItems.push("(rename,Rename)");
      menuItems.push("(delete,Delete)");
      menuItems.push("(createSubFolder,Create Sub Folder)");
      menuItems.push("(createFile,Create New File)");
      menuItems.push("(uploadFile,Upload File)");
      menuItems.push("(downloadFolder,Download Folder)");
      menuItems.push("(refreshFolder,Refresh)");
      menuItems.push("(securityFolder,Security Settings)");

    }
    else if(menuData.item.nodeType==="repoFolderRoot"){

      menuItems.push("(createSubFolder,Create Sub Folder)");
      menuItems.push("(createFile,Create New File)");
      menuItems.push("(uploadFile,Upload File)");
      menuItems.push("(refreshFolder,Refresh)");
      menuItems.push("(securityFolder,Security Settings)");

    }
    else if(menuData.item.nodeType=="document"){

      menuItems.push("(rename,Rename)");
      menuItems.push("(delete,Delete)");
      menuItems.push("(versionFileHistory,Version History)");
      menuItems.push("(downloadFile,Download)");
      menuItems.push("(uploadNewFileVersion,Upload New Version)");
      menuItems.push("(refreshFile,Refresh)");
      menuItems.push("(securityFile,Security Settings)");

    }
    menuData.deferred.resolve(menuItems.toString());
  };
  
  docRepoController.prototype.isLeaf = function(nodeItem){
    return nodeItem.nodeType==='document';
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
    alert("TODO: UNFV utilize file upload directive if possible.");
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

    name = "New Folder, " + name;
    parentFolderId = (parentFolderNode.nodeType=="folder")?parentFolderNode.id:parentFolderNode.path;

    this.documentService.createFolder(parentFolderId,name)
    .then(function(newFolder){
      newFolder.id=newFolder.uuid;
      newFolder.nodeType = "folder";
      parentFolderNode.children.push(newFolder);
      that.$timeout(function(){
        that.treeApi.expandNode(parentFolderNode.id);
        that.treeApi.editNode(newFolder.id);
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

      that.$timeout(function(){
        that.treeApi.expandNode(parentFolderNode.id);
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