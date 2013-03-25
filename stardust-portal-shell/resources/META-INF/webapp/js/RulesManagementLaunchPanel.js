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
		"bpm-modeler/js/m_communicationController", "bpm-modeler/js/m_dialog", "rules-manager/js/Outline",
		"stardust-portal-shell/js/ViewManager", "stardust-portal-shell/js/OrionRuleSetManager" ], function(Utils,
		m_extensionManager, m_communicationController, Ui, Outline,
		ViewManager, OrionRuleSetManager) {
	return {
		create : function(viewManager) {
			var launchPanel = new RulesManagementLaunchPanel();

			launchPanel.initialize(viewManager);

			return launchPanel;
		}
	};

	/**
	 * 
	 */
	function RulesManagementLaunchPanel() {
		/**
		 * 
		 */
		RulesManagementLaunchPanel.prototype.toString = function() {
			return "Lightdust.RulesManagementLaunchPanel";
		};

		/**
		 * 
		 */
		RulesManagementLaunchPanel.prototype.initialize = function(viewManager) {
			this.viewManager = viewManager;
			this.saveAllRuleSetsButton = jQuery("#saveAllRuleSetsButton");			
			this.ruleSetManager = OrionRuleSetManager.create();
			
			this.saveAllRuleSetsButton.click({launchPanel: this}, function(event){
				event.data.launchPanel.saveAllRuleSets();
			})
		};

		/**
		 * Called when Portal switches to this Perspective. 
		 */
		RulesManagementLaunchPanel.prototype.activate = function() {
			if (!this.outline) {
				this.outline = Outline.init(this.viewManager,
						"rulesManagementLaunchPanel");
			}
		};

		/**
		 * 
		 */
		RulesManagementLaunchPanel.prototype.saveAllRuleSets = function() {
			Utils.debug("Save all Rule Sets");
			
			Ui.showWaitCursor();
			
			this.ruleSetManager.saveRuleSets().always(Ui.showAutoCursor);
		}
	}
});
