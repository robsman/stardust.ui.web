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
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_extensionManager",
		"bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_outline",
		"stardust-portal-shell/js/ViewManager", "stardust-portal-shell/js/OrionProcessModelManager" ], function(m_utils,
		m_extensionManager, m_communicationController, m_outline,
		ViewManager, OrionProcessModelManager) {
	return {
		create : function(viewManager) {
			var launchPanel = new ProcessModelingLaunchPanel();

			launchPanel.initialize(viewManager);

			return launchPanel;
		}
	};

	/**
	 * 
	 */
	function ProcessModelingLaunchPanel() {
		/**
		 * 
		 */
		ProcessModelingLaunchPanel.prototype.toString = function() {
			return "Lightdust.ProcessModelingLaunchPanel";
		};

		/**
		 * 
		 */
		ProcessModelingLaunchPanel.prototype.initialize = function(viewManager) {
			this.viewManager = viewManager;
			this.processModelManager = OrionProcessModelManager.create();
		};

		/**
		 * 
		 */
		ProcessModelingLaunchPanel.prototype.activate = function() {
			if (!this.outline) {
				this.outline = m_outline.init(this.viewManager,
						"processModelingLaunchPanel");
			}
		};
	}
});
