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

		function isThisIppWindow(win) {
			var baseLocation = String(win.document.location);

			// Remove Query Params
			if (-1 != baseLocation.indexOf("?")) {
				baseLocation = baseLocation.substr(0, baseLocation.indexOf("?"));
			}

			// Check url, it should either read main.iface or login.iface
			if (-1 != baseLocation.indexOf("main.iface")
					|| -1 != baseLocation.indexOf("login.iface")) {
				return true;
			} else {
				return false;
			}
		}

		function findIppWindow(win) {
			if (!isThisIppWindow(win)) {
				var frames = win.frames;
				for ( var i = 0; i < frames.length; i++) {
					var ippWindow = findIppWindow(frames[i]);
					if (ippWindow != null) {
						return ippWindow;
					}
				}
				return null;
			} else {
				return win;
			}
		}

		function getIppWindow() {
			try {
				var ippWindow = findIppWindow(top);
				return ippWindow;
			} catch (x) {
				alert(getMessage("portal.common.js.ippMainWindow.notFound", "Error getting IPP Main Window. Portal will not properly work.") + "\n"
						+ x);
				return null;
			}
		}
		
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

		var mainIppFrame = getIppWindow();

		return {
			completeActivity : function() {
				// alert("Delegating complete() to portal frame.");
				var portalMainWnd = mainIppFrame["ippPortalMain"];
				if (portalMainWnd.InfinityBpm.ProcessPortal) {
					portalMainWnd.InfinityBpm.ProcessPortal.completeActivity();
				} else {
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
				}
			},

			qaPassActivity : function() {
				// alert("Delegating qaPassActivity() to portal frame.");
				var portalMainWnd = mainIppFrame["ippPortalMain"];
				if (portalMainWnd.InfinityBpm.ProcessPortal) {
					portalMainWnd.InfinityBpm.ProcessPortal.qaPassActivity();
				} else {
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
				}
			},
			
			qaFailActivity : function() {
				// alert("Delegating qaFailActivity() to portal frame.");
				var portalMainWnd = mainIppFrame["ippPortalMain"];
				if (portalMainWnd.InfinityBpm.ProcessPortal) {
					portalMainWnd.InfinityBpm.ProcessPortal.qaFailActivity();
				} else {
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
				}
			},

			suspendActivity : function(saveOutParams) {
				// alert("Delegating suspend() to portal frame.");
				var portalMainWnd = mainIppFrame["ippPortalMain"];
				if (portalMainWnd.InfinityBpm.ProcessPortal) {
					portalMainWnd.InfinityBpm.ProcessPortal.suspendActivity(saveOutParams);
				} else {
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
				}
			},

			abortActivity : function() {
				// alert("Delegating abort() to portal frame.");
				var portalMainWnd = mainIppFrame["ippPortalMain"];
				if (portalMainWnd.InfinityBpm.ProcessPortal) {
					portalMainWnd.InfinityBpm.ProcessPortal.abortActivity();
				} else {
					alert(getMessage("portal.common.js.processPortal.api.notAvailable", "The Process Portal API is not available."));
				}
			}
		};
	};
} // IppProcessPortalClient