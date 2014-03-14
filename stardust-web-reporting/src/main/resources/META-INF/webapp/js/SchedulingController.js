/*****************************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC. All rights reserved.
 ****************************************************************************************/

define([ "bpm-reporting/js/I18NUtils", "bpm-reporting/js/utils" ], function(I18NUtils,
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
            executionTime : "08:00 AM",
            delivery : {
               mode : "personalFolder"
            },
            recurrenceRange : {
               startDate : new Date(),
               endMode : "noEnd",
               occurences : 10,
               endDate : utils.calculateDateAfterNDays(new Date(), 60)
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
               day : utils.getWeekdayName(),
               dayIndex : utils.findWeekOfMonth(new Date()),
               monthIndex : 1
            },
            yearlyRecurrenceOptions : {
               yearlyRecurrence : "weekday",
               recurEveryYear : 1,

               onMonth : utils.getMonthName(),
               onDay : utils.getDDFromDate(),

               onTheXDay : utils.findWeekOfMonth(new Date()),
               onTheXDayName : utils.getWeekdayName(),
               onTheMonth : utils.getMonthName()
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
});