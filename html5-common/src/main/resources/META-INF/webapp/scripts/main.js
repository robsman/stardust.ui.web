// Define Custom Modules
angular.module('bpm-common.services', []);

angular.module('bpm-common.directives', []);

angular.module('bpm-common.init', []).run(['$rootScope', 'sdUtilService', function($rootScope, sdUtilService) {
	$rootScope.bpmCommon = {
		stopEvent : sdUtilService.stopEvent
	};
}]);

angular.module('bpm-common', ['bpm-common.services', 'bpm-common.directives', 'bpm-common.init', 'ngDialog'])
.config(['$httpProvider', function ($httpProvider) {
	$httpProvider.interceptors.push('httpInterceptor');
}]);

// Register top level module to Framework
portalApplication.registerModule('bpm-common');