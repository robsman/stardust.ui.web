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
define([ "m_utils" ], function(m_utils) {
	return {
		create : function() {
			return new JsfViewManager();
		}
	};

	/**
	 *
	 */
	function JsfViewManager() {
		/**
		 *
		 */
		JsfViewManager.prototype.toString = function() {
			return "Lightdust.JsfViewManager";
		};

		/**
		 *
		 */
		JsfViewManager.prototype.openView = function(viewId, queryString,
				objectId) {
			var link = jQuery("a[id $= 'model_view_link']",
					window.parent.frames['ippPortalMain'].document);
			var linkId = link.attr('id');
			var form = link.parents('form:first');
			var formId = form.attr('id');

			window.parent.EventHub.events.publish("OPEN_VIEW", linkId, formId,
					viewId, queryString, objectId);
		};

		/**
		 *
		 */
		JsfViewManager.prototype.updateView = function(viewId, queryString,
				objectId) {
			var link = jQuery("a[id $= 'view_updater_link']",
					window.parent.frames['ippPortalMain'].document);
			var linkId = link.attr('id');
			var form = link.parents('form:first');
			var formId = form.attr('id');

			window.parent.EventHub.events.publish("UPDATE_VIEW", linkId, formId,
					viewId, queryString, objectId);
		};
	}
});
