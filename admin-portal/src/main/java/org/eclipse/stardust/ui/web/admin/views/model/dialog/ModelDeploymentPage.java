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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPage;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPageEvent;


import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ModelDeploymentPage extends WizardPage
{

   private static final int VERSIONIZED = 1;
   private static final int OVERWRITE = 2;
   private AdminMessagesPropertiesBean propsBean;
   private List<String> fileTypes;
   private List<ModelDeployTableEntry> modelList = new ArrayList<ModelDeployTableEntry>();
   private SortableTable<ModelDeployTableEntry> deployModelTable;
   private TreeTable treeTable;
   private final boolean overwrite;
   private final DeploymentOptions deploymentOptions;
   
   private  String modelName;
   private  String version;
   private  int modelOID;
   

   public ModelDeploymentPage(List<String> fileTypes, boolean overwrite, int modelOID, String modelName, String version)
   {
      super("DEPLOYMENT_PAGE","/plugins/admin-portal/views/model/_modelDeployPage.xhtml");
      this.modelName = modelName;
      this.version = version;
      this.modelOID = modelOID;
      this.fileTypes = fileTypes;
      this.overwrite = overwrite;
      propsBean = AdminMessagesPropertiesBean.getInstance();
      deploymentOptions = new DeploymentOptions();
      

      createModelDeployTable();
      initialize();
   }
   public ModelDeploymentPage(List<String> fileTypes,boolean overwrite)
   {
      super("DEPLOYMENT_PAGE","/plugins/admin-portal/views/model/_modelDeployPage.xhtml");
      this.fileTypes = fileTypes;
      this.overwrite = overwrite;
      propsBean = AdminMessagesPropertiesBean.getInstance();
      deploymentOptions = new DeploymentOptions();
      
      createModelDeployTable();
      initialize();
      
   }

  

   public SortableTable<ModelDeployTableEntry> getDeployModelTable()
   {
      return deployModelTable;
   }

   public DeploymentOptions getDeploymentOptions()
   {
      return deploymentOptions;
   }

   public String getModelName()
   {
      return modelName;
   }

   public int getModelOID()
   {
      return modelOID;
   }

   @Override
   public String getTitle()
   {
      if (overwrite)
      {
         return propsBean.getParamString("views.deploymodel.title.overwrite", new String[] {
               modelName, version, String.valueOf(modelOID)});
      }
      else
      {
         return propsBean.getString("views.deploymodel.title.newVersion");
      }
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public void handleEvent(ViewEvent event)
   {}

   /**
    * method used to initialize view specific data.
    */
   public void initialize()
   {
      deployModelTable.setList(modelList);
      deployModelTable.initialize();
   }

   public boolean isFileUploadEnable()
   {
      // if upload type is overwrite and one file is uploaded then return false
      if (overwrite && !modelList.isEmpty())
      {
         return false;
      }

      return true;
   }

   /**
    * method to check whether model file(s) uploaded or not
    * 
    * @return
    */
   public boolean isModelUploaded()
   {
      if (modelList.isEmpty())
      {
         return false;
      }

      return true;
   }

   /**
    * <p>
    * Allows a user to remove a file from a list of uploaded files. This methods assumes
    * that a request param "row" has been set to a ModelDeployTableEntry(row object) that
    * the user wishes to remove or delete
    * </p>
    * 
    * @see ModelDeployTableEntry
    * 
    * @param event
    *           jsf action event
    * 
    * 
    */
   public void removeUploadedFile(ActionEvent event)
   {
      ModelDeployTableEntry modelTableEntry = (ModelDeployTableEntry) event.getComponent().getAttributes().get("row");
      modelList.remove(modelTableEntry);
      initialize();
   }
   
   public void addModelToModelList(ModelDeployTableEntry modelTableEntry)
   {
      modelList.add(modelTableEntry);
   }

   public void setDeployModelTable(SortableTable<ModelDeployTableEntry> deployModelTable)
   {
      this.deployModelTable = deployModelTable;
   }

   /**
    * <p>
    * Action event method which is triggered when a user clicks on the upload file button.
    * Uploaded files are added to a list so that user have the option to delete them from
    * table. If user upload non supported file type then it will show error message Any
    * errors that occurs during the file uploaded are added the error messages.
    * </p>
    * 
    * @param event
    *           jsf action event.
    */
   public void uploadFile(ActionEvent event)
   {
      // AdministrationService administrationService =
      // workflowFacade.getServiceFactory().getAdministrationService();
      InputFile inputFile = (InputFile) event.getSource();
      FileInfo fileInfo = inputFile.getFileInfo();
      boolean canUploadFile = isVaildFileType(fileInfo.getFileName());

      if (canUploadFile)
      {
         String filePath = fileInfo.getPhysicalPath();

         try
         {
            if (filePath.toLowerCase().endsWith(FileUtils.ZIP_FILE))
            {
               List<String> files = FileUtils.getFileNamesFromZip(filePath);

               // if overwrite and uploaded file is zip file then file should contain only
               // one xpdl file.
               if (overwrite && (files.size() > 1))
               {
                  MessageDialog
                        .addMessage(MessageDialog.MessageType.ERROR,

                        propsBean.getString("views.deploymodel.invalidSourceFile"), propsBean.getParamString(
                              "views.deploymodel.zipFileCannotContainMoreThanOneFile", new String[] {fileInfo
                                    .getFileName()}));
                  inputFile = null;

                  return;
               }
               else
               {
                  // if uploaded file is zip file then file should not contain other then
                  // xpdl file.
                  for (String fileName : files)
                  {
                     if (!fileName.toLowerCase().endsWith(FileUtils.XPDL_FILE))
                     {
                        MessageDialog.addMessage(MessageDialog.MessageType.ERROR, propsBean
                              .getString("views.deploymodel.invalidSourceFile"), propsBean.getParamString(
                              "views.deploymodel.zipFileContainsInvalidFile", new String[] {
                                    fileInfo.getFileName(), fileName}));

                        return;
                     }
                  }

               }

               for (String fileName : files)
               {
                  ModelDeployTableEntry modelTableEntry = new ModelDeployTableEntry();
                  modelTableEntry.setFileName(fileName);
                  modelTableEntry.setFilePath(filePath);
                  modelTableEntry.setDeploymentAction(overwrite ? OVERWRITE : VERSIONIZED);
                  modelList.add(modelTableEntry);
               }
            }
            else
            {
               ModelDeployTableEntry modelTableEntry = new ModelDeployTableEntry();
               modelTableEntry.setFileName(fileInfo.getFileName());
               modelTableEntry.setFilePath(filePath);
               modelTableEntry.setDeploymentAction(overwrite ? OVERWRITE : VERSIONIZED);
               modelList.add(modelTableEntry);
            }

            initialize();
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else
      {
         MessageDialog.addMessage(MessageDialog.MessageType.ERROR,

         propsBean.getString("views.deploymodel.invalidSourceFile"), propsBean.getParamString(
               "views.deploymodel.canNotUploadFile", new String[] {fileInfo.getFileName()}));
         inputFile = null;
      }
   }

   /**
    * method to create table definition for model file
    */
   private void createModelDeployTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colFileName = new ColumnPreference("FileName", null, propsBean
            .getString("views.deployModelView.column.fileName"), ResourcePaths.V_DEPLOY_MODEL_DIALOG_COLUMNS, true,
            false);

      ColumnPreference colDeploymentAction = new ColumnPreference("DeploymentAction",
            null, // "deploymentActionMessage",
            propsBean.getString("views.deployModelView.column.deploymentAction"),
            ResourcePaths.V_DEPLOY_MODEL_DIALOG_COLUMNS, true, false);

      // add this to fixed column
      ColumnPreference colDelete = new ColumnPreference("Delete", null, propsBean
            .getString("views.deployModelView.column.delete"), ResourcePaths.V_DEPLOY_MODEL_DIALOG_COLUMNS, true, false);

      colDeploymentAction.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colFileName);
      cols.add(colDeploymentAction);
      cols.add(colDelete);

      IColumnModel deployModelColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_ADMIN,
            ResourcePaths.V_modelManagementView);

      deployModelTable = new SortableTable<ModelDeployTableEntry>(deployModelColumnModel, null,
            new SortableTableComparator<ModelDeployTableEntry>("fileName", true));
   }

   /**
    * method to check valid file type.
    * 
    * @param fileName
    * @return
    */
   private boolean isVaildFileType(String fileName)
   {
      boolean isValidFile = false;

      if (fileName != null)
      {
         final String lowerCaseNameExt = fileName.toLowerCase();

         if (fileTypes != null)
         {
            for (String fileType : fileTypes)
            {
               if (lowerCaseNameExt.endsWith(fileType))
               {
                  isValidFile = true;

                  break;
               }
            }
         }
      }

      return isValidFile;
   }

   public String getVersion()
   {
      return version;
   }

   public String getPageName()
   {    
      return null;
   }
   public void setModelName(String modelName)
   {
      this.modelName = modelName;
   }
   public void setVersion(String version)
   {
      this.version = version;
   }
   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }
   public void handleEvent(WizardPageEvent event)
   {     
     
   }
   public boolean isOverwrite()
   {
      return overwrite;
   }
   public List<ModelDeployTableEntry> getModelList()
   {
      return modelList;
   }  
   
}
