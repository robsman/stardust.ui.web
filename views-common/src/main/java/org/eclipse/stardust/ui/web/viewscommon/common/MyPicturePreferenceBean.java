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
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;


import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * @author Shrikant.Gangal
 * 
 */
public class MyPicturePreferenceBean
{
   private static final String [] VALID_IMAGE_FILE_EXTENSIONS = new String [] {".JPEG", ".JPG", ".PNG", ".GIF"};
   private final Logger logger = LogManager.getLogger(MyPicturePreferenceBean.class);
   private String savedHTTPImageURL = MyPicturePreferenceUtils.DEFAULT_HTTP_IMAGE_URL;
   private String currentHTTPImageURL = MyPicturePreferenceUtils.DEFAULT_HTTP_IMAGE_URL;
   private String savedMonsterImageURL;
   private String currentMonsterImageURL;
   private String savedPicturePreference;
   private String currentPicturePreference;
   private byte[] savedImage;
   private byte[] currentImage;
   private User user;
   private MessagesViewsCommonBean messageBean;
   private String validationMsg;

   /**
    * @param user
    *           - the logged in user.
    */
   public MyPicturePreferenceBean(User user)
   {
      this.user = user;
      initialize();
   }

   /**
    * Reinstates the My picture tab with the saved settings.
    */
   public void reset()
   {
      validationMsg = null;
      currentPicturePreference = savedPicturePreference;
      currentHTTPImageURL = savedHTTPImageURL;
      currentMonsterImageURL = savedMonsterImageURL;
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
      cleanOldPreferences(savedPicturePreference);
      MyPicturePreferenceUtils.savePreference(UserPreferencesEntries.F_MY_PICTURE_TYPE, currentPicturePreference);
      savedPicturePreference = currentPicturePreference;
      if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER.equals(currentPicturePreference))
      {
         MyPicturePreferenceUtils.saveImage(user, currentImage);
         savedImage = currentImage;
         savedHTTPImageURL = currentHTTPImageURL = MyPicturePreferenceUtils.DEFAULT_HTTP_IMAGE_URL;
         savedMonsterImageURL = currentMonsterImageURL = null;
      }
      else if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL.equals(currentPicturePreference))
      {
         MyPicturePreferenceUtils.savePreference(UserPreferencesEntries.F_MY_PICTURE_HTTP_URL, currentHTTPImageURL);
         savedHTTPImageURL = currentHTTPImageURL;
         savedMonsterImageURL = currentMonsterImageURL = null;
         savedImage = currentImage = null;

      }
      else if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID.equals(currentPicturePreference))
      {
         MyPicturePreferenceUtils.savePreference(UserPreferencesEntries.F_MY_PICTURE_HTTP_URL, currentMonsterImageURL);
         savedMonsterImageURL = currentMonsterImageURL;
         savedHTTPImageURL = currentHTTPImageURL = MyPicturePreferenceUtils.DEFAULT_HTTP_IMAGE_URL;
         savedImage = currentImage = null;
      }
      else
      {
         savedHTTPImageURL = currentHTTPImageURL = MyPicturePreferenceUtils.DEFAULT_HTTP_IMAGE_URL;
         savedMonsterImageURL = currentMonsterImageURL = null;
         savedImage = currentImage = null;
      }
   }   

   /**
    * @return
    */
   public boolean isSelectedImageValid()
   {
      if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL.equals(currentPicturePreference)
            && !MyPicturePreferenceUtils.isURLValid(currentHTTPImageURL))
      {
         validationMsg = messageBean.getString("views.userProfile.myPicture.savedWithoutImageExceptionMessage");
         return false;
      }
      else if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER.equals(currentPicturePreference)
            && (null == currentImage))
      {
         validationMsg = messageBean.getString("views.userProfile.myPicture.savedWithoutImageExceptionMessage");
         return false;
      }

      return true;
   }

   /**
    * Listens for selection change of radio buttons. In case of a change to 'Monster Id',
    * sets the current image to the derived monster id URL.
    * 
    * @param actionEvent
    */
   public void selectionChangeListener(ValueChangeEvent actionEvent)
   {
      validationMsg = null;
      if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID.equals(actionEvent.getNewValue()))
      {
         if (currentMonsterImageURL == null)
         {
            currentMonsterImageURL = MyPicturePreferenceUtils.getMonsterIdURL(user);
         }
      }
   }

   /**
    * @return boolean indicating whether the current preference is 'Image URL' or not.
    */
   public boolean isImageURLSelected()
   {
      return (currentPicturePreference != null && MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL
            .equals(currentPicturePreference));
   }

   /**
    * @return boolean indicating whether 'My computer' is selected or not.
    */
   public boolean isMyComputerSelected()
   {
      return (currentPicturePreference != null && MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER
            .equals(currentPicturePreference));
   }

   /**
    * @return boolean indicating whether 'No picture' is selected or not.
    */
   public boolean isNoPictureSelected()
   {
      return (currentPicturePreference == null || MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_NO_PICTURE
            .equals(currentPicturePreference));
   }

   /**
    * @return
    */
   public boolean isMonsterIdSelected()
   {

      return (currentPicturePreference != null && MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID
            .equals(currentPicturePreference));
   }

   /**
    * @return current picture preference.
    */
   public String getCurrentPicturePreference()
   {
      return currentPicturePreference;
   }

   /**
    * @param picturePreference
    */
   public void setCurrentPicturePreference(String picturePreference)
   {
      currentPicturePreference = picturePreference;
   }

   /**
    * @return current image URL.
    */
   public String getCurrentHTTPImageURL()
   {
      return currentHTTPImageURL;
   }

   /**
    * @param profileImageURL
    */
   public void setCurrentHTTPImageURL(String profileImageURL)
   {
      validationMsg = null;
      currentHTTPImageURL = profileImageURL;
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
    * @return
    */
   public String getCurrentMonsterImageURL()
   {
      return currentMonsterImageURL;
   }

   /**
    * @param currentMonsterImageURL
    */
   public void setCurrentMonsterImageURL(String currentMonsterImageURL)
   {
      this.currentMonsterImageURL = currentMonsterImageURL;
   }
   
   /**
    * Initializes the 'My picture' tab view. Sets retrieves the picture preference, URL
    * and Image from JCR and sets the current values.
    */
   private void initialize()
   {
      messageBean = MessagesViewsCommonBean.getInstance();
      currentPicturePreference = savedPicturePreference = MyPicturePreferenceUtils.getLoggedInUsersImagePreference();
      if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL.equals(currentPicturePreference))
      {
         currentHTTPImageURL = savedHTTPImageURL = MyPicturePreferenceUtils.getHTTPImageURL(savedPicturePreference);
      }
      else if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID.equals(currentPicturePreference))
      {
         currentMonsterImageURL = savedMonsterImageURL = MyPicturePreferenceUtils
               .getHTTPImageURL(savedPicturePreference);
      }
      else if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER.equals(currentPicturePreference))
      {
         currentImage = savedImage = MyPicturePreferenceUtils.getUserProfileImage(user);
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
      if (MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER.equals(oldPreference)
            && !MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MY_COMPUTER.equals(currentPicturePreference))
      {
         MyPicturePreferenceUtils.deleteUsersProfileImage(user);
      }
      else if ((MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL.equals(oldPreference) || MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID
            .equals(oldPreference))
            && !(MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_HTTP_URL.equals(currentPicturePreference) || MyPicturePreferenceUtils.F_MY_PICTURE_TYPE_MONSTER_ID
                  .equals(currentPicturePreference)))
      {
         MyPicturePreferenceUtils.resetPreference(UserPreferencesEntries.F_MY_PICTURE_HTTP_URL);
      }
   }
}
