package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;

public class ReportSchedulingWeeklyRecurrenceOptions implements IValidateAble
{
   private long recurrenceWeekCount;
   private boolean mondays;
   private boolean tuesdays;
   private boolean wednesdays;
   private boolean thursdays;
   private boolean fridays;
   private boolean saturdays;
   private boolean sundays;
}
