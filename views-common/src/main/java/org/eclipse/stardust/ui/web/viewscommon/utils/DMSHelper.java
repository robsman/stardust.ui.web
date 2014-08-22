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

import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.runtime.DmsUtils.createFolderInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.event.DocumentEvent;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;



/**
 * @author Subodh.Godbole
 * 
 */
public class DMSHelper
{
   private static final Logger trace = LogManager.getLogger(DMSHelper.class);
   private static Boolean securityEnabled = null;
   
   /**
    * @param pi
    */
   public static String getProcessAttachmentsFolderPath(ProcessInstance pi)
   {
      if(pi == null)
      {
         throw new IllegalArgumentException("pi can not be null for getProcessAttachmentsFolderPath()");
      }
         
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      String s = defaultPath + "/process-attachments";
      
      return s;
   }

   /**
    * @param dms
    * @param folderPath
    * @throws DocumentManagementServiceException
    */
   public static Folder ensureFolderExists(DocumentManagementService dms, String folderPath)
         throws DocumentManagementServiceException
   {
      Folder targetFolder = null;
      
      if (!isEmpty(folderPath) && folderPath.startsWith("/"))
      {
         // try to create folder
         String[] segments = folderPath.substring(1).split("/");

         // walk backwards to find existing path prefix, then go forward again creating
         // missing segments

         Folder folder = null;
         LinkedList<String> missingSegments = newLinkedList();
         for (int i = segments.length - 1; i >= 0; --i)
         {
            StringBuilder path = new StringBuilder();
            for (int j = 0; j <= i; ++j)
            {
               path.append("/").append(segments[j]);
            }

            folder = dms.getFolder(path.toString(), Folder.LOD_NO_MEMBERS);
            if (null != folder)
            {
               // found existing prefix
               break;
            }
            else
            {
               // folder missing?
               missingSegments.add(0, segments[i]);
            }
         }
         
         String currentPath = (null != folder) ? folder.getPath() : "";
         while (!missingSegments.isEmpty())
         {
            String parentFolderId = isEmpty(currentPath) ? "/" // VfsUtils.REPOSITORY_ROOT
                  : currentPath;

            String segment = missingSegments.remove(0);

            // create missing sub folder
            folder = dms.createFolder(parentFolderId, createFolderInfo(segment));
            currentPath = folder.getPath();
         }
         
         targetFolder = folder;
      }
      else
      {
         targetFolder = dms.getFolder(folderPath);
      }

      return targetFolder;
   }
   
   /**
    * @param processInstance
    * @return
    */
   public static boolean existsProcessAttachmentsDataPath(ProcessInstance processInstance)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Model model = modelCache.getModel(processInstance.getModelOID());
      ProcessDefinition pd = model != null ? model.getProcessDefinition(processInstance
            .getProcessID()) : null;
      List<DataPath> dataPaths = pd.getAllDataPaths();
      
