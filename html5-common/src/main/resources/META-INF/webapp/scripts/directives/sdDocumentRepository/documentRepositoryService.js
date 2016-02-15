(function(){
  
 
  //Virtual root node of all instanced repositories
  var virtualRoot = {
    "name" : "Available Repositories",
    "id" : "VirtualRoot",
    "type" : "VirtualRoot",
    "version" : "0.0.0",
    "isDefault" : false,
    "nodeType" : "RootRepo",
    "children" : []
  };

  //Artificially constructed Root Nodes for a single Repository.
  var repoRoot = {
    "name" : "",
    "id" : "",
    "uuid" : "",
    "path" : "",
    "nodeType" : "repoFolderRoot",
    "children" : []
  }

  /**Bootstrap Section Ends**/
  
  documentRepoService.$inject = ["$q","$timeout","$location","$http", "sdUtilService"];
  
  function documentRepoService($q, $timeout, $location, $http, sdUtilService){

    var absUrl;

    //Put dependencies on our object which we will need later
    this.$q = $q;
    this.$timeout = $timeout;
    this.$http = $http;

    //TODO:remove
    this.docRepo = [{"id":"System","name":"Jackrabbit","type":"Content Repository API for Java(TM) Technology Specification 2.0","version":"2.6.1","transactionSupported":false,"versioningSupported":false,"writeSupported":false}]; 
    
    //Calculate base URL
    absUrl=$location.absUrl();;
    
    //And calculate our url fragments
    this.absUrl = sdUtilService.getRootUrl();
    this.folderRoot = this.absUrl + "/services/rest/portal/folders";
    this.documentRoot = this.absUrl + "/services/rest/portal/documents";
    this.rootUrl = this.absUrl + "/services/rest/portal/repository";

  }

  documentRepoService.prototype.searchRepository = function(searchVal){

    var deferred = this.$q.defer(),
        results,
        data;

    url = this.rootUrl + "/search";

    data = {
      "name" : searchVal,
      "searchType" : "BOTH"
    };

    if(searchVal){
      this.$http({
        "method" : "POST",
        "url" : url,
        "data" : data
      })
      .then(function(res){
        //documents have a repositoryId property but folders do not.
        //We will add the proper repoID to each folder at this point.
        res.data.folders.list.forEach(function(folder){
            folder.repositoryId =  folder.uuid.split("}{")[0].split(":")[2];
        });
        deferred.resolve(res.data);
      })
      ["catch"](function(err){
        deferred.reject(err);
      });
    }
    else{
      deferred.reject("Empty Search Value");
    }

    return deferred.promise;
  };

  documentRepoService.prototype.getRepositoryRootFolder = function(repositoryId){

    var deferred = this.$q.defer();
    var repoUrlId = "{urn:repositoryId:" + repositoryId + "}";

    this.$http({
      "method" : "GET",
      "url" : this.folderRoot + "/" + repoUrlId + "//",
    })
    .then(function(res){
      deferred.resolve(res);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });

    return deferred.promise;

  };

  documentRepoService.prototype.getDocumentPolicy = function(documentId){

    var deferred = this.$q.defer();

    this.$http({
      "method" : "GET",
      "url" : this.documentRoot + "/policy/" + documentId
    })
    .then(function(res){
      deferred.resolve(res);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });

    return deferred.promise;

  };

  documentRepoService.prototype.getFolderPolicy = function(folderId){

    var deferred = this.$q.defer();

    this.$http({
      "method" : "GET",
      "url" : this.folderRoot + "/policy/" + folderId
    })
    .then(function(res){
      deferred.resolve(res);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });

    return deferred.promise;
  };

  documentRepoService.prototype.getVirtualRoot = function(){
    var deferred = this.$q.defer();
    deferred.resolve(angular.extend({},virtualRoot));
    return deferred.promise;
  };

  documentRepoService.prototype.setFileSecurity = function(fileId,settings){
    var deferred = this.$q.defer();
    deferred.resolve({});
    return deferred.promise;
  };
  
  documentRepoService.prototype.setFolderSecurity = function(folderId,settings){
    var deferred = this.$q.defer();
    deferred.resolve({});
    return deferred.promise;
  };
  
  documentRepoService.prototype.getFileVersionHistory = function(documentId){
    var deferred = this.$q.defer();

    this.$http({
      "method" : "GET",
      "url" : this.documentRoot + "/history/" + documentId
    })
    .then(function(res){
      deferred.resolve(res);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });

    return deferred.promise;

  };
  
  documentRepoService.prototype.getFolder = function(folderId){
    var deferred = this.$q.defer();
    deferred.resolve({});
    return deferred.promise;
  };
  
  documentRepoService.prototype.createFolder = function(parentFolderId,name){

    var deferred = this.$q.defer();
    var url= this.folderRoot + "/" + parentFolderId;

    this.$http({
      "method" : "POST",
      "url" : url,
      "data" : {
        "name" : name
      }
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.deleteFolder = function(folderId){

    var deferred = this.$q.defer();
    var url= this.folderRoot + "/" + folderId;

    this.$http({
      "method" : "DELETE",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.getDocument = function(documentId){

    var deferred = this.$q.defer();
    var url= this.documentRoot + "/" + documentId;
    
    this.$http({
      "method" : "GET",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;
  };
  
  documentRepoService.prototype.createDocument = function(path,name){

    var deferred = this.$q.defer();
    var url= this.documentRoot;
    var data = { 
      "name" : name,
      "content" : "",
      "description" : "",
      "parentFolderPath" : path,
      "properties" : {}
    };
    this.$http({
      "method" : "POST",
      "url" : url,
      "data" : data
    })
    .then(function(res){
      deferred.resolve(res.data.documents[0]);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.deleteDocument = function(docId){

    var deferred = this.$q.defer();
    var url= this.documentRoot + "/" + docId;
    
    this.$http({
      "method" : "DELETE",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.renameFolder = function(folderId,newName){

    var deferred = this.$q.defer();
    var url= this.folderRoot + "/" + folderId;
    var data = { "name" : newName};

    this.$http({
      "method" : "PUT",
      "url" : url,
      "data" : data
    })
    .then(function(res){
      deferred.resolve(newName);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;
  };
  
  documentRepoService.prototype.renameDocument = function(docId,newName){

      var deferred = this.$q.defer();
      var url= this.documentRoot + "/" + docId;
      var data = { "name" : newName};

      this.$http({
        "method" : "PUT",
        "url" : url,
        "data" : data
      })
      .then(function(res){
        deferred.resolve(res.data);
      })
      ["catch"](function(err){
        deferred.reject(err);
      });
      
      return deferred.promise;

  };
  
  documentRepoService.prototype.makeRepositoryDefault = function(repoId){

    var deferred = this.$q.defer();
    var url= this.rootUrl + "/default/" + repoId;

    this.$http({
      "method" : "PUT",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.unbindRepository = function(repoId){

    var deferred = this.$q.defer();
    var url= this.rootUrl + "/unbind/" + repoId;

    this.$http({
      "method" : "PUT",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.getVirtualRoot = function(){
    var deferred = this.$q.defer();
    deferred.resolve(virtualRoot);
    return deferred.promise;
  };

  //Returns repositories along with a virtual root element.
  documentRepoService.prototype.getRepositories = function(){

    var deferred = this.$q.defer();

    this.$http({
      "method" : "GET",
      "url" : this.rootUrl,
    })
    .then(function(res){
      var vr = angular.extend({}, virtualRoot)
      vr.children=(res.data);
      deferred.resolve([vr]);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });

    return deferred.promise;

  };
  
  documentRepoService.prototype.getRepositoryProviders = function(){

    var deferred = this.$q.defer();
    var url= this.rootUrl + "/providers";

    this.$http({
      "method" : "GET",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;

  };
  
  documentRepoService.prototype.bindRepository = function(providerId, id, jndiName){

    var deferred = this.$q.defer();
    var url= this.rootUrl + "/bind";
    var data

    data={
      "providerId" : providerId,
      "id" : id,
      "jndiName" : jndiName
    };

    this.$http({
      "method" : "PUT",
      "url" : url,
      "data" : data
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;
  };
  
  documentRepoService.prototype.getChildren = function(folderId){
    var deferred = this.$q.defer();
    var url = this.folderRoot + "/" + folderId;
    
    this.$http({
      "method" : "GET",
      "url" : url
    })
    .then(function(res){
      deferred.resolve(res.data);
    })
    ["catch"](function(err){
      deferred.reject(err);
    });
    
    return deferred.promise;
  };
  
  
  angular.module("bpm-common.directives")
  .service("documentRepositoryService",documentRepoService);
  
})();