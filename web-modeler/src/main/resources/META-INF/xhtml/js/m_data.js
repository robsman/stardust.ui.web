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

			data.initialize(model, "Data" + index, "Data " + index);

			data.type = m_constants.PRIMITIVE_DATA_TYPE;
			data.primitiveDataType = m_constants.STRING_PRIMITIVE_DATA_TYPE;

			return data;
		},
		
		createDataFromDataStructure : function(model, dataStructure) {
			var data = new Data();
			var index = model.getNewDataIndex();

			data.initialize(model, dataStructure.id + index,
					dataStructure.name + index, m_constants.STRUCTURED_DATA_TYPE);

			data.type = m_constants.STRUCTURED_DATA_TYPE;
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
		Data.prototype.initialize = function(model, id, name) {
			this.model = model;
			this.id = id;
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
		Data.prototype.createUpdateCommand = function() {
			return m_command.createUpdateCommand("/models/"
					+ this.model.id + "/data/"
					+ this.dataFullId, this.createTransferObject());
		};

		/**
		 * 
		 */
		Data.prototype.createDeleteCommand = function() {
			return m_command.createDeleteCommand("/models/"
					+ this.model.id + "/data/"
					+ this.dataFullId, this.createTransferObject());
		};
		
		/**
		 * 
		 */
		Data.prototype.submitCreation = function() {
			return m_commandsController.submitCommand(m_command
					.createCreateStructuredDataCommand(this.model.id, this.model.id,
							{
								"name" : this.name,
								"id" : this.id,
								"structuredDataTypeFullId" : this.structuredDataTypeFullId
							}));
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