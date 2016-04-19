/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/
 
(function(){

	sdMyDocumentsTreeService.$inject = ["$http", "$q", "sdUtilService"];

	function sdMyDocumentsTreeService($http, $q, sdUtilService){

		this.$http = $http;
		this.$q = $q;
		this.rootPath = "/documents";
		this.subFolders = ["/documents/stamps","/documents/correspondence-templates"];

		this.baseUrl = sdUtilService.getBaseUrl() + "services/rest/portal/folders";
	};

	sdMyDocumentsTreeService.prototype.calculateMissingFolders = function(folders){
		var missingFolders = this.subFolders.filter(function(subFolder){
			return !folders.some(function(folder){
				return folder.path == subFolder;
			});
		});
		return missingFolders || [];
	};

	sdMyDocumentsTreeService.prototype.createFolderIfNotExist = function(path){
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

	sdMyDocumentsTreeService.prototype.buildDefaultStructure = function(user){
		var deferred,
			that=this,
			url;

		deferred = this.$q.defer();
		url = this.baseUrl + this.rootPath + "?create=true";

		this.subFolders.push(user.myDocumentsFolderPath + "/reports/designs");

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
	.service("sdMyDocumentsTreeService",sdMyDocumentsTreeService);

})();