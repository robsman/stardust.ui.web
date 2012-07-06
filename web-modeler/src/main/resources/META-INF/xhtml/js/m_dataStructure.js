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

define(
		[ "m_utils", "m_constants", "m_modelElement", "m_typeDeclaration" ],
		function(m_utils, m_constants, m_modelElement, m_typeDeclaration) {
			return {
				initializeFromJson : function(model, json) {					
					// TODO Ugly, use prototype					
					m_utils.typeObject(json, new DataStructure());

					json.initializeFromJson(model);

					return json;
				},
				deleteStructuredType : function(id,model) {
					delete model.structuredDataTypes[id];
				}
			};

			/**
			 * 
			 */
			function DataStructure() {
				m_utils.inheritMethods(DataStructure.prototype, m_modelElement.create());

				/**
				 * 
				 */
				DataStructure.prototype.toString = function() {
					return "Lightdust.DataStructure";
				};

				/**
				 * 
				 */
				DataStructure.prototype.initializeFromJson = function(model) {
					this.model = model;
					this.model.structuredDataTypes[this.id] = this;

					m_typeDeclaration.initializeFromJson(this.typeDeclaration);
				};

				/**
				 * 
				 */
				DataStructure.prototype.rename = function(id, name) {
					delete this.model.structuredDataTypes[this.id];

					this.id = id;
					this.name = name;

					this.model.structuredDataTypes[this.id] = this;

					// TODO Create other name
					
					if (this.typeDeclaration != null) {
						this.typeDeclaration.rename(this.model.id + ":" + this.id);
					}
				};
			}
		});