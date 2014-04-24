package org.eclipse.stardust.ui.web.rules_manager.common;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

public class DefaultServiceFactoryLocator implements ServiceFactoryLocator
{
   /**
   *
   */
  @Resource
  private SessionContext sessionContext;

  @Override
  public ServiceFactory get()
  {
     return sessionContext.getServiceFactory();
  }
}
