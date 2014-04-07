package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.List;

public class ReportResultSeries
{
   private String label;
   private List<Object> data;

   public ReportResultSeries(String label, List<Object> data)
   {
      this.label = label;
      this.data = data;
   }

   public String getLabel()
   {
      return label;
   }

   public List<Object> getData()
   {
      return data;
   }
}
