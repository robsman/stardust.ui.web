/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Subodh.Godbole
 */

'use strict';

/*
 * 
 */
angular.module('bpm-ui').controller(
		'bpm-ui.SidebarCtrl',
		[
				'$scope',
				'$timeout',
				'sgSidebarStateService',
				'sdSidebarService',
				'sdViewUtilService',
				'sdUtilService',
				'sgPubSubService',
				function($scope, $timeout, sgSidebarStateService, sdSidebarService, sdViewUtilService, sdUtilService,
						sgPubSubService) {
					/*
					 * 
					 */
					function disableSidebarResizing() {
						// There is no API to disable resizing, so add
						// workaround
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
								// BridgeUtils is somehow not loaded. Very
								// unlikely situation. But then wait.
								window.setTimeout(function() {
									resizeAndRepositionAllActive(--hiddenCounter);
								}, 100);
							}
						}
					}

					// BridgeUtils not loaded at this point, so save these
					// handlers in root for later use
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

					// TODO: It's observed that title is not getting set by
					// Shell. So manually change it here!
					if ($scope.appConfigData && $scope.appConfigData.windowTitle) {
						document.title = $scope.appConfigData.windowTitle;
					}

					/*
					 * 
					 */
					sdSidebarService.initialize().then(function() {
						$scope.perspectives = sdSidebarService.getPerspectives();
						$scope.activePerspective = sdSidebarService.getActivePerspective();
						sgPubSubService.publish("sdActivePerspectiveChange");
					});

					/*
					 * 
					 */
					$scope.activatePerspective = function(perspective) {
						$scope.activePerspective = sdSidebarService.activatePerspective(perspective);
						sgPubSubService.publish("sdActivePerspectiveChange");
					}

					/*
					 * 
					 */
					$scope.openHelpLink = function() {
						if ($scope.activePerspective.helpUrl) {
							var helpUrl = sdUtilService.getRootUrl() + $scope.activePerspective.helpUrl;
							var helpDocWin = window.open(helpUrl, 'helpDoc',
									'scrollbars=1,status=1,toolbar=1,width=1024,height=768');
							helpDocWin.focus();
						}
						return false;
					}

					/*
					 * 
					 */
					$scope.watchForSidebarContents = function() {
						var launchPanelIframe = document.getElementById("portalLaunchPanels");
						if (launchPanelIframe) {
							return launchPanelIframe.offsetTop;
						}
						return 0;
					}

					$scope.$watch('watchForSidebarContents()', function(newValue, oldValue) {
						if (newValue != oldValue) {
							BridgeUtils.handleResize($scope.shell.sizes);
						}
					});

					// Open and Pin Sidebar upon initialization.
					// More delay is required between Open and Pin
					window.setTimeout(function() {
						// TODO: Temporary hack. Some how shell is not removing
						// these classes
						jQuery('body').removeClass('hidden');
						jQuery('body').removeClass('sg-theme');

						$scope.$root.openSidebar();
						window.setTimeout(function() {
							$scope.$root.pinSidebar();
							$scope.$apply();
							// Ideally this would not be needed, and should be
							// covered by sidebar events
							// But somehow 'sidebar pinned' event does not reach
							// BridgeUtils listener
							// So as a workaround fire resize iframe explicitly
							resizeAndRepositionAllActive();
						}, 900);
					}, 100);
				} ]);