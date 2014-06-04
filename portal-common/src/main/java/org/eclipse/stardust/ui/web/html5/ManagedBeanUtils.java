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
package org.eclipse.stardust.ui.web.html5;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public abstract class ManagedBeanUtils
{
   public static Object getManagedBean(String beanId)
   {
      return getManagedBean(FacesContext.getCurrentInstance(), beanId);
   }

   /**
    * 
    * @param context
    * @param beanId
    * @return
    */
   public static Object getManagedBean(FacesContext context, String beanId)
   {
      Object bean = null;
      if (FacesContext.getCurrentInstance() != null)
      {
         bean = context.getApplication().getVariableResolver().resolveVariable(context, beanId);
      }
      else
      {
         ServletContext servletContext = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
               .getRequest().getSession().getServletContext();
         ApplicationContext applicationContext = WebApplicationContextUtils
               .getRequiredWebApplicationContext(servletContext);
         bean = applicationContext.getBean(beanId);
      }
      return bean;
   }

   private ManagedBeanUtils()
   {}
}
