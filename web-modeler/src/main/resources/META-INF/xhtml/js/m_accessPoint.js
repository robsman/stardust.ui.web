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
		[ "m_utils", "m_constants", "m_model", "m_parameter" ],
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
				initializeFromJson : function() {
					// TODO Ugly, use prototype
					m_utils.typeObject(json, new AccessPoint());

					json.initializeFromJson();

					return json;
				},
				/**
				 * TODO Very ugly conversion, because server stores data
				 * reference in a server-specific string.
				 */
				retrieveTypeDeclaration : function(accessPoint, scopeModel) {
					// TODO Workaround for client site programming, this is not
					// what the server returns
					if (accessPoint.structuredDataTypeFullId != null) {
						return m_model
								.findTypeDeclaration(accessPoint.structuredDataTypeFullId);
					}

					var encodedId = accessPoint.attributes["carnot:engine:dataType"];

					if (encodedId == null) {
						return null;
					}

					if (encodedId.indexOf("typeDeclaration") == 0) {
						var parts = encodedId.split("{")[1].split("}");

						return m_model.findTypeDeclaration(parts[0] + ":"
								+ parts[1]);
					} else {
						return scopeModel.typeDeclarations[encodedId];
					}
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
					
					// Remove/move to server
					
					this.attributes = {
						"carnot:engine:dataType" : "typeDeclaration:{"
								+ dataStructure.model.id + "}"
								+ dataStructure.id
					};
				};

				/**
				 * 
				 */
				AccessPoint.prototype.initializeFromJson = function() {
				};
			}
		});