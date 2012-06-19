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
package org.eclipse.stardust.ui.client.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.AttributeOrder;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.OrderCriterion;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.ui.client.util.DateRange;




public class ProcessFilter
{
   private Client client;
   private String id;

   private Set<ProcessDefinition> processDefinitions = new HashSet<ProcessDefinition>();
   private ProcessDefinition process;
   
   private Date lastDeploymentTime = null;
   private boolean labelSetFromActiveModel = false;
   
   public ProcessFilter(Client client, String id)
   {
      this.client = client;
      this.id = id;
   }

   public void add(ProcessDefinition process)
   {
      if (process == null)
      {
         throw new NullPointerException();
      }
      if (id == null)
      {
         throw new IllegalStateException("This filter do not accept processes.");
      }
      if (!id.equals(process.getId()))
      {
         throw new IllegalArgumentException("All processes in this filter set must have the same id.");
      }
      if (!processDefinitions.contains(process))
      {
         if (!labelSetFromActiveModel)
         {
            // (fh) assumes that the models are already updated, since you have hold on a process definition.
            // TODO: (fh) get rid of that casting
            DeployedModel model = (DeployedModel) client.getModels().getModel(process.getModelOID());
            if (model.isActive())
            {
               labelSetFromActiveModel = true;
               this.process = process;
            }
            else
            {
               // set the action name if no action name set or if the process belongs to a model deployed later
               // than the one from which the current label was retrieved 
               Date deploymentTime = model.getDeploymentTime();
               if (lastDeploymentTime == null || lastDeploymentTime.before(deploymentTime))
               {
                  lastDeploymentTime = deploymentTime;
                  this.process = process;
               }
            }
         }
         processDefinitions.add(process);
      }
   }

   public String getId()
   {
      return id;
   }
   
   public String getLabel()
   {
      // TODO i18n
      return process == null ? null : process.getName();
   }

   public String[] getDescriptors()
   {
      if (processDefinitions != null && !processDefinitions.isEmpty())
      {
         Set<String> properties = new HashSet<String>();
         for (ProcessDefinition process : processDefinitions)
         {
            @SuppressWarnings("unchecked")
            List<DataPath> paths = process.getAllDataPaths();
            for (DataPath dataPath : paths)
            {
               if (dataPath.isDescriptor())
               {
                  properties.add(dataPath.getId());
               }
            }
         }
         return properties.toArray(new String[properties.size()]);
      }
      return null;
   }
   
   public OrderCriterion createDescriptorOrderCriterion(String descriptor, boolean ascending)
   {
      if (descriptor != null && processDefinitions != null && !processDefinitions.isEmpty())
      {
         for (ProcessDefinition process : processDefinitions)
         {
            // (fh) assumes that the models are already updated, since you have hold on a process definition.
            Model model = client.getModels().getModel(process.getModelOID());
            @SuppressWarnings("unchecked")
            List<DataPath> paths = process.getAllDataPaths();
            for (DataPath dataPath : paths)
            {
               if (dataPath.isDescriptor() && descriptor.equals(dataPath.getId()))
               {
                  DataDetails data = (DataDetails) model.getData(dataPath.getData());
                  String dataId = data.getId();
                  if (StructuredDataConstants.STRUCTURED_DATA.equals(data.getTypeId()) && !StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     return new DataOrder(dataId, dataPath.getAccessPath(), ascending);
                  }
                  else if (StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     return PredefinedConstants.PROCESS_PRIORITY.equals(dataId)
                        ? new AttributeOrder(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY, ascending)
                        : new DataOrder(dataId, ascending);
                  }
               }
            }
         }
      }
      return null;
   }
   
   public boolean isSortableDescriptor(String descriptor)
   {
      if (processDefinitions != null && !processDefinitions.isEmpty())
      {
         for (ProcessDefinition process : processDefinitions)
         {
            Model model = client.getModels().getModel(process.getModelOID());
            @SuppressWarnings("unchecked")
            List<DataPath> paths = process.getAllDataPaths();
            for (DataPath dataPath : paths)
            {
               if (dataPath.isDescriptor() && descriptor.equals(dataPath.getId()))
               {
                  DataDetails data = (DataDetails) model.getData(dataPath.getData());
                  if (StructuredDataConstants.STRUCTURED_DATA.equals(data.getTypeId())
                        && !StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     return true;
                  }
                  else if (StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     return PredefinedConstants.PRIMITIVE_DATA.equals(data.getTypeId());
                  }
               }
            }
         }
      }
      return false;
   }

