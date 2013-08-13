/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * 
	 */
	bpmUi.module.controller('bpm-ui.SidebarCtrl', ['$scope', function($scope) {
		// BridgeUtils not loaded at this point, so save these handlers in root for later use
		$scope.$root.openSidebar = $scope.openSidebar;
		$scope.$root.closeSidebar = $scope.closeSidebar;
		$scope.$root.pinSidebar = $scope.pinSidebar;
		$scope.$root.unpinSidebar = $scope.unpinSidebar;
	}]);
});