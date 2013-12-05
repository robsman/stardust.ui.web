/*
 * @author Subodh.Godbole
 */
define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

    /*
     * 
     */
	bpmUi.module.controller('bpm-ui.InternalPageCtrl', ['$scope', function($scope) {
	}]);

	/*
	 * 
	 */
	function log(msg) {
		if (window.BridgeUtils) {
			window.BridgeUtils.log(msg);
		}
	}
});