      for (DataPath dataPath : dataPaths)
      {
          if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId()) && dataPath.getDirection().equals(Direction.IN))
          {
             return true;
          }
      }
      
      return false;
   }

   /**
    * @param pi
    * @return
    */
   public static List<Document> fetchProcessAttachments(ProcessInstance processInstance)
   {
      List<Document> processAttachments = new ArrayList<Document>();

      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(), processInstance.getProcessID()) ;
      List dataPaths = processDefinition.getAllDataPaths();
      
      for (int n = 0; n < dataPaths.size(); ++n)
      {
         DataPath dataPath = (DataPath) dataPaths.get(n);

         if (!dataPath.getDirection().equals(Direction.IN))
         {
            continue;
         }

         try
         {
            if (dataPath.getId().equals(CommonProperties.PROCESS_ATTACHMENTS))
            {
               Object object = getWorkflowService().getInDataPath(processInstance.getOID(), dataPath.getId());

               if (object != null)
               {
                  processAttachments.addAll((Collection) object);
                  break;
               }
            }
         }
         catch (Exception e)
         {
            trace.error("Error fetching Process Attachments: " + e.getMessage(), e);
         }
      }
      
      return processAttachments;
   }
   
   /**
    * @param processAttachments
    */
   public static void saveProcessAttachments(ProcessInstance processInstance, List<Document> processAttachments)
   {
      getWorkflowService().setOutDataPath(processInstance.getOID(),
            CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
   }
   
   /**
    * @param processAttachments
    * @param documentToCheck
    */
   public static boolean containsProcessAttachment(List<Document> processAttachments, Document documentToCheck)
   {
      if (getDocumentIndex(processAttachments, documentToCheck) > -1)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @param processInstance
    * @param processAttachments
    * @param document
    * @return
    */
   public static boolean addAndSaveProcessAttachment(ProcessInstance processInstance,
         List<Document> processAttachments, Document document)
   {
      if(!DMSHelper.containsProcessAttachment(processAttachments, document))
      {
         processAttachments.add(document);
         DMSHelper.saveProcessAttachments(processInstance, processAttachments);
         IppEventController.getInstance().notifyEvent(
               new DocumentEvent(DocumentEvent.EventType.CREATED, DocumentEvent.EventMode.PROCESS_ATTACHMENTS,
                     processInstance.getOID(), document, processAttachments));
         return true;
      }

      return false;
   }
   
   /**
    * updates the provided process instance with provided new document
    * 
    * @param processInstance
    * @param document
    * @return
    */
   public static boolean addAndSaveProcessAttachment(ProcessInstance processInstance,
         Document document)
   {
      List<Document> processAttachments = DMSHelper
            .fetchProcessAttachments(processInstance);
      
      return addAndSaveProcessAttachment(processInstance, processAttachments, document);
   }

   /**
    * Renames the document to added with timeStamp if the attachment with same name already exists
    * 
    * @param processInstance
    * @param documentToBeAdded
    * @param renameIfDuplicate
    * @return
    */
   public static boolean addAndSaveProcessAttachment(ProcessInstance processInstance, Document documentToBeAdded,
         boolean renameIfDuplicate)
   {
      List<Document> processAttachments = DMSHelper.fetchProcessAttachments(processInstance);

      if (renameIfDuplicate)
      {
         if (null != getProcessAttachment(processInstance, documentToBeAdded.getName(), processAttachments))
         {
            documentToBeAdded.setName(DocumentMgmtUtility.appendTimeStamp(documentToBeAdded.getName()));
            documentToBeAdded = DocumentMgmtUtility.getDocumentManagementService().updateDocument(documentToBeAdded,
                  false, "", false);
         }
      }

      return addAndSaveProcessAttachment(processInstance, processAttachments, documentToBeAdded);
   }

   /**
    * return the attachment with provided name for a process instance
    * 
    * @param processInstance
    * @param docName
    * @param processAttachments
    *           (optional)
    * @return
    */
   public static Document getProcessAttachment(ProcessInstance processInstance, String docName,
         List<Document> processAttachments)
   {

      if (null == processAttachments)
      {
         processAttachments = DMSHelper.fetchProcessAttachments(processInstance);
      }
      // check in the process attachments (attachment may be resided in different
      // location)
      for (Document attachment : processAttachments)
      {
         if (null != attachment && (attachment.getName().equalsIgnoreCase(docName)))
         {
            return attachment;
         }
      }
      return null;
   }
  
   /**
    * deletes the process attachment from provided process instance
    * 
    * @param processInstance
    * @param documentToBeDeltd
    */
   public static void deleteProcessAttachment(ProcessInstance processInstance, Document documentToBeDeltd)
   {
      List<Document> processAttachments = fetchProcessAttachments(processInstance);
      IppEventController.getInstance().notifyEvent(
            new DocumentEvent(DocumentEvent.EventType.DELETED, DocumentEvent.EventMode.PROCESS_ATTACHMENTS,
                  processInstance.getOID(), documentToBeDeltd, processAttachments));

   }

   /**
    * @param processInstance
    * @param documentToBeDetached
    * @return
    */
   public static boolean detachProcessAttachment(ProcessInstance processInstance, Document documentToBeDetached)
   {
      List<Document> processAttachments = DMSHelper.fetchProcessAttachments(processInstance);
      int index = getDocumentIndex(processAttachments, documentToBeDetached);

      if (index > -1)
      {
         processAttachments.remove(index);
         DMSHelper.saveProcessAttachments(processInstance, processAttachments);

         IppEventController.getInstance().notifyEvent(
               new DocumentEvent(DocumentEvent.EventType.DELETED, DocumentEvent.EventMode.PROCESS_ATTACHMENTS,
                     processInstance.getOID(), documentToBeDetached, processAttachments));

         return true;
      }
      return false;
   }
   
   /**
    * updates the process attachment into provided process instance
    * @param processInstance
    * @param documentToBeUpdated
    * @return
    */
   public static boolean updateProcessAttachment(ProcessInstance processInstance, Document documentToBeUpdated)
   {
      List<Document> processAttachments = fetchProcessAttachments(processInstance);
      int index = getDocumentIndex(processAttachments, documentToBeUpdated);

      if (index > -1)
      {
         processAttachments.remove(index);
         processAttachments.add(index, documentToBeUpdated);
         DMSHelper.saveProcessAttachments(processInstance, processAttachments);

         publishProcessAttachmentUpdatedEvent(processInstance, processAttachments, documentToBeUpdated);

         return true;
      }
      return false;
   }
   
   /**
    * @param processInstance
    * @param processAttachments
    * @param documentToBeUpdated
    */
   public static void publishProcessAttachmentUpdatedEvent(ProcessInstance processInstance,
         List<Document> processAttachments, Document documentToBeUpdated)
   {
      IppEventController.getInstance().notifyEvent(
            new DocumentEvent(DocumentEvent.EventType.EDITED, DocumentEvent.EventMode.PROCESS_ATTACHMENTS,
                  processInstance.getOID(), documentToBeUpdated, processAttachments));
   }
   
   
   /**
    * @return
    */
   private static WorkflowService getWorkflowService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getWorkflowService();
   }
   
   public static boolean hasPrivilege(String resourceID, DmsPrivilege privilege)
   {
      if (isSecurityEnabled())
      {
         DocumentManagementService dms = ContextPortalServices.getDocumentManagementService();
         try
         {
            if ((dms.getPrivileges(resourceID).contains(privilege))
                  || dms.getPrivileges(resourceID).contains(DmsPrivilege.ALL_PRIVILEGES))
            {
               return true;
            }
            else
            {
               return false;
            }
         }
         catch (Exception e)
         {
            //TODO- review the same after CRNT-29870 is resolved
            return false;
         }
         
      }
      else
      {
         // Return true if Security is not enabled
         return true;
      }
   }

   public static boolean isSecurityEnabled()
   {
       if (securityEnabled == null){
           initializeSecurityEnabled();
       }
      return securityEnabled;
   }
   
   /**
    * returns -1 if the document does not exist in process attachments or valid index in
    * the provided process attachment list
    * 
    * @param processAttachments
    * @param documentToCheck
    * @return
    */
   private static int getDocumentIndex(List<Document> processAttachments, Document documentToCheck)
   {
      int counter = 0;
      if (documentToCheck != null)
      {
         for (Document document : processAttachments)
         {
            if (document != null && document.getId() != null)
            {
               if (document.getId().equals(documentToCheck.getId()))
               {
                  return counter;
               }
               counter++;
            }
         }
      }
      return -1;
   }

   /**
    * initialize jcr security flag
    */
   private static void initializeSecurityEnabled()
   {
      try
      {
         DocumentManagementService dms = ContextPortalServices.getDocumentManagementService();
         Set<AccessControlPolicy> applicablePolicies = dms.getApplicablePolicies("/");
         Set<AccessControlPolicy> policies = dms.getPolicies("/");

         if (applicablePolicies.isEmpty() && policies.isEmpty())
         {
            // jcr security is disabled
            securityEnabled = false;
         }
         else
         {
            securityEnabled = true;
         }
      }
      catch (DocumentManagementServiceException dmse)
      {
         securityEnabled = true;
      }
   }
}