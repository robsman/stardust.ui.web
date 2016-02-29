(function(){
	
	sdReportsService.$inject = ["$http", "$q", "sdUtilService"];
	
	function sdReportsService($http, $q, sdUtilService){
		this.$http = $http;
		this.$q = $q;
		this.rootUrl = sdUtilService.getRootUrl();
	};
	
	
	//Retrieve personal and private reports for the user, these are created as either
	//a personal report template or a global report template in the Reporting view.
	sdReportsService.prototype.getReportPaths = function(myDocumentsFolderPath){
		
		var deferred = this.$q.defer(),
			paths =[],
			upathKey;
		
		//1: Add the fixed path we do know
		paths={"reports/designs" : "Personel Report Definitions"}

		//2:Now compute the one we don't
		upathKey = myDocumentsFolderPath + "/reports/designs"
		paths[upathKey] = "Private Report Definitions";
		deferred.resolve(paths);

		return deferred.promise;
		
	};

	//Retrieve reports created as 'Report Defininition for a Role or Organization'
	//This will return an array with two object, item 0 is reports design folders
	//which should be flattened out at the top level, item 1 will need to go under a
	//saved reports folder.
	sdReportsService.prototype.getPersonalReports = function(){

		var url = this.rootUrl + "/services/rest/portal/reports/personal";
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