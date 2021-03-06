package org.eclipse.stardust.ui.web.rules_manager.upload;

import java.util.EventObject;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.rules_manager.service.RulesManagementService;
import org.eclipse.stardust.ui.web.rules_manager.store.RulesManagementStrategy;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

public class RulesFileUploadDialog extends PopupUIComponentBean
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
            RulesManagementService rulesService = (RulesManagementService) FacesUtils.getBeanFromContext("rulesManagementService");
            Document ruleSet = rulesService.getRulesManagementStrategy()
                  .getRuleSetByName(fileInfo.getFileName());
            if (null == ruleSet)
            {
               rulesService.getRulesManagementStrategy().createRuleSet(
                     fileInfo.getFileName(),
                     FileUtils.fileToBytes(fileInfo.getPhysicalPath()));
			   reloadRulesAndClosePopup();
            }
            else
            {
               uploadMode = false;
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
            RulesManagementService rulesService = (RulesManagementService) FacesUtils.getBeanFromContext("rulesManagementService");
            Document ruleSet = rulesService.getRulesManagementStrategy().getRuleSetByName(fileInfo.getFileName());
            if (null != ruleSet)
            {
               rulesService.getRulesManagementStrategy().saveRuleSet(ruleSet.getId(),
                     FileUtils.fileToBytes(fileInfo.getPhysicalPath()));
            }
            
            reloadRulesAndClosePopup();
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
   private void reloadRulesAndClosePopup()
   {
      PortalApplication.getInstance().addEventScript(
            "window.parent.EventHub.events.publish('RELOAD_RULES');");

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
