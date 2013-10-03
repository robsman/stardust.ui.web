define(['rules-manager/js/RuleSetProvider'], function(RuleSetProvider) {
	return {
		ruleSetProvider : [ {
			id : "defaultRuleSetProvider",
			provider: RuleSetProvider
		}]
	};
});