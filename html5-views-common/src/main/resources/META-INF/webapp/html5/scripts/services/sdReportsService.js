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
	
	
	//Retrieve personal and private reports for the user, these are created as either
	//a personal report template or a global report template in the Reporting view.
	sdReportsService.prototype.getReportPaths = function(myDocumentsFolderPath){
		
		var deferred = this.$q.defer(),
			paths =[],
			upathKey,
			publicReportsLabel,
			privateReportsLabel;
		
		//0:compute our i18n labels
		publicReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-publicReportDefinitions-label");
		privateReportsLabel = this.i18n("views-common-messages.views-genericRepositoryView-systemFolders-privateReportDefinitions-label");
		
		//1: Add the fixed path we do know
		paths={"reports/designs" : publicReportsLabel}

		//2:Now compute the one we don't
		upathKey = myDocumentsFolderPath + "/reports/designs"
		paths[upathKey] = privateReportsLabel;
		deferred.resolve(paths);

		return deferred.promise;
		
	};

	//Retrieve reports created as 'Report Defininition for a Role or Organization'
	//This will return an array with two object, item 0 is reports design folders
	//which should be flattened out at the top level, item 1 will need to go under a
	//saved reports folder.
	sdReportsService.prototype.getPersonalReports = function(){

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