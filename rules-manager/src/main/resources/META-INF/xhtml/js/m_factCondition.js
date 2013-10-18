/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "rules-manager/js/PropertyCondition" ],
		function(m_utils, PropertyCondition) {
			return {
				create : function(rule, fact, parameterDefinition) {
					var factCondition = new FactCondition();

					factCondition.initialize(rule, fact, parameterDefinition);

					return factCondition;
				},
				typeObject : function(json) {
					m_utils.typeObject(json, new FactCondition());

					if (!json.propertyConditions) {
						json.propertyConditions = [];
					}

					for ( var n = 0; n < json.propertyConditions.length; ++n) {
						PropertyCondition
								.typeObject(json.propertyConditions[n]);
					}

					return json;
				}
			};

			/**
			 * 
			 */
			function FactCondition() {
				this.type = "factCondition";
				this.propertyConditions = [];

				/**
				 * 
				 */
				FactCondition.prototype.initialize = function(rule, fact,
						parameterDefinition) {
					this.rule = rule;
					this.fact = fact;
					this.parameterDefinition = parameterDefinition;
				};

				/**
				 * 
				 */
				FactCondition.prototype.addPropertyCondition = function(
						property, element) {
					var propertyCondition = PropertyCondition.create(this,
							property, element);

					this.propertyConditions.push(propertyCondition);

					return propertyCondition;
				};

				/**
				 * 
				 */
				FactCondition.prototype.generateDrl = function() {
					var drl = "";

					drl += "\t$" + this.fact + ": " + this.fact + "(\n";

					for ( var n = 0; n < this.propertyConditions.length; ++n) {
						var propertyCondition = this.propertyConditions[n];

						drl += "\t\t" +
								propertyCondition.property + " " + propertyCondition.operator
								+ " " + propertyCondition.value + "\n";
					}

					drl += "\t)\n";

					return drl;
				};
				
				/**
				 * 
				 */
				FactCondition.prototype.bindToRule = function(rule) {
					this.rule = rule;
					this.parameterDefinition = this.rule.ruleSet.getParameterDefinitionByName(this.fact);

					for ( var n = 0; n < this.propertyConditions.length; ++n) {
						this.propertyConditions[n].bindToFactCondition(this);
					}
				};
			}
		});