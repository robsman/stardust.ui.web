/**
 * @author Shrikant.Gangal
 */

define(["m_constants"], function(m_constants) {
	var _toolClickActionsMap;
	return {
		init: function(divId, toolClickActionsMap){		
			addToolSpecificEventHandling(divId);
			_toolClickActionsMap = toolClickActionsMap;
		}		
	}
	
	function addToolSpecificEventHandling(divId) {
		jQuery('#' + divId).click(function(e) {
			//Invokes a function mapped to the tool id (e.target.id) in object _toolClickActionsMap.
			var methodName = e.target.id + "ToolSelected";
			if (typeof _toolClickActionsMap[methodName] == 'function')
			{
				_toolClickActionsMap[methodName]({toolId : e.target.id});
			}
		});
	};
});