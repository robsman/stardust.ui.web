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
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants" ], function(m_utils, m_constants) {

	return {
		createControlFlow : function(process) {
			var controlFlow = new ControlFlow();
			return controlFlow;
		},

		prototype: ControlFlow.prototype
	};

	/**
	 *
	 */
	function ControlFlow(id) {
		this.type = m_constants.CONTROL_FLOW;
		this.id = id;
		this.name = "";
		this.attributes = {};
		this.conditionExpression = null;
		this.otherwise = false;

		/**
		 *
		 */
		ControlFlow.prototype.toString = function() {
			return "Lightdust.ControlFlow";
		};
	}
});