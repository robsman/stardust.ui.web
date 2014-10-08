// Define Custom Modules
// Top level module needs to have dependency on framework top level module called 'bpm-ui'
angular.module('bpm-common.services', []);
angular.module('bpm-common.directives', []);
angular.module('bpm-common', ['bpm-common.services', 'bpm-common.directives', 'ngDialog'])
.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('httpInterceptor');
}]);

// Register top level module to Framework
portalApplication.registerModule('bpm-common');