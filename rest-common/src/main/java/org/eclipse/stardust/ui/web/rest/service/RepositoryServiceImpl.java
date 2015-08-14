/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class RepositoryServiceImpl implements RepositoryService
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    *
    */
   public Map<String, List<DocumentDTO>> getFolder(String folderId)
   {
      Folder folder = getDMS().getFolder(folderId);

      Map<String, List<DocumentDTO>> childs = new HashMap<String, List<DocumentDTO>>();
      childs.put("folders", new ArrayList<DocumentDTO>());
      childs.put("documents", new ArrayList<DocumentDTO>());

      if (folder == null)
      {
         throw new I18NException("No Folder exist with Id: " + folderId);
      }

      // update child nodes
      int folderCount = folder.getFolderCount();
      int documentCount = folder.getDocumentCount();

      if (folderCount > 0 || documentCount > 0)
      {
         // create new folder nodes
         childs.get("folders").addAll(DocumentDTOBuilder.buildFolders(folder.getFolders()));

         // create new document nodes
         childs.get("documents").addAll(DocumentDTOBuilder.build(folder.getDocuments()));
      }
      return childs;
   }

   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }
}