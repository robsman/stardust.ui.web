package org.eclipse.stardust.ui.web.modeler.portal.integration;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;

@Component
@Scope("session")
public class PortalSessionUserIdProvider implements UserIdProvider
{
   @Resource
   private PortalApplication portalApp;

   @Override
   public String getCurrentUserId()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getUID() : null;
   }

   @Override
   public String getLoginName()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getLoginName() : null;
   }

   @Override
   public String getFirstName()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getFirstName() : null;
   }

   @Override
   public String getLastName()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getLastName() : null;
   }

   @Override
   public String getCurrentUserDisplayName()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getDisplayName() : null;
   }

   @Override
   public boolean isAdministrator()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().isAdministrator() : false;
   }
}
