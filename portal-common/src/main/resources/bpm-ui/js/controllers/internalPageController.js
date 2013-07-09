/*
 * @author Subodh.Godbole
 */
define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

    /*
     * 
     */
	bpmUi.module.controller('bpm-ui.InternalPageCtrl', ['$scope', function($scope) {
		var view = $scope.activeViewPanel();
		if (view) {
			if (view.params) {
				if (view.iconBase && view.iconBase != "") {
					$scope.setIcon(BridgeUtils.substituteParams(view.iconBase, view.params, true));
				}
			}
		} else {
			if (console) {
				console.debug("View is null or it does not have Params. Something is incorrect. View = " + view);
			}
		}
	}]);
});