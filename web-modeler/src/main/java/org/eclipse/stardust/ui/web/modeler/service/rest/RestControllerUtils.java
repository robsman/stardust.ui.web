package org.eclipse.stardust.ui.web.modeler.service.rest;

import org.springframework.context.ApplicationContext;

public class RestControllerUtils
{
   public static <T> T resolveSpringBean(Class<T> type, ApplicationContext context)
   {
      return context.getBean(type);
   }

   public static <T> Iterable<T> resolveSpringBeans(Class<T> type, ApplicationContext context)
   {
      return context.getBeansOfType(type).values();
   }
}
