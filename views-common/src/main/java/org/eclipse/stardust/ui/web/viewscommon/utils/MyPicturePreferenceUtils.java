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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.net.URL;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryUtils;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.MD5Utils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;



/**
 * @author Shrikant.Gangal
 * 
 */
public class MyPicturePreferenceUtils
{
   private static final String AVATAR_BASE_URL = "http://www.gravatar.com/avatar/";
   private static final String AVATAR_PREFERENCE = "monsterid";
   private static final int AVATAR_IMAGE_SIZE = 128;
   private static final String DEFAULT_USER_IMAGE = "/plugins/views-common/images/icons/user-default.png";
   private static final String DMS_CONTENT_SERVLET_NAME = "/dms-content/";
   private static final String GENERIC_COMPONENT_SELECTION_KEY = "?.#."; // moduleId.viewId.<featureId>
   private static final String REFERENCE_ID = "preference";
   private static final String VALID_PROTOCOL_REGEX=  "^http(s{0,1})";

   public static final String F_MY_PICTURE_TYPE_NO_PICTURE = "NoPicture";
   public static final String F_MY_PICTURE_TYPE_MONSTER_ID = "MonsterID";
   public static final String F_MY_PICTURE_TYPE_HTTP_URL = "ImageURL";
   public static final String F_MY_PICTURE_TYPE_MY_COMPUTER = "MyComputer";
   public static final String PROFILE_IMAGE_FILE_NAME = "userImage.jpg";
   public static final String DEFAULT_HTTP_IMAGE_URL = "http://";
   public static final String PROFILE_IMAGE_FOLDER = "/profile";

   /**
    * Utility method to return the logged in user's saved picture preference.
    * User need not be passed to the method as UserPreferencesHelper doesn't need it and returns
    * details for the logged in user.
    * 
    * @return user's picture preference.
    */
   public static String getLoggedInUsersImagePreference()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      String picturePreference = userPrefsHelper.getSingleString(UserPreferencesEntries.V_MY_PICTURE,
            UserPreferencesEntries.F_MY_PICTURE_TYPE);

      picturePreference = (StringUtils.isEmpty(picturePreference)) ? F_MY_PICTURE_TYPE_NO_PICTURE : picturePreference;

