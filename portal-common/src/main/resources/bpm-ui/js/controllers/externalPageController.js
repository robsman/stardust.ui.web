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
		log("External Page Controller. View = " + view);
		if (view) {
			$scope.iframeUrl = view.externalURL;
			if (view.params) {
				$scope.iframeUrl = BridgeUtils.substituteParams($scope.iframeUrl, view.params, true);
			} else {
				view.params = [];
			}

			$scope.iframeId = "Frame" + Math.floor((Math.random()*100000) + 1);
			view.params["iframeId"] = $scope.iframeId;

			$scope.label = view.label;

			// In IE It's observed that url is hit twice. Add a workaround to get around this
			// As this causes issues on server side due to two duplicate requests
			// This might be Angular / HTML5 Framework issue
			if (BridgeUtils.Util.isIE()) {
				$scope.iframeUrl2 = $scope.iframeUrl;
				$scope.iframeUrl = "about:blank";
	
				// Execute as soon as possible. No specific delay
				window.setTimeout(function(){
					var frame = window.document.getElementById($scope.iframeId);
					frame.setAttribute("src", $scope.iframeUrl2);
				});
			}
		} else {
			log("View is null or it does not have Params. Something is incorrect. View = " + view);
		}
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