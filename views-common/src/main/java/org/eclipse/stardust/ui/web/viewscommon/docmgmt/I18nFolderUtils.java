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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils;


/**
 * helps to internationalize virtual folders and system folders
 * 
 * @author Yogesh.Manware
 * 
 */
public class I18nFolderUtils
{
   // virtual folders starts
   public static final String ROOT = "root";
   public static final String MY_DOCUMENTS_V = "myDocuments";
   public static final String MY_REPORT_DESIGNS_V = "myReportDesigns";
   public static final String MY_SAVED_REPORTS_V = "mySavedReports";
   public static final String COMMON_DOCUMENTS_V = "commonDocuments";
   public static final String PROCESS_DOCUMENTS_V = "processDocuments";
   public static final String PROCESS_ATTACHMENTS_V = "processAttachments";
   public static final String SPECIFIC_DOCUMENTS_V = "specificDocuments";
   public static final String ARTIFACTS_V = "artifacts";
   public static final String NOTES_V = "notes";
   public static final String PREDEFINED_REPORTS = "predefinedReports";
   public static final String REPORTS = "reports";
   public static final String REPORT_MANAGER_REPORTS = "reportManagerReport";
   public static final String PRIVATE_REPORT_DEFINITIONS = "privateReportDefinitions";
   public static final String PUBLIC_REPORT_DEFINITIONS = "publicReportDefinitions";
   public static final String PRIVATE_SAVED_REPORTS = "privateSavedReports";
   public static final String PUBLIC_SAVED_REPORTS = "publicSavedReports";
   public static final String AD_HOC = "AdHoc";
   
   // virtual folders ends
   
   public static final String PROCESS_MODELS = "process-models";
   public static final String RULES = "rules";

   private List<SystemFolder> systemFolders = null;
   private List<SystemFolder> systemFolders_virtual = null;

   private static final String FS = "[" + "\\\\/" + "]";
   private static final String PROCESS_DOCUMENTS_PATH_PATTERN = FS + "process-instances" + FS + "\\d\\d\\d\\d" + FS
         + "\\d\\d" + FS + "\\d\\d" + FS + "\\d\\d" + FS + "\\w\\w" + "-" + "\\d*" + FS;
   private static final String USERS_PATTERN = FS + "realms" + FS + ".*" + FS + "users";
   private static final String MY_DOCUMENTS_PATTERN = USERS_PATTERN + FS + ".*" + FS + "documents";

   public I18nFolderUtils()
   {
      super();
      initialize();
   }

   public static I18nFolderUtils getInstance()
   {
      return (I18nFolderUtils) FacesUtils.getBeanFromContext("i18nFolderUtils");
   }

   /**
    * @param path
    * @return
    */
   public static String getLabel(String path)
   {
      String i18nFolderName = "";
      I18nFolderUtils i18nFolderUtils = I18nFolderUtils.getInstance();

      if (path.contains("/") || path.contains("\\")) // make sure it is not virtual folder
      {
         for (SystemFolder systemFolder : i18nFolderUtils.systemFolders)
         {
            if (path.matches(systemFolder.getPath()))
            {
               i18nFolderName = systemFolder.getI18Name();
               break;
            }
         }
         if (org.eclipse.stardust.common.StringUtils.isEmpty(i18nFolderName))
         {
            if (path.contains("/"))
            {
               i18nFolderName = StringUtils.substringAfterLast(path, "/");
            }
            else
            {
               i18nFolderName = StringUtils.substringAfterLast(path, "\\");
            }
            for (SystemFolder systemFolder : i18nFolderUtils.systemFolders_virtual)
            {
               if (i18nFolderName.matches(systemFolder.getPath()))
               {
                  i18nFolderName = systemFolder.getI18Name();
                  break;
               }
            }
         }
      }
      else
      // virtual folder
      {
         for (SystemFolder systemFolder : i18nFolderUtils.systemFolders_virtual)
         {
            if (path.equalsIgnoreCase(systemFolder.getPath()))
            {
               i18nFolderName = systemFolder.getI18Name();
               break;
            }
         }
         if (org.eclipse.stardust.common.StringUtils.isEmpty(i18nFolderName))
         {
            i18nFolderName = path;
         }
      }

      return i18nFolderName;
   }

