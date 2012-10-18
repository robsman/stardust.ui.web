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


import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;


/**
 * @author Subodh.Godbole
 *
 */
public class DMSUtils
{
   public static final String DOCUMENT_ICON_BASE_PATH = "/plugins/views-common/images/icons/mime-types/";
   public static final String I_FOLDER_MY_DOCS = "/plugins/views-common/images/icons/folder_user.png";
   public static final String I_FOLDER_CORRESPONDENCE = "/plugins/views-common/images/icons/folder_page.png";
   public static final String I_FOLDER_PROCESS_ATTACH = "/plugins/views-common/images/icons/process-attachment.png";
   public static final String F_CORRESPONDENCE_FOLDER = "/documents/correspondence-templates";
   private static final char SPECIAL_CHARS[] = {'<', '>', '/', '\\', '*', '?', ':', '|', '\"'};
   
   /**
    * @param id
    * @return
    */
   public static Folder getFolder(String id)
   {
      Folder folder = getDocumentManagementService().getFolder(id);
      return folder;
   }

   /**
    * @return
    */
   public static Folder getCommonDocumentsFolder()
   {
      return getFolder("/documents");
   }

   /**
    * @return
    */
   public static Folder getMyDocumentsFolder()
   {
      User user = getUser();

      StringBuffer folderPath = new StringBuffer();
      folderPath.append("/ipp-repository/partitions/").append(user.getPartitionId()).append("/realms/")
            .append(user.getRealm().getId()).append("/users/").append(user.getAccount()).append("/documents");

      return getFolder(folderPath.toString());
   }

   /**
    * @return
    */
   public static DocumentManagementService getDocumentManagementService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getDocumentManagementService();
   }

   /**
    * @return
    */
   private static User getUser()
   {
      return SessionContext.findSessionContext().getUser();
   }
   
   /**
    * @param document
    * @return
    */
   public static String getDocumentIcon(Document document)
   {
      String path = DOCUMENT_ICON_BASE_PATH + MimeTypesHelper.detectMimeType(document.getName(), document.getContentType()).getIconPath();
      return path;
   }
   
   /**
    * @param inputString
    * @return
    */
   public static String replaceAllSpecialChars(String inputString)
   {
      String result = inputString;
      for (char c : SPECIAL_CHARS)
      {
         result = result.replace(c, '-');
      }
      return result;
   }
}
