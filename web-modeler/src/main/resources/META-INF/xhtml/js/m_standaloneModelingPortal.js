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
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager", "bpm-modeler/js/m_communicationController",
		"bpm-modeler/js/m_outline", "bpm-modeler/js/m_htmlViewManager" ], function(m_utils,
		m_extensionManager, m_communicationController, m_outline,
		m_htmlViewManager) {
	return {
		initialize : function(file) {
			var portal = new StandaloneModelingPortal();

			portal.initialize(file);

			return portal;
		}
	};

	/**
	 * 
	 */
	function StandaloneModelingPortal() {
		/**
		 * 
		 */
		StandaloneModelingPortal.prototype.toString = function() {
			return "Lightdust.StandaloneModelingPortal";
		};

		/**
		 * 
		 */
		StandaloneModelingPortal.prototype.initialize = function(file) {
			m_utils.debug("====> Initializing with file: " + file);

			// Switch to URI Model Management Strategy

			if (file != null) {
				m_communicationController.postData({
					url : m_communicationController.getEndpointUrl()
							+ "/model/management/strategy"
				}, JSON.stringify({
					fileUri : file
				}), {
					success : function(data) {
						m_utils.debug("URI Management Strategy set");

						m_outline.init(m_htmlViewManager.create());
					}
				});
			} else {
				m_outline.init(m_htmlViewManager.create());
			}
		};
	}
});
