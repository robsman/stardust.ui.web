(function(){
	
	sdReportsService.$inject = ["$http", "$q", "sdUtilService"];
	
	function sdReportsService($http, $q, sdUtilService){
		this.$http = $http;
		this.$q = $q;
		this.rootUrl = sdUtilService.getRootUrl();
	};
	
	
	sdReportsService.prototype.getReportPaths = function(){
		
		var deferred = this.$q.defer(),
			paths =[];
		
		//this will always exist as such...
		//paths.push("reports/designs");
		
		//TODO: "realms/carnot/users/motu/documents/" derive from current user data
		//paths.push("realms/carnot/users/motu/documents/reports/designs");
		
		paths={
			"reports/designs" : "Private Report Definitions",
			"realms/carnot/users/motu/documents/reports/designs" : "Public Report Definitions"
		}

		//TODO: Need list if fully qualified participant IDs
		//For each fully qualified participant create a path as such...
		//.../reports/{fullyQualifeidParticipantId}/designs
		
		deferred.resolve(paths);
		
		return deferred.promise;
		
	};
	angular.module("viewscommon-ui.services").service("sdReportsService",sdReportsService);
	
})();