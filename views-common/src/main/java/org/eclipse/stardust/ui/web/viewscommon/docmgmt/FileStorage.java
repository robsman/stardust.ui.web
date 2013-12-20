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

package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class FileStorage implements Serializable
{
   private static final long serialVersionUID = -7062477503597547388L;

   private static final String BEAN_NAME = "fileStorage";

   private final static String FIRE_STORAGE_LOCATION = "Carnot.Portal.FileUploadPath";

   private Map<String, String> uuidPathMap = new HashMap<String, String>();

   public static FileStorage getInstance()
   {
      return (FileStorage) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * 
    * @param uuid
    * @return
    */
   // Physical Path
   public String pullPath(String uuid)
   {
      String path = uuidPathMap.get(uuid);
      return path;
   }

   /**
    * 
    * @param path
    * @return
    */
   public String pushPath(String path)
   {
      String uuid = getUUID();
      uuidPathMap.put(uuid, path);
      return uuid;
   }

   /**
    * 
    * @param uuid
    * @return
    */
   public AbstractDocumentContentInfo retrieveFile(String uuid)
   {
      String path = uuidPathMap.get(uuid);
      if (path == null)
      {
         return null;
      }
      FileSystemJCRDocument fileSystemJCRDoc = new FileSystemJCRDocument(path, null, null, null, null);
      return fileSystemJCRDoc;
   }

   /**
    * 
    * @return
    */
   private String getUUID()
   {
      return UUID.randomUUID().toString();
   }

   /**
    * 
    * @param servletContext
    * @return
    */
   public String getStoragePath(ServletContext servletContext)
   {
      return Parameters.instance().getString(FIRE_STORAGE_LOCATION, servletContext.getRealPath("/"));
   }
}
