package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.ArrayList;
import java.util.List;

public class ReportResultDataRow
{
   private List<ReportResultDataRowColumn> columns;

   public ReportResultDataRow()
   {
      columns = new ArrayList<ReportResultDataRowColumn>();
   }

   public List<ReportResultDataRowColumn> getColumns()
   {
      return columns;
   }

   public void addColumn(ReportResultDataRowColumn column)
   {
      columns.add(column);
   }
}
