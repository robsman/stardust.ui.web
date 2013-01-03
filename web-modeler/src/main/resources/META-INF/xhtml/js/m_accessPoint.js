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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_model", "bpm-modeler/js/m_parameter" ],
		function(m_utils, m_constants, m_model, m_parameter) {

			return {
				createDefault : function(id, name, direction) {
					var accessPoint = new AccessPoint();

					accessPoint.initializeDefault(id, name, direction);

					return accessPoint;
				},
				createFromDataStructure : function(dataStructure, id, name,
						direction) {
					var accessPoint = new AccessPoint();

					accessPoint.initializeFromDataStructure(dataStructure, id,
							name, direction);

					return accessPoint;
				},
				createFromPrimitive : function(primitiveType, id, name,
						direction) {
					var accessPoint = new AccessPoint();

					accessPoint.initializeFromPrimitive(primitiveType, id,
							name, direction);

					return accessPoint;
				},
				initializeFromJson : function() {
					// TODO Ugly, use prototype
					m_utils.typeObject(json, new AccessPoint());

					json.initializeFromJson();

					return json;
				},
				retrieveTypeDeclaration : function(accessPoint, scopeModel) {
					if (accessPoint.structuredDataTypeFullId != null) {
						return m_model
								.findTypeDeclaration(accessPoint.structuredDataTypeFullId);
					}

					return null;
				},
				prototype : AccessPoint.prototype
			};

			/**
			 * 
			 */
			function AccessPoint() {
				var parameter = m_parameter.create();

				m_utils.inheritFields(this, parameter);
				m_utils.inheritMethods(AccessPoint.prototype, parameter);

				/**
				 * 
				 */
				AccessPoint.prototype.initializeDefault = function(id, name,
						direction) {
					this.id = id;
					this.name = name;
					this.accessPointType = m_constants.ANY_ACCESS_POINT;
					this.structuredDataTypeFullId = null;
					this.primitiveDataType = null;
					this.direction = direction;
					this.attributes = {};
				};

				/**
				 * 
				 */
				AccessPoint.prototype.initializeFromDataStructure = function(
						dataStructure, id, name, direction) {
					this.id = id;
					this.name = name;
					this.accessPointType = m_constants.DATA_STRUCTURE_ACCESS_POINT;
					this.dataType = m_constants.STRUCTURED_DATA_TYPE;
					this.direction = direction;
					this.structuredDataTypeFullId = dataStructure.getFullId();
				};

				/**
				 * 
				 */
				AccessPoint.prototype.initializeFromPrimitive = function(
						primitiveType, id, name, direction) {
					this.id = id;
					this.name = name;
					this.accessPointType = m_constants.PRIMITIVE_ACCESS_POINT;
					this.dataType = m_constants.PRIMITIVE_DATA_TYPE;
					this.direction = direction;
					this.primitiveDataType = primitiveType;
				};

				/**
				 * 
				 */
				AccessPoint.prototype.initializeFromJson = function() {
				};
			}
		});