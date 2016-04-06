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
	
	sdReportsService.$inject = ["$http", "$q", "sdUtilService", "sgI18nService"];
	
	function sdReportsService($http, $q, sdUtilService, sgI18nService){
		this.$http = $http;
		this.$q = $q;
		this.rootUrl = sdUtilService.getRootUrl();
	    this.i18n = sgI18nService.translate;
	};
	
	/**
	 * Build out the repository folder structure required to support reports on the users
	 * document path. This function will verify and create if not exist in the following sequence:
	 * Firstly we verify the [myDocumentsFolderPath]/reports folder and create it if it does not exist.
	 * Next we build two child folders underneath that reports folder, [saved-reports-design].
	 * Note that if the reports folder exists this will still work as each child is checked individually.
	 * @param  {[type]} myDocumentsFolderPath [description]
	 * @return {[type]}                       [description]
	 */
	sdReportsService.prototype.verifySavedReportsStructure = function(myDocumentsFolderPath){

		var that = this;
		var deferred = this.$q.defer();

		this.__createSRRoot(myDocumentsFolderPath)
		.then(function(){
			var promises = [];
			promises.push(that.__createSRPrivateReports(myDocumentsFolderPath));
			promises.push(that.__createSRPublicReports(myDocumentsFolderPath));
			return that.$q.all(promises);
		})
		.then(function(vals){
			deferred.resolve()

		})
		["catch"](function(err){
			deferred.reject(err);
		});

		return deferred.promise;
	};

	//Retrieve personal and private reports for the user, these are created as either
	//Public or Private report definitions under the Report Definitions root folder. (#1,#2)
	sdReportsService.prototype.getReportDefinitionPaths = function(myDocumentsFolderPath){
		
		var deferred = this.$q.defer(),
			paths =[],
			upathKey,
			publicReportsLabel,
			privateReportsLabel,
			that = this;
		
		//Ccompute our i18n labels
		publicReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-publicReportDefinitions-label");
		privateReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-privateReportDefinitions-label");
		
		//1: Add the fixed path we do know for the global Public Report Definitions
		paths={"/reports/designs" : publicReportsLabel}

		//2:Now compute the private path for the current users Private Report Definitions
		upathKey = myDocumentsFolderPath + "/reports/designs"
		paths[upathKey] = privateReportsLabel;
		deferred.resolve(paths);


		return deferred.promise;
		
	};

	//Retrieve the public and private folder for Saved Reports (#4,#5)
	sdReportsService.prototype.getSavedReportsPaths = function(myDocumentsFolderPath){

		var deferred = this.$q.defer(),
			paths =[],
			promises=[],
			upathKey,
			publicReportsLabel,
			privateReportsLabel;
		
		//Compute our i18n labels
		publicReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-publicReportDefinitions-label");
		privateReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-privateReportDefinitions-label");
		
		//1: Add the fixed path we do know for the global Public Report Definitions
		paths={"/reports/saved-reports" : publicReportsLabel}

		//2:Now compute the private path for the current users Private Report Definitions
		upathKey = myDocumentsFolderPath + "/reports/saved-reports";
		paths[upathKey] = privateReportsLabel;

		deferred.resolve(paths);

		return deferred.promise;
	};

	/**
	 * Utility function to create a folder
	 * @param  {[type]} parentPath [description]
	 * @param  {[type]} name       [description]
	 * @return {[type]}            [description]
	 */
	sdReportsService.prototype.createFolder = function(parentPath,name){

		var url = this.rootUrl + "/services/rest/portal/folders";
		var deferred = this.$q.defer();
		var data = {};

		data.parentFolderPath = parentPath;
		data.name = name;

		this.$http({
			"method" : "POST",
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

	/**
	 * Create the root reports folder underneath the users myDocumentsFolderPath.
	 * More of a CreateIfNotExist function as it only creates the folder if it doesnt exist.
	 * @param  {[type]} myDocumentsFolderPath [description]
	 * @return {[type]}                       [description]
	 */
	sdReportsService.prototype.__createSRRoot = function(myDocumentsFolderPath){

		var url = this.rootUrl + "/services/rest/portal/folders" + myDocumentsFolderPath + "/reports";
		var that = this;
		var deferred = this.$q.defer();

		//Test public saved reports
		this.$http({
			"method" : "GET",
			"url" : url
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(err){

			if(err.status===404){
				return that.createFolder(myDocumentsFolderPath + "/","reports");
			}
			else{
				deferred.reject();
			}
			
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(){
			deferred.reject();
		});

		return deferred.promise;
	};

	/**
	 * Checks for the existance of the saved-reports folder underneath the users document path reports folder
	 * and creates it if it does nto exist.
	 * @param  {[type]} myDocumentsFolderPath [description]
	 * @return {[type]}                       [description]
	 */
	sdReportsService.prototype.__createSRPrivateReports = function(myDocumentsFolderPath){

		var url = this.rootUrl + "/services/rest/portal/folders" + myDocumentsFolderPath + "/reports/saved-reports";
		var that = this;
		var deferred = this.$q.defer();

		//Test public saved reports
		this.$http({
			"method" : "GET",
			"url" : url
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(err){

			if(err.status===404){
				return that.createFolder(myDocumentsFolderPath + "/reports/","saved-reports");
			}
			else{
				deferred.reject();
			}
			
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(){
			deferred.reject();
		});

		return deferred.promise;
	};

	/**
	 * Checks for the existance of the users designs folder underneath the users document path reports folder,
	 * and creates it if it does not exist.
	 * @param  {[type]} myDocumentsFolderPath [description]
	 * @return {[type]}                       [description]
	 */
	sdReportsService.prototype.__createSRPublicReports = function(myDocumentsFolderPath){

		var url = this.rootUrl + "/services/rest/portal/folders" + myDocumentsFolderPath + "/reports/designs";
		var that = this;
		var deferred = this.$q.defer();

		//Test public saved reports
		this.$http({
			"method" : "GET",
			"url" : url
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(err){

			if(err.status===404){
				return that.createFolder(myDocumentsFolderPath + "/reports/","designs");
			}
			else{
				deferred.reject();
			}
			
		})
		.then(function(){
			deferred.resolve();
		})
		["catch"](function(){
			deferred.reject();
		});

		return deferred.promise;
	};

	//Get the Role/org based reports, should return two collections , designs and saved-reports,
	//designs will be used underneath Report Definitions while saved-reports will be merged with 
	//the Saved Reports node. (#3)
	sdReportsService.prototype.getParticipantReports = function(){

		var url = this.rootUrl + "/services/rest/portal/reports/participant";
		var deferred = this.$q.defer();

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


	/**
	 * Return the grants for participants (ROLES/ORGS)
	 * @param  {[type]} userId [description]
	 * @return {[type]}        [description]
	 */
	sdReportsService.prototype.getRoleOrgReportDefinitionGrants = function(userId){

		var url = this.rootUrl + "/services/rest/portal/participant/grant/" + userId;
		var deferred = this.$q.defer();

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

	/**
	 * This one sells itself...
	 * @return {[type]} [description]
	 */
	sdReportsService.prototype.getCurrentUser = function(){

		var url = this.rootUrl + "/services/rest/portal/user/whoAmI";
		var deferred = this.$q.defer();

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

	angular.module("viewscommon-ui.services").service("sdReportsService",sdReportsService);
	
})();