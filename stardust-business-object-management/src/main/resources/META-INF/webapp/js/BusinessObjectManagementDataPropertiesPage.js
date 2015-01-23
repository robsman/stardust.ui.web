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
									|| !data.attributes["carnot:engine:primaryKey"]) {
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
							 if(participant.attributes["carnot:engine:bound"]){
								 this.organizations.push({
										label : model.name + "/" + participant.name,
										fullId : participant.getFullId()
									});
							 }
						}
					}
					// populate the model for Automatic Department creation
					if (this.propertiesPanel.data.attributes["carnot:engine:managedOrganizations"]) {
						this.managedOrganizations = JSON
								.parse(this.propertiesPanel.data.attributes["carnot:engine:managedOrganizations"]);
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

							// Lookup field objects from names in Extended
							// Attribute

							var otherForeignKeyField = this
									.findTopLevelFieldForBusinessObject(
											this.propertiesPanel.data,
											this.relationships[n].otherForeignKeyField);
							var thisForeignKeyField = this
									.findTopLevelFieldForBusinessObject(
											otherBusinessObject,
											this.relationships[n].thisForeignKeyField);

							this.relationships[n] = {
								otherBusinessObject : otherBusinessObject,
								otherRole : this.relationships[n].otherRole,
								otherForeignKeyField : otherForeignKeyField,
								thisRole : this.relationships[n].thisRole,
								thisForeignKeyField : thisForeignKeyField
							};
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
							console.log("Field");
							console.log(fields[n]);
							// if (!fields[n].appinfo) {
							// storage.indexed: "false"

							fields[n].cardinalityLabel = fields[n].name
									+ " ("
									+ (fields[n].cardinality == "many" ? "zero or more"
											: "zero or one") + ")";
							topLevelFields.push(fields[n]);
							// }
						}

					}

					return topLevelFields;
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.findTopLevelFieldForBusinessObject = function(
						businessObject, fieldName) {
					var fields = null;

					if (businessObject.getFullId() == this.propertiesPanel.data
							.getFullId()) {
						fields = this.propertiesPanel.propertiesPage.topLevelFields;
					} else {
						fields = this.otherBusinessObjectTopLevelFields[businessObject
								.getFullId()];
					}

					for (var n = 0; n < fields.length; ++n) {
						if (fields[n].name == fieldName) {
							return fields[n];
						}
					}

					throw "No field with name " + fieldName
							+ " in Business Object.";
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
					this
							.loadOtherBusinessObjectTopLevelKeyFields(relationship.otherBusinessObject);
					this.submitRelationshipChanges(relationship);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.removeRelationship = function(
						index) {
					var detachedRelationships = this.relationships.splice(index, 1);
					
					// TODO Need more logic to remove inverse Relationship

					this.submitRelationshipsChanges(this.propertiesPanel.data,
							this.relationships);
					
					var roleUnchanged = null;
					var relationship = null;
					if (detachedRelationships.length > 0) {
						relationship = detachedRelationships[0];
					}
					if(this.relationshipsUnchanged[index]){
						roleUnchanged = this.relationshipsUnchanged[index].thisRole;	
					}
					if (relationship && relationship.otherBusinessObject.attributes["carnot:engine:businessObjectRelationships"]) {
						// Read related businessObject
						otherRelationships = JSON
								.parse(relationship.otherBusinessObject.attributes["carnot:engine:businessObjectRelationships"]);

						console.log("Found inverse Relationship to remove");
						console.log(otherRelationships);

						for (var n = 0; n < otherRelationships.length; ++n) {
							// TODO Ugly comparison, but probably the price
							// of a semi-typed approach

							var fullId = otherRelationships[n].otherBusinessObject.modelId
									+ ":"
									+ otherRelationships[n].otherBusinessObject.id;

							if ((fullId == this.propertiesPanel.data
									.getFullId()) && (otherRelationships[n].otherRole == roleUnchanged)) {
								var rem = otherRelationships.splice(n,1);
								var otherBusinessObject = relationship.otherBusinessObject;
								// Create change object and save 
								var element = {
										modelElement : {
											attributes : {}
										}
									};

								element.modelElement.attributes["carnot:engine:businessObjectRelationships"] = JSON
											.stringify(otherRelationships);

								m_commandsController.submitCommand(m_command
										.createUpdateModelElementCommand(
												otherBusinessObject.model.id,
												otherBusinessObject.oid,
												element.modelElement));
								break;
							}
						}
					}
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
					// Submit changes for Relationships of this Business Object

					this.submitRelationshipsChanges(this.propertiesPanel.data,
							this.relationships);

					var thisRoleUnchanged = null;
					if(this.relationshipsUnchanged && this.relationshipsUnchanged.length > 0){
						var otherBusinessObjectUnchanged = this.relationshipsUnchanged[index].otherBusinessObject;
						thisRoleUnchanged = this.relationshipsUnchanged[index].thisRole;
					}
					 

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

							console.log("Found Relationships");
							console.log(otherRelationships);

							for (var n = 0; n < otherRelationships.length; ++n) {
								// TODO Ugly comparison, but probably the price
								// of a semi-typed approach

								var fullId = otherRelationships[n].otherBusinessObject.modelId
										+ ":"
										+ otherRelationships[n].otherBusinessObject.id;

								if ((fullId == this.propertiesPanel.data
										.getFullId())
										&& (otherRelationships[n].otherRole == thisRoleUnchanged)) {
									otherRelationship = otherRelationships[n];

									console.log("Found mathing Relationship");
									console.log(otherRelationship);

									break;
								}
							}
						} else {
							otherRelationships = [];
						}

						// Make sure that there is a inverse relationship

						if (!otherRelationship) {
							otherRelationship = {};
							otherRelationship.otherBusinessObject = this.propertiesPanel.data;
							otherRelationships.push(otherRelationship);
						}

						// Apply all (possible) changes to the inverse of this
						// relationship

						otherRelationship.otherRole = relationship.thisRole;
						otherRelationship.otherForeignKeyField = relationship.thisForeignKeyField;
						otherRelationship.thisRole = relationship.otherRole;
						otherRelationship.thisForeignKeyField = relationship.otherForeignKeyField;

						// Submit the adjusted Relationships of the other
						// Business Object

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
					console.log("submitRelationshipsChanges");
					console.log(businessObject);
					console.log(relationships);

					var transfer = [];

					for (var n = 0; n < relationships.length; ++n) {
						if (!relationships[n].otherBusinessObject) {
							return;
						}
						transfer
								.push({
									otherBusinessObject : {
										id : relationships[n].otherBusinessObject.id,
										modelId : relationships[n].otherBusinessObject.modelId
									},
									otherRole : relationships[n].otherRole,
									otherCardinality : relationships[n].otherForeignKeyField.cardinality == "many" ? "TO_MANY"
											: "TO_ONE",
									otherForeignKeyField : relationships[n].otherForeignKeyField.name,
									thisRole : relationships[n].thisRole,
									thisCardinality : relationships[n].thisForeignKeyField.cardinality == "many" ? "TO_MANY"
											: "TO_ONE",
									thisForeignKeyField : relationships[n].thisForeignKeyField.name
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