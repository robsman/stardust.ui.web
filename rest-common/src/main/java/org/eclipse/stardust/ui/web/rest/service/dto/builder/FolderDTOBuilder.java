/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class FolderDTOBuilder extends AbstractDTO
{
   /**
    * @param folder
    * @return
    */
   public static FolderDTO build(Folder folder)
   {
      FolderDTO FolderDTO = new FolderDTO();

      if (folder != null)
      {
         FolderDTO.uuid = folder.getId();
         FolderDTO.name = folder.getName();
         FolderDTO.path = folder.getPath();
         if (folder.getFolderCount() > 0 || folder.getDocumentCount() > 0)
         {
            FolderDTO.hasChildren = true;
         }
      }

      return FolderDTO;
   }

   /**
    * @param folders
    * @return
    */
   public static List<FolderDTO> build(List<Folder> folders)
   {
      List<FolderDTO> folderDTOs = CollectionUtils.newArrayList();

      for (Folder folder : folders)
      {
         folderDTOs.add(build(folder));
      }

      return folderDTOs;
   }

   /**
    * Prevent instantiation
    */
   private FolderDTOBuilder()
   {

   }

}
