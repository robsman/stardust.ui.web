/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager" ],
		function(m_utils, m_constants, m_extensionManager) {
			return {
				getRuleSets : getRuleSets,

				getRuleSetProvider : getRuleSetProvider
			};

			function getRuleSetProvider() {
				var ruleSetProviders = m_extensionManager.findExtensions("ruleSetProvider");
				for (var n = 0; n < ruleSetProviders.length; n++) {
					return ruleSetProviders[n].provider.create();					
				}
			};
			
			function getRuleSets() {
				initRuleSets();
				
				return window.top.ruleSets;
			};
			
			function initRuleSets() {				
				if (!window.top.ruleSets) {
					var ruleSetProvider = getRuleSetProvider();					
					if (ruleSetProvider) {
						// getRuleSets call set the global variable (window.top.ruleSets)
						// with available rule-sets
						ruleSetProvider.getRuleSets();	
					}
				} 
			};
		});