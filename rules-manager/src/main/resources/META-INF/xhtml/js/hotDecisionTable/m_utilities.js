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
	    	oDay=oDate.getDay(),
	    	month=	oDate.getMonth()+1,
	    	year=oDate.getFullYear(),
	    	dateOfMonth=oDate.getDate(),
	    	hour=oDate.getHours(),
	    	minutes=oDate.getMinutes(),
	    	seconds=oDate.getSeconds(),
	    	milliseconds=oDate.getMilliseconds(),
	    	tzo=oDate.getTimezoneOffset(),
	    	meridian=(hour<12)?"AM":"PM",
	    			dpObj={};
		
		var months=["January","February","March","April","May","June","July","August","September","October","November","December"];
		var days=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"];
		
		dpObj.yy=(year %1000)+"";
		dpObj.yyyy=year+"";
		dpObj.M=month+"";
		dpObj.MM=(month <10)?"0"+month:month+"";
		dpObj.MMM=months[month-1].slice(0,3);
		dpObj.MMMM=months[month-1];
		dpObj.d=dateOfMonth+"";
		dpObj.dd=(dateOfMonth <10)?"0"+dateOfMonth:dateOfMonth+"";
		dpObj.ddd=days[oDay].slice(0,3),
		dpObj.dddd=days[oDay],
		dpObj.H=hour+"";
		dpObj.HH=(hour <10)?"0" + hour:hour+"";
		dpObj.h=hour%12+"";
		dpObj.hh=(hour%12===0)?12:hour;
		dpObj.hh=(dpObj.hh < 10)?"0" + dpObj.hh:dpObj.hh+"";
		dpObj.m=minutes+"";
		dpObj.mm=(minutes<10)?"0"+minutes:minutes+"";
		dpObj.s=seconds +"";
		dpObj.ss=(seconds < 10)?"0"+seconds:seconds+"";
		dpObj.sss=milliseconds;
		dpObj.K=tzo/60;
		dpObj.t=meridian.slice(0,1);
		dpObj.tt=meridian;
		return dpObj;
	};
	
	/* Given a datePartObject and a corresponding format we hash the format string against the datePartObject
	 * to build a string corresponding to the format.
	 * Caveats: A pipe in the format string will break everything as we inject pipes to find our delimiters.
	 * 			Literal text following the last matched pattern will not be included in the format.
	 * 			Escape sequences are not supported (but would be a worthwhile effort).
	 **/
	var formatDate=function(dpo,format){
	    var pattern=/yyyy|yy|MMMM|MMM|MM|M|HH|H|hh|h|dddd|ddd|dd|d|mm|m|sss|ss|s|tt|t|K/g;
	    var matches=format.match(pattern);
	    var delimiters=format.replace(pattern,"|").split(/\|/g);
	    var sDate="";
	    var i=0;
	
	    while(i<matches.length){
	      sDate +=delimiters[i] + dpo[matches[i++]];
	    }
	    return sDate;
	};
	
	return {
		"generateID": function(baseName,coExistantObjs,prop,self){
			var key,				/*key in a for-in construct*/
				temp,				/*temp obj we pull from our coexisters*/
				tempHash={},		/*Hash map we will check against*/
				tempSuffix,			/*Sufffix extracted from our baseName*/
				patt=/_[0-9]+\b/;	/*suffix matcher*/
			
			/*Remove spaces and special characters*/
			baseName=baseName.replace(/[^a-zA-Z0-9_.]+/g,"");
			
			/*Build a hash of our existing IDs*/
			for(key in coExistantObjs){
				if(coExistantObjs.hasOwnProperty(key)){
					temp=coExistantObjs[key];
					/*Avoid checking self against self*/
					if(temp && temp!=self){
						tempHash[temp[prop || "id"]]={};
					}
				}
			}
			/*Now check our hash for our baseName, adding an incremental suffix
			 *to our baseName until no hash is found*/
			while(tempHash.hasOwnProperty(baseName)){
				if(patt.test(baseName)){
					tempSuffix=patt.exec(baseName)[0];
					baseName=baseName.replace(tempSuffix,"");
					tempSuffix =(1*tempSuffix.replace("_",""))+1;
				}
				else{
					tempSuffix=1;
				}
				baseName =baseName + "_" + tempSuffix;
			}
			return baseName;
		},
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