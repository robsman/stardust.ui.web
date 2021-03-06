/*
 * 
 */
	'use strict';
	
angular.module('shell').controller('sg.shell.Controller',
			['$scope', '$resource', '$q', '$window', '$document', '$timeout', '$compile', '$controller', 'sgConfigService', 'sgNavigationService', 'sgViewPanelService', 'sgPubSubService', 'sgSidebarStateService', 'sgI18nService',
			function ($scope, $resource, $q, $window, $document, $timeout, $compile, $controller, sgConfigService, sgNavigationService, sgViewPanelService, sgPubSubService, sgSidebarStateService, sgI18nService) {

				// ****************** Config Service - START ******************
				/*
				 * 
				 */
				function fetchTheme(endpoint, params) {
					var deferred = $q.defer();
					$resource(endpoint).get(params,
						function (results) {
							deferred.resolve(results);
						},
						function () {
							deferred.reject('Error in loading theme from ' + endpoint);
						}
					);
					return deferred.promise;
				}

				var deferred = $q.defer();
				$scope.config = sgConfigService;
				$scope.config.then(function (cfg){
				$scope.sidebarWidth = cfg.sidebar.width + "px";

					fetchTheme(cfg.endpoints.theme + "/current").then(function(theme){
						$scope.theme = {};
						$scope.theme.scripts = theme.scripts;
						$scope.theme.styleSheets = theme.stylesheets;
					});
				});
				// ****************** Config Service - END ******************

				// ****************** Navigation Service - START ******************
		        // Wait on Navigation Service Promise for further processing
				sgNavigationService.load().then(function(nav) {
					// Initialize View Panels
					sgViewPanelService.load(nav);
					
					// Setup Sidebar
					$scope.sidebars = nav.sidebar;

					// Setup Utility Menu
					$scope.utilityItems = nav.utility;
					
					// Setup Footer Items
					$scope.footerItems = nav.footer;
					
					// Top Menu
					$scope.topMenu = {
						visible: false,
						items: sgNavigationService.getTopMenuItems(),
						toggle: function() {
							$scope.topMenu.visible = !$scope.topMenu.visible; 
						},
						close: function() {
							if ($scope.topMenu.visible) {
								$scope.topMenu.visible = false;
							}
						},
						openItem: function(path, event) {
							$scope.topMenu.toggle();
							$scope.open(path, event);
						}
					};
				});
				
				$scope.topMenu = {};
				// ****************** Navigation Service - END ******************

		        // ****************** View Panel Service - START ******************
				// Expose some of methods on shell scope
				$scope.open = sgViewPanelService.open;
				$scope.close = sgViewPanelService.close;
		        $scope.viewPanels = sgViewPanelService.viewPanels;
		        $scope.viewPanelIndex = sgViewPanelService.viewPanelIndex;
		        $scope.activeViewPanel = sgViewPanelService.activeViewPanel;

		        // For Rendering Tabs
		        $scope.tabs = [];
		        $scope.overflowTabs = {
		        	visible: false,
		        	tabs: [],
		        	toggle: function() {
		        		$scope.overflowTabs.visible = !$scope.overflowTabs.visible; 
					},
		        	close: function() {
		        		if ($scope.overflowTabs.visible) {
		        			$scope.overflowTabs.visible = false;
		        		}
					},
					openView: function(path, event) {
		        		$scope.overflowTabs.toggle();
						$scope.open(path, event);
		        	}
		        };

		        /*
		         * 
		         */
                $scope.watchMethodForTabs = function () {
                    var paths = "";
                    var panels = $scope.viewPanels();
                    for(var i in panels) {
                    	paths += panels[i].path + '|';
                    }
                    
                    var activePanel = $scope.activeViewPanel();
                    if (activePanel) {
                    	paths += activePanel.path;
                    }

                    return paths;
                };

                /*
                 * 
                 */
                $scope.$watch('watchMethodForTabs()', function (newItem, oldItem) {
                	$scope.tabs = sgViewPanelService.displayTabs();
                	$scope.overflowTabs.tabs = sgViewPanelService.overflowTabs();
                });
                
                // For Rendering View Contents
                $scope.panels = [];
                
                /*
                 * 
                 */
                $scope.watchMethodForActivePanel = function () {
                    var ret = "";
                    var panel = $scope.activeViewPanel();
                    if (panel) {
                    	ret = panel.path;
                    }
                    return ret;
                };

                /*
                 * 
                 */
                $scope.$watch('watchMethodForActivePanel()', function (newItem, oldItem) {
                	$scope.panels = $scope.viewPanels();
                });

                var currentViewPanel;

                /*
                 * 
                 */
                $scope.viewPanelCheck = function(panel) {
                	currentViewPanel = panel;
                	return true;
                };
                
                /*
                 * 
                 */                
                $scope.viewPanelCtrl = function() {
                	if (currentViewPanel && currentViewPanel.controller) {
                		if (!currentViewPanel.$ctrl) {
                			currentViewPanel.$ctrl = $controller(currentViewPanel.controller, {$scope: $scope});
                		}
                		return currentViewPanel.$ctrl;
                	}
                };
		        // ****************** View Panel Service - END ******************

				// ****************** I18n - START ******************
                sgI18nService.load();
				// ****************** I18n - START ******************

				// ****************** Sidebar - START ******************
				var currSidebar;
				/*
				 * 
				 */
				$scope.sidebarCheck = function(sidebar) {
					currSidebar = sidebar;
					return true;
				};

				/*
				 * 
				 */
				$scope.sidebarCtrl = function() {
					if (currSidebar && currSidebar.controller) {
						if (!currSidebar.$ctrl) {
							currSidebar.$ctrl = $controller(currSidebar.controller, {$scope: $scope});
						}
						return currSidebar.$ctrl;
					}
				};
				
				$scope.sidebar = sgSidebarStateService.sidebar;

				/*
				 * 
				 */
				$scope.getSidebarMargin = function() {
					if ($scope.sidebar && $scope.sidebar.pinned) {
						return $scope.sidebarWidth;
					} else {
						return "0px;"
					}
				}

				// ****************** Sidebar - END ******************

				// ****************** Utility Bar - START ******************
				/*
				 * 
				 */
				$scope.$watch("utilityItems", function(newVal, oldVal){
					if (newVal !== oldVal) {
						$timeout(function(){
							var utilityItems = angular.element(".app-header .utility-item");
							angular.forEach(utilityItems, function(utilItemElem){
								var utilItem = angular.element(utilItemElem);
								var utilLoopScope = utilItem.scope();
								var item = utilLoopScope.item;
								if (item.actionController) {
									utilItem.attr("ng-controller", item.actionController);
									utilItem.attr("ng-click", item.action);
									utilItem.addClass("utility-link");
								}

								if (item.cls) {
									utilItem.addClass(item.cls);
								}

								if (item.id) {
									utilItem.attr("id", item.id);
								}

								// This needs to be removed, to avoid double repeating when $compile is applied
								utilItem.removeAttr("ng-repeat");

								$compile(utilItem)(utilLoopScope);
							});
						});
					}
				});
				// ****************** Utility Bar - END ******************

				// ****************** Window Resizing - START ******************
				var resizeTimeoutId;
		        $scope.shell = {
		        	sizes: {}
		        };

		        /*
		         * 
		         */
		        function calculateShellSizes() {
		            resizeTimeoutId = null;
		            var footerElem = jQuery(".footer");
	                $scope.shell.sizes.footerHeight = footerElem.outerHeight() + getAbsoluteSize(footerElem.css("marginTop"));
		            $scope.shell.sizes.windowHeight = angular.element($window).height();
		        }

				/*
				 *
				 */
				function getAbsoluteSize(size) {
					if (size) {
						if (size.indexOf('px') != -1) {
							size = size.substr(0, size.indexOf('px'));
						}
	
						return parseInt(size);
					}
					
					return 0;
				}

		        // Initially Calculate sizes
				// Have few more attempts, on first attempt footer height is not calculated correctly
				$timeout(calculateShellSizes, 0);
				angular.element(document).ready(function() {
			        $timeout(calculateShellSizes, 500);
			        $timeout(calculateShellSizes, 500);
			        $timeout(calculateShellSizes, 500);
			        $timeout(calculateShellSizes, 500);
		        });
		        
		        // Register for window resize, and re-calculate sizes
		        angular.element($window).resize(function() {
		            if(resizeTimeoutId) {
		            	clearTimeout(resizeTimeoutId);
		            }
		            resizeTimeoutId = $timeout(calculateShellSizes, 300);
		        });
				// ****************** Window Resizing - END ******************
	}]);