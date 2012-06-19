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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EventObject;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;


import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * 
 * @author Vikas.Mishra
 * PopUp bean class to import Configuration variables 
 */
public class ConfigurationImportDialogBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "configurationImportDialogBean";

   private FileInfo currentFile;
   private int fileProgress;
   private MessagesViewsCommonBean messageBean;
   public ConfigurationImportDialogBean()
   {
      messageBean=MessagesViewsCommonBean.getInstance();
   }

   public FileInfo getCurrentFile()
   {
      return currentFile;
   }

   public int getFileProgress()
   {
      return fileProgress;
   }

   /**
    * @return
    */
   public static ConfigurationImportDialogBean getCurrent()
   {
      return (ConfigurationImportDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);

   }

   /**
    * method create Resource object if type is Export
    */
   @Override
   public void initialize()
   {

   }

   /**
    * <p>
    * Action event method which is triggered when a user clicks on the upload preferences
    * zip file.
    * </p>
    * 
    * @param event
    *           jsf action event.
    */
   public void uploadFile(ActionEvent event)
   {

      InputFile inputFile = (InputFile) event.getSource();
      if (!inputFile.getFileInfo().getPhysicalPath().endsWith(FileUtils.ZIP_FILE))
      {
         MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("views.configurationImportDialog.invalidFileFormat.title"), messageBean.getString("views.configurationImportDialog.invalidFileFormat") );
         return ;
      }
      currentFile = inputFile.getFileInfo();

   }

   /**
    * <p>
    * This progress information can then be used with a progressBar component for
    * preference file upload progress.
    * </p>
    * 
    * @param event
    *           holds a InputFile object in its source which can be probed for the file
    *           upload percentage complete.
    */

   public void progressListener(EventObject event)
   {
      InputFile ifile = (InputFile) event.getSource();
      fileProgress = ifile.getFileInfo().getPercent();
   }

   public void importPreferences()
   {
      String filePath = currentFile.getPhysicalPath();
      InputStream inputStream = null;

      try
      {
         if (filePath.endsWith(FileUtils.ZIP_FILE))
         {
            inputStream = new FileInputStream(currentFile.getFile());
            ServiceFactory serviceFactory = SessionContext.findSessionContext().getServiceFactory();
            PreferenceStoreUtils.loadFromZipFile(inputStream, serviceFactory);

         }
         else
         {
           
           MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("views.configurationImportDialog.invalidFileFormat.title"), messageBean.getString("views.configurationImportDialog.invalidFileFormat") );
         }

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         FileUtils.close(inputStream);
      }

      closePopup();

   }

   @Override
   public void closePopup()
   {
      currentFile = null;
      fileProgress = 0;
      super.closePopup();
   }

}
