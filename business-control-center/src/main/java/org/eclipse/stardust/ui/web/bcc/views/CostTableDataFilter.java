/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.google.gson.JsonObject;

public class CostTableDataFilter extends TableDataFilterCustom
{
   private String columnTitle;
   private String columnId;
   private String startDateNumDays;
   private Integer startDateType;
   private String durationNumDays;
   private Integer durationType;
   private Date startDate;
   private Date endDate;
   private boolean showDatePicker = false;
   
   private List<SelectItem> daysCount;
   private List<SelectItem> durationCount;
   private List<SelectItem> durationItems;
   private Map<Integer, List<SelectItem>> dayTypeMapping;

   public CostTableDataFilter()
   {
      this("", "", "", true);
   }

   public CostTableDataFilter(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_COST_CUSTOM_COLUMN_FILTER);
   }

   public void initialize()
   {
      dayTypeMapping = CollectionUtils.newHashMap();
      daysCount = CustomColumnUtils.populateDateList(dayTypeMapping);
      durationCount = daysCount.subList(1, daysCount.size());
      durationItems = CustomColumnUtils.populateDurationList();

   }

   public void updateFilterFields(Object columnDefinition)
   {
      initialize();
      JsonObject jsonObject = (JsonObject) columnDefinition;
      this.columnId = GsonUtils.extractString(jsonObject, "columnId");
      this.columnTitle = GsonUtils.extractString(jsonObject, "columnTitle");
      this.startDateNumDays = GsonUtils.extractInt(jsonObject, "startNumOfDays").toString();
      this.startDateType = GsonUtils.extractInt(jsonObject, "startDateType");
      this.durationNumDays = GsonUtils.extractInt(jsonObject, "durationNumOfDays").toString();
      this.durationType = GsonUtils.extractInt(jsonObject, "durationDateType");
      if(jsonObject.has("showDatePicker"))
      {
         this.showDatePicker = GsonUtils.extractBoolean(jsonObject, "showDatePicker");
         this.startDate = DateUtils.parseDateTime(GsonUtils.extractString(jsonObject, "startDate"));
         this.endDate = DateUtils.parseDateTime(GsonUtils.extractString(jsonObject, "endDate"));
      }
   }

   public void toggleDatePicker()
   {
      if(showDatePicker)
      {
         showDatePicker = false;
      }
      else
      {
         showDatePicker = true;
      }
   }
   
   /**
    * 
    * @param event
    */
   public void updateAvailableDays(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         Integer dateType =  (Integer) event.getComponent().getAttributes().get("dateType");
         String columnName = (String) event.getComponent().getAttributes().get("columnName");
         if ("startDate".equals(columnName))
         {
            daysCount = dayTypeMapping.get(dateType);
         }
         else
         {
            durationCount = dayTypeMapping.get(dateType);
         }

      }
   }

   
   public boolean isFilterSet()
   {
      if (StringUtils.isEmpty(columnId) && StringUtils.isEmpty(columnId))
      {
         return false;
      }

      return true;
   }

   public void resetFilter()
   {
      CostsBean costBean = (CostsBean) FacesUtils.getBeanFromContext("costsBean");
      costBean.deleteFilter(this);
      columnId = null;
      columnTitle = null;
      startDate = null;
      endDate = null;
   }
   
   public String getFilterSummaryTitle()
   {
      return MessagesBCCBean.getInstance().get("views.customColumn.filter.Label");
   }

   public boolean contains(Object compareValue)
   {
      // TODO Auto-generated method stub
      return true;
   }

   public ITableDataFilter getClone()
   {
      CostTableDataFilter cFilter = this;

      return cFilter;
   }

   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      if (dataFilterToCopy instanceof CostTableDataFilter)
      {
         setColumnTitle(((CostTableDataFilter) dataFilterToCopy).getColumnTitle());
      }

   }

   public String getValidationMessage()
   {
      String validationMessage = "";
      Object startVal = getStartDate();
      Object endVal = getEndDate();
      if (StringUtils.isEmpty(columnTitle))
      {
         validationMessage = MessagesBCCBean.getInstance().getString("views.customColumn.columnName.error");
      }
      else if (showDatePicker)
      {
         if (startVal == null || endVal == null)
         {
            validationMessage = MessagesBCCBean.getInstance().getString("views.customColumn.dates.mandatory.error");
         }
         else
         {
            Date startDate = (Date) startVal;
            Date endDate = (Date) endVal;
            if (startDate.after(new Date()))
            {
               validationMessage = MessagesBCCBean.getInstance().getString("views.customColumn.startDate.error");
            }
            else if (startDate.after(endDate))
            {
               validationMessage = MessagesBCCBean.getInstance().getString("views.customColumn.dateBetween.error");
            }
         }
      }
      return validationMessage;
   }
   
   public String getColumnTitle()
   {
      return columnTitle;
   }

   public void setColumnTitle(String columnTitle)
   {
      this.columnTitle = columnTitle;
   }

   public String getColumnId()
   {
      return columnId;
   }

   public void setColumnId(String columnId)
   {
      this.columnId = columnId;
   }

   public String getStartDateNumDays()
   {
      return startDateNumDays;
   }

   public void setStartDateNumDays(String startDateNumDays)
   {
      this.startDateNumDays = startDateNumDays;
   }

   public Integer getStartDateType()
   {
      return startDateType;
   }

   public void setStartDateType(Integer startDateType)
   {
      this.startDateType = startDateType;
   }

   public Integer getDurationType()
   {
      return durationType;
   }

   public void setDurationType(Integer durationType)
   {
      this.durationType = durationType;
   }

   public String getDurationNumDays()
   {
      return durationNumDays;
   }

   public void setDurationNumDays(String durationNumDays)
   {
      this.durationNumDays = durationNumDays;
   }

   public List<SelectItem> getDaysCount()
   {
      return daysCount;
   }

   public void setDaysCount(List<SelectItem> daysCount)
   {
      this.daysCount = daysCount;
   }

   public List<SelectItem> getDurationCount()
   {
      return durationCount;
   }

   public void setDurationCount(List<SelectItem> durationCount)
   {
      this.durationCount = durationCount;
   }

   public List<SelectItem> getDurationItems()
   {
      return durationItems;
   }

   public void setDurationItems(List<SelectItem> durationItems)
   {
      this.durationItems = durationItems;
   }

   public Map<Integer, List<SelectItem>> getDayTypeMapping()
   {
      return dayTypeMapping;
   }

   public Date getStartDate()
   {
      return startDate;
   }

   public void setStartDate(Date startDate)
   {
      this.startDate = startDate;
   }

   public Date getEndDate()
   {
      return endDate;
   }

   public void setEndDate(Date endDate)
   {
      this.endDate = endDate;
   }

   public boolean isShowDatePicker()
   {
      return showDatePicker;
   }

   public void setShowDatePicker(boolean showDatePicker)
   {
      this.showDatePicker = showDatePicker;
   }

}
