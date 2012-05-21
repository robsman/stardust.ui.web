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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.common.form.jsf.messages.DefaultLabelProvider;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.common.introspection.xsd.XsdPath;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;




/**
 * @author Yogesh.Manware
 * @author Subodh.Godbole
 * 
 */
public class TypedDocumentsUtil
{

   private static final Logger trace = LogManager.getLogger(TypedDocumentsUtil.class);

   /**
    * returns IN data paths details associated with provided process instance data in
    * Association between IN datapath and corresponding OUT datapath is determined by comparing document data in different datapaths
    * datapaths having duplicate document data will be ignored
    * 
    * @param processInstance
    * @return
    */
   public static List<TypedDocument> getTypeDocuments(ProcessInstance processInstance)
   {
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());

      Map<String, TypedDocument> typedDocumentsData = new HashMap<String, TypedDocument>();
      Map<String, DataPath> outDataMappings = new HashMap<String, DataPath>();
      Model model = ModelUtils.getModel(processInstance.getModelOID());
      @SuppressWarnings("rawtypes")
      List dataPaths = processDefinition.getAllDataPaths();
      
      TypedDocument typedDocument;
      String dataDetailsQId;
      
      for (Object objectDataPath : dataPaths)
      {
         DataPath dataPath = (DataPath) objectDataPath;
         DataDetails dataDetails = (DataDetails) model.getData(dataPath.getData());
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()))
         {
            dataDetailsQId = dataDetails.getQualifiedId();
            Direction direction = dataPath.getDirection();
            if (Direction.IN.equals(direction) && !typedDocumentsData.containsKey(dataDetailsQId))
            {
               try
               {
                  typedDocument = new TypedDocument(processInstance, dataPath, dataDetails);
                  if (outDataMappings.containsKey(dataDetailsQId))
                  {
                     typedDocument.setDataPath(outDataMappings.get(dataDetailsQId));
                     typedDocument.setOutMappingExist(true);
                  }
                  typedDocumentsData.put(dataDetailsQId, typedDocument);
               }
               catch (Exception e)
               {
                  trace.error(e);
               }
            }
            else if (Direction.OUT.equals(direction))
            {
               if (typedDocumentsData.containsKey(dataDetailsQId))
               {
                  typedDocument = typedDocumentsData.get(dataDetailsQId);
                  if (!typedDocument.isOutMappingExist())
                  {
                     typedDocument.setDataPath(dataPath);
                     typedDocument.setOutMappingExist(true);
                  }
               }
               else
               {
                  outDataMappings.put(dataDetailsQId, dataPath);
               }
            }
         }
      }

      return new ArrayList<TypedDocument>(typedDocumentsData.values());
   }

   /**
    * update typed document
    * 
    * @param typedDocoumet
    */
   public static void updateTypedDocument(TypedDocument typedDocument)
   {
      updateTypedDocument(typedDocument.getProcessInstance().getOID(), typedDocument.getDataPath().getId(),
            typedDocument.getDataDetails().getId(), typedDocument.getDocument());
   }

   /**
    * @param processInstanceOID
    * @param dataPathId
    * @param dataId
    * @param document
    */
   public static void updateTypedDocument(long processInstanceOID, String dataPathId, String dataId, Document document)
   {
      try
      {
         // CRNT-21235 - following code is kept for backward compatibility 
         if (null != document)
         {
            document.getProperties().remove(CommonProperties.DESCRIPTION);
            document.getProperties().remove(CommonProperties.COMMENTS);
         }
         ServiceFactoryUtils.getWorkflowService().setOutDataPath(processInstanceOID, dataPathId, document);
         // update activity panel
         IppEventController.getInstance().notifyEvent(
               new DocumentEvent(DocumentEvent.EventType.EDITED, processInstanceOID, dataId, document));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param document
    * @param supressBlank
    * @return
    */
   @SuppressWarnings("unchecked")
   public static List<Pair<String, String>> getMetadataAsList(Document document, boolean supressBlank)
   {
      Path path = null;
      List<Pair<String, String>> metadataList = new ArrayList<Pair<String,String>>();

      if (null != document && null != document.getDocumentType())
      {
         Model model = ModelUtils.getModelForDocumentType(document.getDocumentType());
         if (null != model)
         {
            String typeId = document.getDocumentType().getDocumentTypeId()
                  .substring(document.getDocumentType().getDocumentTypeId().lastIndexOf("}") + 1);
   
            TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeId);
            Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
   
            for (TypedXPath tPath : allXPaths)
            {
               if (tPath.getParentXPath() == null)
               {
                  path = new XsdPath(null, tPath, true);
                  break;
               }
            }
   
            if (null != path && null != document.getProperties())
            {
               getMetadataAsList(path, document.getProperties(), metadataList, supressBlank, model);
            }
         }
      }

      return metadataList;
   }
   
   /**
    * @author Yogesh.Manware
    * @param data
    * @return
    */
   public static DocumentType getDocumentTypeFromData(Data data)
   {
      DeployedModel relevantModel = ModelUtils.getModel(data.getModelOID());
      return DocumentTypeUtils.getDocumentTypeFromData(relevantModel, data);
   }


   /**
    * @param path
    * @param metadata
    * @param metadataList
    * @param supressBlank
    * @param model
    */
   private static void getMetadataAsList(Path path, Map<String, ? > metadata, List<Pair<String, String>> metadataList,
         boolean supressBlank, Model model)
   {
      if (path.isPrimitive())
      {
         String value = getValue(path, metadata);
         if (supressBlank && StringUtils.isEmpty(value))
         {
         }
         else
         {
            String label = null;
            if (path instanceof XsdPath)
            {
               label = getFullXPathLabel(((XsdPath)path).getTypedXPath(), model);
            }
            else // This should not happen!
            {
               label = DefaultLabelProvider.convertToLabel(path.getId());
            }

            metadataList.add(new Pair<String, String>(label, value));
         }
      }
      else
      {
         for (Path cPath : path.getChildPaths())
         {
            getMetadataAsList(cPath, metadata, metadataList, supressBlank, model);
         }
      }
   }
   
   /**
    * @param path
    * @param mainMetadata
    * @return
    */
   @SuppressWarnings("unchecked")
   private static String getValue(Path path, Map<String, ?> mainMetadata)
   {
      String[] paths = path.getFullXPath().split("/");
      
      Map<String, ?> data = mainMetadata;
      for (int i = 2; i < paths.length - 1; i++)
      {
         if (data.get(paths[i]) instanceof Map)
         {
            data = (Map<String, ?>)data.get(paths[i]);
         }
         else
         {
            return null;
         }
      }

      return getValueAsString(path, data.get(paths[paths.length - 1]));
   }

   /**
    * @param path
    * @param value
    * @return
    */
   private static String getValueAsString(Path path, Object value)
   {
      String retValue = null;
      
      if (null != value)
      {
         if (path.getJavaClass() == Date.class || path.getJavaClass() == Calendar.class)
         {
            Date dateValue = null;
            if (path.getJavaClass() == Calendar.class)
            {
               dateValue = ((Calendar) value).getTime();
            }
            else
            {
               dateValue = (Date) value;
            }
   
            if ("date".equals(path.getTypeName()))
            {
               retValue = DateUtils.formatDate(dateValue);
            }
            else if ("time".equals(path.getTypeName()))
            {
               retValue = DateUtils.formatTime(dateValue);
            }
            else // dateTime
            {
               retValue = DateUtils.formatDateTime(dateValue);
            }
         }
         else if (path.getJavaClass() == Boolean.class)
         {
            MessagePropertiesBean props = MessagePropertiesBean.getInstance();
            retValue = (Boolean) value ? props.getString("common.true") : props.getString("common.false");
         }
         else
         {
            retValue = value.toString();
         }
      }

      return retValue;
   }

   /**
    * @param typedXPath
    * @param model
    * @return
    */
   private static String getFullXPathLabel(TypedXPath typedXPath, Model model)
   {
      StringBuffer sb = new StringBuffer(getXPathLabel(typedXPath, model));
      while (null != typedXPath.getParentXPath())
      {
         typedXPath = typedXPath.getParentXPath();

         // Top most Parent is Declared Data Type. This needs to be skipped
         if (null != typedXPath && null == typedXPath.getParentXPath()) 
         {
            break;
         }

         sb.insert(0, getXPathLabel(typedXPath, model) + " / ");
      }

      return sb.toString();
   }

   /**
    * @param typedXPath
    * @param model
    * @return
    */
   private static String getXPathLabel(TypedXPath typedXPath, Model model)
   {
      String label = I18nUtils.getLabel(typedXPath, model, typedXPath.getId());
      if (label.equals(typedXPath.getId()))
      {
         label = DefaultLabelProvider.convertToLabel(typedXPath.getId());
      }
         
      return label;
   }
}