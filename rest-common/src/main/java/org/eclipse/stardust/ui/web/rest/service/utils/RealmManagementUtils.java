package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.springframework.stereotype.Component;

@Component("RealmManagementUtilsREST")
public class RealmManagementUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @SuppressWarnings("unchecked")
   public List<UserRealm> getUserRealms()
   {
      return serviceFactoryUtils.getUserService().getUserRealms();
   }

   public UserRealm createRealm(String id, String name, String description)
   {
      UserService service = serviceFactoryUtils.getUserService();

      return service != null ? service.createUserRealm(id, name, description) : null;
   }

   public void deleteRealms(List<String> ids)
   {
      for (String id : ids)
      {
         serviceFactoryUtils.getUserService().dropUserRealm(id);
      }
   }
}
