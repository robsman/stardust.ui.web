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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.MySignaturePreferenceUtils;


import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * @author Aditya.Gaikwad
 * 
 */
public class MySignaturePreferenceBean
{
   private static final String [] VALID_IMAGE_FILE_EXTENSIONS = new String [] {".JPEG", ".JPG", ".PNG", ".GIF"};
   private final Logger logger = LogManager.getLogger(MySignaturePreferenceBean.class);
   private String savedSignaturePreference;
   private String currentSignaturePreference;
   private byte[] savedImage;
   private byte[] currentImage;
   private User user;
   private MessagesViewsCommonBean messageBean;
   private String validationMsg;

   /**
    * @param user
    *           - the logged in user.
    */
   public MySignaturePreferenceBean(User user)
   {
      this.user = user;
      initialize();
   }

   /**
    * Reinstates the My signature tab with the saved settings.
    */
   public void reset()
   {
      validationMsg = null;
      currentSignaturePreference = savedSignaturePreference;
      currentImage = savedImage;
   }

   /**
    * Uploads the user selected image, reads and holds the images bytes in memory and
    * deletes the uploaded file from fileSystem.
    * 
    * @param event
    */
   public void uploadActionListener(ActionEvent event)
   {
      validationMsg = null;
      FileInfo fileInfo = ((InputFile) event.getSource()).getFileInfo();
      String filePath = fileInfo.getPhysicalPath();

      if (hasValidFileExtension(filePath, event))
      {
         try
         {
            currentImage = FileUtils.fileToBytes(filePath);

            /*
             * Delete the file from file system as it's already loaded in memory and
             * needed temporarily.
             */
            if (!FileUtils.deleteFile(filePath))
            {
               logger.warn("Uploaded file could not be deleted from path " + filePath);
            }
         }
         catch (IOException e)
         {
            logger.error("Exception while trying to load image: " + e.getMessage(), e);
         }
      }
   }

   /**
    * Saves the user selected image and the user preference and URL (if present) in the
    * JCR.
    */
   public void save()
   {
      cleanOldPreferences(savedSignaturePreference);
      MySignaturePreferenceUtils.savePreference(UserPreferencesEntries.F_MY_SIGNATURE_TYPE, currentSignaturePreference);
      savedSignaturePreference = currentSignaturePreference;
      if (MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(currentSignaturePreference))
      {
         MySignaturePreferenceUtils.saveImage(user, currentImage);
         savedImage = currentImage;
      }
      else
      {
         savedImage = currentImage = null;
      }
   }   

   /**
    * @return
    */
   public boolean isSelectedImageValid()
   {
      if (MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(currentSignaturePreference)
            && (null == currentImage))
      {
         validationMsg = messageBean.getString("views.userProfile.myPicture.savedWithoutImageExceptionMessage");
         return false;
      }

      return true;
   }

   /**
    * 
    * @param actionEvent
    */
   public void selectionChangeListener(ValueChangeEvent actionEvent)
   {
      validationMsg = null;
   }

   /**
    * @return boolean indicating whether 'My computer' is selected or not.
    */
   public boolean isMyComputerSelected()
   {
      return (currentSignaturePreference != null && MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER
            .equals(currentSignaturePreference));
   }

   /**
    * @return boolean indicating whether 'No signature' is selected or not.
    */
   public boolean isNoPictureSelected()
   {
      return (currentSignaturePreference == null || MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_NO_PICTURE
            .equals(currentSignaturePreference));
   }

   /**
    * @return current signature preference.
    */
   public String getcurrentSignaturePreference()
   {
      return currentSignaturePreference;
   }

   /**
    * @param signaturePreference
    */
   public void setcurrentSignaturePreference(String signaturePreference)
   {
      currentSignaturePreference = signaturePreference;
   }

   /**
    * @return byte array of currently selected image.
    */
   public byte[] getCurrentImage()
   {
      if (null == currentImage)
      {
         return new byte[] {};
      }
      return currentImage;
   }

   /**
    * @param downloadedImage
    */
   public void setCurrentImage(byte[] downloadedImage)
   {
      currentImage = downloadedImage;
   }

   /**
    * @return if the currentImage fields is empty or not.
    */
   public boolean isCurrentImageNotEmpty()
   {
      return (currentImage != null);
   }

   /**
    * @return
    */
   public String getValidationMsg() {
      return validationMsg;
   }
   
   /**
    * Initializes the 'My Signature' tab view. Sets retrieves the signature preference, URL
    * and Image from JCR and sets the current values.
    */
   private void initialize()
   {
      messageBean = MessagesViewsCommonBean.getInstance();
      currentSignaturePreference = savedSignaturePreference = MySignaturePreferenceUtils.getLoggedInUsersSignaturePreference();
     if (MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(currentSignaturePreference))
      {
         currentImage = savedImage = MySignaturePreferenceUtils.getUserSignatureImage(user);
      }
   }
   
   /**
    * Checks if the file extension is of a valid type.
    * 
    * @param filePath
    * @param event
    * @return
    */
   private boolean hasValidFileExtension(final String filePath, final ActionEvent event)
   {
      if (null != filePath)
      {
         String extn = filePath.substring(filePath.lastIndexOf("."));
         for (String ex : VALID_IMAGE_FILE_EXTENSIONS)
         {
            if (ex.equalsIgnoreCase(extn))
            {
               return true;
            }
         }
      }
      /* If not a valid extension display an error message. */
      FacesMessage message = new FacesMessage();
      message.setSeverity(FacesMessage.SEVERITY_ERROR);
      message.setSummary(messageBean.getString("views.userProfile.myPicture.invalidFileExtn"));
      message.setDetail(messageBean.getString("views.userProfile.myPicture.invalidFileExtn"));
      FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(FacesContext.getCurrentInstance()),
            message);

      return false;
   }

   /**
    * 
    */
   private void cleanOldPreferences(String oldPreference)
   {
      if (MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(oldPreference)
            && !MySignaturePreferenceUtils.F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(currentSignaturePreference))
      {
         MySignaturePreferenceUtils.deleteUserSignatureImage(user);
      }
   }
}
