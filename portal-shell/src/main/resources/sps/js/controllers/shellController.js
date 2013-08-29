/*
 * @author Subodh.Godbole
 */
define(['sps/js/shell'], function (shell) {
	'use strict';
	
	shell.module.controller('sg.shell.Controller',
			['$scope', '$resource', '$q', '$window', '$document', '$timeout', '$compile', '$controller', 'sgConfigService', 'sgNavigationService', 'sgViewPanelService',
			function ($scope, $resource, $q, $window, $document, $timeout, $compile, $controller, sgConfigService, sgNavigationService, sgViewPanelService) {

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
					fetchTheme(cfg.endpoints.theme + "/current").then(function(theme){
						$scope.theme = {};
						$scope.theme.scripts = theme.scripts;
						// TODO
						//$scope.theme.styleSheets = theme.stylesheets;
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
				});
				// ****************** Navigation Service - END ******************

		        // ****************** View Panel Service - START ******************
				// Expose some of methods on shell scope
				$scope.open = sgViewPanelService.open;
				$scope.close = sgViewPanelService.close;
		        $scope.viewPanels = sgViewPanelService.viewPanels;
		        $scope.viewPanelIndex = sgViewPanelService.viewPanelIndex;
		        $scope.activeViewPanel = sgViewPanelService.activeViewPanel;

		        $scope.tabs = [];

		        /*
		         * 
		         */
                $scope.watchMethodForTabs = function () {
                    var paths = "";
                    var panels = $scope.viewPanels();
                    for(var i in panels) {
                    	paths += panels[i].path + '|';
                    }
                    return paths;
                };

                /*
                 * 
                 */
                $scope.$watch('watchMethodForTabs()', function (newItem, oldItem) {
                	$scope.tabs = $scope.viewPanels();
                });

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
                	if (currentViewPanel) {
                		if (!currentViewPanel.$ctrl) {
//                			currentViewPanel.$scope = $scope.$new();
                			currentViewPanel.$ctrl = $controller(currentViewPanel.controller, {$scope: $scope});

//                			currentViewPanel.$scope.$parent.$on("destroy", function(){
//                				alert("Scope Destroyed!");
//                			});
//                			currentViewPanel.$ctrl = $controller(currentViewPanel.controller, {$scope: currentViewPanel.$scope});
                			
//                			var contentCompiler = $compile("<ng-include src=\"" + currentViewPanel.partial + "\"></ng-include><br/><b>{{$id}}</b>");
//                			var contentCompileElem = contentCompiler(currentViewPanel.$scope);
//
//                			var currentViewPanelIndex = $scope.viewPanelIndex(currentViewPanel);
//                			var viewPanels = jQuery(".view-panels");
//                			var viewPanel = viewPanels.children().get(currentViewPanelIndex);
//                			jQuery(jQuery(viewPanel).children().get(0)).append(contentCompileElem);
                		}
                		return currentViewPanel.$ctrl;
                	}
                };
		        // ****************** View Panel Service - END ******************
		        
				// ****************** Top Menu - START ******************
				$scope.topMenu = {
					toggle: function() {
						// TODO
					}	
				};
				// ****************** Top Menu - END ******************

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
					return $controller(currSidebar.controller, {$scope: $scope});
				};
				// ****************** Sidebar - END ******************

				// ****************** Utility Bar - START ******************
				/*
				 * 
				 */
				$scope.$watch("utilityItems", function(newVal, oldVal){
					if (newVal !== oldVal) {
						$timeout(function(){
							var utilityItems = angular.element(".utility-item");
							angular.forEach(utilityItems, function(utilItemElem){
								var utilItem = angular.element(utilItemElem);
								var childScope = utilItem.scope();
								var item = childScope.item;
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

								utilItem.removeAttr("ng-repeat");
								$compile(utilItem)(childScope);
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
	                $scope.shell.sizes.footerHeight = jQuery(".footer").outerHeight();
		            $scope.shell.sizes.windowHeight = angular.element($window).height();
		        }

		        $timeout(calculateShellSizes, 0);

		        angular.element($window).resize(function() {
		            if(resizeTimeoutId) {
		            	clearTimeout(resizeTimeoutId);
		            }
		            resizeTimeoutId = $timeout(calculateShellSizes, 300);
		        });
				// ****************** Window Resizing - END ******************
		}]);
});