      return picturePreference;
   }

   /**
    * Utility method to return the logged in User's profile Image URI.
    * Returns the HTTP URL if saved picture preference is 'Image URL' or 'Monster ID'
    * If the saved preference is 'My computer' then returns a URL to retrieve data from DMS.
    * If none of the above then returns the default URL.
    * If the user is null returns the default image.
    * 
    * @param user
    * @return 
    */
   public static String getLoggedInUsersImageURI()
   {
      String imageURI = DEFAULT_USER_IMAGE;
      SessionContext context = SessionContext.findSessionContext();
      User user = (context != null) ? context.getUser() : null;
      
      if (user != null)
      {
         String picturePreference = getLoggedInUsersImagePreference();

         if (F_MY_PICTURE_TYPE_HTTP_URL.equals(picturePreference)
               || F_MY_PICTURE_TYPE_MONSTER_ID.equals(picturePreference))
         {
            UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(
                  UserPreferencesEntries.M_VIEWS_COMMON, PreferenceScope.USER);
            imageURI = userPrefsHelper.getSingleString(UserPreferencesEntries.V_MY_PICTURE,
                  UserPreferencesEntries.F_MY_PICTURE_HTTP_URL);
         }
         else if (F_MY_PICTURE_TYPE_MY_COMPUTER.equals(picturePreference))
         {
            DocumentManagementService dms = getDocumentManagementService();
            Document doc = dms.getDocument(getUsersProfileImageFolderpath(user) + "/" + PROFILE_IMAGE_FILE_NAME);
            if (doc != null)
            {
               imageURI = DMS_CONTENT_SERVLET_NAME + dms.requestDocumentContentDownload(doc.getId());
            }
         }
      }

      return imageURI;
   }
   
   /**
    * Utility method to return the given User's profile Image URI.
    * (currently retrieves default image - dependency on CRNT-17864)
    * Returns the HTTP URL if saved picture preference is 'Image URL' or 'Monster ID'
    * If the saved preference is 'My computer' then returns a URL to retrieve data from DMS.
    * If none of the above then returns the default URL.
    * If the user is null returns the default image.
    * 
    * @param user
    * @return 
    */
   public static String getUsersImageURI(final User user)
   {
      String imageURI = DEFAULT_USER_IMAGE;

      if (user != null)
      {
         String picturePreference = (String) user
               .getAttribute(getPreferencesId(UserPreferencesEntries.F_MY_PICTURE_TYPE));
         if (null == picturePreference)
         {
            if (UserUtils.isLoggedInUser(user))
            {
               picturePreference = getLoggedInUsersImagePreference();
            }
            else
            {
               picturePreference = getPicturePreferenceForUser(user);
            }
         }
         if (F_MY_PICTURE_TYPE_HTTP_URL.equals(picturePreference)
               || F_MY_PICTURE_TYPE_MONSTER_ID.equals(picturePreference))
         {

            String prefURLStr = (String) user
                  .getAttribute(getPreferencesId(UserPreferencesEntries.F_MY_PICTURE_HTTP_URL));
            if (StringUtils.isNotEmpty(prefURLStr))
            {
               imageURI = prefURLStr;
            }
         }
         else if (F_MY_PICTURE_TYPE_MY_COMPUTER.equals(picturePreference))
         {
            DocumentManagementService dms = getDocumentManagementService();
            Document doc = dms.getDocument(getUsersProfileImageFolderpath(user) + "/" + PROFILE_IMAGE_FILE_NAME);
            if (doc != null)
            {
               imageURI = DMS_CONTENT_SERVLET_NAME + dms.requestDocumentContentDownload(doc.getId());
            }
         }
      }

      return imageURI;
   }

   public static String getPicturePreferenceForUser(final User user)
   {
      String picturePreference = null;
      QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = queryService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(user
            .getRealm().getId(), user.getId(), UserPreferencesEntries.M_VIEWS_COMMON, REFERENCE_ID));
      for (Preferences userPref : prefs)
      {
         picturePreference = (String) userPref.getPreferences().get(getPreferencesId(UserPreferencesEntries.F_MY_PICTURE_TYPE));
      }
      return picturePreference;
   }
   /**
    * Utility method to get the profile image's JCR folder path.
    * 
    * @param user
    * @return the JCR folder path.
    */
   public static String getUsersProfileImageFolderpath(final User user)
   {
      String userFolderPath = (new StringBuffer(DocumentRepositoryFolderNames.getRepositoryRootFolder()).append(
            DocumentRepositoryFolderNames.PARTITIONS_FOLDER).append(user.getPartitionId()).append("/").append(
            DocumentRepositoryFolderNames.REALMS_FOLDER).append(user.getRealm().getId()).append("/").append(
            DocumentRepositoryFolderNames.USERS_FOLDER).append(user.getAccount()).append(PROFILE_IMAGE_FOLDER))
            .toString();

      return userFolderPath;
   }

   /**
    * @return Retrieves and return the user image from JCR.
    */
   public static byte[] getUserProfileImage(final User user)
   {
      return getDocumentManagementService().retrieveDocumentContent(
            MyPicturePreferenceUtils.getUsersProfileImageFolderpath(user) + "/" + PROFILE_IMAGE_FILE_NAME);
   }

   /**
    * Utility method to retrieve the HTTP Image URL. This method returns the URL stored in JCR in case the
    * preference if 'Image URL' or 'Monster ID'. Else returns a default string 'http://'.
    * 
    * @param userPicturePreference
    * @return
    */
   public static String getHTTPImageURL(final String userPicturePreference)
   {
      String imageURL = DEFAULT_HTTP_IMAGE_URL;
      if (F_MY_PICTURE_TYPE_HTTP_URL.equals(userPicturePreference)
            || F_MY_PICTURE_TYPE_MONSTER_ID.equals(userPicturePreference))
      {
         UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
               PreferenceScope.USER);
         imageURL = userPrefsHelper.getSingleString(UserPreferencesEntries.V_MY_PICTURE,
               UserPreferencesEntries.F_MY_PICTURE_HTTP_URL);
      }

      return imageURL;
   }

   /**
    * Utility method to generate and returns the Gravtar URL for the given user.
    * 
    * @return
    */
   public static String getMonsterIdURL(final User user)
   {
      String url = AVATAR_BASE_URL + MD5Utils.computeMD5Hex(user.getAccount()) + "?s=" + AVATAR_IMAGE_SIZE + "&d="
      + AVATAR_PREFERENCE;

      return url;
   }
   
   /**
    * Saves the user's picture preference into JCR.
    */
   public static void savePreference(final String preferenceKey, final String preferenceValue)
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);

      userPrefsHelper.setString(UserPreferencesEntries.V_MY_PICTURE, preferenceKey, preferenceValue);
   }
   
   /**
    * Resets the user's picture preference into JCR.
    */
   public static void resetPreference(final String preferenceKey)
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);

      userPrefsHelper.resetValue(UserPreferencesEntries.V_MY_PICTURE, preferenceKey);      
   }
   
   /**
    * Saves the image (uploaded from PC by the user) into JCR. If an image already exists
    * then it updates it, else creates a new one. 
    * 
    */
   public static void saveImage(final User user, final byte[] currentImage)
   {
      DocumentManagementService dms = getDocumentManagementService();
      Document doc = dms.getDocument(getUsersProfileImageFolderpath(user) + "/" + PROFILE_IMAGE_FILE_NAME);

      if (currentImage != null)
      {
         if (doc != null)
         {
            dms.updateDocument(doc, currentImage, "", false, "", "", false);
         }
         else
         {
            String folderPath = getUsersProfileImageFolderpath(user);
            DocumentRepositoryUtils.getSubFolder(dms, folderPath);
            dms.createDocument(folderPath, DmsUtils.createDocumentInfo(PROFILE_IMAGE_FILE_NAME), currentImage, "");
         }
      }
      else
      {
         if (doc != null)
         {
            dms.removeDocument(doc.getId());
         }
      }
   }
   
   /**
    * Saves the image (uploaded from PC by the user) into JCR. If an image already exists
    * then it updates it, else creates a new one. 
    * 
    */
   public static void deleteUsersProfileImage(final User user)
   {
      DocumentManagementService dms = getDocumentManagementService();
      Document doc = dms.getDocument(getUsersProfileImageFolderpath(user) + "/" + PROFILE_IMAGE_FILE_NAME);

      if (doc != null)
      {
         dms.removeDocument(doc.getId());
      }
   }
	
	/**
    * @param url
    * @return
    */
   public static boolean isURLValid(String url)
   {      
      try
      {
         URL u = new URL(url);
         // getPort() & getHost() call is made only to check if the host and port are valid.
         // If they are not then an exception is thrown
         u.getPort();         
         u.getHost();
         if (!u.getProtocol().matches(VALID_PROTOCOL_REGEX))
         {
            return false;
         }
      }
      catch(Exception e)
      {
         return false;
      }     

      return true;
   }

   /**
    * @return an implementation of DocumentManagementService
    */
   private static DocumentManagementService getDocumentManagementService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getDocumentManagementService();
   }
   
	/**
    * @param key
    * @return
    */
   private static String getPreferencesId(String key)
   {
      String str = StringUtils.replace(GENERIC_COMPONENT_SELECTION_KEY + key, "?",
            UserPreferencesEntries.M_VIEWS_COMMON);
      str = StringUtils.replace(str, "#", UserPreferencesEntries.V_MY_PICTURE);

      return str;
   }
}
