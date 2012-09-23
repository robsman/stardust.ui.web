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

define([ "m_utils", "m_constants", "m_modelElement", "m_command", "m_commandsController"], function(m_utils, m_constants, m_modelElement, m_command, m_commandsController) {
	return {
		/**
		 * @deprecated Is this still needed?
		 * 
		 * @param model
		 * @returns
		 */
		createData : function(model) {
			var data = new Data();
			var index = model.getNewDataIndex();

			data.initialize(model, "Data " + index);

			data.dataType = m_constants.PRIMITIVE_DATA_TYPE;
			data.primitiveDataType = m_constants.STRING_PRIMITIVE_DATA_TYPE;

			return data;
		},
		
		createDataFromDataStructure : function(model, dataStructure) {
			var data = new Data();
			var index = model.getNewDataIndex();

			data.initialize(model, dataStructure.name + index,
							m_constants.STRUCTURED_DATA_TYPE);

			data.dataType = m_constants.STRUCTURED_DATA_TYPE;
			data.structuredDataTypeFullId = dataStructure.getFullId();
			
			return data;
		},
		
		initializeFromJson : function(model, json) {
			// TODO Ugly, use prototype					
			m_utils.typeObject(json, new Data());

			json.initializeFromJson(model);

			return json;
		},

		deleteData : function(id, model) {
			delete model.dataItems[id];
		}
	};

	/**
	 * 
	 */
	function Data() {
		m_utils.inheritMethods(Data.prototype, m_modelElement.create());

		/**
		 * 
		 */
		Data.prototype.toString = function() {
			return "Lightdust.Data()";
		};

		/**
		 * 
		 */
		Data.prototype.initialize = function(model, name) {
			this.type = m_constants.DATA;
			this.model = model;
			this.id = m_utils.generateIDFromName(name);
			this.name = name;

			// TODO This implies that even data created implicitly from data symbol creation would remain in the model 
			this.model.dataItems[this.id] = this;
		};

		/**
		 * 
		 */
		Data.prototype.initializeFromJson = function(model) {
			this.model = model;
			this.model.dataItems[this.id] = this;
		};
		
		/**
		 * 
		 */
		Data.prototype.createTransferObject = function() {
			var transferObject = {};

			m_utils.inheritFields(transferObject, this);

			transferObject.path = null;
			transferObject.text = null;
			transferObject.model = null;

			return transferObject;
		};

		/**
		 * 
		 */
		Data.prototype.submitCreation = function() {
			var command =m_command
					.createCreateStructuredDataCommand(this.model.id, this.model.id,
							{
								"name" : this.name,
								"id" : this.id,
								"structuredDataTypeFullId" : this.structuredDataTypeFullId
							});
			command.sync = true; // sync submit
			return m_commandsController.submitCommand(command);
		};

		/**
		 * 
		 */
		Data.prototype.rename = function(id, name)
		{
			delete this.model.dataItems[this.id];

			this.id = id;
			this.name = name;

			this.model.dataItems[this.id] = this;
		};

		/**
		 * 
		 */
		Data.prototype.onCreate = function() {
		};
	}
});