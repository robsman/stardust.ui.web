/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils", "rules-manager/js/FactCondition",
		"rules-manager/js/FactAction" ],
		function(m_utils, FactCondition, FactAction) {
			return {
				create : function(ruleSet, uuid, id, name) {
					var rule = new Rule();

					rule.initialize(ruleSet, uuid, id, name);

					return rule;
				},
				typeObject : function(json) {
					m_utils.inheritMethods(json, new Rule());

					if (!json.conditions) {
						json.conditions = [];
					}

					for ( var n = 0; n < json.conditions.length; ++n) {
						FactCondition.typeObject(json.conditions[n]);
					}

					if (!json.actions) {
						json.actions = [];
					}

					for ( var n = 0; n < json.actions.length; ++n) {
						FactAction.typeObject(json.actions[n]);
					}

					return json;
				}
			};

			/**
			 * 
			 */
			function Rule() {
				this.type = "rule";
				this.conditions = [];
				this.actions = [];

				/**
				 * 
				 */
				Rule.prototype.initialize = function(ruleSet, uuid, id, name) {
					this.ruleSet = ruleSet;
					this.uuid = uuid;
					this.id = id;
					this.name = name;
				};

				/**
				 * 
				 */
				Rule.prototype.addFactCondition = function(fact,
						parameterDefinition) {
					var factCondition = FactCondition.create(this, fact,
							parameterDefinition);

					this.conditions.push(factCondition);

					return factCondition;
				};

				/**
				 * 
				 */
				Rule.prototype.addFactAction = function(fact,
						parameterDefinition) {
					var factAction = FactAction.create(this, fact,
							parameterDefinition);

					this.actions.push(factAction);

					return factAction;
				};

				/**
				 * 
				 */
				Rule.prototype.generateDrl = function() {
					var drl = "";

					drl += "rule \"" + this.name + "\"\n";
					drl += "when\n";

					for ( var i = 0; i < this.conditions.length; ++i) {
						var condition = this.conditions[i];

						drl += condition.generateDrl();
					}

					drl += "then\n";

					for ( var i = 0; i < this.actions.length; ++i) {
						var action = this.actions[i];

						drl += action.generateDrl();
					}

					drl += "end\n\n";

					return drl;
				};

				/**
				 * 
				 */
				Rule.prototype.bindToRuleSet = function(ruleSet) {
					this.ruleSet = ruleSet;

					for ( var i = 0; i < this.conditions.length; ++i) {
						this.conditions[i].bindToRule(this);
					}

					for ( var i = 0; i < this.actions.length; ++i) {
						this.actions[i].bindToRule(this);
					}
				};
			}
		});