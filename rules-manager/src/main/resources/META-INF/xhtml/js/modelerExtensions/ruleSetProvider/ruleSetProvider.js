define(['rules-manager/js/m_ruleSetProvider'], function(RuleSetProvider) {
	return {
		ruleSetProvider : [ {
			id : "defaultRuleSetProvider",
			provider: RuleSetProvider
		}]
	};
});