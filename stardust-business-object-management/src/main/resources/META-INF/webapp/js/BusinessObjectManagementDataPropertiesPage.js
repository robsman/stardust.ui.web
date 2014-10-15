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

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.setElement = function() {
					var typeDeclaration = m_model
							.findTypeDeclaration(this.propertiesPanel.data.structuredDataTypeFullId);
					this.propertiesPanel.propertiesPage.typeDeclaration = typeDeclaration;

					var fields = typeDeclaration.typeDeclaration.schema.elements[0].body[0].body;

					this.propertiesPanel.propertiesPage.topLevelFields = [];

					for (var n = 0; n < fields.length; ++n) {
						if (!fields[n].appinfo) {
							this.propertiesPanel.propertiesPage.topLevelFields
									.push(fields[n]);
						}
					}

					this.otherBusinessObjects = [];

					for ( var n in m_model.getModels()) {
						var model = m_model.getModels()[n];

						for ( var m in model.dataItems) {
							var data = model.dataItems[m];

							if (data.dataType != "struct"
							/*
							 * || !data.attributes["carnot:engine:primaryKey"]
							 */) {
								continue;
							}

							data.label = data.name + "/" + model.name;

							this.otherBusinessObjects.push(data);
						}
					}

					console.log("Other Business Object =====>");
					console.log(this.otherBusinessObjects);
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
				BusinessObjectManagementDataPropertiesPage.prototype.updatePrimaryKey = function(
						key, value) {
					console.log("Update PK: " + key, value);

					this.propertiesPanel.submitModelElementAttributeChange(key,
							value);
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.validate = function() {
					return true;
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.removeRelationship = function(
						index) {
					this.relationships.splice(index, 1);

					this.submitRelationshipsChanges();
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.addRelationship = function() {
					this.relationships.push({
						otherCardinality : "TO_ONE",
						thisCardinality : "TO_ONE"
					});

					this.submitRelationshipsChanges();
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.submitRelationshipChanges = function(
						relationship) {
					this.submitRelationshipsChanges();
				};

				/**
				 * 
				 */
				BusinessObjectManagementDataPropertiesPage.prototype.submitRelationshipsChanges = function() {
					console.log("Relationships");
					console.log(JSON.stringify(this.relationships));

					// this.propertiesPanel.submitModelElementAttributeChange(
					// 'carnot:engine:businessObjectRelationships', JSON
					// .stringify(this.relationships));
				};
			}
		});