   private void initialize()
   {
      // folders
      systemFolders = new ArrayList<SystemFolder>();
      systemFolders.add(new SystemFolder(PROCESS_DOCUMENTS_PATH_PATTERN + "process-attachments", "processAttachments"));
      systemFolders.add(new SystemFolder(PROCESS_DOCUMENTS_PATH_PATTERN + "specific-documents", "specificDocuments"));
      systemFolders.add(new SystemFolder(MY_DOCUMENTS_PATTERN, "documents"));
      systemFolders.add(new SystemFolder(USERS_PATTERN, "users"));

      String path = FS + "artifacts";
      systemFolders.add(new SystemFolder(path, "artifacts"));
      path += FS;
      systemFolders.add(new SystemFolder(path + "skins", "skins"));
      systemFolders.add(new SystemFolder(path + "bundles", "bundles"));
      systemFolders.add(new SystemFolder(path + "content", "content"));
      path = FS + "documentTypes";
      systemFolders.add(new SystemFolder(FS + "documentTypes", "documentTypes"));
      path += FS;
      systemFolders.add(new SystemFolder(path + "schemas", "schemas"));
      systemFolders.add(new SystemFolder(FS + "process-instances", "processInstances"));
      path = FS + "documents";
      systemFolders.add(new SystemFolder(path, "documents"));
      path += FS;
      systemFolders.add(new SystemFolder(path + "correspondence-templates",
            "views.genericRepositoryView.correspondenceFolderLabel"));
      systemFolders.add(new SystemFolder(path + "stamps", "stamps"));
      systemFolders.add(new SystemFolder(FS + "realms", "realms"));

      // virtual folders (having no physical path)
      systemFolders_virtual = new ArrayList<SystemFolder>();
      systemFolders_virtual.add(new SystemFolder(ROOT, ROOT));
      systemFolders_virtual.add(new SystemFolder(MY_DOCUMENTS_V, "views.myDocumentsTreeView.documentTree.myDocuments"));
      systemFolders_virtual.add(new SystemFolder(COMMON_DOCUMENTS_V,
            "views.myDocumentsTreeView.documentTree.commonDocumentsFolderLabel"));
      systemFolders_virtual.add(new SystemFolder(PREDEFINED_REPORTS, PREDEFINED_REPORTS));
      systemFolders_virtual.add(new SystemFolder(REPORTS, "views.genericRepositoryView.systemFolders.reports.label"));
      
      systemFolders_virtual.add(new SystemFolder(REPORT_MANAGER_REPORTS, "views.genericRepositoryView.systemFolders.reportManagerReport.label"));
      
      systemFolders_virtual.add(new SystemFolder(PRIVATE_REPORT_DEFINITIONS, "views.genericRepositoryView.systemFolders.privateReportDefinitions.label"));
      systemFolders_virtual.add(new SystemFolder(PUBLIC_REPORT_DEFINITIONS, "views.genericRepositoryView.systemFolders.publicReportDefinitions.label"));
      
      systemFolders_virtual.add(new SystemFolder(PRIVATE_SAVED_REPORTS, "views.genericRepositoryView.systemFolders.privateSavedReports.label"));
      systemFolders_virtual.add(new SystemFolder(PUBLIC_SAVED_REPORTS, "views.genericRepositoryView.systemFolders.publicSavedReports.label"));
      systemFolders_virtual.add(new SystemFolder(PRIVATE_SAVED_REPORTS + AD_HOC, "views.genericRepositoryView.systemFolders.savedReports.adHoc.label"));
      systemFolders_virtual.add(new SystemFolder(PUBLIC_SAVED_REPORTS + AD_HOC, "views.genericRepositoryView.systemFolders.savedReports.adHoc.label"));
      
      systemFolders_virtual.add(new SystemFolder(MY_SAVED_REPORTS_V  + AD_HOC, "views.genericRepositoryView.systemFolders.savedReports.adHoc.label"));
      
      systemFolders_virtual.add(new SystemFolder(MY_REPORT_DESIGNS_V, MY_REPORT_DESIGNS_V));
      systemFolders_virtual.add(new SystemFolder(MY_SAVED_REPORTS_V, MY_SAVED_REPORTS_V));
      systemFolders_virtual.add(new SystemFolder(PROCESS_DOCUMENTS_V,
            "views.processInstanceDetailsView.processDocumentTree.processDocuments"));
      systemFolders_virtual.add(new SystemFolder(PROCESS_ATTACHMENTS_V,
            "views.processInstanceDetailsView.processDocumentTree.processAttachment"));
      systemFolders_virtual.add(new SystemFolder(SPECIFIC_DOCUMENTS_V,
            "views.processInstanceDetailsView.processDocumentTree.coreDocuments"));
      systemFolders_virtual.add(new SystemFolder(ARTIFACTS_V, "views.genericRepositoryView.artifacts"));
      systemFolders_virtual
            .add(new SystemFolder(NOTES_V, "views.processInstanceDetailsView.processDocumentTree.notes"));
      systemFolders_virtual.add(new SystemFolder(PROCESS_MODELS,
            "views.genericRepositoryView.systemFolders.processModels"));
      systemFolders_virtual.add(new SystemFolder(RULES,
            "views.genericRepositoryView.systemFolders.rules"));
   }

