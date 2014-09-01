/*
 * 
 */
'use strict';

/*
 * 
 */
angular.module('bpm-ui').controller('bpm-ui.ExternalPageCtrl', ['$scope', function($scope) {
	var view = $scope.activeViewPanel();
	log("External Page Controller. View = " + view);
	if (view) {
		var iframeUrl = view.externalURL;
		if (view.params) {
			iframeUrl = BridgeUtils.substituteParams(iframeUrl, view.params, true);
		} else {
			view.params = [];
		}

		view.params["iframeUrl"] = iframeUrl;
		view.params["iframeId"] = "Frame" + Math.floor((Math.random()*100000) + 1);
		
		// In IE It's observed that url is hit twice. Add a workaround to get around this
		// As this causes issues on server side due to two duplicate requests
		// This might be Angular / HTML5 Framework issue
		if (BridgeUtils.Util.isIE()) {
			view.params["iframeUrl2"] = view.params["iframeUrl"];
			view.params["iframeUrl"] = "about:blank";

			// Execute as soon as possible. No specific delay
			window.setTimeout(function(){
				var frame = window.document.getElementById(view.params["iframeId"]);
				frame.setAttribute("src", view.params["iframeUrl2"]);
			});
		}
	} else {
		log("View is null or it does not have Params. Something is incorrect. View = " + view);
	}
	
	/*
	 * 
	 */
	function log(msg) {
		if (window.BridgeUtils) {
			window.BridgeUtils.log(msg);
		}
	}
}]);