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
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;



/**
 * @author Aditya.Gaikwad
 * 
 */
public class MySignaturePreferenceUtils
{
   private static final String DMS_CONTENT_SERVLET_NAME = "/dms-content/";
   private static final String GENERIC_COMPONENT_SELECTION_KEY = "?.#."; // moduleId.viewId.<featureId>
   private static final String REFERENCE_ID = "preference";

   public static final String F_MY_SIGNATURE_TYPE_NO_PICTURE = "NoSignature";
   public static final String F_MY_SIGNATURE_TYPE_MY_COMPUTER = "MyComputer";
   public static final String SIGNATURE_IMAGE_FILE_NAME = "userSignature.jpg";
   public static final String DEFAULT_HTTP_IMAGE_URL = "http://";
   public static final String SIGNATURE_IMAGE_FOLDER = "/signature";

   /**
    * Utility method to return the logged in user's saved signature preference.
    * User need not be passed to the method as UserPreferencesHelper doesn't need it and returns
    * details for the logged in user.
    * 
    * @return user's signature preference.
    */
   public static String getLoggedInUsersSignaturePreference()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      String signaturePreference = userPrefsHelper.getSingleString(UserPreferencesEntries.V_MY_PICTURE,
            UserPreferencesEntries.F_MY_SIGNATURE_TYPE);

      signaturePreference = (StringUtils.isEmpty(signaturePreference)) ? F_MY_SIGNATURE_TYPE_NO_PICTURE : signaturePreference;

      return signaturePreference;
   }

   /**
    * Utility method to return the logged in User's signature Image URI.
    * If the saved preference is 'My computer' then returns a URL to retrieve data from DMS.
    * If none of the above then returns the default URL.
    * If the user is null returns the default image.
    * 
    * @param user
    * @return 
    */
   public static String getLoggedInUserSignatureURI()
   {
      String imageURI = "";
      SessionContext context = SessionContext.findSessionContext();
      User user = (context != null) ? context.getUser() : null;
      
      if (user != null)
      {
         String signaturePreference = getLoggedInUsersSignaturePreference();

         if (F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(signaturePreference))
         {
            DocumentManagementService dms = getDocumentManagementService();
            Document doc = dms.getDocument(getUserSignatureImageFolderpath(user) + "/" + SIGNATURE_IMAGE_FILE_NAME);
            if (doc != null)
            {
               imageURI = DMS_CONTENT_SERVLET_NAME + dms.requestDocumentContentDownload(doc.getId());
            }
         }
      }

      return imageURI;
   }
   
   /**
    * Utility method to return the given User's signature Image URI.
    * (currently retrieves default image - dependency on CRNT-17864)
    * If the saved preference is 'My computer' then returns a URL to retrieve data from DMS.
    * If none then returns the default URL.
    * If the user is null returns the default image.
    * 
    * @param user
    * @return 
    */
   public static String getUsersSignatureURI(final User user)
   {
      String imageURI = "";

      if (user != null)
      {
         String signaturePreference = (String) user
               .getAttribute(getPreferencesId(UserPreferencesEntries.F_MY_SIGNATURE_TYPE));
         if (null == signaturePreference)
         {
            if (UserUtils.isLoggedInUser(user))
            {
               signaturePreference = getLoggedInUsersSignaturePreference();
            }
            else
            {
               signaturePreference = getSignaturePreferenceForUser(user);
            }
         }
         else if (F_MY_SIGNATURE_TYPE_MY_COMPUTER.equals(signaturePreference))
         {
            DocumentManagementService dms = getDocumentManagementService();
            Document doc = dms.getDocument(getUserSignatureImageFolderpath(user) + "/" + SIGNATURE_IMAGE_FILE_NAME);
            if (doc != null)
            {
               imageURI = DMS_CONTENT_SERVLET_NAME + dms.requestDocumentContentDownload(doc.getId());
            }
         }
      }

      return imageURI;
   }

   public static String getSignaturePreferenceForUser(final User user)
   {
      String signaturePreference = null;
      QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = queryService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(user
            .getRealm().getId(), user.getId(), UserPreferencesEntries.M_VIEWS_COMMON, REFERENCE_ID));
      for (Preferences userPref : prefs)
      {
         signaturePreference = (String) userPref.getPreferences().get(getPreferencesId(UserPreferencesEntries.F_MY_SIGNATURE_TYPE));
      }
      return signaturePreference;
   }
   /**
    * Utility method to get the signature image's JCR folder path.
    * 
    * @param user
    * @return the JCR folder path.
    */
   public static String getUserSignatureImageFolderpath(final User user)
   {
      String userFolderPath = (new StringBuffer(DocumentRepositoryFolderNames.getRepositoryRootFolder()).append(
            DocumentRepositoryFolderNames.PARTITIONS_FOLDER).append(user.getPartitionId()).append("/").append(
            DocumentRepositoryFolderNames.REALMS_FOLDER).append(user.getRealm().getId()).append("/").append(
            DocumentRepositoryFolderNames.USERS_FOLDER).append(user.getAccount()).append(SIGNATURE_IMAGE_FOLDER))
            .toString();

      return userFolderPath;
   }

   /**
    * @return Retrieves and return the user image from JCR.
    */
   public static byte[] getUserSignatureImage(final User user)
   {
      return getDocumentManagementService().retrieveDocumentContent(
            MySignaturePreferenceUtils.getUserSignatureImageFolderpath(user) + "/" + SIGNATURE_IMAGE_FILE_NAME);
   }

   /**
    * Saves the user's signature preference into JCR.
    */
   public static void savePreference(final String preferenceKey, final String preferenceValue)
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);

      userPrefsHelper.setString(UserPreferencesEntries.V_MY_PICTURE, preferenceKey, preferenceValue);
   }
   
   /**
    * Resets the user's signature preference into JCR.
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
      Document doc = dms.getDocument(getUserSignatureImageFolderpath(user) + "/" + SIGNATURE_IMAGE_FILE_NAME);

      if (currentImage != null)
      {
         if (doc != null)
         {
            dms.updateDocument(doc, currentImage, "", false, "", "", false);
         }
         else
         {
            String folderPath = getUserSignatureImageFolderpath(user);
            DocumentRepositoryUtils.getSubFolder(dms, folderPath);
            dms.createDocument(folderPath, DmsUtils.createDocumentInfo(SIGNATURE_IMAGE_FILE_NAME), currentImage, "");
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
   public static void deleteUserSignatureImage(final User user)
   {
      DocumentManagementService dms = getDocumentManagementService();
      Document doc = dms.getDocument(getUserSignatureImageFolderpath(user) + "/" + SIGNATURE_IMAGE_FILE_NAME);

      if (doc != null)
      {
         dms.removeDocument(doc.getId());
      }
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
