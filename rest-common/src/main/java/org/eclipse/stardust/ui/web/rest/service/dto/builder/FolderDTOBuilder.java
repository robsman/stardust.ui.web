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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.UiPropertiesDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;

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
   @SuppressWarnings("unchecked")
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
         // read properties
         if (folder.getProperties() != null)
         {
            Map<String, Serializable> properties = folder.getProperties();
            // Ui properties
            if (properties.get(RepositoryUtility.UI_PROPERTIES) != null)
            {
               Map<String, Serializable> uiProperties = (Map<String, Serializable>) properties
                     .get(RepositoryUtility.UI_PROPERTIES);
               UiPropertiesDTO uiPropertiesDTO = new UiPropertiesDTO();
               FolderDTO.uiProperties = uiPropertiesDTO;
               if (uiProperties.get(RepositoryUtility.UIProperties.readOnly.name()) != null)
               {
                  uiPropertiesDTO.readOnly = (Boolean) uiProperties.get(RepositoryUtility.UIProperties.readOnly.name());
               }
               if (uiProperties.get(RepositoryUtility.UIProperties.clickable.name()) != null)
               {
                  uiPropertiesDTO.clickable = (Boolean) uiProperties.get(RepositoryUtility.UIProperties.clickable
                        .name());
               }
               if (uiProperties.get(RepositoryUtility.UIProperties.type.name()) != null)
               {
                  uiPropertiesDTO.resourceType = (String) uiProperties.get(RepositoryUtility.UIProperties.type.name());
               }

               // correspondence meta-data
               if (properties.get("correspondenceMetaData") != null)
               {
                  FolderDTO.correspondenceMetaDataDTO = (Map<String, Serializable>) properties
                        .get("correspondenceMetaData");
               }
            }
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
