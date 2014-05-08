/**
 * Module containing angular filters we wish to provide to an application.
 * FILTERS:
 * 1. test : "Returns static string, testing purposes only."
 * 2. humaneDate : port of https://github.com/zachleat/Humane-Dates/
 * 				   Provides human friendly date deltas (3 days ago etc)
 */
define([],function(){

	var serializeObject =function(obj,delimiter,addLabel,labelDelimiter){
			var k,result="";
			delimiter=(!delimiter)?", ":delimiter;
			labelDelimiter=(!labelDelimiter)?" : ":labelDelimiter;
			for (k in obj) {
			    if (obj.hasOwnProperty(k)) {
			    	if(addLabel){
			    		result += k + labelDelimiter;
			    	}
			        result += obj[k] + delimiter;
			    }   
			}
			return result.substring(0,result.lastIndexOf(delimiter));
	};
	
	/* Expects input between 0.0 and 1, 
	 * results High/Med/Low based on thirds*/
	var criticality = function(val){
		
		var result;
		
		val = (val*1000).toFixed(0);
		result = val;
		if(val < 333.33){
			result =  "Low (" + val + ")";
		}
		else if(val < 666.33){
			result = "Medium (" + val + ")";
		}
		else{
			result = "High (" + val + ")";
		}
		return result;
	};
	
	var priority = function(val){
		var result;
		
		val = val *1;/*ensure cast to a number*/
		switch(val){
			case -1:
				result = "Low";
				break;
			case 0:
				result = "Normal";
				break;
			case 1:
				result = "High";
				break;
			default:
				result = "NA";
		}
		return result;
	};
	
	/*Given a number of ticks/milliseconds, this will return a string
	 * representing the absolute amount of days / hours / minutes those 
	 * ticks represent. 
	 */
	var absoluteTime = function(val){
		var d = 86400000, /*number of milliseconds in a day*/
			h= d/24,	  /*number milliseconds in one hour*/
			m = h/60,	  /*number of milliseconds in a minute*/
			sec = m/60,	  /*number of milliseconds in a second*/
			result,
			days,hours,minutes;


		days=val/d;
		hours=days%1*24;
		minutes = hours%1*60;
		return Math.floor(days) + "d" + "  " + Math.floor(hours) + "h" + " " + Math.floor(minutes) + "m";
	};
	
	/**
	 * friendlyDate
	 * ---------------------------------------------
	 * Given a date in the form of ticks since 1970 (Date.getTime()),
	 * calculate the difference in time from the current time, and 
	 * return the value in a friendly, human readable format.
	 */
	var friendlyDate = function(pastTicks){
		  var baseTicks=(new Date()).getTime(),
		      deltaTicks = baseTicks-pastTicks,
		      d = 86400000, /*milliseconds in one day*/
			  delta={"d":0,"h":0,"m":0},
			  timePart,
			  timeParts={ "y" : "y",
				          "M" : "M",
				          "w" : "w",
				          "d" : "d",
				          "h" : "h",
				          "m" : "m"},    
			  result;
			
			/*calculate at high accuracy*/		
			delta.d=deltaTicks/d;
			delta.h=delta.d%1*24; /*Mod 1 as we only want the fractional part of the day*/
			delta.m=delta.h%1*60; /*Mod 1 as we only want the fractional part of the hour*/
			
			/*Now floor to round and drop decimals*/
			delta.d=Math.floor(delta.d);
			delta.h=Math.floor(delta.h);
			delta.m=Math.floor(delta.m);
		 
		  if(delta.d > 365){
		        result = Math.floor(delta.d/365);
		        timePart = timeParts.y;
		  }
		  else if(delta.d >= 30){
		      result=Math.ceil(delta.d/30);
		      timePart=timeParts.M;
		  }
		  else if(delta.d >=7){
		      result = Math.floor(delta.d/7);
		      timePart = timeParts.w;
		  }
		  else if(delta.d >= 1){
		      result = delta.d;
		      timePart = timeParts.d;
		  }
		  else if(delta.h > 0){
		      result = delta.h;
		      timePart = timeParts.h;
		  }else if(delta.m > 0){
		      result = delta.m;
		      timePart=timeParts.m;
		  }else{
		      result ="";
		      timePart = "Now";
		  }
		    
		  //if(result > 1){timePart += "s";}
		  
		  return (result + timePart).trim();
	};
	
	/*collection of filters we will return..*/
	var filters={
			"test"         : function(){return "hello from test filter!";},
			"serializeObject" : function(){return serializeObject;},
			"criticality"  : function(){return criticality;},
			"absoluteTime" : function(){return absoluteTime;},
			"priority"     : function(){return priority;},
			"friendlyDate" : function(){return friendlyDate;}
				
	};
	
	return filters;

});