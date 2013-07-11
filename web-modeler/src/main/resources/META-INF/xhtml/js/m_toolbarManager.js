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
 * @author Shrikant.Gangal
 */

define(["bpm-modeler/js/m_constants", "bpm-modeler/js/m_utils"], function(m_constants, m_utils) {
	var _toolClickActionsMap;
	return {
		init: function(divId, toolClickActionsMap){
			addToolSpecificEventHandling(divId);
			_toolClickActionsMap = toolClickActionsMap;
		}
	}

	function addToolSpecificEventHandling(divId) {
		m_utils.jQuerySelect('#' + divId).click(function(e) {
			//Invokes a function mapped to the tool id (e.target.id) in object _toolClickActionsMap.
			if (false == m_utils.jQuerySelect(e.target).hasClass("toolDisabled")) {
				var methodName = e.target.id + "ToolSelected";
				if (typeof _toolClickActionsMap[methodName] == 'function')
				{
					_toolClickActionsMap[methodName]({toolId : e.target.id});
				}
			}
		});
	};
});