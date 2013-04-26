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
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_modelElement" ],
		function(m_utils, m_modelElement) {
			var STRUCTURE_TYPE = "STRUCTURE_TYPE";
			var ENUMERATION_TYPE = "ENUMERATION_TYPE";

			/**
			 * Represents a structured type.
			 *
			 * @constructor
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
				 * Initializes a the type declaration from JSON.
				 *
				 * @param {string}
				 *            name the type's name
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
					return findType(
							this.typeDeclaration.schema,
							("ExternalReference" === this.typeDeclaration.type.classifier) ? this.typeDeclaration.type.xref
									: this.id);
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
							&& (this.getBody().classifier === 'sequence'
								|| this.getBody().classifier === 'all'
								|| this.getBody().classifier === 'choice');
				};

				TypeDeclaration.prototype.asSchemaType = function() {
					return this
							.resolveSchemaType(("ExternalReference" === this.typeDeclaration.type.classifier && this.typeDeclaration.type.xref) ? this.typeDeclaration.type.xref
									: this.id);
				};

				TypeDeclaration.prototype.getType = function() {
					if ("ExternalReference" === this.typeDeclaration.type.classifier) {
						return "importedStructuredDataType";
					}
					if (this.isSequence()) {
						return "compositeStructuredDataType";
					} else {
						return "enumStructuredDataType";
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getBody = function() {
					if (this.getTypeDeclaration()) {
						return this.getTypeDeclaration().body;
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getFacets = function() {
					if (this.getTypeDeclaration()) {
						return this.getTypeDeclaration().facets;
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getSchemaName = function() {
					// TODO@Robert Review
					return this.id;

					/*
					 * var element = this.getElement(this.id); if (element) {
					 * return element.type; } return null;
					 */
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.getElementCount = function() {
					var n = 0;

					jQuery.each(this.getElements(), function() {
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

					var obj = this;
					jQuery.each(typeDeclaration.getBody().elements, function(i,
							element) {
						var type = element.type;

						// Strip prefix
						if (element.type.indexOf(':') !== -1) {
							type = element.type.split(":")[1];
						}

						var childTypeDeclaration = obj.model
								.findTypeDeclarationBySchemaName(type);

						if (childTypeDeclaration != null) {
							if (childTypeDeclaration.isSequence()) {
								instance[element.name] = {};

								obj.populateSequenceInstanceRecursively(
										childTypeDeclaration,
										instance[element.name]);
							} else {
								for ( var enumerator in childTypeDeclaration
										.getFacets()) {
									instance[element.name] = enumerator;

									break;
								}
							}
						} else {
							instance[element.name] = "";
						}
					});
					return instance;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToComplexType = function() {
					if (!this.isSequence()
							&& this.getTypeDeclaration()) {
						var td = this.getTypeDeclaration();
						delete td.type;
						delete td.facets;

						td.body = {
							name : "<sequence>",
							icon : "XSDModelGroupSequence.gif",
							classifier : "sequence",
							elements : []
						};
						td.icon = "XSDComplexTypeDefinition.gif";
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToEnumeration = function() {
					if (this.isSequence()
							&& this.getTypeDeclaration()) {
						var td = this.getTypeDeclaration();
						delete td.body;

						td.type = "xsd:string";
						td.facets = [];
						td.icon = "XSDSimpleTypeDefinition.gif";
					}
				};

				TypeDeclaration.prototype.getElements = function() {
					return this.isSequence() ? this.getBody().elements : this
							.getFacets();
				};

				TypeDeclaration.prototype.getElement = function(name) {
					var element;
					jQuery.each(this.getElements(), function() {
						if (this.name === name) {
							element = this;
							return false;
						}
					});
					return element;
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
					this.getElements().push(newElement);

					return newElement;
				};

				TypeDeclaration.prototype.moveElement = function(name, dIdx) {
					var element = this.getElement(name);
					if (element) {
						var elements = this.getElements();
						var oldIdx = elements.indexOf(element);
						var newIdx = oldIdx + dIdx;
						if ((0 <= newIdx) && (newIdx < elements.length)) {
							// remove at old position
							elements.splice(oldIdx, 1);
							// if (oldIdx <= newIdx) {
							// // adjust new position accordingly
							// --newIdx;
							// }
							// insert at new position
							elements.splice(newIdx, 0, element);
						}
					}
				};

				TypeDeclaration.prototype.renameElement = function(oldName,
						newName) {
					var element = this.getElement(oldName);
					if (element) {
						element.name = newName;
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
					var element = this.getElement(name);
					if (element) {
						var elements = this.getElements();
						elements.splice(elements.indexOf(element), 1);
					}
				};

				TypeDeclaration.prototype.resolveElementType = function(name) {
					var element = this.getElement(name);
					if (element) {
						return this.resolveSchemaType(element.type);
					}
				};

				TypeDeclaration.prototype.resolveSchemaType = function(name) {
					var typeQName = parseQName(name);
					if (typeQName.namespace) {
						return resolveSchemaTypeFromModel("{"
								+ typeQName.namespace + "}" + typeQName.name,
								this.model);
					} else {
						// no ns prefix, resolve to containing schema
						var schema = this.typeDeclaration.schema;
						var schemaNsUri = schema.targetNamespace;

						var type = findType(schema, typeQName.name);
						return new SchemaType(typeQName.name, schemaNsUri,
								type, schema, this.model);
					}
				};
			}
			;

			/**
			 * @constructor
			 */
			function SchemaType(name, nsUri, type, schema, scope) {
				this.name = name;
				this.nsUri = nsUri;
				this.type = type;
				this.schema = schema;
				// scope is effectively the model with its type declarations
				this.scope = scope;
			}
			;

			SchemaType.prototype.toString = function() {
				return "Lightdust.SchemaType";
			};

			/**
			 * @returns {Boolean}
			 */
			SchemaType.prototype.isBuiltinType = function() {
				return (null == this.type) && (null == this.schema);
			};

			/**
			 * @returns {Boolean}
			 */
			SchemaType.prototype.isStructure = function() {
				return (null != this.type)
						&& (null != this.type.body)
						&& ((this.type.body.classifier === 'sequence')
								|| (this.type.body.classifier === 'choice')
								|| (this.type.body.classifier === 'all'));
			};

			/**
			 * @returns {Boolean}
			 */
			SchemaType.prototype.isEnumeration = function() {
				return (null != this.type) && (null != this.type.facets);
			};

			/**
			 * @returns {Array}
			 */
			SchemaType.prototype.getElements = function() {
				if (this.isStructure()) {
					return this.type.body.elements;
				} else {
					return this.type.facets || [];
				}
			};

			SchemaType.prototype.getElement = function(name) {
				var element;
				jQuery.each(this.getElements(), function() {
					if (this.name === name) {
						element = this;
						return false;
					}
				})
				return element;
			};

			SchemaType.prototype.resolveElementType = function(elementName) {
				var element = this.getElement(elementName);
				if (element && element.type) {

					var typeQName = parseQName(element.type);
					if (!typeQName.prefix) {
						// no ns prefix, resolve to containing schema
						var type = findType(this.schema, typeQName.name);

						return new SchemaType(typeQName.name,
								this.schema.targetNamespace, type, this.schema,
								this.scope);
					} else {
						if (!typeQName.namespace && typeQName.prefix) {
							typeQName.namespace = this.schema.nsMappings[typeQName.prefix];
						}
						if (this.scope) {
							return resolveSchemaTypeFromModel("{"
									+ typeQName.namespace + "}"
									+ typeQName.name, this.scope);
						} else if (this.schema) {
							return resolveSchemaTypeFromSchema("{"
									+ typeQName.namespace + "}"
									+ typeQName.name, this.schema);
						}
					}
				} else {
					return undefined;
				}
			};

			function resolveSchemaTypeFromModel(sqName, model) {
				var schema;
				var parsedName = parseQName(sqName);
				if (parsedName.namespace) {
					// resolve ns prefix to schema
					if (parsedName.namespace === "http://www.w3.org/2001/XMLSchema") {
						return new SchemaType("xsd:" + parsedName.name,
								parsedName.namespace);
					} else if (model) {
						jQuery
								.each(
										model.typeDeclarations,
										function(i, declaration) {
											if ((null != declaration.typeDeclaration)
													&& (null != declaration.typeDeclaration.schema)
													&& (declaration.typeDeclaration.schema.targetNamespace === parsedName.namespace)) {
												schema = declaration.typeDeclaration.schema;
												return false;
											}
										});

						// Disabled as Kernal still doesn't support external
						// schema
						// TODO - review
						// Looping over all models as there can be external
						// references.
						// var allModels = model.getAllModels();
						// for (var i in allModels) {
						// var mod = window.top.models[i];
						// if (schema) {
						// break;
						// }
						// jQuery.each(mod.typeDeclarations, function(i,
						// declaration) {
						// if ((null != declaration.typeDeclaration)
						// && (null != declaration.typeDeclaration.schema)
						// &&
						// (declaration.typeDeclaration.schema.targetNamespace
						// === parsedName.namespace)) {
						// schema = declaration.typeDeclaration.schema;
						// return false;
						// }
						// });
						// }

						if (schema) {
							var type = findType(schema, parsedName.name);
							return new SchemaType(parsedName.name,
									parsedName.namespace, type, schema, model);
						}
					}
				}

				return undefined;
			}

			function parseQName(name) {
				if (name && ("{" === name.charAt(0))) {
					return {
						namespace : name.substr(1, name.length).split("}")[0],
						name : name.substr(1, name.length).split("}")[1]
					};
				} else if (name && (0 <= name.indexOf(":"))) {
					return {
						prefix : name.split(":")[0],
						name : name.split(":")[1]
					};
				} else {
					return {
						name : name
					};
				}
			}

			function findType(schema, typeName) {
				var parsedName = parseQName(typeName);
				var element;
				var type;

				// (fh) spec says we should search for elements
				if (!parsedName.namespace
						|| (schema && (parsedName.namespace === schema.targetNamespace))) {
					if (schema.elements) {
						jQuery.each(schema.elements, function() {
							if (this.name === parsedName.name) {
								element = this;
								return false;
							}
						});
					}

					if (element) {
						if (element.body) {
							// (fh) anonymous type declaration
							return element;
						}
						if (element.type) {
							// (fh) referenced type
							parsedName = parseQName(element.type);
							if (parsedName.prefix) {
								// (fh) resolve prefix to actual namespace
								parsedName.namespace = schema.nsMappings[parsedName.prefix];
							}
						}
					}
				}

				// (fh) now search the type
				if (!parsedName.namespace
						|| (schema && (parsedName.namespace === schema.targetNamespace))) {
					if (schema.types) {
						jQuery.each(schema.types, function() {
							if (this.name === parsedName.name) {
								type = this;
								return false;
							}
						});
					}
				}

				return type;
			}

			function resolveSchemaTypeFromSchema(typeName, schema) {
				var type = findType(schema, typeName);
				if (type) {
					return new SchemaType(typeName, schema.targetNamespace,
							type, schema);
				}
			}

			/**
			 * @returns {Array} the list of core XSD built-in types (roughly
			 *          equivalent to the list of Java primitive types)
			 */
			function getXsdCoreTypes() {
				return [ "string", "boolean", "long", "int", "short",
						"byte", "double", "float", "decimal", "date", "dateTime" ];
			}

			/**
			 * @returns {Array} the list of not so commonly used XSD built-in
			 *          types (all but the list of types equivalent to Java
			 *          primitive types)
			 */
			function getXsdExtraTypes() {
				var miscTypes = [];

				// stringy types
				miscTypes.push("ENTITIES", "ENTITY", "ID", "IDREF", "IDREFS",
						"language", "Name", "NCName", "NMTOKEN", "NMTOKENS",
						"normalizedString", "QName", "token");
				// numeric types
				miscTypes.push("integer", "negativeInteger",
						"nonNegativeInteger", "nonPositiveInteger",
						"positiveInteger", "unsignedLong", "unsignedInt",
						"unsignedShort", "unsignedByte");
				// data/time types
				miscTypes.push("time", "duration", "gDay", "gMonth",
						"gMonthDay", "gYear", "gYearMonth");
				// other
				miscTypes.push("anyURI", "base64Binary", "hexBinary",
						"NOTATION");

				miscTypes.sort();

				return miscTypes;
			}

			/**
			 *
			 */
			function generateJsonRepresentation(typeDeclarations) {
				var json = {};

				for ( var n = 0; n < typeDeclarations.length; ++n) {
					generateJsonRepresentationRecursively(json,
							typeDeclarations[n]);
				}

				return json;
			}

			/**
			 *
			 */
			function generateJsonRepresentationRecursively(
					typeDeclarationsJson, typeDeclaration) {
				if (typeDeclarationsJson[typeDeclaration.id]) {
					return;
				}

				var typeDeclarationJson = {};

				typeDeclarationsJson[typeDeclaration.id] = typeDeclarationJson;

				jQuery
						.each(
								typeDeclaration.getBody().elements,
								function(i, element) {
									var type = element.type;

									// Strip prefix

									if (element.type.indexOf(':') !== -1) {
										type = element.type.split(":")[1];
									}

									var childTypeDeclaration = typeDeclaration.model
											.findTypeDeclarationBySchemaName(type);

									if (childTypeDeclaration != null) {
										if (childTypeDeclaration.isSequence()) {
											typeDeclarationJson[element.name] = childTypeDeclaration.id;

											generateJsonRepresentationRecursively(
													typeDeclarationsJson,
													childTypeDeclaration);
										} else {
											if (element.cardinality == "many") {
												typeDeclarationJson[element.name] = [];
												typeDeclarationJson[element.name]
														.push(element.type);
											} else {
												typeDeclarationJson[element.name] = element.type;
											}
										}
									} else {
										if (element.cardinality == "many") {
											typeDeclarationJson[element.name] = [];
											typeDeclarationJson[element.name]
													.push(element.type);
										} else {
											typeDeclarationJson[element.name] = element.type;
										}
									}
								});
			}

			/**
			 * XSD based structured type declarations.
			 *
			 * @exports bpmModeler/js/m_typeDeclaration
			 */
			var moduleApi = {

				/**
				 * Creates a new TypeDeclaration instance.
				 *
				 * @param name
				 *            the type's name
				 * @returns {TypeDeclaration}
				 */
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

				/**
				 *
				 * @param {string}
				 *            sqName The schema qualified name of the type to
				 *            resolve.
				 * @param model
				 *            The model providing context for type resolution.
				 * @returns {SchemaType} The resolved schema type.
				 */
				resolveSchemaTypeFromModel : function(sqName, model) {
					return resolveSchemaTypeFromModel(sqName, model);
				},
				resolveSchemaTypeFromSchema : function(typeName, schema) {
					return resolveSchemaTypeFromSchema(typeName, schema);
				},

				getXsdCoreTypes : function() {
					return getXsdCoreTypes();
				},
				getXsdExtraTypes : function() {
					return getXsdExtraTypes();
				},

				parseQName : function(qName) {
					return parseQName(qName);
				},

				getPrimitiveTypeLabel : function(type) {
					if (type == null) {
						return "None"; // I18N
					} else if (type === "int") {
						return "Integer"; // I18N
					} else if (type === "string") {
						return "String"; // I18N
					} else if (type === "boolean") {
						return "Boolean"; // I18N
					}

					return type;
				},

				deleteTypeDeclaration : function(id, model) {
					delete model.typeDeclarations[id];
				},

				generateJsonRepresentation : generateJsonRepresentation,

				STRUCTURE_TYPE : STRUCTURE_TYPE,
				ENUMERATION_TYPE : ENUMERATION_TYPE
			};

			return moduleApi;
		});