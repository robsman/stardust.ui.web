/*
 * $Id$
 * (C) 2000 - 2014 CARNOT AG
 */
package org.eclipse.stardust.ui.web.reporting.common;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;

public class DocumentUtils
{
   public static final String PUBLIC_REPORT_DEFINITIONS_DIR = "/reports";
   public static final String PARTICIPANTS_REPORT_DEFINITIONS_DIR = "/participants/reports/";

   private DocumentManagementService documentManagementService;
   private ServiceFactory serviceFactory = null;
   private UserService userService;

   public DocumentUtils(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;

   }


   /**
   *
   * @return
   */
  private UserService getUserService()
  {
   if (userService == null)
     {
        userService = serviceFactory.getUserService();
     }

     return userService;
  }


   /**
   *
   * @return
   */
  public DocumentManagementService getDocumentManagementService()
  {
     if (documentManagementService == null)
     {
        documentManagementService = serviceFactory.getDocumentManagementService();
     }

     return documentManagementService;
  }


  /**
  *
  * @return
  */
 public String getUserDocumentFolderPath()
 {
    return "/realms/" + getUserService().getUser().getRealm().getId() + "/users/"
          + getUserService().getUser().getId() + "/documents/reports/designs";
 }

 /**
  *
  * @return
  */
 public String getParticipantDocumentFolderPath(String participant)
 {
    return PARTICIPANTS_REPORT_DEFINITIONS_DIR + participant;
 }




  /**
   * Returns the folder if exist otherwise create new folder
   *
   * @param folderPath
   * @return
   */
  public Folder findOrCreateFolder(String folderPath)
  {
     Folder folder = getDocumentManagementService().getFolder(folderPath);

     if (null == folder)
     {
        // folder does not exist yet, create it
        String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
        String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

        if (StringUtils.isEmpty(parentPath))
        {
           // Top-level reached

           return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
        }
        else
        {
           Folder parentFolder = findOrCreateFolder(parentPath);

           return getDocumentManagementService().createFolder(parentFolder.getId(),
                 DmsUtils.createFolderInfo(childName));
        }
     }
     else
     {
        return folder;
     }
  }
}