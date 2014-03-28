package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.List;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportDataSet implements IValidateAble
{
   @NotNull
   private String type;

   @NotNull
   private String primaryObject;

   private boolean joinExternalData = false;

   private List<ReportExternalJoin> externalJoins;

   private List<ReportComputedColumn> computedColumns;

   private List<String> columns;

   private List<ReportFilter> filters;

   private String factDurationUnit;

   private Long firstDimensionCumulationIntervalCount;

   private String firstDimensionCumulationIntervalUnit;

   private String fact;

   private String firstDimension;

   private String firstDimensionFrom;

   private String firstDimensionTo;

   private String firstDimensionDuration;

   private Long firstDimensionDurationCount;

   private String firstDimensionDurationUnit;

   private String firstDimensionValue;

   private List<String> firstDimensionValueList;

   private String groupBy;

   public String getType()
   {
      return type;
   }

   public String getPrimaryObject()
   {
      return primaryObject;
   }

   public boolean isJoinExternalData()
   {
      return joinExternalData;
   }

   public List<ReportExternalJoin> getExternalJoins()
   {
      return externalJoins;
   }

   public List<ReportComputedColumn> getComputedColumns()
   {
      return computedColumns;
   }

   public List<String> getColumns()
   {
      return columns;
   }

   public List<ReportFilter> getFilters()
   {
      return filters;
   }

   public String getFactDurationUnit()
   {
      return factDurationUnit;
   }

   public Long getFirstDimensionCumulationIntervalCount()
   {
      return firstDimensionCumulationIntervalCount;
   }

   public String getFirstDimensionCumulationIntervalUnit()
   {
      return firstDimensionCumulationIntervalUnit;
   }

   public String getFact()
   {
      return fact;
   }

   public String getFirstDimension()
   {
      return firstDimension;
   }

   public String getFirstDimensionFrom()
   {
      return firstDimensionFrom;
   }

   public String getFirstDimensionTo()
   {
      return firstDimensionTo;
   }

   public String getFirstDimensionDuration()
   {
      return firstDimensionDuration;
   }

   public Long getFirstDimensionDurationCount()
   {
      return firstDimensionDurationCount;
   }

   public String getFirstDimensionDurationUnit()
   {
      return firstDimensionDurationUnit;
   }

   public String getGroupBy()
   {
      return groupBy;
   }

   public String getFirstDimensionValue()
   {
      return firstDimensionValue;
   }

   public List<String> getFirstDimensionValueList()
   {
      return firstDimensionValueList;
   }
}
