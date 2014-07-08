/*****************************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ****************************************************************************************/

define([ "bpm-reporting/public/js/report/I18NUtils", "bpm-reporting/js/utils" ], function(I18NUtils,
         utils)
{
   return {
      create : function()
      {
         var controller = new SchedulingController();

         return controller;
      }
   };

   /**
    * 
    */
   function SchedulingController()
   {
      this.DAILY = "daily";
      this.WEEKLY = "weekly";
      this.MONTHLY = "monthly";
      this.YEARLY = "yearly";

      /**
       * 
       */
      SchedulingController.prototype.createDefaultSettings = function()
      {
         return {
            recurrenceInterval : this.WEEKLY,
            executionTime : "18",
            delivery : {
               mode : "personalFolder"
            },
            recurrenceRange : {
               startDate : convertDate(new Date()),
               endMode : "noEnd",
               occurences : 10,
               endDate : convertDate(new Date())
            },
            dailyRecurrenceOptions : {
               daysRecurrence : "interval",
               daysIntervalCount : 1
            },
            weeklyRecurrenceOptions : {
               recurrenceWeekCount : 1,
               mondays : isCurrentWeekDay(1),
               tuesdays : isCurrentWeekDay(2),
               wednesdays : isCurrentWeekDay(3),
               thursdays : isCurrentWeekDay(4),
               fridays : isCurrentWeekDay(5),
               saturdays : isCurrentWeekDay(6),
               sundays : isCurrentWeekDay(0)
            },
            monthlyRecurrenceOptions : {
               monthsRecurrence : "day",
               dayNumber : utils.getDDFromDate(),
               month : 1,

               // monthsRecurrence : "weekday",
               day : new Date().getDay(),
               dayIndex : ((0 | new Date().getDate() / 7) + 1),
               monthIndex : 1
            },
            yearlyRecurrenceOptions : {
               yearlyRecurrence : "weekday",
               recurEveryYear : 1,

               onMonth : new Date().getMonth() + 1,
               onDay : utils.getDDFromDate(),

               onTheXDay : ((0 | new Date().getDate() / 7) + 1),
               onTheXDayName : new Date().getDay(),
               onTheMonth : new Date().getMonth() + 1
            }
         };
      };

      /**
       * 
       */
      SchedulingController.prototype.initialize = function(scheduling)
      {
         this.scheduling = scheduling;
      };
   }

   /**
    *  returns true if current weekday() and passed index of weekday are same else false
    */
   function isCurrentWeekDay(index)
   {
      return (utils.getWeekday() == index) ? true : false;
   }
   
   /**
    *  returns inputed date in yyyy-mm-dd format
    */
   function convertDate(input)
   {
      function pad(s) { return (s < 10) ? '0' + s : s; }
      var d = new Date(input);
      return [d.getFullYear(), pad(d.getMonth()+1), pad(d.getDate())].join('-');
   }
   
});