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
					console.debug("* " + str);
				}
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

			func();

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
		BridgeUtils.FrameManager.closeAll();
		runInAngularContext(function($scope) {
			BridgeUtils.logout();
			});
		log("Handled Server Disconnect for " + type);
	    }

		/*
		 *
		 */
		function logout() {
			var href = window.location.href.substr(0, window.location.href.indexOf('/main.html'));
			href += '/ipp/common/ippPortalLogout.jsp';
			window.location.replace(href);
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

		return {
			log : log,
			runInAngularContext : runInAngularContext,
			runScript : runScript,
			isScriptRunning : isScriptRunning,
			handleServerDisconnect : handleServerDisconnect,
			logout : logout,
			getAbsoluteSize : getAbsoluteSize
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
			BridgeUtils.log("BridgeUtils.View Initializing");

			var sgPubSubService;
			BridgeUtils.runInAngularContext(function($scope) {
				sgPubSubService = $scope.$root.sgPubSubService;

				$scope.$root.$on('$destroy', function() {
					BridgeUtils.View.destroy();
		        });
			});

			BridgeUtils.log("Subscribing to View Events");
			unsubscribers.push(sgPubSubService.subscribe('sgActiveViewPanelChanged', viewChanged));
			unsubscribers.push(sgPubSubService.subscribe('sgViewPanelCloseIntent', viewClosing));
			unsubscribers.push(sgPubSubService.subscribe('sgViewPanelClosed', viewClosed));

			BridgeUtils.log("Subscribing to Sidebar Events");
			unsubscribers.push(sgPubSubService.subscribe('sgSidebarVisibilityChanged', sidebarVisibilityChanged));
			unsubscribers.push(sgPubSubService.subscribe('sgSidebarPinStateChanged', sidebarPinStateChanged));

			BridgeUtils.log("BridgeUtils.View Initialized Successfully");
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
		function openView(viewId, params) {
			BridgeUtils.log("Opening View = " + viewId);
			BridgeUtils.runInAngularContext(function($scope) {
				$scope.open(viewId, true, params);
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
					$scope.close(viewId);
				} catch(e) {
					BridgeUtils.log("Failed in HTML5 Framework close() call: " + e);
				}
			});
			BridgeUtils.log("View Closed = " + viewId);
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

			var view = data.viewPanel;
			if (isPortalPath(view.path) && view.params) {
				var iframeId = view.params["iframeId"];
				var iframe = document.getElementById(iframeId);
				BridgeUtils.log("iFrame to be removed = " + iframeId);

				if (iframe) {
					iframe.style.display = "none";
					iframe.src = "about:blank"; // Must change url before relocating iFrame

					var frameContainer = BridgeUtils.FrameManager.getFrameContainer();
					frameContainer.appendChild(iframe);

					BridgeUtils.log("Scheduling delayed iFrame Closing = " + iframeId);
					window.setTimeout(function() {
						BridgeUtils.log("Removing iFrame = " + iframeId);
						iframe.parentNode.removeChild(iframe);
						BridgeUtils.log("Removed iFrame = " + iframeId);
					}, 200);
				}

				BridgeUtils.log("Processed View Close Intent Event = " + data);
			}
			return true;
		}

		/*
		 *
		 */
		function viewClosed(data) {
			if (BridgeUtils.isScriptRunning()) {
				return;
			}

			BridgeUtils.log("Processing View Closed Event = " + data);
			var navPath = data.path;
			if (isPortalPath(navPath)) {
				var viewInfo = getViewInfoFromNavPath(navPath);
				BridgeUtils.log("Firring viewClosed for = " + viewInfo.viewId + ":" + viewInfo.viewKey);
				doPartialSubmit("modelerLaunchPanels", "viewFormLP", "viewClosed",
						viewInfo.viewId + ":" + viewInfo.viewKey);
			}
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

			// Delay so that UI adjustments are done
			window.setTimeout(function() {
				BridgeUtils.FrameManager.repositionAllActive();
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
			var iframe = document.getElementById(iframeId);
			if (iframe) {
				var form = iframe.contentDocument.getElementById(formId);
				var field = iframe.contentDocument.getElementById(formId + ":" + fieldId);

				field.value = fieldValue;
				iframe.contentWindow.iceSubmitPartial(form, field);
			} else {
				BridgeUtils.log("Frame not Found = " + iframeId);
			}
		}

		/*
		 *
		 */
		function syncActiveView() {
			BridgeUtils.log("Trying to sync active view.");

			window.setTimeout(function() {
				var iframeId = getIframeIdForActiveView();
				if (iframeId) {
					BridgeUtils.log("Firring active view sync, for iframeId = " + iframeId);
					doPartialSubmit(iframeId, "viewFormMain", "activeViewSync", Math.floor(Math.random()*10000)+1);
					BridgeUtils.log("Firred active view sync, for iframeId = " + iframeId);
				} else {
					BridgeUtils.log("Could not sync active view, has no iframeId");
				}
			}, 400);
		}

		/*
		 *
		 */
		function syncLaunchPanels() {
			// TODO: Check if sidebar is visible
			BridgeUtils.log("Trying to sync launch panels.");
			window.setTimeout(function() {
				BridgeUtils.log("Firring launch panels sync");
				doPartialSubmit("modelerLaunchPanels", "viewFormLP", "launchPanelsSync", Math.floor(Math.random()*10000)+1);
				BridgeUtils.log("Firred launch panels sync");
			}, 200);
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
			doPartialSubmit : doPartialSubmit,
			syncActiveView : syncActiveView,
			syncLaunchPanels : syncLaunchPanels
		}
	};

	BridgeUtils.View.init();
} // !BridgeUtils.View

if (!window["BridgeUtils"].Dialog) {
	BridgeUtils.Dialog = new function() {

		var popupDialogDiv;
		var iframeForHeader;
		var iframeForFooter;
		var iframeForSidebar;

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
		}

		/*
		 *
		 */
		function open(fromlaunchPanels) {
			if (!popupDialogDiv) {
				createDialog();
			}

			var scrollWidth = document.body.scrollWidth;

			// Header
			iframeForHeader.style.visibility = "visible";
			iframeForHeader.style.width = scrollWidth + "px";

			// Footer
			iframeForFooter.style.visibility = "visible";
			iframeForFooter.style.width = scrollWidth + "px";

			// Sidebar
			if (!fromlaunchPanels) {
				var sidebar = document.getElementById("sidebar");
				sidebar.style.display = "none";

				var iframeWidth;
				var left = BridgeUtils.getAbsoluteSize(sidebar.style.left);
				if (left < 0) { // Sidebar not pinned but is hidden
					iframeWidth = BridgeUtils.getAbsoluteSize(sidebar.parentNode.children[0].style.paddingLeft) + 10;
				} else if (left < 10) { // Sidebar Pinned - TODO: Need HTML5 FW API to find out this
					iframeWidth = BridgeUtils.getAbsoluteSize(sidebar.parentNode.children[0].style.marginLeft) + 10;
					sidebar.style.display = "inline-block";
				} else {
					iframeWidth = left;
				}
				iframeForSidebar.style.width = iframeWidth + "px";
				iframeForSidebar.style.visibility = "visible";

				iframeForSidebar.style.height = (BridgeUtils.getAbsoluteSize(sidebar.style.height) - 6) + "px";
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

				// Remove Div & iFrames
				popupDialogDiv.parentNode.removeChild(popupDialogDiv);
				popupDialogDiv = undefined;

				// Show Sidebar
				var sidebar = document.getElementById("sidebar");
				sidebar.style.display = "inline-block";
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
			injectJavaScript(head, 'plugins/common/js/Messaging.js');
			injectJavaScript(head, 'plugins/common/js/ProcessPortal.js');
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
				alert("Failed handling before unload event: " + e.message);
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
				alert("Failed handling after unload event: " + e.message);
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
			if ('true' != contentFrame.getAttribute('noUnloadWarning')) {
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
						alert("Error in handling browser scroll - " + e.message);
					}
				}
			});
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

			if (anchor.indexOf(":") > -1) {
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
			}

			doWithContentFrame(
					contentId,
					function(contentFrame) {
						var anchor = anchorId;

						if (anchor == undefined && contentFrame.getAttribute('anchorId')) {
							anchor = contentFrame.getAttribute('anchorId')
						} else if (anchor == undefined) {
							anchor = 'ippActivityPanelAnchor';
							autoResize = true;
						}

						var viewFrameData = getViewFrameDetails(anchor);

						var contentPanelAnchor = viewFrameData.doc.getElementById(viewFrameData.anchor);
						if (contentPanelAnchor) {
							var pos = findPosition(contentPanelAnchor);
							var posFrame = findPosition(viewFrameData.win);
							pos.x += posFrame.x;
							pos.y += posFrame.y;

							var iFrameWith = (width == undefined) ? contentPanelAnchor.offsetWidth : width;
							if (iFrameWith == 0) {
								iFrameWith = 300; // TODO?
							}

							var iFrameHeight = (height == undefined) ? contentPanelAnchor.offsetHeight : height;
							if (iFrameHeight == 0) {
								iFrameHeight = BridgeUtils.getAbsoluteSize(viewFrameData.win.style.height) - 20;
							}

							openOnRight = (openOnRight == undefined) ? true : openOnRight;
							anchorXAdjustment = (anchorXAdjustment == undefined) ? 0 : anchorXAdjustment;
							anchorYAdjustment = (anchorYAdjustment == undefined) ? 0 : anchorYAdjustment;

							var posX = openOnRight ? pos.x : (pos.x - iFrameWith);
							posX += anchorXAdjustment;

							var posY = pos.y + anchorYAdjustment;

							contentFrame.style.position = 'absolute';
							contentFrame.style.left = posX + 'px';
							contentFrame.style.top = posY + 'px';
							contentFrame.style.width = iFrameWith + 'px';
							contentFrame.style.height = iFrameHeight + 'px';

							// Save values for future use
							contentFrame.setAttribute('anchorId', anchor);
							contentFrame.setAttribute('autoResize', autoResize);
							contentFrame.setAttribute('anchorXAdjustment', anchorXAdjustment);
							contentFrame.setAttribute('anchorYAdjustment', anchorYAdjustment);
							contentFrame.setAttribute('openOnRight', openOnRight);

							contentFrame.style.display = 'inline';

							if (border != undefined) {
								contentFrame.style.border = border;
							}

							if (zIndex != undefined) {
								contentFrame.style.zIndex = zIndex;
							}

							addIframe(contentId, posX, posY);

							// This is needed because if page is scrolled at the time of iFrame activation
							// Then it has to be readjusted for scroll position.
							handleScroll();

							BridgeUtils.log("Frame Activated = " + contentId);
						} else {
							// Anchor is still loading. Delay activation
							if (hiddenCounter == undefined) {
								hiddenCounter = 35; // Max tries
							}

							BridgeUtils.log("Anchor not found. Delaying frame activation = " + contentId + ", Counter: " + hiddenCounter);
							if (hiddenCounter >= 0) {
								window.setTimeout(function(){
									activate(contentId, advanceArgs, --hiddenCounter);
								}, 100);
							} else {
								BridgeUtils.log("Anchor not found while activating. Max tries exceeded for " + contentId);
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
			BridgeUtils.log("Trying to Close Frame = " + contentId);
			doWithContentFrame(contentId, function(contentFrame) {
				contentFrame.style.display = 'none';
				if (contentFrame.parentNode) {
					contentFrame.parentNode.removeChild(contentFrame);
				}
				BridgeUtils.log("Closed Frame = " + contentId);
			});
		}

		/*
		 *
		 */
		function closeAll() {
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
	    function repositionAllActive() {
		BridgeUtils.log("About to reposition all active content frames");
		doWithContentFrame(null, function(contentFrame) {
			if (contentFrame && contentFrame.style.display == 'inline') {
				reposition(contentFrame);
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
					BridgeUtils.log("Anchor not found while repositioning. Max tries exceeded for " + contentId);
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
			closeAll : closeAll,
			resizeAndReposition : resizeAndReposition,
			repositionAllActive : repositionAllActive,
			getFrameContainer : getFrameContainer,
			doWithContentFrame : doWithContentFrame
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