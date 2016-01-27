(function(){
  
 
  //Virtual root node of all instanced repositories
  var virtualRoot = {
    "name" : "Root",
    "id" : "VirtualRoot",
    "type" : "VirtualRoot",
    "version" : "0.0.0",
    "isDefault" : false,
    "nodeType" : "RootRepo",
    "children" : []
  };
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
  
  documentRepoService.prototype.getVirtualRoot = function(){
    var deferred = this.$q.defer();
    deferred.resolve(angular.extend({},virtualRoot));
    return deferred.promise;
  }

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
  
  documentRepoService.prototype.getFileVersionHistory = function(fileId){
    var deferred = this.$q.defer();
    deferred.resolve({"data" : []});
    return deferred.promise;
  };
  
  documentRepoService.prototype.getFolder = function(folderId){
    var deferred = this.$q.defer();
    deferred.resolve({});
    return deferred.promise;
  };
  
  documentRepoService.prototype.createFolder = function(parentFolderId,name){

    var deferred = this.$q.defer();
    var url= this.folderRoot;

    this.$http({
      "method" : "POST",
      "url" : url,
      "data" : {
        "parentFolderId" : parentFolderId,
        "folderName" : name
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
    deferred.resolve({});
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
  
  documentRepoService.prototype.bindRepository = function(repo){

    var deferred = this.$q.defer();
    var url= this.rootUrl + "/bind";

    this.$http({
      "method" : "PUT",
      "url" : url,
      "data" : repo
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