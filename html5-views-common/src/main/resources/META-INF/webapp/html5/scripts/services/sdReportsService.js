(function(){
	
	sdReportsService.$inject = ["$http", "$q", "sdUtilService"];
	
	function sdReportsService($http, $q, sdUtilService){
		this.$http = $http;
		this.$q = $q;
		this.rootUrl = sdUtilService.getRootUrl();
	};
	
	
	sdReportsService.prototype.getReportPaths = function(myDocumentsFolderPath){
		
		var deferred = this.$q.defer(),
			paths =[],
			upathKey;
		
		//1: Add the fixed path we do know
		paths={"reports/designs" : "Private Report Definitions"}

		//2:Now compute the one we dont
		upathKey = myDocumentsFolderPath + "/reports/designs"
		paths[upathKey] = "Public Report Definitions";
		deferred.resolve(paths);

		return deferred.promise;
		
	};

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