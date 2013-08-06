/**
 * Bridging for HTML5 Framework
 *
 * @author Subodh.Godbole
 */

if (!window["BridgeUtils"]) {
	BridgeUtils = new function() {
		var scriptRunning;

		/*
		 * type: i = Info, w = Warning, e = Error, d or undefined = Debug
		 */
		function log(str, type) {
			if (console) {
				if (type != undefined && type != 'd') {
					if (type == 'i') {
						console.info(str);
					}
					else if (type == 'w') {
						console.warn(str);
					}
					else if (type == 'e') {
						console.error(str);
					}
				} else {
					console.log("* " + str);
				}
			}
		}

		/*
		 * 
		 */
		function initIframe(win) {
			log("Initializing iframe " + win.location);

			log("Listening for session expiry");
			win.Ice.onSessionExpired('document:body', function() {
	        	parent.BridgeUtils.handleServerDisconnect("SessionExpired");
	        });

			log("Listening for Connection Trouble");
			win.Ice.onConnectionTrouble('document:body', function() {
	        	parent.BridgeUtils.handleServerDisconnect("ConnectionTrouble");
	        });

			log("Listening for Connection Lost");
			win.Ice.onConnectionLost('document:body', function() {
	        	parent.BridgeUtils.handleServerDisconnect("ConnectionLost");
	        });

			// Main View
			if (win.location.href.indexOf("/plugins/common/portalSingleViewMain.") > -1) {
				win.onscroll = function(event){
					BridgeUtils.FrameManager.handleViewScroll(event.currentTarget);
				};
			}
		}

		/*
		 *
		 */
		function runInAngularContext(func) {
			// TODO document.body?
			var scope = angular.element(document.body).scope();
			scope.$apply(func);
		}

		/*
		 *
		 */
		function runScript(func, random) {
			scriptRunning = true;
			// TODO: Set cursor to wait
			log("## scriptRunning = " + scriptRunning, "w");

			try {
				var scriptStr = func.toString();
				scriptStr = scriptStr.replace(/try{/g, '\ntry{');
				log("Run Script = " + scriptStr, "i");
			} catch(e) {
				log("## Error in logging Run Script Function." + e, "e");
			}

			try {
				func();
			} catch(ex) {
				log("## Error while running script." + ex, "e");
			}
			
			scriptRunning = false;
			// TODO: Set cursor to normal
			log("## scriptRunning = " + scriptRunning, "w");
		}

		/*
		 *
		 */
		function isScriptRunning() {
			return scriptRunning;
		}

		/*
		 *
		 */
		function handleServerDisconnect(type) {
		log("Handling Server Disconnect for " + type);
	    	BridgeUtils.FrameManager.forceCloseAll();
		runInAngularContext(function($scope) {
	    		BridgeUtils.logout(true);
			});
		log("Handled Server Disconnect for " + type);
	    }

		/*
		 *
		 */
		function logout(force) {
			log("Logging out = " + force);
			if (force == undefined || !force) {
				BridgeUtils.View.doPartialSubmit("modelerLaunchPanels", "viewFormLP", "logout", Math.floor(Math.random()*10000)+1);
			} else {
				// Wait for some time so that iFrame will be removed
				window.setTimeout(function(){
					var href = window.location.href.substr(0, window.location.href.indexOf('/main.html'));
					href += '/ipp/common/ippPortalLogout.jsp';
					window.location.replace(href);
				}, 800);
			}
		}

		/*
		 *
		 */
		function getAbsoluteSize(size) {
			if (size.indexOf('px') != -1) {
				size = size.substr(0, size.indexOf('px'));
			}

			return parseInt(size);
		}

		/*
		 * Copied from HTML5 Framework and modified a bit
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
		function showAlert(msg) {
			alert(msg);
		}

		/*
		 * Called from Angular Controller
		 */
		function handleResize(shellSizes) {
			// Because it's called from Angular, wait for digest to get over. TODO: Find better solution
			window.setTimeout(function() {
				// Sidebar: Resize Launch Panels iFrame
	            var elem = document.getElementById("modelerLaunchPanels");
	            if (elem) {
		            if(elem.offsetTop < shellSizes.windowHeight) {
		            	var pos = BridgeUtils.FrameManager.findPosition(elem);
		            	// Subtracting 16 because sometimes offset returns an incorrect value.
		            	// Subtracting 20 more.
		            	var height = shellSizes.windowHeight - pos.y - shellSizes.footerHeight - 16 - 20;
		            	elem.style.height = height + "px";
		            }
	            } else {
	            	// Ugly hack?
	            	window.setTimeout(function() {
	            		handleResize(shellSizes);
	            	}, 200);
	            }

	            // Resize/Reposition iFrames
	            BridgeUtils.FrameManager.resizeAndRepositionAllActive();
			}, 100);
		}

		/*
		 * 
		 */
		function getContextRoot() {
			return location.href.substring(0, location.href.indexOf("/main.html"));
		}

		/*
		 * 
		 */
		function showHideAlertNotifications() {
			var id = "alerts";
			var found = false;

			BridgeUtils.FrameManager.doWithContentFrame(id, function(contentFrame) {
				found = true;
			});

			if (found) {
				hideAlertNotifications();
			} else {
				showAlertNotifications();
			}
		}

		/*
		 * 
		 */
		function showAlertNotifications() {
			var id = "alerts";
			var url = "/plugins/common/portalSingleViewAlerts.iface";
			var fullUrl = BridgeUtils.getContextRoot() + url;
			var advanceArgs = {
					anchorId: 'jQuery_div.navbar-inner .sg-controls i.bell',
					zIndex: 1000, width: 300, height: 200, openOnRight: false,
					anchorXAdjustment: 50, anchorYAdjustment: 20,
					noUnloadWarning: 'true', border: '1px solid black'
			};

			BridgeUtils.FrameManager.createOrActivate(id, fullUrl, advanceArgs);
		}

		/*
		 * 
		 */
		function hideAlertNotifications() {
			var id = "alerts";
			BridgeUtils.FrameManager.close(id);
		}

		return {
			log : log,
			initIframe : initIframe,
			runInAngularContext : runInAngularContext,
			runScript : runScript,
			isScriptRunning : isScriptRunning,
			handleServerDisconnect : handleServerDisconnect,
			logout : logout,
			getAbsoluteSize : getAbsoluteSize,
			substituteParams : substituteParams,
			showAlert : showAlert,
			handleResize : handleResize,
			getContextRoot : getContextRoot,
			showHideAlertNotifications : showHideAlertNotifications,
			showAlertNotifications : showAlertNotifications,
			hideAlertNotifications : hideAlertNotifications
		}
	};
} // !BridgeUtils

if (!window["BridgeUtils"].View) {
	BridgeUtils.View = new function() {

		var unsubscribers = [];

		/*
		 *
		 */
		function init() {
			var initialized = false;
			try {
				BridgeUtils.log("BridgeUtils.View Initializing");
	
				var sgPubSubService;
				BridgeUtils.runInAngularContext(function($scope) {
					sgPubSubService = $scope.$root.sgPubSubService;
	
					$scope.$root.$on('$destroy', function() {
						BridgeUtils.View.destroy();
			        });
				});
	
				if(sgPubSubService) {
					initialized = true;

					BridgeUtils.log("Subscribing to View Events");
					unsubscribers.push(sgPubSubService.subscribe('sgActiveViewPanelChanged', viewChanged));
					unsubscribers.push(sgPubSubService.subscribe('sgViewPanelCloseIntent', viewClosing));
		
					BridgeUtils.log("Subscribing to Sidebar Events");
					unsubscribers.push(sgPubSubService.subscribe('sgSidebarVisibilityChanged', sidebarVisibilityChanged));
					unsubscribers.push(sgPubSubService.subscribe('sgSidebarPinStateChanged', sidebarPinStateChanged));
		
					BridgeUtils.log("BridgeUtils.View Initialized Successfully");
				}
			} catch(e) {
				BridgeUtils.log("BridgeUtils.View Initialization Failed. " + e, "e");
			}
			
			if (!initialized){
				BridgeUtils.log("BridgeUtils.View Initialization Delaying");
				window.setTimeout(function(){
					init();
				}, 200);
			}
		}

		/*
		 *
		 */
		function destroy() {
			BridgeUtils.log("BridgeUtils.View Destroying");
			for (var i = 0; i < unsubscribers.length; i++) {
				BridgeUtils.log("Destroying: " + unsubscribers[i]);
				unsubscribers[i]();
			}
			BridgeUtils.log("BridgeUtils.View Destroyed Successfully");
		}

		/*
		 *
		 */
		function openView(force, html5FWViewId, viewId, params) {
			BridgeUtils.log("Opening View = " + viewId);
			BridgeUtils.runInAngularContext(function($scope) {
				
				var view = getViewPanel($scope, html5FWViewId);
				
				// If View not there, create it. If exists then open it if force = true
				if (!view || force) {
					$scope.open(viewId, true, params);
					// Set Icon
					var view = $scope.activeViewPanel();
					BridgeUtils.log("Setting Icon. Icon Base = " + view.iconBase);
					if (view.iconBase && view.iconBase != "") {
						BridgeUtils.log("Setting Icon = " + view.params);
						view.setIcon(BridgeUtils.substituteParams(view.iconBase, view.params, true));
					}
				} else {
					BridgeUtils.log("Skipping Open View. It's already created and not forced. = " + viewId);
				}
			});
			BridgeUtils.log("View Opened= " + viewId);
		}

		/*
		 *
		 */
		function closeView(viewId) {
			BridgeUtils.log("Closing View = " + viewId);
			BridgeUtils.runInAngularContext(function($scope) {
				try {
					var view = getViewPanel($scope, viewId);
					if (view) {
						if (!view.markedForClosing) {
							// HTML5 Framework uses timeout for publishing sgViewPanelCloseIntent and this is asynchronous
							// Because of this callback function get called after entire script is run
							// Due to this when callback function is called BridgeUtils.isScriptRunning() returns false
							// Below hack is added to get around this 

							view.markedForClosing = true;
							$scope.close(viewId);
						} else {
							BridgeUtils.log("View already marked for closing = " + viewId);
						}
					} else {
						BridgeUtils.log("View already closed = " + viewId);
					}
				} catch(e) {
					BridgeUtils.log("Failed in HTML5 Framework close() call: " + e, "e");
				}
			});
			BridgeUtils.log("View Closed = " + viewId);
		}

		/*
		 * 
		 */
		function getViewPanel($scope, viewId) {
			var view;
			var viewPanels;
			
			if ($scope != null) {
				viewPanels = $scope.viewPanels();
			} else {
				BridgeUtils.runInAngularContext(function($scope){
					viewPanels = $scope.viewPanels();
				});
			}

			if (viewPanels) {
				for (i in viewPanels) {
					if (viewPanels[i].path == viewId) {
						view = viewPanels[i];
						break;
					}
				}
			}
			return view;
		}
		/*
		 *
		 */
		function viewChanged(data) {
			if (BridgeUtils.isScriptRunning()) {
				return;
			}

			BridgeUtils.log("Processing View Changed Event = " + data);
			var navPath = data.currentNavItem.path;
			if (isPortalPath(navPath)) {
				var viewInfo = getViewInfoFromNavPath(navPath);
				var value = viewInfo.viewId + ":" + viewInfo.viewKey;
				doPartialSubmit("modelerLaunchPanels", "viewFormLP", "activeViewChanged", value);
			} else {
				navPath = (typeof data.before == 'object') ? data.before.path : data.before;
				if (isPortalPath(navPath)) {
					doPartialSubmit("modelerLaunchPanels", "viewFormLP", "activeViewChanged", "blank:blank");
				}
			}
		}

		/*
		 *
		 */
		function viewClosing(data) {
			BridgeUtils.log("Processing View Close Intent Event = " + data);

			var ret = true;
			var view = data.viewPanel;
			BridgeUtils.log("Processing View Close Intent Event for View = " + view.path);

			if (isPortalPath(view.path)) {
				if (BridgeUtils.isScriptRunning() || view.markedForClosing) {
					var iframeId = view.params["iframeId"];
					var iframe = document.getElementById(iframeId);
					BridgeUtils.log("iFrame to be removed = " + iframeId);
	
					if (iframe) {
						iframe.style.display = "none";
						iframe.src = "about:blank"; // Must change url before relocating iFrame
	
						var frameContainer = BridgeUtils.FrameManager.getFrameContainer();
						frameContainer.appendChild(iframe);
	
						BridgeUtils.FrameManager.close(iframeId);
					}
				} else {
					ret = false;

					var viewInfo = getViewInfoFromNavPath(view.path);
					BridgeUtils.log("Firring viewClosed for = " + viewInfo.viewId + ":" + viewInfo.viewKey);
					doPartialSubmit("modelerLaunchPanels", "viewFormLP", "viewClosing", viewInfo.viewId + ":" + viewInfo.viewKey);
				}
			}

			BridgeUtils.log("Processed View Close Intent Event returning = " + ret);
			return ret;
		}

		/*
		 *
		 */
		function sidebarVisibilityChanged(data) {
			BridgeUtils.log("Sidebar Visibility Changed = " + data.oldValue + ":" + data.newValue);
			var sidebarVisible = data.newValue;

			// Find All Anchors in sidebar (Launch Panels)
			window.setTimeout(function() {
				var iframe = document.getElementById("modelerLaunchPanels");
				if (iframe) {
					var elems = iframe.contentDocument.getElementsByTagName("div");
					for (var i in elems) {
						if (elems[i] && elems[i].id && elems[i].id.indexOf("Anchor", elems[i].id.length - "Anchor".length) !== -1) {
							BridgeUtils.FrameManager.doWithContentFrame(null, function(contentFrame) {
								if ("modelerLaunchPanels:" + elems[i].id == contentFrame.getAttribute('anchorId')) {
									var frameVisible = (contentFrame.style.display != "none")
									if (!sidebarVisible && frameVisible) {
										BridgeUtils.FrameManager.deactivate(contentFrame.getAttribute('name'));
									}
									else if (sidebarVisible && !frameVisible) {
										BridgeUtils.FrameManager.createOrActivate(contentFrame.getAttribute('name'));
									}
								}
							});
						}
					}
				}
			}, sidebarVisible ? 500 : 0); // Delay required when panel is becoming visible to get correct offset of Anchor
		}

		/*
		 *
		 */
		function sidebarPinStateChanged(data) {
			BridgeUtils.log("Sidebar Pin State Changed = " + data.oldValue + ":" + data.newValue);

			EventHub.events.publish("SIDEBAR_PINNED", data.newValue);
			
			// Delay so that UI adjustments are done
			window.setTimeout(function() {
				BridgeUtils.FrameManager.resizeAndRepositionAllActive();
			}, 100);
		}

		/*
		 *
		 */
		function getActiveViewParams() {
			return new JQueryUrlParamsProxy();
		}

		/*
		 *
		 */
		function getActiveView() {
			var view = null;
			BridgeUtils.runInAngularContext(function($scope) {
				view = $scope.activeViewPanel();
			});

			return view;
		}

		/*
		 *
		 */
		function getIframeIdForActiveView() {
			var view = getActiveView();
			if (view && view.params) {
				return view.params["iframeId"];
			}
			return "";
		}

		/*
		 *
		 */
		function doPartialSubmit(iframeId, formId, fieldId, fieldValue) {
			BridgeUtils.log("doPartialSubmit: Values = " + iframeId + ":" + formId + ":" + fieldId + ":" + fieldValue);
			var iframe = document.getElementById(iframeId);
			if (iframe) {
				var form = iframe.contentDocument.getElementById(formId);
				var field = iframe.contentDocument.getElementById(formId + ":" + fieldId);

				if (form != null && field != null) {
					BridgeUtils.log("doPartialSubmit: Existing Value = " + field.value + ", New value = " + fieldValue);
					
					field.value = fieldValue;
					iframe.contentWindow.iceSubmitPartial(form, field);
				} else {
					BridgeUtils.log("doPartialSubmit: form or field can not be NULL", "e");
				}
			} else {
				BridgeUtils.log("doPartialSubmit: Frame not Found = " + iframeId);
			}
		}

		/*
		 *
		 */
		function syncActiveView(immediate) {
			BridgeUtils.log("Trying to sync active view.");

			if (immediate != undefined && immediate == true) {
				_syncActiveView();
			} else {
				window.setTimeout(function() {
					_syncActiveView();
				}, 200);
			}
		}

		/*
		 * 
		 */
		function _syncActiveView() {
			var iframeId = getIframeIdForActiveView();
			if (iframeId) {
				BridgeUtils.log("Firring active view sync, for iframeId = " + iframeId);
				doPartialSubmit(iframeId, "viewFormMain", "activeViewSync", Math.floor(Math.random()*10000)+1);
				BridgeUtils.log("Firred active view sync, for iframeId = " + iframeId);
			} else {
				BridgeUtils.log("Could not sync active view, has no iframeId");
			}
		}
		/*
		 *
		 */
		function syncLaunchPanels(value) {
			// TODO: Check if sidebar is visible
			BridgeUtils.log("Trying to sync launch panels.");
			if (value == undefined) {
				value = Math.floor(Math.random()*10000)+1;
			}
			window.setTimeout(function() {
				BridgeUtils.log("Firring launch panels sync");
				doPartialSubmit("modelerLaunchPanels", "viewFormLP", "launchPanelsSync", value);
				BridgeUtils.log("Firred launch panels sync");
			}, 200);
		}

		/*
		 * 
		 */
		function setIcon(icon, viewId) {
			BridgeUtils.runInAngularContext(function($scope) {
				var view = (viewId == undefined) ? $scope.activeViewPanel() : getViewPanel($scope, viewId);
				if (view) {
					if (view.iconBase && view.iconBase != "") {
						icon = BridgeUtils.substituteParams(view.iconBase, {"icon": icon}, true);
					}
					view.setIcon(icon);
				}
			});
		}

		/*
		 * 
		 */
		function setTitle(title, viewId) {
			BridgeUtils.runInAngularContext(function($scope) {
				var view = (viewId == undefined) ? $scope.activeViewPanel() : getViewPanel($scope, viewId);
				if (view) {
					view.setTitle(title);
				}
			});
		}

		/*
		 * 
		 */
		function openSidebar() {
			BridgeUtils.runInAngularContext(function($scope) {
				$scope.$root.openSidebar();
			});
		}

		/*
		 * 
		 */
		function closeSidebar() {
			BridgeUtils.runInAngularContext(function($scope) {
				$scope.$root.closeSidebar();
			});
		}

		/*
		 * 
		 */
		function pinSidebar() {
			BridgeUtils.runInAngularContext(function($scope) {
				$scope.$root.pinSidebar();
			});
		}

		/*
		 * 
		 */
		function unpinSidebar() {
			BridgeUtils.runInAngularContext(function($scope) {
				$scope.$root.unpinSidebar();
			});
		}

		/*
		 * Private Function
		 */
		function isPortalPath(navPath) {
			return navPath.indexOf("/ippPortal/") == 0;
		}

		/*
		 * Private Function
		 */
		function getViewInfoFromNavPath(navPath) {
			var viewInfo = {};
			navPath = navPath.substr(10); // Remove PortalPath
			var navParts = navPath.split("/");
			if (navParts.length > 2) {
				viewInfo.viewKey = navParts[navParts.length - 1];
				viewInfo.viewId = navParts[navParts.length - 2];
			} else if (navParts.length > 1) {
				viewInfo.viewKey = "";
				viewInfo.viewId = navParts[navParts.length - 1];
			}

			return viewInfo;
		}

		/*
		 * Private Function
		 */
		function JQueryUrlParamsProxy() {
			var params = [];
			BridgeUtils.runInAngularContext(function($scope) {
				var view = $scope.activeViewPanel();
				params = view.params.custom;
			});

			function param(str) {
				return params[str];
			}

			return {
				param : param
			}
		}

		return {
			init : init,
			destroy : destroy,
			openView : openView,
			closeView : closeView,
			getActiveViewParams : getActiveViewParams,
			getActiveView : getActiveView,
			getIframeIdForActiveView : getIframeIdForActiveView,
			getViewPanel : getViewPanel,
			doPartialSubmit : doPartialSubmit,
			syncActiveView : syncActiveView,
			syncLaunchPanels : syncLaunchPanels,
			setIcon : setIcon,
			setTitle : setTitle,
			openSidebar : openSidebar,
			closeSidebar : closeSidebar,
			pinSidebar : pinSidebar,
			unpinSidebar : unpinSidebar
		}
	};

	BridgeUtils.View.init();
} // !BridgeUtils.View

if (!window["BridgeUtils"].Dialog) {
	BridgeUtils.Dialog = new function() {

		var popupDialogDiv;
		var invokedFromlaunchPanels;
		var sidebarPinned;

		var iframeForHeader;
		var iframeForFooter;
		var iframeForSidebar;

		var launchPanelIframe;
		var launchPanelIframeOrgData;
		
		/*
		 *
		 */
		function createDialog() {
			popupDialogDiv = document.createElement('div');
			popupDialogDiv.id = "jsPopupDiv";
			document.getElementsByTagName('body')[0].appendChild(popupDialogDiv);

			popupDialogDiv.innerHTML = '<iframe id="iframeForHeader" class="gray-out-header" src="about:blank"></iframe>';
			popupDialogDiv.innerHTML += '<iframe id="iframeForFooter" class="gray-out-footer" src="about:blank"></iframe>';
			popupDialogDiv.innerHTML += '<iframe id="iframeForSidebar" class="gray-out-sidebar" src="about:blank"></iframe>';

			// Header
			iframeForHeader = document.getElementById('iframeForHeader');
			iframeForHeader.style.visibility = "hidden";

			// Footer
			iframeForFooter = document.getElementById('iframeForFooter');
			iframeForFooter.style.visibility = "hidden";

			// Sidebar
			iframeForSidebar = document.getElementById('iframeForSidebar');
			iframeForSidebar.style.visibility = "hidden";
			
			// Sidebar + View
			launchPanelIframe = null;
		}

		/*
		 *
		 */
		function open(fromlaunchPanels, fromViewId) {
			if (!popupDialogDiv) {
				createDialog();
			}

			invokedFromlaunchPanels = fromlaunchPanels;

			var scrollWidth = document.body.scrollWidth;

			// Header
			iframeForHeader.style.visibility = "visible";
			iframeForHeader.style.width = scrollWidth + "px";

			// Footer
			iframeForFooter.style.visibility = "visible";
			iframeForFooter.style.width = scrollWidth + "px";

			// Sidebar
			var sidebar = document.getElementById("sidebar");
			var left = BridgeUtils.getAbsoluteSize(sidebar.style.left);
			sidebarPinned = left < 10; // Sidebar Pinned - TODO: Need HTML5 FW API to find out this
			if (!fromlaunchPanels) {
				sidebar.style.display = "none";

				var iframeWidth;
				if (left < 0) { // Sidebar not pinned but is hidden
					iframeWidth = BridgeUtils.getAbsoluteSize(sidebar.parentNode.children[0].style.paddingLeft) + 10;
				} else if (sidebarPinned) {
					iframeWidth = BridgeUtils.getAbsoluteSize(sidebar.parentNode.children[0].style.marginLeft) + 10;
					sidebar.style.display = "inline-block";
				} else {
					iframeWidth = left;
				}
				iframeForSidebar.style.width = iframeWidth + "px";
				iframeForSidebar.style.height = (BridgeUtils.getAbsoluteSize(sidebar.style.height) - 6) + "px";
				iframeForSidebar.style.visibility = "visible";
				
				// Activate the View, if fromView is not visible
				if (fromViewId != undefined && fromViewId != null && fromViewId != "") {
					var activeView = BridgeUtils.View.getActiveView();
					var view = BridgeUtils.View.getViewPanel(null, fromViewId);
					if (activeView != null && view != null && activeView.path != view.path) {
						BridgeUtils.View.openView(view.path, false, view.params);
					}
				}
			} else {
				if (!sidebarPinned) {
					BridgeUtils.View.pinSidebar();
				}

				// Sidebar Title
				jQuery(".sidebar-title").get(0).style.display = "none";

				// New Size
				var newWidth = scrollWidth + "px";
				var newHeight = (BridgeUtils.getAbsoluteSize(sidebar.style.height) - 6) + "px";

				// Launch Panels iframe				
				launchPanelIframe = document.getElementById("modelerLaunchPanels");
				launchPanelIframeOrgData = {};
				launchPanelIframeOrgData.clazz = launchPanelIframe.getAttribute("class");
				launchPanelIframeOrgData.width = launchPanelIframe.style.width;
				launchPanelIframeOrgData.height = launchPanelIframe.style.height;
				
				launchPanelIframe.setAttribute("class", "gray-out-sidebar-view");
				launchPanelIframe.style.width = newWidth;
				launchPanelIframe.style.height = newHeight;

				// Sometimes ICE Modal frame retain old size. So resize that too
				var iceModalFrame;
				var iframes = launchPanelIframe.contentDocument.getElementsByTagName("iframe");
				for (i in iframes) {
					if (iframes[i].title == "Ice Modal Frame") {
						iceModalFrame = iframes[i];
						break;
					}
				}
				iceModalFrame.style.width = newWidth;
				iceModalFrame.style.height = newHeight;

				BridgeUtils.FrameManager.resizeAndRepositionAllActive();
			}
		}

		/*
		 *
		 */
		function close() {
			if (popupDialogDiv) {
				// Header
				iframeForHeader.style.visibility = "hidden";
	
				// Footer
				iframeForFooter.style.visibility = "hidden";
	
				// Sidebar
				iframeForSidebar.style.visibility = "hidden";
	
				// Sidebar And View
				if (launchPanelIframe) {
					launchPanelIframe.setAttribute("class", launchPanelIframeOrgData.clazz);
					launchPanelIframe.style.width = launchPanelIframeOrgData.width;
					launchPanelIframe.style.height = launchPanelIframeOrgData.height;
					
					// Sidebar Title
					jQuery(".sidebar-title").get(0).style.display = "";
				} else {
					// Show Sidebar
					var sidebar = document.getElementById("sidebar");
					sidebar.style.display = "inline-block";
				}
	
				if (!sidebarPinned) {
					BridgeUtils.View.unpinSidebar();
				}

				invokedFromlaunchPanels = undefined;
				sidebarPinned = undefined;
				launchPanelIframe = undefined;
				launchPanelIframeOrgData = undefined;

				popupDialogDiv.parentNode.removeChild(popupDialogDiv);
				popupDialogDiv = undefined;

				BridgeUtils.FrameManager.resizeAndRepositionAllActive();
			}
		}

		return {
			open : open,
			close : close
		}
	};
} // !BridgeUtils.Dialog

if (!window["BridgeUtils"].FrameManager) {
	BridgeUtils.FrameManager = new function() {

		var initialized = false;
		var overlayIframes = new Array();

		/*
		 *
		 */
		function injectJavaScript(head, scriptSrc) {
			var found = false;
			for ( var i = 0; i < head.children.length; i++) {
				var tag = head.children[i];
				if ("SCRIPT" == tag.nodeName.toUpperCase()) {
					if (tag.src.length == (tag.src.indexOf(scriptSrc) + scriptSrc.length)) {
						found = true;
						break;
					}
				}
			}

			if (!found) {
				var script = document.createElement('script');
				script.src = scriptSrc;
				head.appendChild(script);
			}
		}

		/*
		 *
		 */
		function injectJavaScripts() {
			var head = document.getElementsByTagName('head')[0];
			injectJavaScript(head, 'plugins/common/eventHub.js');
			injectJavaScript(head, 'plugins/common/iDnD.js');
			injectJavaScript(head, 'plugins/common/iPopupDialog.js');
			injectJavaScript(head, 'plugins/common/js/Messaging.js');
			injectJavaScript(head, 'plugins/common/js/ProcessPortal.js');
			injectJavaScript(head, 'plugins/common/UiUtils.js');
		}

		/*
		 *
		 */
		function createOverlaysContainer() {
			var ippOverlays = document.getElementById('ippOverlays');
			if (!ippOverlays) {

				// ippOverlays
				ippOverlays = document.createElement('div');
				ippOverlays.id = "ippOverlays";
				document.getElementsByTagName('body')[0]
						.appendChild(ippOverlays);
				ippOverlays.style.position = 'absolute';
				ippOverlays.style.top = '0px';
				ippOverlays.style.left = '0px';
				ippOverlays.style.width = '0px';
				ippOverlays.style.height = '0px';

				// ippOverlaysFrameContainer
				ippOverlaysFrameContainer = document.createElement('div');
				ippOverlaysFrameContainer.setAttribute('id',
						'ippOverlaysFrameContainer');
				document.getElementById('ippOverlays').appendChild(
						ippOverlaysFrameContainer);
			}
		}

		/*
		 *
		 */
		function registerPageUnloadHandler() {
			BridgeUtils.log("Registering onUnload handler: " + window.location);

			// BEFORE UNLOAD
			var currentBeforeUnload = window.onbeforeunload;
	        window.onbeforeunload = function(event) {
			BridgeUtils.log('<before unload>: ' + event);
			var messages = new Array();
			try {
				var message = onPageUnload(window, event, 'before');
				if (message) {
					messages.push(message);
				}
			} catch (e) {
	        		alert("Failed handling before unload event: " + e, "e");
			}

			if (currentBeforeUnload) {
				var message = currentBeforeUnload(event);
				if (message) {
					messages.push(message);
				}
			}

			if (0 < messages.length) {
				if (event) {
					event.returnValue = messages.join('\n');
				}
				return messages.join('\n');
			}
	        }

			// AFTER UNLOAD
	        var currentUnload = window.onunload;
	        window.onunload = function(event) {
			BridgeUtils.log('<unload>: ' + event);
			try {
				onPageUnload(window, event, 'after');
			} catch (e) {
	        		alert("Failed handling after unload event: " + e, "e");
			}

			if (currentUnload) {
				currentUnload(event);
			}
	        }
		}

		/*
		 *
		 */
		function onPageUnload(window, event, phaseId) {
			BridgeUtils.log("Handling unload event (" + phaseId + ").");

			var openContentFrames = new Array();
		    doWithContentFrame(null, function(contentFrame) {
		    	if ('true' == contentFrame.getAttribute('noUnloadWarning') ||
		    			'true' == contentFrame.getAttribute('isClosing')) {
		    		// Ignore frame
		    	} else {
				openContentFrames.push(contentFrame.getAttribute('name'));
			}
		    });

		    BridgeUtils.log("Open content frames at page unload (" + phaseId + "): " + openContentFrames);

		    var messages = new Array();
		    if (0 < openContentFrames.length) {
			if ('before' == phaseId) {
				messages.push(1 == openContentFrames.length //
						? "Currently there is 1 open content frame. If you proceed to load another page any unsaved content will be lost."
		                    : "Currently there are " + openContentFrames.length + " open content frames. If you proceed to load another page any unsaved content will be lost.");
		        } else if ('after' == phaseId) {
				alert("Unsaved content of " + openContentFrames.length + " content frame(s) will be lost.");
		        }
		    }

		    if (0 < messages.length) {
			BridgeUtils.log('Leaving was not permitted.');
		        return messages.join('\n');
		    }
		}

		/*
		 *
		 */
		function registerWindowScrollHandler() {
			BridgeUtils.log("Registering onscroll handler");
			window.onscroll = function(event){
				handleScroll();
			};
			BridgeUtils.log("Registered onscroll handler");
		}

		/*
		 * Adjusts all active iFrames as per current browser scroll position
		 */
		function handleScroll() {
			doWithContentFrame(null, function(contentFrame) {
				var scrollable = contentFrame.getAttribute('repotitionOnScroll');
				scrollable = (scrollable == undefined || scrollable == true || scrollable == 'true');

				if (contentFrame.style.display == 'inline') {
					try {
						var scrollX = 0;
						var scrollY = 0;
						var mainWin = window;
						if (navigator.appName == 'Netscape') {
							scrollX = mainWin.pageXOffset;
							scrollY = mainWin.pageYOffset;
						} else if (navigator.appName == 'Microsoft Internet Explorer') {
							scrollX = mainWin.document.documentElement.scrollLeft;
							scrollY = mainWin.document.documentElement.scrollTop;
						}

						// TODO: Ideally if scrollable is false then iFrame needs to be skipped, but it did not work
						// Even if iFrame is adsolutely positioned it gets scrolled automativally. So, reverse the scroll effect
						if (!scrollable) {
							scrollX = 0 - scrollX;
							scrollY = 0 - scrollY;
						}

						var iFrame = getIframe(contentFrame.getAttribute('name'));
						if (iFrame != null) {
							var newX = iFrame.posX - scrollX;
							var newY = iFrame.posY - scrollY;

							var diffX = BridgeUtils.getAbsoluteSize(contentFrame.style.left) - newX;
							if (diffX < 0) {
								diffX = -diffX;
							}

							var diffY = BridgeUtils.getAbsoluteSize(contentFrame.style.top) - newY;
							if (diffY < 0) {
								diffY = -diffY;
							}

							// It's observed that sometimes scrollX/scrollY is 1 when there is no scroll on UI.
							// Also, 1px is very small, can be ignored. So check if it's greater than 1
							if (diffX > 1 || diffY > 1) {
								BridgeUtils.log("Repositioning iFrame '" + contentFrame.getAttribute('name') + "' to [" + newX + ", " + newY + "]");

								contentFrame.style.left = newX + "px";
								contentFrame.style.top = newY + "px";
							}
						}
					} catch(e) {
						alert("Error in handling browser scroll - " + e, "e");
					}
				}
			});
		}

		/*
		 * Adjusts relavent active iFrames as per current view's browser scroll position
		 */
		function handleViewScroll(currWindow, frame) {
			var currDoc = currWindow.document ? currWindow.document : currWindow.contentDocument;
			currWindow = currWindow.contentWindow ? currWindow.contentWindow : currWindow;

			if (frame == undefined) {
				doWithContentFrame(null, function(contentFrame) {
					if (contentFrame.style.display == 'inline') {
						var anchor = currDoc.getElementById(contentFrame.getAttribute('anchorId'));
						if (anchor) {
							_handleViewScroll(currWindow, contentFrame);
						}
					}
				});
			} else {
				_handleViewScroll(currWindow, frame);
			}
		}

		/*
		 * 
		 */
		function _handleViewScroll(currWindow, contentFrame) {
			try {
				var scrollPos = getScrollPosition(currWindow);

				var iFrame = getIframe(contentFrame.getAttribute('name'));
				if (iFrame != null) {
					var newX = iFrame.posX - scrollPos.x;
					var newY = iFrame.posY - scrollPos.y;

					// Hide the content frame, so that the right scroll position can be retrieved 
					contentFrame.style.display = "none";

					var scrollWidth = document.body.scrollWidth;
					var scrollHeight = document.body.scrollHeight;
					
					var right = newX + BridgeUtils.getAbsoluteSize(contentFrame.width);
					var bottom = newY + BridgeUtils.getAbsoluteSize(contentFrame.height);
					
					if (right > scrollWidth){
						newX -= (right - scrollWidth) + 25; // Buffer for scrollbar
					}
					if (bottom > scrollHeight){
						newY -= (bottom - scrollHeight) + 25; // Buffer for scrollbar
					}

					var diffX = BridgeUtils.getAbsoluteSize(contentFrame.style.left) - newX;
					if (diffX < 0) {
						diffX = -diffX;
					}

					var diffY = BridgeUtils.getAbsoluteSize(contentFrame.style.top) - newY;
					if (diffY < 0) {
						diffY = -diffY;
					}

					// It's observed that sometimes scrollPos.x/scrollPos.y is 1 when there is no scroll on UI.
					// Also, 1px is very small, can be ignored. So check if it's greater than 1
					if (diffX > 1 || diffY > 1) {
						BridgeUtils.log("Repositioning iFrame '" + contentFrame.getAttribute('name') + "' to [" + newX + ", " + newY + "]");

						contentFrame.style.left = newX + "px";
						contentFrame.style.top = newY + "px";
					}

					// Show back the frame
					contentFrame.style.display = "inline";
				}
			} catch(e) {
				alert("Error in handling browser scroll - " + e, "e");
			}
		}
		
		/*
		 * 
		 */
		function getScrollPosition(win) {
			var scrollX = 0;
			var scrollY = 0;
			if (navigator.appName == 'Netscape') {
				scrollX = win.pageXOffset;
				scrollY = win.pageYOffset;
			} else if (navigator.appName == 'Microsoft Internet Explorer') {
				scrollX = win.document.documentElement.scrollLeft;
				scrollY = win.document.documentElement.scrollTop;
			}
			
			return {
				x : scrollX,
				y : scrollY
			}
		}

		/*
		 *
		 */
		function init() {
			registerPageUnloadHandler();
			registerWindowScrollHandler();

			getFrameContainer();
		}

		/*
		 *
		 */
		function getFrameContainer() {
			if (!initialized) {
				injectJavaScripts();
				createOverlaysContainer();
				initialized = true;
			}
			return document.getElementById('ippOverlaysFrameContainer');
		}

		/*
		 *
		 */
		function doWithContentFrame(contentId, action) {
			var frameContainer = getFrameContainer();
			if (frameContainer) {
				var frames = frameContainer.getElementsByTagName('iframe');
				for ( var i = 0; i < frames.length; i++) {
					var frame = frames[i];
					if (!contentId || (contentId == frame.getAttribute('name'))) {
						action(frame);
						if (contentId != null) {
							break;
						}
					}
				}
			}
		}

		/*
		 *
		 */
		function createOrActivate(contentId, contentUrl, advanceArgs) {
			var contentFrame;
			doWithContentFrame(contentId, function(frame) {
				contentFrame = frame;
			});

			if (!contentFrame) {
				// create content frame
				contentFrame = document.createElement('iframe');
				contentFrame.setAttribute('id', contentId);
				contentFrame.setAttribute('name', contentId);
				contentFrame.setAttribute('frameBorder', '0');
				contentFrame.setAttribute('marginWidth', '0');
				contentFrame.setAttribute('marginHeight', '0');
				contentFrame.setAttribute('scrolling', 'auto');
				contentFrame
						.setAttribute(
								'style',
								'display: none; z-index:100; position: relative; top: 450px; left: 100px; width: 400px; height: 300px;');
				if (advanceArgs != undefined) {
					contentFrame.setAttribute('noUnloadWarning',
							advanceArgs.noUnloadWarning);
					// Loop through custom attributes and add those as well
					if (advanceArgs.frmAttrs) {
						var frmAttrs = advanceArgs.frmAttrs;
						for ( var attr in frmAttrs) {
							if (attr) {
								contentFrame.setAttribute(attr, frmAttrs[attr]);
							}
						}
					}
				}
				contentFrame.setAttribute('src', contentUrl);

				var frameContainer = getFrameContainer();
				frameContainer.appendChild(contentFrame);

				BridgeUtils.log("Frame Created = " + contentId);
			}

			activate(contentId, advanceArgs);
		}

		/*
		 * Private
		 */
		function getViewFrameDetails(anchor) {
			var viewFrame;
			var viewFrameDoc;

			if (anchor.indexOf("jQuery_") == 0) {
				var anchorElem = jQuery(anchor.substr(anchor.indexOf("jQuery_") + 7)).get(0);
				if (!anchorElem.id) {
					anchorElem.id = "id_" + Math.floor(Math.random()*100000)+1;
				}
				anchor = anchorElem.id;
				viewFrame = window;
				viewFrameDoc = document;
			} else if (anchor.indexOf(":") > -1) {
				var frame = anchor.substring(0, anchor.indexOf(":"));
				anchor = anchor.substring(anchor.indexOf(":")+1);
				viewFrame = document.getElementById(frame);
				if (viewFrame) {
					viewFrameDoc = viewFrame.contentDocument;
				}
			}

			if (!viewFrameDoc) {
				var tabWindow = getTabWindowAndDocument();
				viewFrame = tabWindow.win;
				viewFrameDoc = tabWindow.doc;
			}

			return {
				anchor : anchor,
				win : viewFrame,
				doc : viewFrameDoc
			}
		}

		/*
		 *
		 */
		function activate(contentId, advanceArgs, hiddenCounter) {
			BridgeUtils.log("Trying to activate Frame = " + contentId);

			if (hiddenCounter == undefined) {
				hiddenCounter = 50; // Max tries
			}

			doWithContentFrame( contentId, function(contentFrame) {
				if (advanceArgs != undefined) {
					var anchorId = advanceArgs.anchorId;
					var width = advanceArgs.width;
					var height = advanceArgs.height;
					var openOnRight = advanceArgs.openOnRight;
					var anchorXAdjustment = advanceArgs.anchorXAdjustment;
					var anchorYAdjustment = advanceArgs.anchorYAdjustment;
					var zIndex = advanceArgs.zIndex;
					var border = advanceArgs.border;
					var autoResize = advanceArgs.autoResize;
					var widthAdjustment = advanceArgs.widthAdjustment;
					var heightAdjustment = advanceArgs.heightAdjustment;
					if (width != undefined || height != undefined) {
						autoResize = false;
					}
				} else {
					// Read From Frame Attributes
					anchorId = getFrameAttribute(contentFrame, 'anchorId');
					width = getFrameAttribute(contentFrame, 'width', 'Integer');
					height = getFrameAttribute(contentFrame, 'height', 'Integer');
					openOnRight = getFrameAttribute(contentFrame, 'openOnRight', 'Boolean');
					anchorXAdjustment = getFrameAttribute(contentFrame, 'anchorXAdjustment', 'Integer');
					anchorYAdjustment = getFrameAttribute(contentFrame, 'anchorYAdjustment', 'Integer');
					autoResize = getFrameAttribute(contentFrame, 'autoResize', 'Boolean');
					widthAdjustment = getFrameAttribute(contentFrame, 'widthAdjustment', 'Integer');
					heightAdjustment = getFrameAttribute(contentFrame, 'heightAdjustment', 'Integer');
				}

				// Set Defaults
				autoResize = autoResize != undefined ? autoResize : true;
				widthAdjustment = widthAdjustment != undefined ? widthAdjustment : 0;
				heightAdjustment = heightAdjustment != undefined ? heightAdjustment : 0;
				openOnRight = openOnRight != undefined ? openOnRight : true;
				anchorXAdjustment = anchorXAdjustment != undefined ? anchorXAdjustment : 0;
				anchorYAdjustment = anchorYAdjustment != undefined ? anchorYAdjustment : 0;

				if (anchorId == undefined) {
					anchorId = 'ippActivityPanelAnchor';
					autoResize = true;
				}

				var delayActivation = true;
				var viewFrameData = getViewFrameDetails(anchorId);
				var contentPanelAnchor = viewFrameData.doc.getElementById(viewFrameData.anchor);
				if (contentPanelAnchor) {
					var pos = findPosition(contentPanelAnchor);
					var posFrame = findPosition(viewFrameData.win);

					// Sometimes frame position comes as zero
					// Add workaround i.e. delay activation. if last iteration then continue
					if (hiddenCounter < 0 || !(posFrame.x == 0 && posFrame.y == 0)) {
						delayActivation = false;

						posFrame.x = isNaN(posFrame.x) ? 0 : posFrame.x;
						posFrame.y = isNaN(posFrame.y) ? 0 : posFrame.y;
						
						pos.x += posFrame.x;
						pos.y += posFrame.y;
	
						var iFrameWith = (width == undefined) ? getOffsetWidth(contentPanelAnchor) : width;
						var iFrameHeight = (height == undefined) ? contentPanelAnchor.offsetHeight : height;
						if (iFrameHeight == 0) {
							iFrameHeight = BridgeUtils.getAbsoluteSize(viewFrameData.win.style.height) - 20;
						}
	
						iFrameWith = iFrameWith + widthAdjustment;
						iFrameHeight = iFrameHeight + heightAdjustment;
	
						var posX = openOnRight ? pos.x : (pos.x - iFrameWith);
						posX += anchorXAdjustment;
	
						var posY = pos.y + anchorYAdjustment;
	
						contentFrame.style.position = 'absolute';
						contentFrame.style.left = posX + 'px';
						contentFrame.style.top = posY + 'px';
						contentFrame.style.width = iFrameWith + 'px';
						contentFrame.style.height = iFrameHeight + 'px';
	
						if (border != undefined) {
							contentFrame.style.border = border;
						}
	
						if (zIndex != undefined) {
							contentFrame.style.zIndex = zIndex;
						}
	
						// Save values for future use
						contentFrame.setAttribute('anchorId', anchorId);
						if (!autoResize) {
							contentFrame.setAttribute('width', iFrameWith);
							contentFrame.setAttribute('height', iFrameHeight);
						}
						contentFrame.setAttribute('openOnRight', openOnRight);
						contentFrame.setAttribute('anchorXAdjustment', anchorXAdjustment);
						contentFrame.setAttribute('anchorYAdjustment', anchorYAdjustment);
						contentFrame.setAttribute('autoResize', autoResize);							
						contentFrame.setAttribute('widthAdjustment', widthAdjustment);
						contentFrame.setAttribute('heightAdjustment', heightAdjustment);
	
						// Finally make iFrame visible
						contentFrame.style.display = 'inline';
	
						addIframe(contentId, posX, posY);
	
						// This is needed because if page is scrolled at the time of iFrame activation
						// Then it has to be readjusted for scroll position.
						handleScroll();
						handleViewScroll(viewFrameData.win, contentFrame);
	
						BridgeUtils.log("Frame Activated = " + contentId);
					}
				}
				
				if (delayActivation){
					// Anchor is still loading. Delay activation
					BridgeUtils.log("Something is not right. Delaying frame activation = " + contentId + ", Counter: " + hiddenCounter);
					if (hiddenCounter >= 0) {
						window.setTimeout(function(){
							activate(contentId, advanceArgs, --hiddenCounter);
						}, 100);
					} else {
						BridgeUtils.log("Anchor not found while activating. Max tries exceeded for " + contentId, "e");
						contentFrame.style.display = 'inline';
					}
				}
			});
		}

		/*
		 *
		 */
		function deactivate(contentId) {
			BridgeUtils.log("Trying to deactivated Frame = " + contentId);
			doWithContentFrame(contentId, function(contentFrame) {
				contentFrame.style.display = 'none';
				// removeIframe(contentId);
				BridgeUtils.log("Frame Deactivated = " + contentId);
			});
		}

		/*
		 *
		 */
		function close(contentId) {
			BridgeUtils.log("Trying to Close iFrame = " + contentId);
			doWithContentFrame(contentId, function(contentFrame) {
				if ("true" != contentFrame.getAttribute("isClosing")) {
					contentFrame.setAttribute("isClosing", "true");

					contentFrame.style.display = 'none';
					if (contentFrame.src != "about:blank") {
						contentFrame.src = "about:blank";
					}

					BridgeUtils.log("Scheduling delayed iFrame Closing = " + contentId);
					window.setTimeout(function() {
						BridgeUtils.log("Removing iFrame = " + contentId);
						if (contentFrame.parentNode) {
							contentFrame.parentNode.removeChild(contentFrame);
						}
						BridgeUtils.log("Removed iFrame = " + contentId);
					}, 200);
				} else {
					BridgeUtils.log("iFrame is already in Closing = " + contentId);
				}
			});
		}

		/*
		 *
		 */
		function forceCloseAll() {
			return kill(2);
		}

		/*
		 * Private
		 */
		function kill(tryCount) {
			BridgeUtils.log("Trying to Kill All Frames, try count = " + tryCount);
		tryCount--;

			var iFrames = new Array();
		doWithContentFrame(null, function(contentFrame) {
			iFrames.push(contentFrame);
		});

		if (iFrames.length > 0) {
			var errors = new Array();
			for (var index = 0; index < iFrames.length; index++) {
				try {
					BridgeUtils.log("Removing iFrame = " + iFrames[index]);
					iFrames[index].parentNode.removeChild(iFrames[index]);
				} catch (e) {
					errors.push(e);
					}
			}

			// Confirm iFrames Count
			iFrames = new Array();
			doWithContentFrame(null, function(contentFrame) {
				iFrames.push(contentFrame);
			});

			if (iFrames.length > 0) {
				if (tryCount > 0) {
					return kill(tryCount);
				} else {
					alert("Could not remove " + iFrames.length + " iFrames.");
					return false;
				}
			}
		}

		return true;
		}

		/*
		 *
		 */
	    function resizeAndReposition(contentId, advanceArgs) {
		BridgeUtils.log("About to resize and reposition content frame with ID " + contentId);
		doWithContentFrame(contentId, function(contentFrame) {
			activate(contentId, advanceArgs);

			if (advanceArgs.width != undefined) {
				var newWidth = (advanceArgs.maxWidth != undefined && advanceArgs.maxWidth < advanceArgs.width) ? advanceArgs.maxWidth : advanceArgs.width;
				contentFrame.style.width = newWidth + 'px';
			}

			if (advanceArgs.height != undefined) {
				var newHeight = (advanceArgs.maxHeight != undefined && advanceArgs.maxHeight < advanceArgs.height) ? advanceArgs.maxHeight : advanceArgs.height;
				contentFrame.style.height = newHeight + 'px';
			}
		});
	    }

	    /*
	     *
	     */
	    function resizeAndRepositionAllActive() {
	    	BridgeUtils.log("About to resize & reposition all active content frames");
		doWithContentFrame(null, function(contentFrame) {
			if (contentFrame && contentFrame.style.display == 'inline') {
	    			activate(contentFrame.getAttribute('id'));
			}
		});
	    }

	    /*
	     * Private
	     */
	    function reposition(contentFrame, hiddenCounter) {
		var contentId = contentFrame.getAttribute('name');
		var anchor = contentFrame.getAttribute('anchorId');

		BridgeUtils.log("Trying to reposition Frame = " + contentId);

			var viewFrameData = getViewFrameDetails(anchor);
			var contentPanelAnchor = viewFrameData.doc.getElementById(viewFrameData.anchor);
			if (contentPanelAnchor) {
				var pos = findPosition(contentPanelAnchor);
				var posFrame = findPosition(viewFrameData.win);
				pos.x += posFrame.x;
				pos.y += posFrame.y;

				var iFrameWith = BridgeUtils.getAbsoluteSize(contentFrame.style.width);
				var openOnRight = contentFrame.getAttribute('openOnRight') == "true" ? true : false;
				var anchorXAdjustment = parseInt(contentFrame.getAttribute('anchorXAdjustment'));
				var anchorYAdjustment = parseInt(contentFrame.getAttribute('anchorYAdjustment'));

				var posX = openOnRight ? pos.x : (pos.x - iFrameWith);
				posX += anchorXAdjustment;
				var posY = pos.y + anchorYAdjustment;

				contentFrame.style.left = posX + 'px';
				contentFrame.style.top = posY + 'px';
			} else {
				// Just in case!? Anchor is still not available. Delay activation
				if (hiddenCounter == undefined) {
					hiddenCounter = 10; // Max tries
				}

				BridgeUtils.log("Anchor not found. Delaying frame repositioning = " + contentId + ", Counter: " + hiddenCounter);
				if (hiddenCounter >= 0) {
					window.setTimeout(function(){
						reposition(contentFrame, --hiddenCounter);
					}, 50);
				} else {
					BridgeUtils.log("Anchor not found while repositioning. Max tries exceeded for " + contentId, "e");
					contentFrame.style.display = 'inline';
				}
			}
	    }

		/*
		 *
		 */
		function getTabWindowAndDocument(notMandatory) {
			var ret = {};
			var viewFrame = document.getElementById(BridgeUtils.View.getIframeIdForActiveView());
			if (viewFrame) {
				return {
					win: viewFrame,
					doc: viewFrame.contentDocument
				};
			} else {
				if (!notMandatory) {
					return {
						win: window,
						doc: document
					};
				} else {
					return null;
				}
			}
		}

		/*
		 * If node parent is TD then offsetWidth value is not correct.
		 * So loop through all parents till correct value is found
		 */
		function getOffsetWidth(node, shouldBeMoreThan, defaultOffsetWidth) {
			var minOffsetWidth = shouldBeMoreThan ? shouldBeMoreThan : 10;
			var defValue = defaultOffsetWidth ? defaultOffsetWidth : 200;

			var offsetWidth = minOffsetWidth;
			while (node && node.offsetWidth != undefined) {
				if (node.offsetWidth > minOffsetWidth) {
					offsetWidth = node.offsetWidth;
					break;
				} else {
					node = node.parentNode;
				}
			}

			offsetWidth = offsetWidth > minOffsetWidth ? offsetWidth : defValue;

			return offsetWidth;
		}

		/*
		 *
		 */
		function findPosition(node) {
			var curleft = curtop = 0;
			do {
				curleft += node.offsetLeft;
				curtop += node.offsetTop;
			} while (node = node.offsetParent);

			var pos = new Object();
			pos.x = curleft;
			pos.y = curtop;

			return pos;
		}

		/*
		 * 
		 */
		function getFrameAttribute(contentFrame, attribute, type) {
			var retValue = undefined;
			if (contentFrame.getAttribute(attribute) != undefined) {
				retValue = contentFrame.getAttribute(attribute);
				if (type === "Integer") {
					retValue = parseInt(retValue);
				} else if (type === "Boolean") {
					retValue = retValue === "true" ? true : false;
				}
			}

			return retValue;
		}
		
		/*
		 *
		 */
		function addIframe(id, posX, posY) {
			var iFrame = {id: id, posX: posX, posY: posY};
			var index = getIframe(id, true);
			if (index == -1) {
				overlayIframes.push(iFrame);
			} else {
				overlayIframes.splice(index, 1, iFrame);
			}
		}

		/*
		 *
		 */
		function getIframe(id, returnIndex) {
			for (var i = 0; i < overlayIframes.length; i++) {
				if (overlayIframes[i].id == id) {
					return (returnIndex != undefined && returnIndex == true) ? i : overlayIframes[i];
				}
			}
			return null;
		}

		return {
			init : init,
			getTabWindowAndDocument : getTabWindowAndDocument,
			createOrActivate : createOrActivate,
			deactivate : deactivate,
			close : close,
			forceCloseAll : forceCloseAll,
			resizeAndReposition : resizeAndReposition,
			resizeAndRepositionAllActive : resizeAndRepositionAllActive,
			getFrameContainer : getFrameContainer,
			doWithContentFrame : doWithContentFrame,
			findPosition : findPosition,
			handleViewScroll : handleViewScroll
		}
	};

	BridgeUtils.FrameManager.init();
} // !BridgeUtils.FrameManager

if (!window["BridgeUtils"].Util) {
	BridgeUtils.Util = new function() {

		/*
		 *
		 */
	    function isIE() {
		if (navigator.appVersion && (-1 != navigator.appVersion.indexOf("MSIE"))) {
			return true;
		}

		return false;
	    }

	    /*
		 *
		 */
	    function isIE7() {
		if (navigator.appVersion && (-1 != navigator.appVersion.indexOf("MSIE 7.0"))) {
			return true;
		}

		return false;
	    }

		/*
		 *
		 */
	    function isFF() {
		if (navigator.userAgent && (-1 != navigator.userAgent.indexOf("Firefox"))) {
			return true;
		}

		return false;
	    }

		return {
			isFF : isFF,
			isIE : isIE,
			isIE7 : isIE7
		}
	}
} // !BridgeUtils.Util