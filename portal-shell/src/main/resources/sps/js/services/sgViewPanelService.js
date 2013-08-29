/*
 * @author Subodh.Godbole
 */
define(['sps/js/shell'], function (shell) {
    'use strict';

    shell.module.provider('sgViewPanelService', function () {
    	var self = this;
    	
    	/*
    	 * 
    	 */
    	self.setViewOpeningStrategy = function (fn) {
    		// Dummy Function just for compatibility.
    		// Only one strategy is supported
        };
        
        self.$get = ['$location', '$timeout', '$filter', '$route', '$routeParams', '$rootScope', 'sgNavigationService', /*'sdPubSubService',*/ 
                     function ($location, $timeout, $filter, $route, $routeParams, $rootScope, sgNavigationService/*, sdPubSubService*/) {

        	var service = {};
        	
            var activePanelPath;
            var tabs = [];

            /*
             * 
             */
            service.load = function(nav) {
            	// TODO: Initialization if any
            };

            /*
             * 
             */
            service.open = function (navPath, event, params) {
            	var navItem = findTabDetails(navPath);
            	if (!navItem) {            	
	            	var navItemTemp = sgNavigationService.findNavItem(navPath);
	        		if (navItemTemp) {
	        			navItem = {};
	        			angular.copy(navItemTemp, navItem);
	
	        			navItem.path = substituteParams(navItem.path, params);
	        			var existingNavItem = findTabDetails(navItem.path);
	        			if (existingNavItem) {
	        				navItem = existingNavItem;
	        			} else {
		        			navItem.label = substituteParams2(navItem.label, params);
		        			navItem.title = navItem.label;
		        			navItem.params = params;

	        				tabs.push(new ViewPanel(navItem));
	        			}
	        		}
            	}
            	
            	if (navItem) {
        			activePanelPath = navItem.path;
            	}
            };

            /*
             * 
             */
            service.close = function (navPath) {
            	var navItemDetails = findTabDetails(navPath, true);
            	if (navItemDetails.tab) {
            		tabs.splice(navItemDetails.index, 1);
        			activateNextViewPanel();
            	}
            };

            /*
             * 
             */
            service.viewPanels = function () {
                return tabs;
            };

            /*
             * 
             */
            service.viewPanelIndex = function (panel) {
            	var navItemDetails = findTabDetails(panel.path, true);
            	if (navItemDetails.tab) {
            		return navItemDetails.index;
            	} else {
            		return -1;
            	}
            };

            /*
             * 
             */
            service.activeViewPanel = function () {
            	return findTabDetails(activePanelPath);
            };

            /*
             * 
             */
            function activateNextViewPanel() {
            	if (!service.activeViewPanel()) {
	        		if (tabs.length > 0) {
	        			var activePanel = tabs[tabs.length - 1];
	        			activePanelPath = activePanel.path;
	        		} else {
	        			activePanelPath = undefined;
	        		}
            	}
            }

            /*
             * 
             */
            function findTabDetails(navPath, isFull) {
            	var ret = {};
            	for(var i = 0; i < tabs.length; i++) {
            		if (tabs[i].path == navPath) {
            			ret.tab = tabs[i];
            			ret.index = i;
            			break;
            		}
            	}
            	
            	if (isFull) {
            		return ret;            		
            	} else {
            		return ret.tab;
            	}
            }

            /*
             * 
             */
    		function substituteParams(str, params, onePass) {
    	        var tempStr = str;
    	        while (tempStr.indexOf(':') > -1) {
    	            var paramStr = tempStr.substring(tempStr.indexOf(':') + 1);
    	            var param = paramStr.indexOf('/') > -1 ? paramStr.substring(0, paramStr.indexOf('/')) : paramStr;
    	            var remainingStr = paramStr.substring(param.length);
    	            var paramValue = params[param];
    	            if (paramValue != undefined) {
    	            	str = str.substring(0, str.indexOf(':')) + paramValue + remainingStr;
    	            }
    	            tempStr = str;

    	            if (onePass){
    					break;
    				}
    	        }
    	        return str;
    	    }

    		/*
    		 * 
    		 */
    		function substituteParams2(str, params) {
    	        var tempStr = str;
    	        var paramsToReplace = [];
    	        while (tempStr.indexOf('{') > -1) {
    	            var paramStr = tempStr.substring(tempStr.indexOf('{') + 1);
    	            var param = paramStr.indexOf('}') > -1 ? paramStr.substring(0, paramStr.indexOf('}')) : paramStr;
    	            paramsToReplace.push(param);
    	            tempStr = paramStr.substring(param.length + 1);
    	        }
    	        
    	        for(var i in paramsToReplace) {
    	        	var param = paramsToReplace[i];
    	            var paramValue = params[param];
    	            if (paramValue != undefined) {
    	            	str = str.replace("{" + param + "}", paramValue);
    	            }
    	        }
    	        return str;
    	    }

        	return service;
        }];

        /*
         * 
         */
        function ViewPanel(viewDef, parameters) {
            angular.extend(this, viewDef);

            this.title = viewDef.label;

            /*
             * 
             */
            this.setTitle = function (title) {
            	if (angular.isString(title)) {
            		this.title = title;
            	}
            };

            this.resetTitle = function () {
                this.title = this.label;
            };

            this.setIcon = function (icon) {
                if (angular.isString(icon)) {
                    this.icon = icon;
                }
            };

            return this;
        }
    });
});