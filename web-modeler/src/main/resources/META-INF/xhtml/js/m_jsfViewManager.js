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
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_jsfViewManagerHelper" ],
		function(m_utils, m_globalVariables, m_jsfViewManagerHelper) {
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
			m_utils.showWaitCursor();
			m_utils.debug("Open View");
			m_utils.debug(m_utils.jQuerySelect(m_globalVariables.get("frames")['ippPortalMain']));

			var link = m_utils.jQuerySelect("a[id $= 'model_view_link']", m_utils.getOutlineWindowAndDocument().doc);
			var linkId = link.attr('id');
			var form = link.parents('form:first');
			var formId = form.attr('id');

			m_jsfViewManagerHelper.updateView(linkId, formId, viewId, queryString, objectId);
		};

		/**
		 *
		 */
		JsfViewManager.prototype.updateView = function(viewId, queryString,
				objectId) {
			var link = m_utils.jQuerySelect("a[id $= 'view_updater_link']", m_utils.getOutlineWindowAndDocument().doc);
			var linkId = link.attr('id');
			var form = link.parents('form:first');
			var formId = form.attr('id');

			m_jsfViewManagerHelper.updateView(linkId, formId, viewId, queryString, objectId);
		};

		/**
		 *
		 */
		JsfViewManager.prototype.closeViewsForElement = function(uuid) {
			var link = m_utils.jQuerySelect("a[id $= 'views_close_link']", m_utils.getOutlineWindowAndDocument().doc);
			var linkId = link.attr('id');
			var form = link.parents('form:first');
			var formId = form.attr('id');

			m_jsfViewManagerHelper.closeView(linkId, formId, uuid);
		};
	}
});
