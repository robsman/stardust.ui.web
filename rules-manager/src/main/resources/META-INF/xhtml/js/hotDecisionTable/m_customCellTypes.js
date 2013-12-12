define([],function(){
	
	/*validates on the form of YYYY-MM-DD hh:mm:ss*/
	var dateTimeValidator=function(value,callback){
		var isValid=false,
			dateObj,
			dateParts,
			pattern=/\b\d{4}-[0-1]\d-[0-3]\d\s\d{2}:\d{2}:[0-5]\d\b/;
		
		/*Test 1: ensure we are of the correct pattern, this does not mean we 
		 *have a valid date as the pattern has limitations. For instance it
		 *can ensure that all months are two digits with the first digit being in the
		 *domain of [0,1] but it does not check the validity of the second digit, it just
		 *enforces that it is a number.*/
		isValid=pattern.test(value);
		
		/*Now test if we have a valid date!*/
		if(isValid){
			dateObj = new Date(value);
			isValid = dateObj.toString()!=="Invalid Date";
		}
	    callback(isValid);
	};
	
	/*CORE binding function for dateTime*/
	var bindDateTime=function(HOTCore){
		/*TODO:ZZM-Need a custom editor for this type.*/
		HOTCore.DateTimeCell = {
		    "editor": "text",
		    "renderer": HOTCore.TextRenderer,
		    "validator" : dateTimeValidator
		};
		HOTCore.cellTypes.dateTime = HOTCore.DateTimeCell;
	};
	
	return {
		
		"bind" : function(hotType,HOTCore){
			/*Detect that we have a valid Handsontable core*/
			if(!HOTCore || !HOTCore.TextRenderer){return;}
			
			switch(hotType){
				case "dateTime":  
					bindDateTime(HOTCore);
					break;
				default:
					console.log("Custom handsontable type not found: " + hotType);
			}
		}
	};
});