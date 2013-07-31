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
if (!window["IppProcessPortalClient"]) {
	IppProcessPortalClient = new function() {

		var portalMainWnd = getIppWindow();

		// ********** APIs to Find IPP window/frame - START **********
		function isThisIppWindow(win) {
			try {
				var baseLocation = String(win.document.location);
	
				// Remove Query Params
				if (-1 != baseLocation.indexOf("?")) {
					baseLocation = baseLocation.substr(0, baseLocation.indexOf("?"));
				}

				// Remove # Params
				if (-1 != baseLocation.indexOf("#")) {
					baseLocation = baseLocation.substr(0, baseLocation.indexOf("#"));
				}

				// Check url, it should either read main.iface or login.iface
				if (-1 != baseLocation.indexOf("main.iface")
						|| -1 != baseLocation.indexOf("login.iface")
						|| -1 != baseLocation.indexOf("main.html")) {
					return true;
				} else {
					return false;
				}
			} catch (e) {
				// May be Access Control restriction
				return false;
			}
		}

	    function findIppWindowBottomUp(win){
	        if (!isThisIppWindow(win)) {
	        	if (win.parent != null && win.parent != win) {
	        		return findIppWindowBottomUp(win.parent);
	        	}
	    	}
	        else{
	        	return win;
	        }	
	    }

		function getIppWindow() {
			try {
	    		var ippWindow = findIppWindowBottomUp(window);
	    		if (ippWindow == null && window.opener != null) {
	    			ippWindow = findIppWindowBottomUp(window.opener);
	    		}

	    		if (null != ippWindow) {
	    			if (null != ippWindow["ippPortalMain"]) {
	    				return ippWindow["ippPortalMain"]; // Pre HTML5
	    			} else {
	    				return ippWindow; // After HTML5 Move
	    			}
	    		} else {
	    			// Assume parent or opener is IPP window, but there is no access to window object, due to access restriction
	    			if (null != window.parent && window.parent != window) {
	    				ippWindow = window.parent;
	    			} else if (null != window.opener) {
	    				ippWindow = window.opener;
	    			}
	    		}
	    		return ippWindow;
			} catch (x) {
				alert(getMessage("portal.common.js.ippMainWindow.notFound", "Error getting IPP Main Window. Portal will not properly work.") + "\n"
						+ x);
				return null;
			}
		}
		// ********** APIs to Find IPP window/frame - END **********

	    // ********** API for closing Activity Panel - START **********
		function closeEmbeddedActivityPanel(targetWindow, commandId) {
			if (targetWindow) {
				try {
					
					if (targetWindow.InfinityBpm.ProcessPortal) {
						if ('complete' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.completeActivity();
						} else if ('suspendAndSave' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.suspendActivity(true);
						} else if ('suspend' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.suspendActivity(false);
						} else if ('abort' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.abortActivity();
						} else if ('qaPass' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.qaPassActivity();
						} else if ('qaFail' === commandId) {
							targetWindow.InfinityBpm.ProcessPortal.qaFailActivity();
						} 
						return;
					} else {
						//alert('Did not find InfinityBpm.ProcessPortal module in main page' + typeof targetWindow.InfinityBpm.ProcessPortal);
					}
				} catch (x1) {
					// probably forbidden to access location, assuming other page
					//alert('Failed invoking top level IPP function: ');
				}

				// trying postMessage
				try {
					if (targetWindow.postMessage) {
						//alert('Using post message ... ');
						//alert('Target window: ' + targetWindow.toString() + ' Command id: ' + commandId);
						sleep(2000);
						targetWindow.postMessage(commandId, "*");
						sleep(3000);
						//alert('Post message finished');
						return;
					}
				} catch (x2) {
					 //failed using postMessage, fall back to FIM
					//alert('Failed invoking postMessage: ' + x2);
				}
				
				try {
					//alert('Unfortunately this browser is currently not yet supported.');
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
					return;
					
					ifrm = document.createElement("IFRAME");
					ifrm.setAttribute('style', 'display: none; width: 0px; height: 0px;');
					//TODO replace with dynamic URL determination
					ifrm.setAttribute("src", "http:localhost:9090/ipp/ipp/process/remoteControl/" + commandId + "EmbeddedActivityPanel.html");
					document.body.appendChild(ifrm);
				} catch (x3) {
					//alert('Failed triggering cross domain panel close: ' + x3.description)
				}
			}
		}
		
		function sleep(ms)
		{
			var dt = new Date();
		    dt.setTime(dt.getTime() + ms);
		    while (new Date().getTime() < dt.getTime());
		}
	    // ********** API for closing Activity Panel - END **********

	    function getMessage(messageProp, defaultMsg) {
			if (InfinityBPMI18N && InfinityBPMI18N.common)
			{
				var propVal = InfinityBPMI18N.common.getProperty(messageProp, defaultMsg);
				if (propVal && propVal != "")
				{
					return propVal;
				}
			}

			return defaultMsg;
	    }

		return {
			completeActivity : function() {
				// alert("Delegating complete() to portal frame.");
				closeEmbeddedActivityPanel(portalMainWnd, 'complete');
			},

			qaPassActivity : function() {
				// alert("Delegating qaPassActivity() to portal frame.");
				closeEmbeddedActivityPanel(portalMainWnd, 'qaPass');
			},
			
			qaFailActivity : function() {
				// alert("Delegating qaFailActivity() to portal frame.");
				closeEmbeddedActivityPanel(portalMainWnd, 'qaFail');
			},

			suspendActivity : function(saveOutParams) {
				// alert("Delegating suspend() to portal frame.");
				closeEmbeddedActivityPanel(portalMainWnd, saveOutParams ? 'suspendAndSave' : 'suspend');
			},

			abortActivity : function() {
				// alert("Delegating abort() to portal frame.");
				closeEmbeddedActivityPanel(portalMainWnd, 'abort');
			}
		};
	};
} // IppProcessPortalClient