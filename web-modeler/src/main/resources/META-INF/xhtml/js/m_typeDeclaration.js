/**
 * @author Marc.Gille
 */
define(
		[ "m_utils", "m_modelElement" ],
		function(m_utils, m_modelElement) {
			var STRUCTURE_TYPE = "STRUCTURE_TYPE";
			var ENUMERATION_TYPE = "ENUMERATION_TYPE";

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
				STRUCTURE_TYPE : STRUCTURE_TYPE,
				ENUMERATION_TYPE : ENUMERATION_TYPE
			};

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

				/**
				 * 
				 */
				TypeDeclaration.prototype.isSequence = function() {
					return this.getBody() != null;
				};

				/**
				 * 
				 */
				TypeDeclaration.prototype.getBody = function() {
					return this.typeDeclaration.schema.types[this.id].body;
				};

				/**
				 * 
				 */
				TypeDeclaration.prototype.getFacets = function() {
					return this.typeDeclaration.schema.types[this.id].facets;
				};

				/**
				 * 
				 */
				TypeDeclaration.prototype.getSchemaName = function() {
					return this.typeDeclaration.schema.elements[this.id].type;
				};

				/**
				 * 
				 */
				TypeDeclaration.prototype.getElementCount = function() {
					var n = 0;

					if (this.isSequence()) {
						for ( var element in this.getBody().elements) {
							++n;
						}
					} else {
						for ( var element in this.getFacets()) {
							++n;
						}
					}

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
								m_utils.debug("===> Type Declaration");
								m_utils.debug(childTypeDeclaration);
								m_utils.debug("===> Facets");
								m_utils.debug(childTypeDeclaration.getFacets());

								for ( var enumerator in childTypeDeclaration.getFacets()) {
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
			}
		});