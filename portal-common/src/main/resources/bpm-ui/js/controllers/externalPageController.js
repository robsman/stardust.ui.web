/*
 *
 */
define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * Copied from HTML5 and modified a bit
	 */
	function substituteParams(path, localParams, onePass) {
        var tempPath = path;
        while (tempPath.indexOf(':') > -1) {
            var paramStr = tempPath.substring(tempPath.indexOf(':') + 1);
            var param = paramStr.indexOf('/') > -1 ? paramStr.substring(0, paramStr.indexOf('/')) : paramStr;
            var remainingStr = paramStr.substring(param.length);
            var paramValue = localParams[param];
            if (paramValue != undefined) {
		path = path.substring(0, path.indexOf(':')) + paramValue + remainingStr;
            }
            tempPath = path;

            if (onePass){
				break;
			}
        }
        return path;
    }

    /*
     *
     */
	bpmUi.module.controller('bpm-ui.ExternalPageCtrl', ['$scope', function($scope) {
		var view = $scope.activeViewPanel();
		if (view) {
			$scope.iframeUrl = view.externalURL;
			if (view.params) {
				$scope.iframeUrl = substituteParams($scope.iframeUrl, view.params, true);
				if (view.icon && view.icon != "") {
					$scope.setIcon(substituteParams(view.icon, view.params, true));
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