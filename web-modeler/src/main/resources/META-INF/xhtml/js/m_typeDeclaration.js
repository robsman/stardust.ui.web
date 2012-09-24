/**
 * @author Marc.Gille
 */
define([ "m_utils", "m_modelElement" ], function(m_utils, m_modelElement) {
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
		m_utils.inheritMethods(TypeDeclaration.prototype, m_modelElement.create());

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
	}
});