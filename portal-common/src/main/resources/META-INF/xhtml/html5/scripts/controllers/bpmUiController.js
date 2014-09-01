/**
 * @author Subodh.Godbole
 */

'use strict';

/*
 * 
 */
angular.module('bpm-ui').controller('bpm-ui.BpmUiCtrl', ['$scope', function($scope) {
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
	$scope.showHideAlerts = function() {
		BridgeUtils.showHideAlertNotifications();
	}
	
	/*
	 * 
	 */
	$scope.$watch('shell.sizes', function(sizes) {
        if(sizes !== {}) {
        	doResizing(sizes);
        }
    }, true);
}]);