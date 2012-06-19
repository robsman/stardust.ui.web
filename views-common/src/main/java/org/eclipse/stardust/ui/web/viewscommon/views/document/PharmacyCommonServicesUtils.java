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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;



public class PharmacyCommonServicesUtils
{
   private static final String STAMPS_FOLDER = "/stamps";
   private static final String STANDARD_STAMPS_FOLDER = "/documents" + STAMPS_FOLDER;
   private static final Logger trace = LogManager.getLogger(PharmacyCommonServicesUtils.class);

   /**
    * @return
    */
   public static String getStampsJSON(HttpServletRequest httpRequest)
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");
      DocumentManagementService dms = (DocumentManagementService) sessionSharedMap
            .getObject("DOCUMENT_MANAGEMENT_SERVICE");
      SessionContext sessionContext = (SessionContext) sessionSharedMap.getObject("SESSION_CONTEXT");

      StringBuffer jsonString = new StringBuffer("{");

      jsonString.append(getStampsInFolder(httpRequest, STANDARD_STAMPS_FOLDER, dms, "STANDARD_STAMPS"));

      jsonString.append(",");

      String myStampsFolderPath = DocumentMgmtUtility.getMyDocumentsPath(sessionContext) + STAMPS_FOLDER;
      jsonString.append(getStampsInFolder(httpRequest, myStampsFolderPath, dms, "MY_STAMPS"));

      jsonString.append("}");

      return jsonString.toString();
   }

   /**
    * @return
    */
   public static String retrieveDocumentDownloadURL(HttpServletRequest httpRequest, String documentId)
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");
      DocumentManagementService dms = (DocumentManagementService) sessionSharedMap
            .getObject("DOCUMENT_MANAGEMENT_SERVICE");

      try
      {
         return DocumentMgmtUtility.getDocumentDownloadURL(documentId, httpRequest, dms);
      }
      catch (Exception e)
      {
         return "";
      }
   }

   /**
    * @param parentFolderPath
    * @param dms
    * @param folderName
    * @return
    */
   public static String getStampsInFolder(HttpServletRequest httpRequest, String parentFolderPath,
         DocumentManagementService dms, String folderName)
   {
      StringBuffer jsonString = new StringBuffer("");
      jsonString.append("\"").append(folderName).append("\" : {\"Uncategorized\" : ");

      Folder parentFolder = dms.getFolder(parentFolderPath, Folder.LOD_LIST_MEMBERS_OF_MEMBERS);
      if (null != parentFolder)
      {
         jsonString.append(getStampsInFolder(httpRequest, dms, parentFolder));

         List<Folder> subFolders = getAllSubFolders(parentFolder);
         for (int i = 0; i < subFolders.size(); i++)
         {
            jsonString.append(", ");
            jsonString.append("\"").append(((Folder) subFolders.get(i)).getName()).append("\" : ");
            jsonString.append(getStampsInFolder(httpRequest, dms, (Folder) subFolders.get(i)));
         }
      }
      else
      {
         jsonString.append("\"\"");
      }

      jsonString.append("}");

      return jsonString.toString();
   }
   
   public static Document getDocument(HttpServletRequest httpRequest, String documentId)
   {
      SessionSharedObjectsMap sessionSharedMap = (SessionSharedObjectsMap) httpRequest.getSession().getAttribute(
            "sessionSharedObjectsMap");
      DocumentManagementService dms = (DocumentManagementService) sessionSharedMap
            .getObject("DOCUMENT_MANAGEMENT_SERVICE");
      return dms.getDocument(documentId);
   }

   /**
    * @return
    */
   public static List<Folder> getAllSubFolders(Folder folder)
   {
      try
      {
         return appendAllSubFolders(copyToModifyableFolderList(folder.getFolders()));
      }
      catch (Throwable e)
      {
         trace.error(e);
      }

      return new ArrayList<Folder>();
   }

   /**
    * @return
    */
   private static List<Folder> appendAllSubFolders(List<Folder> folders)
   {
      List<Folder> subFolders = new ArrayList<Folder>();
      for (Folder folder : folders)
      {
         subFolders.addAll(folder.getFolders());
      }
      if (subFolders.size() > 0)
      {
         appendAllSubFolders(subFolders);
      }

      folders.addAll(subFolders);

      return folders;
   }

   /**
    * Returns stamps in the folder as a JSON array. Throws nullpointer exception if
    * 'folder' is null
    * 
    * @param dms
    * @param folderpath
    * @return
    */
   private static String getStampsInFolder(HttpServletRequest httpRequest, DocumentManagementService dms, Folder folder)
   {
      StringBuffer jsonString = new StringBuffer();
      List<Document> stamps = folder.getDocuments();
      jsonString.append("[");
      for (int i = 0; i < stamps.size(); i++)
      {
         jsonString.append("{\"stampDocId\" : \"");
         jsonString.append(stamps.get(i).getId());
         jsonString.append("\", \"stampURL\" : \"");
         jsonString.append(DocumentMgmtUtility.getDocumentDownloadURL(stamps.get(i).getId(), httpRequest, dms));
         jsonString.append("\"}");
         if (i < (stamps.size() - 1))
         {
            jsonString.append(", ");
         }
      }
      jsonString.append("]");

      return jsonString.toString();
   }

   /**
    * @param unmodifyableList
    * @return
    */
   private static List<Folder> copyToModifyableFolderList(List<Folder> unmodifyableList)
   {
      List<Folder> modifyableList = new ArrayList<Folder>();
      modifyableList.addAll(unmodifyableList);

      return modifyableList;
   }
}
