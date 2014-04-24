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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveXmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.xml.data.XPathUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class ModelUtils
{
   private static final Logger trace = LogManager.getLogger(ModelUtils.class);
   
   public static enum SystemDefinedDataType
   {
      PROCESS_ID,
      ROOT_PROCESS_ID,
      CURRENT_LOCALE,
      CURRENT_DATE,
      CURRENT_MODEL,
      CURRENT_USER,
      STARTING_USER,
      LAST_ACTIVITY_PERFORMER,
      PROCESS_ATTACHMENTS,
      PROCESS_PRIORITY
   }

   public static SystemDefinedData[] SYSTEM_DEFINED_DATA = new SystemDefinedData[]{
      new SystemDefinedData("PROCESS_ID", true),
      new SystemDefinedData("ROOT_PROCESS_ID", true),
      new SystemDefinedData("CURRENT_LOCALE", true),
      new SystemDefinedData("CURRENT_DATE", true),
      new SystemDefinedData("CURRENT_MODEL", true),
      new SystemDefinedData("CURRENT_USER", true),
      new SystemDefinedData("STARTING_USER", true),
      new SystemDefinedData("LAST_ACTIVITY_PERFORMER", true),
      new SystemDefinedData("PROCESS_ATTACHMENTS", true),
      new SystemDefinedData("PROCESS_PRIORITY", false)
   };

   /**
    * @author subodh.godbole
    *
    */
   public static enum DmsWritableData
   {
      DESCRIPTION ("Description", "description");
      
      private final String dataMappingId;
      private final String dataPath;
      
      /**
       * @param dataMappingId
       * @param dataPath
       */
      DmsWritableData(String dataMappingId, String dataPath)
      {
         this.dataMappingId = dataMappingId;
         this.dataPath = dataPath;
      }

      public String getDataMappingId()
      {
         return dataMappingId;
      }

      public String getDataPath()
      {
         return dataPath;
      }
   }
   
   /**
    * @param model
    * @param mapping
    * @return
    */
   public static boolean isPrimitiveType(Model model, DataMapping mapping)
   {
      Class<?> type = mapping.getMappedType();
      if (isSimplePrimitive(type))
      {
         return true;
      }

      return PrimitiveXmlUtils.isPrimitiveType(model, mapping);
   }
   
   /**
    * 
    * @param type
    * @return
    */
   public static boolean isSimplePrimitive(Class<?> type)
   {
      if (Boolean.class == type || Long.class == type || Integer.class == type || Double.class == type ||
            Float.class == type || Short.class == type || Byte.class == type ||
            String.class == type || Character.class == type || Date.class == type || Calendar.class == type)
      {
         return true;
      }
      return false;
   }
   
   /**
    * 
    * @param mapping
    * @return
    */
   public static boolean isPrimitiveAsEnum(DataMapping mapping)
   {
      Object carnotType = getDataDetails(mapping).getAttribute(PredefinedConstants.TYPE_ATT);
      if (carnotType != null && carnotType.equals("Enumeration"))
      {
         return true;
      }
      return false;
   }
   
   /**
    * @param model
    * @param mapping
    * @return
    */
   public static boolean isEnumerationType(Model model, DataMapping mapping)
   {
      boolean isEnum = false;
      if (isStructuredType(model, mapping) || isPrimitiveAsEnum(mapping))
      {
         Set<TypedXPath> xpaths = getXPaths(model, mapping);
         for (TypedXPath path : xpaths)
         {
            if (path.getParentXPath() == null)
            {
               isEnum = path.isEnumeration();
               break;
            }
         }
      }

      return isEnum;
   }

   /**
    * 
    * @return
    */
   private static DataDetails getDataDetails(DataMapping dataMapping)
   {
      Model model = ModelCache.findModelCache().getModel(dataMapping.getModelOID());
      Data data = model.getData(dataMapping.getDataId());

      return (DataDetails)data;
   }
   /**
    * @param model
    * @param mapping
    * @return
    */
   public static boolean isStructuredType(Model model, DataMapping mapping)
   {
      boolean isStruct = false;

      if (null != model)
      {
         Data data = model.getData(mapping.getDataId());

         isStruct = isStructuredType(model, data);
      }

      return isStruct;
   }
   
   public static boolean isStructuredType(Model model, Data data)
   {
      boolean isStruct = false;

      String dataType = getTypeId(data);
      isStruct = StructuredTypeRtUtils.isStructuredType(dataType);

      return isStruct;
   }
   
   public static String getTypeId(Data data)
   {
      return (String) Reflect.getFieldValue(data, "typeId");
   }   
   
   /**
    * @param model
    * @param mapping
    * @return
    */
   @SuppressWarnings("unchecked")
   public static boolean isDocumentType(Model model, DataMapping mapping)
   {
      if (isDMSType(model, mapping))
      {
         return mapping.getMappedType().isAssignableFrom(Document.class);
      }
      return false;
   }

   /**
    * @param model
    * @param mapping
    * @return
    */
   @SuppressWarnings("unchecked")
   public static boolean isFolderType(Model model, DataMapping mapping)
   {
      if (isDMSType(model, mapping))
      {
         return mapping.getMappedType().isAssignableFrom(Folder.class);
      }
      return false;
   }

   /**
    * @param model
    * @param mapping
    * @return
    */
   public static boolean isDMSType(Model model, DataMapping mapping)
   {
      Data data = model.getData(mapping.getDataId());
      return StructuredTypeRtUtils.isDmsType(getTypeId(data));
   }

   /**
    * @param model
    * @param mapping
    * @return
    */
   @SuppressWarnings("unchecked")
   public static String getDMSType(Model model, DataMapping mapping)
   {
      String type = mapping.getMappedType().isAssignableFrom(Folder.class) ? DMSAdapter.FOLDER : DMSAdapter.DOCUMENT;
      return type;
   }
   
   /**
    * @param mapping
    * @return
    */
   public static SystemDefinedData getSystemDefinedData(DataMapping mapping)
   {
      for (SystemDefinedData systemDefinedData : SYSTEM_DEFINED_DATA)
      {
         if(systemDefinedData.getDataId().equals(mapping.getId()))
         {
            return systemDefinedData;
         }
      }

      return null;
   }

   /**
    * @param id
    * @return
    */
   public static SystemDefinedDataType getSystemDefinedDataType(String id)
   {
      SystemDefinedDataType dataType = null;

      if (SystemDefinedDataType.PROCESS_ID.name().equals(id))
      {
         dataType = SystemDefinedDataType.PROCESS_ID;
      }
      else if (SystemDefinedDataType.ROOT_PROCESS_ID.name().equals(id))
      {
         dataType = SystemDefinedDataType.ROOT_PROCESS_ID;
      }
      else if (SystemDefinedDataType.CURRENT_LOCALE.name().equals(id))
      {
         dataType = SystemDefinedDataType.CURRENT_LOCALE;
      }
      else if (SystemDefinedDataType.CURRENT_DATE.name().equals(id))
      {
         dataType = SystemDefinedDataType.CURRENT_DATE;
      }
      else if (SystemDefinedDataType.CURRENT_MODEL.name().equals(id))
      {
         dataType = SystemDefinedDataType.CURRENT_MODEL;
      }
      else if (SystemDefinedDataType.CURRENT_USER.name().equals(id))
      {
         dataType = SystemDefinedDataType.CURRENT_USER;
      }
      else if (SystemDefinedDataType.STARTING_USER.name().equals(id))
      {
         dataType = SystemDefinedDataType.STARTING_USER;
      }
      else if (SystemDefinedDataType.LAST_ACTIVITY_PERFORMER.name().equals(id))
      {
         dataType = SystemDefinedDataType.LAST_ACTIVITY_PERFORMER;
      }
      else if (SystemDefinedDataType.PROCESS_ATTACHMENTS.name().equals(id))
      {
         dataType = SystemDefinedDataType.PROCESS_ATTACHMENTS;
      }
      else if (SystemDefinedDataType.PROCESS_PRIORITY.name().equals(id))
      {
         dataType = SystemDefinedDataType.PROCESS_PRIORITY;
      }

      return dataType;
   }

   /**
    * @param mapping
    * @return
    */
   public static boolean isSystemDefinedData(DataMapping mapping)
   {
      return null != getSystemDefinedData(mapping) ? true : false;
   }

   /**
    * @param mapping
    * @return
    */
   public static boolean isSystemDefinedReadOnlyData(DataMapping mapping)
   {
      SystemDefinedData systemDefinedData = getSystemDefinedData(mapping);
      if (null != systemDefinedData)
      {
         return systemDefinedData.isReadOnly();
      }
      return false;
   }
   
   /**
    * @param model
    * @param mapping
    * @return
    */
   public static boolean isDMSReadOnlyData(Model model, DataMapping mapping)
   {
      if (isDMSType(model, mapping))
      {
         for (DmsWritableData dmsWritableData : DmsWritableData.values())
         {
            if (dmsWritableData.getDataMappingId().equals(mapping.getId()))
            {
               return false;
            }
         }
         return true;
      }
      return false;
   }

   /**
    * @param model
    * @param mapping
    * @return
    */
   public static Serializable getSystemDefinedInteractionData(Model model,
         DataMapping mapping, Interaction interaction, Serializable data)
   {
      if("PROCESS_ATTACHMENTS".equals(mapping.getDataId()))
      {
         return new DMSAdapter(interaction, data, mapping.getId());
      }

      return null;
   }

   /**
    * @param model
    * @param dm
    * @return
    */
   public static Set<TypedXPath> getXPaths(Model model, DataMapping dm)
   {
      Data data = model.getData(dm.getDataId());
      Reference ref = data.getReference();
      Model refModel = model;

      String typeDeclarationId = null;
      if (ref == null)
      {
        typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
        if (data.getModelOID() != refModel.getModelOID())
        {
           refModel = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
        }
         if (!StringUtils.isEmpty(typeDeclarationId) && typeDeclarationId.indexOf("typeDeclaration") == 0)
         {
            // For data created in current model, Structured type in different model
            try
            {
               String parts[] = typeDeclarationId.split("\\{")[1].split("\\}");
               typeDeclarationId = parts[1];
               Model newRefModel = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(parts[0]);
               refModel = newRefModel != null ? newRefModel : refModel;
            }
            catch (Exception e)
            {
               trace.error("Error occured in Type declaration parsing", e);
            }
         }
      }
      else
      {
        typeDeclarationId = ref.getId();
        refModel = ModelCache.findModelCache().getModel(ref.getModelOid());
      }

      return XPathUtils.getXPaths(refModel, typeDeclarationId, dm.getDataPath());
   }

   /**
    * @return
    */
   public static SelectItem[] getPriorityItems()
   {
      int i = 0;
      SelectItem[] preferenceScopes = new SelectItem[3];
      preferenceScopes[i++] = new SelectItem(ProcessInstancePriority.HIGH,
            ProcessInstanceUtils.getPriorityLabel(ProcessInstancePriority.HIGH));
      preferenceScopes[i++] = new SelectItem(ProcessInstancePriority.NORMAL, 
            ProcessInstanceUtils.getPriorityLabel(ProcessInstancePriority.NORMAL));
      preferenceScopes[i++] = new SelectItem(ProcessInstancePriority.LOW, 
            ProcessInstanceUtils.getPriorityLabel(ProcessInstancePriority.LOW));
      
      return preferenceScopes;
   }

   /**
    * @param dm
    * @param value
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Serializable wrapPrimitiveValue(DataMapping mapping, Serializable value)
   {
      Serializable returnValue = value;
      
      // Convert Calendar to Date
      if (mapping.getMappedType().isAssignableFrom(Calendar.class))
      {
         if (value instanceof Calendar)
         {
            returnValue = ((Calendar)value).getTime();
         }
      }

      return returnValue;
   }

   /**
    * @param dm
    * @param value
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Serializable unwrapPrimitiveValue(DataMapping mapping, Serializable value)
   {
      Serializable returnValue = value;
      
      // Convert Date to Calendar
      if (mapping.getMappedType().isAssignableFrom(Calendar.class))
      {
         if (value instanceof Date)
         {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(((Date)value));
            
            returnValue = calendar;
         }
      }

      return returnValue;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class SystemDefinedData
   {
      private String dataId;
      private boolean readOnly;

      private SystemDefinedData(String dataId, boolean readOnly)
      {
         super();
         this.dataId = dataId;
         this.readOnly = readOnly;
      }

      public String getDataId()
      {
         return dataId;
      }
      public boolean isReadOnly()
      {
         return readOnly;
      }
   }
}
