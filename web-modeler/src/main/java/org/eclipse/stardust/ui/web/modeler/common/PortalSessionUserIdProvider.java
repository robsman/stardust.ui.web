package org.eclipse.stardust.ui.web.modeler.common;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;

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
