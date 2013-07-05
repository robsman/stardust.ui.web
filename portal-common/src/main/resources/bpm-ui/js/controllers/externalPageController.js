/*
 *
 */
define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

    /*
     *
     */
	bpmUi.module.controller('bpm-ui.ExternalPageCtrl', ['$scope', function($scope) {
		var view = $scope.activeViewPanel();
		if (view) {
			$scope.iframeUrl = view.externalURL;
			if (view.params) {
				$scope.iframeUrl = BridgeUtils.substituteParams($scope.iframeUrl, view.params, true);
				if (view.iconBase && view.iconBase != "") {
					$scope.setIcon(BridgeUtils.substituteParams(view.iconBase, view.params, true));
				}
			} else {
				view.params = [];
			}

			$scope.iframeId = "Frame" + Math.floor((Math.random()*100) + 1);
			view.params["iframeId"] = $scope.iframeId;

			$scope.label = view.label;
		} else {
			if (console) {
				console.debug("View is null or it does not have Params. Something is incorrect. View = " + view);
			}
		}
	}]);
});