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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;


public class GenericDataMapping implements ModelElement, DataMapping
{
   private final ModelElement modelElement;
   private final Direction direction;
   private final Class<?> mappedType;
   private String dataId;
   private final String dataPath;

   public static List<DataMapping> generateGenericDataMappingFromDataMappings(
         List<DataMapping> dataMappings)
   {
      List<DataMapping> genericDataMappings = new ArrayList<DataMapping>(dataMappings.size());

      for (int i = 0; i < dataMappings.size(); i++)
      {
         DataMapping dataMapping = dataMappings.get(i);
         GenericDataMapping genericDataMapping = new GenericDataMapping(dataMapping);
         genericDataMappings.add(genericDataMapping);
      }

      return genericDataMappings;
   }

   public static List<DataMapping> generateGenericDataMappingFromDataPaths(List<DataPath> dataPaths)
   {
      return generateGenericDataMappingFromDataPaths(dataPaths, false);
   }
   
   public static List<DataMapping> generateGenericDataMappingFromDataPaths(
         List<DataPath> dataPaths, boolean onlyDescriptors)
   {
      List<DataMapping> genericDataMappings = new ArrayList<DataMapping>(dataPaths.size());

      for (int i = 0; i < dataPaths.size(); i++)
      {
         DataPath dataPath = dataPaths.get(i);
         if((onlyDescriptors && dataPath.isDescriptor()) || !onlyDescriptors)
         {
            GenericDataMapping genericDataMapping = new GenericDataMapping(dataPath);
            genericDataMappings.add(genericDataMapping);
         }
      }

      return genericDataMappings;
   }
   
   /** Gets a list with all DataPath's from all models with the given process ID **/
   //TODO:FQID Fix required
   public static List<DataMapping> generateGenericDataMappingsForDescriptorFilters(
         String processId)
   {
      List<DataMapping> result = CollectionUtils.newArrayList();
      Set<String> fetchedData = CollectionUtils.newHashSet();
      ModelCache modelCache = ModelCache.findModelCache();
      Iterator<DeployedModel> modelIter = modelCache.getAllModels().iterator();
      while (modelIter.hasNext())
      {
         Model model = modelIter.next();
         ProcessDefinition pd = model.getProcessDefinition(processId);
         if(pd != null)
         {
            Iterator<DataMapping> mIter = 
               generateGenericDataMappingsForDescriptorFilters(pd).iterator();
            while (mIter.hasNext())
            {
               DataMapping gdm = mIter.next();
               if(!fetchedData.contains(gdm.getId()))
               {
                  fetchedData.add(gdm.getId());
                  result.add(gdm);
               }
            }
         }
      }
      return result;
   }

   public static List<DataMapping> generateGenericDataMappingsForDescriptorFilters(
         ProcessDefinition process)
   {
      List<DataMapping> result = Collections.emptyList();

      List dataPaths = process.getAllDataPaths();      
      if ( !dataPaths.isEmpty())
      {
         result = new ArrayList<DataMapping>(dataPaths.size());
         
         for (int i = 0; i < dataPaths.size(); i++)
         {
            DataPath dataPath = (DataPath) dataPaths.get(i);
            
            // only in data mappings can be used as filter
            if (dataPath.isDescriptor() && Direction.IN.equals(dataPath.getDirection()))
            {
               // and only those, that directly map to a primitive data, since filtering
               // on attributes of complex data cannot be done with reasonable
               // performance
               if (DescriptorFilterUtils.isDataFilterable(dataPath))
               {
                  result.add(new GenericDataMapping(dataPath));
               }
            }
         }
      }

      return result;
   }

   public GenericDataMapping(DataMapping dataMapping)
   {
      this(dataMapping, dataMapping.getDirection(), dataMapping.getMappedType(), dataMapping.getDataId(), dataMapping.getDataPath());
   }

   public GenericDataMapping(DataPath dataPath)
   {
      this(dataPath, dataPath.getDirection(), dataPath.getMappedType(), dataPath.getData(), dataPath.getAccessPath());
   }

   public GenericDataMapping(ModelElement modelElement, Direction direction,
                             Class<?> mappedType, String dataId, String dataPath)
   {
      this.modelElement = modelElement;
      this.direction = direction;
      this.mappedType = mappedType;
      this.dataId = dataId;
      this.dataPath = dataPath;
   }

   public String getNamespace()
   {
      return modelElement.getNamespace();
   }

   public AccessPoint getApplicationAccessPoint()
   {
      return (modelElement instanceof DataMapping)
            ? ((DataMapping) modelElement).getApplicationAccessPoint()
            : null;
   }

   public String getApplicationPath()
   {
      return (modelElement instanceof DataMapping)
            ? ((DataMapping) modelElement).getApplicationPath()
            : null;
   }

   public ModelElement getModelElement()
   {
      return modelElement;
   }

   public Direction getDirection()
   {
      return direction;
   }

   public Class<?> getMappedType()
   {
      return mappedType;
   }

   public String getId()
   {
      return modelElement.getId();
   }

   public String getDescription()
   {
      return modelElement.getDescription();
   }   
   
   public String getName()
   {
      return modelElement.getName();
   }

   public int getModelOID()
   {
      return modelElement.getModelOID();
   }

   public int getElementOID()
   {
      return modelElement.getElementOID();
   }

   public short getPartitionOID()
   {
      return modelElement.getPartitionOID();
   }
   
   public String getPartitionId()
   {
      return modelElement.getPartitionId();
   }

   public String getDataId()
   {
      return dataId;
   }

   public void setDataId(String dataId)
   {
      this.dataId = dataId;
   }

   public Map getAllAttributes()
   {
      return modelElement.getAllAttributes();
   }

   public Object getAttribute(String name)
   {
      return modelElement.getAttribute(name);
   }

   public String getQualifiedId()
   {
      return modelElement.getQualifiedId();
   }

   public String getDataPath()
   {
      return this.dataPath;
   }
}