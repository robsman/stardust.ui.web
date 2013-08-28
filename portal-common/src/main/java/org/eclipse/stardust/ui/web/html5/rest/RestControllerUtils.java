package org.eclipse.stardust.ui.web.html5.rest;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class RestControllerUtils
{
   public static <T> T resolveSpringBean(Class<T> type, ServletContext servletContext)
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      return context.getBean(type);
   }
   
   public static Object resolveSpringBean(String beanId, ServletContext servletContext)
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      return context.getBean(beanId);
   }
}
