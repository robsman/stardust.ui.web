/**
 * Module containing angular filters we wish to provide to an application.
 * FILTERS:
 * 1. test : "Returns static string, testing purposes only."
 * 2. humaneDate : port of https://github.com/zachleat/Humane-Dates/
 * 				   Provides human friendly date deltas (3 days ago etc)
 */
define([],function(){
	
	/*helper func for humaneDate*/
	var lang = {
			ago: 'Ago',
			from: '',
			now: 'Just Now',
			minute: 'Minute',
			minutes: 'Minutes',
			hour: 'Hour',
			hours: 'Hours',
			day: 'Day',
			days: 'Days',
			week: 'Week',
			weeks: 'Weeks',
			month: 'Month',
			months: 'Months',
			year: 'Year',
			years: 'Years'
		},
		formats = [
			[60, lang.now],
			[3600, lang.minute, lang.minutes, 60], // 60 minutes, 1 minute
			[86400, lang.hour, lang.hours, 3600], // 24 hours, 1 hour
			[604800, lang.day, lang.days, 86400], // 7 days, 1 day
			[2628000, lang.week, lang.weeks, 604800], // ~1 month, 1 week
			[31536000, lang.month, lang.months, 2628000], // 1 year, ~1 month
			[Infinity, lang.year, lang.years, 31536000] // Infinity, 1 year
		];
	
	/*helper func for humaneDate*/
	var  normalize =function(val, single)
	{
		var margin = 0.1;
		if(val >= single && val <= single * (1+margin)) {
			return single;
		}
		return val;
	};
	
	/*Main filter func for humaneDate*/
	var humaneDate = function(date, compareTo){

		if(!date) {
			return;
		}

		var isString = typeof date == 'string',
			date = isString ?
						new Date(('' + date).replace(/-/g,"/").replace(/T|(?:\.\d+)?Z/g," ")) :
						date,
			compareTo = compareTo || new Date,
			seconds = (compareTo - date +
							(compareTo.getTimezoneOffset() -
								// if we received a GMT time from a string, doesn't include time zone bias
								// if we got a date object, the time zone is built in, we need to remove it.
								(isString ? 0 : date.getTimezoneOffset())
							) * 60000
						) / 1000,
			token;

		if(seconds < 0) {
			seconds = Math.abs(seconds);
			token = lang.from ? ' ' + lang.from : '';
		} else {
			token = lang.ago ? ' ' + lang.ago : '';
		}

		/*
		 * 0 seconds && < 60 seconds        Now
		 * 60 seconds                       1 Minute
		 * > 60 seconds && < 60 minutes     X Minutes
		 * 60 minutes                       1 Hour
		 * > 60 minutes && < 24 hours       X Hours
		 * 24 hours                         1 Day
		 * > 24 hours && < 7 days           X Days
		 * 7 days                           1 Week
		 * > 7 days && < ~ 1 Month          X Weeks
		 * ~ 1 Month                        1 Month
		 * > ~ 1 Month && < 1 Year          X Months
		 * 1 Year                           1 Year
		 * > 1 Year                         X Years
		 *
		 * Single units are +10%. 1 Year shows first at 1 Year + 10%
		 */

		for(var i = 0, format = formats[0]; formats[i]; format = formats[++i]) {
			if(seconds < format[0]) {
				if(i === 0) {
					// Now
					return format[1];
				}

				var val = Math.ceil(normalize(seconds, format[3]) / (format[3]));
				return val +
						' ' +
						(val != 1 ? format[2] : format[1]) +
						(i > 0 ? token : '');
			}
		}
	};
	
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
	
	
	/*collection of filters we will return..*/
	var filters={
			"test"         : function(){return "hello from test filter!";},
			"humaneDate"   : function(){return humaneDate;},
			"serializeObject" : function(){return serializeObject;},
			"criticality"  : function(){return criticality;},
			"absoluteTime" : function(){return absoluteTime;},
			"priority"     : function(){return priority;}
				
	};
	
	return filters;

});