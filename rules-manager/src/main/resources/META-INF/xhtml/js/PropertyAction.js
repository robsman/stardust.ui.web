/**
 * @author Marc.Gille
 */
define(
		[ "bpm-modeler/js/m_utils", "bpm-modeler/js/m_model" ],
		function(m_utils, m_model) {
			return {
				create : function(factAction, property, element) {
					var propertyAction = new PropertyAction();

					propertyAction.initialize(factAction, property, element);

					return propertyAction;
				},
				typeObject : function(json) {
					m_utils.typeObject(json, new PropertyAction());

					return json;
				}
			};

			/**
			 * 
			 */
			function PropertyAction() {
				this.type = "propertyAction";

				/**
				 * 
				 */
				PropertyAction.prototype.initialize = function(factAction,
						property, element) {
					this.factAction = factAction;
					this.property = property;
					this.element = element;
					this.operator = "=";
				};

				/**
				 * 
				 */
				PropertyAction.prototype.generateDrl = function() {
					var drl = "";

					return drl;
				};

				/**
				 * 
				 */
				PropertyAction.prototype.bindToFactAction = function(factAction) {
					this.factAction = factAction;

					m_utils.debug("Bind property action " + this.property);

					if (this.factAction.parameterDefinition.dataType == "struct") {
						var typeDeclaration = m_model.findTypeDeclaration(this.factAction.parameterDefinition.structuredDataTypeFullId);

						this.element = typeDeclaration.getElement(this.property);
					}
				};
			}
		});