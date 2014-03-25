package org.eclipse.stardust.ui.web.reporting.core.mapping;

import java.util.List;

import com.google.gson.JsonElement;

public class ReportDataSet
{
   private String type;
   private String primaryObject;
   private boolean joinExternalData;
   private List<ReportExternalJoin> externalJoins;
   private List<String> computedColumns;
   private List<String> columns;
   private List<ReportFilter> filters;

   private String factDurationUnit;
   private long firstDimensionCumulationIntervalCount;
   private String firstDimensionCumulationIntervalUnit;

   private String fact;
   private String firstDimension;
}
