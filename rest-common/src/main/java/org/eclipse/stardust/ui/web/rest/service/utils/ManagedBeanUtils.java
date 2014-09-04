/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * JSF managed bean utilities
 * 
 * @author rsauer
 * @author Yogesh.Manware
 * 
 */
public abstract class ManagedBeanUtils
{
   private final static Logger trace = LogManager.getLogger(ManagedBeanUtils.class);

   /**
    * 
    * @param context
    * @param beanId
    * @return
    */
   public static Object getManagedBean(String beanId)
   {
      Object bean = null;
      try
      {
         ServletContext servletContext = ((ServletRequestAttributes) RequestContextHolder
               .getRequestAttributes()).getRequest().getSession().getServletContext();
         ApplicationContext applicationContext = WebApplicationContextUtils
               .getRequiredWebApplicationContext(servletContext);
         bean = applicationContext.getBean(beanId);
      }
      catch (Throwable t)
      {
         trace.error("Failed to retrieve or initialize spring based bean...." + beanId
               + "ERROR: " + t.getMessage());
      }
      return bean;
   }

   private ManagedBeanUtils()
   {}
}
