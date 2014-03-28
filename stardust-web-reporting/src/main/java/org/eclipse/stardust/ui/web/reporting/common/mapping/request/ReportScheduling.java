package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;

public class ReportScheduling implements IValidateAble
{
   private String recurrenceInterval;
   private String executionTime;
   private ReportSchedulingDelivery delivery;
   private ReportSchedulingRecurrenceRange recurrenceRange;
   private ReportSchedulingDailyRecurrenceOptions dailyRecurrenceOptions;
   private ReportSchedulingWeeklyRecurrenceOptions weeklyRecurrenceOptions;
   private ReportSchedulingMonthlyRecurrenceOptions monthlyRecurrenceOptions;
   private ReportSchedulingYearlyRecurrenceOptions yearlyRecurrenceOptions;
}
