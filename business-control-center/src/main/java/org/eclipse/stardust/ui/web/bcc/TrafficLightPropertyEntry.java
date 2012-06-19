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
package org.eclipse.stardust.ui.web.bcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficLightViewPropertyProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



public class TrafficLightPropertyEntry
{
   private ProcessDefinition processDefinition;

   private Map/* <String, ProcessingThreshold> */processingThresholds = new HashMap();

   private Map/* <String,ProcsessingThreshold> */rowData = new HashMap();

   private String stateCalculator;

   private String descriptorFilter;

   private boolean displayTotalRow;

   public static TrafficLightPropertyEntry EMPTY_PROPERTY_ENTRY = new TrafficLightPropertyEntry();

   public TrafficLightPropertyEntry(ProcessDefinition processDefinition)
   {
      this.processDefinition = processDefinition;
      initProcessData();
      initProcessingThreshold();
      initProperties();
   }

   private TrafficLightPropertyEntry()
   {}

   private void initProperties()
   {
      TrafficLightViewPropertyProvider provider = TrafficLightViewPropertyProvider
            .getInstance();
      stateCalculator = provider.getStateCalculatorClassName(processDefinition.getQualifiedId());
      descriptorFilter = provider.getDescriptorFilterName(processDefinition.getQualifiedId());
      displayTotalRow = provider.withTotalRow(processDefinition.getQualifiedId());
   }

   private void initProcessingThreshold()
   {
      processingThresholds = new HashMap();
      TrafficLightViewPropertyProvider provider = TrafficLightViewPropertyProvider
            .getInstance();
      Map thresholds = provider.getAllProcessingThresholds();
      List activityIds = provider.getAllColumnIDs(processDefinition.getQualifiedId());
      int order = 1;
      for (Iterator iterator = activityIds.iterator(); iterator.hasNext();)
      {
         String activityId = (String) iterator.next();
         Activity activity=processDefinition.getActivity(activityId);
        
         processingThresholds.put(activityId, new ProcessingThreshold(activityId, I18nUtils.getActivityName(activity),
               (String) thresholds.get(processDefinition.getQualifiedId() + "." + activityId),
               new Integer(order)));
         order++;
      }
      for (Iterator iterator = processDefinition.getAllActivities().iterator(); iterator
            .hasNext();)
      {
         Activity activity = (Activity) iterator.next();
         if (!processingThresholds.containsKey(activity.getId()))
         {
            processingThresholds.put(activity.getId(), new ProcessingThreshold(activity
                  .getId(),I18nUtils.getActivityName(activity), (String) thresholds.get(processDefinition.getId() + "."
                  + activity.getId()), new Integer(order)));
            order++;
         }
      }
   }

   public List getOrderNumbers()
   {
      List/* <SelectedItems> */orderNumbers = new ArrayList();
      List activities = processDefinition.getAllActivities();
      for (int i = 1; i <= activities.size(); i++)
      {
         orderNumbers.add(new SelectItem(new Integer(i), String.valueOf(i)));
      }
      return orderNumbers;
   }

