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
		  case "XSD:STRING":
		  case "STRING":
			  hotType={type:"text","default": ""};
			  break;
		  case "XSD:LONG":
		  case "XSD:FLOAT":
		  case "XSD:DECIMAL":
			  hotType={type: "numeric", 
					   format:'0,0.00',
					   language:'en',
					   "default": 0};
			  break;
		  case "XSD:SHORT":
		  case "XSD:INT":
		  case "XSD:DOUBLE":
		  case "DOUBLE":
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
		  if(hotType.type==="unkown"){
			  console.log("Error: Unkown type encountered : typeMapper.js->" + ippType);
		  }
		  return hotType;
		}
	};
});