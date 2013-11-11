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

package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.common.form.jsf.DocumentPath;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.common.introspection.java.JavaPath;
import org.eclipse.stardust.ui.common.introspection.xsd.XsdPath;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class ManualActivityUi
{
   private static final Logger trace = LogManager.getLogger(ManualActivityForm.class);

   private ApplicationContext applicationContext;
   private ActivityInstance activityInstance;
   
   private ManualActivityPath manualActivityPath;

   /**
    * @param generationPreferences
    * @param formBinding
    * @param activityInstance
    * @param workflowService
    * @param applicationContext
    */
   public ManualActivityUi(ActivityInstance activityInstance, ApplicationContext applicationContext)
   {
      this.activityInstance = activityInstance;
      this.applicationContext = applicationContext;
      init();
   }

   /*
    * Generates the top level panel for all Data Mappings of the Activity.
    */
   @SuppressWarnings("unchecked")
   private void init()
   {
      List<Object> allOutMappings = getApplicationContext().getAllOutDataMappings();
      List<Object> allInMappings = getApplicationContext().getAllInDataMappings();

      // Process OUT Mappings first
      List<Object> allMappings = new ArrayList<Object>();
      allMappings.addAll(allOutMappings);
      allMappings.addAll(allInMappings);

      // Process All IN/OUT Mappings and collect all of them in ManualActivityPath
      Path path = null;
      manualActivityPath = new ManualActivityPath("MA" + activityInstance.getOID(), false);
      Map<String, DataMapping> dataMappingMap = new HashMap<String, DataMapping>();
      for (Object object : allMappings)
      {
         DataMapping dataMapping = (DataMapping) object;

         if (dataMappingMap.containsKey(dataMapping.getId()))
         {
            continue;
         }

         dataMappingMap.put(dataMapping.getId(), dataMapping);
         if (trace.isDebugEnabled())
         {
            trace.debug("Processing Data Mapping - " + dataMapping.getId() + ":" + dataMapping.getName());
         }

         // Handle Data Mapping as per Type
         if (ModelUtils.isSystemDefinedData(dataMapping))
         {
            path = createSystemDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isDMSType(getModel(), dataMapping))
         {
            path = createDMSDataMapping(dataMapping, allInMappings, manualActivityPath);
         }
         else if (ModelUtils.isEnumerationType(getModel(), dataMapping))
         {
            path = createStructureDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isPrimitiveType(getModel(), dataMapping))
         {
            path = createPrimitiveDataMapping(dataMapping, manualActivityPath);
         }
         else if (ModelUtils.isStructuredType(getModel(), dataMapping))
         {
            path = createStructureDataMapping(dataMapping, manualActivityPath);
         }

         if (null != path)
         {
            manualActivityPath.getChildPaths().add(path);
         }
         else
         {
            trace.warn("Skipping Data Mapping - Not supported - " + dataMapping.getId() + ":" + dataMapping.getName());
         }
      }
   }

   /**
    * @return
    */
   public ManualActivityPath getManualActivityPath()
   {
      return manualActivityPath;
   }

   /**
    * @param dataMapping
    * @param allInMappings
    * @param maPath
    * @return
    */
   private Path createDMSDataMapping(DataMapping dataMapping, List<Object> allInMappings, ManualActivityPath maPath)
   {
      if (ModelUtils.isDocumentType(getModel(), dataMapping)) // Document
      {
         if (!isWriteOnly(dataMapping, allInMappings))
         {
            DocumentType documentType = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getDocumentTypeFromData(
                  getModel(), getModel().getData(dataMapping.getDataId()));

            if (documentType == null)
            {
               trace.debug("Could not resolve type for Document:, " + dataMapping.getQualifiedId() + ". It may be set defualt by design");
            }

            return new DocumentPath(maPath, dataMapping.getId(), documentType, null,
                  Direction.IN == dataMapping.getDirection());
         }
         else
         {
            trace.warn("Skipping Data Mapping - Found it as Write Only - " + dataMapping.getId() + ":" + dataMapping.getName());
         }
      }
      else if (ModelUtils.isFolderType(getModel(), dataMapping)) // Folder
      {
         // Skip, Not supported
      }
      else // Only Meta Data
      {
         Path docTypePath = null;
         Data documentData = getModel().getData(dataMapping.getDataId());
         String metaDataTypeId = (String) documentData.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);

         if (StringUtils.isNotEmpty(metaDataTypeId))
         {
            TypeDeclaration typeDeclaration = getModel().getTypeDeclaration(metaDataTypeId);
            Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(getModel(), typeDeclaration);

            for (TypedXPath path : allXPaths)
            {
               if ("properties".equals(dataMapping.getDataPath())) // Mapping to entire properties
               {
                  if (null == path.getParentXPath())
                  {
                     docTypePath = new XsdPath(maPath, path, dataMapping.getId(),
                           Direction.IN == dataMapping.getDirection());
                     break;
                  }
               }
               else if (dataMapping.getDataPath().equals("properties/" + path.getXPath())) // Mapping to nested item in properties
               {
                  docTypePath = new XsdPath(maPath, path, dataMapping.getId(),
                        Direction.IN == dataMapping.getDirection());
                  break;
               }
            }
         }

         // if null means - Mapping to documenmt's attributes e.g. id, owner, etc
         if (null == docTypePath)
         {
            // This is the only possibility, but still check
            if (ModelUtils.isPrimitiveType(getModel(), dataMapping))
            {
               docTypePath = createPrimitiveDataMapping(dataMapping, maPath);
            }
         }

         return docTypePath;
      }

      return null;
   }

   /**
    * @param dataMapping
    * @param maPath
    * @return
    */
   private Path createSystemDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      return new IppSystemPath(maPath, dataMapping.getId(), isReadOnly(dataMapping));
   }

   /**
    * @param dataMapping
    * @param path
    * @param maPath
    * @return
    */
   private Path createPrimitiveDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      return JavaPath.createFromClass(maPath, dataMapping.getId(), dataMapping.getMappedType(), isReadOnly(dataMapping));
   }

   /**
    * @param dataMapping
    * @param maPath
    * @return
    */
   private Path createStructureDataMapping(DataMapping dataMapping, ManualActivityPath maPath)
   {
      Set<TypedXPath> xpaths = ModelUtils.getXPaths(getModel(), dataMapping);

      for (TypedXPath path : xpaths)
      {
         if (path.getParentXPath() == null)
         {
            return new XsdPath(maPath, path, dataMapping.getId(), Direction.IN == dataMapping.getDirection());
         }
      }
      return null;
   }

   /**
    * @param dataMapping
    * @return
    */
   private boolean isReadOnly(DataMapping dataMapping)
   {
      if (ModelUtils.isSystemDefinedReadOnlyData(dataMapping))
      {
         return true;
      }
      else if (ModelUtils.isDMSReadOnlyData(getModel(), dataMapping))
      {
         return true;
      }
      return Direction.IN == dataMapping.getDirection();
   }

   /**
    * @param dataMapping
    * @param allInMappings
    * @return
    */
   private boolean isWriteOnly(DataMapping dataMapping, List<Object> allInMappings)
   {
      if (Direction.IN == dataMapping.getDirection() || Direction.IN_OUT == dataMapping.getDirection())
      {
         return false;
      }
      else if (Direction.OUT == dataMapping.getDirection())
      {
         for (Object object : allInMappings)
         {
            DataMapping dm = (DataMapping) object;
            if (dm.getId().equals(dataMapping.getId()))
            {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * @return
    */
   public Model getModel()
   {
      return org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(activityInstance.getModelOID());
   }

   public ApplicationContext getApplicationContext()
   {
      return applicationContext;
   }
}
