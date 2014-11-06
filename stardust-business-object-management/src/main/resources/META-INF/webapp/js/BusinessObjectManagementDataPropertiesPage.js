define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_constants",
				"bpm-modeler/js/m_commandsController",
				"bpm-modeler/js/m_command", "bpm-modeler/js/m_propertiesPage",
				"bpm-modeler/js/m_model" ],
		function(m_utils, m_constants, m_commandsController, m_command,
				m_propertiesPage, m_model) {
			return {
				create : function(propertiesPanel) {
					return new BusinessObjectManagementDataPropertiesPage(
							propertiesPanel);
				}
			};

			function BusinessObjectManagementDataPropertiesPage(
					newPropertiesPanel, newId, newTitle) {
				// Inheritance

				var propertiesPage = m_propertiesPage.createPropertiesPage(
						newPropertiesPanel,
						"businessObjectManagementDataPropertiesPage",
						"Business Object Management");

				m_utils.inheritFields(this, propertiesPage);
				m_utils.inheritMethods(
						BusinessObjectManagementDataPropertiesPage.prototype,
						propertiesPage);

				// TODO Hack to bind propertiesPanel; introduces a circular
				// reference
				// which prohibits printing

				this.propertiesPanel.propertiesPage = this;
				this.relationships = [];
				this.otherBusinessObjectTopLevelFields = {};
				this.managedOrganizations = [];

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.setElement = function() {
					this.propertiesPanel.propertiesPage.topLevelFields = this
							.getTopLevelFieldsForBusinessObject(this.propertiesPanel.data);
					this.otherBusinessObjects = [];
					this.otherBusinessObjectsMap = {};
					this.organizations = [];

					for ( var n in m_model.getModels()) {
						var model = m_model.getModels()[n];

						console.log(model);

						for ( var m in model.dataItems) {
							var data = model.dataItems[m];

							if (data.dataType != "struct"
							/*
							 * || !data.attributes["carnot:engine:primaryKey"]
							 */) {
								continue;
							}

							data.label = model.name + "/" + data.name;

							this.otherBusinessObjects.push(data);
							this.otherBusinessObjectsMap[model.id + "/"
									+ data.id] = data;
						}

						for ( var l in model.participants) {
							var participant = model.participants[l];

							if (participant.type != "organizationParticipant") {
								continue;
							}

							this.organizations.push({
								label : model.name + "/" + participant.name,
								fullId : participant.getFullId()
							});
						}
					}

					// Initialize relationships

					if (this.propertiesPanel.data.attributes["carnot:engine:businessObjectRelationships"]) {
						this.relationships = JSON
								.parse(this.propertiesPanel.data.attributes["carnot:engine:businessObjectRelationships"])

						// Keep backup to identify other side in case of changes

						this.relationshipsUnchanged = JSON
								.parse(this.propertiesPanel.data.attributes["carnot:engine:businessObjectRelationships"])

						for (var n = 0; n < this.relationships.length; ++n) {
							var otherBusinessObject = this.relationships[n].otherBusinessObject ? this.otherBusinessObjectsMap[this.relationships[n].otherBusinessObject.modelId
							                                                           										+ "/"
							                                                        										+ this.relationships[n].otherBusinessObject.id]
							                                                        										: null;
							this
									.loadOtherBusinessObjectTopLevelKeyFields(otherBusinessObject);

							this.relationships[n] = {
								otherBusinessObject : otherBusinessObject,
								otherRole : this.relationships[n].otherRole,
								otherCardinality : this.relationships[n].otherCardinality,
								otherForeignKeyField : this.relationships[n].otherForeignKeyField,
								thisRole : this.relationships[n].thisRole,
								thisCardinality : this.relationships[n].thisCardinality,
								thisForeignKeyField : this.relationships[n].thisForeignKeyField
							};
						}

						if (this.propertiesPanel.data.attributes["carnot:engine:managedOrganizations"]) {
							this.managedOrganizations = JSON
									.parse(this.propertiesPanel.data.attributes["carnot:engine:managedOrganizations"]);
						}
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.getTopLevelFieldsForBusinessObject = function(
						businessObject) {
					var topLevelFields = [];

					if (businessObject.structuredDataTypeFullId) {
						var typeDeclaration = m_model
								.findTypeDeclaration(businessObject.structuredDataTypeFullId);
						var fields = typeDeclaration.typeDeclaration.schema.elements[0].body[0].body;

						for (var n = 0; n < fields.length; ++n) {
							console.log("App Info");
							console.log(fields[n].appinfo);
							// if (!fields[n].appinfo) {
							// storage.indexed: "false"
							topLevelFields.push(fields[n]);
							// }
						}

					}

					return topLevelFields;
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.getData = function() {
					return propertiesPanel.data;
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.updateAttribute = function(
						key, value) {
					var modelElement = {
						attributes : {}
					};

					modelElement.attributes[key] = value;
					this.propertiesPanel.submitChanges(modelElement);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * Lazy loading.
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.loadOtherBusinessObjectTopLevelKeyFields = function(
						otherBusinessObject) {
					if (!this.otherBusinessObjectTopLevelFields[otherBusinessObject
							.getFullId()]) {
						this.otherBusinessObjectTopLevelFields[otherBusinessObject
								.getFullId()] = this
								.getTopLevelFieldsForBusinessObject(otherBusinessObject);
					}
				};

				/**
				 * Lazy loading.
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.changeOtherBusinessObject = function(
						relationship) {
					this.loadOtherBusinessObjectTopLevelKeyFields(relationship.otherBusinessObject);
					this.submitRelationshipChanges(relationship);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.removeRelationship = function(
						index) {
					this.relationships.splice(index, 1);

					// TODO Need more logic to remove inverse Relationship

					this.submitRelationshipsChanges(this.propertiesPanel.data,
							this.relationships);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.addRelationship = function() {
					this.relationships.push({
						otherCardinality : "TO_ONE",
						thisCardinality : "TO_ONE"
					});

					this.submitRelationshipsChanges(this.propertiesPanel.data,
							this.relationships);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.submitRelationshipChanges = function(
						relationship, index) {
					var otherBusinessObjectUnchanged = this.relationshipsUnchanged[index].otherBusinessObject;
					var thisRoleUnchanged = this.relationshipsUnchanged[index].thisRole;

					// Only for non-recursive Relationships

					// TODO Ugly comparison, but probably the price
					// of a semi-typed approach

					if ((relationship.otherBusinessObject.modelId + ":" + relationship.otherBusinessObject.id) != this.propertiesPanel.data
							.getFullId()) {
						// Apply changes to other BO

						var otherRelationships = null;
						var otherRelationship = null;

						if (relationship.otherBusinessObject.attributes["carnot:engine:businessObjectRelationships"]) {
							otherRelationships = JSON
									.parse(relationship.otherBusinessObject.attributes["carnot:engine:businessObjectRelationships"]);

							for (var n = 0; n < otherRelationships.length; ++n) {
								// TODO Ugly comparison, but probably the price
								// of a semi-typed approach

								var fullId = otherRelationships[n].otherBusinessObject.modelId
										+ ":"
										+ otherRelationships[n].otherBusinessObject.id;
								console.log("Finding recursive relationship");
								console.log(fullId);

								// TODO Also filter by unchanged role

								if ((fullId == this.propertiesPanel.data
										.getFullId())
										&& (otherRelationships.otherRole == thisRoleUnchanged)) {
									otherRelationship = otherRelationships[n];

									break;
								}
							}
						} else {
							otherRelationships = [];
						}

						// Make sure that there is a inverse relationship

						if (!otherRelationship) {
							otherRelationship = {};

							otherRelationships.push(otherRelationship);
						}

						// Apply all (possible) changes

						otherRelationship.otherRole = relationship.thisRole;
						otherRelationship.otherForeignKeyField = relationship.thisForeignKeyField;
						otherRelationship.otherCardinality = relationship.thisCardinality;
						otherRelationship.thisRole = relationship.otherRole;
						otherRelationship.thisForeignKeyField = relationship.otherForeignKeyField;
						otherRelationship.thisCardinality = relationship.otherCardinality;
					}

					this.submitRelationshipsChanges(this.propertiesPanel.data,
							this.relationships);

					// Only for non-recursive Relationships

					if (relationship.otherBusinessObject.getFullId() != this.propertiesPanel.data
							.getFullId()) {
						this.submitRelationshipsChanges(
								relationship.otherBusinessObject,
								otherRelationships);
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.submitRelationshipsChanges = function(
						businessObject, relationships) {
					var transfer = [];

					for (var n = 0; n < relationships.length; ++n) {
						transfer
								.push({
									otherBusinessObject : {
										id : relationships[n].otherBusinessObject.id,
										modelId : relationships[n].otherBusinessObject.modelId
									},
									otherRole : relationships[n].otherRole,
									otherCardinality : relationships[n].otherCardinality,
									otherForeignKeyField : relationships[n].otherForeignKeyField,
									thisRole : relationships[n].thisRole,
									thisCardinality : relationships[n].thisCardinality,
									thisForeignKeyField : relationships[n].thisForeignKeyField
								});
					}

					console.log("Transfer");
					console.log(transfer);

					if (businessObject.getFullId() == this.propertiesPanel.data
							.getFullId()) {
						this.propertiesPanel.submitModelElementAttributeChange(
								"carnot:engine:businessObjectRelationships",
								JSON.stringify(transfer));
					} else {
						// Submit change for other model element

						console.log("Update relationships for other BO");
						console.log(businessObject);
						console.log(transfer);

						var element = {
							modelElement : {
								attributes : {}
							}
						};

						element.modelElement.attributes["carnot:engine:businessObjectRelationships"] = JSON
								.stringify(transfer);

						m_utils.debug("Changes to be submitted for UUID "
								+ businessObject.oid + ":");
						m_utils.debug(element);
						m_commandsController.submitCommand(m_command
								.createUpdateModelElementCommand(
										businessObject.model.id,
										businessObject.oid,
										element.modelElement));
					}
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.submitManagedOrganizationsChanges = function() {
					console.log("Transfer");
					console.log(this.managedOrganizations);

					this.propertiesPanel.submitModelElementAttributeChange(
							'carnot:engine:managedOrganizations', JSON
									.stringify(this.managedOrganizations));
				};
			}
		});