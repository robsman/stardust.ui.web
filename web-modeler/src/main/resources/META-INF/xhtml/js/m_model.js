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
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_urlUtils",
				"bpm-modeler/js/m_communicationController",
				"bpm-modeler/js/m_application", "bpm-modeler/js/m_data",
				"bpm-modeler/js/m_process", "bpm-modeler/js/m_participant",
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

				findElementByUuid : findElementByUuid,

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
				},

				isModelReferencedIn : isModelReferencedIn
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
				 * returns an array of elements with matching IDs.
				 */
				Model.prototype.findModelElementsById = function(id) {
					var elems = [];
					var n;

					for (n in this.processes) {
						if (this.processes[n].id == id) {
							elems.push(this.processes[n]);
						}
					}

					for (n in this.applications) {
						if (this.applications[n].id == id) {
							elems.push(this.applications[n]);
						}
					}

					for (n in this.dataItems) {
						if (this.dataItems[n].id == id) {
							elems.push(this.dataItems[n]);
						}
					}

					for (n in this.participants) {
						if (this.participants[n].id == id) {
							elems.push(this.participants[n]);
						}
					}

					for (n in this.typeDeclarations) {
						if (this.typeDeclarations[n].id == id) {
							elems.push(this.typeDeclarations[n]);
						}
					}

					return elems;
				};

				/**
				 * Used to identify if an element with same name exists, used
				 * while determining name for newly created element.
				 */
				Model.prototype.findModelElementByName = function(name) {
					var n;

					for (n in this.processes) {
						if (this.processes[n].name == name) {
							return this.processes[n];
						}
					}

					for (n in this.applications) {
						if (this.applications[n].name == name) {
							return this.applications[n];
						}
					}

					for (n in this.dataItems) {
						if (this.dataItems[n].name == name) {
							return this.dataItems[n];
						}
					}

					for (n in this.participants) {
						if (this.participants[n].name == name) {
							return this.participants[n];
						}
					}

					for (n in this.typeDeclarations) {
						if (this.typeDeclarations[n].name == name) {
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
				if (window.top.models) {
					return window.top.models;
				}
				loadModels(true);

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
			function findElementByUuid(uuid) {
				var models = getModels();
				for ( var i in models) {
					var elem = models[i].findModelElementByUuid(uuid);
					if (elem) {
						return elem;
					}
				}
			}

			/**
			 * As part of CRNT-28015. a regular function has been used instead of the
			 * singleton object DefaultModelManager / window.top.modelManager
			 * as it was apparently giving problems (on model loading) in IOD env. when releases were switched.
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

			/**
			 *
			 */
			function isModelReferencedIn(modelId1, modelId2) {
				var model1 = findModel(modelId1);
				var model2 = findModel(modelId2);

				if (model1 && model2) {
					for (n in model2.dataItems) {
						if ((model2.dataItems[n][m_constants.EXTERNAL_REFERENCE_PROPERTY]
							&& stripModelId(model2.dataItems[n].dataFullId) === model1.id)
								|| (model2.dataItems[n].structuredDataTypeFullId
										&& stripModelId(model2.dataItems[n].structuredDataTypeFullId) === model1.id)) {
							return true;
						}
					}

					for (n in model2.typeDeclarations) {
						if (model2.typeDeclarations[n][m_constants.EXTERNAL_REFERENCE_PROPERTY]
								&& stripModelId(model2.typeDeclarations[n]
										.getFullId()) === model1.id) {
							return true;
						}
					}

					for (n in model2.participants) {
						if (model2.participants[n][m_constants.EXTERNAL_REFERENCE_PROPERTY]
								&& stripModelId(model2.participants[n].participantFullId) === model1.id) {
							return true;
						}
					}

					for (n in model2.processes) {
						for (m in model2.processes[n].activities) {
							if (model2.processes[n].activities[m].applicationFullId
									&& stripModelId(model2.processes[n].activities[m].applicationFullId) === model1.id) {
								return true;
							} else if (model2.processes[n].activities[m].subprocessFullId
									&& stripModelId(model2.processes[n].activities[m].subprocessFullId) === model1.id) {
								return true;
							}
						}
					}
				}

				return false;
			}
		});