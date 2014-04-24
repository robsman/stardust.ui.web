/**
 * @author Marc.Gille
 */
define([ "bpm-modeler/js/m_utils",
		"rules-manager/js/m_ruleSet" ], function(m_utils, RuleSet) {
	return {
		create : function() {
			return new RuleSetProvider();
		}	
	};

	/**
	 * 
	 */
	function RuleSetProvider() {
		/**
		 * 
		 */
		RuleSetProvider.prototype.getRuleSets = function() {
			return RuleSet.getRuleSets();
		};
	}
});