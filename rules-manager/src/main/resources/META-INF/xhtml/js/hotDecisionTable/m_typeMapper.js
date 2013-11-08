/*Implement all type mapping functions here (any function which
 * maps to or from an IPP type).
 * */
define(["jquery"],function($){

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
			  hotType={type:"text","default": ""};
			  break;
		  case "XSD:DOUBLE":
		  case "DOUBLE":
		  case "XSD:FLOAT":
		  case "XSD:DECIMAL":
			  hotType={type: "numeric", 
					   format:'0,0.00',
					   language:'en',
					   "default": 0};
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
			  hotType={type: "numeric","default":0};
			  break;
		  case "XSD:DATETIME":
		  case "XSD:DATE":
		  case "TIMESTAMP":
			  hotType={type:"date", "default": today,dateFormat: format};
			  break;
		  case "XSD:BOOLEAN":
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
		}
	};
});