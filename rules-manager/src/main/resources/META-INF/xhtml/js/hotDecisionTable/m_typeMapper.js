/*Implement all type mapping functions here (any function which
 * maps to or from an IPP type).
 * */
define(["jquery",
        "rules-manager/js/hotDecisionTable/m_utilities"],
        function($,m_utilities){

	var format="yy-mm-dd",
	    today = new Date();
	/*dependency on jqueryui datepicker for formatting.*/
	try{
		today=$.datepicker.formatDate(format, new Date());
		console.log("Calcualting today...");
		console.log(today);
	}catch (err){
		console.log("Error formatting default date. " + err);
	}
	
	return{
		ippToHoTTable: function(ippType){
		  var hotType={};
		  switch(ippType.toUpperCase()){
		  case "NMTOKENS":
		  case "IDREFS":
		  case "NOTATION":
		  case "TIME":
		  case "LANGUAGE":
		  case "ANYURI":
		  case "TOKEN":
		  case "NMTOKEN":
		  case "NAME":
		  case "QNAME":
		  case "ID":
		  case "IDREF":
		  case "DURATION":
		  case "NCNAME":
		  case "NORMALIZEDSTRING":
		  case "HEXBINARY":
		  case "BASE64BINARY":
		  case "GMONTHDAY":
		  case "GYEARMONTH":
		  case "STRING":
			  hotType={type:"text","default": ""};
			  break;
		  case "DOUBLE":
		  case "FLOAT":
		  case "DECIMAL":
			  hotType={type: "numeric", 
					   format:'0,0.00',
					   language:'en',
					   "default": 0};
			  break;
		  case "NEGATIVEINTEGER":
		  case "POSITIVEINTEGER":
		  case "NONNEGATIVEINTEGER":
		  case "NONPOSITIVEINTEGER":
		  case "UNSIGNEDBYTE" :
		  case "UNSIGNEDINT" :
		  case "UNSIGNEDLONG" :
		  case "UNSIGNEDSHORT":
		  case "GDAY":
		  case "GMONTH":
		  case "GYEAR":
		  case "BYTE":
		  case "SHORT":
		  case "INT":
		  case "LONG":
		  case "INTEGER":
			  hotType={type: "numeric","default":0};
			  break;
		  case "TIMESTAMP":
		  case "XSD:DATETIME":
			  hotType={
				  "type" : "dateTime", 
				  "default": m_utilities.formatDate(new Date(),"yyyy-MM-dd hh:mm:ss")
				  };
			  break;
		  case "XSD:DATE":
			  hotType={type:"date", "default": today,dateFormat: format};
			  break;
		  case "BOOLEAN":
			  hotType={type: "checkbox","default":true};
			  break;
		  case "ENUMERATION":
			  hotType={type:"autocomplete",source:[]};
			  break;
		  default:
			  hotType={type: "unknown","default":""};
		  }
		  if(hotType.type==="unknown"){
			  console.log("Error: Unknown type encountered : typeMapper.js->" + ippType);
		  }
		  return hotType;
		},
		ippToFriendlyText: function(ippType){
			  var friendType="";
			  if(!ippType){return;}
			  
			  switch(ippType.toUpperCase()){
			  case "XSD:NMTOKENS":
			  case "XSD:IDREFS":
			  case "XSD:NOTATION":
			  case "XSD:TIME":
			  case "XSD:LANGUAGE":
			  case "XSD:ANYURI":
			  case "XSD:TOKEN":
			  case "XSD:NMTOKEN":
			  case "XSD:NAME":
			  case "XSD:QNAME":
			  case "XSD:ID":
			  case "XSD:IDREF":
			  case "XSD:DURATION":
			  case "XSD:NCNAME":
			  case "XSD:NORMALIZEDSTRING":
			  case "XSD:HEXBINARY":
			  case "XSD:BASE64BINARY":
			  case "XSD:GMONTHDAY":
			  case "XSD:GYEARMONTH":
			  case "XSD:STRING":
			  case "STRING":
				  friendType="Text";
				  break;
			  case "XSD:DOUBLE":
			  case "DOUBLE":
			  case "XSD:FLOAT":
			  case "XSD:DECIMAL":
				  friendType="Decimal (1.234)";
				  break;
			  case "XSD:NEGATIVEINTEGER":
			  case "XSD:POSITIVEINTEGER":
			  case "XSD:NONNEGATIVEINTEGER":
			  case "XSD:NONPOSITIVEINTEGER":
			  case "XSD:UNSIGNEDBYTE" :
			  case "XSD:UNSIGNEDINT" :
			  case "XSD:UNSIGNEDLONG" :
			  case "XSD:UNSIGNEDSHORT":
			  case "XSD:GDAY":
			  case "XSD:GMONTH":
			  case "XSD:GYEAR":
			  case "XSD:INTEGER":
			  case "XSD:BYTE":
			  case "XSD:SHORT":
			  case "XSD:INT":
			  case "XSD:LONG":
			  case "INTEGER":
			  case "INT":
			  case "LONG":
				  friendType="Number (Integers, signed or unsigned, 32 or 64 bit)";
				  break;
			  case "XSD:DATETIME":
			  case "XSD:DATE":
			  case "TIMESTAMP":
				  friendType="Date (2099-12-31)";
				  break;
			  case "XSD:BOOLEAN":
			  case "BOOLEAN":
				  friendType="Boolean (True,False)";
				  break;
			  case "ENUMERATION":
				  friendType="Enumerations (Red,Green,Blue...)";
				  break;
			  default:
				  friendType=ippType;
			  }
			  if(friendType.type===ippType){
				  console.log("Error: Unknown type encountered : typeMapper.js->" + ippType);
			  }
			  return friendType;
			}
	};
});