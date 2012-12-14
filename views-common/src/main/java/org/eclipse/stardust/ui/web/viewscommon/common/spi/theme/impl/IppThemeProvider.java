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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.theme.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.repository.IRepositoryManager;
import org.eclipse.stardust.engine.core.repository.RepositorySpaceKey;
import org.eclipse.stardust.ui.client.util.PluginResourceUtils;
import org.eclipse.stardust.ui.web.common.spi.theme.Theme;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.login.dialogs.LoginDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;




/**
 * @author Subodh.Godbole
 *
 */
public class IppThemeProvider implements ThemeProvider
{
   private static final long serialVersionUID = 1L;

   public static final String THEME_SERVLET_PATH = "/skins/";
   
   private static List<Theme> availableThemes;
   
   private String themeId;
   private String loginStyleSheet;
   private List<String> themeStyleSheets;
   private Map<String, List<String>> pluginAvailableSkins;

   /**
    * 
    */
   public IppThemeProvider()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider#loadTheme(java.lang.String
    * )
    */
   public void loadTheme(String themeId)
   {
      this.themeId = themeId;
      availableThemes = new ArrayList<Theme>();
      themeStyleSheets = new ArrayList<String>();
      availableThemes.add(new IppTheme("", MessagePropertiesBean.getInstance().getString(
            "views.configurationPanel.skins.defaultSkin")));

      Set<Theme> availableThemesSet = CollectionUtils.newHashSet();
      
      availableThemesSet.addAll(bootstrapThemes());
      availableThemesSet.addAll(bootstrapPluginThemes());

      availableThemes.addAll(availableThemesSet);
      
      loginStyleSheet = Parameters.instance().getString(LoginDialogBean.LOGIN_SKIN_CSS_PARAM,
            LoginDialogBean.DEFAULT_LOGIN_SKIN_CSS_NAME);
      
      loadThemeStyleSheets();
      loadPluginThemeStyleSheets();
   }

   /**
    * 
    */
   private Set<Theme> bootstrapThemes()
   {
      Set<Theme> availableJCRThemes = new HashSet<Theme>();
      
      List<Folder> skins = getSkinsFolders();

      for (Folder skinFolder : skins)
      {
         availableJCRThemes.add(new IppTheme(skinFolder.getId(), skinFolder.getName()));
      }
      return availableJCRThemes;
   }
   
   /**
    * Load the plugin themes from /public/skins folder
    */
   private Set<Theme> bootstrapPluginThemes()
   {
      Set<Theme> availablePluginThemes = new HashSet<Theme>();

      pluginAvailableSkins = PluginResourceUtils.findPluginSkins(Constants.PLUGIN_FOLDER_PATH, null);

      for (Map.Entry<String, List<String>> entry : pluginAvailableSkins.entrySet())
      {
         String key = entry.getKey();
            availablePluginThemes.add(new IppTheme(key, key.substring(key.lastIndexOf("/")+1)));
      }
     return availablePluginThemes;
   }

   /**
    * @throws IOException 
    * 
    */
   private void loadThemeStyleSheets() 
   {
      String skinFolderId = themeId;
      List<String> jcrStyleSheets = new ArrayList<String>();

      if (StringUtils.isNotEmpty(skinFolderId))
      {
         Folder skinRoot = getSkinsRootFolder();
         if (null != skinRoot)
         {
            List<Folder> allSkinFolders = getSkinsFolders();
            for (Folder skinFolder : allSkinFolders)
            {
               if (skinFolder.getId().equals(skinFolderId))
               {
                  skinFolder = wrapForFetchingChildren(skinFolder);

                  @SuppressWarnings("unchecked")
                  List<Document> documents = skinFolder.getDocuments();
                  for (Document skinFile : documents)
                  {
                     if (skinFile.getName().toLowerCase().endsWith(".css")
                           && !loginStyleSheet.equals(skinFile.getName()))
                     {
                        String path = skinFile.getPath();
                        path = path.replace(skinRoot.getPath() + "/", "");
                        jcrStyleSheets.add(THEME_SERVLET_PATH + path);
                     }
                  }

                  break;
               }
            }
            themeStyleSheets.addAll(jcrStyleSheets);
         }
      }
   }
   
   /**
    * 
    */
   private void loadPluginThemeStyleSheets()
   {
      String skinFolderId = themeId;
      List<String> pluginStyleSheets = new ArrayList<String>();

      if (StringUtils.isNotEmpty(skinFolderId))
      {
         Set<String> allSkinFolders = pluginAvailableSkins.keySet();
         for (String skinFolder : allSkinFolders)
         {
            if (skinFolder.equals(skinFolderId))
            {
               List<String> documents = pluginAvailableSkins.get(skinFolder);
               for (String skinFile : documents)
               {
                  if (skinFile.toLowerCase().endsWith(".css") && !loginStyleSheet.equals(skinFile))
                  {
                     String path = Constants.PLUGIN_ROOT_FOLDER_PATH + skinFolderId + "/"
                           + skinFile.substring(skinFile.lastIndexOf("\\") + 1);
                     pluginStyleSheets.add(path);
                  }
               }
               break;
            }
         }
         themeStyleSheets.addAll(pluginStyleSheets);
      }
   }

   /**
    * @return
    */
   private List<Folder> getSkinsFolders()
   {
      Folder skinsFolder = getSkinsRootFolder();
      if (skinsFolder != null)
      {
         @SuppressWarnings("unchecked")
         List<Folder> skins = skinsFolder.getFolders();
         return skins;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   /**
    * @return
    */
   private Folder getSkinsRootFolder()
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();

      IRepositoryManager repoManager = sessionCtx.getRepositoryManager();

      if (sessionCtx.isSessionInitialized())
      {
         if (sessionCtx.getUser().isAdministrator() || !DMSHelper.isSecurityEnabled())
         {
            return repoManager.getContentFolder(RepositorySpaceKey.SKINS, true);
         }
         else
         {
            return repoManager.getContentFolder(RepositorySpaceKey.SKINS, false);
         }
      }
      return null;
   }
   
    /**
     * @param folder
     */
    private Folder wrapForFetchingChildren(Folder folder)
    {
       SessionContext sessionCtx = SessionContext.findSessionContext();
       DocumentManagementService dms = sessionCtx.isSessionInitialized() ? 
             sessionCtx.getServiceFactory().getDocumentManagementService() : null;
       if(dms != null && (folder.getFolderCount() == 0 || folder.getDocumentCount() == 0))
       {
          folder = dms.getFolder(folder.getId(), Folder.LOD_LIST_MEMBERS);
       }
       return folder;
    }
    
    public List<String> getStyleSheets()
    {
       return themeStyleSheets;
    }

    public List<Theme> getThemes()
    {
       return availableThemes;
    }
}
