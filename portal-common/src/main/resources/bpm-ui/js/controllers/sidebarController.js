/**
 * @author Subodh.Godbole
 */

define(['bpm-ui/js/bpm-ui'], function (bpmUi) {
	'use strict';

	/*
	 * 
	 */
	bpmUi.module.controller('bpm-ui.SidebarCtrl', ['$scope', '$timeout', 'sgSidebarStateService', function($scope, $timeout, sgSidebarStateService) {
		/*
		 *
		 */
		function disableSidebarResizing() {
			// There is no API to disable resizing, so add workaround
			var sidebarResizeHandle = jQuery('.sg-sidebar-resize');
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
		$scope.$root.openSidebar = function() {
			sgSidebarStateService.openSidebar('main');
		}
		
		$scope.$root.closeSidebar = function() {
			sgSidebarStateService.closeSidebar('main');
		}

		$scope.$root.pinSidebar = function() {
			sgSidebarStateService.pinSidebar('main');
		}
		
		$scope.$root.unpinSidebar = function() {
			sgSidebarStateService.unpinSidebar('main');
		}
		
		$scope.$root.getSidebarDetails = sgSidebarStateService.getSidebarDetails;
		
		$scope.$root.$timeout = $timeout;

		$scope.$watch('sidebar.position', function(newValue) {
			disableSidebarResizing();
		}, true);

		// Open and Pin Sidebar upon initialization.
		// More delay is required between Open and Pin
		window.setTimeout(function(){
		  // TODO: Temporary hack. Some how shell is not removing these classes
		  jQuery('body').removeClass('hidden');
      jQuery('body').removeClass('sg-theme');

			$scope.$root.openSidebar();
			window.setTimeout(function(){
				$scope.$root.pinSidebar();
				$scope.$apply();
				// Ideally this would not be needed, and should be covered by sidebar events
				// But somehow 'sidebar pinned' event does not reach BridgeUtils listener
				// So as a workaround fire resize iframe explicitly
				resizeAndRepositionAllActive();
			}, 900);
		}, 100);
	}]);
});