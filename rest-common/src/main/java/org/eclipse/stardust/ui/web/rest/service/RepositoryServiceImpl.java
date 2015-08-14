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
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.FolderDTOBuilder;
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

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   /**
    *
    */
   public Map<String, List<AbstractDTO>> getFolder(String folderId)
   {
      // fetching of children information may be time consuming, may need to be
      // parameterized later
      Folder folder = getDMS().getFolder(folderId, 2);

      Map<String, List<AbstractDTO>> childs = new HashMap<String, List<AbstractDTO>>();
      childs.put("folders", new ArrayList<AbstractDTO>());
      childs.put("documents", new ArrayList<AbstractDTO>());

      if (folder == null)
      {
         throw new I18NException(restCommonClientMessages.getParamString("folder.notFound", folderId));
      }

      // update child nodes
      int folderCount = folder.getFolderCount();
      int documentCount = folder.getDocumentCount();

      if (folderCount > 0 || documentCount > 0)
      {
         // create new folder nodes
         childs.get("folders").addAll(FolderDTOBuilder.build(folder.getFolders()));

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