package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.ui.web.rest.component.util.RealmManagementUtils;
import org.springframework.stereotype.Component;

@Component
public class RealmManagementService
{

   private static final Logger trace = LogManager.getLogger(RealmManagementService.class);

   @Resource
   private RealmManagementUtils realmManagementUtilsREST;

   public List<UserRealm> getUserRealms()
   {
      return realmManagementUtilsREST.getUserRealms();
   }

   public UserRealm createRealm(String id, String name, String description) throws Exception
   {
      try {
         return realmManagementUtilsREST.createRealm(id, name, description);   
      }
      catch (AccessForbiddenException e) 
      {
         trace.error(e.getMessage(), e);
         throw e;
      }
      catch (Exception e)
      {
         trace.error(e.getMessage(), e);
         throw e;
      }
   }

   public void deleteRealms(List<String> ids)
   {
      realmManagementUtilsREST.deleteRealms(ids);
   }
}
