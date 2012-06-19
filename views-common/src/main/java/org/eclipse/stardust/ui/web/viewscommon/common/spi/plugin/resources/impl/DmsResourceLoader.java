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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.plugin.resources.impl;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.repository.AbstractDocumentServiceRepositoryManager;
import org.eclipse.stardust.engine.core.repository.IRepositoryContentProvider;
import org.eclipse.stardust.engine.core.repository.RepositorySpaceKey;
import org.eclipse.stardust.ui.web.plugin.support.resources.ClassLoaderResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.ResourceLoader;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class DmsResourceLoader extends ClassLoaderResourceLoader 
{
   private static String URL_PREFIX_SKINS = "/" + RepositorySpaceKey.SKINS.getId() + "/";
   
   protected static final ThreadLocal<SessionContext> SESSION_CONTEXT_HOLDER = new ThreadLocal<SessionContext>();
   
   /**
    * Use a separate repository manager per partition, to have separate resource caches
    */
   private final Map<String, DmsContentRepositoryManager> repositoryManagerCache = new ConcurrentHashMap<String, DmsContentRepositoryManager>();
   
   public static boolean isDmsResource(String requestedPath)
   {
      // TODO add more resource types
      return isSkinResource(requestedPath);
   }

   public static boolean isSkinResource(String requestedPath)
   {
      return !isEmpty(requestedPath) && requestedPath.startsWith(URL_PREFIX_SKINS);
   }

   public DmsResourceLoader(ResourceLoader parent)
   {
      super(parent);
   }
   
   public URL getResource(HttpServletRequest request, String name) throws IOException
   {
      URL resourceUrl = null;

      if (Parameters.instance().getBoolean(
            IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false))
      {
         HttpSession httpSession = request.getSession(false);
         if (null != httpSession)
         {
            SessionContext sessionContext = (SessionContext) httpSession.getAttribute(SessionContext.BEAN_ID);
            if ((null != sessionContext) && sessionContext.isSessionInitialized())
            {
               if (isSkinResource(name))
               {
                  String partitionId = sessionContext.getUser().getPartitionId();

                  DmsContentRepositoryManager dmsContentManager = repositoryManagerCache.get(partitionId);
                  if (null == dmsContentManager)
                  {
                     dmsContentManager = new DmsContentRepositoryManager(partitionId);
                     repositoryManagerCache.put(partitionId, dmsContentManager);
                  }

                  // bind session context to thread
                  SessionContext sessionContextBackup = SESSION_CONTEXT_HOLDER.get();
                  try
                  {
                     SESSION_CONTEXT_HOLDER.set(sessionContext);
                     
                     // resolve resource, caching will be done inside the content provider
                     IRepositoryContentProvider contentProvider = dmsContentManager.getContentProvider(RepositorySpaceKey.SKINS);
                     resourceUrl = contentProvider.getContentUrl(name.substring(URL_PREFIX_SKINS.length()));
                  }
                  finally
                  {
                     // restore outer state
                     if (null != sessionContextBackup)
                     {
                        SESSION_CONTEXT_HOLDER.set(sessionContextBackup);
                     }
                     else
                     {
                        SESSION_CONTEXT_HOLDER.remove();
                     }
                  }
               }
            }
         }
      }

      return resourceUrl;
   }

   public void resetCaches()
   {
      repositoryManagerCache.clear();
   }

   private static class DmsContentRepositoryManager extends AbstractDocumentServiceRepositoryManager
   {
      private final String partitionId;
      
      public DmsContentRepositoryManager(String partitionId)
      {
         this.partitionId = partitionId;
      }

      @Override
      protected DocumentManagementService getDocumentService()
      {
         SessionContext sessionContext = SESSION_CONTEXT_HOLDER.get();
         
         if ((null != sessionContext) && (null != sessionContext.getServiceFactory()))
         {
            return sessionContext.getServiceFactory().getDocumentManagementService();
         }
         else
         {
            return null;
         }
      }

      @Override
      protected String getPartitionId()
      {
         return partitionId;
      }
   }
}
