/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

/*
 * @author Subodh.Godbole
 */
'use strict';

angular.module('shell.services').provider('sgViewPanelService', function () {
	var self = this;
	
	/*
	 * 
	 */
	self.setViewOpeningStrategy = function (fn) {
		// Dummy Function just for compatibility.
		// Only one strategy is supported
    };
    
    self.$get = ['$location', '$timeout', '$filter', '$route', '$routeParams', '$rootScope', 'sgNavigationService', 'sgPubSubService', 
                 function ($location, $timeout, $filter, $route, $routeParams, $rootScope, sgNavigationService, sgPubSubService) {

    	var service = {};
    	
        var activePanelPath;
        var tabs = [];
        
        var MAX_TABS = 4; // TODO
        var displayTabs = [];
        var overflowTabs = [];

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

	        			navItem = new ViewPanel(navItem);
        				tabs.push(navItem);
        			}
        		}
        	}
        	
        	if (navItem) {
        		activateNextViewPanel(navItem);
        		
        		if (getIndex(displayTabs, navItem) == -1) {
        			displayTabs.push(navItem);
        			
        			if (MAX_TABS < displayTabs.length) {
        				displayTabs.splice(0, 1);
        			}
        			
        			buildOverflowTabs();
        		}
        	}
        };

        /*
         * 
         */
        service.close = function (navPath) {
        	var navItemDetails = findTabDetails(navPath, true);
        	if (navItemDetails.tab) {
        		var ret = sgPubSubService.publish('sgViewPanelCloseIntent', {viewPanel: navItemDetails.tab});
        		if (ret) {
        			$timeout(function() {
        				// Find nav details (position) again
        				navItemDetails = findTabDetails(navPath, true);
        				if (navItemDetails.tab) {
    	            		tabs.splice(navItemDetails.index, 1);
    	        			activateNextViewPanel();
    	        			
    	        			var index = getIndex(displayTabs, navItemDetails.tab);
    	        			displayTabs.splice(index, 1);
    	        			
    	        			for(var i = 0; i < tabs.length; i++) {
                        		if (getIndex(displayTabs, tabs[i]) == -1) {
                        			displayTabs.push(tabs[i]);
                        			break;
                        		}
                        	}

    	        			buildOverflowTabs();
        				}
        			});
        		}
        	}
        };

        /*
         * 
         */
        service.displayTabs = function() {
        	return displayTabs;
        }

        /*
         * 
         */
        service.overflowTabs = function() {
        	return overflowTabs;
        }

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
        function activateNextViewPanel(navItem) {
        	var activePanel;
        	if (navItem != undefined) {
        		activePanelPath = navItem.path;
        		activePanel = navItem;
        	} else if (!service.activeViewPanel()) {
        		// Activate Last Tab
        		if (tabs.length > 0) {
        			activePanel = tabs[tabs.length - 1];
        			activePanelPath = activePanel.path;
        		} else {
        			activePanelPath = undefined;
        			activePanel = null;
        		}
        	}

        	if (activePanel) {
        		sgPubSubService.publish('sgActiveViewPanelChanged', {currentNavItem: activePanel});
        	}
        }

        /*
         * 
         */
        function buildOverflowTabs() {
			overflowTabs = [];
        	for(var i = 0; i < tabs.length; i++) {
        		if (getIndex(displayTabs, tabs[i]) == -1) {
        			overflowTabs.push(tabs[i]);
        		}
        	}
        }

        /*
         * 
         */
        function getIndex(tabs, tab) {
        	for(var i = 0; i < tabs.length; i++) {
        		if (tabs[i].path === tab.path) {
        			return i;
        		}
        	}
        	return -1;
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