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
		"stardust-portal-shell/js/PerspectiveManager", "stardust-portal-shell/js/OrionFileManager",
		"stardust-portal-shell/js/ViewManager" ], function(m_utils,
		m_extensionManager, PerspectiveManager,
		OrionFileManager, ViewManager) {
	return {
		initialize : function(file) {
			console.log("Initialize Portal Shell");
			
			var portal = new PortalShell();

			portal.initialize(file);

			return portal;
		}
	};

	/**
	 * 
	 */
	function PortalShell() {
		/**
		 * 
		 */
		PortalShell.prototype.toString = function() {
			return "Lightdust.PortalShell";
		};

		/**
		 * 
		 */
		PortalShell.prototype.initialize = function(file) {
			this.fileManager = OrionFileManager.create();
			this.viewManager = ViewManager.create();
			this.perspectiveManager = PerspectiveManager.create(this.viewManager);

			var portal = this;
			
			this.fileManager.bootstrap(file).done(function() {
				m_utils.debug("File Manager successfully bootstrapped.");
				
				portal.perspectiveManager.activateInitialPerspective();
				
				// Open the view for the file provided in the URL 

				var viewInfo = portal.fileManager.getViewInfo(file);
				
				portal.viewManager.openView(viewInfo.viewId,
						viewInfo.queryString, viewInfo.objectId);
				portal.perspectiveManager.changePerspective(viewInfo.perspectiveId);
			}).fail(function() {
				window.alert("Failed to boostrap from provided URL.")
			});
		};
	}
});
