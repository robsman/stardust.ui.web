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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;



/**
 * @author Yogesh.Manware
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
            typedDocument.getDocument());
   }

   /**
    * @param processInstanceOID
    * @param dataPathId
    * @param document
    */
   public static void updateTypedDocument(long processInstanceOID, String dataPathId, Document document)
   {
      try
      {
         // updating the document to process before adding description and comments
         // as kernel throws exception otherwise
         // TODO: Due to kernel issue CRNT-20987
         if (null != document)
         {
            document.getProperties().remove(CommonProperties.DESCRIPTION);
            document.getProperties().remove(CommonProperties.COMMENTS);
         }
         ServiceFactoryUtils.getWorkflowService().setOutDataPath(processInstanceOID, dataPathId, document);
         // update activity panel
         IppEventController.getInstance().notifyEvent(
               new DocumentEvent(DocumentEvent.EventType.EDITED, processInstanceOID));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
}