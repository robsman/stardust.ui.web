/**
 * @author Marc.Gille
 */
define([ "m_utils", "m_constants" ], function(m_utils, m_constants) {

	return {
		createControlFlow : function(process) {
			var controlFlow = new ControlFlow("ControlFlow" + process.getControlFlowIndex());

			return controlFlow;
		},
		
		prototype: ControlFlow.prototype
	};

	/**
	 * 
	 */
	function ControlFlow(id) {
		this.type = m_constants.CONTROL_FLOW;
		this.id = id;
		this.attributes = {};
		this.conditionExpression = null;
		this.otherwise = false;

		/**
		 * 
		 */
		ControlFlow.prototype.toString = function() {
			return "Lightdust.ControlFlow";
		};

		/**
		 * 
		 */
		ControlFlow.prototype.toJsonString = function() {
			return JSON.stringify(this);
		};
	}
});