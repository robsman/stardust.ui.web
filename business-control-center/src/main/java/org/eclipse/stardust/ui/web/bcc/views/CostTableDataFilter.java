/*
 * $Id$
 * (C) 2000 - 2014 CARNOT AG
 */
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.google.gson.JsonObject;

public class CostTableDataFilter extends TableDataFilterCustom
{
   private final static int DAY_TYPE = 1;
   private final static int WEEK_TYPE = 2;
   private final static int MONTH_TYPE = 3;
   private final static int YEAR_TYPE = 4;

   private String columnTitle;
   private String columnId;
   private String startDateNumDays;
   private Integer startDateType;
   private String durationNumDays;
   private Integer durationType;

   private List<SelectItem> daysCount;
   private List<SelectItem> durationCount;
   private List<SelectItem> durationItems;
   private ITableDataFilterListener listener;
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
      daysCount = populateDateList();
      durationCount = daysCount.subList(1, daysCount.size());
      durationItems = populateDurationList();

   }

   /**
    * 
    * @param startableProcesses
    * @return
    */
   private List<SelectItem> populateDateList()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      dayTypeMapping = CollectionUtils.newHashMap();
      for (Integer i = 0; i <= 31; i++)
      {
         items.add(new SelectItem(i.toString(), i.toString()));
      }
      dayTypeMapping.put(DAY_TYPE, items);
      dayTypeMapping.put(WEEK_TYPE, items.subList(0, 6));
      dayTypeMapping.put(MONTH_TYPE, items.subList(0, 13));
      dayTypeMapping.put(YEAR_TYPE, items);
      return items;
   }

   /**
    * 
    * @return
    */
   private List<SelectItem> populateDurationList()
   {
      List<SelectItem> items = new ArrayList<SelectItem>();
      items.add(new SelectItem(DAY_TYPE, "Days"));
      items.add(new SelectItem(WEEK_TYPE, "Weeks"));
      items.add(new SelectItem(MONTH_TYPE, "Months"));
      items.add(new SelectItem(YEAR_TYPE, "Year"));
      return items;
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
   }
   
   public String getFilterSummaryTitle()
   {
      return MessagesBCCBean.getInstance().get("views.costs.column.customColumn.filter.Label");
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

   public ITableDataFilterListener getListener()
   {
      return listener;
   }

   public void setListener(ITableDataFilterListener listener)
   {
      this.listener = listener;
   }
   
   

}
