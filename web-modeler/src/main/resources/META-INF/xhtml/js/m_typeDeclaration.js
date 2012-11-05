/**
 * @author Marc.Gille
 */
define(
		[ "jquery", "m_utils", "m_modelElement" ],
		function(jQuery, m_utils, m_modelElement) {
			var STRUCTURE_TYPE = "STRUCTURE_TYPE";
			var ENUMERATION_TYPE = "ENUMERATION_TYPE";

			/**
			 *
			 */
			function TypeDeclaration() {
				m_utils.inheritMethods(TypeDeclaration.prototype,
						m_modelElement.create());

				/**
				 *
				 */
				TypeDeclaration.prototype.toString = function() {
					return "Lightdust.TypeDeclaration";
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.initialize = function(name, type) {
					this.name = name;
					this.type = type;
					// TODO Should be able to call getTypeDeclarations()

					// if (window.parent.typeDeclarations == null) {
					// window.parent.typeDeclarations = {};
					// }

					if (this.name != null) {
						getTypeDeclarations()[this.name] = this;
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.initializeFromJson = function(model) {
					this.model = model;
					this.model.typeDeclarations[this.id] = this;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.rename = function(id, name) {
					delete this.model.typeDeclarations[this.id];

					this.id = id;
					this.name = name;

					this.model.typeDeclarations[this.id] = this;
				};

				TypeDeclaration.prototype.getTypeDeclaration = function() {
					return this.typeDeclaration.schema.types[this.id];
				};

				TypeDeclaration.prototype.isReadOnly = function() {
					return (null != this.typeDeclaration.type)
							&& (this.typeDeclaration.type.classifier === 'ExternalReference');
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.isSequence = function() {
					return (null != this.getBody())
							&& (this.getBody().classifier === 'sequence');
				};

				TypeDeclaration.prototype.asSchemaType = function() {
					return this.resolveSchemaType(this.id);
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getBody = function() {
					return this.getTypeDeclaration().body;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getFacets = function() {
					return this.getTypeDeclaration().facets;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getSchemaName = function() {
					// TODO@Robert Review
					if ((null != this.typeDeclaration)
							&& this.typeDeclaration.schema
							&& this.typeDeclaration.schema.elements
							&& this.typeDeclaration.schema.elements[this.id]) {
						return this.typeDeclaration.schema.elements[this.id].type;
					}

					return null;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getElementCount = function() {
					var n = 0;

					jQuery.each(this.getElements(), function(i, element) {
						++n;
					});

					return n;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.createInstance = function() {
					if (this.isSequence()) {
						var instance = {};

						this
								.populateSequenceInstanceRecursively(this,
										instance);

						return instance;
					} else {
						return this.getFacets();
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.populateSequenceInstanceRecursively = function(
						typeDeclaration, instance) {
					for ( var id in typeDeclaration.getBody().elements) {
						var element = typeDeclaration.getBody().elements[id];

						var childTypeDeclaration = this.model
								.findTypeDeclarationBySchemaName(element.type);

						if (childTypeDeclaration != null) {
							if (childTypeDeclaration.isSequence()) {
								instance[id] = {};

								this.populateSequenceInstanceRecursively(
										childTypeDeclaration, instance[id]);
							} else {
								for ( var enumerator in childTypeDeclaration
										.getFacets()) {
									instance[id] = enumerator;

									break;
								}
							}
						} else {
							instance[id] = "";
						}
					}
					return instance;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToComplexType = function() {
					if (!this.isSequence()) {
						var td = this.getTypeDeclaration();
						delete td.type;
						delete td.facets;

						td.body = {
							name : "<sequence>",
							icon : "XSDModelGroupSequence.gif",
							classifier : "sequence",
							elements : {}
						};
						td.icon = "XSDComplexTypeDefinition.gif";
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToEnumeration = function() {
					if (this.isSequence()) {
						var td = this.getTypeDeclaration();
						delete td.body;

						td.type = "xsd:string";
						td.facets = {};
						td.icon = "XSDSimpleTypeDefinition.gif";
					}
				};

				TypeDeclaration.prototype.getElements = function() {
					return this.isSequence() ? this.getBody().elements : this
							.getFacets();
				};

				TypeDeclaration.prototype.getElement = function(name) {
					return this.getElements()[name];
				};

				TypeDeclaration.prototype.addElement = function(name) {
					// TODO detect if name is already present
					var newName = name || "New" + (this.getElementCount() + 1);

					var newElement;
					if (this.isSequence()) {
						newElement = {
							name : newName,
							type : "xsd:string",
							cardinality : "required"
						};
					} else {
						newElement = {
							name : newName,
							classifier : "enumeration"
						};
					}
					this.getElements()[newElement.name] = newElement;

					return newElement;
				};

				TypeDeclaration.prototype.renameElement = function(oldName,
						newName) {
					var elementContainer = this.getElements();

					var element = elementContainer[oldName];
					if (element) {
						delete elementContainer[oldName];
						element.name = newName;
						elementContainer[element.name] = element;
					}
				};

				TypeDeclaration.prototype.setElementType = function(name,
						typeName) {
					var element = this.getElement(name);
					if (element) {
						element.type = typeName;
					}
				};

				TypeDeclaration.prototype.getEffectiveElementType = function(
						name) {
					var element = this.getElement(name);
					if (element) {
						;
					}
				};

				TypeDeclaration.prototype.setElementCardinality = function(
						name, cardinality) {
					var element = this.getElement(name);
					if (element) {
						element.cardinality = cardinality;
					}
				};

				TypeDeclaration.prototype.removeElement = function(name) {
					delete this.getElements()[name];
				};

				TypeDeclaration.prototype.resolveElementType = function(name) {
					var element = this.getElement(name);
					if (element) {
						return this.resolveSchemaType(element.type);
					}
				};

				TypeDeclaration.prototype.resolveSchemaType = function(name) {
					var typeQName = name.split(":");
					if (1 == typeQName.length) {
						// no ns prefix, resolve to containing schema
						var typeName = typeQName[0];
						var schema = this.typeDeclaration.schema;
						var schemaNsUri = schema.targetNamespace;

						var type = schema.types[typeName];
						return new SchemaType(typeName, schemaNsUri, type, schema, this.model);
					} else if (2 == typeQName.length) {
						var schemaNsUri = this.typeDeclaration.schema.nsMappings[typeQName[0]];
						return resolveSchemaTypeFromModel("{" + schemaNsUri + "}" + typeQName[1], this.model);
					}
				};
			};

			function SchemaType(name, nsUri, type, schema, scope) {
				this.name = name;
				this.nsUri = nsUri;
				this.type = type;
				this.schema = schema;
				// scope is effectively the model with its type declarations
				this.scope = scope;
			};

			SchemaType.prototype.toString = function() {
				return "Lightdust.SchemaType";
			};

			SchemaType.prototype.isBuiltinType = function() {
				return (null == this.type) && (null == this.schema);
			};

			SchemaType.prototype.isStructure = function() {
				return (null != this.type)
						&& (null != this.type.body)
						&& ((this.type.body.classifier === 'sequence') || (this.type.body.classifier === 'choice'));
			};

			SchemaType.prototype.getElements = function() {
				if (this.isStructure()) {
					return this.type.body.elements;
				} else {
					return this.type.facets | [];
				}
			};

			SchemaType.prototype.getElement = function(name) {
				return this.getElements()[name];
			};

			SchemaType.prototype.resolveElementType = function(elementName) {
				var element = this.getElement(elementName);
				if (element) {

					var typeQName = element.type.split(":");
					if (1 == typeQName.length) {
						// no ns prefix, resolve to containing schema
						var typeName = typeQName[0];
						var type = this.schema.types[typeName];

						return new SchemaType(typeName, this.schema.targetNamespace, type, this.schema, this.scope);
					} else if (2 == typeQName.length) {
						var schemaNsUri = this.schema.nsMappings[typeQName[0]];
						return resolveSchemaTypeFromModel("{" + schemaNsUri + "}" + typeQName[1], this.scope);
					}
				} else {
					return undefined;
				}
			};

			function resolveSchemaTypeFromModel(sqName, model) {
				var parsedSQName = ("{" == sqName.charAt(0)) ? sqName.substr(1, sqName.length).split("}") : [];
				if (2 == parsedSQName.length) {
					// resolve ns prefix to schema
					var schemaNsUri = parsedSQName[0];
					var typeName = parsedSQName[1];

					if (schemaNsUri == "http://www.w3.org/2001/XMLSchema") {
						return new SchemaType("xsd:" + typeName, schemaNsUri);
					} else {
						var schema;
						jQuery.each(model.typeDeclarations, function(i, declaration) {
							if ((declaration.typeDeclaration != null)
									&& (declaration.typeDeclaration.schema != null)
									&& (declaration.typeDeclaration.schema.targetNamespace == schemaNsUri)) {
								schema = declaration.typeDeclaration.schema;
								return false;
							}
						});

						if (schema) {
							var type = schema.types[typeName];
							return new SchemaType(typeName, schemaNsUri, type, schema, model);
						}
					}
				}

				return undefined;
			};

			function resolveSchemaTypeFromSchema(typeName, schema) {
				var type = schema.types[typeName];
				if (type) {
					return new SchemaType(typeName, schema.targetNamespace, type, schema);
				}
			};

			// module interface
			return {
				createTypeDeclaration : function(name) {
					var typeDeclaration = new TypeDeclaration();

					typeDeclaration.initialize(name, STRUCTURE_TYPE);

					return typeDeclaration;
				},
				initializeFromJson : function(model, json) {
					// TODO Ugly, use prototype
					m_utils.inheritMethods(json, new TypeDeclaration());

					json.initializeFromJson(model);

					return json;
				},

				resolveSchemaTypeFromModel: function(sqName, model) {
					return resolveSchemaTypeFromModel(sqName, model);
				},
				resolveSchemaTypeFromSchema: function(typeName, schema) {
					return resolveSchemaTypeFromSchema(typeName, schema);
				},

				getPrimitiveTypeLabel : function(type) {
					if (type == null) {
						return "None"; // I18N
					}
					else if (type == "int") {
						return "Integer"; // I18N
					}
					else if (type == "string") {
						return "String"; // I18N
					}
					else if (type == "boolean") {
						return "Boolean"; // I18N
					}

					return type;
				},

				STRUCTURE_TYPE : STRUCTURE_TYPE,
				ENUMERATION_TYPE : ENUMERATION_TYPE
			};

		});