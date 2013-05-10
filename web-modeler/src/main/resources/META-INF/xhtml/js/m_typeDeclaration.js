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
					var body = this.getBody();
					if (body) {
						for (i in body) {
							if (!body[i].inherited
									&& (body[i].classifier === 'sequence'
											|| body[i].classifier === 'all' || body[i].classifier === 'choice')) {
								return true;
							}
						}
					}

					return false;
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
					var facets = [];
					if (this.typeDeclaration
							&& this.typeDeclaration.schema && this.typeDeclaration.schema.elements) {
						for (var i in this.typeDeclaration.schema.elements) {
							if (this.typeDeclaration.schema.elements[i].facets) {
								for (var j in this.typeDeclaration.schema.elements[i].facets) {
									facets.push(this.typeDeclaration.schema.elements[i].facets[j]);
								}
							}
						}
					}
					if (this.typeDeclaration.schema.types) {
						for (var i in this.typeDeclaration.schema.types) {
							if (this.typeDeclaration.schema.types[i].facets) {
								for (var j in this.typeDeclaration.schema.types[i].facets) {
									facets.push(this.typeDeclaration.schema.types[i].facets[j]);
								}
							}
						}
					}

					return facets;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.deleteFacet = function(classifier) {
					if (this.typeDeclaration
							&& this.typeDeclaration.schema && this.typeDeclaration.schema.elements) {
						for (var i in this.typeDeclaration.schema.elements) {
							if (this.typeDeclaration.schema.elements[i].facets) {
								for (var j in this.typeDeclaration.schema.elements[i].facets) {
									if (this.typeDeclaration.schema.elements[i].facets[j].classifier === classifier) {
										this.typeDeclaration.schema.elements[i].facets.splice(j, 1);
									}
								}
							}
						}
					}
				};


				/**
				 *
				 */
				TypeDeclaration.prototype.addFacet = function(facet) {
					if (this.typeDeclaration
							&& this.typeDeclaration.schema && this.typeDeclaration.schema.elements) {
						for (var i in this.typeDeclaration.schema.elements) {
							if (this.typeDeclaration.schema.elements[i].facets) {
								this.typeDeclaration.schema.elements[i].facets.push(facet);
							}
						}
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
				TypeDeclaration.prototype.createInstance = function(options) {
					if (!options) {
						options = {
							initializePrimitives : true
						};
					}

					if (this.isSequence()) {
						var instance = {};

						this.populateSequenceInstanceRecursively(this.asSchemaType(),
								instance, options);

						return instance;
					} else {
						return this.getFacets();
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.populateSequenceInstanceRecursively = function(
						typeDeclaration, instance, options) {

					var obj = this;
					jQuery
							.each(
									typeDeclaration.getElements(),
									function(i, element) {
										var type = element.type;

										// Strip prefix
										if (element.type.indexOf(':') !== -1) {
											type = element.type.split(":")[1];
										}

										var childTypeDeclaration = obj.model
												.findTypeDeclarationBySchemaName(type);

										if (element.cardinality === "required") {
											if (childTypeDeclaration != null) {
												if (childTypeDeclaration
														.isSequence()) {

													instance[element.name] = {};

													obj
															.populateSequenceInstanceRecursively(
																	childTypeDeclaration,
																	instance[element.name],
																	options);
												} else {
													if (options.initializePrimitives) {
														for ( var enumerator in childTypeDeclaration
																.getFacets()) {
															instance[element.name] = enumerator;

															break;
														}
													}
												}
											} else {
												if (options.initializePrimitives) {
													// TODO Consider primitive
													// type

													instance[element.name] = "";
												}
											}
										} else {
											instance[element.name] = [];

											if (childTypeDeclaration != null
													&& childTypeDeclaration
															.isSequence()) {
												instance[element.name][0] = {};

												obj
														.populateSequenceInstanceRecursively(
																childTypeDeclaration,
																instance[element.name][0],
																options);
											} else {
												// TODO Consider primitive type

												instance[element.name][0] = "";
											}
										}
									});

					return instance;
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToComplexType = function() {
					if (!this.isSequence() && this.getTypeDeclaration()) {
						var td = this.getTypeDeclaration();
						delete td.facets;
						delete td.base;
						delete td.method;

						td.body = [{
							name : "<sequence>",
							icon : "XSDModelGroupSequence.gif",
							classifier : "sequence",
							body : []
						}];
						td.icon = "XSDComplexTypeDefinition.gif";
					}
				};

				/**
				 *
				 */
				TypeDeclaration.prototype.switchToEnumeration = function() {
					if (this.isSequence() && this.getTypeDeclaration()) {
						var td = this.getTypeDeclaration();
						delete td.body;

						td.base = "{http://www.w3.org/2001/XMLSchema}string";
						td.facets = [];
					}
				};

				TypeDeclaration.prototype.getElements = function() {
					if (this.isSequence()) {
						var elements = [];
						var body = this.getBody();
						if (body) {
							getElementsFromBody(body, elements);
						}

						return elements;
					} else {
						return this.getFacets();
					}
				};

				TypeDeclaration.prototype.getUninheritedElements = function() {
					if (this.isSequence()) {
						var body = this.getBody();
						if (body) {
							for (var i in body) {
								if (!body[i].inherited && body[i].body) {
									return body[i].body;
								}
							}
						}
					} else {
						if (this.typeDeclaration
								&& this.typeDeclaration.schema && this.typeDeclaration.schema.elements) {
							for (var i in this.typeDeclaration.schema.elements) {
								if (!this.typeDeclaration.schema.elements.inherited) {
									return this.typeDeclaration.schema.elements[i].facets;
								}
							}
						}
					}
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
							cardinality : "required",
							classifier : "element"
						};
						this.addNewElementToSequence(newElement);
					} else {
						newElement = {
							name : newName,
							classifier : "enumeration"
						};
						this.addFacet(newElement);
					}

					return newElement;
				};

				TypeDeclaration.prototype.addNewElementToSequence = function(newElement) {
					if (this.isSequence()) {
						var body = this.getBody();
						var thisBody;
						for (var i in body) {
							if (!body[i].inherited) {
								body[i].body = body[i].body ? body[i].body : [];
								thisBody = body[i].body;
							}
						}

						// Additional check - may not be needed
						if (!thisBody) {
							body[0].body = [];
							thisBody = body[0].body;
						}

						thisBody.push(newElement);
					}
				};

				TypeDeclaration.prototype.addFacet = function(newElement) {
					if (this.typeDeclaration
							&& this.typeDeclaration.schema && this.typeDeclaration.schema.elements) {
						for (var i in this.typeDeclaration.schema.elements) {
							if (!this.typeDeclaration.schema.elements.inherited) {
								if (!this.typeDeclaration.schema.elements[i].facets) {
									this.typeDeclaration.schema.elements[i].facets = [];
								}
								this.typeDeclaration.schema.elements[i].facets.push(newElement);
								break;
							}
						}
					}
				};

				TypeDeclaration.prototype.moveElement = function(name, dIdx) {
					var moved = false;

					var element = this.getElement(name);
					if (element && !element.readOnly) {
						var elements = this.getUninheritedElements();
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
							moved = true;
						}
					}

					return moved;
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
						var elements = this.getUninheritedElements();
						var idx = elements.indexOf(element);
						if (idx >= 0) {
							elements.splice(idx, 1);
						}
					}
				};

				TypeDeclaration.prototype.resolveElementType = function(name) {
					var element = this.getElement(name);
					if (element) {
						return this.resolveSchemaType(element.type);
					}
				};


				TypeDeclaration.prototype.setBaseType = function(baseType) {
					if (baseType) {
						this.getTypeDeclaration().base = "{" + baseType.typeDeclaration.schema.targetNamespace + "}" + baseType.id;
						this.getTypeDeclaration().method = "extension";
					} else {
						this.getTypeDeclaration().base = null;
						this.getTypeDeclaration().method = null;
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
//				return (null != this.type)
//						&& (null != this.type.body)
//						&& ((this.type.body.classifier === 'sequence')
//								|| (this.type.body.classifier === 'choice') || (this.type.body.classifier === 'all'));

				// TODO - check
				if (this.type) {
					var type = this.type
				} else if (this.schema && this.schema.types) {
					var type = this.schema.types[0];
				}
				if (type && type.body) {
					for (i in type.body) {
						if (!type.body[i].inherited
								&& (type.body[i].classifier === 'sequence'
										|| type.body[i].classifier === 'all' || type.body[i].classifier === 'choice')) {
							return true;
						}
					}
				}

				return false;
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
				// TODO - check
				if (this.isStructure()) {
					var elements = [];
					if (this.type) {
						var type = this.type
					} else if (this.schema && this.schema.types) {
						var type = this.schema.types[0];
					}

					if (type && type.body) {
						getElementsFromBody(type.body, elements);
					}

					return elements;
				} else {
					var facets = [];
					if (this.schema && this.schema.elements) {
						for (var i in this.schema.elements) {
							if (this.schema.elements[i].facets) {
								for (var j in this.schema.elements[i].facets) {
									if (this.schema.elements[i].facets[j].classifier === "enumeration") {
										facets.push(this.schema.elements[i].facets[j]);
									}
								}
							}
						}
					}
					if (this.schema.types) {
						for (var i in this.schema.types) {
							if (this.schema.types[i].facets) {
								for (var j in this.schema.types[i].facets) {
									if (this.schema.types[i].facets[j].classifier === "enumeration") {
										facets.push(this.schema.types[i].facets[j]);
									}
								}
							}
						}
					}

					return facets;
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

			function getElementsFromBody(body, elements, inherited) {
				for (var i in body) {
					if (body[i] && body[i].classifier !== "element") {
						getElementsFromBody(body[i].body, elements, (inherited || body[i].inherited));
					} else {
						if (inherited || body[i].inherited) {
							body[i].readOnly = true;
						}
						elements.push(body[i]);
					}
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
													&& (declaration.typeDeclaration.schema.targetNamespace === parsedName.namespace)
													&& (declaration.id === parsedName.name)) {
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
						if (element.body || element.facets) {
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
				return [ "string", "boolean", "long", "int", "short", "byte",
						"double", "float", "decimal", "date", "dateTime" ];
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
								typeDeclaration.getElements(),
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