   private static class SystemFolder
   {
      private static String PROPERTY_KEY_PREFIX = "views.genericRepositoryView.systemFolders.";
      private String path; // pattern or folder path
      private String bundleKey;

      SystemFolder(String path, String bundleKey)
      {
         this.path = path;
         this.bundleKey = bundleKey;
      }

      public String getPath()
      {
         return path;
      }

      public String getI18Name()
      {
         if (bundleKey.contains(".")) // bundle key is already qualified
         {
            return MessagesViewsCommonBean.getInstance().getString(bundleKey);
         }
         else
         {
            return MessagesViewsCommonBean.getInstance().getString(PROPERTY_KEY_PREFIX + bundleKey);
         }
      }
   }
   
   /**
    * @param path
    * @return
    */
   public static String getLabel(String path, String folderName)
   {
      String i18nFolderName = "";
      I18nFolderUtils i18nFolderUtils = I18nFolderUtils.getInstance();

      if (path.contains("/") || path.contains("\\")) // make sure it is not virtual folder
      {
         for (SystemFolder systemFolder : i18nFolderUtils.systemFolders)
         {
            if (path.matches(systemFolder.getPath()))
            {
               i18nFolderName = systemFolder.getI18Name();
               break;
            }
         }
         if (org.eclipse.stardust.common.StringUtils.isEmpty(i18nFolderName))
         {
            if (path.contains("/"))
            {
               // For CMIS repository, path and folderName[label] are different
               // We can directly use the folderName as label
               i18nFolderName = folderName;
            }
            else
            {
               i18nFolderName = StringUtils.substringAfterLast(path, "\\");
            }
            for (SystemFolder systemFolder : i18nFolderUtils.systemFolders_virtual)
            {
               if (i18nFolderName.matches(systemFolder.getPath()))
               {
                  i18nFolderName = systemFolder.getI18Name();
                  break;
               }
            }
         }
      }
      else
      // virtual folder
      {
         for (SystemFolder systemFolder : i18nFolderUtils.systemFolders_virtual)
         {
            if (path.equalsIgnoreCase(systemFolder.getPath()))
            {
               i18nFolderName = systemFolder.getI18Name();
               break;
            }
         }
         if (org.eclipse.stardust.common.StringUtils.isEmpty(i18nFolderName))
         {
            // For CMIS repository, path and folderName[label] are different
            // We can directly use the folderName as label
            i18nFolderName = folderName;
         }
      }

      return i18nFolderName;
   }

}