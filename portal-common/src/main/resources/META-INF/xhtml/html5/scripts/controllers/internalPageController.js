/*
 * @author Subodh.Godbole
 */
'use strict';

/*
 * 
 */
angular.module('bpm-ui').controller('bpm-ui.InternalPageCtrl', ['$scope', function($scope) {

	/*
	 * 
	 */
	function log(msg, type) {
		if (window.BridgeUtils) {
			window.BridgeUtils.log(msg, type);
		}
	}
}]);