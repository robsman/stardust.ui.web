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
package org.eclipse.stardust.ui.web.modeler.service;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;

public abstract class DmsPersistenceUtils
{

   private static final String UNVERSIONED = "UNVERSIONED";

   private static final String SPECIAL_CHARACTER_SET = "[\\\\/:*?\"<>|\\[\\]]";

   /**
    * returns DocumentManagementService
    *
    * @return DocumentManagementService
    */
   public abstract DocumentManagementService getDocumentManagementService();

   /**
    * returns current user
    *
    * @return
    */
   public abstract User getUser();

   /**
    * creates document revision or overwrites it based on the input parameter, updates document owner as
    * well.
    *
    * @param existingDocument
    * @param content
    * @param description
    * @param comments
    * @param overwrite
    * @return
    */
   public Document updateDocument(Document existingDocument, byte[] content, String description,
         String comments, boolean overwrite)
   {
      Document doc = null;

      existingDocument.setDescription(description);
      existingDocument.setOwner(getUser().getAccount());

      if (!overwrite && !isDocumentVersioned(existingDocument))
      {
         getDocumentManagementService().versionDocument(existingDocument.getId(), "", null);
      }

      if (null != content)
      {
         doc = getDocumentManagementService().updateDocument(existingDocument, content, "", !overwrite, comments, null,
               false);
      }
      return doc;
   }

   /**
    * returns true if the folder(having name as input parameter 'name') already exist
    *
    * @param parentFolder
    * @param name
    * @return
    */
   public boolean isFolderPresent(String path, String name)
   {
      Folder parentFolder = getFolder(path);
      if (null != parentFolder)
      {
         name = stripOffSpecialCharacters(name);
         Folder finalFolder = getDocumentManagementService().getFolder(parentFolder.getId());
         List<Folder> folders = finalFolder.getFolders();

         for (Folder folder : folders)
         {
            if (folder.getName().equalsIgnoreCase(name))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * returns document if the document(having name as input parameter 'name') already exist in the
    * folder
    *
    * @param parentFolder path
    * @param name
    * @return
    */
   public Document getDocument(String path, String name)
   {
      Folder parentFolder = getFolder(path);
      if (null != parentFolder)
      {
         name = stripOffSpecialCharacters(name);
         Folder folder = getDocumentManagementService().getFolder(parentFolder.getId());
         List<Document> documents = folder.getDocuments();
         for (Document document : documents)
         {
            if (document.getName().equalsIgnoreCase(name))
            {
               return document;
            }
         }
      }
      return null;
   }

   /**
    * returns true if the folder/file(having name as input parameter 'name') already exist
    *
    * @param path
    * @param name
    * @return
    */
   public boolean isExistingResource(String path, String name)
   {
      return (isFolderPresent(path, name) || null != getDocument(path, name));
   }

   /**
    * strips off the special characters
    *
    * @param inputString
    * @return
    */
   public static String stripOffSpecialCharacters(String inputString)
   {
      String outputString = inputString.trim();
      outputString = outputString.replaceAll(SPECIAL_CHARACTER_SET, "");
      return outputString;
   }

   /**
    * returns the folder on the specified path
    * @param path
    * @return
    */
   public Folder getFolder(String path)
   {
      return getDocumentManagementService().getFolder(path);
   }

   /**
    * @param document
    * @return
    */
   public static boolean isDocumentVersioned(Document document)
   {
      if (UNVERSIONED.equals(document.getRevisionId()))
      {
         return false;
      }
      return true;
   }

}
