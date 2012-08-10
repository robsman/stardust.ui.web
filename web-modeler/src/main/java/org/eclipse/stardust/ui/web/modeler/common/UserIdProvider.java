package org.eclipse.stardust.ui.web.modeler.common;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;

@Component
@Scope("session")
public class UserIdProvider
{
   @Resource
   private PortalApplication portalApp;

   public String getCurrentUserId()
   {
      return (null != portalApp) ? portalApp.getLoggedInUser().getUID() : null;
   }
}
