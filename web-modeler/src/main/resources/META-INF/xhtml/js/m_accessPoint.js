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

define([ "m_utils", "m_constants", "m_model" ], function(m_utils, m_constants, m_model) {

	return {
		createDefault: function(id, name, direction)
		{
			var accessPoint = new AccessPoint();
			
			accessPoint.initializeDefault(id, name, direction);
			
			return accessPoint;
		},
		createFromDataStructure: function(dataStructure, id, name, direction)
		{
			var accessPoint = new AccessPoint();
			
			accessPoint.initializeFromDataStructure(dataStructure, id, name, direction);
			
			return accessPoint;
		},
		initializeFromJson: function()
		{
			// TODO Ugly, use prototype					
			m_utils.typeObject(json, new AccessPoint());

			json.initializeFromJson();

			return json;
		},
		prototype: AccessPoint.prototype
	};

	/**
	 * 
	 */
	function AccessPoint() {
		/**
		 * 
		 */
		AccessPoint.prototype.initializeDefault = function(id, name, direction) {
			this.id = id;
			this.name = name;
			this.accessPointType = m_constants.ANY_ACCESS_POINT;
			this.direction = direction;
			this.attributes = {};
		};

		/**
		 * 
		 */
		AccessPoint.prototype.initializeFromDataStructure = function(dataStructure, id, name, direction) {
			this.id = id;
			this.name = name;
			this.accessPointType = m_constants.DATA_STRUCTURE_ACCESS_POINT;
			this.dataType = m_constants.STRUCTURED_DATA_TYPE;
			this.structuredDataTypeFullId = dataStructure.getFullId();
			this.direction = direction;
			this.attributes = {};
			this.attributes["carnot:engine:dataType"] = dataStructure.id;
		};

		/**
		 * 
		 */
		AccessPoint.prototype.initializeFromJson = function() {			
		};
	}
});