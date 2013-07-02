/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * @class
	 * @name bpm-ui.LoginController
	 * @description controller for login and log out
	 * @namespace controllers
	 */
	bpmUi.module.controller('bpm-ui.BpmUiController', ['$scope', function($scope) {

		/*
		 *
		 */
		$scope.logout = function() {
			BridgeUtils.logout();
		}

		/*
		 *
		 */
		$scope.openAllProcessMgmtViews = function() {
			parent.BridgeUtils.openView(processOverviewView);
			parent.BridgeUtils.openView(processSearch);
		}

	}]);

});