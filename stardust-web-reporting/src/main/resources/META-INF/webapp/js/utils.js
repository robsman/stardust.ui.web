/**
 * 
 */

/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Aditya.Gaikwad
 */
define(
         [ "bpm-reporting/js/I18NUtils" ],
         function(I18NUtils)
         {
            return {

               /**
                * Function to get the current day index. Returned values (0 = Sunday, 1 =
                * Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 =
                * Saturday)
                */
               getWeekday : function()
               {
                  var d = new Date();
                  return d.getDay();
               },

               /**
                * Function to get the i18n name of weekday
                */
               getWeekdayName : function()
               {
                  var d = new Date();
                  var weekday = new Array(7);
                  weekday[0] = I18NUtils
                           .getProperty("reporting.definitionView.sunday.label");
                           weekday[1] = I18NUtils
                                    .getProperty("reporting.definitionView.monday.label"),
                           weekday[2] = I18NUtils
                                    .getProperty("reporting.definitionView.tuesday.label"),
                           weekday[3] = I18NUtils
                                    .getProperty("reporting.definitionView.wednesday.label"),
                           weekday[4] = I18NUtils
                                    .getProperty("reporting.definitionView.thursday.label"),
                           weekday[5] = I18NUtils
                                    .getProperty("reporting.definitionView.friday.label"),
                           weekday[6] = I18NUtils
                                    .getProperty("reporting.definitionView.saturday.label");

                  return weekday[d.getDay()];
               },

               /**
                * Function to get the i18n name of month
                */
               getMonthName : function()
               {
                  var d = new Date();
                  var month = new Array(12);
                           month[0] = I18NUtils
                                    .getProperty("reporting.definitionView.january.label"),
                           month[1] = I18NUtils
                                    .getProperty("reporting.definitionView.february.label"),
                           month[2] = I18NUtils
                                    .getProperty("reporting.definitionView.march.label"),
                           month[3] = I18NUtils
                                    .getProperty("reporting.definitionView.april.label"),
                           month[4] = I18NUtils
                                    .getProperty("reporting.definitionView.may.label"),
                           month[5] = I18NUtils
                                    .getProperty("reporting.definitionView.june.label"),
                           month[6] = I18NUtils
                                    .getProperty("reporting.definitionView.july.label"),
                           month[7] = I18NUtils
                                    .getProperty("reporting.definitionView.august.label"),
                           month[8] = I18NUtils
                                    .getProperty("reporting.definitionView.september.label"),
                           month[9] = I18NUtils
                                    .getProperty("reporting.definitionView.october.label"),
                           month[10] = I18NUtils
                                    .getProperty("reporting.definitionView.november.label"),
                           month[11] = I18NUtils
                                    .getProperty("reporting.definitionView.december.label");

                  return month[d.getMonth()];
               },

               /**
                * Function to get the i18n name of XX week of the month
                */
               getNWeekOfMonth : function()
               {
                  var d = new Date();
                  var week = new Array(5);
                           week[0] = I18NUtils
                                    .getProperty("reporting.definitionView.first.label"),
                           week[1] = I18NUtils
                                    .getProperty("reporting.definitionView.second.label"),
                           week[2] = I18NUtils
                                    .getProperty("reporting.definitionView.third.label"),
                           week[3] = I18NUtils
                                    .getProperty("reporting.definitionView.fourth.label"),
                           week[4] = I18NUtils
                                    .getProperty("reporting.definitionView.last.label");

                  //FIXME: Logic to get week of the month
                  return week[1];
               },

               /**
                * Function to get the dd part of dd/mm/yyyy
                */
               getDDFromDate : function()
               {
                  var d = new Date();
                  return d.getDate();
               },

               /**
                * Function to calculate next date after specified number of days
                */
               calculateDateAfterNDays : function(startDate, noOfDays)
               {
                  if (!(isNaN(noOfDays)))
                  {
                     startDate.setDate(startDate.getDate() + noOfDays);
                  }
                  return startDate;
               },

               /**
                * Function to calculate next valid Execution Date.
                */
               calculateValidExecutionTime : function(startDate, interval)
               {
                  if (isNaN(interval) || interval <= 0)
                  {
                     return startDate;
                  }
                  var todayDate = new Date();
                  var executionDate = this.calculateDateAfterNDays(startDate, interval);
                  if (executionDate >= todayDate)
                  {
                     return executionDate;
                  }
                  else
                  {
                     return this.calculateValidExecutionTime(executionDate, interval);
                  }
               },

               /**
                * Function to calculate x week of month of a given date.
                */
               findWeekOfMonth : function(date)
               {
                  var week = new Array(5);
                  week[0] = I18NUtils
                           .getProperty("reporting.definitionView.first.label"),
                  week[1] = I18NUtils
                           .getProperty("reporting.definitionView.second.label"),
                  week[2] = I18NUtils
                           .getProperty("reporting.definitionView.third.label"),
                  week[3] = I18NUtils
                           .getProperty("reporting.definitionView.fourth.label"),
                  week[4] = I18NUtils
                           .getProperty("reporting.definitionView.last.label");

                  return week[0 | date.getDate() / 7];
               }

            };
         });