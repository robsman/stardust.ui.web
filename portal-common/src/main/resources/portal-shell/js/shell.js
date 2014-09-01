/**
 *  
 */
	var shellAMod = angular.module('shell', ['shell.services', 'ngResource', 'ngRoute', 'shell.init']);

	var shellServicesAMod = angular.module('shell.services', []);

	shellAMod.config(['$routeProvider', 'sgNavigationServiceProvider', function($routeProvider, sgNavigationServiceProvider) {
		sgNavigationServiceProvider.$routeProvider = $routeProvider;
	}]);
	
	angular.module('shell.init', []).
		run(['$log', '$window', function($log, $window){
			$log.log('Initializing shell.');
	}]);
