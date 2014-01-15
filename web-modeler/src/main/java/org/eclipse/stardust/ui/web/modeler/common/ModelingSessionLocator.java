package org.eclipse.stardust.ui.web.modeler.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Component
@Scope("singleton")
public class ModelingSessionLocator implements ApplicationContextAware
{
   private ApplicationContext springContext;

   @Override
   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.springContext = applicationContext;
   }

   public ModelingSession currentModelingSession()
   {
      ModelService modelingService = springContext.getBean(ModelService.class);

      // TODO handle non-resolvable model service
      ModelingSession currentSession = modelingService.currentSession();

      return currentSession;
   }
}
