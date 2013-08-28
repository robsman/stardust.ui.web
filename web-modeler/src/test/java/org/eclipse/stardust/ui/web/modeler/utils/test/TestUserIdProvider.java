package org.eclipse.stardust.ui.web.modeler.utils.test;

import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

public class TestUserIdProvider implements UserIdProvider
{
   @Override
   public String getCurrentUserId()
   {
      return "motu";
   }
}
