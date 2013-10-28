/* uuidv4-UUID generator following version 4.4 of the spec.
 *  	http://www.ietf.org/rfc/rfc4122.txt @see section 4.4
		http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
 * hashString-Javascript implementation of Javas string.hashCode function
 */
define([],function(){
	
	
	/*given a dateString that the javascript dateObject can instantiate itself from,
	 *build an object representing the constituent date parts in various formats.
	 *This is a helper function for our formatDate function which uses the hashmap
	 *of this object to dynamically construct a string based on a format whose pattern
	 *will hash into the datePartObjects hashmap.*/
	var buildDatePartObject = function(dateString) {
		var oDate = new Date(dateString),
	    	month=	oDate.getMonth()+1,
	    	year=oDate.getFullYear(),
	    	dateOfMonth=oDate.getDate(),
	    	hour=oDate.getHours(),
	    	minutes=oDate.getMinutes(),
	    	meridian=(hour<12)?"AM":"PM",
	    	dpObj={};
		
		dpObj.yy=(year %1000)+"";
		dpObj.yyyy=year+"";
		dpObj.M=month+"";
		dpObj.MM=(month <10)?"0"+month:month+"";
		dpObj.d=dateOfMonth+"";
		dpObj.dd=(dateOfMonth <10)?"0"+dateOfMonth:dateOfMonth+"";
		dpObj.H=hour+"";
		dpObj.HH=(hour <12)?"0" + hour:hour+"";
		hour=hour %12;
		dpObj.h=hour+"";
		dpObj.hh=(hour===0)?12:hour;
		dpObj.hh=(dpObj.hh < 12)?"0" + dpObj.hh:dpObj.hh+"";
		dpObj.m=minutes+"";
		dpObj.mm=(minutes<10)?"0"+minutes:minutes+"";
		dpObj.meridian=meridian;
		return dpObj;
	};
	
	/* Given a datePartObject and a corresponding format we hash the format string against the datePartObject
	 * to build a string corresponding to the format.
	 * Caveats: A pipe in the format string will break everything as we inject pipes to find our delimiters.
	 * 		    Each piece in the formatString must be mappable into datePartObjects hashmap.
	 * 			Delimiters must be a single character, this includes whitespace.
	 **/
	var formatDate=function(dpo,format){
		  var pattern=/meridian|yyyy|yy|MM|M|HH|H|hh|h|dd|d|mm|m/g;
		  var matches=format.match(pattern);
		  var delimiters=format.replace(pattern,"|").match(/[^|]/g);
		  var matchCount=matches.length;
		  var sDate="";
		  var i=0;
		  
		  while(i<matches.length){
		    sDate+=dpo[matches[i]];
		    if(i < delimiters.length){
		      sDate+=delimiters[i];
		    }
		    i=i+1;
		  }
		  return sDate;
	};
	
	return {
		"uuidV4":function(){
			  var uuid='xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
			      return v.toString(16);
			  });
			  return uuid;
			},
		"hashString": function(str){
		    var hash = 0;
		    var i;
		    if (str.length == 0) return hash;
		    for (i = 0; i < str.length; i++) {
		        char = str.charCodeAt(i);
		        hash = ((hash<<5)-hash)+char;
		        hash = hash & hash; // Convert to 32bit integer
		    }
		    return hash;
		},
		"buildDatePartObject" : buildDatePartObject,
		"formatDate_base": formatDate,
		"formatDate": function(dateVal,format){
			var dateString=(dateVal instanceof Date)?dateVal.toString():dateVal;
			var dpo=buildDatePartObject(dateString);
			return formatDate(dpo,format);
		}
	};
});