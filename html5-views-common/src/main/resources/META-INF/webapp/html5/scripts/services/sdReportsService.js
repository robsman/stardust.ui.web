(function(){
	
	sdReportsService.$inject = ["$http", "$q", "sdUtilService"];
	
	function sdReportsService($http, $q, sdUtilService){
		this.$http = $http;
		this.$q = $q;
		this.rootUrl = sdUtilService.getRootUrl();
	};
	
	
	sdReportsService.prototype.getReportPaths = function(){
		
		var deferred = this.$q.defer(),
			paths =[],
			that;
		
		//this will always exist as such...
		//paths.push("reports/designs");
		


		//TODO: Need list if fully qualified participant IDs
		//For each fully qualified participant create a path as such...
		//.../reports/{fullyQualifeidParticipantId}/designs
		paths={
			"reports/designs" : "Private Report Definitions",
		}

		this.getCurrentUser()
		.then(function(user){

			var upathKey = user.myDocumentsFolderPath + "/reports/designs"
			paths[upathKey] = "Public Report Definitions";
			deferred.resolve(paths);

		})
		["catch"](function(){
			deferred.reject();
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