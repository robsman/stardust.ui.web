/**
 * @author Marc.Gille
 */
define([ "m_utils", "m_constants", "m_model" ],
		function(m_utils, m_constants, m_model) {

			return {
				createDataFlow : function(process, data, activity) {
					var dataFlow = new DataFlow("DataFlow"
							+ process.getDataFlowIndex());

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
				this.inDataMapping = true;
				this.outDataMapping = true;
				this.dataPath = null;
				this.applicationPath = null;
				this.inAccessPointId = null;
				this.inContext = null;
				this.outAccessPointId = null;
				this.outContext = null;

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
			}
		});