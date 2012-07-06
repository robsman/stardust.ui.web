/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_constants", "m_urlUtils", "m_communicationController",
				"m_application", "m_data", "m_process", "m_dataStructure",
				"m_participant" ],
		function(m_utils, m_constants, m_urlUtils, m_communicationController,
				m_application, m_data, m_process, m_dataStructure,
				m_participant) {

			return {
				stripModelId : stripModelId,

				stripElementId : stripElementId,

				loadModels : loadModels,

				getModels : getModels,

				createModel : function() {
					return new Model();
				},

				findModel : function(id) {
					return getModels()[id];
				},

				deleteModel : function(id) {
					delete getModels()[id];
				},

				findDataStructure : function(fullId) {
					return getModels()[stripModelId(fullId)].structuredDataTypes[stripElementId(fullId)];
				},

				findData : function(fullId) {
					return getModels()[stripModelId(fullId)].dataItems[stripElementId(fullId)];
				},

				findApplication : function(fullId) {
					return getModels()[stripModelId(fullId)].applications[stripElementId(fullId)];
				},

				findParticipant : function(fullId) {
					return getModels()[stripModelId(fullId)].participants[stripElementId(fullId)];
				},

				findProcess : function(fullId) {
					return getModels()[stripModelId(fullId)].processes[stripElementId(fullId)];
				},

				createModel : function(id, name) {
					var model = new Model();

					model.id = id;
					model.name = name;

					getModels()[id] = model;

					return model;
				},

				renameModel : function(id, newId, newName) {
					var model = getModels()[id];

					model.id = newId;
					model.name = newName;
					getModels()[newId] = model;

					delete getModels()[id];
				},
				findElementTypeByPath : function(path) {
					var steps = path.split("/");

					if (steps[1] != "models") {
						m_utils.debug("Path must contain /models element ("
								+ path + ").");
					}

					if (steps.length < 4) {
						return m_constants.MODEL;
					}

					if (steps[3] == "processes") {
						if (steps.length < 6) {
							return m_constants.PROCESS_DEFINITION;
						}

						return m_constants.ACTIVITY;
					} else if (steps[3] == "applications") {
						return m_constants.APPLICATION;
					} else if (steps[3] == "structuredDataTypes") {
						return m_constants.STRUCTURED_DATA_TYPE;
					} else if (steps[3] == "data") {
						return m_constants.DATA;
					} else if (steps[3] == "participants") {
						return m_constants.PARTICIPANT;
					} else {
						m_utils.debug("Unsupported model element type "
								+ steps[3] + " in path " + path)
								+ ".";
					}
				},
				findElementByPath : function(path) {
					var steps = path.split("/");

					if (steps[1] != "models") {
						m_utils.debug("Path must contain /models element ("
								+ path + ").");
					}

					var model = getModels()[steps[2]];

					if (steps.length == 3) {
						return model;
					}

					if (steps[3] == "processes") {
						if (steps.length == 5) {
							return model.processes[steps[4]];
						}

						if (steps[5] == "activities") {
							return model.processes[steps[4]].activities[steps[6]];
						}
					} else if (steps[3] == "applications") {
						if (steps.length < 5) {
							m_utils
									.debug("Path to application must contain 5 steps ("
											+ path + ").");
							return null;
						}

						return model.applications[steps[4]];
					} else if (steps[3] == "structuredDataTypes") {
						return model.structuredDataTypes[steps[4]];
					} else if (steps[3] == "data") {
						return model.dataItems[steps[4]];
					} else if (steps[3] == "participants") {
						return model.participants[steps[4]];
					} else {
						m_utils.debug("Unsupported model element " + steps[3]
								+ " in path " + path)
								+ ".";
					}
				}
			};

			/**
			 * 
			 */
			function stripModelId(fullId) {
				// TODO Change to format {modelId}/elementId once server has
				// been changed
				var ids = fullId.split(":");

				return ids[0];
			}

			/**
			 * 
			 */
			function stripElementId(fullId) {
				// TODO Change to format {modelId}/elementId once server has
				// been changed
				var ids = fullId.split(":");

				return ids[1];
			}

			/**
			 * 
			 */
			function Model() {
				this.type = m_constants.MODEL;
				this.id = null;
				this.name = null;
				this.processes = {};
				this.applications = {};
				this.dataItems = {};
				this.structuredDataTypes = {};
				this.participants = {};

				/**
				 * 
				 */
				Model.prototype.toString = function() {
					return "Lightdust.Model";
				};

				/**
				 * 
				 */
				Model.prototype.getFullId = function() {
					return this.id;
				};

				/**
				 * 
				 */
				Model.prototype.rename = function(id, name) {
					m_utils.debug("Renaming model " + this.id);
					delete getModels()[this.id];

					this.id = id;
					this.name = name;
					getModels()[this.id] = this;
				};

				/**
				 * 
				 */
				Model.prototype.getNewDataIndex = function() {
					var index = 0;

					for ( var n in this.dataItems) {
						++index;
					}

					++index;

					return index;
				};

				/**
				 * 
				 */
				Model.prototype.toJsonString = function() {
					return JSON.stringify(this);
				};

				/**
				 * 
				 */
				Model.prototype.getApplicationIndex = function() {
					var index = 0;

					for ( var n in this.application) {
						++index;
					}

					++index;

					return index;
				};

				/**
				 * 
				 */
				Model.prototype.getStructuredDataTypeIndex = function() {
					var index = 0;

					for ( var n in this.structuredDataTypes) {
						++index;
					}

					++index;

					return index;
				};
			}

			/**
			 * Singleton on DOM level.
			 */
			function getModels() {
				return window.top.models;
			}

			/**
			 * 
			 */
			function loadModels() {
				if (getModels() != null) {
					return;
				}

				refreshModels();
			}

			/**
			 * 
			 */
			function refreshModels() {
				m_communicationController.syncGetData({
					url : m_communicationController.getEndpointUrl()
							+ "/models"
				}, {
					"success" : function(json) {
						window.top.models = json;

						bindModels();
					},
					"error" : function() {
						alert('Error occured while fetching models');
					}
				});
			}

			/**
			 * 
			 */
			function bindModels() {
				for ( var model in getModels()) {
					bindModel(getModels()[model]);
				}
			}

			/**
			 * 
			 */
			function bindModel(model) {
				// TODO Ugly, user prototype

				m_utils.typeObject(model, new Model());

				m_utils.debug("Model before bind");
				m_utils.debug(model);

				for ( var process in model.processes) {
					m_process.createProcessFromJson(model,
							model.processes[process]);
				}

				for ( var dataStructure in model.structuredDataTypes) {
					m_dataStructure.initializeFromJson(model,
							model.structuredDataTypes[dataStructure]);
				}

				for ( var participant in model.participants) {
					m_participant.initializeFromJson(model,
							model.participants[participant]);
				}

				for ( var application in model.applications) {
					m_application.initializeFromJson(model,
							model.applications[application]);
				}

				for ( var dataItem in model.dataItems) {
					m_data.initializeFromJson(model, model.dataItems[dataItem]);
				}

				m_utils.debug("Model after bind");
				m_utils.debug(model);
			}
		});