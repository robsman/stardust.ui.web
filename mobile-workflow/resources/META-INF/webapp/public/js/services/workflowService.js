define([],function(){
	
	var srvc,
		ngInjector = angular.injector(["ng"]),
	    $http = ngInjector.get("$http"),
	    $q = ngInjector.get("$q");
	
	srvc = {
			
			"test" : "Hello From workflowService",
			
			"addNote" : function(){
				console.log("http=");
				console.log($http);
			}
		};

		return function(){
			return srvc;
		};

});