package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;

import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.common.util.DateUtils;

public class ResourceLoginInfoDTO
{
 
   public String name;

   public String userId;

   public long userOid;

   public String day;

   public String week;

   public String month;
   
   public ResourceLoginInfoDTO(String name, String userId, long userOid, Date timeLoggedInToday, Date timeLoggedInThisWeek, Date timeLoggedInThisMonth)
   {
      super();
      this.name = name;
      this.userId = userId;
      this.userOid = userOid;
      this.day = formatDate(timeLoggedInToday);
      this.week = formatDate(timeLoggedInThisWeek);
      this.month = formatDate(timeLoggedInThisMonth);
   }

   private String formatDate(Date date)
   {
      double timeInMs = date != null ? date.getTime() : 0;
      int minutesPerDay = BusinessControlCenterConstants.getWorkingMinutesPerDay();
      return timeInMs == 0 ? "-" : DateUtils
            .formatDurationInHumanReadableFormat((long) timeInMs, minutesPerDay * 60);
   }
}
