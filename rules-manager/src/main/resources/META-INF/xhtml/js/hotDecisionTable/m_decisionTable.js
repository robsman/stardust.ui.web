define(["jquery","bpm-modeler/js/m_utils"], 
        function ($,m_utils)
{
   var initialize = function (tableSelector,config)
   {
	   var instance=m_utils.jQuerySelect(tableSelector).handsontable('getInstance');
	   if(instance){
		   console.log("Previous handsontable instance detected, Destroying..." + tableSelector);
		   instance.destroy();
	   };
	   m_utils.jQuerySelect(tableSelector).handsontable(config);
   };
   
   return {
      initialize : initialize
   };
});