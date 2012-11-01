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

/**
 * View Management
 * 
 * @author Marc.Gille
 */
define([ "m_utils", "m_extensionManager" ], function(m_utils,
		m_extensionManager) {
	return {
		create : function() {
			return new HtmlViewManager();
		}
	};

	/**
	 * 
	 */
	function HtmlViewManager() {
		/**
		 * 
		 */
		HtmlViewManager.prototype.toString = function() {
			return "Lightdust.HtmlViewManager";
		};

		/**
		 * 
		 */
		HtmlViewManager.prototype.openView = function(viewId, queryString,
				objectId) {
			var extension = m_extensionManager.findExtensions("view", "viewId",
					viewId)[0];

			m_utils.debug("Extension: " + extension.viewHtmlUrl + " "
					+ extension.viewJavaScriptUrl);

			jQuery("#viewAnchor").attr("src", extension.viewHtmlUrl + "?" + queryString);
		};
	}
});
