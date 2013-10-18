/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_model"], function(m_utils, m_model) {
	return {
		EQUAL: "=",
		LESS_THAN: "<",
		LESS_THAN_EQUAL: "<",
		GREATER_THAN: ">",
		GREATER_THAN_EQUAL: ">=",
		NOT_EQUAL: "!=",
		CONTAINS: "contains",
		DOES_NOT_CONTAIN: "doesnotcontain",
		create : function(factCondition, property, element) {
			var propertyCondition = new PropertyCondition();

			propertyCondition.initialize(factCondition, property, element);
			
			return propertyCondition;
		},
		typeObject : function(json) {
			m_utils.debug("Property Condition");
			m_utils.debug(json);

			m_utils.typeObject(json, new PropertyCondition());

			return json;
		}
	};

	/**
	 * 
	 */
	function PropertyCondition() {
		this.type = "propertyCondition";

		/**
		 * 
		 */
		PropertyCondition.prototype.initialize = function(factCondition, property, element) {
			this.factCondition = factCondition;
			this.property = property;
			this.element = element;
			this.operator = "=";
		};
		
		/**
		 * 
		 */
		PropertyCondition.prototype.generateDrl = function() {
			var drl = "";
			
			return drl;
		};

		/**
		 * 
		 */
		PropertyCondition.prototype.bindToFactCondition = function(factCondition) {
			this.factCondition = factCondition;

			m_utils.debug("Bind property action " + this.property);

			if (this.factCondition.parameterDefinition.dataType == "struct") {
				var typeDeclaration = m_model.findTypeDeclaration(this.factCondition.parameterDefinition.structuredDataTypeFullId);

				this.element = typeDeclaration.getElement(this.property);
			}
		};
	}
});