   private void initProcessData()
   {
      rowData = new HashMap();
      TrafficLightViewPropertyProvider provider = TrafficLightViewPropertyProvider
            .getInstance();
      for (Iterator iterator = processDefinition.getAllDataPaths().iterator(); iterator
            .hasNext();)
      {
         DataPath dataPath = (DataPath) iterator.next();
         if (dataPath.isDescriptor())
         {
            String rowDataValues = provider.getAllRowDataValues(
                  processDefinition.getQualifiedId(), dataPath.getId());
            String name= I18nUtils.getDataPathName(dataPath);
            rowData.put(dataPath.getId(), new RowData(dataPath.getId(),name, rowDataValues));
         }
      }
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   public void setProcessDefinition(ProcessDefinition processDefinition)
   {
      this.processDefinition = processDefinition;
   }

   public String getStateCalculator()
   {
      return stateCalculator;
   }

   public void setStateCalculator(String stateCalculator)
   {
      this.stateCalculator = stateCalculator;
   }

   public String getDescriptorFilter()
   {
      return descriptorFilter;
   }

   public void setDescriptorFilter(String descriptorFilter)
   {
      this.descriptorFilter = descriptorFilter;
   }

   public boolean isDisplayTotalRow()
   {
      return displayTotalRow;
   }

   public void setDisplayTotalRow(boolean displayTotalRow)
   {
      this.displayTotalRow = displayTotalRow;
   }

   public boolean isHasProcessingThresholds()
   {
      return !processingThresholds.isEmpty();
   }

   public List<ProcessingThreshold> getProcessingThresholds()
   {
      List<ProcessingThreshold> thresholds = new ArrayList<ProcessingThreshold>(processingThresholds.values());
      Collections.sort(thresholds);
      return thresholds;
   }

   public void setProcessingThresholds(Map processingThresholds)
   {
      this.processingThresholds = processingThresholds;
   }

   public boolean isHasRowData()
   {
      return !rowData.isEmpty();
   }

   public List<RowData> getRowData()
   {
      return new ArrayList(rowData.values());
   }

   public void setRowData(Map rowData)
   {
      this.rowData = rowData;
   }

   public class ProcessingThreshold implements Comparable
   {
      private final String id;
      private final String name;
      private String value;

      private Integer order;

      public ProcessingThreshold(String id,String name, String value, Integer order)
      {
         this.id = id;
         this.name = name;
         this.value = value;
         this.order = order;
      }

      public String getId()
      {
         return id;
      }

      public String getName()
      {
         return name;
      }

      public String getValue()
      {
         return value;
      }

      public void setValue(String value)
      {
         this.value = value;
      }

      public Integer getOrder()
      {
         return order;
      }

      public void setOrder(Integer order)
      {
         this.order = order;
      }

      public int compareTo(Object o)
      {
         int result = 0;
         if (o instanceof ProcessingThreshold)
         {
            result = this.order.compareTo(((ProcessingThreshold) o).getOrder());
         }
         return result;
      }
      
      public void valueChanged(ValueChangeEvent vce)
      {
         if(!((String)vce.getNewValue()).equals((String)vce.getOldValue()))
         {
            this.value = (String) vce.getNewValue();
         }
      }
      
   }

   public class RowData implements Comparable
   {
      private final String id;
      private final String name;
      private String values;

      public RowData(String id,String name, String values)
      {
         this.id = id;
         this.name=name;
         this.values = values;
      }

      public String getId()
      {
         return id;
      }
      

      public String getName()
      {
         return name;
      }

      public String getValues()
      {
         return values;
      }

      public void setValues(String rowDataValues)
      {
         this.values = rowDataValues;
      }

      public int compareTo(Object o)
      {
         if(o instanceof RowData)
         {
            return this.id.compareTo(((RowData)o).getId());
         }
         else
         {
            return 0;
         }
      }
      public void valueChanged(ValueChangeEvent vce)
      {
         if(!((String)vce.getNewValue()).equals((String)vce.getOldValue()))
         {
            this.values = (String) vce.getNewValue();
         }
      }
   }

   public void save()
   {
      TrafficLightViewPropertyProvider provider = TrafficLightViewPropertyProvider
            .getInstance();
      provider.setStateCalculator(processDefinition.getQualifiedId(), stateCalculator);
      provider.setDescriptorFilterName(processDefinition.getQualifiedId(), descriptorFilter);
      provider.setDisplayTotalRow(processDefinition.getQualifiedId(), displayTotalRow);

      StringBuffer selectedActivityIds = new StringBuffer();

      List processingThresholdList = new ArrayList(processingThresholds.values());
      Collections.sort(processingThresholdList);
      for (Iterator iterator = processingThresholdList.iterator(); iterator.hasNext();)
      {
         ProcessingThreshold threshold = (ProcessingThreshold) iterator.next();
         if (!StringUtils.isEmpty(threshold.getValue()))
         {
            selectedActivityIds.append(threshold.getId()
                  + TrafficLightViewPropertyProvider.PROPERTY_VALUE_SEPARATOR);
            provider.setProcessingThreshold(processDefinition.getQualifiedId(), threshold.getId(),
                  threshold.getValue());
         }
      }
      provider.setAllColumnIds(processDefinition.getQualifiedId(), selectedActivityIds.toString());

      StringBuffer rowDataIds = new StringBuffer();
      for (Iterator iterator = rowData.values().iterator(); iterator.hasNext();)
      {
         RowData rowData = (RowData) iterator.next();
         if (!StringUtils.isEmpty(rowData.getValues()))
         {
            rowDataIds.append(rowData.getId()
                  + TrafficLightViewPropertyProvider.PROPERTY_VALUE_SEPARATOR);
            provider.setRowDataValues(processDefinition.getQualifiedId(), rowData.getId(), rowData
                  .getValues());
         }
      }
      provider.setRowDataIds(processDefinition.getQualifiedId(), rowDataIds.toString());

      provider.save();
   }
   
   public void reset()
   {
      initProcessData();
      initProcessingThreshold();
      initProperties();
   }
}
