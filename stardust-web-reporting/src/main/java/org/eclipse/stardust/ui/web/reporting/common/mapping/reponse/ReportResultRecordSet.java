package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.List;

public class ReportResultRecordSet
{
   private List<ReportResultDataRow> rows;

   public ReportResultRecordSet(List<ReportResultDataRow> rows)
   {
      this.rows = rows;
   }

   public List<ReportResultDataRow> getRows()
   {
      return rows;
   }
}
