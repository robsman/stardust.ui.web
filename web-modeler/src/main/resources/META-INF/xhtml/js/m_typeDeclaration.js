/**
 * @author Marc.Gille
 */
define([ "m_utils" ], function(m_utils) {
	var STRUCTURE_TYPE = "STRUCTURE_TYPE";
	var ENUMERATION_TYPE = "ENUMERATION_TYPE";

	// TODO Remove once testing can be done against imported XSDs
	
	var dataStructures = {
		"ord:Order" : {
			"name" : "Order",
			"children" : {
				"OrderId" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"OrderDate" : {
					"type" : "xsd:date",
					"cardinality" : "1"
				},
				"Customer" : {
					"type" : "per:Person",
					"cardinality" : "1"
				}
			}
		},
		"per:Person" : {
			"name" : "Person",
			"children" : {
				"FirstName" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"LastName" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"DateOfBirth" : {
					"type" : "xsd:date",
					"cardinality" : "1"
				},
				"Address" : {
					"type" : "adr:Address",
					"cardinality" : "N"
				}
			}
		},
		"adr:Address" : {
			"name" : "Address",
			"children" : {
				"Street" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"City" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"PostalCode" : {
					"type" : "xsd:string",
					"cardinality" : "1"
				},
				"Country" : {
					"type" : "cty:Country",
					"cardinality" : "1"
				}
			}
		}
	};

	return {
		getTypeDeclarations : getTypeDeclarations,
		getTestTypeDeclarations : getTypeDeclarations,
		createTypeDeclaration : function(name) {
			var typeDeclaration = new TypeDeclaration();

			typeDeclaration.initialize(name, STRUCTURE_TYPE);

			return typeDeclaration;
		},
		initializeTypeDeclaration : function(name) {
			var typeDeclaration = new TypeDeclaration();

			typeDeclaration.initialize(name, STRUCTURE_TYPE);

			return typeDeclaration;
		},
		initializeFromJson : function(json) {
			// TODO Ugly, use prototype
			m_utils.inheritMethods(json, new TypeDeclaration());

			json.initializeFromJson();

			return json;
		},
		STRUCTURE_TYPE : STRUCTURE_TYPE,
		ENUMERATION_TYPE : ENUMERATION_TYPE
	};

	/**
	 * Singleton on DOM level.
	 */
	function getTypeDeclarations() {
		if (window.parent.typeDeclarations == null) {
			window.parent.typeDeclarations = {};
		}

		return window.parent.typeDeclarations;
	}

	/**
	 * 
	 */
	function TypeDeclaration() {
		this.name = null;
		this.type = STRUCTURE_TYPE;

		this.children = {};

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
		TypeDeclaration.prototype.initializeFromJson = function() {
			// TODO Should be able to call getTypeDeclarations()

			// if (window.parent.typeDeclarations == null) {
			// window.parent.typeDeclarations = {};
			// }

			if (this.name != null) {
				getTypeDeclarations()[this.name] = this;
			}

			for ( var child in this.children) {
				// TODO Ugly, use prototype
				m_utils.inheritMethods(this.children[child],
						new SchemaElement());

				this.children[child].initializeFromJson(this);
			}
		};

		/**
		 * 
		 */
		TypeDeclaration.prototype.resolveTypes = function(typeMap) {
			for ( var child in this.children) {
				this.children[child].resolveType(typeMap);
			}
		};

		/**
		 * 
		 */
		TypeDeclaration.prototype.cloneChildren = function(typeMap, parent) {
			var cloneChildren = {};

			for ( var child in this.children) {
				var cloneChild = new SchemaElement();

				// TODO Not this instead of parent?

				cloneChild.initialize(parent, child,
						this.children[child].typeName,
						this.children[child].cardinality);

				cloneChild.resolveType(typeMap);

				cloneChildren[child] = cloneChild;
			}
			return cloneChildren;
		};

		/**
		 * 
		 */
		TypeDeclaration.prototype.getSchemaElementCount = function() {
			var n = 0;

			for ( var child in this.children) {
				++n;
			}

			return n;
		};

		/**
		 * 
		 */
		TypeDeclaration.prototype.addSchemaElement = function(name, typeName,
				cardinality) {
			var schemaElement = new SchemaElement();

			schemaElement.initialize(this, name, typeName, cardinality);
		};

		/**
		 * 
		 */
		TypeDeclaration.prototype.rename = function(name) {
			m_utils.debug("Renaming type declaration from name " + this.name
					+ " to " + name);

			delete getTypeDeclarations()[this.name];

			this.name = name;

			getTypeDeclarations()[this.name] = this;
		};
	}
	;

	/**
	 * 
	 */
	function SchemaElement() {
		this.parent = null;
		this.name = null;
		this.typeName = null;
		this.cardinality = null;
		this.children = {};

		/**
		 * 
		 */
		SchemaElement.prototype.toString = function() {
			return "Lightdust.SchemaElement";
		};

		/**
		 * 
		 */
		SchemaElement.prototype.initialize = function(parent, name, typeName,
				cardinality) {
			this.parent = parent;
			this.name = name;
			this.typeName = typeName;
			this.cardinality = cardinality;

			// TODO Hack

			if (parent.children == null) {
				parent.children = {};
			}
			
			parent.children[name] = this;
		};

		/**
		 * 
		 */
		SchemaElement.prototype.initializeFromJson = function(parent) {
			this.parent = parent;

			parent.children[this.name] = this;
		};

		/**
		 * 
		 */
		SchemaElement.prototype.resolveType = function(typeMap) {
			this.type = typeMap[this.typeName];

			if (this.type != null) {
				this.children = this.type.cloneChildren(typeMap, this);
			}
		};
		/**
		 * 
		 */
		SchemaElement.prototype.getFullPath = function() {
			var fullPath = "";

			if (this.parent != null) {
				fullPath += this.parent.getFullPath();
				fullPath += ".";
			}

			fullPath += this.name;

			return fullPath;
		};
	}
	;
});