(function(){

	sdResourceMgmtTreeService.$inject = ["$http", "$q", "sdUtilService"];

	function sdResourceMgmtTreeService($http, $q, sdUtilService){

		this.$http = $http;
		this.$q = $q;
		this.rootPath = "/artifacts";
		this.subFolders = ["/artifacts/skins","/artifacts/bundles","/artifacts/content"];

		this.baseUrl = sdUtilService.getBaseUrl() + "services/rest/portal/folders";
	};

	sdResourceMgmtTreeService.prototype.calculateMissingFolders = function(folders){
		var missingFolders = this.subFolders.filter(function(subFolder){
			return !folders.some(function(folder){
				return folder.path == subFolder;
			});
		});
		return missingFolders || [];
	};

	sdResourceMgmtTreeService.prototype.createFolderIfNotExist = function(path){
		var deferred = this.$q.defer(),
			url;

		url = this.baseUrl + path + "?create=true";

		this.$http.get(url)
		.then(function(res){
			deferred.resolve();
		})
		["catch"](function(err){
			deferred.reject(err);
		})

		return deferred.promise;
	};

	sdResourceMgmtTreeService.prototype.buildDefaultStructure = function(){
		var deferred,
			that=this,
			url;

		deferred = this.$q.defer();
		url = this.baseUrl + this.rootPath + "?create=true";

		//Retrieve root folder, creating it if it does not exist.
		this.$http.get(url)
		.then(function(res){

			var folders = res.data.folders,
				promises = [],
				missingFolders = [];
			
			missingFolders = that.calculateMissingFolders(folders);
			missingFolders.forEach(function(path){
				promises.push(that.createFolderIfNotExist(path));
			})

			return that.$q.all(promises);

		})
		.then(function(vals){
			deferred.resolve(vals);
		})
		["catch"](function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};

	angular.module("viewscommon-ui.services")
	.service("sdResourceMgmtTreeService",sdResourceMgmtTreeService);

})();