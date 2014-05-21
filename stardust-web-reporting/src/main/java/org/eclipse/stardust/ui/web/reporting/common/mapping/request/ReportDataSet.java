package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.List;

import com.google.gson.JsonElement;

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

   private JsonElement columnsDurationUnit;

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

   public void setType(String type)
   {
      this.type = type;
   }

   public String getPrimaryObject()
   {
      return primaryObject;
   }

   public void setPrimaryObject(String primaryObject)
   {
      this.primaryObject = primaryObject;
   }

   public boolean isJoinExternalData()
   {
      return joinExternalData;
   }

   public void setJoinExternalData(boolean joinExternalData)
   {
      this.joinExternalData = joinExternalData;
   }

   public List<ReportExternalJoin> getExternalJoins()
   {
      return externalJoins;
   }

   public void setExternalJoins(List<ReportExternalJoin> externalJoins)
   {
      this.externalJoins = externalJoins;
   }

   public List<ReportComputedColumn> getComputedColumns()
   {
      return computedColumns;
   }

   public void setComputedColumns(List<ReportComputedColumn> computedColumns)
   {
      this.computedColumns = computedColumns;
   }

   public List<String> getColumns()
   {
      return columns;
   }

   public void setColumns(List<String> columns)
   {
      this.columns = columns;
   }

   public List<ReportFilter> getFilters()
   {
      return filters;
   }

   public void setFilters(List<ReportFilter> filters)
   {
      this.filters = filters;
   }

   public JsonElement getColumnsDurationUnit()
   {
      return columnsDurationUnit;
   }

   public String getFactDurationUnit()
   {
      return factDurationUnit;
   }

   public void setFactDurationUnit(String factDurationUnit)
   {
      this.factDurationUnit = factDurationUnit;
   }

   public Long getFirstDimensionCumulationIntervalCount()
   {
      return firstDimensionCumulationIntervalCount;
   }

   public void setFirstDimensionCumulationIntervalCount(
         Long firstDimensionCumulationIntervalCount)
   {
      this.firstDimensionCumulationIntervalCount = firstDimensionCumulationIntervalCount;
   }

   public String getFirstDimensionCumulationIntervalUnit()
   {
      return firstDimensionCumulationIntervalUnit;
   }

   public void setFirstDimensionCumulationIntervalUnit(
         String firstDimensionCumulationIntervalUnit)
   {
      this.firstDimensionCumulationIntervalUnit = firstDimensionCumulationIntervalUnit;
   }

   public String getFact()
   {
      return fact;
   }

   public void setFact(String fact)
   {
      this.fact = fact;
   }

   public String getFirstDimension()
   {
      return firstDimension;
   }

   public void setFirstDimension(String firstDimension)
   {
      this.firstDimension = firstDimension;
   }

   public String getFirstDimensionFrom()
   {
      return firstDimensionFrom;
   }

   public void setFirstDimensionFrom(String firstDimensionFrom)
   {
      this.firstDimensionFrom = firstDimensionFrom;
   }

   public String getFirstDimensionTo()
   {
      return firstDimensionTo;
   }

   public void setFirstDimensionTo(String firstDimensionTo)
   {
      this.firstDimensionTo = firstDimensionTo;
   }

   public String getFirstDimensionDuration()
   {
      return firstDimensionDuration;
   }

   public void setFirstDimensionDuration(String firstDimensionDuration)
   {
      this.firstDimensionDuration = firstDimensionDuration;
   }

   public Long getFirstDimensionDurationCount()
   {
      return firstDimensionDurationCount;
   }

   public void setFirstDimensionDurationCount(Long firstDimensionDurationCount)
   {
      this.firstDimensionDurationCount = firstDimensionDurationCount;
   }

   public String getFirstDimensionDurationUnit()
   {
      return firstDimensionDurationUnit;
   }

   public void setFirstDimensionDurationUnit(String firstDimensionDurationUnit)
   {
      this.firstDimensionDurationUnit = firstDimensionDurationUnit;
   }

   public String getFirstDimensionValue()
   {
      return firstDimensionValue;
   }

   public void setFirstDimensionValue(String firstDimensionValue)
   {
      this.firstDimensionValue = firstDimensionValue;
   }

   public List<String> getFirstDimensionValueList()
   {
      return firstDimensionValueList;
   }

   public void setFirstDimensionValueList(List<String> firstDimensionValueList)
   {
      this.firstDimensionValueList = firstDimensionValueList;
   }

   public String getGroupBy()
   {
      return groupBy;
   }

   public void setGroupBy(String groupBy)
   {
      this.groupBy = groupBy;
   }
}
