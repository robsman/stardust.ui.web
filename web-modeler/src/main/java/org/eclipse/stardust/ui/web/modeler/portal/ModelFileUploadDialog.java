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
package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.EventObject;

import javax.faces.event.ActionEvent;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

/**
 * @author Shrikant.Gangal
 *
 */
public class ModelFileUploadDialog extends PopupUIComponentBean
{
   private boolean uploadMode = true;

   private int fileUploadProgress;

   private FileUploadDialogAttributes attributes;

   private InputFile inputFile;

   private String uploadedFileName;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public void initialize()
   {
      attributes = new FileUploadDialogAttributes();
      // attributes.title =
      // MessagesViewsCommonBean.getInstance().getString("fileUpload.label");
      fileUploadProgress = 0;
   }

   /**
    * Gets invoked when icefaces completes the file upload to some temporary location
    *
    * @param event
    */
   public void uploadFile(ActionEvent event)
   {
      inputFile = (InputFile) event.getSource();
      FileInfo fileInfo = inputFile.getFileInfo();
      uploadedFileName = fileInfo.getFileName();
      try
      {
         if (fileInfo.isSaved())
         {
            ModelService modelService = (ModelService) FacesUtils.getBeanFromContext("modelService");
            ModelManagementStrategy.ModelUploadStatus status = modelService.getModelManagementStrategy()
                  .uploadModelFile(fileInfo.getFileName(),
                        FileUtils.fileToBytes(fileInfo.getPhysicalPath()), false);
            switch (status)
            {
            case MODEL_ALREADY_EXISTS:
               uploadMode = false;
               break;
            case NEW_MODEL_CREATED:
            case NEW_MODEL_VERSION_CREATED:
               reloadModelsAndClosePopup();
               break;
            }
         }
         else
         {
            switch (fileInfo.getStatus())
            {
            case FileInfo.UNSPECIFIED_NAME:
               break;
            default:
               closePopup();
               ExceptionHandler.handleException(
                     "commonFile" + getBeanId(),
                     MessagesViewsCommonBean.getInstance().getString(
                           "views.genericRepositoryView.fileUploadError"));
               break;
            }
         }
      }
      catch (Exception exception)
      {
         closePopup();
         ExceptionHandler.handleException(exception);
      }
   }

   /**
    *
    */
   public void createNewVersion(ActionEvent event)
   {
      FileInfo fileInfo = inputFile.getFileInfo();

      try
      {
         if (fileInfo.isSaved())
         {
            ModelService modelService = (ModelService) FacesUtils.getBeanFromContext("modelService");
            ModelManagementStrategy.ModelUploadStatus status = modelService.getModelManagementStrategy()
                  .uploadModelFile(fileInfo.getFileName(),
                        FileUtils.fileToBytes(fileInfo.getPhysicalPath()), true);
            reloadModelsAndClosePopup();
         }
         else
         {
            closePopup();
            switch (fileInfo.getStatus())
            {
            case FileInfo.UNSPECIFIED_NAME:
               ExceptionHandler.handleException(
                     "commonFile" + getBeanId(),
                     MessagesViewsCommonBean.getInstance().getString(
                           "views.genericRepositoryView.UNSPECIFIED_NAME"));
               break;
            default:
               ExceptionHandler.handleException(
                     "commonFile" + getBeanId(),
                     MessagesViewsCommonBean.getInstance().getString(
                           "views.genericRepositoryView.fileUploadError"));
               break;
            }
         }
      }
      catch (Exception exception)
      {
         ExceptionHandler.handleException(exception);
      }
   }

   /**
   *
   */
   public void closePopup()
   {
      uploadMode = true;
      fileUploadProgress = 0;
      inputFile = null;
      uploadedFileName = null;

      super.closePopup();
   }

   /**
    *
    */
   private void reloadModelsAndClosePopup()
   {
      PortalApplication.getInstance().addEventScript(
            "window.parent.EventHub.events.publish('RELOAD_MODELS');");

      PortalApplication.getInstance().addEventScript(
            "window.parent.EventHub.events.publish('CONTEXT_UPDATED');");

      closePopup();
   }

   /**
    * tracks the progress
    *
    * @param event
    */
   public void measureProgress(EventObject event)
   {
      InputFile file = (InputFile) event.getSource();
      fileUploadProgress = file.getFileInfo().getPercent();
   }

   public int getFileUploadProgress()
   {
      return fileUploadProgress;
   }

   public FileUploadDialogAttributes getAttributes()
   {
      return attributes;
   }

   public boolean isUploadMode()
   {
      return uploadMode;
   }

   public String getUploadedFileName()
   {
      return uploadedFileName;
   }
}
