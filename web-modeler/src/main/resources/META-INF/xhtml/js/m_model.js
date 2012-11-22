/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants", "bpm-modeler/js/m_urlUtils", "bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_application", "bpm-modeler/js/m_data", "bpm-modeler/js/m_process", "bpm-modeler/js/m_participant",
				"bpm-modeler/js/m_typeDeclaration" ],
		function(m_utils, m_constants, m_urlUtils, m_communicationController,
				m_application, m_data, m_process, m_participant,
				m_typeDeclaration) {

			return {
				stripModelId : stripModelId,

				stripElementId : stripElementId,

				loadModels : loadModels,

				getModels : getModels,

				createModel : function() {
					return new Model();
				},

				findModel : findModel,

				deleteModel : function(id) {
					delete getModels()[id];
				},

				findTypeDeclaration : function(fullId) {
					return findModel(stripModelId(fullId)).typeDeclarations[stripElementId(fullId)];
				},

				findData : function(fullId) {
					return findModel(stripModelId(fullId)).dataItems[stripElementId(fullId)];
				},

				findApplication : function(fullId) {
					return findModel(stripModelId(fullId)).applications[stripElementId(fullId)];
				},

				findParticipant : function(fullId) {
					return findModel(stripModelId(fullId)).participants[stripElementId(fullId)];
				},

				findProcess : function(fullId) {
					return findModel(stripModelId(fullId)).processes[stripElementId(fullId)];
				},
				createModel : function(id, name, uuid) {
					var model = new Model();

					model.id = id;
					model.name = name;
					model.uuid = uuid;

					getModels()[id] = model;

					return model;
				},
				renameModel : function(id, newId, newName) {
					var model = findModel(id);

					model.id = newId;
					model.name = newName;
					getModels()[newId] = model;

					delete getModels()[id];
				},
				/**
				 * TODO May not be safe as element OIDs are not unique.
				 *
				 * @param guid
				 * @returns
				 */
				findModelElementByGuid : function(guid) {
					var model = null;
					var element = null;

					for ( var model in getModels()) {
						if ((element = getModels()[model]
								.findModelElementByGuid(guid)) != null) {
							return element;
						}
					}

					return null;
				},

				findModelByUuid : function(uuid) {
					var model = null;

					for ( var model in getModels()) {
						if (uuid == getModels()[model].uuid) {
							return getModels()[model];
						}
					}

					return null;
				},

				/**
				 * Fetches the model for given element UUID.
				 *
				 * @param elementUUID
				 * @returns model
				 */
				findModelForElement : function(elementUuid) {
					var model = null;

					for ( var index in getModels()) {
						model = getModels()[index];
						if (model.findModelElementByUuid(elementUuid) != null) {
							return model;
						}
					}

					return null;
				},

				/**
				 * Fetches the element with given OID within the given modelId.
				 *
				 * @param guid
				 * @returns
				 */
				findElementInModelByOid : function(modelId, oid) {
					var model = null;
					var element = null;

					for ( var index in getModels()) {
						model = getModels()[index];
						if (model.id == modelId
								&& (element = model.findModelElementByGuid(oid)) != null) {
							return element;
						}
					}

					return null;
				},

				/**
				 * Fetches the element with given UUID within the given modelId.
				 *
				 * @param guid
				 * @returns
				 */
				findElementInModelByUuid : function(modelId, uuid) {
					var model = null;
					var element = null;

					for ( var index in getModels()) {
						model = getModels()[index];
						if (model.id == modelId
								&& (element = model
										.findModelElementByUuid(uuid)) != null) {
							return element;
						}
					}

					return null;
				},

				/**
				 *
				 */
				getFullId : function(model, symbolId) {
					return model.id + ":" + symbolId;
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
				this.typeDeclarations = {};
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

					for ( var n in this.applications) {
						++index;
					}

					++index;

					return index;
				};

				Model.prototype.findModelElementByUuid = function(uuid) {
					var n;

					for (n in this.processes) {
						if (this.processes[n].uuid == uuid) {
							return this.processes[n];
						}
					}

					for (n in this.applications) {
						if (this.applications[n].uuid == uuid) {
							return this.applications[n];
						}
					}

					for (n in this.dataItems) {
						if (this.dataItems[n].uuid == uuid) {
							return this.dataItems[n];
						}
					}

					for (n in this.participants) {
						if (this.participants[n].uuid == uuid) {
							return this.participants[n];
						}
					}

					for (n in this.typeDeclarations) {
						if (this.typeDeclarations[n].uuid == uuid) {
							return this.typeDeclarations[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				Model.prototype.findModelElementById = function(id) {
					var n;

					for (n in this.processes) {
						if (this.processes[n].id == id) {
							return this.processes[n];
						}
					}

					for (n in this.applications) {
						if (this.applications[n].id == id) {
							return this.applications[n];
						}
					}

					for (n in this.dataItems) {
						if (this.dataItems[n].id == id) {
							return this.dataItems[n];
						}
					}

					for (n in this.participants) {
						if (this.participants[n].id == id) {
							return this.participants[n];
						}
					}

					for (n in this.typeDeclarations) {
						if (this.typeDeclarations[n].id == id) {
							return this.typeDeclarations[n];
						}
					}

					return null;
				};

				/**
				 *
				 */
				Model.prototype.findTypeDeclarationBySchemaName = function(
						schemaName) {
					for ( var n in this.typeDeclarations) {
						var typeDeclaration = this.typeDeclarations[n];

						if (typeDeclaration.getSchemaName() == schemaName) {
							return typeDeclaration;
						}
					}

					return null;
				};
			}

			/**
			 * Singleton on DOM level.
			 */
			function getModels() {
				return window.top.models;
			}

			function findModel(id) {
				return getModels()[id];
			}

			/**
			 *
			 */
			function loadModels(force) {
				if (!force && getModels() != null) {
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

				for ( var typeDeclaration in model.typeDeclarations) {
					m_typeDeclaration.initializeFromJson(model,
							model.typeDeclarations[typeDeclaration]);
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