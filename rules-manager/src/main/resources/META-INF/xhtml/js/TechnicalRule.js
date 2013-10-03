define([ "bpm-modeler/js/m_utils", "rules-manager/js/FactCondition",
		"rules-manager/js/FactAction" ],
		function(m_utils, FactCondition, FactAction) {
			return {
				create : function(ruleSet, uuid, id, name) {
					var technicalRule = new TechnicalRule();
					technicalRule.initialize(ruleSet, uuid, id, name);
					return technicalRule;
				}
			};

			function TechnicalRule() {
				var drl="";
				this.type = "TechnicalRule";
				
				this.getDRL=function(){
					return drl;
				};
				
				this.setDRL=function(val){
					drl=val;
				};
				
				TechnicalRule.prototype.initialize = function(ruleSet, uuid, id, name) {
					var currentDateTime=(new Date()).toString();
					this.ruleSet = ruleSet;
					this.uuid = uuid;
					this.id = id;
					this.name = name;
					this.lastModificationDate=currentDateTime;
					this.creationDate=currentDateTime;
				};

				TechnicalRule.prototype.bindToRuleSet = function(ruleSet) {
					this.ruleSet = ruleSet;
				};
			}
		});