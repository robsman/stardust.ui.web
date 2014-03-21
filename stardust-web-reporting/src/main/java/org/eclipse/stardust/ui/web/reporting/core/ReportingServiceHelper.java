package org.eclipse.stardust.ui.web.reporting.core;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.reporting.common.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportingServiceHelper
{
   @Resource
   private SessionContext sessionContext;

   @Resource
   private ServletContext servletContext;

   public ServiceFactory getServiceFactory()
   {
      return sessionContext.getServiceFactory();
   }

   public ModelCache getModelCache()
   {
      return ModelCache.getModelCache(sessionContext, servletContext);
   }
}
