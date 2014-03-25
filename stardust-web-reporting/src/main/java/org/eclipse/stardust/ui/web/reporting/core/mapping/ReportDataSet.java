package org.eclipse.stardust.ui.web.reporting.core.mapping;

import java.util.List;

public class ReportDataSet
{
   private String type;
   private String primaryObject;
   private boolean joinExternalData;
   private List<ReportExternalJoin> externalJoins;
   private List<String> computedColumns;
   private List<String> columns;
   private ReportFilter[] filters;

   private String fact;
   private String firstDimension;



}
