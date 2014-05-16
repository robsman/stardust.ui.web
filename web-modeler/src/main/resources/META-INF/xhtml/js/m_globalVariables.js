/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Marc.Gille
 */
define(function() {

	return {
		get : function(key) {
			return get(key);
		},

		set : function(key, value) {
			return set(key, value);
		},
		
		findMainWindowBottomUp : function(win) {
			return findMainWindowBottomUp(win);
		}
	};
	
	function set(key, value) {
		var win = findMainWindowBottomUp();
		win[key] = value;
	};

	function get(key) {
		var win = findMainWindowBottomUp();
		return win[key];
	};

	// ********** APIs to Find Main window/frame - START **********
	function isThisMainWindow(win) {
		try {
			var baseLocation = String(win.document.location);

			// Remove Query Params
			if (-1 != baseLocation.indexOf("?")) {
				baseLocation = baseLocation
						.substr(0, baseLocation.indexOf("?"));
			}

			// Remove # Params
			if (-1 != baseLocation.indexOf("#")) {
				baseLocation = baseLocation
						.substr(0, baseLocation.indexOf("#"));
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

	function findMainWindowBottomUp(win) {
		if (win == undefined) {
			win = window;
		}
		if (!isThisMainWindow(win)) {
			if (win.parent != null && win.parent != win) {
				return findMainWindowBottomUp(win.parent);
			}
		} else {
			return win;
		}
	};
});