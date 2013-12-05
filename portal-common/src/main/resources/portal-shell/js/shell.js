/**
 *  
 */
define(['angular', 'jQuery', 'angular-resources'], function () {
	var shellRMod = {};

	var shellAMod = angular.module('shell', ['shell.services', 'ngResource', 'shell.init']);
	shellRMod.module = shellAMod;

	var shellServicesAMod = angular.module('shell.services', []);
	shellRMod.services = shellServicesAMod;

	shellAMod.config(['$routeProvider', 'sgNavigationServiceProvider', function($routeProvider, sgNavigationServiceProvider) {
		sgNavigationServiceProvider.$routeProvider = $routeProvider;
	}]);
	
	angular.module('shell.init', []).
		run(['$log', '$window', function($log, $window){
			$log.log('Initializing shell.');
	}]);

	return shellRMod;
});