   public String getDescriptorLabel(String descriptor)
   {
      String name = getDataPathName(descriptor, process);
      if (name == null)
      {
         for (ProcessDefinition process : processDefinitions)
         {
            name = getDataPathName(descriptor, process);
            if (name != null)
            {
               break;
            }
         }
      }
      return name;
   }

   private String getDataPathName(String id, ProcessDefinition process)
   {
      String name = null;
      if (process != null)
      {
         @SuppressWarnings("unchecked")
         List<DataPath> paths = process.getAllDataPaths();
         for (DataPath dataPath : paths)
         {
            if (dataPath.isDescriptor() && id.equals(dataPath.getId()))
            {
               // TODO: i18n
               name = dataPath.getName();
               break;
            }
         }
      }
      return name;
   }

   public FilterCriterion createDescriptorFilterCriterion(DescriptorFilter propertyFilter)
   {
      if (propertyFilter != null && processDefinitions != null && !processDefinitions.isEmpty())
      {
         Object rawFilterValue = propertyFilter.getFilterValue();
         String filterString = rawFilterValue instanceof String
               ? (String) rawFilterValue
               : null;
         boolean wildcardSearch = filterString != null
               ? filterString.indexOf('*') >= 0
               : false;
         Serializable filterValue = null;
         
         for (ProcessDefinition process : processDefinitions)
         {
            @SuppressWarnings("unchecked")
            List<DataPath> paths = process.getAllDataPaths();
            for (DataPath dataPath : paths)
            {
               if (dataPath.isDescriptor() && propertyFilter.getProperty().equals(dataPath.getId()))
               {
                  boolean isDate = false;
                  if (wildcardSearch)
                  {
                     filterString = filterString.replace('*', '%');
                  }
                  else
                  {
                     Class<?> mappedType = dataPath.getMappedType();
                     if (String.class.equals(mappedType))
                     {
                        if (StringUtils.isEmpty((String) filterString))
                        {
                           filterString = null;
                        }
                     }
                     else if (Boolean.class.equals(mappedType))
                     {
                        if ("true".equals(filterString)) //$NON-NLS-1$
                        {
                           filterValue = Boolean.TRUE;
                        }
                        else if ("false".equals(filterString)) //$NON-NLS-1$
                        {
                           filterValue = Boolean.FALSE;
                        }
                     }
                     else if (Byte.class.equals(mappedType))
                     {
                        filterValue = new Byte(filterString);
                     }
                     else if (Short.class.equals(mappedType))
                     {
                        filterValue = new Short(filterString);
                     }
                     else if (Integer.class.equals(mappedType))
                     {
                        filterValue = new Integer(filterString);
                     }
                     else if (Long.class.equals(mappedType))
                     {
                        filterValue = new Long(filterString);
                     }
                     else if (Float.class.equals(mappedType))
                     {
                        filterValue = new Float(filterString);
                     }
                     else if (Double.class.equals(mappedType))
                     {
                        filterValue = new Double(filterString);
                     }
                     else if (Date.class.equals(mappedType) || Calendar.class.equals(mappedType))
                     {
                        isDate  = Date.class.equals(mappedType);
                        if (rawFilterValue instanceof DateRange)
                        {
                           filterValue = (DateRange) rawFilterValue;
                        }
                        else if (rawFilterValue instanceof String)
                        {
                            Calendar cal = (Calendar) filterValue;
                            cal.setTimeInMillis(Long.parseLong(filterString));
                            Calendar start = Calendar.getInstance();
                            start.clear();
                            start.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                            Calendar end = Calendar.getInstance();
                            end.clear();
                            end.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                            end.add(Calendar.DATE, 1);
                            end.add(Calendar.MILLISECOND, -1);
                            filterValue = new DateRange();
                            ((DateRange) filterValue).setFromDate(start);
                            ((DateRange) filterValue).setToDate(end);
                        }
                     }
                  }
                  String data = dataPath.getData();
                  if (isDataType(process, data, StructuredDataConstants.STRUCTURED_DATA) && !StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     if (wildcardSearch)
                     {
                        return DataFilter.like(data, dataPath.getAccessPath(), filterString, propertyFilter.isCaseSensitive());
                     }
                     else
                     {
                        if (filterValue == null)
                        {
                           return DataFilter.isEqual(data, dataPath.getAccessPath(), filterString, propertyFilter.isCaseSensitive());
                        }
                        else
                        {
                           if (filterValue instanceof DateRange)
                           {
                              DataFilter dataFilter = null;
                              DateRange dateRange = (DateRange) filterValue;
                              Calendar fromDateValue = dateRange.getFromDate();
                              Calendar toDateValue = dateRange.getToDate();
                              if (toDateValue != null && fromDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.between(data, dataPath
                                          .getAccessPath(), fromDateValue.getTime(), toDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.between(data, dataPath
                                       .getAccessPath(), fromDateValue, toDateValue);
                                 }
                              }
                              else if (fromDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.greaterOrEqual(data, dataPath
                                          .getAccessPath(), fromDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.greaterOrEqual(data, dataPath
                                       .getAccessPath(), fromDateValue);
                                 }
                              }
                              else if (toDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.lessOrEqual(data, dataPath
                                          .getAccessPath(), toDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.lessOrEqual(data, dataPath
                                       .getAccessPath(), toDateValue);
                                 }
                              }
                              else
                              {
                                 filterValue = null;
                              }
                              return dataFilter;
                           }
                           else
                           {
                              return DataFilter.isEqual(data, dataPath.getAccessPath(), filterValue);
                           }
                        }
                     }
                  }
                  else if (StringUtils.isEmpty(dataPath.getAccessPath()))
                  {
                     if (wildcardSearch)
                     {
                        return DataFilter.like(data, filterString, propertyFilter.isCaseSensitive());
                     }
                     else
                     {
                        if (filterValue == null)
                        {
                           return DataFilter.isEqual(data, filterString, propertyFilter.isCaseSensitive());
                        }
                        else if (PredefinedConstants.PROCESS_PRIORITY.equals(data))
                        {
                           return WorklistQuery.PROCESS_INSTANCE_PRIORITY.isEqual(((Number)filterValue).intValue());
                        }
                        else
                        {
                           if (filterValue instanceof DateRange)
                           {
                              DataFilter dataFilter = null;
                              DateRange dateRange = (DateRange) filterValue;
                              Calendar fromDateValue = dateRange.getFromDate();
                              Calendar toDateValue = dateRange.getToDate();
                              if (toDateValue != null && fromDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.between(data, fromDateValue.getTime(), toDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.between(data, fromDateValue, toDateValue);
                                 }
                              }
                              else if (fromDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.greaterOrEqual(data, fromDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.greaterOrEqual(data, fromDateValue);
                                 }
                              }
                              else if (toDateValue != null)
                              {
                                 if (isDate)
                                 {
                                    dataFilter = DataFilter.lessOrEqual(data, toDateValue.getTime());
                                 }
                                 else
                                 {
                                    dataFilter = DataFilter.lessOrEqual(data, toDateValue);
                                 }
                              }
                              else
                              {
                                 filterValue = null;
                              }
                              return dataFilter;
                           }
                           else
                           {
                              return DataFilter.isEqual(data, filterValue);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      return null;
   }

   private boolean isDataType(ProcessDefinition process, String dataId, String dataTypeId)
   {
      // (fh) assumes that the models are already updated, since you have hold on a process definition.
      Models models = client.getModels();
      Model model = models.getModel(process.getModelOID());
      DataDetails data = (DataDetails) model.getData(dataId);
      return dataTypeId.equals(data.getTypeId());
   }

   public boolean isFilterableDescriptor(String descriptorId)
   {
      return isFilterableDescriptor(descriptorId, getDescriptorFilterInfo(descriptorId));
   }

   public boolean isFilterableDescriptor(String descriptorId, DescriptorFilterInfo info)
   {
      if (info == null || !info.isFilterable())
      {
         return false;
      }
      for (ProcessDefinition process : processDefinitions)
      {
         Model model = client.getModels().getModel(process.getModelOID());
         @SuppressWarnings("unchecked")
         List<DataPath> paths = process.getAllDataPaths();
         for (DataPath dataPath : paths)
         {
            if (dataPath.isDescriptor() && descriptorId.equals(dataPath.getId()))
            {
               DataDetails data = (DataDetails) model.getData(dataPath.getData());
               if (!info.matches(data.getId(), data.getTypeId(), dataPath.getAccessPath()))
               {
                  return false;
               }
            }
         }
      }
      return true;
   }
   
   public DescriptorFilterInfo getDescriptorFilterInfo(String descriptorId)
   {
      if (processDefinitions != null)
      {
         for (ProcessDefinition process : processDefinitions)
         {
            Model model = client.getModels().getModel(process.getModelOID());
            @SuppressWarnings("unchecked")
            List<DataPath> paths = process.getAllDataPaths();
            for (DataPath dataPath : paths)
            {
               if (dataPath.isDescriptor() && descriptorId.equals(dataPath.getId()))
               {
                  DataDetails data = (DataDetails) model.getData(dataPath.getData());
                  String accessPath = dataPath.getAccessPath();
                  String typeId = data.getTypeId();
                  return new DescriptorFilterInfo(data.getId(), typeId, accessPath);
               }
            }
         }
      }
      return null;
   }
}
