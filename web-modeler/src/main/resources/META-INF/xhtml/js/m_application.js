/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

define([ "m_utils", "m_constants", "m_modelElement" ], function(m_utils,
		m_constants, m_modelElement) {
	return {
		initializeFromJson : function(model, json) {
			// TODO Ugly, use prototype
			m_utils.typeObject(json, new Application());

			json.initializeFromJson(model);

			return json;
		},
		deleteApplication : function(id, model) {
			delete model.applications[id];
		}
	};

	/**
	 * 
	 */
	function Application() {
		m_utils.inheritMethods(Application.prototype, m_modelElement.create());

		/**
		 * 
		 */
		Application.prototype.toString = function() {
			return "Lightdust.Application";
		};

		/**
		 * 
		 */
		Application.prototype.initializeFromJson = function(model) {
			this.model = model;

			this.model.applications[this.id] = this;
		};

		/**
		 * 
		 */
		Application.prototype.rename = function(id, name) {
			delete this.model.applications[this.id];

			this.id = id;
			this.name = name;

			this.model.applications[this.id] = this;
		};

		/**
		 * 
		 */
		Application.prototype.getAccessPointById = function(id) {
			for ( var n = 0; n < this.accessPoints.length; ++n) {
				if (this.accessPoints[n].id == id) {
					return this.accessPoints[n];
				}
			}

			return null;
		};
	}
});