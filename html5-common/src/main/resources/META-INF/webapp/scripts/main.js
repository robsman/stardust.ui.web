// Define Custom Modules
angular.module('bpm-common.services', []);
angular.module('bpm-common.directives', []);
angular.module('bpm-common', ['bpm-common.services', 'bpm-common.directives'])
.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('httpInterceptor');
}]);

// Register top level module to Framework
portalApplication.registerModule('bpm-common');