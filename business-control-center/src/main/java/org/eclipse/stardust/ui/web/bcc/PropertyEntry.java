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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.PropertyProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



public class PropertyEntry
{
   private static final String EMPTY = "";
   
   private String key;

   private String value;

   private String plannedStartTime;

   private String plannedTerminationTime;

   private String estimatedDurationSeconds;

   private String thresholdPercentage;

   private String successor;

   private String predecessor;

   private String descriptorKey;

   private String descriptorValues;

   private ProcessDefinition processDefinition;

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   public void setProcessDefinition(ProcessDefinition processDefinition)
   {
      this.processDefinition = processDefinition;
   }

   private List<SelectItem> descriptors = new ArrayList<SelectItem>();

   public static PropertyEntry EMPTY_PROPERTY_ENTRY = new PropertyEntry();

   private String descriptorVal;

   public PropertyEntry(ProcessDefinition processDefinition)
   {
      this(processDefinition, null);
   }

   public PropertyEntry(ProcessDefinition processDefinition, String descriptorVal)
   {
      this.processDefinition = processDefinition;
      this.descriptorVal = descriptorVal;
      initProperties();
     // if (descriptorVal == null)
      {
         initDescriptors();
      }
   }

   public String getId()
   {
      return descriptorVal == null
            ? processDefinition == null ? null : processDefinition.getQualifiedId()
            : processDefinition.getQualifiedId() + "." + descriptorVal;
   }

   private void initProperties()
   {
      PropertyProvider props = PropertyProvider.getInstance();
      plannedStartTime = props.getProperty(getId(), PropertyProvider.PLANNED_START_TIME_PROPERTY);
      plannedTerminationTime = props.getProperty(getId(), PropertyProvider.PLANNED_TERMINATION_TIME_PROPERTY);
      estimatedDurationSeconds = props.getProperty(getId(), PropertyProvider.ESTIMATED_DURATION_PROPERTY);
      thresholdPercentage = props.getProperty(getId(), PropertyProvider.THRESHOLD_PROPERTY);
      successor = props.getProperty(getId(), PropertyProvider.SUCCESSOR_PROPERTY);
      predecessor = props.getProperty(getId(), PropertyProvider.PREDECESSOR_PROPERTY);
      descriptorKey = props.getProperty(getId(), PropertyProvider.INSTANCE_DESCRIPTOR_KEY);
      descriptorValues = props.getProperty(getId(), PropertyProvider.INSTANCE_DESCRIPTOR_VALUES);
   }

   public void initDescriptors()
   {
      for (Iterator<DataPath> iterator = processDefinition.getAllDataPaths().iterator(); iterator.hasNext();)
      {
         DataPath dataPath = (DataPath) iterator.next();
         if (dataPath.isDescriptor())
         {
            descriptors.add(new SelectItem(dataPath.getId(), I18nUtils.getDataPathName(dataPath)));
         }
      }
   }

   private PropertyEntry()
   {}

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public String getKey()
   {
      return key;
   }

   public String getPlannedStartTime()
   {
      return plannedStartTime;
   }

   public void setPlannedStartTime(String plannedStartTime)
   {
      if (processDefinition != null)
      {
         this.plannedStartTime = plannedStartTime;
      }
   }

   public String getPlannedTerminationTime()
   {
      return plannedTerminationTime;
   }

   public void setPlannedTerminationTime(String plannedTerminationTime)
   {
      if (processDefinition != null)
      {
         this.plannedTerminationTime = plannedTerminationTime;
      }
   }

   public String getEstimatedDurationSeconds()
   {
      return estimatedDurationSeconds;
   }

   public void setEstimatedDurationSeconds(String estimatedDurationSeconds)
   {
      if (processDefinition != null)
      {
         this.estimatedDurationSeconds = estimatedDurationSeconds;
      }
   }

   public String getThresholdPercentage()
   {
      return thresholdPercentage;
   }

   public void setThresholdPercentage(String thresholdPercentage)
   {
      if (processDefinition != null)
      {
         this.thresholdPercentage = thresholdPercentage;
      }
   }

   public String getSuccessor()
   {
      return successor;
   }

   public void setSuccessor(String successor)
   {
      if (processDefinition != null)
      {
         this.successor = successor;
      }
   }

   public String getPredecessor()
   {
      return predecessor;
   }

   public void setPredecessor(String predecessor)
   {
      if (processDefinition != null)
      {
         this.predecessor = predecessor;
      }
   }

   public String getDescriptorKey()
   {
      return descriptorKey;
   }

   public void setDescriptorKey(String descriptorKey)
   {
      if (processDefinition != null)
      {
         this.descriptorKey = descriptorKey;
         if (StringUtils.isEmpty(descriptorKey))
         {
            descriptorValues = null;
         }
      }
   }

   public String getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(String descriptorValues)
   {
      if (processDefinition != null && StringUtils.isNotEmpty(descriptorKey))
      {
         this.descriptorValues = descriptorValues;
      }
   }

   public SelectItem[] getDescriptors()
   {
      SelectItem[] descrList = new SelectItem[descriptors.size()];
      for (int i = 0; i < descriptors.size(); i++)
      {
         descrList[i] = (SelectItem) descriptors.get(i);
      }
      return descrList;
   }

   public boolean isHasDescriptorValues()
   {
      return !StringUtils.isEmpty(descriptorValues);
   }

   public List<SelectItem> getDescriptorValueList()
   {
      List<SelectItem> descriptorValueList = new ArrayList<SelectItem>();
      if (StringUtils.isNotEmpty(descriptorValues) && StringUtils.isNotEmpty(descriptorKey))
      {
         StringTokenizer token = new StringTokenizer(descriptorValues, ",");
         while (token.hasMoreTokens())
         {
            String descriptorValue = token.nextToken().trim();
            descriptorValueList.add(new SelectItem(descriptorValue, descriptorValue));
         }
      }
      return descriptorValueList;
   }

   public void save()
   {
      PropertyProvider provider = PropertyProvider.getInstance();
      provider.setProperty(getId(), PropertyProvider.PLANNED_START_TIME_PROPERTY, plannedStartTime);
      provider.setProperty(getId(), PropertyProvider.PLANNED_TERMINATION_TIME_PROPERTY, plannedTerminationTime);
      provider.setProperty(getId(), PropertyProvider.ESTIMATED_DURATION_PROPERTY, estimatedDurationSeconds);
      provider.setProperty(getId(), PropertyProvider.THRESHOLD_PROPERTY, thresholdPercentage);
      provider.setProperty(getId(), PropertyProvider.SUCCESSOR_PROPERTY, successor);
      provider.setProperty(getId(), PropertyProvider.PREDECESSOR_PROPERTY, predecessor);
      provider.setProperty(getId(), PropertyProvider.INSTANCE_DESCRIPTOR_KEY, descriptorKey);
      provider.setProperty(getId(), PropertyProvider.INSTANCE_DESCRIPTOR_VALUES, descriptorValues);
   }

   /**
    * method to reset instance value
    */
   public void reset()
   {
      plannedStartTime = EMPTY;
      plannedTerminationTime = EMPTY;
      estimatedDurationSeconds = EMPTY;
      thresholdPercentage = EMPTY;
      successor = EMPTY;
      predecessor = EMPTY;
      descriptorKey = EMPTY;
      descriptorValues = EMPTY;
      descriptors = CollectionUtils.newArrayList();
   }

}
