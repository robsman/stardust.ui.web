/**
 * @author Subodh.Godbole
 */

'use strict';

var bpmUiAMod = angular.module('bpm-ui', ['bpm-ui.services', 'shell', 'bpm-ui.init']);
var bpmUiServicesAMod = angular.module('bpm-ui.services', []);
bpmUiAMod.config([
		'$routeProvider', '$controllerProvider', '$compileProvider', '$filterProvider', '$provide',
		'sgViewPanelServiceProvider',
		function($routeProvider, $controllerProvider, $compileProvider, $filterProvider, $provide, 
					sgViewPanelServiceProvider) {
			
			// These are to be used to define modules after Angular is initialized 
			bpmUiAMod.providers = {};
			
			bpmUiAMod.providers.controllerProvider = $controllerProvider;
			bpmUiAMod.providers.compileProvider = $compileProvider;
			bpmUiAMod.providers.routeProvider = $routeProvider;
			bpmUiAMod.providers.filterProvider = $filterProvider;
			bpmUiAMod.providers.provide = $provide;
			
			sgViewPanelServiceProvider.setViewOpeningStrategy('mdi');
		}
]);

angular.module('bpm-ui.init', []) .
	run(['$log', '$rootScope', '$window', 'sgPubSubService', function ($log, $rootScope, $window, sgPubSubService) {
		// execute here the code that needs to be executed before the app starts.
		$log.log('- BPM UI started!');
		$rootScope.$on('sgLoginRequired', function (event) {
			var href = $window.location.href.substr(0, window.location.href.indexOf('#'));
			$window.location.replace(href);
		});

		// sgPubSubService is required in BridgeUtils. But BridgeUtils is not loaded at this point
		// Also BridgeUtils runs is non Angular Context, so save data in root
		$rootScope.sgPubSubService = sgPubSubService;
}]);