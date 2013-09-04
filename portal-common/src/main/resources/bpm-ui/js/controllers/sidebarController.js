/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * 
	 */
	bpmUi.module.controller('bpm-ui.SidebarCtrl', ['$scope', function($scope) {
		/*
		 *
		 */
		function disableSidebarResizing() {
			// There is no API to disable resizing, so add workaround
			var sidebarResizeHandle = jQuery('.sidebarResizeHandle');
			if (sidebarResizeHandle) {
				sidebarResizeHandle.off();
			}
		}

		/*
		 * 
		 */
		function resizeAndRepositionAllActive(hiddenCounter) {
			if (window.BridgeUtils) {
				BridgeUtils.FrameManager.resizeAndRepositionAllActive();
			} else {
				if (hiddenCounter == undefined) {
					hiddenCounter = 4; // Max Iteration Count
				}
				
				if (hiddenCounter > 0) {
					// BridgeUtils is somehow not loaded. Very unlikely situation. But then wait.
					window.setTimeout(function(){
						resizeAndRepositionAllActive(--hiddenCounter);
					}, 100);
				}
			}
		}

		// BridgeUtils not loaded at this point, so save these handlers in root for later use
		$scope.$root.openSidebar = $scope.openSidebar;
		$scope.$root.closeSidebar = $scope.closeSidebar;
		$scope.$root.pinSidebar = $scope.pinSidebar;
		$scope.$root.unpinSidebar = $scope.unpinSidebar;
		$scope.$root.getSidebarDetails = $scope.getSidebarDetails;

		$scope.$watch('sidebar.position', function(newValue) {
			disableSidebarResizing();
		}, true);

		// Open and Pin Sidebar upon initialization.
		// More delay is required between Open and Pin
		window.setTimeout(function(){
			$scope.openSidebar();
			window.setTimeout(function(){
				$scope.pinSidebar();
				// Ideally this would not be needed, and should be covered by sidebar events
				// But somehow 'sidebar pinned' event does not reach BridgeUtils listener
				// So as a workaround fire resize iframe explicitly
				resizeAndRepositionAllActive();
			}, 900);
		}, 100);
	}]);
});