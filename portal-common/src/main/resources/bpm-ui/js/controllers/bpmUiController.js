/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * 
	 */
	bpmUi.module.controller('bpm-ui.BpmUiCtrl', ['$scope', 'sgPubSubService', function($scope, sgPubSubService) {

		/*
		 *
		 */
		function doResizing(sizes) {
        	if (window.BridgeUtils) {
        		BridgeUtils.handleResize(sizes);
        	} else {
        		// Ugly Hack?
        		window.setTimeout(function() {
        			doResizing(sizes);
        		}, 200);
        	}			
		}
		
		/*
		 * 
		 */
		$scope.logout = function() {
			BridgeUtils.logout();
		}

		/*
		 *
		 */
		$scope.$watch('shell.sizes', function(sizes) {
            if(sizes !== {}) {
            	doResizing(sizes);
            }
        }, true);

		// BridgeUtils not loaded at this point, so save these handlers in root for later use
		$scope.$root.openSidebar = $scope.openSidebar;
		$scope.$root.closeSidebar = $scope.closeSidebar;
		$scope.$root.pinSidebar = $scope.pinSidebar;
		$scope.$root.unpinSidebar = $scope.unpinSidebar;
	}]);
});