/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "rules-manager/js/PropertyAction" ],
		function(m_utils, PropertyAction) {
			return {
				create : function(rule, fact, parameterDefinition) {
					var factAction = new FactAction();

					factAction.initialize(rule, fact, parameterDefinition);

					return factAction;
				},
				typeObject : function(json) {
					m_utils.typeObject(json, new FactAction());

					if (!json.propertyActions) {
						json.propertyActions = [];
					}

					for ( var n = 0; n < json.propertyActions.length; ++n) {
						PropertyAction.typeObject(json.propertyActions[n]);
					}

					return json;
				}
			};

			/**
			 * 
			 */
			function FactAction() {
				this.type = "factAction";
				this.propertyActions = [];

				/**
				 * 
				 */
				FactAction.prototype.initialize = function(rule, fact,
						parameterDefinition) {
					this.rule = rule;
					this.fact = fact;
					this.parameterDefinition = parameterDefinition;
				};

				/**
				 * 
				 */
				FactAction.prototype.addPropertyAction = function(property, element) {
					var propertyAction = PropertyAction.create(this, property, element);

					this.propertyActions.push(propertyAction);

					return propertyAction;
				};

				/**
				 * 
				 */
				FactAction.prototype.generateDrl = function() {
					var drl = "";

					drl += "\t\tmodify($" + this.fact + ") {\n";

					for ( var n = 0; n < this.propertyActions.length; ++n) {
						var propertyAction = this.propertyActions[n];

								drl += "\t\t" + propertyAction.property + " "
								+ propertyAction.operator + " "
								+ propertyAction.value + "\n";
					}

					drl += "\t}\n";

					return drl;
				};

				/**
				 * 
				 */
				FactAction.prototype.bindToRule = function(rule) {
					this.rule = rule;
					this.parameterDefinition = this.rule.ruleSet.getParameterDefinitionByName(this.fact);

					for ( var n = 0; n < this.propertyActions.length; ++n) {
						this.propertyActions[n].bindToFactAction(this);
					}
				};
			}
		});