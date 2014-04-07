package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportResult
{
   @NotNull
   private String type;

   @NotNull
   private String primaryObject;

   private Map<String, ReportResultSeries> allSeries;

   @SuppressWarnings("unused")
   private ReportResultRecordSet recordSet;

   public void setRecordSet(ReportResultRecordSet recordSet)
   {
      this.recordSet = recordSet;
   }

   public void addSeries(String coodinate, ReportResultSeries series)
   {
      if(allSeries == null)
      {
         allSeries = new HashMap<String, ReportResultSeries>();
      }

      allSeries.put(coodinate, series);
   }
}
