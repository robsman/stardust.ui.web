define([],function(){
	var catBool=1,
	    catString=2,
	    catNumeric=4,
	    catDate=8;	
	
	/* hashcodes are generated using utilities.hashString and should correspond 
	 * to Javas' haschcode function. Hashcode is based on the symbol value, so if you change the symbol 
	 * rerun the hashcode. Do not run it against the literal symbol (as if you copy and pasted from the source in this file),
	 * run it off the HTML encoding of the symbol as the symbol would appear on a web-page. This is to allow lookups
	 * based on the html value of the symbol.
	 * */
	var operators=[
		{
			operator:"EqualTo",
			hashCode: 61,
			DRLoperator:"==",
			text: "Equal To",
			symbol:"&#x3d;",
			validBitMask: catBool |
						  catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"NotEqualTo",
			hashCode: 8800,
			DRLoperator:"!=",
			text: "Not Equal To",
			symbol:"&#x2260;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"GreaterThan",
			hashCode: 62,
			DRLoperator:">",
			text: "Greater Than",
			symbol:"&#x3e;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"LessThan",
			hashCode: 60,
			DRLoperator:"<",
			text: "Less Than",
			symbol:"&#x3c;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"LessThanOrEqualTo",
			hashCode: 8804,
			DRLoperator:"<=",
			text: "Less Than or Equal To",
			symbol:"&#x2264;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"GreaterThanOrEqualTo",
			hashCode: 8805,
			DRLoperator:">=",
			text: "Greater ThanOr Equal To",
			symbol:"&#x2265;",
			validBitMask: catString  |
						 catNumeric | 
						 catDate
		},
		{
			operator:"Matches",
			hashCode:8773,
			DRLoperator:"matches",
			text: "Matches",
			symbol:"&#x2245;",
			validBitMask: catString
		},
		{
			operator:"SoundsLike",
			hashCode: 9835,
			DRLoperator:"soundslike",
			text: "Sounds Like",
			symbol:"&#9835;",
			validBitMask: catString
		},
		{
			operator:"Null",
			hashCode: 63,
			DRLoperator:"== null",
			text: "Null",
			symbol:"?",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"NotNull",
			hashCode: 1086,
			DRLoperator:"!= null",
			text: "Not Null",
			symbol:"!?",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"InList",
			hashCode: 8712,
			DRLoperator:"in",
			text: "In List",
			symbol:"&#8712;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"NotInList",
			hashCode: 8713,
			DRLoperator:"not in",
			text: "Not In List",
			symbol:"&#8713;",
			validBitMask: catString  |
						  catNumeric | 
						  catDate
		},
		{
			operator:"After",
			hashCode: 303490,
			DRLoperator:"after",
			text: "After",
			symbol:"&#x263c;&#x3e;",
			validBitMask: catDate
		},
		{
			operator:"Before",
			hashCode: 11648,
			DRLoperator:"before",
			text: "Before",
			symbol:"&#x3c;&#x263c;",
			validBitMask: catDate
		},
		{
			operator:"Coincides",
			hashCode: 9788,
			DRLoperator:"?coincides",
			text: "Coincides",
			symbol:"&#x263c;",
			validBitMask: catDate
		}
];
		
  var opFac={
		MenuCategory : {
				CAT_BOOLEAN: catBool,
				CAT_STRING:  catString,
				CAT_NUMERIC: catNumeric,
				CAT_DATE:    catDate
		},
		getOperators: function(category){
		  var opCount=operators.length,
		      matchedOps=[],
		      tempOp;
		  while(opCount--){
		    tempOp=operators[opCount];
		    if(tempOp.validBitMask & category){
		      matchedOps.push(tempOp);
		    }
		  }
		  return matchedOps;
		},
		getOperatorByHashCode: function(val){
		  var opCount=operators.length,
              tempOp;
		  while(opCount--){
		    tempOp=operators[opCount];
		    if(tempOp.hashCode === val){
		      return tempOp;
		    }
		  }
		}
	};
  return opFac;
});