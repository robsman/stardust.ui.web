/*
 * $Id$
 * (C) 2000 - 2014 CARNOT AG
 */
package org.eclipse.stardust.ui.web.viewscommon.core;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.ui.web.viewscommon.beans.ApplicationContext;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;

public class RepositoryCache implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -4799713515326027141L;
   private Map<String, Object> repositoryMap = null;

   private static final String REPOSITORY_CACHE_ID = "repositoryCache";

   private RepositoryCache()
   {
      repositoryMap = CollectionUtils.newHashMap();
      reset();
   }

   private void reset()
   {
      repositoryMap.clear();
      List<IRepositoryInstanceInfo> repositoryInstance = DocumentMgmtUtility.getDocumentManagementService()
            .getRepositoryInstanceInfos();
      for (IRepositoryInstanceInfo repoInstance : repositoryInstance)
      {
         repositoryMap.put(repoInstance.getRepositoryId(), repoInstance);
      }
   }

   private static RepositoryCache getRepositoryCache()
   {
      ApplicationContext appContext = ApplicationContext.findApplicationContext();
      SessionContext sessionContext = SessionContext.findSessionContext();
      String partitionId = sessionContext.getUser().getPartitionId();
      @SuppressWarnings("unchecked")
      Map<String, RepositoryCache> repositoryCacheMap = (Map<String, RepositoryCache>) appContext
            .lookup(REPOSITORY_CACHE_ID);
      if (null == repositoryCacheMap)
      {
         repositoryCacheMap = CollectionUtils.newMap();
         appContext.bind(REPOSITORY_CACHE_ID, repositoryCacheMap);
      }
      if (repositoryCacheMap.containsKey(partitionId))
      {
         return repositoryCacheMap.get(partitionId);
      }
      else
      {
         RepositoryCache repositoryCache = new RepositoryCache();
         repositoryCacheMap.put(partitionId, repositoryCache);
         return repositoryCache;
      }
   }

   public static RepositoryCache findRepositoryCache()
   {
      RepositoryCache tempCache = null;
      SessionContext session = SessionContext.findSessionContext();
      if (session != null)
      {
         return getRepositoryCache();
      }
      return tempCache;
   }

   public Object getObject(String key)
   {
      Object value = repositoryMap.get(key);
      return value;
   }
}
