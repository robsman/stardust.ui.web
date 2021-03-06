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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_globalVariables", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_extensionManager" ],
		function(m_utils, m_globalVariables, m_constants, m_extensionManager) {
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
				
				return m_globalVariables.get("ruleSets");
			};
			
			function initRuleSets() {				
				if (!m_globalVariables.get("ruleSets")) {
					var ruleSetProvider = getRuleSetProvider();					
					if (ruleSetProvider) {
						m_globalVariables.set("ruleSets",ruleSetProvider.getRuleSets());	
					}
				} 
			};
		});