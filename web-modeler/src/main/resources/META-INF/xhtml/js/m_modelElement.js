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

define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants" ], function(m_utils, m_constants) {

	return {
		create: function()
		{
			return new ModelElement();
		},
		prototype: ModelElement.prototype
	};

	/**
	 *
	 */
	function ModelElement() {
		this.comments = [];

		/**
		 *
		 */
		ModelElement.prototype.getFullId = function() {
			return this.model.id + ":" + this.id;
		};

		/**
		 *
		 */
		ModelElement.prototype.isReadonly = function() {
			var rOnly = false;

			if (this.readonly != undefined) {
				rOnly = this.readonly;
			} else {
				if (this.model != undefined) {
					rOnly = this.model.isReadonly();
				}
			}

			return rOnly;
		};
	}
});