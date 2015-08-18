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
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_model" ],
		function(m_utils, m_constants, m_model) {

			return {
				createDataFlow : function(process, data, activity) {
					var dataFlow = new DataFlow();

					dataFlow.initialize(data, activity);

					return dataFlow;
				},
				initializeFromJson : function(process, json) {
					// TODO Ugly, use prototype
					m_utils.typeObject(json, new DataFlow());

					json.initializeFromJson(process);

					return json;
				},
				prototype : DataFlow.prototype
			};

			/**
			 *
			 */
			function DataFlow(id) {
				this.type = m_constants.DATA_FLOW;
				this.id = id;
				this.description = null;
				this.inputDataPath = null;
				this.outputDataPath = null;
				this.inputAccessPointId = null;
				this.inputAccessPointContext = null;
				this.outputAccessPointId = null;
				this.outputAccessPointContext = null;

				/**
				 *
				 */
				DataFlow.prototype.toString = function() {
					return "Lightdust.DataFlow";
				};

				/**
				 *
				 */
				DataFlow.prototype.initialize = function(data, activity) {
					this.data = data;
					this.activity = activity;
				};

				/**
				 *
				 */
				DataFlow.prototype.initializeFromJson = function(process) {
					this.activity = process.activities[this.activityId];
					this.data = m_model.findData(this.dataFullId);
				};

				/**
				 *
				 */
				DataFlow.prototype.createTransferObject = function() {
					var transferObject = {};

					m_utils.inheritFields(transferObject, this);

					transferObject.dataFullId = this.data.getFullId();
					transferObject.activityId = this.activity.id;

					transferObject.data = null;
					transferObject.activity = null;

					return transferObject;
				};

				/**
				 * 
				 */
        DataFlow.prototype.inputOutputMappingExists = function() {
          var inputMappingExist = false;
          var outputMappingExist = false;
    
          var dataMappings = this.dataMappings
          for (var n = 0; n < dataMappings.length; n++) {
            if (dataMappings[n].direction == "IN") {
              inputMappingExist = true;
            }
            if (dataMappings[n].direction == "OUT") {
              outputMappingExist = true;
            }
            if (inputMappingExist && outputMappingExist) {
              break;
            }
          }
          return {
            input: inputMappingExist,
            output: outputMappingExist
          };
        }            

			}
		});