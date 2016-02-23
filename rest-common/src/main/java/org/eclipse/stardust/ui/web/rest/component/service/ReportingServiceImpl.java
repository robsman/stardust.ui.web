/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
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
public class ReportingServiceImpl implements ReportingService
{
   private static final String REPORTS_ROOT_FOLDER = "/reports";
   private static final String REPORT_DESIGN = "designs";
   private static final String SAVED_REPORTS = "saved-reports";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * @return
    */
   @Override
   public List<FolderDTO> getPersonalReports()
   {
      List<FolderDTO> roleOrgReportDefinitionsNodes = new ArrayList<FolderDTO>();

      Folder participantFolder = getDMS().getFolder(REPORTS_ROOT_FOLDER, Folder.LOD_LIST_MEMBERS);
      List<Folder> subfolders = participantFolder.getFolders();
      User loggedInUser = IppUserProvider.getInstance().getUser();

      ModelCache modelCache = ModelCache.findModelCache();

      for (Folder participantSubFolder : subfolders)
      {
         // check the permissions to current user
         if (loggedInUser.isAdministrator() || loggedInUser.isInOrganization(participantSubFolder.getName()))
         {
            Participant participant = modelCache.getParticipant(participantSubFolder.getName(), null);
            if (participant == null)
            {
               continue;
            }

            Folder ReportDesignFolder = DocumentMgmtUtility.getFolder(participantSubFolder, SAVED_REPORTS,
                  Folder.LOD_LIST_MEMBERS_OF_MEMBERS);
            Folder savedReportFolder = DocumentMgmtUtility.getFolder(participantSubFolder, REPORT_DESIGN,
                  Folder.LOD_LIST_MEMBERS_OF_MEMBERS);

            if (ReportDesignFolder == null && savedReportFolder == null)
            {
               continue;
            }

            FolderDTO participantFolderDTO = FolderDTOBuilder.build(participantSubFolder);
            participantFolderDTO.folders = new ArrayList<FolderDTO>();
            participantFolderDTO.hasChildren = true;

            if (ReportDesignFolder != null)
            {
               participantFolderDTO.folders.add(FolderDTOBuilder.build(ReportDesignFolder));
            }

            if (savedReportFolder != null)
            {
               participantFolderDTO.folders.add(FolderDTOBuilder.build(savedReportFolder));
            }

            roleOrgReportDefinitionsNodes.add(participantFolderDTO);
         }
      }
      return roleOrgReportDefinitionsNodes;
   }

   /**
    * @return
    */
   private DocumentManagementService getDMS()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }
}